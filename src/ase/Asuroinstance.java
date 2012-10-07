package ase;

import ase.outputs.IEventdrivenOutput;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import ase.geometrics.*;
import java.util.ArrayList;
import ase.hardware.AsuroNachricht.*;
import ase.hardware.AsuroNachricht;
import ase.datalayers.SzenarioParameters.unitOfLenght;

/**
 * <code>Asuroinstance</code> is the class which contains the sourcecode which is als executed on the asuro-cpu. <code>Asuroinstance</code> implements <code>Runnable</code> because its asynchron to <code>Asurohardware</code>
 * @author Tobias Sturm
 */
public class Asuroinstance implements Runnable, ase.outputs.IOutputEventCreator, ase.IThreadControl {

    private ase.hardware.Asurohardware myAsurohardware;
    private java.util.concurrent.ConcurrentHashMap<String, IEventdrivenOutput> myEventOutputs;
    //static values; same names like in the asurolibrary, which is on the asuro
    private static byte FALSE = 0;
    private static byte TRUE = 1;
    private static byte ON = 1;
    private static byte OFF = 0;
    private static byte GREEN = 1;
    private static byte RED = 2;
    private static byte YELLOW = 3;
    private static byte LEFT = 0;
    private static byte RIGHT = 1;
    private static byte CENTER = 2;
    private ase.World refWorld;
    private String instanceName;
    transient private Thread thisThread;
    ///Asurocode-Variabeln
    Point zielHaufenPosition;
    Point aktuellesZielObjekt;
    ArrayList<Point> flaschenpositionen = new ArrayList<Point>(); //?
    ArrayList<Point> scanpositionen = new ArrayList<Point>();
    Hashtable<String, Position> asuropositionen = new Hashtable<String, Position>();
    Hashtable<String, Integer> asuroUeberlebensliste = new Hashtable<String, Integer>();
    int messweite = 400; //entspricht objdist in mm
    int intZielhaufenGroesse = 0; //zählt mit, wieviele Dosen zum Zielhaufen egstellt wurden (auch von anderen Robotern)
    int intFlaschenHaufenGrenzwert = 150; //der Abstand in mm den zwei Flaschen maximal zueinander haben dürfen, damit sie als Haufen erkannt werden
    int intDosenUmfang = 40; //in mm
    int intZielGenauigkeit;

    ///Ende Asurocode-Variabeln
    /**
     * Creates an instance of <code>Asurocode</code> and starts it as an thread
     * @param name the name of the asuro
     * @param refHardware a reference to one <code>Asurohardware</code>
     */
    public Asuroinstance(String name, ase.hardware.Asurohardware refHardware, ase.World refWorld) {
        myAsurohardware = refHardware;
        myEventOutputs = new java.util.concurrent.ConcurrentHashMap<String, IEventdrivenOutput>();
        instanceName = name;

        this.refWorld = refWorld;

        thisThread = new Thread(this);
        thisThread.setName("Thread of asuroinstance " + name);
        thisThread.start();
    }

    /**
     * Tis method contains the asurosourcecode
     */
    @SuppressWarnings("empty-statement")
    public void run() {
        //Wait because of the bootloader on the asuro-cpu
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e) {
        }

        while (myAsurohardware != null) {
            notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[]{"starting asurosourcecode"});
            //Asurohardware sleeping time
            try {
                Thread.sleep(refWorld.getSleepTimeForAsurocode());
            } catch (InterruptedException e) {
            }

            //asuroinstance-software
            kommentar("starte Asurocode");
            Init();
            StatusLED(OFF);


            //Start Hauptprogramm --------------------------------------------------------------------------

            gruppenInitialisierung();
            notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind zielhaufen", (int) (zielHaufenPosition.x), (int) (zielHaufenPosition.y)});
//            //Findet neue Flaschen
            boolean bolScanErfolgreich = false;
            while (true) {
                kommentar("Beginne mit einem neuen Arbeitstag");
                verarbeiteKommunikationspuffer();
                bolScanErfolgreich = false;
                while (!bolScanErfolgreich) {
                    if (flaschenpositionen.size() > 0) {
                        notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Es befinden sich Flaschen in der Flaschenliste. Überspringe Scanvorgang", this});
                        bolScanErfolgreich = true;
                        kommentar("Es befinden sich Flaschen in der Warteliste");
                        break;
                    } else {
                        kommentar("Es befinden sich keine Flaschen in der Warteliste.");
                        notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Es befinden sich keine Flaschen mehr in der Flaschenliste. Starte Scanvorgang", this});
                    }

                    //Einen Scanversuch starten
                    bolScanErfolgreich = scan();
                    if (!bolScanErfolgreich) {
                        kommentar("suche neue Scanposition");
                        int intSleepTime = rand(2500, 15000);
                        notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[]{"suche neue Scanposition", intSleepTime, this});
                        SetMotorPower(-100, -100);
                        int intTmp = 0;
                        while (intTmp < intSleepTime) {
                            intTmp += 100;
                            Msleep(100);
                            kollisionsreaktion();
                            verarbeiteKommunikationspuffer();
                        }
                    } else {
                        break;
                    }
                }

                //nächste Flasch aus Liste wählen
                int intNaechsteFlasche = -1;
                double dGeringsterAbstand = Double.MAX_VALUE;
                double dTmp;
                for (int i = 0; i < flaschenpositionen.size(); i++) {
                    dTmp = Math.sqrt(Math.pow(flaschenpositionen.get(i).x - getGreiferposition().x, 2) + Math.pow(flaschenpositionen.get(i).y - getGreiferposition().y, 2));
                    if (dTmp < dGeringsterAbstand) {
                        intNaechsteFlasche = i;
                        dGeringsterAbstand = dTmp;
                    }
                }
                if (intNaechsteFlasche != -1) {
                    sendeNachricht(AsuroNachrichtBefehl.BeansprucheDoseFuerMich, flaschenpositionen.get(intNaechsteFlasche));
                    aktuellesZielObjekt = flaschenpositionen.get(intNaechsteFlasche);
                    flaschenpositionen.remove(intNaechsteFlasche);
                    verarbeiteKommunikationspuffer();
                    kommentar("Fahre Flasche an: " + aktuellesZielObjekt);
                    if (!routenPlaner(aktuellesZielObjekt,true)) {
                        //Dose konnte nicht angesteuert werden -> Dose wieder freigeben
                        sendeNachricht(AsuroNachrichtBefehl.NeueDoseGefundenBeiScan, aktuellesZielObjekt);
                        if (!istDosenpositionBereitsInListe(aktuellesZielObjekt)) {
                            flaschenpositionen.add(zielHaufenPosition);
                        }
                        kommentar("Flasche konnte nicht angesteuert werden :-( Gebe Flasche wieder für andere Frei und suche neue");
                    } else {
                        kommentar("habe Dose aufgenommen. Bringe sie jetzt zum Ziehlaufen");
                        //Dose konnte aufgenommen werden --> Zielhaufen ansteuern und Dose abstellen
                        kollisionsreaktion();
                        verarbeiteKommunikationspuffer();
                        intZielGenauigkeit = getZielhaufenradius();
                        if (navigiere(zielHaufenPosition)) {
                            kommentar("Zielhaufen erreicht");
//                            //Der zielhaufen wurde erreicht --> Abstellen beginnen
//                            while (getAbstand() > intFlaschenHaufenGrenzwert) {
//                                SetMotorPower(100, 100);
//                                Msleep(100);
//                            }
                            kommentar("Dose abstellen");
                            intZielhaufenGroesse++;
                            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"asuromind kreis", (int) (zielHaufenPosition.x), (int) (zielHaufenPosition.y), (int) (getZielhaufenradius() + heightVectorialAsMM().getNorm() + widthVectorialAsMM().getNorm()), java.awt.Color.RED, 30000, "Schutzzone"});
                            sendeNachricht(AsuroNachrichtBefehl.DoseZumZielhaufenGestellt, intZielhaufenGroesse);
                            SetMotorPower(-100, -100);
                            Msleep(3000);

                            kollisionsreaktion();
                            verarbeiteKommunikationspuffer();

                            //Vom Haufen wegdrehen
                            double gedreht = 0;
                            double lastDrehMessung = getEigenePosition().angle;
                            int intTimer = 0;
                            SetMotorPower(0, -100);
                            while (gedreht < 180) {
                                Msleep(200);
                                kollisionsreaktion();
                                verarbeiteKommunikationspuffer();
                                intTimer += 200;
                                if (intTimer > 10000) {
                                    kommentar("Konnte mich nicht vom Zielhaufen wegdrehen -> Zeitüberschreitung");
                                    break;
                                }
                                gedreht += Math.abs(lastDrehMessung - getEigenePosition().angle);
                                lastDrehMessung = getEigenePosition().angle;
                                if (lastDrehMessung > 360) {
                                    lastDrehMessung -= 360;
                                }

                            }
                            SetMotorPower(0, 0);
                        } else {
                            kommentar("Dose konnte nicht zum Zielhaufen gestellt werden! AAAAH mein Weltbild ist zerstört!");
                        }
                    }
                } else {
                    kommentar("Keine Flasche von meiner Position zu erreichen :-(");
                }
            }

            //veraltet:
//            //Start "Haufen bauen":
//            int j = 0; int abstand;
//            SetMotorPower(255,0);-
//            for(int k=0; k<5; k++){
//                abstand = getabstand();
//                if(abstand <= messweite){   //eigene Position + Infrarotstrahl --> Flaschenposition:
//                    //Flaschenpositionen[j] = myPosition.combine(Math.cos(myPosition.Drehung) * abstand, Math.sin(myPosition.Drehung) * abstand); //TODO steht 0° für "zeigt nach oben"???
//                }
//            }
//            int minabstand = 1000;
//            Point zielflasche;
//            for(Point Fla : Flaschenpositionen){
//                //abstand = Math.sqrt((myPosition.x - Fla.x)^2 + (myPosition.y - Fla.y)^2);
//            //    if(minabstand < abstand) minabstand = abstand;
//                zielflasche = Fla;
//            }
//            //navigiere(zielflasche, true);
//            //navigiere(haufenposition, false);
//            SetMotorPower(-255,-255); Msleep(1000);
//            //Nachricht.Befehl.DoseZumZielGestellt
//            //SendeNachricht(Asuronachricht.Asuronachrichtbefehl.  "Flasche abgestellt"
//            //zufällig drehen

            //Ende Hauptprogramm -------------------------------------------------------------------------
        }
    }

    // <editor-fold desc="Asurocode">
    /**
     * Diese Methode steuert den Asuro direkt zu einem Punkt. Dabei wird die Luftlinie eingeschlagen. Um  den Zielhaufen zu umfahren muss die Methode <code>routenPlaner</code> verwendet werden. <code>routenPlaner</code> ruft diese Methode auf.
     * @param Zielpunkt der Punkt (Angaben in mm), der <b>direkt</b> angesteuert werden soll
     * @return true falls sich der Asuro am Zielpunkt befindet, false wenn der Zielpunkt nicht erreicht werden konnte
     */
    private boolean navigiere(Point Zielpunkt) {
        Point differenzVektor = new Point(Zielpunkt.x - getGreiferposition().x, Zielpunkt.y - getGreiferposition().y);
        Point ankerDifferenzVektor = new Point(getEigenePosition().x - getGreiferposition().x, getEigenePosition().y - getGreiferposition().y);
        double drehWinkel = (heightVectorialAsMM().x * differenzVektor.x + heightVectorialAsMM().y * differenzVektor.y) / (heightVectorialAsMM().getNorm() * differenzVektor.getNorm());
        double lastDrehwinkel = drehWinkel;
        double drehwinkelabweichung = 0.9;
        double tmpRadius = (new Point(getEigenePosition().x - getGreiferposition().x, getEigenePosition().y - getGreiferposition().y)).getNorm();
        boolean clockwise = true;
        int intTimer = 0;
        int intTmpTimer = getSystemZeit();
        boolean fahreGeradeAus = false;

        notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind punkt", (int) (Zielpunkt.x), (int) (Zielpunkt.y), java.awt.Color.darkGray, 60000, "naviZiel"});

        do {
            notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind line", (int) (getGreiferposition().x), (int) (getGreiferposition().y), (int) (Zielpunkt.x), (int) (Zielpunkt.y), java.awt.Color.darkGray, 10000, "naviRoute"});
            differenzVektor = new Point(Zielpunkt.x - getGreiferposition().x, Zielpunkt.y - getGreiferposition().y);
            drehWinkel = (heightVectorialAsMM().x * differenzVektor.x + heightVectorialAsMM().y * differenzVektor.y) / (heightVectorialAsMM().getNorm() * differenzVektor.getNorm());
            lastDrehwinkel = drehWinkel;
            if (differenzVektor.getNorm() > 1000) {
                drehwinkelabweichung = 0.9;
            } else if (differenzVektor.getNorm() > 300) {
                drehwinkelabweichung = 0.99;
            } else {
                drehwinkelabweichung = 0.999;
            }

            while (drehWinkel < drehwinkelabweichung) {
                tmpRadius = (new Point(getEigenePosition().x - getGreiferposition().x, getEigenePosition().y - getGreiferposition().y)).getNorm();
                ankerDifferenzVektor = new Point(getEigenePosition().x - Zielpunkt.x, getEigenePosition().y - Zielpunkt.y);
                while (ankerDifferenzVektor.getNorm() < tmpRadius) {
                    ankerDifferenzVektor = new Point(getEigenePosition().x - Zielpunkt.x, getEigenePosition().y - Zielpunkt.y);
                    tmpRadius = (new Point(getEigenePosition().x - getGreiferposition().x, getEigenePosition().y - getGreiferposition().y)).getNorm();
                    fahreGeradeAus = false;
                    if (drehWinkel < 0) {
                        SetMotorPower(100, 100);
                        Msleep(1000);
                    } else {
                        SetMotorPower(-100, 1);
                        Msleep(500);
                        SetMotorPower(1, -100);
                        Msleep(500);
                    }

                    kollisionsreaktion();
                    verarbeiteKommunikationspuffer();

                    intTimer = getSystemZeit() - intTmpTimer;
                    intTmpTimer = getSystemZeit();
                    if (intTimer > 60000) {
                        break;
                    }
                }


                differenzVektor = new Point(Zielpunkt.x - getGreiferposition().x, Zielpunkt.y - getGreiferposition().y);
                lastDrehwinkel = drehWinkel;
                drehWinkel = (heightVectorialAsMM().x * differenzVektor.x + heightVectorialAsMM().y * differenzVektor.y) / (heightVectorialAsMM().getNorm() * differenzVektor.getNorm());

                kollisionsreaktion();
                            verarbeiteKommunikationspuffer();
                //Richtung korrigieren, wenn sich das Asuro vom Ziel weg bewegt
                if (lastDrehwinkel > drehWinkel) {
                    if (clockwise) {
                        clockwise = false;
                    } else {
                        clockwise = true;
                    }
                }

                if (clockwise) {
                    SetMotorPower(0, 100);
                    fahreGeradeAus = false;
                } else {
                    SetMotorPower(100, 0);
                    fahreGeradeAus = false;
                }


                Msleep(100);
                if (differenzVektor.getNorm() <= intZielGenauigkeit) {
                    break;
                }
                intTimer = getSystemZeit() - intTmpTimer;
                intTmpTimer = getSystemZeit();
                if (intTimer > 60000) {
                    break;
                }
            }
            Msleep(100);
            if (!fahreGeradeAus) {
                SetMotorPower(100, 100);
                fahreGeradeAus = true;
            }
            kollisionsreaktion();
            verarbeiteKommunikationspuffer();

            intTimer = getSystemZeit() - intTmpTimer;
            intTmpTimer = getSystemZeit();
            //notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new String[]{"Abstand zum Ziel: " + differenzVektor.getNorm()});
        } while ((differenzVektor.getNorm() > intZielGenauigkeit) && (intTimer < 60000));

        SetMotorPower(0, 0);
        //TODO Rechts vor Links
        return true;
    }

    /**
     * Navigiert den Asuro um den Zielhaufen herum zum gegebenen Zielpunkt
     * @param Zielpunkt der Punkt der direkt oder indirekt angefahren werden soll.
     * @return true wenn der Zielpunkt erreicht wurde und false, wenn das Manöver abgebrochen wurde
     */
    private boolean routenPlaner(Point Zielpunkt, boolean bolIstDose) {
        if (bewegungZulaessig(getGreiferposition(), Zielpunkt)) {
            //direkt zu dem Punkt fahren
            notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Kann ohne Umwege zum Zielpunkt fahren", Zielpunkt});
            kommentar("Kann ohne Umwege zum Zielpunkt fahren");
            verarbeiteKommunikationspuffer();
            if (bolIstDose) {
                intZielGenauigkeit = (int) (intDosenUmfang / 2.2);
            } else {
                intZielGenauigkeit = getZielhaufenradius();
            }
            return navigiere(Zielpunkt);
        } else {

            //Mit Route um den Zielhaufen herum berechnen
            verarbeiteKommunikationspuffer();
            notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Kann nicht ohne Umwege zum Zielpunkt fahren, berechne Route", Zielpunkt});
            kommentar("Kann nicht ohne Umwege zum Zielpunkt fahren, berechne Route");

            Point ausgangsPunkt = getGreiferposition();
            Point tmpPunkt;
            ArrayList<Point> wegPunkte = new ArrayList<Point>();
            double dAusgangsrehung;
            double dDiffDrehung = 0;
            boolean bolAbbruch = false;
            while (!bewegungZulaessig(ausgangsPunkt, Zielpunkt)) {
                bolAbbruch = false;
                dDiffDrehung = 0;
                dAusgangsrehung = new Point(Zielpunkt.x - ausgangsPunkt.x, Zielpunkt.y - ausgangsPunkt.y).getAngle() - heightVectorialAsMM().getAngle() + heightVectorialAsMM().getAngle() + 90;
                tmpPunkt = new Point((heightVectorialAsMM().getNorm() + widthVectorialAsMM().getNorm() + getZielhaufenradius()) * Math.cos(Math.toRadians(dAusgangsrehung + dDiffDrehung - 90)) + ausgangsPunkt.x, (heightVectorialAsMM().getNorm() + widthVectorialAsMM().getNorm() + getZielhaufenradius()) * Math.sin(Math.toRadians(dAusgangsrehung + dDiffDrehung - 90)) + ausgangsPunkt.y);

                while ((Math.sqrt(Math.pow(tmpPunkt.x - zielHaufenPosition.x, 2) + Math.pow(tmpPunkt.y - zielHaufenPosition.y, 2)) < (getZielhaufenradius() + heightVectorialAsMM().getNorm() + widthVectorialAsMM().getNorm())) && !bolAbbruch) {
                    dDiffDrehung += 5;
                    tmpPunkt = new Point((heightVectorialAsMM().x + widthVectorialAsMM().x) * Math.cos(Math.toRadians(dAusgangsrehung + dDiffDrehung)) + ausgangsPunkt.x + getZielhaufenradius(), (heightVectorialAsMM().y + widthVectorialAsMM().y) * Math.sin(Math.toRadians(dAusgangsrehung + dDiffDrehung)) + ausgangsPunkt.y + getZielhaufenradius());
                    if (dDiffDrehung > 180) {
                        bolAbbruch = true;
                        kommentar("Winkel > 90° -> konnte keinen geigneten Zwischenwegpunkt finden :-( Breche Routenplanung ab.");
                        break;
                    }



                }

                if (bolAbbruch) {
                    break;
                }

                wegPunkte.add(tmpPunkt);
                ausgangsPunkt = tmpPunkt;
            }
            wegPunkte.add(Zielpunkt);


            notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind line", (int) (getGreiferposition().x), (int) (getGreiferposition().y), (int) (Zielpunkt.x), (int) (Zielpunkt.y), java.awt.Color.darkGray, 15000, "Luftlinie"});
            if (wegPunkte.size() > 1) {
                for (int i = 1; i < wegPunkte.size(); i++) {
                    notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind punkt", (int) (wegPunkte.get(i).x), (int) (wegPunkte.get(i).y), java.awt.Color.blue, 50000, "wegpunkt"});
                    notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind punkt", (int) (wegPunkte.get(i).x), (int) (wegPunkte.get(i).y), (int) (wegPunkte.get(i - 1).x), (int) (wegPunkte.get(i - 1).y), java.awt.Color.blue, 50000, "weg"});
                }
            }

            if (bolAbbruch) {
                return false;
            }

            boolean bolRouteAbgefahren = true;
            for (int i = 0; i < wegPunkte.size(); i++) {
                if (bolIstDose) {
                    intZielGenauigkeit = (int) (intDosenUmfang / 2.2);
                } else {
                    intZielGenauigkeit = getZielhaufenradius();
                }
                if (!navigiere(wegPunkte.get(i))) {
                    bolRouteAbgefahren = false;
                    break;
                }
            }
            return bolRouteAbgefahren;
        }
    }

    /**
     * Gibt den geschätzten Radius des Zielhaufens zurück. Der Radius ist abhängig von der Anzahl der Flaschen beim zielhaufen.
     * Sind keine dosen beim zielhaufen, wird <code>intDosenUmfang</code> zurück gegeben.
     * @return die erwartete Größe des Zielhaufens in mm, oder <code>intDosenUmfang</code>
     */
    private int getZielhaufenradius() {
        if (intZielhaufenGroesse == 0) {
            return intDosenUmfang;
        }
        return (int) (intDosenUmfang * Math.sqrt(2) * (intZielhaufenGroesse));
    }

    private boolean bewegungZulaessig(Point from, Point to) {
        if (intZielhaufenGroesse == 0) {
            return true;
        }

        int i = 0;
        Point bewegung = new Point(to.x - from.x, to.y - from.y);
        Point checkPoint = new Point();
        do {
            i++;
            checkPoint = new Point(getGreiferposition().x + bewegung.getUnitVector().x * heightVectorialAsMM().getNorm() / 4.0 * i, getGreiferposition().y + bewegung.getUnitVector().y * heightVectorialAsMM().getNorm() / 4.0 * i);
            if (Math.sqrt(Math.pow(zielHaufenPosition.x - checkPoint.x, 2) + Math.pow(zielHaufenPosition.y - checkPoint.y, 2)) < (getZielhaufenradius() + heightVectorialAsMM().getNorm())) {
                notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"Die Bewegung war nicht zulässig, da sie den Zielhaufen gefährdet", bewegung});
                return false;
            }
        } while ((new Point(checkPoint.x - from.x, checkPoint.y - from.y)).getNorm() < bewegung.getNorm());
        notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"Die Bewegung ist zulässig", bewegung});

        return true;
    }

    private void kollisionsreaktion() {
        if (PollSwitch() != (byte) 0) {
            kommentar("kollidiere! Hilfe Hilfe!");
            notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind point", (int) (getGreiferposition().x), (int) (getGreiferposition().y), java.awt.Color.pink, 5000, "Kollision"});
            SetMotorPower(0, 0);
            if (PollSwitch() < (byte) 8) {
                notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Kollision an der rechten Frontseite", this});
                SetMotorPower(-100, -100);
                Msleep(250);
                SetMotorPower(-50, 100);
                Msleep(rand(1800, 3000));
            } else {
                notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Kollision an der linken Frontseite", this});
                SetMotorPower(-100, -100);
                Msleep(250);
                SetMotorPower(100, -50);
                Msleep(rand(1800, 3000));
            }
        }
    }

    /**
     * Führt einen Scan (an der aktuellen Position) durch, neu erkannte Dosen werden dabei in die Dosenliste eingetragen und anderen Robotern mitgeteilt. Der Asuro teilt den anderen auch mit, dass er einen Scan durchgeführt hat, aber nur wenn dieser erfolgreich verlaufen ist
     * @return true wenn ein erfolgreicher Scan durchgeführt wurde (360° Drehung), false wenn nicht (z.B. gegen die Bande gestoßen) in diesem Fall soll der Asuro zu einer anderen Stelle navigieren und dort einen neuen Scan durchführen
     */
    private boolean scan() {
        kommentar("Scanne von aktueller Position aus");
        notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind addscan", (int) (getEigenePositionAlsLE().x), (int)(getEigenePositionAlsLE().y)});
        notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[]{"Beginne Scan", getEigenePosition()});
        double dGedreht = 0;
        double dLastMeasure = 0;
        int systemTicks = 0;
        boolean bolScanErfolgreich = true;
        SetMotorPower(100, 0);
        do {
            dGedreht += Math.abs(dLastMeasure - getEigenePosition().angle);
            dLastMeasure = dGedreht;
            if (dLastMeasure > 360) {
                dLastMeasure -= 360;
            }

            if (getAbstand() <= messweite) {
                notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind punkt", (int) (getPositionDesGemessenenObjektes().x), (int) (getPositionDesGemessenenObjektes().y), java.awt.Color.GREEN, 5000});
                if (!istDosenpositionBereitsInListe(getPositionDesGemessenenObjektes())) {
                    notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[]{"neue Dosen gefunden", getPositionDesGemessenenObjektes()});
                    sendeNachricht(AsuroNachrichtBefehl.NeueDoseGefundenBeiScan, getPositionDesGemessenenObjektes());
                    flaschenpositionen.add(getPositionDesGemessenenObjektes());
                }
            }
            Msleep(100);
            systemTicks += 100;
            if (systemTicks > 25000) {
                bolScanErfolgreich = false;
                kommentar("Der Scan hat länger gedauert als 25 Sekunden. Anscheinend bleibe ich hängen.");
            }
        } while (dGedreht < 360 && bolScanErfolgreich);
        SetMotorPower(0, 0);
        verarbeiteKommunikationspuffer();
        if (bolScanErfolgreich) {
            sendeNachricht(AsuroNachrichtBefehl.ScanAnPosition, getEigenePosition());
            kommentar("Der Scan wurde erfolgreich abgeschlossen");
        } else {
            notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[]{"Scan nicht erfolgreich", getEigenePosition()});
        }

        return bolScanErfolgreich;
    }

    /**
     * Verarbeitet den Kommunikationspuffer, sollte ständig aufgerufen werden.
     * Wenn ein Asuro länger als 10 Sekunden nichts gesendet hat, wird er als tot markiert, d.h. er wird aus allen listen gelöscht
     */
    private void verarbeiteKommunikationspuffer() {
        if (getKommunikationsBufferSize() > 0) {
            notifyMonitors(IEventdrivenOutput.messageType.ACTION, new String[]{"verarbeite Kommunikationsbuffer", getKommunikationsBufferSize() + " anstehende Nachrichten"});
            ase.hardware.AsuroNachricht[] tmpPuffer = getKommunikationsBuffer(true);

            for (int i = 0; i < tmpPuffer.length; i++) {
                //Asuroueberlebensliste aktualisieren --> durch diese Nachricht wird der Sender als überlebender markiert
                asuroUeberlebensliste.put(tmpPuffer[i].getAsuroID(), getSystemZeit());

                //wenn ein anderer Asuro einen Scan durchführt
                if (tmpPuffer[i].getBefehl().equals(ase.hardware.AsuroNachricht.AsuroNachrichtBefehl.ScanAnPosition)) {
                    if (tmpPuffer[i].isParameterPoint()) {
                        scanpositionen.add(tmpPuffer[i].getParameterAsPoint());
                        notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[]{"Scanposition eines anderen Asuros verarbeitet", tmpPuffer[i]});
                    } else {
                        notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"Benachrichtigung eines anderen Asuros einer Scanposition war fehlerhaft", tmpPuffer[i]});
                    }
                }

                //wenn ein anderer Asuro eine Dose aufnimmt
                if (tmpPuffer[i].getBefehl().equals(ase.hardware.AsuroNachricht.AsuroNachrichtBefehl.BeansprucheDoseFuerMich)) {
                    if (tmpPuffer[i].isParameterPoint()) {
                        for (Point current : flaschenpositionen) {
                            if (Math.sqrt(Math.pow(current.x - tmpPuffer[i].getParameterAsPoint().x, 2) + Math.pow(current.y - tmpPuffer[i].getParameterAsPoint().y, 2)) < intFlaschenHaufenGrenzwert / 2.0)//wenn die beiden Flaschen einen Abstand von weniger als dem halben maximalAbstand zweier Haufenflaschen zueinander haben, handelt es sich um ein und das selbe Objekt
                            {
                                flaschenpositionen.remove(current);
                                break;
                            }
                        }

                        notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[]{"Anderer Asuro hat Dose aufgenommen, lösche diese Dose aus meiner Dosenliste", tmpPuffer[i]});
                    } else {
                        notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"Benachrichtigung eines anderen Asuros einer Dosenaufnahme war fehlerhaft", tmpPuffer[i]});
                    }
                }

                //wenn ein anderer Asuro eine Dose zum Haufen gestellt hat
                if (tmpPuffer[i].getBefehl().equals(ase.hardware.AsuroNachricht.AsuroNachrichtBefehl.DoseZumZielhaufenGestellt)) {
                    intZielhaufenGroesse++;
                    intZielGenauigkeit = getZielhaufenradius();
                    notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asuromind kreis", (int) (zielHaufenPosition.x), (int) (zielHaufenPosition.y), (int) (getZielhaufenradius() + heightVectorialAsMM().getNorm() + widthVectorialAsMM().getNorm()), java.awt.Color.RED, 30000, "Schutzzone"});
                    notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[]{"Anderer Asuro hat Dose zum Zielhaufen gestellt. Meine Zielhaufengroesse beträgt ", intZielhaufenGroesse});
                }

                //Positionsangaben der anderen Asuros verarbeiten
                if (tmpPuffer[i].getBefehl().equals(ase.hardware.AsuroNachricht.AsuroNachrichtBefehl.EigeneAktuellePosition)) {
                    if (tmpPuffer[i].isParameterPosition()) {
                        asuropositionen.put(tmpPuffer[i].getAsuroID(), tmpPuffer[i].getParameterAsPosition());
                        notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[]{"Benachrichtigung eines anderen Asuros seiner eigenen Position erfolgreich verarbeitet", tmpPuffer[i].getParameterAsPosition()});
                    } else {
                        notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"Benachrichtigung eines anderen Asuros seiner eigenen Position war fehlerhaft", tmpPuffer[i]});
                    }
                }

                if (tmpPuffer[i].getBefehl().equals(ase.hardware.AsuroNachricht.AsuroNachrichtBefehl.NeueDoseGefundenBeiScan)) {
                    if (tmpPuffer[i].isParameterPoint()) {
                        if (istDosenpositionBereitsInListe(tmpPuffer[i].getParameterAsPoint())) {
                            notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[]{"Benachrichtigung eines anderen Asuros einer neuen Dosenposition erfolgreich verarbeitet, Dose war bereits bekannt", tmpPuffer[i].getParameterAsPoint()});
                        } else {
                            flaschenpositionen.add(tmpPuffer[i].getParameterAsPoint());
                            notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[]{"Benachrichtigung eines anderen Asuros einer neuen Dosenposition erfolgreich verarbeitet, neue Dose hinzugefügt", tmpPuffer[i].getParameterAsPoint()});
                        }
                    } else {
                        notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"Benachrichtigung eines anderen Asuros einer Dosenposition war fehlerhaft", tmpPuffer[i]});
                    }
                }
            }
        } else // wenn der Kommunikationsbuffer leer ist
        {
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new String[]{"keine zu verarbeitenden Nachrichten"});
        }

        //System.out.println("JETZT:" + instanceName);
        //Asuros, die länger als 10 Sekunden nichts gesendet haben als tot markieren
        for (String strCurrentID : asuroUeberlebensliste.keySet()) {
            //  System.out.println(instanceName + " sagt " + ( getSystemZeit() - asuroUeberlebensliste.get(strCurrentID)));
            if ((getSystemZeit() - asuroUeberlebensliste.get(strCurrentID)) > 10000) {
                asuroUeberlebensliste.remove(strCurrentID);
                asuropositionen.remove(strCurrentID);
                notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Ihr Schweine ihr habt Asuro getötet!", strCurrentID});
            }
        }

        //Dosen- und Asuropositionen an asuoMindOutput senden
        Object[] tmp = new Object[flaschenpositionen.size() + 1];
        tmp[0] = "asuromind dosenpositionen";
        for (int i = 0; i < flaschenpositionen.size(); i++) {
            tmp[i + 1] = flaschenpositionen.get(i);
        }
        notifyMonitors(IEventdrivenOutput.messageType.OTHERS, tmp);

        tmp = new Object[asuropositionen.size() + 1];
        tmp[0] = "asuromind asuropositionen ";
        for (int i = 0; i < asuropositionen.size(); i++) {
            tmp[i + 1] = asuropositionen.get(i);
        }
        notifyMonitors(IEventdrivenOutput.messageType.OTHERS, tmp);
    }

    /**
     * Prüft ob die angegebene Dosenposition bereits in der Liste vorhanden ist. Der Abstand des gegebenen Punktes wird zu jeder bekannten Falschenposition ermittelt. überschreitet dieser abstand den Umfang einer Dose, so handelt es sich nicht um die selbe flasche
     * @param zuPruefendePosition eine neue Dosenposition, die evtl. zur Dosenliste hinzugefügt werden soll
     * @return Dose bereits in Liste (true) | Dose nicht in Liste (false)
     */
    private boolean istDosenpositionBereitsInListe(Point zuPruefendePosition) {
        for (Point current : flaschenpositionen) {
            if (Math.sqrt(Math.pow(current.x - zuPruefendePosition.x, 2) + Math.pow(current.y - zuPruefendePosition.y, 2)) < intDosenUmfang) {
                notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Dosenposition war bereits in Liste enthalten", zuPruefendePosition});
                kommentar("Die gefundene Dose (" + zuPruefendePosition + ") war bereits in der internen Dosenliste");
                return true;
            }
        }
        notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Dosenposition war nicht in Liste enthalten", zuPruefendePosition});
        kommentar("Die gefundene Dose (" + zuPruefendePosition + ") war nicht in der internen Dosenliste und wurde jetzt hinzugefügt");
        return false;
    }

    /**
     * Legt gemeinsam mit anderen Asuros die Zielhaufenposition fest, indem alle Asuros den Schwerpunkt der Startpositionen senden.
     */
    private void gruppenInitialisierung() {
        //Eigene Position senden und 5 Sekunden warten
        notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[]{"Lege mit allen andern Asuros die Position des Zielhaufens fest. Meine Startposition ist", getEigenePosition()});
        kommentar("Sende eigene Startposition: " + getEigenePosition() + " und warte 5 Sekunden.");
        sendeNachricht(AsuroNachrichtBefehl.EigeneStartposition, getEigenePosition());
        Msleep(5000);

        //wenn keine Nachrichten nach 5 Sekunden vorliegen
        if (getKommunikationsBufferSize() == 0) {
            notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Keine anderen Startpositionen empfangen. Bin alleine auf der Welt und muss die schwere Last alleine tragen :'-(. An dieser Stelle werde ich einen Flaschenhaufen errichten", getEigenePosition()});
            zielHaufenPosition = getEigenePosition().toPoint();
            kommentar("keine Nachricht eines anderen Asuros gefunden. Lege eigene Startposition als zielHaufenPosition fest.");
        } else //Nachrichten von anderen Asuros empfangen
        {
            boolean bolPositionFestgelegt = false;
            //als erstes Prüfen, ob bereits ein Zielhaufenpunkt festgelegt wurde
            for (int i = 0; i < getKommunikationsBuffer(false).length; i++) {
                if (getKommunikationsBuffer(false)[i].getBefehl().equals(ase.hardware.AsuroNachricht.AsuroNachrichtBefehl.MittelPunktFestgelegt) && getKommunikationsBuffer(false)[i].isParameterPoint()) {
                    zielHaufenPosition = getKommunikationsBuffer(false)[i].getParameterAsPoint();
                    notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Zielhaufenposition bereits festgelegt worden", getKommunikationsBuffer(false)[i].getParameterAsPoint()});
                    bolPositionFestgelegt = true;
                    kommentar("ZielhaufenPosition wurde bereits festgelegt: " + zielHaufenPosition.toString());
                }
            }

            Hashtable<String, Integer> anzahlEmpfangenerPositionen = new Hashtable<String, Integer>(); //Anzahl empfangener Positionen pro Asuro
            Hashtable<String, Point> empfangenePositionen = new Hashtable<String, Point>(); //Pro asuro die zuletzt empfanene Position
            ase.hardware.AsuroNachricht[] tmpPuffer;
            //auf andere Positionsangaben warten, bis jeder Asuro zwei mal seine Position gesendet hat, dann Mittelpunkt errechnen
            while (!bolPositionFestgelegt) {
                //notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[] {"Meine Startposition ist ", getEigenePosition()});
                sendeNachricht(AsuroNachrichtBefehl.EigeneStartposition, getEigenePosition());
                kommentar("ZielhaufenPosition wird gemeinsam bestimmt: Sende eigene Position");
                if (getKommunikationsBufferSize() > 0) {
                    tmpPuffer = getKommunikationsBuffer(true);
                    //zuert prüfen ob Zielhaufenposition bereits festgelegt wurde
                    for (int i = 0; i < tmpPuffer.length; i++) {
                        if (tmpPuffer[i].getBefehl().equals(ase.hardware.AsuroNachricht.AsuroNachrichtBefehl.MittelPunktFestgelegt) && tmpPuffer[i].isParameterPoint()) {
                            zielHaufenPosition = tmpPuffer[i].getParameterAsPoint();
                            notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"Zielhaufenposition bereits festgelegt worden", tmpPuffer[i].getParameterAsPoint()});
                            bolPositionFestgelegt = true;
                            kommentar("ZielhaufenPosition wurde bereits festgelegt: " + zielHaufenPosition.toString());
                        }
                    }

                    //wenn noch nicht festgelegt alle Nachrichten verarbeiten
                    if (!bolPositionFestgelegt) {
                        for (int i = 0; i < tmpPuffer.length; i++) {
                            if (tmpPuffer[i].getBefehl().equals(ase.hardware.AsuroNachricht.AsuroNachrichtBefehl.EigeneStartposition) && tmpPuffer[i].isParameterPosition()) {
                                //Anzahl der empfangenen Nachrichten dieses Roboters hochzählen
                                if (anzahlEmpfangenerPositionen.containsKey(tmpPuffer[i].getAsuroID())) {
                                    anzahlEmpfangenerPositionen.put(tmpPuffer[i].getAsuroID(), anzahlEmpfangenerPositionen.get(tmpPuffer[i].getAsuroID()) + 1);
                                } else {
                                    anzahlEmpfangenerPositionen.put(tmpPuffer[i].getAsuroID(), 1);
                                }

                                kommentar("Habe Position (" + tmpPuffer[i].getParameterAsPosition().toPoint().toString() + ") von Asuro " + tmpPuffer[i].getAsuroID() + " empfangen");
                                //Empfangene Position dieses Asuros speichern
                                empfangenePositionen.put(tmpPuffer[i].getAsuroID(), tmpPuffer[i].getParameterAsPosition().toPoint());
                            }
                        }
                    }
                }
                //Prüfen ob von jedem roboter mindestens 2 Startpositionen empfangen wurden
                boolean bolGenugStartpositionenEmpfangen = true;
                for (String strCurrentID : anzahlEmpfangenerPositionen.keySet()) {
                    if (anzahlEmpfangenerPositionen.get(strCurrentID) < 2) {
                        bolGenugStartpositionenEmpfangen = false;
                    }
                }

                if (bolGenugStartpositionenEmpfangen) {
                    kommentar("Habe genügend Startpositionen emfpangen, lege nun Zielhaufenposition fest");
                    //Es sind genügend Startpositionen vorhanden --> Mittelpunkt errechnen & senden
                    Point tmp = new Point();
                    for (String strCurrentID : empfangenePositionen.keySet()) {
                        tmp.x += empfangenePositionen.get(strCurrentID).x;
                        tmp.y += empfangenePositionen.get(strCurrentID).y;
                    }
                    //Eigene Position mit einbeziehen
                    tmp.x += getEigenePosition().x;
                    tmp.y += getEigenePosition().y;


                    tmp.x = Math.round(tmp.x / (empfangenePositionen.size() + 1));
                    tmp.y = Math.round(tmp.y / (empfangenePositionen.size() + 1));
                    bolPositionFestgelegt = true;
                    sendeNachricht(AsuroNachrichtBefehl.MittelPunktFestgelegt, tmp);
                    zielHaufenPosition = tmp;
                    notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"habe Zielhaufenposition festgelegt", tmp});
                    kommentar("Habe ZielHaufenPosition festgelegt auf: " + tmp);
                }
                Msleep(5000);
            }
        }
    }

    /**
     * Gibt zufällig 1 oder 0 zurück
     * @return 1 oder 0
     */
    private int rand() {
        return rand(0, 1);
    }

    /**
     * Gibt eine zufällige Zahl im angegebenen Wertebereich zurück
     * @param min minimal mögliche Zahl
     * @param max maximal mögliche Zahl
     * @return eine Zufällige Zahl im geschlossenen intervall von min bis max
     */
    private int rand(int min, int max) {
        return (int) (Math.random() * max + 1) + min;
    }

    /**
     * Gibt die Position des Objektes, das gerade gemessen wird, als Point zurück
     * @return die Position als Point des Objektes, dass gerade vom Abstandssensor gemessen wird
     */
    private Point getPositionDesGemessenenObjektes() {
        return getPositionDesGemessenenObjektes(getAbstand());
    }

    /**
     * Gibt die Position eines Objektes zurück, dass sich im gegebenen Abstand zum Asuro befindet
     * @param intDistanzMesswert die Entfernung des Abstandsensors zum Objekt in mm, dessen Position ermittelt werden soll
     * @return die Position als Point des Objektes, dass sich im gegebenen Abstand zum Asuro befindet
     */
    private Point getPositionDesGemessenenObjektes(int intDistanzMesswert) {
        Point tmp = new Point(widthVectorial().x * 0.5 + getEigenePositionAlsLE().x + heightVectorial().x + refWorld.convertMetric(intDistanzMesswert, unitOfLenght.mm, unitOfLenght.LE) * heightVectorial().getUnitVector().x, heightVectorial().y + getEigenePositionAlsLE().y + refWorld.convertMetric(intDistanzMesswert, unitOfLenght.mm, unitOfLenght.LE) * heightVectorial().getUnitVector().y + widthVectorial().y * 0.5);
        return new Point(refWorld.convertMetric(tmp.x, unitOfLenght.LE, unitOfLenght.mm), refWorld.convertMetric(tmp.y, unitOfLenght.LE, unitOfLenght.mm));
    }

    /**
     * Kommentar aus der Asurosoftware. Wird vom DavidOutput ausgegeben werden
     * @param myKommentar das Kommentar, das ausgegben werden soll
     */
    private void kommentar(String myKommentar) {
        notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[]{"asurocode comment", myKommentar});
    }

    /**
     * Gibt eine Debug-Benachrichtigung an registrierte Outputklassen weiter
     * @param strDebug
     */
    private void Debug(String strDebug) {
        notifyMonitors(IEventdrivenOutput.messageType.ASUROSOFTWAREDEBUG, new Object[]{"asurocode debug", strDebug});
    }

    /**
     * An asurolibrary method; !!!at the moment this class always returns te maximum battery level (1023)
     * @return returns the current battery level: max is 1023
     */
    private int Battery() {
        /* Info aus der AsuroLibrary
         * Rückgabe:
        10-Bit-Wert der Batteriespannung (Bereich 0..1023)
        Die Spannung in Volt kann mit folgende Formel berechnet werden:
        Umess[V] = (Uref / 2 ^ ADC-Bitsanzahl) * Batterie ()
        Ubat[V] = ((R1 + R2) * Umess) / R2
        Dabei sind:
        Uref = 2.56 Volt
        ADC-Bitsanzahl = 10 Bit
        R1 = 12000 Ohm auf der ASURO-Platine
        R2 = 10000 Ohm auf der ASURO-Platine
        Oder einfach:
        Ubat[V] = 0,0055 * Battery ()
         */
        notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[]{"requesting Batterylevel", 1023});
        return 1023;
    }

    /**
     * An asurolibrary method; Changels the state of the statusLED
     * @param color Color of the statusLED
     */
    private void StatusLED(byte color) {
        notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE, new Object[]{"changing state of the status-led", color});
        myAsurohardware.setStatusLed(color);
    }

    /**
     * starts the odometry-method and resets values
     */
    private void EncoderInit() {
        myAsurohardware.EncoderInit();
    }

    /**
     * stops the odometry-method
     */
    private void EncoderStop() {
        myAsurohardware.EncoderStop();
    }

    /**
     * An asurolibrary method; Changes the state of the BackLED
     * @param left the state for the left LED
     * @param right the state for the right LED
     */
    private void BackLED(byte left, byte right) {
        notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE, new Object[]{"changing state of back-leds", left, right});
        myAsurohardware.setBackLedLeft(left);
        myAsurohardware.setBackLedRight(right);
    }

    /**
     * An asurolibrary method; Initualizes the CPU
     */
    private void Init() {
        //implement Init
        notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[]{"init asurolibrary"});
        myAsurohardware.init();
    }

    /**
     * An asurolibrary method; waits for a specific time
     * @param dauer sleeping time in ms
     */
    private void Msleep(int dauer) {
        try {
            Thread.sleep(dauer);
            notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[]{"sleep", dauer});
        } catch (InterruptedException e) {
            notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"error while sleep", dauer, e});
        }
    }

    /**
     * An asurolibrary method; sets the speed and direction of the motors. To rewind the motor direction, the speed must be a negative number.
     * @param leftPower the speed of the left motor (-255 .. 255)
     * @param rightPower the speed of the right motor (-255 .. 255)
     */
    private void SetMotorPower(int leftPower, int rightPower) {
        if (leftPower >= -255 && leftPower <= 255 && rightPower >= -255 && rightPower <= 255) {
            myAsurohardware.setMotorLeft(leftPower);
            myAsurohardware.setMotorRight(rightPower);
            notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE, new Object[]{"set motorpower", leftPower, rightPower});
        } else {
            notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"invaild motorpower", leftPower, rightPower});
        }

    }

    /**
     * An asurolibrary method; Generates a sound with the motors
     * @param frequenz the frequence of the sound
     * @param msDauer the duration of the sound
     * @param amplitude the amplitude
     */
    private void Sound(int frequenz, int msDauer, int amplitude) {
        notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[]{"sound", frequenz, msDauer, amplitude});
    }

    /**
     * An asurolibrary function; Returns the state of the pollswiches at the fromt of the asuro;
     * First pollswich on: +1
     * Second pollswich on: +2
     * thrid pollswich on: +4
     * fourth pollswich on: +8
     * fivth pollswich on: +16
     * sixt pollswich on: + 32
     * when you look from the top on the asuro and the pollswiches are upwards, the first pollswich is the right one
     * @return the sum of the pressed pollswiches
     */
    private byte PollSwitch() {
        byte value = myAsurohardware.PollSwitch();
        notifyMonitors(IEventdrivenOutput.messageType.DESCISIONANDANSWER, new Object[]{"pollswich", value});
        return value;
    }

    /**
     * Gibt den mit dem Abstandssensor gemessenen Abstand in mm zurück
     * @return
     */
    private short getAbstand() {
        return (short) (myAsurohardware.getDistanceMeasurement());
    }

    // </editor-fold>
    /**
     * Adds a new Output. One Output-instance cannot be added twice; this method will reject it.
     * Afer a succsesful registration a notification will be sent to the added output.
     * @param newOutput to add
     */
    public void registerOutput(IEventdrivenOutput newOutput) {
        //Prüfen ob der Monitor bereits registriert wurde
        boolean bolAlreadyReg = false;
        if (newOutput != null) {
            if (myEventOutputs.contains(newOutput)) {
                bolAlreadyReg = true;
                notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"OutputAlreadyExists", newOutput});
                newOutput.notification(IEventdrivenOutput.messageType.ERROR, new Object[]{"thisOutputAlreadyExists"}, this);
            }
        }
        if (!bolAlreadyReg) {
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"registerOutput", newOutput});
            newOutput.notification(IEventdrivenOutput.messageType.DEBUG, new Object[]{"registerThisOutput"}, this);
            myEventOutputs.put(newOutput.toString(), newOutput);
        }
    }

    /**
     * removes a registered outputinterface. After a succsesful deregistration a notification will be sent.
     * @param myOutput
     */
    public void removeOutput(IEventdrivenOutput myOutput) {
        if (myEventOutputs.contains(myOutput)) {
            myOutput.notification(IEventdrivenOutput.messageType.DEBUG, new Object[]{"removeThisOutput"}, this);
            myEventOutputs.remove(myOutput.toString());
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"removeOutput", myOutput});
        } else {
            notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"removeOutputFailed", myOutput});
        }
    }

    /**
     * notifys all registered monitors
     * @param mType the tye of the notivication-event
     * @param parameters all parameters as Object[].
     */
    private void notifyMonitors(IEventdrivenOutput.messageType mType, Object[] parameters) {
        for (IEventdrivenOutput myOutput : myEventOutputs.values()) {
            myOutput.notification(mType, parameters, this);
        }
    }

    /**
     * returns the instance-name of this class
     * @return instanceName
     */
    public String getInstanceName() {
        return instanceName;
    }

    public void kill() {
        thisThread.interrupt();
        thisThread = null;
        myAsurohardware = null;
        try {
            this.finalize();
        } catch (Throwable ex) {
            Logger.getLogger(Asuroinstance.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

//<editor-fold desc="cheating">
    public void sendeNachricht(AsuroNachrichtBefehl befehl, Object parameter) {
        myAsurohardware.sendeNachricht(befehl, parameter);
        notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[]{"sending Message", new ase.hardware.AsuroNachricht(instanceName, befehl, parameter).toString()});
    }

    /**
     * Gibt die Systemzeit in ms seit Start der Asurohardware zurück
     * @return Wert zwischen 0 und (2^62)-1, entspricht (Millisekunden)
     */
    public int getSystemZeit() {
        return myAsurohardware.getSystemZeit();
    }

    /**
     * Liest alle emfpangen Nachrichten aus dem Buffer und löscht diese ggf.
     * @param delete gibt an ob der Buffer nach dem Lesen gelöscht werden soll
     * @return der Nachrichtenbuffer. Wenn keine Nachrichten vorhanden sind, dann <code>null</code>
     */
    public AsuroNachricht[] getKommunikationsBuffer(boolean delete) {
        return myAsurohardware.getKommunikationsBuffer(delete);
    }

    /**
     * Gibt an, wieviele Nachrichten sich im Buffer befinden
     * @return die Anzahl der Nachrichten im Buffer
     */
    public int getKommunikationsBufferSize() {
        return myAsurohardware.getKommunikationsBufferSize();
    }

    /**
     * Gibt die Position der Hardware zurück, an der sich der Asuro gerade befindet. Angaben sind in cm.
     * @return die Eigene Position als Punkt-Information in cm
     */
    private Position getEigenePosition() {
        return myAsurohardware.getEigenePosition();
    }

    /**
     * Gibt die Position des Greifers zurück (in mm).
     * = eigenePosition + 0.5 * BreiteVektor + 1.1 * Höhevektor
     * @return die Position an der eine Dose aufgenommen werden kann in mm des Greifers in mm
     */
    private Point getGreiferposition() {
        return new Point(getEigenePosition().x + 0.5 * widthVectorialAsMM().x + 1.1 * heightVectorialAsMM().x, getEigenePosition().y + 0.5 * widthVectorialAsMM().y + 1.1 * heightVectorialAsMM().y);
    }

    /**
     * Gibt die Position der Hardware zurück, an der sich der Asuro gerade befindet. Angaben sind in LE.
     * @return  die Eigene Position als Punkt-Information in LE
     */
    private Position getEigenePositionAlsLE() {
        return myAsurohardware.getEigenePositionAlsLE();
    }

    /**
     * Gibt die Höhe des Asuros als Vektor zurück
     * @return Höhe des Vektors in LE
     */
    private Point heightVectorial() {
        return myAsurohardware.getHeightVector();
    }

    /**
     * gibt die Breite des Asuros als Vektor zurück
     * @return Breite des Asuros in LE
     */
    private Point widthVectorial() {
        return myAsurohardware.getWidthVector();
    }

    /**
     * Gibt die Höhe des Asuros als Vektor zurück
     * @return Höhe des Vektors in mm
     */
    private Point heightVectorialAsMM() {
        return myAsurohardware.getHeightVectorAsMM();
    }

    /**
     * gibt die Breite des Asuros als Vektor zurück
     * @return Breite des Asuros in mm
     */
    private Point widthVectorialAsMM() {
        return myAsurohardware.getWidthVectorAsMM();
    }
    //</editor-fold>
}

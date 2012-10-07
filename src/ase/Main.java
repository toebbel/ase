package ase;


import ase.datalayers.SzenarioParameters.unitOfLenght;
import ase.datalayers.SzenarioContainer;
import ase.geometrics.*;
import ase.outputs.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.*;



/**
 * 
 * @author Tobias Sturm
 */
public class Main{
    private static World myWorld;

    /**
     * Startet einen Simulationsvorgang. Dabei können verschiedene Parameter eingegeben werden:
     * [Szenario-Dateipfad oder default] [-beginoutputlist [... verschiedene outputKlassen] -endoutputlist]
     *
     * @param args Kommandozeilenparameter
     */
    public static void main(String[] args) throws InterruptedException, IOException {

        //Szenario-Datei öffnen
        SzenarioContainer mySzenario = new ase.datalayers.SzenarioContainer();
        

        //Szenario editieren

        //Größe des Testgeländes
        mySzenario.szenarioParams.setSizeOfWorld(new Point(200,200), unitOfLenght.cm);
        
        //Einheiten der Startpositionen für Asuros und Dosen
        mySzenario.szenarioParams.setUStartPositionsUnit(unitOfLenght.cm);
        
        //Startpositionen Dosen und Asuros
        //mySzenario.szenarioParams.setAsuroPositionen(new Position[] {new Position(50,50,0)});//,new Position(130,50,180),new Position(30,80,180),new Position(130,180,90)
        //mySzenario.szenarioParams.setStartPointsPhials(new Point[] {new Point(50,70),new Point(50,30),new Point(25,50),new Point(80,50)});
        mySzenario.szenarioParams.setAsuroPositionen(new Position[] {new Position(50,50,0)});
        mySzenario.szenarioParams.setStartPointsPhials(new Point[] {new Point(55,30),new Point(77,30)});

        //Dosengröße als Radius
        mySzenario.szenarioParams.setSizeOfPhials(2, unitOfLenght.cm);

        //Messgenauigkeit Abstandssensor
        mySzenario.szenarioParams.setDistanceMeasureDistanceStep(5,unitOfLenght.mm);

        //Abstandssensor einstellen
        mySzenario.szenarioParams.setDistanceMeasureMaxDistance(40, unitOfLenght.cm);
        mySzenario.szenarioParams.setDistanceMeasureMinDistance(3, unitOfLenght.cm);

        //Strecke die Asuro rückwärts fahren soll um Dose fallen zu lassen
        mySzenario.szenarioParams.setIntDeltaDistanceDropPhial((int)(mySzenario.szenarioParams.getSizeOfPhials() / 2.0));

        //Simulationsticks bis beenden der Simulation
        mySzenario.timeControl.setSzenarioTicksTillEnd(300);
        mySzenario.timeControl.setWorldThreadSleep(1000); //1 Simulationstick = 1 Sekunde
        mySzenario.timeControl.setTimeFactor(1);


         //Ozutputklassen erzeugen und in Array ablegen
        Mapper myMapper = new Mapper(mySzenario.szenarioParams.getSizeOfWorld(unitOfLenght.LE),"mapper");
        AsuroinstanceCommentsConsoleOutput softwareConsoleLog = new AsuroinstanceCommentsConsoleOutput("asuroCommentWatch");
        ase.outputs.SimpleConsoleOutput myDebugger = new SimpleConsoleOutput("bla");
        ase.outputs.AsuroMindDisplay myMindDisplay = new AsuroMindDisplay("asumd");

        //DavidOutput myDavidOutput = new DavidOutput("DavidOutput");

        IEventdrivenOutput myEventOutputs[] = new IEventdrivenOutput[] {softwareConsoleLog,myMindDisplay,myDebugger};
        ITimedrivenOutput myTimeOutputs[] = {myMapper};
        
        //Initialisierungen
        myWorld = new World();
        myMindDisplay.init(myWorld);
        myWorld.initialize(mySzenario,myEventOutputs,myTimeOutputs);
        
        //Warten bis Versuch beendet wurde
        while(myWorld.isAlive())
        {
            Thread.sleep(1000);
        }
        myWorld = null;
        System.out.println("Deleted World");

    }

    static void massenVersuche() throws InterruptedException
    {
        SzenarioContainer mySzenario = new ase.datalayers.SzenarioContainer();

        
        
       for(int j = 1; j < 17;j++)
       {
           if(j==2)
               j = 3;
           if(j == 7)
               j = 10;
           if(j==11)
               j=16;
           String strFolder = "F:\\Jugend-forscht Versuche Run2\\4x4 Meter\\" + j + " Asuros\\15 Minuten\\";
            try {
                mySzenario = ase.datalayers.SzenarioSerializer.loadSzenario("F:\\Jugend-forscht Versuche Run2\\4x4Meter 104 Dosen " + j + " Asuros.ser");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

           mySzenario.szenarioParams.setIntDeltaDistanceDropPhial((int)(mySzenario.szenarioParams.getSizeOfPhials() / 2.0));
           mySzenario.timeControl.setSzenarioTicksTillEnd(900);
           mySzenario.timeControl.setTimeFactor(1);

           try {
                //
                ase.datalayers.SzenarioSerializer.saveSzenario(mySzenario, "F:\\Jugend-forscht Versuche Run2\\4x4 Meter\\" + j + " Asuros\\15 Minuten\\currentSzenario.ser");
                Logger.getLogger(Main.class.getName()).log(Level.FINE,"Szenario saved: currentSzenario.ser");
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println(mySzenario.toString());


            for(int i = 0; i < 10;i++)
            {

                if(!new java.io.File(strFolder + "Versuch " + i + "\\").mkdir())
                    System.out.println("Fehler beim erstellen des Verzeichnisses");

                        myWorld = new World();

                    ase.outputs.Effektivitaetsmesser myEM = new ase.outputs.Effektivitaetsmesser(mySzenario.szenarioParams.getSizeOfWorld(), "EM");
                    ase.outputs.Mapper myMapper = new ase.outputs.Mapper(mySzenario.szenarioParams.getSizeOfWorld(unitOfLenght.LE),"mapper");
                     ITimedrivenOutput myTimeOutputs[] = {myMapper,myEM};


                    ase.outputs.AsuroinstanceCommentsSimpleFileOutput softwareFileLog = new ase.outputs.AsuroinstanceCommentsSimpleFileOutput("software log");
                    ase.outputs.AsuroinstanceCommentsConsoleOutput softwareConsoleLog = new ase.outputs.AsuroinstanceCommentsConsoleOutput("asuroCommentWatch");
                   IEventdrivenOutput myEventOutputs[] = new IEventdrivenOutput[] {softwareConsoleLog,softwareFileLog};

            //       if(args.length > 0)
            //       {
            //           if(args[0].equals("-detailOutput"))
            //               myEventOutputs = new IEventdrivenOutput[] {new ase.outputs.SimpleConsoleOutput("alles Ausgeben")};
            //           else
            //               myEventOutputs = new IEventdrivenOutput[] {new ase.outputs.AsuroinstancecommentsConsoleOutput("asuroCommentWatch")};
            //       }
            //       else
            //           myEventOutputs = new IEventdrivenOutput[] {new ase.outputs.AsuroinstancecommentsConsoleOutput("asuroCommentWatch")};
            //


                    myWorld.initialize(mySzenario,myEventOutputs,myTimeOutputs);
                    myEM.init(myWorld, (int)(myWorld.convertMetric(1, unitOfLenght.cm, unitOfLenght.LE)), 15000, strFolder + "Versuch " + i + "\\" , (int)(myWorld.convertMetric(15, unitOfLenght.cm, unitOfLenght.LE)), mySzenario.szenarioParams.getStartPositionsAsuros().length);
                    softwareFileLog.initialize(strFolder+ "Versuch " + i + "\\");

                    while(myWorld.isAlive())
                    {
                        Thread.sleep(1000);
                    }
                    myWorld = null;
                    System.out.println("Deleted World");
                }
       }
    }

static void massenVersucheOLD() throws InterruptedException
    {
        SzenarioContainer mySzenario = new ase.datalayers.SzenarioContainer();
        String strFolder = "F:\\Jugend-forscht Versuche Run2\\4x4 Meter\\1 Asuros\\15 Minuten";
        mySzenario.szenarioParams.setStartPointsPhials(new Point[] {new Point(186,073),new Point(43,147),new Point(174,137),new Point(137,143),new Point(73,184),new Point(130,84),new Point(5,87),new Point(160,117),new Point(131,11),new Point(138,168),new Point(103,135),new Point(12,63),new Point(22,106),new Point(41,138),new Point(143,175),new Point(51,123),new Point(133,68),new Point(155,48),new Point(83,178),new Point(151,108),new Point(130,180),new Point(37,162),new Point(163,145),new Point(86,61),new Point(73,60),new Point(106,14)});
//        mySzenario.szenarioParams.setStartPointsPhials(new Point[] {new Point(60,30),new Point(90,30),new Point(120,30),new Point(30,60),new Point(60,60),new Point(90,60),new Point(120,60),new Point(150,60),new Point(30,90),new Point(60,90),new Point(90,90),new Point(120,90),new Point(150,90),new Point(30,120),new Point(60,120),new Point(90,120),new Point(120,120),new Point(150,120),new Point(60,150),new Point(90,150),new Point(120,150)});


       mySzenario.szenarioParams.setAsuroPositionen(new Position[] {new Position(88,141,296),new Position(175,064,27),new Position(133,124,156),new Position(60,50,15),new Position(138,184,116),new Position(100,14,156)});

       mySzenario.szenarioParams.setIntDeltaDistanceDropPhial((int)(mySzenario.szenarioParams.getSizeOfPhials() / 2.0));
       mySzenario.szenarioParams.setSizeOfPhials(2, unitOfLenght.cm);
       mySzenario.timeControl.setSzenarioTicksTillEnd(900);
       mySzenario.timeControl.setTimeFactor(1);
       mySzenario.szenarioParams.setSizeOfAsuro(new Point(75,100), unitOfLenght.mm);

       try {
            //
            ase.datalayers.SzenarioSerializer.saveSzenario(mySzenario, "F:\\Jugend-forscht Versuche Run2\\6 Asuros\\Zufall\\15 Minuten\\currentSzenario.ser");
            Logger.getLogger(Main.class.getName()).log(Level.FINE,"Szenario saved: currentSzenario.ser");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(mySzenario.toString());


for(int i = 0; i < 2;i++)
{

    if(!new java.io.File(strFolder + "Versuch " + i + "\\").mkdir())
        System.out.println("Fehler beim erstellen des Verzeichnisses");

            myWorld = new World();

        ase.outputs.Effektivitaetsmesser myEM = new ase.outputs.Effektivitaetsmesser(mySzenario.szenarioParams.getSizeOfWorld(), "EM");
        ase.outputs.Mapper myMapper = new ase.outputs.Mapper(mySzenario.szenarioParams.getSizeOfWorld(unitOfLenght.LE),"mapper");
         ITimedrivenOutput myTimeOutputs[] = {myMapper,myEM};


        ase.outputs.AsuroinstanceCommentsSimpleFileOutput softwareFileLog = new ase.outputs.AsuroinstanceCommentsSimpleFileOutput("software log");
        ase.outputs.AsuroinstanceCommentsConsoleOutput softwareConsoleLog = new ase.outputs.AsuroinstanceCommentsConsoleOutput("asuroCommentWatch");
       IEventdrivenOutput myEventOutputs[] = new IEventdrivenOutput[] {softwareConsoleLog,softwareFileLog};

//       if(args.length > 0)
//       {
//           if(args[0].equals("-detailOutput"))
//               myEventOutputs = new IEventdrivenOutput[] {new ase.outputs.SimpleConsoleOutput("alles Ausgeben")};
//           else
//               myEventOutputs = new IEventdrivenOutput[] {new ase.outputs.AsuroinstancecommentsConsoleOutput("asuroCommentWatch")};
//       }
//       else
//           myEventOutputs = new IEventdrivenOutput[] {new ase.outputs.AsuroinstancecommentsConsoleOutput("asuroCommentWatch")};
//


        myWorld.initialize(mySzenario,myEventOutputs,myTimeOutputs);
        myEM.init(myWorld, (int)(myWorld.convertMetric(1, unitOfLenght.cm, unitOfLenght.LE)), 60000, strFolder + "Versuch " + i + "\\" , (int)(myWorld.convertMetric(15, unitOfLenght.cm, unitOfLenght.LE)), mySzenario.szenarioParams.getStartPositionsAsuros().length);
        softwareFileLog.initialize(strFolder+ "Versuch " + i + "\\");

        while(myWorld.isAlive())
        {
            Thread.sleep(1000);
        }
        myWorld = null;
        System.out.println("Deleted World");
    }
    }


     static private void SzenarioGenerator(int NumerOfPhials, int NumberOfAsuros, String strPath)
     {
         SzenarioContainer mySzenario = new ase.datalayers.SzenarioContainer();
         mySzenario.szenarioParams.setSizeOfPhials(2, unitOfLenght.cm);
         mySzenario.szenarioParams.setSizeOfWorld(new Point(4,4),unitOfLenght.m);
         mySzenario.szenarioParams.setSizeOfAsuro(new Point(75,100), unitOfLenght.mm);
            mySzenario = StartpositionGenerator(mySzenario, NumerOfPhials, NumberOfAsuros);
            
         for(int i = 16; i > 0; i --)
         {
             try {
                //
                ase.datalayers.SzenarioSerializer.saveSzenario(mySzenario, strPath + i);
                Logger.getLogger(Main.class.getName()).log(Level.FINE,"Szenario saved: currentSzenario.ser");
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

             //Immer einen Asuro abziehen und Szenario dann neu speichern
             Position[] newPos = new Position[mySzenario.szenarioParams.getStartPositionsAsuros().length - 1];
             for(int j = 0; j < mySzenario.szenarioParams.getStartPositionsAsuros().length - 1;j++)
             {
                 newPos[j] = mySzenario.szenarioParams.getStartPositionsAsuros()[j];
             }
             mySzenario.szenarioParams.setAsuroPositionen(newPos);
         }
        System.out.println(mySzenario.toString());


        //Szenario anzeigen
//        ase.outputs.Mapper myMapper = new ase.outputs.Mapper(mySzenario.szenarioParams.getSizeOfWorld(unitOfLenght.LE),"mapper");
//        ase.outputs.SimpleConsoleDebugOutput myDebugger = new ase.outputs.SimpleConsoleDebugOutput("Debugger");
//
//        ITimedrivenOutput myTimeOutputs[] = {myMapper};
//        IEventdrivenOutput myEventOutputs[] = new IEventdrivenOutput[] {myDebugger};

        //World myWorld = new World();
        //myWorld.initialize(mySzenario,myEventOutputs,myTimeOutputs);
     }

     /**
      * Generiert Startpositionen für Asuros und Dosen.
      * @param mySzenario Szenario. Es müssen die Werte gesetzt werden: Dosenradius, Asurogröße, Weltgröße und UnitOfLenghtForStartpositions muss in cm angegeben werden!
      * @param NumerOfPhials
      * @param NumberOfAsuros
      * @return gibt 0 Zurück, wenn der nach 1000 Iterationen keine Startpositionen gefunden werden konnten
      */
    @SuppressWarnings({"static-access", "static-access", "static-access"})
     static private SzenarioContainer StartpositionGenerator(SzenarioContainer mySzenario, int NumerOfPhials, int NumberOfAsuros)
     {

         //Ermittelt den Radius der Dosen in cm und zählt einen cm dazu.
         int intPhialRadius = (int)(Math.round(mySzenario.szenarioParams.getSizeOfPhials(unitOfLenght.cm) + 1));

         //Ermittelt den Mindesabstand von Asuros zu der Bande
         int intAsuroSize = (int)(Math.round(Math.sqrt((mySzenario.szenarioParams.getSizeOfAsuro(unitOfLenght.cm).x*mySzenario.szenarioParams.getSizeOfAsuro(unitOfLenght.cm).x) * (mySzenario.szenarioParams.getSizeOfAsuro(unitOfLenght.cm).y*mySzenario.szenarioParams.getSizeOfAsuro(unitOfLenght.cm).y)) + 1));

        ase.outputs.SimpleConsoleDebugOutput myDebugger = new ase.outputs.SimpleConsoleDebugOutput("Debugger");

        ITimedrivenOutput myTimeOutputs[] = new ITimedrivenOutput[] {};
        IEventdrivenOutput myEventOutputs[] = new IEventdrivenOutput[] {myDebugger};
        World myWorldv = new World();

        int intEmergencyCounter = -1;
         do{
             if(myWorld != null)
                 myWorld.destroyWorld();
             myWorld = new World();

             Point[] startPointsPhials = new Point[NumerOfPhials];
                for(int i = 0; i < NumerOfPhials; i++)
            {
                try {
                    Thread.currentThread().sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                startPointsPhials[i] = new Point(intPhialRadius + (int)Math.round( Math.random() * (mySzenario.szenarioParams.getSizeOfWorld(unitOfLenght.cm).x - intPhialRadius) ),intPhialRadius + (int)Math.round( Math.random() * (mySzenario.szenarioParams.getSizeOfWorld(unitOfLenght.cm).y - intPhialRadius) ));
            }

            Position[] startPositionsAsuros = new Position[NumberOfAsuros];
            for(int i = 0; i < NumberOfAsuros; i++)
            {
                try {
                    Thread.currentThread().sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                startPositionsAsuros[i] = new Position(intAsuroSize + (int)Math.round( Math.random() * (mySzenario.szenarioParams.getSizeOfWorld(unitOfLenght.cm).x - intAsuroSize) ),intAsuroSize + (int)Math.round( Math.random() * (mySzenario.szenarioParams.getSizeOfWorld(unitOfLenght.cm).y - intAsuroSize)),(int)Math.round( Math.random() * 360));
            }

            mySzenario.szenarioParams.setStartPointsPhials(startPointsPhials);
            mySzenario.szenarioParams.setAsuroPositionen(startPositionsAsuros);
            intEmergencyCounter++;
             System.out.println("Versuch "+ intEmergencyCounter + " umStartpositionen zu finden");
             System.out.println(mySzenario.toString());
         }
        while(!myWorld.initialize(mySzenario,myEventOutputs,myTimeOutputs) && intEmergencyCounter < 1000);

        myWorld.destroyWorld();

        if(intEmergencyCounter >= 1000)
            return null;


        return mySzenario;
     }

   }

  /*
   * boolean bolOutputList = false;
       String strSzenario = "default";
        ase.datalayers.SzenarioContainer mySzenario = null;

        java.util.ArrayList<ITimedrivenOutput> myTimeOutputs = new java.util.ArrayList<ITimedrivenOutput>();
        java.util.ArrayList<IEventdrivenOutput> myEventOutputs = new java.util.ArrayList<IEventdrivenOutput>();

        if(args.length > 0)
        {
             if(args[0].toLowerCase().equals("default"))
             {
                mySzenario = new ase.datalayers.SzenarioContainer();
             }else {
                 try {
                 mySzenario = ase.datalayers.SzenarioSerializer.loadSzenario(args[0]);
                 } catch (FileNotFoundException ex) {
                     System.out.println("Die angegebene Datei " + args[0] + "wurde nicht gefunden!");
                 } catch (ClassNotFoundException ex) {
                     System.out.println("Fehler beim Laden des Szenarios :-/" +  ex.getMessage());
                 }
             }

             System.out.println(mySzenario.toString());

        for (int i = 1; i < args.length;i++)
       {
            if(bolOutputList)
            {
                if(args[i].toLowerCase().equals("heatmap"))
                {
                    System.out.println("ASE - Erstelle Heatmap mit dem Instanznamen 'heatmap" + i + "'");
                    myTimeOutputs.add(new ase.outputs.Heatmap(mySzenario.szenarioParams.getSizeOfWorld(), "heatmap " + i));
                }
                if(args[i].toLowerCase().equals("asuroinstanceconsoleoutput"))
                {
                    System.out.println("ASE - Erstelle AsuroinstanceConsoleOutput mit dem Instanznamen 'AsuroinstanceConsoleOutput " + i + "'");
                    myEventOutputs.add(new ase.outputs.AsuroinstanceConsoleOutput("AsuroinstanceConsoleOutput " + i));
                }
                if(args[i].toLowerCase().equals("asuroinstancecommentsconsoleoutput"))
                {
                    System.out.println("ASE - Erstelle AsuroinstancecommentsConsoleOutput mit dem Instanznamen 'AsuroinstancecommentsConsoleOutput " + i + "'");
                    myEventOutputs.add(new ase.outputs.AsuroinstancecommentsConsoleOutput("AsuroinstancecommentsConsoleOutput " + i));
                }
                if(args[i].toLowerCase().equals("mapper"))
                {
                    System.out.println("ASE - Erstelle Mapper mit dem Instanznamen 'Mapper " + i + "'");
                    myTimeOutputs.add(new ase.outputs.Mapper(mySzenario.szenarioParams.getSizeOfWorld(), "mapper " + i));
                }
                if(args[i].toLowerCase().equals("phialpathpainter"))
                {
                    System.out.println("ASE - Erstelle PhialPathPainter mit dem Instanznamen 'PhialPathPainter " + i + "'");
                    myEventOutputs.add(new ase.outputs.PhialPathPainter(mySzenario.szenarioParams.getSizeOfWorld(),100, "PhialPathPainter " + i));
                }
                if(args[i].toLowerCase().equals("simpleconsoledebugoutput"))
                {
                    System.out.println("ASE - Erstelle SimpleConsoleDebugOutput mit dem Instanznamen 'SimpleConsoleDebugOutput " + i + "'");
                    myEventOutputs.add(new ase.outputs.SimpleConsoleDebugOutput("SimpleConsoleDebugOutput " + i));
                }
                if(args[i].toLowerCase().equals("simpleconsoleoutput"))
                {
                    System.out.println("ASE - Erstelle SimpleConsoleOutput mit dem Instanznamen 'SimpleConsoleOutput " + i + "'");
                    myEventOutputs.add(new ase.outputs.SimpleConsoleOutput("SimpleConsoleOutput " + i));
                }
                if(args[i].toLowerCase().equals("checkmapasuropositions"))
                {
                    System.out.println("ASE - Erstelle checkMapAsuroPositions mit dem Instanznamen 'checkMapAsuroPositions " + i + "'");
                    myTimeOutputs.add(new ase.outputs.checkMapAsuroPositions(mySzenario.szenarioParams.getSizeOfWorld(), "checkMapAsuroPositions " + i));
                }
                if(args[i].toLowerCase().equals("checkmapdistancemeasure"))
                {
                    System.out.println("ASE - Erstelle checkMapDistanceMeasure mit dem Instanznamen 'checkMapDistanceMeasure " + i + "'");
                    myTimeOutputs.add(new ase.outputs.checkMapDistanceMeasure(mySzenario.szenarioParams.getSizeOfWorld(), mySzenario.szenarioParams.getDistanceMeasureMinDistance(), mySzenario.szenarioParams.getDistanceMeasureMaxDistance(),"checkMapDistanceMeasure " + i));
                }
            }else if(args[i].toLowerCase().equals("-beginoutputlist"))
            {
                bolOutputList = true;
                System.out.println("Lese Outputklassenliste");
            }else if(args[i].toLowerCase().equals("-endoutputlist"))
            {
                bolOutputList = false;
                System.out.println("Ende Outputklassenliste");
            }
        }
        }
        else
            System.out.println("ASE - Du musst mindestens den Parameter für das Szenario eingeben! Entweder einen Dateipfad, oder 'default'.");


        if(myTimeOutputs.size() == 0)
            myTimeOutputs.add(new ase.outputs.Mapper(mySzenario.szenarioParams.getSizeOfWorld(), "mapper"));

        if(myEventOutputs.size() == 0)
            myEventOutputs.add(new ase.outputs.AsuroinstancecommentsConsoleOutput("AsuroinstancecommentsConsoleOutput"));

        myWorld = new World();
        if(mySzenario != null)
            myWorld.initialize(mySzenario, myEventOutputs.toArray(new IEventdrivenOutput[0]), myTimeOutputs.toArray(new ITimedrivenOutput[0]));
        System.out.println("-------------------");
        System.out.println("--------ASE--------");
        System.out.println("-------------------");

        System.out.println("Starte mit folgendem Szenario:");
        System.out.println(mySzenario.getString("\n"));

        while(myWorld.isAlive())
        {
            Thread.sleep(1000);
        }
        myWorld = null;
        System.out.println("ASE - Deleted World");
   */
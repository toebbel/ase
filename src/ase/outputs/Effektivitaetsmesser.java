/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;
import ase.datalayers.SzenarioParameters.unitOfLenght;
import ase.geometrics.Point;
import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import java.awt.Color;

/**
 *
 * @author Tobi
 */
public class Effektivitaetsmesser extends VisualLiveOutput implements ITimedrivenOutput, Runnable{

    private boolean bolKeepAlive;
    private java.util.Vector<IPositionRecordable> myPhials;
    private Thread thisThread;
    private int _intMeasureDistance;
    private double dMinDistance;
    private String _strFolder;
    private int _intSleepTime;
    private ase.World _refWorld;
    private String strHTMLOutput;
    private String strCSVOutput;

    private int intTicks;
    private int intLastSave;
    private int intPictureTicks;
    private int intSaveEveryXTicks;
    private java.util.ArrayList<ase.geometrics.Point> pointList;
    private int lastAnalyse; //Tick wann di letzte Analyse durchgeführt wurde

    private double dEffektivitaetLetzerTick;
    double dEffektivitaet;
    double dDeltaEffektivitaet;
    private int _intAnzahlAsuros;
    int worldWidth;
    int worldHeight;
    int intGefuellteFlaeche = 0;

    

    public Effektivitaetsmesser(Point sizeOfWorld,String strName) {
        super(sizeOfWorld,strName);
        
        myPhials = new java.util.Vector<IPositionRecordable>();
        bolKeepAlive = false;
        pointList = new java.util.ArrayList<ase.geometrics.Point>();
        lastAnalyse = -1;

    }

    /**
     * Die init-Prozedur darf erst gestartet werden, wenn der Versuch gestartet wurde (!!!).
     * @param refWorld
     * @param measureDistance alle measureDistance * LE wird eine Messung vorgenommen
     * @param sleepTime wartezeit zwischen zwei Analysevorgängen (sollte nicht zu kurz sein, mind. 10 Sekunden)
     * @param haufenDistance wie weit zwei Dosen voneinander entfernt sein dürfen (in LE), sodass sie als ein Haufen gelten. Dieser wert wird dann innerhalb dieser Methode halbiert (da zwei Umkreise)
     */
    public void init(ase.World refWorld,int measureDistance,int sleepTime, String strFolder,int haufenDistance, int anzahlAsuros)
    {
        java.io.File myFolder = new java.io.File(strFolder);
        if(myFolder.isDirectory())
        {
             if(!new java.io.File(strFolder + "positions\\").mkdir())
                System.out.println("Konnte kein Image-Verzeichnis erstellen!");
            _strFolder = strFolder;
            _intAnzahlAsuros = anzahlAsuros;
            _refWorld = refWorld;
            intTicks = 0;
            intSaveEveryXTicks = 60;
            intLastSave = 0;
            _intSleepTime = sleepTime;
            _intMeasureDistance = measureDistance;
            dMinDistance = haufenDistance / 2.0;
            worldWidth =  (int)(_refWorld.getSzenarioParams().getSizeOfWorld(unitOfLenght.LE).x);
            worldHeight  = (int)(_refWorld.getSzenarioParams().getSizeOfWorld(unitOfLenght.LE).y);

            strHTMLOutput = "<html></head><title>" + strFolder + "</title>" + "<style type=\"text/css\"><!--\ntable,td{border               : 1px solid #CCC;border-collapse      : collapse; font                 : small/1.5 \"Tahoma\", \"Bitstream Vera Sans\", Verdana, Helvetica, sans-serif;}table{border                :none;border                :1px solid #CCC;}thead th,tbody th{background            : #FFF url(th_bck.gif) repeat-x;color                 : #666;  padding               : 5px 10px;border-left           : 1px solid #CCC;}tbody th{background            : #fafafb;border-top            : 1px solid #CCC;text-align            : left;font-weight           : normal;}tbody tr td{padding               : 5px 10px;color                 : #666;}tbody tr:hover{background            : #FFF url(tr_bck.gif) repeat;}tbody tr:hover td{color                 : #454545;}tfoot td,tfoot th{border-left           : none;border-top            : 1px solid #CCC;padding               : 4px;background            : #FFF url(foot_bck.gif) repeat;color                 : #666;}caption{text-align : left;font-size : 120%;padding  : 10px 0;color : #666;}table a:link{color : #666;}table a:visited{color : #666;} table a:hover { color : #003366;text-decoration : none; } table a:active {color : #003366;}\n--></style></head>\n<body><h1>" + strFolder + "</h1><h2>Parameter</h2><p>Messabstände in LE" + _intMeasureDistance + " (ein Pixel = 1LE)<br />Ein Tick entspricht " + _intSleepTime + "ms<br/>" + refWorld.getAllParamsAsString("<br />") + "</p><h2>Verlauf</h2><a href=\"effektivitaet.csv\">Verlauf des Versuchs als CSV</a><br />";
            strHTMLOutput += "<table><thead><th>Tick</th><th>Anzahl Asuros</th><th>Anzahl Dosen</th><th>belegte Flaeche</th><th>Effektivitaet * 10^-5</th><th>&Delta;Effektivitaet * 10^-5</th><th>Effizienz * 10^-5</th><th>&Delta;Effizienz * 10^-5</th><th>Bild</th></thead><tbody>";
            strCSVOutput = "Tick;Anzahl Asuros;Anzahl Dosen;belegte Flaeche;Effektivitaet * 10^-5;delta Effektivitaet * 10^-5;Effizienz * 10^-5;delta Effizienz * 10^-5\n";

            bolKeepAlive = true;
            thisThread = new Thread(this);
            thisThread.start();
        }
        else
            System.out.println("!!! Konnte Effektivitaesmesser nicht initialisieren; Ordner '" + strFolder + "' wurde nicht gefunden");
    }

    public void run() {
        if(thisThread != null)
        {
            while(bolKeepAlive)
            {
                repaint();
                if(!strHTMLOutput.equals("")){

                        erstellePositionsliste(_strFolder);
                        intLastSave = 0;

                }
                intTicks++;
                try {
                    thisThread.sleep(_intSleepTime);
                } catch (InterruptedException ex) {
                    //Logger.getLogger(Effektivitaetsmesser.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
            }
        }
    }

    synchronized private void analysiere(Graphics g)
    {
        if(bolKeepAlive)
        {
            
                g.setColor(Color.black);
                g.fillRect(0,0, getWidth(), getHeight());
                pointList.clear();
                for(IPositionRecordable current : myPhials)
                    pointList.add(current.getCurrentPosition().toPoint());

                intGefuellteFlaeche = 0;

                g.setColor(Color.red);

                for(int x = 0; x < worldWidth; x += _intMeasureDistance)
                {
                    for(int y = 0; y < worldHeight; y += _intMeasureDistance)
                    {
                        for(Point current : pointList)
                        {
                            if(Math.sqrt((current.x - x) * (current.x - x) + (current.y - y) * (current.y - y)) <= dMinDistance)
                            {
                                intGefuellteFlaeche++;
                                g.fillRect((int)((x + offsetX) * 1.0 / intDistance), (int)((y + offsetY) * 1.0 / intDistance), 5, 5);
                                break;
                            }
                        }
                    }
                }

                //Effektivität(t) = Anzahl Flaschen(t) / belegte Fläche(t) – 1 / [(7,5cm) ² * pi]
            if(lastAnalyse != intTicks)
            {
                lastAnalyse = intTicks;

                if(intTicks == 0){
                    dEffektivitaetLetzerTick = 0;
                    dDeltaEffektivitaet = 0;
                }
                else
                {
                    dEffektivitaetLetzerTick = dEffektivitaet;
                    dEffektivitaet = ((pointList.size() * 100000) / (double)intGefuellteFlaeche) - ((1 * 100000)/(dMinDistance * dMinDistance) * Math.PI);
                }
                //strHTMLOutput += "<table><thead><th>Tick</th><th>Anzahl Asuros</th><th>Anzahl Dosen</th><th>belegte Flaeche</th><th>Effektivitaet</th><th>&Delta;Effektivitaet</th><th>Effizienz</th><th>&Delta;Effizienz</th><th>Bild</th></thead><tbody>";
                strHTMLOutput += "<tr><td>" + intTicks + "</td><td>" + _intAnzahlAsuros + "</td><td>" + pointList.size() + "</td><td>" + intGefuellteFlaeche + "</td><td>" + dEffektivitaet + "</td><td>" + (dEffektivitaet - dEffektivitaetLetzerTick) + "</td><td>" + (dEffektivitaet / _intAnzahlAsuros) + "</td><td>" + ((dEffektivitaet - dEffektivitaetLetzerTick)/_intAnzahlAsuros) + "</td><td><a href=\"images\\" + intTicks + ".effekt.jpg\"><img src=\"images\\" + intTicks + ".effekt.thumb.jpg\" alt = \"Bild zu diesem Zeitpunkt\" /></a> <a href=\"positions\\positions" + intTicks + ".phials.csv\">positionen</a></td></tr>";
                strCSVOutput += intTicks + ";" + _intAnzahlAsuros + ";" + pointList.size() + ";" + intGefuellteFlaeche + ";" + dEffektivitaet + ";" + (dEffektivitaet - dEffektivitaetLetzerTick) + ";" + (dEffektivitaet / _intAnzahlAsuros) + ";" + ((dEffektivitaet - dEffektivitaetLetzerTick)/_intAnzahlAsuros) + "\n";
            }   
        }
        else
            g.drawString("not initialized", 100, 100);

    }


    public void kill() {
super.close();
        bolKeepAlive = false;
        repaint();
        takeSnapshot(_strFolder +  "images\\",intTicks);
        myPhials.clear();
        thisThread.interrupt();
        thisThread = null;
        try {
            //Speichern des Outputs
            strHTMLOutput += "</tbody></table></body></html>";
            java.io.BufferedWriter myWriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(_strFolder + "index.html")));
            myWriter.write(strHTMLOutput);
            myWriter.flush();
            myWriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(_strFolder + "effektivitaet.csv")));
            myWriter.write(strCSVOutput);
            myWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(Effektivitaetsmesser.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            finalize();
        } catch (Throwable ex) {
            Logger.getLogger(Effektivitaetsmesser.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    //<editor-fold defaultstate="collapsed" desc="unwichtiges Zeugs">

    public void resume() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void pause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getRefreshRate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRefreshRate(int refreshRateMs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //</editor-fold>

    @Override
    public void paintComponent(Graphics g) {        
        super.paintComponents(g);
        analysiere(g);
    }

    public void erstellePositionsliste(String strFolder)
    {
        java.io.BufferedWriter myWriter = null;
        try {
            myWriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(_strFolder + "positions\\positions" + intTicks + ".phials.csv")));
            myWriter.write("ID;X;Y");
            for(int i = 0; i < pointList.size();i++)
                myWriter.write(String.valueOf(i) + ";" + pointList.get(i).x + ";" + pointList.get(i).y + "\n");

            myWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(Effektivitaetsmesser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                myWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(Effektivitaetsmesser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    


    //<editor-fold desc="Hinzufügen und Entfernen von Dosen">

    public void removeObject(Object myObjekt) {
        if(myObjekt.getClass().getSimpleName().equals("phial"))
        {
            if(myPhials.contains((IPositionRecordable)myObjekt)){
                myPhials.remove((IPositionRecordable)myObjekt);
                this.notifyMonitors(messageType.DEBUG, new Object[] {"removed phial from 'Effektivitaetsmesser'",myObjekt});
            }
            else
                this.notifyMonitors(messageType.DEBUG, new Object[] {"couldn remove phial from 'Effektivitaetsmesser'",myObjekt});
        }

    }

    public void registerObject(Object neuesObjekt) {
        if(neuesObjekt.getClass().getSimpleName().equals("phial"))
        {
            if(!myPhials.contains((IPositionRecordable)neuesObjekt)){
                myPhials.add((IPositionRecordable)neuesObjekt);
                this.notifyMonitors(messageType.DEBUG, new Object[] {"Adding Phial to 'Effektivitaetsmesser'",neuesObjekt});
            }
            else
                this.notifyMonitors(messageType.DEBUG, new Object[] {"Phial was already added to 'Effektivitaetsmesser'",neuesObjekt});
        }
    }
//</editor-fold>

        @Override
    public void takeSynchronizedSnapshot(int intSnypshotID, String strFolderPath, int intThumbWidth, int intThumbHeight) {
       intSynchronizedSnapshotCounter++;
        if(intSynchronizedSnapshotCounter == intTakeEveryXSynchronizedSnapshots)
        {
            this.notifyMonitors(messageType.DEBUG, new Object[] {"effektivitätsmesser is taking synchronized snapshot",intSnypshotID,strFolderPath});
            takeSnapshot(strFolderPath, intThumbWidth, intThumbHeight, intSnypshotID);
            intSynchronizedSnapshotCounter = 0;
        }
    }
}

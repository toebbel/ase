/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;

import java.awt.Color;
import java.awt.Graphics;
import ase.geometrics.Point;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Tobi
 */
public class Heatmap extends VisualLiveOutput implements IDrawable,ITimedrivenOutput{
    /**
     * Seitenlänge der Quadranten in Längeneinheiten
     */
    private static int rasterSize;

    /**
     * Intervalldauer in ms zwischen zwei Positionsabfragen
     */
    private int updateInterval;
    private int drawEveryXIntervals;
    private Point worldSize;

    private java.util.Vector<IPositionRecordable> vDosen;
    private java.util.Vector<IPositionRecordable> vBewegteDosen;
    private java.util.Vector<IPositionRecordable> vAsuros;

    private int[][] rasterAsuros;
    private int rasterAsurosMax;
    private int rasterAsurosCounter;

    private Thread thisThread;

    public Heatmap(ase.geometrics.Point sizeOfWorld, String instanceName) {
        super(sizeOfWorld,instanceName);
        rasterSize  = 500;
        updateInterval  = 1000;
        drawEveryXIntervals = 5;
        worldSize = sizeOfWorld;
        
        //Instanzen erzeugen
        vAsuros = new java.util.Vector<IPositionRecordable>();
        vBewegteDosen = new java.util.Vector<IPositionRecordable>();
        vDosen = new java.util.Vector<IPositionRecordable>();

        //Raster berechnen
        int numberX = (int)((double)(sizeOfWorld.x) / (double)(rasterSize)) + 1;
        int numberY = (int)((double)(sizeOfWorld.y) / (double)(rasterSize)) + 1;
        rasterAsuros = new int[numberX][numberY];
        rasterAsurosMax = 0;
        

        //Thread starten
        thisThread = new Thread(this);
        thisThread.setName("Thread of " + instanzName);
        thisThread.start();
    }

    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        for(int i = 0; i < rasterAsuros.length;i++)
            for(int j = 0; j < rasterAsuros[i].length;j++)
            {
                g.setColor(new Color(getColorValue(rasterAsuros[i][j],rasterAsurosMax),0,0));
                g.fillRect((int)((i * rasterSize + offsetX) * zoomLevel), (int)((j * rasterSize + offsetY) * zoomLevel), (int)(rasterSize * zoomLevel), (int)(rasterSize * zoomLevel));
            }
        g.setColor(java.awt.Color.black);
    }

    @Override
    protected synchronized  void paintComponent(Graphics g) {
             super.paintComponent(g);

        //Asuroheatmap zeichnen
        for(int i = 0; i < rasterAsuros.length;i++)
            for(int j = 0; j < rasterAsuros[i].length;j++)
            {
                g.setColor(new Color(getColorValue(rasterAsuros[i][j]  ,rasterAsurosMax),0,0));
                g.fillRect((int)((i * rasterSize + offsetX) * 1.0 / intDistance), (int)((j * rasterSize + offsetY) * 1.0 / intDistance), (int)(rasterSize * 1.0 / intDistance), (int)(rasterSize * 1.0 / intDistance));
            }

                //raster zeichnen
        g.setColor(Color.gray);
        for(int i = 0; i < rasterAsuros.length;i++)
            g.drawLine((int)((i * rasterSize +   offsetX) * 1.0 / intDistance), (int)(offsetY * 1.0 / intDistance), (int)((i*rasterSize + offsetX)* 1.0 / intDistance), (int)((rasterAsuros[i].length * rasterSize + offsetY) * 1.0 / intDistance));
        for(int j = 0; j < rasterAsuros.length;j++)
            g.drawLine((int)((0 + offsetX) * 1.0 / intDistance),(int)((rasterSize * j + offsetY)* 1.0 / intDistance),(int)((rasterAsuros.length * rasterSize + offsetX) * 1.0 /intDistance),(int)((rasterSize * j + offsetY)* 1.0 / intDistance));
        g.setColor(java.awt.Color.black);
         
    }

    public void registerObject(Object neuesObjekt) {
        for(int i = 0; i < neuesObjekt.getClass().getInterfaces().length; i++)
            if(neuesObjekt.getClass().getInterfaces()[i].getName().equals("ase.outputs.IPositionRecordPlayable"))
                registerRecordableObject((IPositionRecordable)neuesObjekt);
    }

    private void registerRecordableObject(IPositionRecordable neuesObjekt)
    {
        if(neuesObjekt.getClass().getSimpleName().equals("Asurohardware"))
            vAsuros.add(neuesObjekt);
    }

    public void removeObject(Object myObjekt) {
        for(int i = 0; i < myObjekt.getClass().getInterfaces().length; i++)
            if(myObjekt.getClass().getInterfaces()[i].getName().equals("ase.outputs.IPositionRecordPlayable"))
                removeRecordableObject((IPositionRecordable)myObjekt);
    }

    private void removeRecordableObject(IPositionRecordable myObjekt)
    {
         if(myObjekt.getClass().getSimpleName().equals("Asurohardware"))
            vAsuros.remove(myObjekt);
    }

    public void resume() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void pause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        super.close();
    }

    public int getRefreshRate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRefreshRate(int refreshRateMs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void run() {
        int currentRun = 0;
        while(true)
        {
            try
            {
                //creating Heatmap-Data for Asuros
                for(IPositionRecordable current : vAsuros)
                {
                    int numberX = (int)((double)(current.getCurrentPosition().x) / (double)(rasterSize));
                    int numberY = (int)((double)(current.getCurrentPosition().y) / (double)(rasterSize));
                    rasterAsuros[numberX][numberY] += 1;
                    rasterAsurosCounter++;
                }
                
                //den maximalen wert der Asurodichten ermitteln
                for(int i = 0; i < rasterAsuros.length;i++)
                    for(int j = 0; j < rasterAsuros[i].length;j++)
                        if(rasterAsuros[i][j] > rasterAsurosMax)
                            rasterAsurosMax= rasterAsuros[i][j];

                currentRun ++;
                if(currentRun == drawEveryXIntervals)
                {
                    currentRun = 0;
                    repaint();
                }
                Thread.sleep(this.updateInterval);
            }
            catch(InterruptedException e)
            {
                this.notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[] {"Heatmap shut down because of " + e.getMessage()});
                break;
            }
        }
    }

    public int getColorValue(int value,int max)
    {
        if (max == 0 || value == 0)
            return 0;
        //System.out.println(255 / max * value);
        if(255 / max * value > 255)
            return 255;
        if(255 / max * value < 0)
            return 0;
        return 255 / max * value;
    }

    @Override
    public void kill() {
        thisThread.interrupt();
        close();
        try {
            finalize();
        } catch (Throwable ex) {
            Logger.getLogger(Heatmap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void takeSynchronizedSnapshot(int intSnypshotID, String strFolderPath, int intThumbWidth, int intThumbHeight) {
        intSynchronizedSnapshotCounter++;
        if(intSynchronizedSnapshotCounter == intTakeEveryXSynchronizedSnapshots)
        {
            notifyMonitors(messageType.DEBUG, new Object[] {"mapper is taking synchronized snapshot",intSnypshotID,strFolderPath});
            takeSnapshot(strFolderPath, intThumbWidth, intThumbHeight, intSnypshotID);
            intSynchronizedSnapshotCounter = 0;
        }
    }

}

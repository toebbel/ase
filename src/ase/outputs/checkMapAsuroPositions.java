/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;

import java.awt.Color;
import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Tobi
 */
public class checkMapAsuroPositions extends VisualLiveOutput  implements IDrawable,ase.IThreadControl,ITimedrivenOutput{
    /**
     * Seitenlänge der Quadranten in Längeneinheiten
     */
    private static int rasterSize = 500;

    /**
     * Intervalldauer in ms zwischen zwei Positionsabfragen
     */
    private int updateInterval = 1000;
    private int drawEveryXIntervals = 5;

    private java.util.Vector<IPositionRecordable> vAsuros;

    private boolean[][] rasterAsuros;
    private int rasterAsurosCounter;

    private String name = "unnamed Heatmap";
    private boolean bolKeepAlive = true;

    public checkMapAsuroPositions(ase.geometrics.Point sizeOfWorld, String instanceName) {
        super(sizeOfWorld,instanceName);
        
        //Instanzen erzeugen
        vAsuros = new java.util.Vector<IPositionRecordable>();

        //Raster berechnen
        int numberX = (int)((double)(sizeOfWorld.x) / (double)(rasterSize)) + 1;
        int numberY = (int)((double)(sizeOfWorld.y) / (double)(rasterSize)) + 1;
        rasterAsuros = new boolean[numberX][numberY];
        

        //Thread starten
        Thread thisThread = new Thread(this);
        thisThread.setName("Thread of " + name);
        thisThread.start();
    }

    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        g.drawRect((int)(offsetX * zoomLevel), (int)(offsetY * zoomLevel), (int)((worldSize.x + offsetX) * zoomLevel), (int)((worldSize.y + offsetY) * zoomLevel));

        //Asuroheatmap zeichnen
        for(int i = 0; i < rasterAsuros.length;i++)
            for(int j = 0; j < rasterAsuros[i].length;j++)
            {
                g.setColor(new Color(getColorValue(rasterAsuros[i][j]),255,255));
                g.fillRect((int)((i * rasterSize + offsetX) * zoomLevel), (int)((j * rasterSize + offsetY) * zoomLevel), (int)(rasterSize * zoomLevel), (int)(rasterSize * zoomLevel));
            }

                //raster zeichnen
        g.setColor(Color.gray);
        for(int i = 0; i < rasterAsuros.length;i++)
            g.drawLine((int)((i * rasterSize +   offsetX) * zoomLevel), (int)(offsetY * zoomLevel), (int)((i*rasterSize + offsetX)* zoomLevel), (int)((rasterAsuros[i].length * rasterSize + offsetY) * zoomLevel));
        for(int j = 0; j < rasterAsuros.length;j++)
            g.drawLine((int)((0 + offsetX) * zoomLevel),(int)((rasterSize * j + offsetY)* zoomLevel),(int)((rasterAsuros.length * rasterSize + offsetX) * 1.0 /intDistance),(int)((rasterSize * j + offsetY)* zoomLevel));

        g.setColor(java.awt.Color.black);

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawObjects(g,1.0/intDistance,offsetX,offsetY);
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

    public void close() {
        super.close();
        this.bolKeepAlive = false;
    }

    public int getRefreshRate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRefreshRate(int refreshRateMs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void run() {
        int currentRun = 0;
        while(bolKeepAlive)
        {
            try
            {
                //creating Heatmap-Data for Asuros
                for(IPositionRecordable current : vAsuros)
                {
                    int numberX = (int)((double)(current.getCurrentPosition().x) / (double)(rasterSize));
                    int numberY = (int)((double)(current.getCurrentPosition().y) / (double)(rasterSize));
                    rasterAsuros[numberX][numberY] = true;
                    rasterAsurosCounter++;
                }
                

                currentRun ++;
                if(currentRun == drawEveryXIntervals)
                {
                    repaint();
                    currentRun = 0;
                }
                Thread.currentThread().sleep(this.updateInterval);
            }
            catch(InterruptedException e)
            {
                notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[] {"Heatmap shut down because of " + e.getMessage()});
                break;
            }
        }
    }

    public int getColorValue(boolean value)
    {
        if(value)
            return 0;
        else
            return 255;
    }

    public void kill() {
        bolKeepAlive=false;
        try {
            this.finalize();
        } catch (Throwable ex) {
            Logger.getLogger(checkMapAsuroPositions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

        @Override
    public void takeSynchronizedSnapshot(int intSnypshotID, String strFolderPath, int intThumbWidth, int intThumbHeight) {
        intSynchronizedSnapshotCounter++;
        if(intSynchronizedSnapshotCounter == intTakeEveryXSynchronizedSnapshots)
        {
            this.notifyMonitors(messageType.DEBUG, new Object[] {"mapper is taking synchronized snapshot",intSnypshotID,strFolderPath});
            takeSnapshot(strFolderPath, intThumbWidth, intThumbHeight, intSnypshotID);
            intSynchronizedSnapshotCounter = 0;
        }
    }
}
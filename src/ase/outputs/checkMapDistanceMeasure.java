/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.awt.Dimension;
import ase.geometrics.Point;


/**
 *
 * @author Tobi
 */
public class checkMapDistanceMeasure extends VisualLiveOutput  implements IDrawable,ITimedrivenOutput, ase.IThreadControl{
    /**
     * Intervalldauer in ms zwischen zwei Positionsabfragen
     */
    private int updateInterval = 500;
    private int drawEveryXIntervals = 10;


    private java.util.Vector<IPositionRecordable> vAsuros;

    private int _distanceFromPositionToMeasureStartpoint;
    private int _maxMeasureDistance;

    private java.util.HashMap<String,java.util.ArrayList<Point>> arrPoints;
    private java.util.HashMap<String,java.awt.Color> arrColors;
    private int tmpX[] = new int[4];
    private int tmpY[] = new int[4];

    private boolean bolKeepAlive = true;

    /**
     * 
     * @param sizeOfWorld dimension der Welt
     * @param intDistanceFromPositionToMeasureStartpoint der Abstand des Punktes des minimal messbaren Abstand vom Ankerpunkt eines Asuros aus must be > 0
     * @param intMaxMeasureDistance die Entfernung vom minimal messbaren Abstand bis zum Ende der Messlinie. must be > 0
     * @param instanceName Instanzname
     */
    public checkMapDistanceMeasure(ase.geometrics.Point sizeOfWorld, int intDistanceFromPositionToMeasureStartpoint, int intMaxMeasureDistance, String instanceName) {
        super(sizeOfWorld,instanceName);
        if(intDistanceFromPositionToMeasureStartpoint <= 0 || intMaxMeasureDistance <= 0)
            throw new IllegalArgumentException();

        _distanceFromPositionToMeasureStartpoint = intDistanceFromPositionToMeasureStartpoint;
        _maxMeasureDistance = intMaxMeasureDistance;
        

        //Instanzen erzeugen
        vAsuros = new java.util.Vector<IPositionRecordable>();
        arrPoints = new java.util.HashMap<String,java.util.ArrayList<Point>>();
        arrColors = new java.util.HashMap<String,java.awt.Color>();

        

        //Thread starten
        Thread thisThread = new Thread(this);
        thisThread.setName("Thread of " + instanceName);
        thisThread.start();
    }

    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        g.setColor(java.awt.Color.black);
        g.drawRect((int)(offsetX * zoomLevel), (int)(offsetY * zoomLevel), (int)((worldSize.x + offsetX) * zoomLevel), (int)((worldSize.y + offsetY) * zoomLevel));
        g.setColor(java.awt.Color.red);
        for(String current : arrPoints.keySet())
        {
            if(arrPoints.get(current).size() > 4)
            {
                for(int i = 4; i < arrPoints.get(current).size();i+=2)
                {
                    tmpX[0] = (int)((arrPoints.get(current).get(i-4).x + offsetX)*zoomLevel);
                    tmpY[0] = (int)((arrPoints.get(current).get(i-4).y + offsetY)*zoomLevel);
                    tmpX[1] = (int)((arrPoints.get(current).get(i-2).x + offsetX)*zoomLevel);
                    tmpY[1] = (int)((arrPoints.get(current).get(i-2).y + offsetY)*zoomLevel);
                    tmpX[2] = (int)((arrPoints.get(current).get(i-1).x + offsetX)*zoomLevel);
                    tmpY[2] = (int)((arrPoints.get(current).get(i-1).y + offsetY)*zoomLevel);
                    tmpX[3] = (int)((arrPoints.get(current).get(i-3).x + offsetX)*zoomLevel);
                    tmpY[3] = (int)((arrPoints.get(current).get(i-3).y + offsetY)*zoomLevel);
                    g.setColor(arrColors.get(current));
                    g.fillPolygon(tmpX, tmpY, 4);
                }
            }
        }
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
                    if(! arrPoints.containsKey(current.getInstanceName()))
                    {
                        arrPoints.put(current.getInstanceName(), new java.util.ArrayList<Point>());
                        arrColors.put(current.getInstanceName(),new java.awt.Color((float)(Math.random()),(float)(Math.random() ),(float)(Math.random() )));
                    }

                    arrPoints.get(current.getInstanceName()).add(new Point(current.getCurrentPosition().x + transform2Vector(_distanceFromPositionToMeasureStartpoint,current.getCurrentPosition().angle).x,current.getCurrentPosition().y + transform2Vector(_distanceFromPositionToMeasureStartpoint,current.getCurrentPosition().angle).y));
                    arrPoints.get(current.getInstanceName()).add(new Point(current.getCurrentPosition().x + transform2Vector(_distanceFromPositionToMeasureStartpoint + _maxMeasureDistance,current.getCurrentPosition().angle).x,current.getCurrentPosition().y + transform2Vector(_distanceFromPositionToMeasureStartpoint + _maxMeasureDistance,current.getCurrentPosition().angle).y));
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

        /**
     * Calculates a vector, based on a lenght (norm) and an angle
     * @param norm the lenght of the vector
     * @param angle the angle, relative to the horizon
     * @return a vector as a <code>Point</code>
     */
    private Point transform2Vector(double norm,double angle)
    {
        return new Point(norm * Math.cos(Math.toRadians(angle - 90)),norm * Math.sin(Math.toRadians(angle - 90)));
    }

    public int getColorValue(boolean value)
    {
        if(value)
            return 0;
        else
            return 255;
    }

    public void stop() {
        bolKeepAlive = false;
    }

    public void kill()
    {
        try {
            this.finalize();
        } catch (Throwable ex) {
            Logger.getLogger(checkMapDistanceMeasure.class.getName()).log(Level.SEVERE, null, ex);
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

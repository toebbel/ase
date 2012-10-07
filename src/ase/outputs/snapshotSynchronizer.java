/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Tobi
 */
public class snapshotSynchronizer implements ITimedrivenOutput, IOutputEventCreator{

    private boolean bolKeepAlive = true;
    private int sleepTime; //in Sekunden
    private String strInstance;
    private int intCurrentID;
    private String folder;
    private int thumbWidth;
    private int thumbHeight;
    private java.util.Vector<VisualLiveOutput> synchronizedObjects;
    protected Hashtable<String,IEventdrivenOutput> myEventOutputs;

    public snapshotSynchronizer(String strName) {
        synchronizedObjects = new Vector<VisualLiveOutput>();
        myEventOutputs = new Hashtable<String, IEventdrivenOutput>();
        thumbWidth = 200;
        thumbHeight = 200;
        folder = "";
        intCurrentID = 0;
        sleepTime = 60;
        strInstance=strName;
    }




    public String getInstanceName() {
        return strInstance;
    }

    public int getRefreshRate() {
        return sleepTime;
    }
    
    public void setRefreshRate(int refreshRateMs) {
        if(refreshRateMs > 1000)
            sleepTime = Math.round(refreshRateMs / 1000);
        else
            sleepTime = 1000;
    }

    public void pause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resume() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Initialisiert und startet den SnapshopSyncrhonizer
     * @param strFolder der Ordner in dem die Snapshots abgelegt werden sollen
     * @param intThumbHeight die Höhe der Thumbnails
     * @param intThumbWidth die Breite der Thumbnails
     * @param intSleepTime die Wartezeit in Sekunden
     */
    public void initialize(String strFolder,int intThumbHeight, int intThumbWidth, int intSleepTime)
    {
        thumbHeight = intThumbHeight;
        thumbWidth =intThumbWidth;
        folder = strFolder;
        sleepTime = intSleepTime;

        Thread currentThread = new Thread(this);
        currentThread.start();
    }

    public void run() {
        System.out.println("SST runs");
        while(bolKeepAlive)
        {
            for(VisualLiveOutput current : synchronizedObjects)
                current.takeSynchronizedSnapshot(intCurrentID, folder, 200, 200);
            intCurrentID++;
            try {
                Thread.sleep(sleepTime * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(snapshotSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            }
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[] {"taking synchronized Snapshot",intCurrentID,folder});
        }
    }

    public void close() {
        bolKeepAlive = false;
    }

    public void removeObject(Object myObjekt) {
        if (bolIsVisualLiveOutput(myObjekt.getClass()))
              if(!synchronizedObjects.contains(myObjekt))
        {
            synchronizedObjects.remove((VisualLiveOutput)myObjekt);
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG,new Object[] {"snapshotSynchronizer un-registered VisualLiveOutput", myObjekt});
        }
        else
        {
            notifyMonitors(IEventdrivenOutput.messageType.ERROR,new Object[] {"snapshotSynchronizer not un-registered this object", myObjekt});
        }
    }

    public void registerObject(Object neuesObjekt) {
         if (bolIsVisualLiveOutput(neuesObjekt.getClass()))
              if(!synchronizedObjects.contains(neuesObjekt))
        {
            synchronizedObjects.add((VisualLiveOutput)neuesObjekt);
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG,new Object[] {"snapshotSynchronizer registered VisualLiveOutput", neuesObjekt});
        }
        else
        {
            notifyMonitors(IEventdrivenOutput.messageType.ERROR,new Object[] {"snapshotSynchronizer not registered this object", neuesObjekt});
        }

    }

    private boolean bolIsVisualLiveOutput(java.lang.Class myClass)
    {
        if(myClass == null)
            return false;
        if(myClass.getName().equals("ase.outputs.VisualLiveOutput"))
            return true;
        else
            return bolIsVisualLiveOutput(myClass.getSuperclass());

    }

    public void registerOutput(IEventdrivenOutput newOutput)
    {
        //Prüfen ob der Monitor bereits registriert wurde
        boolean bolAlreadyReg = false;
        if(newOutput != null)
            if(myEventOutputs.contains(newOutput))
            {
                bolAlreadyReg = true;
                notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[] {"OutputAlreadyExists",newOutput});
                newOutput.notification(IEventdrivenOutput.messageType.ERROR, new Object[] {"thisOutputAlreadyExists"},this);
            }
        if(!bolAlreadyReg)
        {
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[] {"registerOutput",newOutput});
            newOutput.notification(IEventdrivenOutput.messageType.DEBUG, new Object[] {"registerThisOutput"},this);
            myEventOutputs.put(newOutput.toString(), newOutput);
        }
    }

    public void removeOutput(IEventdrivenOutput myOutput)
    {
        if(myEventOutputs.contains(myOutput))
        {
            myOutput.notification(IEventdrivenOutput.messageType.DEBUG, new Object[] {"removeThisOutput"},this);
            myEventOutputs.remove(myOutput.toString());
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[] {"removeOutput",myOutput});
        }
            else
                notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[] {"removeOutputFailed", myOutput});
    }

        protected void notifyMonitors(IEventdrivenOutput.messageType mType,Object[] parameters)
    {
        for(IEventdrivenOutput myOutput: myEventOutputs.values())
            myOutput.notification(mType, parameters, this);
    }

}

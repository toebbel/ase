package ase.outputs;

//Imports für Anzeige


import ase.geometrics.Point;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tobias Sturm
 */
public class Mapper extends VisualLiveOutput implements ITimedrivenOutput {
    private long delta, last, fps;
    private boolean bolKeepAlive = true;
    private java.util.Vector<IDrawable> anzeigeObjekte;
    
    private int intSleepTime = 40;
    private Thread thisThread;

    public Mapper(Point sizeOfWorld,String name) {
        super(sizeOfWorld,name);

        //Zeitberechnung auf Null setzen
        last = System.nanoTime();

        //Instanzen erzeugen
        anzeigeObjekte = new java.util.Vector<IDrawable>();

        //Thread starten
        thisThread = new Thread(this);
        thisThread.setName("Thread of " + toString());
        thisThread.start();
    }

    public void run() {
        notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE,new Object[] {"MapMonitor started"});
        while(true)
        {
            try
            {
                repaint();
                computeDelta();
                Thread.currentThread().sleep(intSleepTime);

            }
            catch(InterruptedException e)
            {
                this.notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[] {"Mapper shut down because of " + e.getMessage()});
                break;
            }
        }
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        g.drawString("Redraw-sleep: " + intSleepTime + "[f|s] FPS: " + fps, 5, 10);
        if(anzeigeObjekte != null)
            for(int i = 0; i < anzeigeObjekte.size();i++){
                anzeigeObjekte.get(i).drawObjects(g, (1.0 / intDistance),offsetX,offsetY);
            }
    }



    @Override
    public void close() {
        super.close();
        this.bolKeepAlive = false;
        anzeigeObjekte.clear();
    }


    public void pause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public void resume() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getRefreshRate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRefreshRate(int refreshRateMs) {
        //implement lowp setRefreshRate
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void kill() {
        close();
        thisThread.interrupt();
        thisThread = null;
        try {
            finalize();
        } catch (Throwable ex) {
            Logger.getLogger(Mapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    public void removeObject(Object myObjekt) {
        for(int i = 0; i < myObjekt.getClass().getInterfaces().length; i++)
        {
            if(myObjekt.getClass().getInterfaces()[i].getName().equals("ase.outputs.IDrawable"))
                removeVisualisierbarobject((IDrawable)myObjekt);
        }

    }


    private void removeVisualisierbarobject(IDrawable myObjekt) {
        if(this.anzeigeObjekte.contains(myObjekt))
        {
            anzeigeObjekte.remove(myObjekt);
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG,new Object[] {"MapMonitor removed IVisualisierbar", myObjekt});
        }
        else
            notifyMonitors(IEventdrivenOutput.messageType.ERROR,new Object[] {"MapMonitor not removed IVisualisierbar", myObjekt});
    }

    private void registerVisualisierungsobjekt(IDrawable neuesObjekt) {
        if(neuesObjekt.getClass().getName().equals("ase.hardware.Asurohardware") || neuesObjekt.getClass().getName().equals("ase.hardware.phial"))
        {
            if(!this.anzeigeObjekte.contains(neuesObjekt))
            {
                anzeigeObjekte.add(neuesObjekt);
                notifyMonitors(IEventdrivenOutput.messageType.DEBUG,new Object[] {"MapMonitor registered IVisualisierbar", neuesObjekt});
            }
            else
            {
                notifyMonitors(IEventdrivenOutput.messageType.ERROR,new Object[] {"MapMonitor not registered IVisualisierbar", neuesObjekt});
            }
        }
    }

    public void registerObject(Object neuesObjekt) {
            for(int i = 0; i < neuesObjekt.getClass().getInterfaces().length; i++)
                if(neuesObjekt.getClass().getInterfaces()[i].getName().equals("ase.outputs.IDrawable"))
                    registerVisualisierungsobjekt((IDrawable)neuesObjekt);
    }


    private void computeDelta()
    {
        delta = System.nanoTime() - last;
        last = System.nanoTime();
        fps = ((long) 1e9)/(delta + 1);
    }

    @Override
    public String toString() {
        return "Mapper " + instanzName;
    }

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        super.keyPressed(e);
        if(e.getKeyChar() == 'f')
            if(intSleepTime > 10)
                intSleepTime -=10;

        if(e.getKeyChar() == 's')
            if(intSleepTime < 5000)
                intSleepTime +=10;

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

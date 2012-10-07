/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;

import java.awt.Graphics;
import ase.geometrics.Path;
import ase.geometrics.Point;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Administrator
 */
public class PhialPathPainter extends VisualLiveOutput implements IDrawable {
    private java.util.concurrent.ConcurrentHashMap<String,Path> phialPaths;
    private int intMaxPathLen;

    /**
     *
     * @param maxPathLen maximum number of points of a path. -1 means, that no limit is given
     */
    public PhialPathPainter(Point sizeOfWorld, int maxPathLen,String name) {
        super(sizeOfWorld,name);
        
        phialPaths = new java.util.concurrent.ConcurrentHashMap<String,Path>();
        intMaxPathLen = maxPathLen;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(phialPaths.size() != 0)
            for(Path current : phialPaths.values())
                current.drawObjects(g, 1.0 / intDistance, offsetX, offsetY);

    }

    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        for(Path current : phialPaths.values())
                current.drawObjects(g, zoomLevel, offsetX, offsetY);
    }

    /**
     * Creates a default Path
     * @return a Path with default values
     */
    private Path createNewPath()
    {
        return new Path(intMaxPathLen);
    }

    @Override
    public void notification(messageType mType, Object[] values, IOutputEventCreator acuator) {
        super.notification(mType,values,acuator);
        if(acuator != null)
        {
            if(acuator.getClass().getSimpleName().equals("phial"))
                if(mType.equals(messageType.STATECHANGE)){
                    if(values.length >= 2)
                    {
                        if(!phialPaths.containsKey(acuator.getInstanceName())) // wenn noch kein Pfad für die Dose existiert
                                phialPaths.put(acuator.getInstanceName(),createNewPath());
                            phialPaths.get(acuator.getInstanceName()).addPoint(((Point)values[1]).clone());

                            //redraw
                                    setSize(getSize().width - 1,getSize().height - 1);
                                    setSize(getSize().width + 1,getSize().height + 1);
                    }
                }
        }
    }
    

    @Override
    public String toString() {
        return getInstanceName();
    }

   
    public void kill() {
        try {
            this.finalize();
        } catch (Throwable ex) {
            Logger.getLogger(PhialPathPainter.class.getName()).log(Level.SEVERE, null, ex);
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

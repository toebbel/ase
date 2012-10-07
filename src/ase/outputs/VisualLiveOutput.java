/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.*;
import java.awt.Dimension;
import ase.geometrics.Point;
import java.util.Hashtable;
import java.awt.image.BufferedImage;
import com.sun.image.codec.jpeg.*;
import java.awt.Dimension;
import java.io.*;

/**
 *
 * @author Tobi
 */
public abstract class VisualLiveOutput extends JPanel implements IOutputEventCreator, java.awt.event.KeyListener, ase.IThreadControl, IEventdrivenOutput  {

    protected int intDistance;
    protected int offsetX;
    protected int offsetY;
    protected String instanzName ;
    protected boolean bolFollowOtherVisualOutputs;
    protected Hashtable<String,IEventdrivenOutput> myEventOutputs;
    protected JFrame thisFrame;
    protected Point worldSize;
    protected int intTakeEveryXSynchronizedSnapshots;
    protected int intSynchronizedSnapshotCounter;

    public VisualLiveOutput(Point sizeOfWorld,String name) {
        intDistance = 30;
        offsetX = 0;
        offsetY = 0;
        myEventOutputs = new Hashtable<String,IEventdrivenOutput>();
        bolFollowOtherVisualOutputs = true;
        instanzName = name;
        worldSize = sizeOfWorld;
        intTakeEveryXSynchronizedSnapshots = 1;
        intSynchronizedSnapshotCounter=0;
        
        this.setPreferredSize(new Dimension((int)(sizeOfWorld.x * 1.0 / intDistance),(int)(sizeOfWorld.y * 1.0 / intDistance)));
        thisFrame = new javax.swing.JFrame(instanzName);
        thisFrame.setLocationByPlatform(true);
        thisFrame.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        thisFrame.add(this);
        thisFrame.addKeyListener(this);
        thisFrame.pack();
        thisFrame.setVisible(true);
    }

    public void close() {
        notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE,new Object[] {"closing " + instanzName});
        thisFrame.dispose();
    }


    public void notification(messageType mType, Object[] values, IOutputEventCreator acuator) {
        if(mType.compareTo(mType.STATECHANGE) == 0)
            if(values[0].equals("change state of view"))
            {
                if(bolFollowOtherVisualOutputs){
                    intDistance = (Integer)values[1];
                    offsetX = (Integer)values[2];
                    offsetY = (Integer)values[3];
                    notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE,new Object[] {"followed state of view",intDistance,offsetX,offsetY});
                    //thisFrame.setSize(thisFrame.getSize().height + 1, thisFrame.getSize().width + 1);
                    //thisFrame.setSize(thisFrame.getSize().height - 1, thisFrame.getSize().width - 1);
                }
            }
    }

    protected void notifyMonitors(IEventdrivenOutput.messageType mType,Object[] parameters)
    {
        for(IEventdrivenOutput myOutput: myEventOutputs.values())
            myOutput.notification(mType, parameters, this);
    }

    public String getInstanceName() {
        return instanzName;
    }

      @SuppressWarnings("empty-statement")
    public void keyPressed(KeyEvent e) {
        notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[] {"keyPressed",e});
        if(e.getKeyCode() == e.VK_LEFT)
        {
            this.offsetX -=intDistance;
            repaint();
        }

        if(e.getKeyCode() == e.VK_RIGHT)
        {
            this.offsetX +=intDistance;
            repaint();
        }

        if(e.getKeyCode() == e.VK_UP){
            this.offsetY -=intDistance;
            repaint();
        }

        if(e.getKeyCode() == e.VK_DOWN){
            this.offsetY +=intDistance;
            repaint();
        }

        if(e.getKeyCode() == e.VK_PLUS)
            if(intDistance > 1){
                intDistance--;
                repaint();
            }

        if(e.getKeyCode() == e.VK_MINUS)
            if(intDistance < 1000){
                intDistance++;
                repaint();
            }


        if(e.getKeyChar() == 'f')
        {
            if(bolFollowOtherVisualOutputs)
                bolFollowOtherVisualOutputs = false;
            else
                bolFollowOtherVisualOutputs = true;
        }

        notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE, new Object[] {"change state of view", intDistance,offsetX,offsetY});;
    }

    public void keyReleased(KeyEvent e) {
//      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void keyTyped(KeyEvent e) {
//        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //g.drawRect((int)(offsetX * 1.0 / intDistance), (int)(offsetY * 1.0 / intDistance), (int)((worldSize.x + offsetX) * 1.0 / intDistance), (int)((worldSize.y + offsetY) * 1.0 / intDistance));
        if(bolFollowOtherVisualOutputs)
            thisFrame.setTitle(instanzName + " (following) - View: " + offsetX + "|" + offsetY + "[Arrows] @ Distance[+|-] " + intDistance);
        else
            thisFrame.setTitle(instanzName + " - View: " + offsetX + "|" + offsetY + "[Arrows] @ Distance[+|-] " + intDistance);
    }

    private void saveSnapshot(BufferedImage img, String strFile)
    {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
            param.setQuality(0.70f, true);

            encoder.encode(img, param);
            FileOutputStream fos = new FileOutputStream(strFile);
            fos.write(out.toByteArray());
            fos.close();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Erstellt ein Snapshot, wobei das Thumbnail eine Abmessung von 200x200 Pixeln hat. Das Bild wird abgelegt unter <i>strFolder</i>\<i>SnapshotID</i>.<i>instanceName</i>.jpg bzw <i>strFolder</i>\<i>SnapshotID</i>.<i>instanceName</i>.thumb.jpg.
     * @param strFolder der Ordner in dem die Bildatei abgelegt werden soll. Der Pfad muss mit einem "\" enden!
     * @param snapshotID die fortlaufende Nummer der Snapshots
     */
    protected void takeSnapshot(String strFolder,int snapshotID)
    {
        takeSnapshot(strFolder,200,200,snapshotID);
    }

    protected void takeSnapshot(String strFolder, int thumbWidth, int thumbHeight, int snapshotID)
    {
        if (new java.io.File(strFolder).exists() && new java.io.File(strFolder).isDirectory())
        {
        //Image-Objekt aus dem JFrame erstellen
        BufferedImage img = new java.awt.image.BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.paint(img.getGraphics());
        saveSnapshot(img, strFolder + snapshotID + "." + instanzName + ".jpg");

        //Ein Thumbnali erstellen
        double thumbRatio = (double)thumbWidth / (double)thumbHeight;
        int imageWidth = img.getWidth(null);
        int imageHeight = img.getHeight(null);
        double imageRatio = (double)imageWidth / (double)imageHeight;
        if (thumbRatio < imageRatio) {
          thumbHeight = (int)(thumbWidth / imageRatio);
        } else {
          thumbWidth = (int)(thumbHeight * imageRatio);
        }

        BufferedImage thumbImg = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics2D = thumbImg.createGraphics();
        graphics2D.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(img, 0, 0, 200, 200, null);
        saveSnapshot(thumbImg, strFolder + snapshotID + "." + instanzName + ".thumb.jpg");
        }
    }

    abstract public void takeSynchronizedSnapshot(int intSnypshotID, String strFolderPath, int intThumbWidth, int intThumbHeight);

    /**
     * Gibt an, ob jeder Synchronisierte Snapshot erstellt werden sol (1), oder nur jeder 2. (2) oder 3. (3)
     * @param intIntervall muss größer=1 sein, ansonsten wird intIntervall = 1 verwendet.
     */
    public void setSynchronizedSnapshotInterval(int intIntervall)
    {
        if(intIntervall > 0)
            intTakeEveryXSynchronizedSnapshots = intIntervall;
        else
            intTakeEveryXSynchronizedSnapshots = 1;
    }
}

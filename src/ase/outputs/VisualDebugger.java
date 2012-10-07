/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;
import ase.datalayers.SzenarioParameters.unitOfLenght;
import ase.geometrics.*;
import java.awt.Color;
/**
 *
 * @author Tobias
 */
public class VisualDebugger extends VisualLiveOutput{

    private java.util.ArrayList<Object[]> myPoints;
    private java.util.ArrayList<Object[]> myLines;
    private ase.World refWorld;
    String strText = "";

    public VisualDebugger(Point sizeOfWorld, String name) {
        super(sizeOfWorld, name);
        myPoints =  new java.util.ArrayList<Object[]>();
        myLines =  new java.util.ArrayList<Object[]>();
    }


    @Override
    protected void takeSnapshot(String strFolder, int snapshotID) {
        super.takeSnapshot(strFolder, snapshotID);
    }

    @Override
    protected void takeSnapshot(String strFolder, int thumbWidth, int thumbHeight, int snapshotID) {
        super.takeSnapshot(strFolder, thumbWidth, thumbHeight, snapshotID);
    }

    @Override
    public void takeSynchronizedSnapshot(int intSnypshotID, String strFolderPath, int intThumbWidth, int intThumbHeight) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void kill() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void notification(messageType mType, Object[] values, IOutputEventCreator acuator) {
        super.notification(mType, values, acuator);
        if(refWorld != null)
        {
            if(mType.equals(messageType.DEBUG) && values.length > 3)
            {
                if(values[0].equals("visual debug add"))
                {
                    try
                    {
                        Object[] tmp = new Object[] {0,0,2,"unnamed",Color.BLACK};
                        tmp[0] = (int)refWorld.convertMetric(Double.valueOf(String.valueOf(values[1])), unitOfLenght.mm, unitOfLenght.LE);
                        tmp[1] = (int)refWorld.convertMetric(Double.valueOf(String.valueOf(values[2])), unitOfLenght.mm, unitOfLenght.LE);
                        if(values.length > 3)
                        {
                            tmp[2] = (int)refWorld.convertMetric(Double.valueOf(String.valueOf(values[3])), unitOfLenght.mm, unitOfLenght.LE);
                            if(values.length > 4)
                            {
                                tmp[3] = values[4];
                                if(values.length > 5)
                                    tmp[4] = values[5];
                            }
                        }
                        myPoints.add(tmp);
                        repaint();
                        //notifyMonitors(messageType.DEBUG, new Object[] {"neues Objekt empfangen" , tmp[0],tmp[1], tmp[2], tmp[3], tmp[4]});
                    }
                    catch(Exception e){
                        System.out.println(e.getLocalizedMessage());
                    }
                }
                else if(values[0].equals("visual debug add line"))
                {
                    try
                    {
                        Object[] tmp = new Object[] {0,0,100,100,"unnamed",Color.BLACK};
                        tmp[0] = (int)refWorld.convertMetric(Double.valueOf(String.valueOf(values[1])), unitOfLenght.mm, unitOfLenght.LE);
                        tmp[1] = (int)refWorld.convertMetric(Double.valueOf(String.valueOf(values[2])), unitOfLenght.mm, unitOfLenght.LE);
                        tmp[2] = (int)refWorld.convertMetric(Double.valueOf(String.valueOf(values[3])), unitOfLenght.mm, unitOfLenght.LE);
                        tmp[3] = (int)refWorld.convertMetric(Double.valueOf(String.valueOf(values[4])), unitOfLenght.mm, unitOfLenght.LE);
                        if(values.length > 5)
                        {
                            tmp[4] = values[5];
                            if(values.length > 6)
                                tmp[5] = values[6];
                        }
                        myLines.add(tmp);
                        repaint();
                        //notifyMonitors(messageType.DEBUG, new Object[] {"neues Objekt empfangen" , tmp[0],tmp[1], tmp[2], tmp[3], tmp[4]});
                    }
                    catch(Exception e){
                        System.out.println(e.getLocalizedMessage());
                    }
                }
            }
             else if(values[0].equals("visual debug text") && values.length > 1)
                {
                    strText = (String)(values[1]);
                    repaint();
                }
            else if(values[0].equals("visual debug remove"))
                {
                    myPoints.clear();
                    strText = "";
                    myLines.clear();
                    repaint();
                }
        }
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if(strText != null)
            g.drawString(strText, 10, 10);
        if(myPoints != null)
        {
            for(int i = 0; i < myPoints.size();i++)
            {
                try
                {
                    g.setColor((Color)(myPoints.get(i)[4]));
                    g.drawString((String)(myPoints.get(i)[3]), (int)(((Integer)(myPoints.get(i)[0]) + offsetX) / intDistance), (int)(((Integer)(myPoints.get(i)[1]) + offsetY) / intDistance));
                    g.drawOval((int)(((Integer)(myPoints.get(i)[0]) + offsetX - ((Integer)(myPoints.get(i)[2]))) / intDistance), (int)(((Integer)(myPoints.get(i)[1]) + offsetY - ((Integer)(myPoints.get(i)[2]))) / intDistance), (Integer)((Integer)(myPoints.get(i)[2]) / intDistance * 2), (Integer)((Integer)(myPoints.get(i)[2]) / intDistance * 2));
                    //System.out.println(("Zeichne an " +  (int)(((Integer)(myObjects.get(i)[0]) + offsetX) / intDistance)) + " " + (int)(((Integer)(myObjects.get(i)[1]) + offsetY) / intDistance));
                }
                catch(Exception e){
                    System.out.println(e.getLocalizedMessage());
                }
            }
            g.setColor(Color.BLACK);
        }

        if(myLines != null)
        {
            for(int i = 0; i < myLines.size();i++)
            {
                try
                {
                    g.setColor((Color)(myLines.get(i)[5]));
                    g.drawString((String)(myLines.get(i)[4]), (int)(((Integer)(myLines.get(i)[0]) + offsetX) / intDistance), (int)(((Integer)(myLines.get(i)[1]) + offsetY) / intDistance));
                    g.drawLine((int)(((Integer)(myLines.get(i)[0]) + offsetX) / intDistance), (int)(((Integer)(myLines.get(i)[1]) + offsetY) / intDistance),(int)(((Integer)(myLines.get(i)[2]) + offsetX) / intDistance), (int)(((Integer)(myLines.get(i)[3]) + offsetY) / intDistance));
                    //System.out.println(("Zeichne an " +  (int)(((Integer)(myObjects.get(i)[0]) + offsetX) / intDistance)) + " " + (int)(((Integer)(myObjects.get(i)[1]) + offsetY) / intDistance));
                }
                catch(Exception e){
                    //System.out.println(e.getLocalizedMessage());
                }
            }
            g.setColor(Color.BLACK);
        }
    }

    public void init(ase.World refWord)
    {
        this.refWorld = refWord;
    }




}

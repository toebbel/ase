package ase.hardware;

import ase.geometrics.Circle;
import ase.geometrics.Point;
import ase.geometrics.Position;
import java.util.Hashtable;
import java.awt.Graphics;
import ase.outputs.IEventdrivenOutput;

/**
 * Phial are the objects which shoudl be moved by the asuros
 * @author Tobias Sturm
 */
public class phial implements  ase.outputs.IDrawable,ase.outputs.IOutputEventCreator, IDozeable, ase.outputs.IPositionRecordable{
    private Circle _position ;
    private String _name;
    private boolean isDozed;

    private Hashtable<String,IEventdrivenOutput> myEventOutputs = new Hashtable<String,IEventdrivenOutput>();

    public phial(Circle _position, String name) {
        notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[] {"Creating phial at",_position});
        this._position = _position;
        _name = name;
    }

    public Point[] getClashPointsAt(Position otherPosition) {
        return _position.getClashPointsAt(otherPosition);
    }



    public String getInstanceName() {
        return _name;
    }

    public boolean isDozed() {
        return isDozed;
    }

    public void pickUp() {
        this.notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE, new Object[] {"pickUp",_position.getAnchor()});
        isDozed = true;
    }

    public void drop() {
      this.notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE, new Object[] {"drop",_position.getAnchor()});
        isDozed = false;
    }






    /**
     * checks if another object collides with this instance
     * @param points all points to check
     * @param actuator the atuator of the check
     * @return
     */
    public boolean checkCollision(Point[] points, ICollideable actuator) {
        return _position.checkCollision(points, actuator);
    }

    /**
     * Draws itself on a Graphicsbject, including its name
     * @param g
     * @param zoomLevel
     * @param offsetX
     * @param offsetY
     */
    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        g.drawString(toString(), (int)((_position.getAnchor().x + offsetX) * zoomLevel), (int)((_position.getAnchor().y + offsetY) * zoomLevel));
        _position.drawObjects(g, zoomLevel, offsetX, offsetY);
    }


    


    public Point[] getClashPoints() {
        return getClashPoints(0,0,0);
    }

    public Point[] getClashPointsAbsolute(int offsetX, int offsetY, double offsetAngle) {
        return _position.getClashPointsAbsolute(offsetX,offsetY);
    }

      

    public Point[] getClashPoints(int offsetX, int offsetY, double offsetAngle){
        return _position.getClashPoints(offsetX, offsetY, offsetAngle);
    }


    public void doze(Point movement)
    {
        notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[] {"doze to position",movement});
        _position.moveTo(movement.x, movement.y);
    }

    @Override
    public String toString() {
        return "phial " + _name;
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

    private void notifyMonitors(IEventdrivenOutput.messageType mType,Object[] parameters)
    {
        for(IEventdrivenOutput myOutput: myEventOutputs.values())
            myOutput.notification(mType, parameters, this);
    }

    public boolean doze(IDozeable newIDozeable) {
        return false;
    }

    public Point[] getDozePoints() {
        return null;
    }

    public void setCurrentPosition(Position newPosition) {
        this._position.moveTo(newPosition.x, newPosition.y);
    }

    public Position getCurrentPosition() {
        return new Position(_position.getAnchor().x,_position.getAnchor().y,0);
    }


}

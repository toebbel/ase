package ase.geometrics;

import ase.hardware.ICollideable;
import java.awt.Graphics;

/**
 * Circle represents a geometric shape: a Circle. Consists of an achor Point which is located in the middle of the circle and an integer-radius.
 * @author Tobias Sturm
 */
public class Circle implements ase.outputs.IDrawable, ase.hardware.ICollideable {

    //Point in the middle of the circle
    private Point _anchor;

    //radius of the circle
    private int _radius;

    //distance between two clashpoints in degree
    private int clashPointDistance;

    //array for the clashpoints
    private Point[] clashPoints;
   

    /**
     * Creates an instance of a <code>Circle</code> at the <code>Point</code> 0|0 with the radius 1 and the clashpointdistance 1
     */
    public Circle() {
        _anchor = new Point(0,0);
        _radius = 1;
        clashPointDistance = 1;
        updateClashArrayLenght();
    }

    /**
     * computes the number of Clashpoints based on their distance in degree
     */
    private void updateClashArrayLenght()
    {
        clashPoints = new Point[(int)(360 / clashPointDistance)];
    }

    /**
     * reates an instance of a <code>Circle</code>
     * @param anchor the position of the middlepoint
     * @param radius the radius
     * @param clashDist The distance between two clashPoints in degree
     */
    public Circle (Point anchor, int radius,int clashDist)
    {
        _anchor = anchor;
        _radius = radius;
        clashPointDistance = clashDist;
        updateClashArrayLenght();
    }

    public Point[] getClashPointsAt(Position otherPosition) {
        updateclashPoints( _anchor.x - otherPosition.x, _anchor.y - otherPosition.y);
        return this.clashPoints;
    }


    /**
     * Creates a String, equal to this formating: <i>C(X|y;r=1)</i>
     * @return e.g. <i>C(X|y;r=1)</i>
     */
    @Override
    public String toString() {
        return "C(" + getAnchor().x + "|" + getAnchor().y + ";r=" + _radius + ")";
    }

    /**
     * Updates the Clashpoints and saves them in ClashPoints[]
     */
    private void updateClashPoints()
    {
        updateclashPoints(0,0);
    }

    private void updateclashPoints(double offsetX, double offsetY)
    {
        int i = 0;
        int alpha = 0;
        while (alpha <= 360 && i < clashPoints.length)
        {
            clashPoints[i] = new Point(getAnchor().x + (Math.sin(alpha) * (_radius)) + offsetX, getAnchor().y + (Math.cos(alpha) * (_radius) ) + offsetY);
            i++;
            alpha += clashPointDistance;
        }
    }

    /**
     * Returns the clashpoints
     * @return 
     */
    public Point[] getClashPoints() {
        updateClashPoints();
        return clashPoints;
    }

    public Point[] getClashPoints(int offsetX,int offsetY, double offsetAngle) {
        updateclashPoints(offsetX,offsetY);
        return clashPoints;
    }

    public Point[] getClashPointsAbsolute(int x,int y)
    {
        return getClashPointsAt(new Position(x,y,0));
    }


    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        updateClashPoints();
        g.drawOval((int)((getAnchor().x + offsetX)*zoomLevel), (int)((getAnchor().y + offsetY)*zoomLevel), 2, 2);
        for(int i = 0; i < clashPoints.length; i++)
            if(clashPoints[i] != null)
                g.drawOval((int)(((offsetX + clashPoints[i].x)*zoomLevel)), (int)(((offsetY + clashPoints[i].y)*zoomLevel)), 1, 1);
    }

   

    public boolean checkCollision(Point[] points, ICollideable actuator) {
        for(int i = 0; i<points.length;i++)
            if(points[i] != null)
                if(Math.sqrt(Math.pow(points[i].x - _anchor.x, 2) + Math.pow(points[i].y - _anchor.y, 2)) <= _radius)
                    return true;
        return false;
    }

    /**
     * @return the _anchor middlepoint of the circle as <code>Point</code>
     */
    public Point getAnchor() {
        return _anchor;
    }

    /**
     * changes the position of the circle
     * @param x how long to moveTo horizon.
     * @param y how long to moveTo verti.
     */
    public void moveTo(double x, double y)
    {
        _anchor.x = x;
        _anchor.y = y;
    }

    public boolean doze(ase.hardware.IDozeable newIDozeable) {
        return false;
    }

    public Point[] getDozePoints() {
        return null;
    }

    public void setCurrentPosition(Position newPosition) {
        _anchor = newPosition.clone();
    }

    public Position getCurrentPosition() {
        return new ase.geometrics.Position(_anchor.x, _anchor.y, 0);
    }

    public String getInstanceName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

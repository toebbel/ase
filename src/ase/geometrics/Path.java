package ase.geometrics;

import java.awt.Graphics;
import java.util.ArrayList;

/**
 * <code>Path</code> is a sorted collection of instances of <code>Point</code>.
 * @author Tobi
 */
public class Path implements ase.outputs.IDrawable{

    protected ArrayList<Point> _points;
    protected int _autoDelete;

    /**
     * Creates an instance of <code>Path</code> with an autoDelete of -1 and no Points
     */
    public Path() {
        _autoDelete = -1;
        _points = new ArrayList<Point>();
    }

    /**
     * Creates an instance of <code>Path</code> with no Points
     * @param _autoDelete
     */
    public Path(int _autoDelete) {
        _points = new ArrayList<Point>();
        this._autoDelete = _autoDelete;
    }

    /**
     * Adds a new <code>Point</code> to the <code>Path</code>
     * @param newPoint a <code>Point</code> to add
     */
    public void addPoint(Point newPoint)
    {
        _points.add(newPoint);
        checkArrSize();
    }

    /**
     * Adds a new <code>Position</code> as a <code>Point</code> to the <code>Path</code>
     * @param newPosition a new <code>Position</code>, treated as a <code>Point</code>
     */
    public void addPoint(Position newPosition)
    {
        _points.add(newPosition);
        checkArrSize();
    }

    /**
     * Adds new <code>Point</code>s to the Path
     * @param newPoints Array of <code>Point</code>s which contains the <code>Point</code>s to add
     */
    public void addPoints(Point[] newPoints)
    {
        for(int i = 0; i < newPoints.length;i++)
            if(newPoints[i] != null)
                _points.add(newPoints[i]);
        checkArrSize();
    }

     /**
     * Adds new <code>Positions</code>s which are treated as <code>Point</code>s to the Path
     * @param newPositions Array of <code>Positions</code>s which contains the <code>Positions</code>s to add.
     */
    public void addPoints(Position[] newPositions)
    {
        for(int i = 0; i < newPositions.length;i++)
            if(newPositions[i] != null)
                _points.add(newPositions[i]);
        checkArrSize();
    }

    private int getPathSize()
    {
        return _points.size();
    }

    /**
     * Checks the size of the <code>Point</code>-Array and deletes Points when the value of autoDelete is smaller than the size of the array.
     */
    private void checkArrSize()
    {
        if(_autoDelete > -1)
            while(_points.size() > _autoDelete)
                _points.remove(0);
    }

    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        if(_points.size() > 1)
            for(int i = 1; i < _points.size(); i++)
            {
                g.drawOval((int)(zoomLevel * (_points.get(i).x + offsetX)), (int)(zoomLevel * (_points.get(i).y + offsetY)), 4, 4);
                g.drawOval((int)(zoomLevel * (_points.get(i - 1).x + offsetX)), (int)(zoomLevel * (_points.get(i - 1).y + offsetY)), 4, 4);
                g.drawLine((int)(zoomLevel * (_points.get(i).x + offsetX)), (int)(zoomLevel * (_points.get(i).y + offsetY)), (int)(zoomLevel * (_points.get(i - 1).x + offsetX)), (int)(zoomLevel * (_points.get(i - 1).y + offsetY)));
            }
        else if (_points.size() == 1) //there is only one point in the path --> draw that point
            g.drawOval((int)(zoomLevel * (_points.get(0).x + offsetX)), (int)(zoomLevel * (_points.get(0).y + offsetY)), 1, 1);
    }





    @Override
    public String toString() {
        return "Path with " + getPathSize() + " points";
    }







}

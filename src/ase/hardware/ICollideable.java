package ase.hardware;
import ase.geometrics.Point;

/**
 * ICollideable colntains two methods to control if two objects collide or not
 * @author Tobias Sturm
 */
public abstract interface ICollideable extends  ase.outputs.IDrawable{

    /**
     * checks if one of the given points clashes with the geometric shape of the instance of this class. only one point has to clash to result a collision-true-result.
     * @param points all points to check
     * @param actuator the triggerclass of the check
     * @return tru = collision, false = no collision
     */
    public boolean checkCollision(Point[] points, ICollideable actuator);

    /**
     * returns all clashPoints, that means all points on the boarder of an object/geometric shape
     * @return an array of points; some values could be null
     */
    public Point[] getClashPoints();

    /**
     * returns all clashPoints, that means all points on the boarder of an object/geometric shape. the position of the objectshape is not modified, but the clashpoints are movet (x+=offsetX;y+=offsetY)
     * @param offsetX move right or left
     * @param offsetY move up or down
     * @param offsetAngle turns the figure
     * @return an array of points; some values could be null
     */
    public Point[] getClashPoints(int offsetX, int offsetY, double offsetAngle);

    public Point[] getClashPointsAt(ase.geometrics.Position otherPosition);

    /**
     * gives a new IDozeable object to this instance. the Doze-Object will be moved with this instance together
     * @param newIDozeable the new doze-object
     * @return true = object will be dozed; false = object can't be dozed
     */
   public boolean doze(IDozeable newIDozeable);

   /**
    * returns Points of the area where a IDozeable object can be dozed.
    * @return array of points where a doze-process cann begin.
    */
   public Point[] getDozePoints();
   

}

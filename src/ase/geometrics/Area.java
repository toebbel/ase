package ase.geometrics;
import ase.hardware.ICollideable;
import ase.outputs.IDrawable;
import java.awt.Graphics;

/**
 * <code>Area</code> represents a surface or a rectangle with an anchor-<code>Point</code> at the left corner at the bottom.
 * @author Tobias Sturm
 */
public class Area implements IDrawable, ase.hardware.ICollideable{

    /**
     * dimensions represents the width and the height of the area. x is the height and y the width
     */
    protected Position anchor;
    protected Point dimensions;



    /**
     * Vector for temp. calculations
     */
    protected Point widthVectorial;

    /**
     * Vector for temp. calculations
     */
    protected Point heightVectorial;

    /**
     * Array for the clashPoints
     */
    protected Point[] clashPoints;

    /**
     * Distance between thwo Clashpoints; Default: 1
     */
    protected int clashPointDistance;



    /**
     * Creates an instance of <code>Area</code> at the Position (0|0; 0°) and the clashPointDistance 1
     */
    public Area() {
        anchor = new Position();
        dimensions = new Point();
        clashPointDistance = 1;
        clashPoints = new Point[(int)(dimensions.y * 2 + dimensions.x * 2) / clashPointDistance];
        updateTempVectors(0);
    }

    /**
     * Creates an instance of <code>Area</code> at the given <code>Position</code>
     * @param posAnchor the <code>Position</code> of the <code>Area</code>-anchorpoint
     * @param pDimension the height and the width of the <code>Area</code>
     * @param intClashPointDist the distance between two clahsPoints
     */
    public Area(Position posAnchor, Point pDimension, int intClashPointDist)
    {
        anchor = posAnchor;
        dimensions = pDimension;
        clashPointDistance = intClashPointDist;
        clashPoints = new Point[(int)(dimensions.y * 2 + dimensions.x * 2) / clashPointDistance];
        updateTempVectors(0);
    }

    public Area(Area otherArea) {
        this.clashPointDistance = otherArea.clone().getClashPointDistance();
        this.dimensions = otherArea.clone().getDimensions();
        this.anchor = otherArea.clone().getAnchor();
        clashPoints = new Point[(int)(dimensions.y * 2 + dimensions.x * 2) / clashPointDistance];
        updateTempVectors(0);
    }



    /**
     * Creates an instance of <code>Area</code> at the given parameters
     * @param x the x-coordinate of the anchor-<code>Point</code>
     * @param y the y-coordinate of the anchor-<code>Point</code>
     * @param angle the angle between the width and the horizon
     * @param width the width of the area
     * @param height the height of the area
     * @param intClashPointDist the distance between two clahsPoints
     */
    public Area(int x, int y, int angle, int width, int height, int intClashPointDist)
    {
        anchor = new Position((double)x,(double)y,angle);
        dimensions = new Point(width, height);
        clashPointDistance = intClashPointDist;
        clashPoints = new Point[(int)(dimensions.y * 2 + dimensions.x * 2) / clashPointDistance];
        updateTempVectors(0);
    }

    /**
     * Returns the values of <code>Area</code> as string
     * @return e.g. <i>A((x|y; angle°)|(width|height))</i>
     */
    @Override
    public String toString() {
        return "A(" + getAnchor().toString() + "|" + getDimensions().toString() + ")";
    }



    /**
     * returns an array of points which are the clashpoints. if any of tis clashpoints collides is in (or at the border) the area of another asuro, phial, ect. there is a collison
     * @return array of clashpoints
     */
    public Point[] getClashPoints(){
        updateClashPoints(0,0,0);
        return clashPoints;
    }

    /**
     * returns an array of points which are the clashpoints. if any of tis clashpoints collides is in (or at the border) the area of another asuro, phial, ect. there is a collison
     * @param offsetX moves the clashpoints to the dight or to the left, without changing the position
     * @param offsetY moves the clashpoints up or down, without changing the position
     * @param offsetAnlge turns the clashpoints , without changing the position
     * @return
     */
    public Point[] getClashPoints(int offsetX, int offsetY, double offsetAnlge){
        updateClashPoints(offsetX,offsetY,offsetAnlge);
        return clashPoints;
    }

    
    /**
     * Updates heightVectorial and widthVectorial with an offset of the angle of the area
     * @param angleOffset turns the vectors without changing the position of the area
     */
    protected void updateTempVectors(double angleOffset)
    {
        while(getAnchor().angle < 0)
        {
            anchor.angle += 360;
        }
        while(getAnchor().angle > 360)
        {
            anchor.angle -= 360;
        }
        widthVectorial = new Point(getDimensions().x * Math.cos(Math.toRadians(getAnchor().angle + angleOffset)),getDimensions().x * Math.sin(Math.toRadians(getAnchor().angle + angleOffset)));
        heightVectorial = new Point(getDimensions().y * Math.cos(Math.toRadians(getAnchor().angle + angleOffset - 90)),getDimensions().y * Math.sin(Math.toRadians(getAnchor().angle + angleOffset - 90)));
    }

     /**
      * Updates heightVectorial and widthVectorial with an offset of the angle of the area
      */
    protected void updateTempVectors(){updateTempVectors(0);}

    /**
     * Updates the internal array of the clashpoints
     * @param offsetX moves the clashpoints right or left, without changing the position of the area
     * @param offsetY moves the clashpoints up or down, without changing the position of the area
     * @param offsetAngle turns the clashpoints, without changing the position of the area
     */
    protected void updateClashPoints(double offsetX,double offsetY, double offsetAngle)
    {
        updateTempVectors(offsetAngle);
        int i = 0;
        int lenCounter = 0;
        //clashPoints = new Point[];
        //Generating Chalshpoints for width
        if(widthVectorial.x > widthVectorial.y)
        {
            //indicating the x-coordinate for the while-condition; needed when y is 0
            while((lenCounter + 1 < clashPoints.length ) && (Math.abs(i * getClashPointDistance() * widthVectorial.getUnitVector().x) < Math.abs(widthVectorial.x)))
            {
                clashPoints[lenCounter] = new Point(i * widthVectorial.getUnitVector().x * getClashPointDistance() + getAnchor().x+ offsetX,i * widthVectorial.getUnitVector().y * getClashPointDistance()  + getAnchor().y + offsetY);
                clashPoints[lenCounter + 1] = new Point(i * widthVectorial.getUnitVector().x * getClashPointDistance() + heightVectorial.x  + getAnchor().x + offsetX,i * widthVectorial.getUnitVector().y * getClashPointDistance() + heightVectorial.y  + getAnchor().y + offsetY);
                lenCounter +=2;
                i++;
            }
        }
        else
        {
            //indicating the y-coordinate for the while-condition; needed when x is 0
            while((lenCounter + 1 < clashPoints.length ) && (Math.abs(i * getClashPointDistance() * widthVectorial.getUnitVector().y) < Math.abs(widthVectorial.y)))
            {
                clashPoints[lenCounter] = new Point(i * widthVectorial.getUnitVector().x * getClashPointDistance() + getAnchor().x + offsetX,i * widthVectorial.getUnitVector().y * getClashPointDistance()  + getAnchor().y + offsetY);
                clashPoints[lenCounter + 1] = new Point(i * widthVectorial.getUnitVector().x * getClashPointDistance() + heightVectorial.x  + getAnchor().x + offsetX,i * widthVectorial.getUnitVector().y * getClashPointDistance() + heightVectorial.y  + getAnchor().y + offsetY);
                lenCounter +=2;
                i++;
            }
        }

        
        
        //Generating Chalshpoints for height
        i = 0;
        if(heightVectorial.x > heightVectorial.y)
        {
            //indicating the x-coordinate for the while-condition; needed when y is 0
            while((lenCounter + 1 < clashPoints.length) && (Math.abs(i * getClashPointDistance() * heightVectorial.getUnitVector().x) < Math.abs(heightVectorial.x)))
            {
                clashPoints[lenCounter] = new Point(i * heightVectorial.getUnitVector().x * getClashPointDistance() + getAnchor().x + offsetX,i * getClashPointDistance() * heightVectorial.getUnitVector().y + getAnchor().y + offsetY);
                clashPoints[lenCounter + 1] = new Point(i * heightVectorial.getUnitVector().x * getClashPointDistance() + widthVectorial.x  + getAnchor().x + offsetX,i * heightVectorial.getUnitVector().y * getClashPointDistance() + widthVectorial.y + getAnchor().y + offsetY);
                lenCounter +=2;
                i++;
            }
        }
        else
        {
            //indicating the y-coordinate for the while-condition; needed when x is 0
            while((lenCounter + 1 < clashPoints.length) && (Math.abs(i * getClashPointDistance() * heightVectorial.getUnitVector().y) < Math.abs(heightVectorial.y)))
            {
                clashPoints[lenCounter] = new Point(i * heightVectorial.getUnitVector().x * getClashPointDistance() + getAnchor().x + offsetX,i * getClashPointDistance() * heightVectorial.getUnitVector().y + getAnchor().y + offsetY);
                clashPoints[lenCounter + 1] = new Point(i * heightVectorial.getUnitVector().x * getClashPointDistance() + widthVectorial.x  + getAnchor().x + offsetX,i * heightVectorial.getUnitVector().y * getClashPointDistance() + widthVectorial.y + getAnchor().y + offsetY);
                lenCounter +=2;
                i++;
            }
        }

    }

    /**
     * Creates a new Instance of Area with the same values
     * @return
     */
    @Override
    public Area clone(){
        return new Area(getAnchor(), getDimensions(), getClashPointDistance());
    }



    /**
     * Draws the clashpoints and the width- and heightvector on a Graphicsobject
     * @param g
     * @param zoomLevel
     * @param offsetX
     * @param offsetY
     */
    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        updateClashPoints(0,0,0);
        g.drawLine((int)((getAnchor().x + widthVectorial.x + offsetX)*zoomLevel), (int)((getAnchor().y + widthVectorial.y + offsetY)*zoomLevel), (int)((getAnchor().x + offsetX) * zoomLevel), (int)((getAnchor().y + offsetY)*zoomLevel));
        g.drawLine((int)((getAnchor().x + heightVectorial.x + offsetX)*zoomLevel), (int)((getAnchor().y + heightVectorial.y + offsetY)*zoomLevel), (int)((getAnchor().x + offsetX)*zoomLevel), (int)((getAnchor().y + offsetY)*zoomLevel));

       

        for(int i = 0; i < clashPoints.length; i++)
        {
            if(clashPoints[i] != null){
                g.drawOval((int)((clashPoints[i].x + offsetX)*zoomLevel), (int)((clashPoints[i].y + offsetY)*zoomLevel), 1, 1);
                //System.out.println("draw " + clashPoints[i].toString());
            }
        }
    }




    /**
     * @return the anchor
     */
    public Position getAnchor() {
        return anchor;
    }

    /**
     * @return the dimensions
     */
    public Point getDimensions() {
        return dimensions;
    }

    /**
     * @return the clashPointDistance
     */
    public int getClashPointDistance() {
        return clashPointDistance;
    }


    /**
     * Checks if a clashpoint of another object returns with this area
     * @param points an array of points to check
     * @param actuator the actuator of the test;
     * @return true = collision (one of the points is inside this area or at the border); false = no collision
     */
    public boolean checkCollision(Point[] points, ICollideable actuator) {
        try
        {
            double r,s;
            updateTempVectors();
            for(int i = 0; i < points.length;i++)
            {
                if(points[i] != null){
                    r =  (points[i].y - anchor.y - ((points[i].x * widthVectorial.y - anchor.x * widthVectorial.y)/widthVectorial.x))/((-1 * widthVectorial.y * heightVectorial.x / widthVectorial.x) + heightVectorial.y);
                    s = (points[i].x - anchor.x - r * heightVectorial.x) / widthVectorial.x;
                    if(((r <= 1 && r >= -1) && (s <= 1 && s >= 0)))
                        return true;
                }
            }
            return false;
        }
        catch (java.lang.Exception e)
        {
            return true;
        }
    }

    public Point[] getClashPointsAt(Position otherPosition) {
        updateClashPoints(otherPosition.x - anchor.x,otherPosition.y - anchor.y, otherPosition.angle - anchor.angle);
        return this.clashPoints;
    }




    public boolean doze(ase.hardware.IDozeable newIDozeable) {
        return false;
    }

    public Point[] getDozePoints() {
        return null;
    }


    public Position getCurrentPosition() {
        return anchor.clone();
    }

    public String getInstanceName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

package ase.geometrics;
import ase.outputs.IDrawable;
import java.awt.Graphics;

/**
 * <code>Point</code> is an object which represents a koordinate or a dimension. Its like a array with the lenght of 2.
 * Point has two fields: <code>x</code> and <code>y</code>.
 * @author Tobias Sturm
 */
public class Point implements IDrawable, java.io.Serializable{
    public double x;
    public double y;

    /**
     * Creates an instanz of <code>Point</code>;Default Values are x = 0, y = 0
     */
    public Point() {
        x = 0;
        y = 0;
    }

    /**
     * Creates an instanz of <code>Point</code>
     * @param x Value of x
     * @param y Value of y
     */
    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

        /**
     * Creates an instanz of <code>Point</code>
     * @param x Value of x
     * @param y Value of y
     */
    public Point(double dX, double dY)
    {
        this.x = dX;
        this.y = dY;
    }

    @Override
    public Point clone()
    {
        return new Point(x,y);
    }


    /**
     *
     * @return returns both values (x and y) as string, for example <i>P(1|2)</i>
     */
    @Override
    public String toString() {
        return "P(" + x + "|" + y + ")";
    }

    /**
     * Adds the value of this instance of <code>Point</code> with the vales of another instance of <code>Point</code> and returns it
     * @param anotherPoint another Point which is to add with with point.
     * @return the sum ob both points as a new point (x+x|y+y)
     */
    public Point addPoint(Point anotherPoint)
    {
        return new Point(this.x + anotherPoint.x, this.y + anotherPoint.y);
    }

    /**
     * Calculates the norm of a <code>Point</code> which represents an vector.
     * @return the norm of an vector: sqr(x²+y²)
     */
    public double getNorm()
    {
        return Math.sqrt(x*x+y*y);
    }

    /**
     * Returns the unit vector (a vector with the norm of 1).
     * @return the unit vector
     */
    public Point getUnitVector()
    {
        return new Point(x / getNorm(),y / getNorm());
    }

    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        g.drawOval((int)((x + offsetX)*zoomLevel), (int)((y + offsetY)*zoomLevel), 1, 1);
    }

    /**
     * Fasst man den Punkt als Information eines Vektors auf, dann ist diese Winkel der Winkle zwischen der X-Achse und diesem Vektor
     * @return der Winkel in Grad zur X-Achse
     */
    public double getAngle()
    {
        if(Math.pow(x, 2) + Math.pow(y, 2) == 0)
            return 0;
        return Math.toDegrees(Math.acos((x) / (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)))));
    }


}

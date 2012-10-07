package ase.geometrics;

/**
 * <code>Position </code> is a <code>Point</code> with the additional information of an angle.
 * @author Tobias Sturm
 */
public class Position extends Point{
    /**
     * Angle represents the angle between the width and the "horizon".
     */
    public double angle;

    /**
     * Creates a <code>Position</code> at the location P(0|0) and the angle 0.
     */
    public Position() {
        super();
        angle = 0;
    }


        /**
     * Creates a <code>Position</code> at the location (x|y) with the given angle.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param angle
     */
    public Position(double intX, double intY, double angle) {
        super(intX,intY);
        this.angle = angle;
    }

    /**
     * Returns the sum of two <code>Position</code>s
     * @param otherPosition another <code>Position</code> to combine with another.
     * @returnthe sum of two <code>Position</code>s: x+x,y+y,angle+angle
     */
    public Position addPosition(Position otherPosition)
    {
        return new Position(x + otherPosition.x,y + otherPosition.y,angle + otherPosition.angle);
    }

    public Point toPoint()
    {
        return new Point(x,y);
    }

    /**
     * Returns the values of the <code>Position</code> as a string.
     * @return e.g. <i>P(x|y; angle°)</i>
     */
    @Override
    public String toString() {
        return "P(" + x + "|" + y + "; " + angle + "°)";
    }

    @Override
    public Position clone() {
        return new Position(x,y,angle);
    }







}

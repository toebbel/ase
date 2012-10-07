/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs.asuroMindDisplay;
import java.awt.Color;

/**
 *
 * @author Tobias
 */
public abstract class amdGraphic implements ase.outputs.IDrawable{
    protected int lifeTime;
    protected int x;
    protected int y;
    protected Color color;
    protected String strLabel;

    /**
     * Gibt zurück, ob die Lebenszeit überschritten wurde oder nicht
     * @return
     */
    public boolean bolReadyForDeath()
    {
        if(getLifeTime() == 0)
            return true;
        else
            return false;
    }

    /**
     * Zieht der Lebenszeit entsprechend viele ms ab
     * @param intTime Zeit in Millisekundne die von der Lebenszeit abgezogen werden soll
     * @return true, wenn die Lebenszeit abgelaufen ist, false, wenn nicht
     */
    public boolean subTime(int intTime)
    {
        if(getLifeTime() == -1)
            return false;
        lifeTime -= intTime;
        if(getLifeTime() < 0){
            lifeTime = 0;return true;}
        return false;
    }

    /**
     * @return the lifeTime
     */
    public int getLifeTime() {
        return lifeTime;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return the strLabel
     */
    public String getStrLabel() {
        return strLabel;
    }
}

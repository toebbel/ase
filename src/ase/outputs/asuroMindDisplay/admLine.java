/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs.asuroMindDisplay;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author Tobias
 */
public class admLine  extends amdGraphic{
    protected int x2,y2;

    public admLine(int x, int y, int x2, int y2, Color color) {
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        lifeTime = -1;
    }

    public admLine(int x, int y, int x2, int y2) {
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
        color = Color.BLACK;
        lifeTime = -1;
    }

    public admLine(int x, int y, int x2, int y2, Color color, int lifeTime, String label) {
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.lifeTime = lifeTime;
        strLabel = label;
    }


    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        if(getLifeTime() != 0){
        g.setColor(getColor());
        g.drawOval((int)((getX() + offsetX) * zoomLevel), (int)((getY() + offsetY) * zoomLevel), 2, 2);
        g.drawString(getStrLabel(), (int)((getX() + offsetX) * zoomLevel), (int)((getY() + offsetY) * zoomLevel));
        g.setColor(Color.BLACK);}
    }

}

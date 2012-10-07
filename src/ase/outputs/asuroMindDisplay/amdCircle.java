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
public class amdCircle extends amdGraphic {
private int radius;
    public amdCircle(int x, int y, int r, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        lifeTime = -1;
        radius = r;
    }

    public amdCircle(int x, int y, int r) {
        this.x = x;
        this.y = y;
        color = Color.BLACK;
        lifeTime = -1;
        radius = r;
    }

    public amdCircle(int x, int y, int r, Color color, int lifeTime, String label) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.lifeTime = lifeTime;
        strLabel = label;
        radius = r;
    }

    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        if(getLifeTime() != 0){
        g.setColor(getColor());
        g.drawOval((int)((getX() + offsetX - radius) * zoomLevel), (int)((getY() + offsetY - radius) * zoomLevel),(int)(2 * radius * zoomLevel), (int)(2 * radius * zoomLevel));
        g.drawString(getStrLabel(), (int)((getX() + offsetX) * zoomLevel), (int)((getY() + offsetY) * zoomLevel));
        g.setColor(Color.BLACK);}
    }



}

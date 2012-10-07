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
public class amdPoint extends amdGraphic {
private int radius;
    public amdPoint(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        lifeTime = -1;
    }

    public amdPoint(int x, int y) {
        this.x = x;
        this.y = y;
        color = Color.BLACK;
        lifeTime = -1;
    }

    public amdPoint(int x, int y, Color color, int lifeTime, String label) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.lifeTime = lifeTime;
        strLabel = label;
    }

    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        if(getLifeTime() != 0){
        g.setColor(getColor());
        g.drawOval((int)((getX() + offsetX) * zoomLevel), (int)((getY() + offsetY) * zoomLevel),2,2);
        g.drawString(getStrLabel(), (int)((getX() + offsetX) * zoomLevel), (int)((getY() + offsetY) * zoomLevel));
        g.setColor(Color.BLACK);}
    }



}

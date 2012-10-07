/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.hardware;

import ase.hardware.ICollideable;
import ase.geometrics.Point;

/**
 *
 * @author Tobi
 */
public interface IDozeable extends ICollideable {

    void doze(Point movement);
    boolean isDozed();
    void pickUp();
    void drop();
    public Point[] getClashPointsAbsolute(int offsetX, int offsetY, double offsetAngle);

}

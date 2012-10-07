/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;
import ase.geometrics.Position;

/**
 * Dieses Interface ermöglicht es zeitgesteuerten Outputklassen Positionen von beweglichen Objekten zu registrieren
 * @author Tobi
 */
public interface IPositionRecordable extends ase.INameable {
    public Position getCurrentPosition();
}

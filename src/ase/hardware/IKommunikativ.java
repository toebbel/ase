/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.hardware;

/**
 * Wird von allen Objekten implementiert, die untereinander kommunizieren können
 * @author Administrator
 */
public interface IKommunikativ {

    /**
     * Enpfängt eine Nachricht und speichert sie im Buffer ab oder verarbeitet sie direkt
     * @param nachricht
     */
    public void empfangeNachricht(AsuroNachricht nachricht);

}

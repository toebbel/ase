/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.hardware;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Stellt eine Nachricht dar, die ein Asuro an die anderen senden kann
 * @author Tobi
 */
public class AsuroNachricht {

    private AsuroNachrichtBefehl _befehl;
    private Object _parameter;
    private String _asuroID;



    /**
     * Erstellt eine Instanz einer AsuroNachricht
     * @param asuroID die ID des Asuros, darf nich leer oder <code>null</code> sein
     * @param befehl der Befehl der Nachricht
     * @param parameter der Parameter der Nachricht, abhängig vom Befehl der Nachricht
     */
    public AsuroNachricht(String asuroID, AsuroNachrichtBefehl befehl, Object parameter)
    {
        if(parameter == null || asuroID == null)
        {
            try {
                finalize();
            } catch (Throwable ex) {
                Logger.getLogger(AsuroNachricht.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if(asuroID.equals(""))
        {
            try {
                finalize();
            } catch (Throwable ex) {
                Logger.getLogger(AsuroNachricht.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        _befehl = befehl;
        _parameter = parameter;
        _asuroID = asuroID;
    }


    /**
     * Wenn es sich bei dem Parameter um eine Position handelt, wird diese zurück gegeben.
     * @return Parameter als Position, wenn Parameter keine Position ist, dann <code>null</code>
     * @see isParameterPosition()
     */
    public ase.geometrics.Position getParameterAsPosition()
    {
        if(isParameterPosition())
            return (ase.geometrics.Position)getParameter();
        else
            return null;
    }

    /**
     * Gibt an, ob es sich bei dem Parameter um eine Positionsinformation handelt
     * @return
     */
    public boolean isParameterPosition()
    {
        if (getParameter().getClass().getSimpleName().equals("Position"))
            return true;
        else
            return false;
    }

 /**
     * Wenn es sich bei dem Parameter um einen Punkt handelt, wird diese zurück gegeben.
     * @return Parameter als Punkt, wenn Parameter kein Punkt ist, dann <code>null</code>
     * @see isParameterPoint()
     */
    public ase.geometrics.Point getParameterAsPoint()
    {
        if(isParameterPoint())
            return (ase.geometrics.Point)getParameter();
        else
            return null;
    }

    /**
     * Gibt an, ob es sich bei dem Parameter um eine Punktinformation handelt
     * @return
     */
    public boolean isParameterPoint()
    {
        if (getParameter().getClass().getSimpleName().equals("Point"))
            return true;
        else
            return false;
    }

    /**
     * Wenn es sich bei dem Parameter um einen Integer handelt, wird diese zurück gegeben.
     * @return Parameter als <code>int</code>, wenn Parameter kein Integer ist, dann <code>-1</code>
     * @see isParameterInt()
     */
    public int getParameterAsInt()
    {
        if(isParameterInt())
            return (Integer)getParameter();
        else
            return -1;
    }

    /**
     * Gibt an, ob es sich bei dem Parameter um eine Integer-Information handelt
     * @return
     */
    public boolean isParameterInt()
    {
        if (getParameter().getClass().getSimpleName().equals(int.class.getSimpleName()))
            return true;
        else
            return false;
    }

    /**
     * @return the _befehl
     */
    public AsuroNachrichtBefehl getBefehl() {
        return _befehl;
    }

    /**
     * @return the _parameter
     */
    public Object getParameter() {
        return _parameter;
    }

    /**
     * @return the _asuroID
     */
    public String getAsuroID() {
        return _asuroID;
    }

    /**
     * Der "Befehl" einer Nachricht eines Asuros
     */
    public static enum AsuroNachrichtBefehl
    {
        MittelPunktFestgelegt,
        EigeneStartposition,
        EigeneAktuellePosition,
        NeueDoseGefundenBeiScan,
        BeansprucheDoseFuerMich,
        DoseZumZielhaufenGestellt,
        ScanAnPosition;
    }

    /**
     * Gibt die komplette Information der Nachricht als String aus:<br />
     * <b>Format:</b><br/>
     * Nachricht von <i>AsuroID</i> <i>Befehl</i> <i>Parameter</i>
     * @return
     */
    @Override
    public String toString() {
        return "Nachricht von " + getAsuroID() + " " + getBefehl().toString() + " " + getParameter().toString();
    }


}

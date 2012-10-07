/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;

/**
 *
 * @author Tobi
 */
public interface IOutputReplayable {
    /**
     * Initialisiert eine Wiedergabe eines gepseicherten Versuchs.
     * @param strFolder Der Ordner in den die Aufzeichnungen abgelegt wurden
     * @param strInstanceName der Instanzname der Outputklasse beim Aufnehmen des Versuchs
     * @return true wenn die Initialisierung erfolgreich war, false, wenn ein Fehler (z.B. FileNotFound) aufgetreten ist
     */
    public boolean initializeReplay(String strFolder, String strInstanceName);

    /**
     * Initialisiert und startet die Aufnahme eines Versuchs
     * @param strFolder der Ordner in den die Aufzeichnungen abgelegt werden sollen. Jede Outputklasse leitet eine oder mehrere Dateien, die sie zum speichern benötigt von ihrem Instanznamen ab
     */
    public boolean initializeRecord(String strFolder);

    /**
     * Da die Aufzeichnung und Wiedergabe nicht kontinuierlich, sondern in Zeitabschnitten voranschreiten, müssen alle Outputklassen bei der Aufnahme und der Wiedergabe synchron laufen.
     * @param intTick
     */
    public void setTickCount(int intTick);

    /**
     * Gibt den aktuellen TickCount zurück
     * @return der TickCount der Outputklasse
     */
    public int getCurrentTickCount();

    /**
     * Räumt den Arbeitsspeicher auf/ schließt FileStreams
     */
    public void endReplay();

    /**
     * schließt alle Verbindungen zu Dateien
     */
    public void endRecord();
}

package ase.outputs;

import java.awt.*;



/**
 * IVisualisierbar bietet eine Methode die das Objekt, das dieses Interfaces implementiert, auffordert seine Position auf ein Graphics-Objekt zu zeichenen
 * @author Tobias Sturm
 */
public interface IDrawable {

    /**
     * Forder das Objekt auf Punkte und Linien auf das Graphics-Objekt g zu zeichnen.
     * @param g Das Graphics-Objekt auf das gezeichnet werden soll
     * @param zoomLevel wie weit heraus gezoomt werden soll (bei einem Level von 10 ist es 10 mal kleiner, da 1/zoomLevel gillt)
     * @param offsetX wie weit die Karte nach rechts bzw. Links verschoben wird
     * @param offsetY wie weit die KArte nach oben bzw. unten verschoben wird
     */
    void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY);
}

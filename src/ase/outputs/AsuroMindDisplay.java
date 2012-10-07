/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ase.outputs;

import java.awt.Graphics;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import ase.outputs.asuroMindDisplay.*;
import java.awt.Color;
import ase.datalayers.SzenarioParameters.unitOfLenght;

/**
 *
 * @author Tobias
 */
public class AsuroMindDisplay implements IEventdrivenOutput {

    private ConcurrentHashMap<String, mindDisplayWindow> asuroList;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<amdGraphic>> graphicsList, phialList, otherAsuroList, scanPositions;
    private ConcurrentHashMap<String, amdGraphic> eigenePosition, zielHaufen;
    private int intSleepTime;
    private ase.World refWorld;
    private String name;

    public AsuroMindDisplay(String strName) {
        asuroList = new ConcurrentHashMap<String, mindDisplayWindow>();
        eigenePosition = new ConcurrentHashMap<String, amdGraphic>();
        zielHaufen = new ConcurrentHashMap<String, amdGraphic>();
        graphicsList = new ConcurrentHashMap<String, ConcurrentLinkedQueue<amdGraphic>>();
        phialList = new ConcurrentHashMap<String, ConcurrentLinkedQueue<amdGraphic>>();
        otherAsuroList = new ConcurrentHashMap<String, ConcurrentLinkedQueue<amdGraphic>>();
        scanPositions = new ConcurrentHashMap<String, ConcurrentLinkedQueue<amdGraphic>>();
        intSleepTime = 500;
        name = strName;
    }

    public void init(ase.World rWorld) {
        refWorld = rWorld;
    }

    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY, String strAsuroID) {
        if (graphicsList.containsKey(strAsuroID)) {
            for (amdGraphic current : graphicsList.get(strAsuroID)) {
                if (current.subTime(intSleepTime)) {
                    graphicsList.get(strAsuroID).remove(current);
                } else {
                    current.drawObjects(g, zoomLevel, offsetX, offsetY);
                }
            }
        }
        if (otherAsuroList.containsKey(strAsuroID)) {
            for (amdGraphic current : otherAsuroList.get(strAsuroID)) {
                if (current.subTime(intSleepTime)) {
                    otherAsuroList.get(strAsuroID).remove(current);
                } else {
                    current.drawObjects(g, zoomLevel, offsetX, offsetY);
                }
            }
        }
        if (phialList.containsKey(strAsuroID)) {
            for (amdGraphic current : phialList.get(strAsuroID)) {
                if (current.subTime(intSleepTime)) {
                    phialList.get(strAsuroID).remove(current);
                } else {
                    current.drawObjects(g, zoomLevel, offsetX, offsetY);
                }
            }
        }
        if (scanPositions.containsKey(strAsuroID)) {
            for (amdGraphic current : scanPositions.get(strAsuroID)) {
                if (current.subTime(intSleepTime)) {
                    scanPositions.get(strAsuroID).remove(current);
                } else {
                    current.drawObjects(g, zoomLevel, offsetX, offsetY);
                }
            }
        }
        if (eigenePosition.containsKey(strAsuroID)) {
            eigenePosition.get(strAsuroID).drawObjects(g, zoomLevel, offsetX, offsetY);
        }
        if (zielHaufen.containsKey(strAsuroID)) {
            zielHaufen.get(strAsuroID).drawObjects(g, zoomLevel, offsetX, offsetY);
        }
    }

    /**
     * Prüft ob der Ausro bereits bekannt ist. Wenn nicht, wird ein neues Fenster für ihn erstellt
     * @param strAsuroInstanceName der Instanzname des Asuros
     */
    private void checkAsuro(String strAsuroInstanceName) {
        if (!asuroList.containsKey(strAsuroInstanceName)) {
            ase.outputs.asuroMindDisplay.mindDisplayWindow myWindow = new mindDisplayWindow(refWorld.getSzenarioParams().getSizeOfWorld(), "asuroMindWindow for " + strAsuroInstanceName, strAsuroInstanceName, intSleepTime, this);
            asuroList.put(strAsuroInstanceName, myWindow);
            graphicsList.put(strAsuroInstanceName, new ConcurrentLinkedQueue<amdGraphic>());
            scanPositions.put(strAsuroInstanceName, new ConcurrentLinkedQueue<amdGraphic>());
            otherAsuroList.put(strAsuroInstanceName,new ConcurrentLinkedQueue<amdGraphic>());
            phialList.put(strAsuroInstanceName, new ConcurrentLinkedQueue<amdGraphic>());
            refWorld.registerAdditionalEventdrivenOutput(myWindow);
        }
    }

    /**
     * Gibt die aktuell bekannte Position des angegebenen Asuros zurück. Liegt keine Position für die ID vor, wird der Punkt 0|0 zurück gegeben
     * @param strAsuroID die ID des Asuros
     * @return letztte bekannte Position des Asuros. Wenn keine bekannt ist, dann 0|0
     */
    public ase.geometrics.Point getCurrentPosition(String strAsuroID)
    {
        if(eigenePosition.containsKey(strAsuroID))
            return new ase.geometrics.Point(eigenePosition.get(strAsuroID).getX(),eigenePosition.get(strAsuroID).getY());
        else
            return new ase.geometrics.Point(0, 0);

    }

    public void notification(messageType mType, Object[] values, IOutputEventCreator acuator) {
        if (!refWorld.equals(null)) {
            try {
                if (acuator.getClass().getSimpleName().equals("Asuroinstance")) {
                    if (values.length >= 3) {
                        if (values[0].equals("asuromind punkt")) {
                            checkAsuro(acuator.getInstanceName());
                            int x = (int) (refWorld.convertMetric((Integer) (values[1]), unitOfLenght.mm, unitOfLenght.LE));
                            int y = (int) (refWorld.convertMetric((Integer) (values[2]), unitOfLenght.mm, unitOfLenght.LE));
                            java.awt.Color color = java.awt.Color.BLACK;
                            int intLifeTime = -1;
                            String label = "";
                            if (values.length > 3) {
                                color = (java.awt.Color) (values[3]);
                            }
                            if (values.length > 4) {
                                intLifeTime = (Integer) (values[4]);
                            }
                            if (values.length == 6) {
                                label = (String) (values[5]);
                            }
                            graphicsList.get(acuator.getInstanceName()).add(new amdPoint(x, y, color, intLifeTime, label));
                        } else if (values[0].equals("asuromind kreis")) {
                            checkAsuro(acuator.getInstanceName());
                            int x = (int) (refWorld.convertMetric((Integer) (values[1]), unitOfLenght.mm, unitOfLenght.LE));
                            int y = (int) (refWorld.convertMetric((Integer) (values[2]), unitOfLenght.mm, unitOfLenght.LE));
                            int radius = (int) (refWorld.convertMetric((Integer) (values[3]), unitOfLenght.mm, unitOfLenght.LE));
                            java.awt.Color color = java.awt.Color.BLACK;
                            int intLifeTime = -1;
                            String label = "";
                            if (values.length > 4) {
                                color = (java.awt.Color) (values[4]);
                            }
                            if (values.length > 5) {
                                intLifeTime = (Integer) (values[5]);
                            }
                            if (values.length == 7) {
                                label = (String) (values[6]);
                            }
                            graphicsList.get(acuator.getInstanceName()).add(new amdCircle(x, y, radius, color, intLifeTime, label));
                        } else if (values[0].equals("asuromind linie")) {
                            checkAsuro(acuator.getInstanceName());
                            int x = (int) (refWorld.convertMetric((Integer) (values[1]), unitOfLenght.mm, unitOfLenght.LE));
                            int y = (int) (refWorld.convertMetric((Integer) (values[2]), unitOfLenght.mm, unitOfLenght.LE));
                            int x2 = (int) (refWorld.convertMetric((Integer) (values[3]), unitOfLenght.mm, unitOfLenght.LE));
                            int y2 = (int) (refWorld.convertMetric((Integer) (values[4]), unitOfLenght.mm, unitOfLenght.LE));
                            String label = "";
                            java.awt.Color color = java.awt.Color.BLACK;
                            int intLifeTime = -1;
                            if (values.length > 5) {
                                color = (java.awt.Color) (values[5]);
                            }
                            if (values.length > 6) {
                                intLifeTime = (Integer) (values[6]);
                            }
                            if (values.length == 8) {
                                label = (String) (values[6]);
                            }
                            graphicsList.get(acuator.getInstanceName()).add(new admLine(x, y, x2, y2, color, intLifeTime, label));
                        } else if (values[0].equals("asuromind dosenpositionen")) {
                            checkAsuro(acuator.getInstanceName());
                            phialList.get(acuator.getInstanceName()).clear();
                            for (int i = 1; i < values.length; i++) {
                                phialList.get(acuator.getInstanceName()).add(new amdPoint((int) (refWorld.convertMetric((((ase.geometrics.Point) (values[i])).x), unitOfLenght.mm, unitOfLenght.LE)), (int) (refWorld.convertMetric((((ase.geometrics.Point) (values[i])).y), unitOfLenght.mm, unitOfLenght.LE)), Color.red, -1, "dose " + i + " von " + acuator.getInstanceName()));
                            }
                        } else if (values[0].equals("asuromind asuropositionen")) {
                            checkAsuro(acuator.getInstanceName());
                            otherAsuroList.get(acuator.getInstanceName()).clear();
                            for (int i = 1; i < values.length; i++) {
                                otherAsuroList.get(acuator.getInstanceName()).add(new amdPoint((int) (refWorld.convertMetric((((ase.geometrics.Point) (values[i])).x), unitOfLenght.mm, unitOfLenght.LE)), (int) (refWorld.convertMetric((((ase.geometrics.Point) (values[i])).y), unitOfLenght.mm, unitOfLenght.LE)), Color.red, -1, "dose " + i + " von " + acuator.getInstanceName()));
                            }
                        } else if (values[0].equals("asuromind addscan")) {
                            checkAsuro(acuator.getInstanceName());
                            scanPositions.get(acuator.getInstanceName()).add(new amdPoint((int) (refWorld.convertMetric((Integer) (values[1]), unitOfLenght.mm, unitOfLenght.LE)), (int) (refWorld.convertMetric((Integer) (values[2]), unitOfLenght.mm, unitOfLenght.LE)), Color.green, 10000, "scan von " + acuator.getInstanceName()));

                        } else if (values[0].equals("asuromind zielhaufen")) {
                            checkAsuro(acuator.getInstanceName());
                            zielHaufen.put(acuator.getInstanceName(), (new amdPoint((int) (refWorld.convertMetric((Integer) (values[1]), unitOfLenght.mm, unitOfLenght.LE)), (int) (refWorld.convertMetric((Integer) (values[2]), unitOfLenght.mm, unitOfLenght.LE)), Color.green, -1, "zielhaufen von " + acuator.getInstanceName())));
                        } 
                    }
                } else if (acuator.getClass().getSimpleName().equals("World")) {
                    if (values.length > 0) {
                        if (values[0].equals("asuromind clear")) {
                            asuroList.clear();
                            graphicsList.clear();
                            zielHaufen.clear();
                            otherAsuroList.clear();
                            phialList.clear();
                            eigenePosition.clear();
                            scanPositions.clear();
                        }
                    }
                } else if (acuator.getClass().getSimpleName().equals("Asurohardware")) {
                    if (values.length == 4) {
                        if (values[0].equals("asuromind ownposition")) {
                            checkAsuro((String) (values[1]));
                            eigenePosition.put((String) (values[1]), new amdPoint((Integer) (values[2]), (Integer) (values[3]), Color.ORANGE, -1, (String) (values[1])));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage() + e.getStackTrace());
            }
        }
    }

    public void close() {
        for (ase.outputs.asuroMindDisplay.mindDisplayWindow current : asuroList.values()) {
            current.kill();
        }

        asuroList.clear();
        graphicsList.clear();
        zielHaufen.clear();
        otherAsuroList.clear();
        phialList.clear();
        eigenePosition.clear();
        scanPositions.clear();
    }

    public String getInstanceName() {
        return name;
    }
}

package ase.datalayers;

import ase.geometrics.Point;
import ase.geometrics.Position;

/**
 * Speichert alle Parameter die für ein Szenario nötig sind, wie z.B. die Größe des Testgeländes, der Dosen oder der Asuros.
 * @author Tobi
 */
public class SzenarioParameters  implements java.io.Serializable
{

    /**
     * @return Umrechnungsfaktor von Millimetern zur internen Längeneinheit
     */
    public int getMM_to_UnitOfLenght() {
        return intMM_to_UnitOfLenght;
    }

    /**
     * Setzt den Umrechnungsfaktor von Millimetern zur internen Längeneinheit
     * @param intMM_to_UnitOfLenght Umrechnungsfaktor von Millimetern zur internen Längeneinheit
     */
    public void setMM_to_UnitOfLenght(int intMM_to_UnitOfLenght) {
        this.intMM_to_UnitOfLenght = intMM_to_UnitOfLenght;
    }

    /**
     * Legt fest in welcher Längeneinheit die Startpositionen der Dosen und Asuros vorliegen
     * @return Die Einheit in der die Startpositionen der Dosen und Asuros abgelegt werden
     */
    public unitOfLenght getUStartPositionsUnit() {
        return uStartPositionsUnit;
    }

    /**
     * Legt fest in welcher Längeneinheit die Startpositionen der Dosen und Asuros vorliegen
     * @param uStartPositionsUnit Die Einheit der Startpositionskoordinaten
     */
    public void setUStartPositionsUnit(unitOfLenght uStartPositionsUnit) {
        this.uStartPositionsUnit = uStartPositionsUnit;
    }

    /**
     * die Distanz die ein Asuro mit beiden Rädern rückwärts fahren muss, sodass er die Flasche verliert
     * @return die Distanz (in LE) die ein Asuro mit beiden Rädern rückwärts fahren muss, sodass er die Flasche verliert
     */
    public int getIntDeltaDistanceDropPhial() {
        return intDeltaDistanceDropPhial;
    }

    /**
     * die Distanz die ein Asuro mit beiden Rädern rückwärts fahren muss, sodass er die Flasche verliert
     * @param unit die Längeneinheit in der die Größe zurück gegeben werden soll
     * @return die Distanz die ein Asuro mit beiden Rädern rückwärts fahren muss, sodass er die Flasche verliert
     */
    public double getIntDeltaDistanceDropPhial(unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            return intDeltaDistanceDropPhial;
        if(unit.equals(unitOfLenght.mm))
            return intDeltaDistanceDropPhial / getMM_to_UnitOfLenght();
        if(unit.equals(unitOfLenght.cm))
            return intDeltaDistanceDropPhial / getMM_to_UnitOfLenght() / 10;
        //meter
         return intDeltaDistanceDropPhial / getMM_to_UnitOfLenght() / 1000;
    }

    /**
     * Legt fest, wie lange ein Asuro mit beiden Rädern rückwärts fahren muss, sodass er die Dose verliert
     * @param intDeltaDistanceDropPhial die Distanz die ein Asuro mit beiden Rädern rückwärts fahren muss, sodass er die Flasche verliert
     */
    public void setIntDeltaDistanceDropPhial(int intDeltaDistanceDropPhial) {
        this.intDeltaDistanceDropPhial = intDeltaDistanceDropPhial;
    }

        public void setIntDeltaDistanceDropPhial(double intDeltaDistanceDropPhial, unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            this.intDeltaDistanceDropPhial = (int)intDeltaDistanceDropPhial;
        if(unit.equals(unitOfLenght.cm))
            this.intDeltaDistanceDropPhial = (int)(intDeltaDistanceDropPhial * 10 * getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.m))
            this.intDeltaDistanceDropPhial = (int)(intDeltaDistanceDropPhial * 1000 * getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.mm))
            this.intDeltaDistanceDropPhial = (int)(intDeltaDistanceDropPhial * getMM_to_UnitOfLenght());
    }


    /**
     * Die Einheit in der eine Längeneinheit gegeb werden kann: Meter, Centimeter, Millimeter und Längeneinheit
     */
    public static enum unitOfLenght
    {
        m,
        cm,
        mm,
        LE;
    }

    /**
     * Wie viele Längeneinheiten ban benötigt um einen Millimenter zu erhalten
     */
    private int intMM_to_UnitOfLenght = 1;

    private unitOfLenght uStartPositionsUnit = unitOfLenght.LE;


    /**
     * Distance to drive backwards with both motors to loose a phial in LE
     */
    private int intDeltaDistanceDropPhial = 1;

    /**
     * In welchem Abstand Kollisionsprüfpunkte gesetzt werden sollen in LE
     */
    private int intClashPointDistance = 5;

     /**
     * In welchem Abstand Kollisionsprüfpunkte auf einer Kreislinie gesetzt werden sollen in Grad
     */
    private int intClashPointsdistanceDegree = 4;
    
    /**
     * Die Größe des Testgeländes in cm
     */
    private Point sizeOfWorld = new Point();

    /**
     * der Betrag der Länge (Y) und der Breite (X) eines Asuros
     */
    private Point sizeOfAsuros = new Point();
    
    /**
     * der Radius einer Dose
     */
    private int sizeOfPhials = 1;
    
    /**
     * Ein Array das alle Startpositionen der Dosen enthält
     */
    private Point[] startPointsPhials;
    
    /**
     * Ein Array das alle Startpositionen der Asuros enthält
     */
    private Position[] StartPositionsAsuros;

    /**
     * Ab welcher Entfernung kann ein Objekt mithilfe des Abstandssensors ermittelt werden? (in mm)
     */
    private double distanceMeasureMinDistance = 0;

    /**
     * Bis zu welcher Entfernung kann ein Objekt mithilfe eines Abstandsensors ermittelt werden? (in mm)
     */
    private double distanceMeasureMaxDistance = 1;
    
    /**
     * Wie viele Millimeter liegen zwischen zwei Kollisionskontrolllinien des AbstandSensorStrahls? (in mm)
     */
    private double distanceMeasureDistanceSteps = 1;

    /**
     * Wie groß ist der Streuwinkel des Abstandsensors
     */
    private double distanceMeasureScatteringAngle = 0.1;

    /**
     * In welchem Abstand Kollisionsprüfpunkte gesetzt werden sollen in LE
     * @return die Distanz der Kollisionskontrollpunkte
     */
    public int getClashPointDistance() {
        return (int)getintClashPointDistance(unitOfLenght.LE);
    }
    
    /**
     * In welchem Abstand Kollisionsprüfpunkte gesetzt werden sollen in
     * @param unit Längeneinheit in der die Größe zurückgegeben werden soll
     * @return der Abstand der Kollisionskontrollpunkte in beliebiger Längeneinheit
     */
    public double getintClashPointDistance(unitOfLenght unit) {
        if(unit== unitOfLenght.LE)
            return intClashPointDistance;
        if(unit== unitOfLenght.cm)
            return intClashPointDistance / 10.0 / getMM_to_UnitOfLenght();
        if(unit.equals(unitOfLenght.m))
            return intClashPointDistance / 1000.0 / getMM_to_UnitOfLenght();
        if(unit.equals(unitOfLenght.m))
            return intClashPointDistance / getMM_to_UnitOfLenght();
        //Fake
        return intClashPointDistance;
    }

    /**
     * In welchem Abstand Kollisionsprüfpunkte gesetzt werden sollen in LE
     * @param clashPointDistance der Abstand zweier Kollisionskontrollpunkte zueinander (in LE)
     */
    public void setClashPointDistance(int clashPointDistance) {
        setClashPointDistance(clashPointDistance, unitOfLenght.LE);
    }

        /**
     * In welchem Abstand Kollisionsprüfpunkte gesetzt werden sollen
     * @param clashPointDistance der Abstand zweier Kollisionskontrollpunkte zueinander
     * @param unit die Längeneinheit in der die Größe vorliegt
     */
    public void setClashPointDistance(double clashPointDistance, unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            this.intClashPointDistance = (int)clashPointDistance;
        if(unit.equals(unitOfLenght.cm))
            this.intClashPointDistance = (int)(clashPointDistance * 10 * getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.m))
            this.intClashPointDistance = (int)(clashPointDistance * 1000 * getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.mm))
            this.intClashPointDistance = (int)(clashPointDistance * getMM_to_UnitOfLenght());
    }


    /**
     * Setzt die KollisionsPunkteAbstandsGröße für Kreise fest (in Winkel)
     * @param clashPointDistanceDegree Winkelabstand von Kollisionskontrollpunkten im Kreis
     */
    public void setClashPointDistanceDegree(int clashPointDistanceDegree) {
        this.intClashPointsdistanceDegree = clashPointDistanceDegree;
    }

    /**
     * Setzt die KollisionsPunkteAbstandsGröße für Kreise fest (in Winkel)
     * @return intClashPointsdistanceDegree Winkelabstand von Kollisionskontrollpunkten im Kreis
     */
    public int getClashPointDistanceDegree() {
        return intClashPointsdistanceDegree;
    }


    /**
     * gibt den Betrag der Länge (Y) und der Breite (X) eines Asuros zurück in LE
     * @return die Größe eines Asuros als Punkt-Information
     */
    public Point getSizeOfAsuro() {
        return getSizeOfAsuro(unitOfLenght.LE);
    }

     /**
     * gibt den Betrag der Länge (Y) und der Breite (X) eines Asuros zurück
     * @param unit die Längeneinheit in der die Größe zurückgegeben werden soll
     * @return die Größe eines Asuros als Punkt-Information, in beliebiger Einheit
     */
    public Point getSizeOfAsuro(unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            return this.sizeOfAsuros;
        if(unit.equals(unitOfLenght.cm))
            return new Point(this.sizeOfAsuros.x / 10 / getMM_to_UnitOfLenght(), this.sizeOfAsuros.y / 10 / getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.m))
            return new Point(this.sizeOfAsuros.x / 1000 / getMM_to_UnitOfLenght(), this.sizeOfAsuros.y / 1000 / getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.mm))
            return new Point(this.sizeOfAsuros.x / getMM_to_UnitOfLenght(), this.sizeOfAsuros.y / getMM_to_UnitOfLenght());
        //fake
            return this.sizeOfAsuros;
    }


    /**
     * setzt der Betrag der Länge (Y) und der Breite (X) eines Asuros in LE
     * @param asuroGroesse die Größe eines Asuros in LE als Punkt-Information
     */
    public void setSizeOfAsuro(Point asuroGroesse) {
        setSizeOfAsuro(asuroGroesse, unitOfLenght.LE);
    }

        /**
     * setzt der Betrag der Länge (Y) und der Breite (X) eines Asuros
     * @param sizeOfAsuro die Größe eines Asuros in LE als Punkt-Information
     * @param unit die Längeneinheit des Parameters <i>sizeOfAsuros</i>
     */
    public void setSizeOfAsuro(Point sizeOfAsuro, unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            this.sizeOfAsuros = sizeOfAsuro;
        if(unit.equals(unitOfLenght.cm))
            this.sizeOfAsuros = new Point((int)(sizeOfAsuro.x * 10 * getMM_to_UnitOfLenght()), (int)(sizeOfAsuro.y * 10 * getMM_to_UnitOfLenght()));
        if(unit.equals(unitOfLenght.m))
            this.sizeOfAsuros = new Point((int)(sizeOfAsuro.x * 1000 * getMM_to_UnitOfLenght()), (int)(sizeOfAsuro.y * 1000 * getMM_to_UnitOfLenght()));
        if(unit.equals(unitOfLenght.mm))
            this.sizeOfAsuros = new Point((int)(sizeOfAsuro.x * getMM_to_UnitOfLenght()), (int)(sizeOfAsuro.y * getMM_to_UnitOfLenght()));
    }

    /**
     * gibt den Betrag der Länge (Y) und der Breite (X) einer Dose zurück in LE
     * @return der Radius der Dosen in LE
     */
    public int getSizeOfPhials() {
        return (int)getSizeOfPhials(unitOfLenght.LE);
    }

     /**
     * gibt den Betrag der Länge (Y) und der Breite (X) einer Dose zurück
     * @param unit die Längeneinheit in der die Größe zurück gegeben werden soll
     * @return der Radius der Dosen in einer beliebigen Größeneinheit
     */
    public double getSizeOfPhials(unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            return sizeOfPhials;
        if(unit.equals(unitOfLenght.cm))
            return sizeOfPhials / getMM_to_UnitOfLenght() / 10;
        if(unit.equals(unitOfLenght.m))
            return sizeOfPhials / getMM_to_UnitOfLenght() / 1000;
        if(unit.equals(unitOfLenght.m))
            return sizeOfPhials / getMM_to_UnitOfLenght();
        //fake
        return sizeOfPhials;
    }

    /**
     * setzt den Radius der Dosen
     * @param dosenGroesse der Radius der Dosen in LE
     */
    public void setSizeOfPhials(int dosenGroesse) {
        this.sizeOfPhials = dosenGroesse;
    }

    /**
     * setzt den Radius der Dosen
     * @param dosenGroesse der Radius der Dosen in einer belibigen Einheit
     * @param unit die Längeneinheit des Parameters <i>sizeOfPhial</i>
     */
    public void setSizeOfPhials(double dosenGroesse, unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            this.sizeOfPhials = (int)dosenGroesse;
        if(unit.equals(unitOfLenght.cm))
            this.sizeOfPhials = (int)(dosenGroesse * 10 * getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.m))
            this.sizeOfPhials = (int)(dosenGroesse * 1000 * getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.mm))
            this.sizeOfPhials = (int)(dosenGroesse * getMM_to_UnitOfLenght());
    }

    /**
     * gibt das Array zürück, das alle Startpositionen der Dosen enthält (in Längeneinheiten)
     * @return Gibt die Startpositionen der Dosen zurück
     */
    public Point[] getStartPointsPhials() {
        return startPointsPhials;
    }

    /**
     * setzt dsa Array das alle Startpositionen der Dosen enthält (in Längeneinheiten)
     * @param startPointsPhials
     */
    public void setStartPointsPhials(Point[] startPointsPhials) {
        this.startPointsPhials = startPointsPhials;
    }

    /**
     * gibt Array zurück, das alle Startpositionen der Asuros enthält
     * @return die Startpositionen der Asuros
     */
    public Position[] getStartPositionsAsuros() {
        return StartPositionsAsuros;
    }

    /**
     * setzt das Array das alle Startpositionen der Dosen enthält (in LE)
     * @param startPositionsAsuros
     */
    public void setAsuroPositionen(Position[] startPositionsAsuros) {
        this.StartPositionsAsuros = startPositionsAsuros;
    }

    /**
     * gibt die Größe der Testfläche aus in Längeneinheiten
     * @return die Größe der Welt als Punkt-Information in Längeneinheiten
     */
    public Point getSizeOfWorld() {
        return getSizeOfWorld(unitOfLenght.LE);
    }

    /**
     * setzt die Größe des Testgeländes
     * @param unit Die Längeneinheit in der die Größe zurückgegeben werden soll
     * @return die Größe der Welt als Punkt-Information in beliebiger Längeneinheit
     */
    public Point getSizeOfWorld(unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            return sizeOfWorld;
        if(unit.equals(unitOfLenght.cm))
            return new Point(sizeOfWorld.x / 10 / getMM_to_UnitOfLenght(),sizeOfWorld.y / 10 / getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.m))
            return new Point(sizeOfWorld.x / 1000 / getMM_to_UnitOfLenght(),sizeOfWorld.y / 1000 / getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.mm))
            return new Point(sizeOfWorld.x / getMM_to_UnitOfLenght(),sizeOfWorld.y / getMM_to_UnitOfLenght());
        //fake
            return sizeOfWorld;
    }

    /**
     * Setzt die Größes des Testgeländes in LE
     * @param sizeOfWorld die Größe des Testgeländes in LE
     */
    public void setSizeOfWorld(Point sizeOfWorld) {
    setSizeOfWorld(sizeOfWorld, unitOfLenght.LE);
    }

     /**
     * Setzt die Größes des Testgeländes
     * @param unit Die Längeneinheit in der die Größe des Testgeländes zurückgegeben werden soll
     * @param sizeOfWorld die Größe des Testgeländes in beliebiger Längeneinheit
     */
    public void setSizeOfWorld(Point sizeOfWorld, unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            this.sizeOfWorld = sizeOfWorld;
        if(unit.equals(unitOfLenght.cm))
            this.sizeOfWorld = new Point((int)(sizeOfWorld.x * 10 * getMM_to_UnitOfLenght()), (int)(sizeOfWorld.y * 10 * getMM_to_UnitOfLenght()));
        if(unit.equals(unitOfLenght.m))
            this.sizeOfWorld = new Point((int)(sizeOfWorld.x * 1000 * getMM_to_UnitOfLenght()), (int)(sizeOfWorld.y * 1000 * getMM_to_UnitOfLenght()));
        if(unit.equals(unitOfLenght.mm))
            this.sizeOfWorld = new Point((int)(sizeOfWorld.x * getMM_to_UnitOfLenght()), (int)(sizeOfWorld.y * getMM_to_UnitOfLenght()));
    }

    /**
     * Ab welcher Entfernung kann ein Objekt mithilfe des Abstandssensors ermittelt werden?
     * @return the getDistanceMeasureMinDistance in Längeneinheiten
     */
    public int getDistanceMeasureMinDistance() {
        return (int)getDistanceMeasureMinDistance(unitOfLenght.LE);
    }

        /**
     * Ab welcher Entfernung kann ein Objekt mithilfe des Abstandssensors ermittelt werden?
     * @param unit die Längeneinheit in der die Größe zurückgegben werden soll
     * @return the getDistanceMeasureMinDistance
     */
    public double getDistanceMeasureMinDistance(unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            return distanceMeasureMinDistance;
        if(unit.equals(unitOfLenght.mm))
            return distanceMeasureMinDistance / getMM_to_UnitOfLenght();
        if(unit.equals(unitOfLenght.cm))
            return distanceMeasureMinDistance / getMM_to_UnitOfLenght() / 10.0;
        if(unit.equals(unitOfLenght.m))
            return distanceMeasureMinDistance / getMM_to_UnitOfLenght() / 1000.0;
        //fake
        return distanceMeasureMinDistance;
    }

    /**
     * Ab welcher Entfernung kann ein Objekt mithilfe des Abstandssensors ermittelt werden? (in LE)
     * @param distanceMeasureMinDistance Die minimale Entfernung die der Abstandssensor erfassen kann
     */
    public void setdistanceMeasureMinDistance(int distanceMeasureMinDistance) {
        setDistanceMeasureMinDistance(distanceMeasureMinDistance,unitOfLenght.LE);
    }



    /**
     * Ab welcher Entfernung kann ein Objekt mithilfe des Abstandssensors ermittelt werden? (in mm)
     * @param distanceMeasureMinDistance Die minimale Entfernung die der Abstandssensor erfassen kann
     * @param unit die Längeneinheit in der die Größe zurückgegeben werden soll
     */
    public void setDistanceMeasureMinDistance(double distanceMeasureMinDistance,unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            this.distanceMeasureMinDistance = (int)(distanceMeasureMinDistance);
        if(unit.equals(unitOfLenght.mm))
            this.distanceMeasureMinDistance = (int)(distanceMeasureMinDistance * getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.cm))
            this.distanceMeasureMinDistance = (int)(distanceMeasureMinDistance * getMM_to_UnitOfLenght() * 10);
        if(unit.equals(unitOfLenght.m))
            this.distanceMeasureMinDistance = (int)(distanceMeasureMinDistance * getMM_to_UnitOfLenght() * 1000);
    }


    /**
     * Bis zu welcher Entfernung kann ein Objekt mithilfe eines Abstandsensors ermittelt werden? (in LE)
     * @return Die maximale Entfernung die der Abstandssensor erfassen kann
     */
    public int getDistanceMeasureMaxDistance() {
        return (int)distanceMeasureMaxDistance;
    }

     /**
     * Bis zu welcher Entfernung kann ein Objekt mithilfe eines Abstandsensors ermittelt werden?
     * @param unit die Längeneinheit in der die Größe zurückgegeben werden soll
     * @return Die maximale Entfernung die der Abstandssensor erfassen kann
     */
    public double getDistanceMeasureMaxDistance(unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            return distanceMeasureMaxDistance;
        if(unit.equals(unitOfLenght.mm))
            return distanceMeasureMaxDistance / getMM_to_UnitOfLenght();
        if(unit.equals(unitOfLenght.cm))
            return distanceMeasureMaxDistance / getMM_to_UnitOfLenght() / 10.0;
        if(unit.equals(unitOfLenght.m))
            return distanceMeasureMaxDistance / getMM_to_UnitOfLenght() / 100.0;
        return distanceMeasureMaxDistance;
    }

    /**
     * Bis zu welcher Entfernung kann ein Objekt mithilfe eines Abstandsensors ermittelt werden? (in LE)
     * @param distanceMeasureMaxDistance Die maximale Entfernung die der Abstandssensor erfassen kann
     */
    public void setDistanceMeasureMaxDistance(int distanceMeasureMaxDistance) {
        setDistanceMeasureMaxDistance(distanceMeasureMaxDistance, unitOfLenght.LE);
    }

    /**
     * Bis zu welcher Entfernung kann ein Objekt mithilfe eines Abstandsensors ermittelt werden?
     * @param distanceMeasureMaxDistance Die maximale Entfernung die der Abstandssensor erfassen kann
     * @param unit die Längeneinheit in der der Parameter gegeben ist
     */
    public void setDistanceMeasureMaxDistance(double distanceMeasureMaxDistance, unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            this.distanceMeasureMaxDistance = (int)distanceMeasureMaxDistance;
        if(unit.equals(unitOfLenght.mm))
            this.distanceMeasureMaxDistance = (int)(distanceMeasureMaxDistance * getMM_to_UnitOfLenght());
        if(unit.equals(unitOfLenght.cm))
            this.distanceMeasureMaxDistance = (int)(distanceMeasureMaxDistance * getMM_to_UnitOfLenght() * 10);
        if(unit.equals(unitOfLenght.m))
            this.distanceMeasureMaxDistance = (int)(distanceMeasureMaxDistance * getMM_to_UnitOfLenght() * 1000);
    }

    /**
     * Wie viele Millimeter liegen zwischen zwei Kollisionskontrolllinien des AbstandSensorStrahls? (in mm)
     * @return Die Messgenauigkeit des Abstandssensors in LE
     */
    public int getDistanceMeasureDistanceStep() {
        return (int)getDistanceMeasureDistanceStep(unitOfLenght.LE);
    }

     /**
     * Wie viele Millimeter liegen zwischen zwei Kollisionskontrolllinien des AbstandSensorStrahls? (in mm)
     * @param unit die Längeneinheit in der die Größe zurückgegeben werden soll
     * @return Die Messgenauigkeit des Abstandssensors in einer beliebigen Einheit
     */
    public double getDistanceMeasureDistanceStep(unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            return distanceMeasureDistanceSteps;
        if(unit.equals(unitOfLenght.mm))
            return distanceMeasureDistanceSteps / getMM_to_UnitOfLenght();
        if(unit.equals(unitOfLenght.cm))
            return distanceMeasureDistanceSteps / getMM_to_UnitOfLenght() / 10;
        //meter
         return distanceMeasureDistanceSteps / getMM_to_UnitOfLenght() / 1000;
    }

    /**
     * Wie viele Millimeter liegen zwischen zwei Kollisionskontrolllinien des AbstandSensorStrahls? (in LE)
     * @param distanceMeasureDistanceStep Die Messgenauigkeit des Abstandssensors
     */
    public void setDistanceMeasureDistanceStep(int distanceMeasureDistanceStep) {
        setDistanceMeasureDistanceStep(distanceMeasureDistanceStep,unitOfLenght.LE);
    }

        /**
     * Wie viele Millimeter liegen zwischen zwei Kollisionskontrolllinien des AbstandSensorStrahls?
     * @param distanceMeasureDistanceStep Die Messgenauigkeit des Abstandssensors
     * @param unit die Einheit in der die MEssgenauigkeit vorliegt
     */
    public void setDistanceMeasureDistanceStep(double distanceMeasureDistanceStep, unitOfLenght unit) {
        if(unit.equals(unitOfLenght.LE))
            this.distanceMeasureDistanceSteps = distanceMeasureDistanceStep;
        if(unit.equals(unitOfLenght.mm))
            this.distanceMeasureDistanceSteps = distanceMeasureDistanceStep * getMM_to_UnitOfLenght();
        if(unit.equals(unitOfLenght.cm))
            this.distanceMeasureDistanceSteps = distanceMeasureDistanceStep * getMM_to_UnitOfLenght() * 10;
        if(unit.equals(unitOfLenght.m))
            this.distanceMeasureDistanceSteps = distanceMeasureDistanceStep * getMM_to_UnitOfLenght() * 1000;
    }

    /**
     * Wie groß ist der Streuwinkel des Abstandsensors (FUNKTION NOCH NICHT IN DER HARDWARE IMPLEMENTIERT)
     * @return Der Streuwinkel des Abstandsensors
     */
    public double getDistanceMeasureScatteringAngle() {
        return distanceMeasureScatteringAngle;
    }

    /**
     * Wie groß ist der Streuwinkel des Abstandsensors (FUNKTION NOCH NICHT IN DER HARDWARE IMPLEMENTIERT)
     * @param abstandsMesserStreuwinkel the distanceMeasureScatteringAngle to set
     */
    public void setDistanceMeasureScatteringAngle(double abstandsMesserStreuwinkel) {
        this.distanceMeasureScatteringAngle = abstandsMesserStreuwinkel;
    }
}
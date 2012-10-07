package ase.datalayers;
import ase.geometrics.Point;
import ase.geometrics.Position;
import ase.datalayers.SzenarioParameters.unitOfLenght;
import java.io.Serializable;
import java.io.*;

/**
 * Eine ContainerKlasse die eine Instanz von <i>SzenarioParameters</i> und <i>Timecontrol</i> enthält, um diese gemeinsam zu serialisieren oder an <i>World</i> zu übergeben
 * @see TimeControl
 * @see SzenarioParameters
 * @author Tobi
 */
public class SzenarioContainer implements Serializable
{
    public ase.datalayers.SzenarioParameters szenarioParams = new ase.datalayers.SzenarioParameters();
    public ase.datalayers.TimeControl timeControl = new ase.datalayers.TimeControl();


    /**
     * Erstellt einen leeren Szenario-Container ohne Werte
     */
    public SzenarioContainer() {
        resetToDefault();
    }

    /**
     * Setzt alle werte des Szenarios auf Default-Werte
     */
    public void resetToDefault()
    {
            //loads default Settings
            //Timecontroling
            timeControl.setAsurocodeThreadSleep(50);
            timeControl.setDeltaT(1);
            timeControl.setObjektbasisThreadSleep(50);
            timeControl.setWorldThreadSleep(1000);
            timeControl.setSzenarioTicksTillEnd(900);
            timeControl.setTimeFactor(1);

            //Define Parameters
            szenarioParams.setMM_to_UnitOfLenght(10);
            szenarioParams.setDistanceMeasureMaxDistance(35,unitOfLenght.cm);
            szenarioParams.setDistanceMeasureMinDistance(7,unitOfLenght.cm);
            szenarioParams.setDistanceMeasureDistanceStep(4,unitOfLenght.mm);
            szenarioParams.setDistanceMeasureScatteringAngle(1);
            szenarioParams.setSizeOfAsuro(new Point(8,12),unitOfLenght.cm);
            szenarioParams.setSizeOfPhials(2,unitOfLenght.cm);
            szenarioParams.setClashPointDistance(15);
            szenarioParams.setClashPointDistanceDegree(5);
            szenarioParams.setSizeOfWorld(new Point(2,2),unitOfLenght.m);
            szenarioParams.setIntDeltaDistanceDropPhial(3,unitOfLenght.cm);

            //Set Startpositions of the phials and the asuros
            szenarioParams.setUStartPositionsUnit(unitOfLenght.cm);
            szenarioParams.setAsuroPositionen(new Position[] {new ase.geometrics.Position(65, 120, 0),new ase.geometrics.Position(165, 130, 30),new ase.geometrics.Position(20, 40, 10)});//
            szenarioParams.setStartPointsPhials(new Point[] {new ase.geometrics.Point(10, 10), new ase.geometrics.Point(60, 10), new ase.geometrics.Point(110, 10), new ase.geometrics.Point(170, 10), new ase.geometrics.Point(30, 5), new ase.geometrics.Point(60, 50), new ase.geometrics.Point(110, 50), new ase.geometrics.Point(170, 50), new ase.geometrics.Point(10, 110), new ase.geometrics.Point(60, 110), new ase.geometrics.Point(110, 110),new ase.geometrics.Point(170, 110), new ase.geometrics.Point(10, 180), new ase.geometrics.Point(60, 180), new ase.geometrics.Point(110, 180), new ase.geometrics.Point(170, 180)});
    }

    @Override
    public String toString() {
       return getString("\n");
    }

    public String getString(String strReturnChar)
    {
        String strReturn ="";
        strReturn += "---SzenarioContainer---" + strReturnChar + strReturnChar + " --TimeControl-- " + strReturnChar;
        strReturn += "deltaT = " + timeControl.getDeltaT() + " ms" + strReturnChar;
        strReturn += "AsuroCodeThreadSleep = " + timeControl.getAsurocodeThreadSleep() + " ms" + strReturnChar;
        strReturn += "AsuroHardwareThreadSleep = " + timeControl.getObjektbasisThreadSleep() + " ms" + strReturnChar;
        strReturn += "WorldThreadSleep = " + timeControl.getWorldThreadSleep() + " ms" + strReturnChar;
        strReturn += "SzenarioTicksTillEnd = " + timeControl.getSzenarioTicksTillEnd() + " units" + strReturnChar;
        strReturn += "TimeFactor = " + timeControl.getTimeFactor()  + strReturnChar;
        strReturn += "" + strReturnChar + " --SzenarioParameters-- " + strReturnChar;
        strReturn += "mm[SI-Unit] to UnitOfLenght = coefficient " + szenarioParams.getMM_to_UnitOfLenght() + "" + strReturnChar;
        strReturn += "DistanceMeasureMaxDistance = " + szenarioParams.getDistanceMeasureMaxDistance() + " LE = " +  szenarioParams.getDistanceMeasureMaxDistance(unitOfLenght.cm) + " cm" + strReturnChar;
        strReturn += "DistanceMeasureMinDistance = " + szenarioParams.getDistanceMeasureMinDistance() + " LE = " + szenarioParams.getDistanceMeasureMinDistance(unitOfLenght.cm) + " cm" + strReturnChar;
        strReturn += "DistanceMeasureDistanceStep = " + szenarioParams.getDistanceMeasureDistanceStep() + " LE = " + szenarioParams.getDistanceMeasureDistanceStep(unitOfLenght.cm) + " cm" + strReturnChar;
        strReturn += "DistanceMeasureScatteringAngle = " + szenarioParams.getDistanceMeasureScatteringAngle() + "°" + strReturnChar;
        strReturn += "SizeOfAsuro = " + szenarioParams.getSizeOfAsuro().toString() + " LE = " + szenarioParams.getSizeOfAsuro(unitOfLenght.cm).toString() + " cm" + strReturnChar;
        strReturn += "SizeOfPhial = " + szenarioParams.getSizeOfPhials() + " LE = " + szenarioParams.getSizeOfPhials(unitOfLenght.cm) + " cm" + strReturnChar;
        strReturn += "ClashPointDistance = " + szenarioParams.getClashPointDistance() + " LE" + strReturnChar;
        strReturn += "ClashPointDistanceDegree = " + szenarioParams.getClashPointDistanceDegree() + " LE" + strReturnChar;
        strReturn += "SizeOfWorld = " + szenarioParams.getSizeOfWorld(unitOfLenght.m) + " m" + strReturnChar;
        strReturn += "IntDeltaDistanceDropPhial = " + szenarioParams.getIntDeltaDistanceDropPhial(unitOfLenght.mm) + " mm" + strReturnChar;
        strReturn += "" + strReturnChar + " --Startpositions-- " + strReturnChar;
        String strUnit = "LE";
        if(szenarioParams.getUStartPositionsUnit() == unitOfLenght.cm)
            strUnit = "cm";
        else if(szenarioParams.getUStartPositionsUnit() == unitOfLenght.m)
            strUnit = "m";
        if(szenarioParams.getUStartPositionsUnit() == unitOfLenght.mm)
            strUnit = "mm";
        for(int i = 0; i < szenarioParams.getStartPositionsAsuros().length;i++)
            strReturn += "Asuro " + i + " starts at " + szenarioParams.getStartPositionsAsuros()[i].toString() + " "+ strUnit + "" + strReturnChar;
        for(int i = 0; i < szenarioParams.getStartPointsPhials().length;i++)
            strReturn += "Phial " + i + " starts at " + szenarioParams.getStartPointsPhials()[i].toString() + " "+ strUnit + "" + strReturnChar;
        strReturn += "----------------------" + strReturnChar;

        return strReturn;
    }




}
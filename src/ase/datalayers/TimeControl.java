package ase.datalayers;

/**
 * Erfasst alle Parameter die wartezeiten von Threads steuern
 * @author Tobi
 */
public class TimeControl implements java.io.Serializable
{
    private int asurohardwareThreadSleep = 100;
    private int worldThreadSleep = 1000;
    private int asurocodeThreadSleep = 50;
    private int deltaT = 10; //Simulationssteps in ms
    private int szenarioTicksTillEnd = 60; //Anzahl der World-Ticks nachdem World das Szenario beenden wird
    private double timeFactor = 1; //Der Faktor mit dem Geschwindigkeiten des Asuros Multipliziert und Wartezeiten des Asuros geteilt werden

    public int getSzenarioTicksTillEnd() {
        return szenarioTicksTillEnd;
    }

    public void setSzenarioTicksTillEnd(int szenarioTicksTillEnd) {
        this.szenarioTicksTillEnd = szenarioTicksTillEnd;
    }
    

    public int getObjektbasisThreadSleep() {
        return asurohardwareThreadSleep;
    }

    public void setObjektbasisThreadSleep(int objektbasisThreadSleep) {
        this.asurohardwareThreadSleep = objektbasisThreadSleep;
    }

    public int getWorldThreadSleep() {
        return worldThreadSleep;
    }

    public void setWorldThreadSleep(int worldThreadSleep) {
        this.worldThreadSleep = worldThreadSleep;
    }

    public int getAsurocodeThreadSleep() {
        return asurocodeThreadSleep;
    }

    public void setAsurocodeThreadSleep(int asurocodeThreadSleep) {
        this.asurocodeThreadSleep = asurocodeThreadSleep;
    }

    public int getDeltaT() {
        return deltaT;
    }

    public void setDeltaT(int deltaT) {
        this.deltaT = deltaT;
    }

    /**
     * @return the timeFactor
     */
    public double getTimeFactor() {
        return timeFactor;
    }

    /**
     * @param timeFactor the timeFactor to set
     */
    public void setTimeFactor(double timeFactor) {
        this.timeFactor = timeFactor;
    }
}
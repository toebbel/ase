package ase.outputs;

/**
 *
 * @author Tobias Sturm
 */
public interface ITimedrivenOutput extends Runnable,ase.INameable {
    
  
    /**
     * registriert ein neues Objekt.
     * @param neuesObjekt
     */
    public void registerObject(Object neuesObjekt);
    
    /**
     * entfernt ein Objekt
     * @param myObjekt
     */
    public void removeObject(Object myObjekt);
    

    /**
     * legt fest in welchem Intervall der Monitor arbeiten muss
     * @param refreshRateMs
     */
    public void setRefreshRate(int refreshRateMs);

    public int getRefreshRate();
    
    /**
     * schließt den Monitor. 
     */
    public void close();

    public void pause();

    public void resume();




}

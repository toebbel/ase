package ase;

import ase.hardware.IDozeable;
import ase.hardware.ICollideable;
import ase.outputs.IEventdrivenOutput;
import ase.outputs.ITimedrivenOutput;
import ase.outputs.IOutputEventCreator;
import ase.geometrics.*;
import ase.datalayers.SzenarioParameters.unitOfLenght;
import ase.datalayers.SzenarioContainer;
import ase.hardware.Asurohardware;
import ase.hardware.phial;

import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World is the supervisor-klass of the simulation. it controls the time and coordinates collision-controling,
 * @author Tobias Sturm
 */
public class World implements Runnable, ase.outputs.IOutputEventCreator, ase.outputs.IDrawable {

    private ConcurrentHashMap<String, ICollideable> myObjects;
    private Point myArea;
    private ConcurrentHashMap<String, IEventdrivenOutput> myEventOutputs = new ConcurrentHashMap<String, IEventdrivenOutput>();
    private ConcurrentHashMap<String, ITimedrivenOutput> myTimeOutputs = new ConcurrentHashMap<String, ITimedrivenOutput>();
    private SzenarioContainer mySzenario = new SzenarioContainer();
    private boolean keepAlive = true;

    public boolean isAlive() {
        return keepAlive;
    }
    //Recording
    private int intCurrentTime = 0;

    /**
     * creates an instance of a simulationenviroment
     */
    public World() {
    }

    /**
     * Startet einen Versuch
     * @param szenario der SzenarioContainer der verwendet wird
     * @param eventOutput ein Array mit allen Instanzen der Ereignisgesteuerten Outputs (!müssen bereits initialisiert worden sein)
     * @param timeOutput ein Array mit allen Instanzen der Zeitgesteuerten Outputs (!müssen bereits initialisiert worden sein)
     * @return gibt zurück, ob der Versuch erfolgreich durchgeführt wurde (true) oder ob ein Fehler auftrat (false)
     */
    public boolean initialize(SzenarioContainer szenario, IEventdrivenOutput[] eventOutput, ITimedrivenOutput[] timeOutput) {
        try {
            myObjects = new ConcurrentHashMap<String, ICollideable>();
            mySzenario = szenario;
            myArea = szenario.szenarioParams.getSizeOfWorld();
            Position[] asuros = mySzenario.szenarioParams.getStartPositionsAsuros();
            Point[] phial = mySzenario.szenarioParams.getStartPointsPhials();
            ase.hardware.Asurohardware myInstance = null;
            ase.hardware.phial myPhial = null;

            for (int i = 0; i < timeOutput.length; i++) {
                myTimeOutputs.put(((INameable) timeOutput[i]).getInstanceName(), timeOutput[i]);
            }


            for (int i = 0; i < asuros.length; i++) {
                if (asuros[i] != null) {
                    myInstance = new Asurohardware("Asuro " + i, new Area(new Position(convertMetric(asuros[i].x, mySzenario.szenarioParams.getUStartPositionsUnit(), unitOfLenght.LE), convertMetric(asuros[i].y, mySzenario.szenarioParams.getUStartPositionsUnit(), unitOfLenght.LE), asuros[i].angle), mySzenario.szenarioParams.getSizeOfAsuro(), mySzenario.szenarioParams.getClashPointDistance()), this);
                    myObjects.put("Asuro " + i, myInstance);

                    if (eventOutput != null) {
                        //register all outputs at the instance
                        for (int j = 0; j < eventOutput.length; j++) {
                            if (eventOutput[j] != null) {
                                myInstance.registerOutput(eventOutput[j]);
                            }
                        }
                    }


                    //register this instance at all timeOutputs
                    for (int k = 0; k < timeOutput.length; k++) {
                        if (timeOutput[k] != null) {
                            timeOutput[k].registerObject(myInstance);
                        }
                    }
                }
            }

            for (int i = 0; i < phial.length; i++) {
                if (phial[i] != null) {
                    myPhial = new phial(new Circle(new Point(convertMetric(phial[i].x, mySzenario.szenarioParams.getUStartPositionsUnit(), unitOfLenght.LE), convertMetric(phial[i].y, mySzenario.szenarioParams.getUStartPositionsUnit(), unitOfLenght.LE)), mySzenario.szenarioParams.getSizeOfPhials(), mySzenario.szenarioParams.getClashPointDistanceDegree()), String.valueOf(i));
                    myObjects.put(myPhial.toString(), myPhial);

                    //register all outputs at the phial
                    for (int j = 0; j < eventOutput.length; j++) {
                        if (eventOutput[j] != null) {
                            myPhial.registerOutput(eventOutput[j]);
                        }
                    }


                    //register this phial at all timeOutputs
                    for (int k = 0; k < timeOutput.length; k++) {
                        if (timeOutput[k] != null) {
                            timeOutput[k].registerObject(myPhial);
                        }
                    }

                }
            }

            //Outputs register each other
            for (int i = 0; i < timeOutput.length; i++) {
                for (int j = 0; j < eventOutput.length; j++) {
                    timeOutput[i].registerObject(eventOutput[j]);
                }
            }

            for (int j = 0; j < timeOutput.length; j++) {
                if (checkImplementation(timeOutput[j].getClass(), "IOutputEventCreator")) {
                    for (int i = 0; i < eventOutput.length; i++) {
                        ((IOutputEventCreator) (timeOutput[j])).registerOutput(eventOutput[i]);
                    }
                }
            }

            for (int j = 0; j < timeOutput.length; j++) {
                if (checkImplementation(timeOutput[j].getClass(), "IOutputEventCreator")) {
                    for (int i = 0; i < timeOutput.length; i++) {
                        if (checkImplementation(timeOutput[i].getClass(), "IEventdrivenOutput")) {
                            ((IOutputEventCreator) (timeOutput[j])).registerOutput((IEventdrivenOutput) timeOutput[i]);
                        }
                    }
                }
            }


            for (int j = 0; j < eventOutput.length; j++) {
                if (checkImplementation(eventOutput[j].getClass(), "IOutputEventCreator")) {
                    for (int i = 0; i < eventOutput.length; i++) {
                        if (!eventOutput[j].equals(eventOutput[i])) {
                            ((IOutputEventCreator) (eventOutput[j])).registerOutput(eventOutput[i]);
                        }
                    }
                }
            }

            for (int j = 0; j < timeOutput.length; j++) {
                for (int i = 0; i < timeOutput.length; i++) {
                    if (i != j) {
                        timeOutput[i].registerObject(timeOutput[j]);
                    }
                }
            }

            //register world itself at monitors
            for (int i = 0; i < eventOutput.length; i++) {
                registerOutput(eventOutput[i]);
            }
            for (int i = 0; i < timeOutput.length; i++) {
                timeOutput[i].registerObject(this);
            }


            //Prüfen ob sich ein Objekt mit einem anderem Überlappt
            boolean bolIncorrectStartPoints = false;
            for (ICollideable current : myObjects.values()) {
                if (this.checkCollision(current.getClashPoints(), current)) {
                    bolIncorrectStartPoints = true;
                    break;
                }
            }

            if (bolIncorrectStartPoints) {
                notifyMonitors(IEventdrivenOutput.messageType.ERROR, new String[]{"Die Startpositionen waren fehlerhaft!"});
                return false;
            }

            Thread thisThread = new Thread(this);
            thisThread.setName("Thread of world");
            thisThread.start();
            return true;
        } catch (Exception e) {
            System.out.println("Error:" + e.getLocalizedMessage());
            return false;
        }

    }

    public void registerAdditionalEventdrivenOutput(IEventdrivenOutput newOutput) {
        for (ICollideable current : myObjects.values()) {
            if (checkImplementation(current.getClass(), "IOutputEventCreator")) {
                ((IOutputEventCreator) current).registerOutput(newOutput);
            }
        }

        for (IEventdrivenOutput current : myEventOutputs.values()) {
            if (checkImplementation(current.getClass(), "IOutputEventCreator")) {
                ((IOutputEventCreator) current).registerOutput(newOutput);
            }
        }

        for (ITimedrivenOutput current : myTimeOutputs.values()) {
            if (checkImplementation(current.getClass(), "IOutputEventCreator")) {
                ((IOutputEventCreator) current).registerOutput(newOutput);
            }
        }

        if (checkImplementation(newOutput.getClass(), "IOutputEventCreator")) {
            for (ICollideable current : myObjects.values()) {
                if (checkImplementation(current.getClass(), "IEventdrivenOutput")) {
                    ((IOutputEventCreator) newOutput).registerOutput((IEventdrivenOutput) current);
                }
            }
            for (ITimedrivenOutput current : myTimeOutputs.values()) {
                if (checkImplementation(current.getClass(), "IEventdrivenOutput")) {
                    ((IOutputEventCreator) newOutput).registerOutput((IEventdrivenOutput) current);
                }
            }
            for (IEventdrivenOutput current : myEventOutputs.values()) {
                if (checkImplementation(current.getClass(), "IEventdrivenOutput")) {
                    ((IOutputEventCreator) newOutput).registerOutput((IEventdrivenOutput) current);
                }
            }
        }

        myEventOutputs.put(newOutput.getInstanceName(), newOutput);
    }

    public void registerAdditionalTimetdrivenOutput(ITimedrivenOutput newOutput) {
          for (ICollideable current : myObjects.values()) {
            newOutput.registerObject(current);
            newOutput.registerObject(current);
        }

        for (IEventdrivenOutput current : myEventOutputs.values()) {
            newOutput.registerObject(current);
            newOutput.registerObject(current);
        }

        for (ITimedrivenOutput current : myTimeOutputs.values()) {
            newOutput.registerObject(current);
            newOutput.registerObject(current);
        }

        myTimeOutputs.put(newOutput.getInstanceName(), newOutput);
    }

    private boolean checkImplementation(Class Instance, String InterfaceName) {
        if (Instance.getPackage().getName().startsWith("ase")) {
            boolean bolImplements = false;
            if (Instance.getSimpleName().equals(InterfaceName)) {
                return true;
            }

            if (Instance.getSuperclass() != null) {
                if (checkImplementation(Instance.getSuperclass(), InterfaceName)) {
                    bolImplements = true;
                }
            }

            for (int j = 0; j < Instance.getInterfaces().length; j++) {
                if (checkImplementation(Instance.getInterfaces()[j], InterfaceName)) {
                    bolImplements = true;
                }
            }

            return bolImplements;
        }
        return false;
    }

    /**
     * Checks if there is a collision between the actuator and any other object. if the actuator moves out of the world-area this function returns a collision
     * @param actuator the acutator of the collision-test
     * @return true = collision, false = no collision
     */
    public boolean checkCollision(ICollideable actuator) {
        return checkCollision(actuator.getClashPoints(), actuator);
    }

    public double getTimeFactor() {
        return mySzenario.timeControl.getTimeFactor();
    }

    /**
     * Prüft ob eine Kollision besteht, woebi mehrere Auslösepartner verwickelt sein können (z.B. Muss ein Asuro prüfen, ob er eine Dose aufnehmen kann, ohne dass eine Kollision ensteht.
     * Entdeckt diese Methode eine Kollision geschiet nichts; es wird nur 'true' zurpck gegeben; ansonsten false
     * @param clashPoints
     * @param actuators mehrere Instanzen die von der Kollisionsprüfung ausgenommen werden
     * @return Gibt an ob der auslöser kollidiert
     */
    public boolean checkCollisionForStartDoze(Point[] clashPoints, ICollideable[] actuators) {
        for (int i = 0; i < clashPoints.length; i++) {
            if (clashPoints[i] != null) {
                if (clashPoints[i].x < 0 || clashPoints[i].x > myArea.x || clashPoints[i].y < 0 || clashPoints[i].y > myArea.y) {
                    return true;
                }
            }
        }




        for (ICollideable current : myObjects.values()) {
            boolean bolCheck = true;
            for (int i = 0; i < actuators.length; i++) {
                if (actuators[i].equals(current)) {
                    bolCheck = false;
                    break;
                }
            }

            if (bolCheck) {
                if (current.checkCollision(clashPoints, actuators[0])) {
                    return true;
                }
            }


        }

        return false;
    }

    /**
     * Checks if there is a collision between the actuator and any other object. if the actuator moves out of the world-area this function returns a collision
     * @param clashPoints array of points to check; if any point collides with another object or is outside of the world, this function returns true
     * @param actuator the actuator of the collison-test
     * @return true = collision; false = no collision
     */
    public boolean checkCollision(Point[] clashPoints, ICollideable actuator) {
        //check if all Points are inside the world
        for (int i = 0; i < clashPoints.length; i++) {
            if (clashPoints[i] != null) {
                if (clashPoints[i].x < 0 || clashPoints[i].x > myArea.x || clashPoints[i].y < 0 || clashPoints[i].y > myArea.y) {
                    return true;
                }
            }
        }


        //Test for each object till a collision
        for (ICollideable current : myObjects.values()) {
            //no collision-testing with itself
            if (!actuator.equals(current)) {
                if (current.checkCollision(clashPoints, actuator)) {
                    //this.notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[] {"collide"});
                    boolean bolCanDoze = false;
                    for (int i = 0; i < current.getClass().getInterfaces().length; i++) {
                        if (current.getClass().getInterfaces()[i].getSimpleName().equals("IDozeable")) {
                            if (!((IDozeable) current).isDozed()) {
                                this.notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"collide with sth. which implements IDozeable"});
                                if (current.checkCollision(actuator.getDozePoints(), actuator)) {
                                    if (actuator.doze((IDozeable) current)) //check if actuator can pull a IDozable Object
                                    {
                                        this.notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"start dozing"});
                                        bolCanDoze = true;
                                        ((IDozeable) current).pickUp();
                                    } else {
                                        this.notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"could doze, but is full"});
                                        return true;
                                    }
                                } else {
                                    this.notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"collide with IDozeable, but cannot doze, because there are no DozePoints"});
                                    return true;
                                }
                            } else {
                                bolCanDoze = true;
                            }
                        }
                    }
                    if (bolCanDoze == false) {
                        return true;
                    }
                }
            }
        }
        return false; //is inside the world & no collision
    }

    /**
     * checks if there is an object at the given position (only asuros and phials, not the boarder of the world).
     * @param measurePoints
     * @return if there is an object or not
     */
    public boolean checkDistanceMeasurement(Point[] measurePoints) {
        for (int i = 0; i < measurePoints.length; i++) {
            if (measurePoints[i] != null) {
                for (ICollideable current : myObjects.values()) {
                    if (current.checkCollision(measurePoints, null)) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    public void putDozeable(IDozeable myObject) {
        myObjects.put(myObject.toString(), myObject);
    }

    /**
     * Checks if there is a collision between the actuator and any other object. if the actuator moves out of the world-area this function returns a collision
     * @param clashPoint if this point collides with another object or is outside of the world, this function returns true
     * @param actuator the actuator of the collison-test
     * @return true = collision; false = no collision
     */
    public boolean checkCollision(Point clashPoint, ICollideable actuator) {
        ase.geometrics.Point[] tmpClashPoint = {clashPoint};
        return checkCollision(tmpClashPoint, actuator);
    }

    public void run() {
        while (keepAlive) {
            try {
                java.lang.Thread.currentThread().sleep(mySzenario.timeControl.getWorldThreadSleep());
            } catch (InterruptedException ex) {
                Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
            }

            intCurrentTime++;


            if (intCurrentTime == mySzenario.timeControl.getSzenarioTicksTillEnd()) {
                destroyWorld();
            }
        }

    }

    public void destroyWorld() {
        //Records und EventDrivenOutputs anhalten
        for (IEventdrivenOutput current : myEventOutputs.values()) {
            if (checkImplementation(current.getClass(), "IThreadControl")) {
                ((IThreadControl) current).kill();
            }
            current = null;
        }

        //TimeDrivenOutputs anhalten
        for (ITimedrivenOutput current : myTimeOutputs.values()) {
            if (checkImplementation(current.getClass(), "IThreadControl")) {
                ((IThreadControl) current).kill();
            }
            current = null;
        }


        //Alle Asuros anhalten
        for (ICollideable current : myObjects.values()) {
            if (checkImplementation(current.getClass(), "IThreadControl")) {
                ((IThreadControl) current).kill();
            }
            current = null;
        }

        System.out.println("Simulation beendet");
        try {
            //todo stop all threads
            keepAlive = false;
            this.finalize();


        } catch (Throwable ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean keepAlive() {
        return keepAlive;
    }

    /**
     * Draws the border on a Graphics-Object
     * @param g the craphicsobject
     * @param zoomLevel
     * @param offsetX
     * @param offsetY
     */
    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        g.drawLine((int) (offsetX * zoomLevel), (int) (offsetY * zoomLevel), (int) ((offsetX) * zoomLevel), (int) ((offsetY + myArea.y) * zoomLevel));
        g.drawLine((int) (offsetX * zoomLevel), (int) (offsetY * zoomLevel), (int) ((offsetX + myArea.x) * zoomLevel), (int) ((offsetY) * zoomLevel));
        g.drawLine((int) (offsetX * zoomLevel), (int) ((offsetY + myArea.y) * zoomLevel), (int) ((offsetX + myArea.x) * zoomLevel), (int) ((offsetY + myArea.y) * zoomLevel));
        g.drawLine((int) ((offsetX + myArea.y) * zoomLevel), (int) (offsetY * zoomLevel), (int) ((offsetX + myArea.x) * zoomLevel), (int) ((offsetY + myArea.y) * zoomLevel));
    }

    public void registerOutput(IEventdrivenOutput newOutput) {
        //Prüfen ob der Monitor bereits registriert wurde
        boolean bolAlreadyReg = false;
        if (newOutput != null) {
            if (myEventOutputs.contains(newOutput)) {
                bolAlreadyReg = true;
                notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"OutputAlreadyExists", newOutput});
                newOutput.notification(IEventdrivenOutput.messageType.ERROR, new Object[]{"thisOutputAlreadyExists"}, this);
            }
        }
        if (!bolAlreadyReg) {
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"registerOutput", newOutput});
            newOutput.notification(IEventdrivenOutput.messageType.DEBUG, new Object[]{"registerThisOutput"}, this);
            myEventOutputs.put(newOutput.toString(), newOutput);
        }
    }

    public void removeOutput(IEventdrivenOutput myOutput) {
        if (myEventOutputs.contains(myOutput)) {
            myOutput.notification(IEventdrivenOutput.messageType.DEBUG, new Object[]{"removeThisOutput"}, this);
            myEventOutputs.remove(myOutput.toString());
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[]{"removeOutput", myOutput});
        } else {
            notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[]{"removeOutputFailed", myOutput});
        }
    }

    private void notifyMonitors(IEventdrivenOutput.messageType mType, Object[] parameters) {
        for (IEventdrivenOutput myOutput : myEventOutputs.values()) {
            myOutput.notification(mType, parameters, this);
        }
    }

    public String getInstanceName() {
        return "world";
    }

    /**
     * Gibt die Wartezeit für den Asuroinstanz zurück
     * @return Threadwartezeit in ms
     */
    public int getSleepTimeForAsurocode() {
        return mySzenario.timeControl.getAsurocodeThreadSleep();
    }

    /**
     * Gibt die Wartezeit für Objektbasen (und deren abgeleitete Klassen) zurück
     * @return Threadwartezeit in ms
     */
    public int getSleepTimeForAsurohardware() {
        return mySzenario.timeControl.getObjektbasisThreadSleep();
    }

    public ase.datalayers.SzenarioParameters getSzenarioParams() {
        return mySzenario.szenarioParams;
    }

    public String getAllParamsAsString(String strZeilenumbruch) {
        return mySzenario.getString(strZeilenumbruch);
    }

    /**
     * Konvertiert eine Größe in eine andere Einheit
     * @param in die Eingangsgröße
     * @param unitIN die Einheit der Einangsgröße
     * @param unitOUT die gewünschte Ausgabeeinheit
     * @return veränderte Größe
     */
    public double convertMetric(double in, unitOfLenght unitIN, unitOfLenght unitOUT) {
        if (unitIN.equals(unitOfLenght.mm)) {
            in = in * mySzenario.szenarioParams.getMM_to_UnitOfLenght();
        } else if (unitIN.equals(unitOfLenght.cm)) {
            in = in * mySzenario.szenarioParams.getMM_to_UnitOfLenght() * 10;
        } else if (unitIN.equals(unitOfLenght.m)) {
            in = in * mySzenario.szenarioParams.getMM_to_UnitOfLenght() * 1000;
        }

        if (unitOUT.equals(unitOfLenght.LE)) {
            return in;
        } else if (unitOUT.equals(unitOfLenght.mm)) {
            return in / mySzenario.szenarioParams.getMM_to_UnitOfLenght();
        } else if (unitOUT.equals(unitOfLenght.cm)) {
            return in / mySzenario.szenarioParams.getMM_to_UnitOfLenght() / 10;
        } else //meter
        {
            return in / mySzenario.szenarioParams.getMM_to_UnitOfLenght() / 1000;
        }
    }
    //<editor-fold desc="Cheating">

    /**
     * Versendet an alle Objekte, die das Interface <i>IKommunikativ</i> implementieren die Nachricht
     * @param Nachricht die Nachricht die versendet werden soll.
     */
    public void versendeNachricht(ase.hardware.AsuroNachricht nachricht) {
        for (ICollideable current : myObjects.values()) {
            if (checkImplementation(current.getClass(), "IKommunikativ")) {
                ((ase.hardware.IKommunikativ) current).empfangeNachricht(nachricht);
            }
        }
    }
    //</editor-fold>
}


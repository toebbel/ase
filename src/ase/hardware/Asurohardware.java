package ase.hardware;

import ase.geometrics.*;
import ase.outputs.*;
import ase.World;
import java.util.Hashtable;
import java.util.ArrayList;
import java.awt.Graphics;
import ase.datalayers.SzenarioParameters.unitOfLenght;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asurohardware is a kind of interface between the asurosourcecode (<code>Asuroinstance</code>) and simulation enviroment (<code>world</code> and the other <code>Asuroinstance</code>s)
 * @author Tobias Sturm
 */
public class Asurohardware extends Area implements IDrawable,ase.IThreadControl, Runnable, ICollideable, ase.outputs.IOutputEventCreator,ase.outputs.IPositionRecordable, ase.hardware.IKommunikativ{
    //Speed of the left Motor
    private int motorLeft = 0;
    
    //Speed of the right Motor
    private int motorRight = 0;

    public double odometrie[] = new double[2];
    private boolean bolOdometrie = false;
    
    //Positions of the Sensors, relative;
    private static double SENSOR1 = 0.0/5.0;
    private static double SENSOR2 = 1.0/5.0;
    private static double SENSOR3 = 2.0/5.0;
    private static double SENSOR4 = 3.0/5.0;
    private static double SENSOR5 = 4.0/5.0;
    private static double SENSOR6 = 5.0/5.0;

    private double minAbstandsMessentfernung = 0; //in mm: Ab welchem Abstand werden Kollisionspunkte berechent um den Abstandssensor zu simulieren
    private double maxAbstandsMessentfernung = 1; //in mm: Bis zu welchem Abstand werden Kollisionspunkte berechent um den Abstandssensor zu simulieren
    private double AbstandsMessentfernungsStep = 1; //in mm: wie groß sind die Schritte von einem Kollisionspunkt bis zum nächsten (von der Abstandssensorkollisionsstrecke)
    private double AbstandsMesswinkel = 0.1; // der Streuwinkel des Abstandsensors

    //Referenz to the instance of World
    private World refWorld;

    //Strecke, die der asuro am Stück rückwärts fährt
    double backWardsDist = 0;

   private byte statusLed;
   private byte backLedLeft;
   private byte backLedRight;

   private ase.hardware.IDozeable myDozePhial;
   ase.Asuroinstance mySoftware;

   private String instanceName;

    transient private Thread thisThread;

   private Hashtable<String,IEventdrivenOutput> myEventOutputs;

   //Cheating
   private ArrayList<AsuroNachricht> kommunikationsInputBuffer;
   private long intSystemTimer; //Die Systemzeit beim Start der Hardware
   private String strAsuroID;
   //Cheating ende

    /**
     * Creates an Instanz of World
     * @param name the Name of the Asurohardware
     * @param position the Position of the Asurohardware
     * @param refWorld a referenz to the instance of <code>World</code>
     */
    public Asurohardware(String name, Area position, World refWorld) {
        super(position);
        this.refWorld = refWorld;
        myEventOutputs  = new Hashtable<String,IEventdrivenOutput>();
        maxAbstandsMessentfernung = refWorld.getSzenarioParams().getDistanceMeasureMaxDistance(unitOfLenght.mm);
        minAbstandsMessentfernung = refWorld.getSzenarioParams().getDistanceMeasureMinDistance(unitOfLenght.mm);
        AbstandsMessentfernungsStep = refWorld.getSzenarioParams().getDistanceMeasureDistanceStep(unitOfLenght.mm);
        myDozePhial = null;
        instanceName = name;

        //Cheating
        kommunikationsInputBuffer = new ArrayList<AsuroNachricht>();
        intSystemTimer = Integer.MIN_VALUE;
        //Cheating ende

        thisThread = new Thread(this);
        thisThread.setName("Thread of asurohardware " + name);
        thisThread.start();

        //Send reference to a new instance of Asuroinstance
        mySoftware = new ase.Asuroinstance(name, this,refWorld);
    }


    /**
     * The Thread wich calculates every n ms the movement of the Asuro
     */
    public void run() {
        notifyMonitors(IEventdrivenOutput.messageType.STATECHANGE, new Object[] {"starting asurohardware"});
        intSystemTimer = System.currentTimeMillis();
        int timer = 0;
        while(thisThread.isAlive())
        {
             try {
                java.lang.Thread.currentThread().sleep(refWorld.getSleepTimeForAsurohardware());
            } catch (InterruptedException ex) {
                notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[] {"error while sleeping",ex});
            }
            calcMotorMove();
            timer += refWorld.getSleepTimeForAsurohardware();
            if(timer >= 2000 && strAsuroID != null)
            {
                sendeNachricht(AsuroNachricht.AsuroNachrichtBefehl.EigeneAktuellePosition, anchor);
                notifyMonitors(IEventdrivenOutput.messageType.OTHERS, new Object[] {"asuromind ownposition",mySoftware.getInstanceName(), anchor.x,anchor.y});
                timer-=2000;
            }
        }
    }

    private double transformDistance2Odometrie(double dist)
    {
        return dist / 20.0;
    }

    
    private void calcMotorMove()
    {
        if(motorRight != 0 || motorLeft != 0)
        {

        odometrie[0] += Math.abs(transformDistance2Odometrie(transformSpeedToDistance(motorLeft)));
        odometrie[1] += Math.abs(transformDistance2Odometrie(transformSpeedToDistance(motorRight)));

        Position myMove = new Position();
        if(motorLeft == motorRight)
        {
            myMove.x = transform2Vector(transformSpeedToDistance(motorLeft),anchor.angle).x;
            myMove.y = transform2Vector(transformSpeedToDistance(motorLeft),anchor.angle).y;
        }
        else
        {

            Point motorRightMove = new Point();
            Point motorLeftMove = new Point();
            Point newWidthVectorial = new Point();

            //movement of the anchorpoint
            myMove.x = transform2Vector(transformSpeedToDistance(motorLeft),anchor.angle).x;
            myMove.y = transform2Vector(transformSpeedToDistance(motorLeft),anchor.angle).y;

            //position of the left motor is equal to the anchor
            motorLeftMove.x = myMove.x;
            motorLeftMove.y =  myMove.y;

            //movement of the right motor
            motorRightMove.x = transform2Vector(transformSpeedToDistance(motorRight),anchor.angle).x;
            motorRightMove.y = transform2Vector(transformSpeedToDistance(motorRight),anchor.angle).y;

            //calc new widthvector after movement
            newWidthVectorial.x = widthVectorial.x + motorRightMove.x - motorLeftMove.x;
            newWidthVectorial.y = widthVectorial.y + motorRightMove.y - motorLeftMove.y;

            //angle between old widthVector and new one
            myMove.angle = Math.pow(Math.cos(Math.toRadians((newWidthVectorial.x * widthVectorial.x + newWidthVectorial.y * widthVectorial.y) / (newWidthVectorial.getNorm() * widthVectorial.getNorm()))), -1);

            //correct the "turn" of the angle
            if(motorLeft < 0 && motorRight < 0)
            {
                if(Math.abs(motorLeft) > Math.abs(motorRight))
                    myMove.angle = myMove.angle * -1;
            }
            else if (motorLeft > 0 && motorRight > 0)
            {
                if(Math.abs(motorLeft) < Math.abs(motorRight))
                    myMove.angle = myMove.angle * -1;
            }
            else
            {
                if((motorLeft) < (motorRight))
                    myMove.angle = myMove.angle * -1;
            }
        }

        //Kollisionskontrolle
        int i =0;
        if(myDozePhial != null)
            myDozePhial.doze(new Point(anchor.x  + heightVectorial.x + 0.5 * widthVectorial.x + transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle + myMove.angle).x + myMove.x,anchor.y + heightVectorial.y + 0.5 * widthVectorial.y+ transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle + myMove.angle).y + myMove.y));
        while (refWorld.checkCollision(this.getClashPoints((int)(myMove.x),(int)(myMove.y),myMove.angle), this) == true && i < 9)
        {
            i++;
            myMove.x = myMove.x / 2.0;
            myMove.y = myMove.y / 2.0;
            myMove.angle = myMove.angle / 2.0;
            if(myDozePhial != null)
                myDozePhial.doze(new Point(anchor.x  + heightVectorial.x + 0.5 * widthVectorial.x + transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle + myMove.angle).x + myMove.x,anchor.y + heightVectorial.y + 0.5 * widthVectorial.y+ transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle + myMove.angle).y + myMove.y));
        }

        //Bewegung durchführen, wenn keine Kollision besteht
        if(refWorld.checkCollision(getClashPoints((int)(myMove.x),(int)(myMove.y),myMove.angle), this) == false)
        {
            anchor = anchor.addPosition(myMove);
            notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[] {"move",myMove});
            if(motorRight < 0 && this.motorLeft < 0)
                backWardsDist += myMove.getNorm();
            else
                backWardsDist = 0;
            if(myDozePhial != null)
                if(backWardsDist > refWorld.getSzenarioParams().getIntDeltaDistanceDropPhial(unitOfLenght.LE))
                {
                    notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[] {"drop phial",myDozePhial});
                    myDozePhial.drop();
                    myDozePhial = null;
                }
                else //do not drop phial, doze it!
                {
                    updateTempVectors();
                    myDozePhial.doze(new Point(anchor.x + heightVectorial.x + 0.5 * widthVectorial.x + transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle).x,anchor.y + heightVectorial.y + 0.5 * widthVectorial.y + transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle).y));
                }
        }
        else
        {
            if(myDozePhial != null)
                myDozePhial.doze(new Point(anchor.x + heightVectorial.x + 0.5 * widthVectorial.x + transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle).x,anchor.y + heightVectorial.y + 0.5 * widthVectorial.y+ + transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle).y));
            this.notifyMonitors(IEventdrivenOutput.messageType.ACTION, new Object[] {"could not execute move",myMove});
        }
           
        }

    }

    /**
     * Converts the given Speed to a Distance
     * @param speed
     * @return
     */
    private double transformSpeedToDistance(int speed)
    {
        return speed / 5.0;
    }

    /**
     * Calculates a vector, based on a lenght (norm) and an angle
     * @param norm the lenght of the vector
     * @param angle the angle, relative to the horizon
     * @return a vector as a <code>Point</code>
     */
    private Point transform2Vector(double norm,double angle)
    {
        return new Point(norm * Math.cos(Math.toRadians(angle - 90)),norm * Math.sin(Math.toRadians(angle - 90)));
    }

    public byte PollSwitch() {
        byte value = 0;
        if (refWorld.checkCollision(getSensorPosition(SENSOR1), this))
            value += 32;

        if (refWorld.checkCollision(getSensorPosition(SENSOR2), this))
            value += 16;
        
        if(this.myDozePhial == null)
        {
            if (refWorld.checkCollision(getSensorPosition(SENSOR3), this))
                value += 8;
            if (refWorld.checkCollision(getSensorPosition(SENSOR4), this))
                value += 4;
        }
        else //dozing a phial
        {
            if(refWorld.checkCollision(myDozePhial.getClashPoints(),this))
                value += 12;
        }

        if (refWorld.checkCollision(getSensorPosition(SENSOR5), this))
            value += 2;
        if (refWorld.checkCollision(getSensorPosition(SENSOR6), this))
            value += 1;
        return value;
    }

    public Point getSensorPosition(double Sensorrelation) {
        updateTempVectors();
        return new Point(anchor.x + (Sensorrelation * widthVectorial.x) + heightVectorial.x * 1.15,anchor.y + (Sensorrelation * widthVectorial.y) + heightVectorial.y * 1.15);
    }

    public void setMotorRight(int motorRight) {
        this.motorRight = motorRight;
    }

    public void setMotorLeft(int motorLeft) {
        this.motorLeft = motorLeft;
    }

    @Override
    public boolean checkCollision(Point[] points, ICollideable actuator) {
    if(myDozePhial != null)
        {
            if(myDozePhial.checkCollision(points, actuator))
                return true;
            return super.checkCollision(points, actuator);
        }
    else
        return super.checkCollision(points, actuator);
    }



    @Override
    public void drawObjects(Graphics g, double zoomLevel, int offsetX, int offsetY) {
        super.drawObjects(g, zoomLevel, offsetX, offsetY);
        g.drawString(instanceName, (int)((offsetX + anchor.x)*zoomLevel), (int)((offsetY + anchor.y)*zoomLevel));
        g.setColor(java.awt.Color.red);
        g.drawOval((int)((getSensorPosition(SENSOR1).x + offsetX) * zoomLevel), (int)((getSensorPosition(SENSOR1).y + offsetY)* zoomLevel ), 3, 1);
        g.drawOval((int)((getSensorPosition(SENSOR2).x + offsetX) * zoomLevel), (int)((getSensorPosition(SENSOR2).y + offsetY)* zoomLevel ), 3, 1);
        g.drawOval((int)((getSensorPosition(SENSOR3).x + offsetX) * zoomLevel), (int)((getSensorPosition(SENSOR3).y + offsetY)* zoomLevel ), 3, 1);
        g.drawOval((int)((getSensorPosition(SENSOR4).x + offsetX) * zoomLevel), (int)((getSensorPosition(SENSOR4).y + offsetY)* zoomLevel ), 3, 1);
        g.drawOval((int)((getSensorPosition(SENSOR5).x + offsetX) * zoomLevel), (int)((getSensorPosition(SENSOR5).y + offsetY)* zoomLevel ), 3, 1);
        g.drawOval((int)((getSensorPosition(SENSOR6).x + offsetX) * zoomLevel), (int)((getSensorPosition(SENSOR6).y + offsetY)* zoomLevel ), 3, 1);
        g.setColor(java.awt.Color.green);

            Point motorRightMove = new Point();
            Point motorLeftMove = new Point();
            Point newWidthVectorial = new Point();

            motorLeftMove.x = transform2Vector(transformSpeedToDistance(motorLeft),anchor.angle).x * 10;
            motorLeftMove.y = transform2Vector(transformSpeedToDistance(motorLeft),anchor.angle).y * 10;

            motorRightMove.x = transform2Vector(transformSpeedToDistance(motorRight),anchor.angle).x * 10;
            motorRightMove.y = transform2Vector(transformSpeedToDistance(motorRight),anchor.angle).y * 10;

            newWidthVectorial.x = widthVectorial.x + motorRightMove.x - motorLeftMove.x;
            newWidthVectorial.y = widthVectorial.y + motorRightMove.y - motorLeftMove.y;

            g.drawLine((int)((motorLeftMove.x + offsetX + anchor.x) * zoomLevel), (int)((motorLeftMove.y + offsetY + anchor.y) * zoomLevel),(int)((anchor.x + offsetX)*zoomLevel),(int)((anchor.y + offsetY)*zoomLevel));
            g.drawLine((int)((motorRightMove.x + offsetX + anchor.x + widthVectorial.x) * zoomLevel), (int)((motorRightMove.y + offsetY + anchor.y + widthVectorial.y) * zoomLevel),(int)((anchor.x + offsetX + widthVectorial.x)*zoomLevel),(int)((anchor.y + offsetY + widthVectorial.y)*zoomLevel));
            g.drawLine((int)((newWidthVectorial.x + offsetX + anchor.x + motorRightMove.x) * zoomLevel), (int)((newWidthVectorial.y + offsetY + anchor.y + motorRightMove.y) * zoomLevel),(int)((anchor.x + offsetX + motorLeftMove.x)*zoomLevel),(int)((anchor.y + offsetY + motorLeftMove.y)*zoomLevel));



            g.setColor(java.awt.Color.blue);
            Point[] tmpArray;

            for(double i = this.minAbstandsMessentfernung; i < this.maxAbstandsMessentfernung; i += this.AbstandsMessentfernungsStep)
            {
                tmpArray = getDistanceMeasurementClashpoints(i);
                for(int j = 0; j < tmpArray.length; j++)
                    g.drawOval((int)(zoomLevel * (tmpArray[j].x + offsetX)), (int)(zoomLevel * (tmpArray[j].y + offsetY)),1,1);
            }

            g.setColor(java.awt.Color.black);
    }

 



    @Override
    public Point[] getClashPoints() {
        return this.getClashPoints(0,0,0);
    }

    @Override
    public Point[] getClashPoints(int offsetX, int offsetY, double offsetAnlge) {
        super.updateClashPoints(offsetX,offsetY,offsetAnlge);
        if(myDozePhial != null)
        {
            //Point[] dozClashPoints = myDozePhial.getClashPoints((int)(offsetX),(int)(offsetY),offsetAnlge);
            Point[] dozClashPoints = myDozePhial.getClashPoints(offsetX,offsetY,offsetAnlge);

            Point[] tmpClashPoints = new Point[clashPoints.length + dozClashPoints.length];
            for(int i = 0; i < clashPoints.length;i++)
                tmpClashPoints[i] = clashPoints[i];

            for(int i = 0; i < dozClashPoints.length;i++)
                tmpClashPoints[i + clashPoints.length] = dozClashPoints[i];

            return tmpClashPoints;
        }
        return clashPoints;
    }






    /**
     * @param statusLed the statusLed to set
     */
    public void setStatusLed(byte statusLed) {
        this.statusLed = statusLed;
    }

    /**
     * @param backLedLeft the backLedLeft to set
     */
    public void setBackLedLeft(byte backLedLeft) {
        this.backLedLeft = backLedLeft;
    }

    /**
     * @param backLedRight the backLedRight to set
     */
    public void setBackLedRight(byte backLedRight) {
        this.backLedRight = backLedRight;
    }

    /**
     * Initialisierungsfunktion der library. Wenn Cheating an ist, wird hier auch eine FunkID festgelegt
     */
    public void init()
    {
       strAsuroID = java.util.UUID.randomUUID().toString();
    }

        /**
     * Den Interrupt Betrieb der Odometriesensoren-Messung initialisieren und starten.
     */
    public void EncoderInit()
    {
        odometrie[0] = 0;
        odometrie[1] = 0;
        bolOdometrie = true;
    }

    public void EncoderStop()
    {
        bolOdometrie = false;
    }


public void registerOutput(IEventdrivenOutput newOutput)
    {
    mySoftware.registerOutput(newOutput);
        //Prüfen ob der Monitor bereits registriert wurde
        boolean bolAlreadyReg = false;
            if(newOutput != null)
                if(myEventOutputs.contains(newOutput))
                {
                    bolAlreadyReg = true;
                    notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[] {"OutputAlreadyExists",newOutput});
                    newOutput.notification(IEventdrivenOutput.messageType.ERROR, new Object[] {"thisOutputAlreadyExists"},this);
                }
        if(!bolAlreadyReg)
        {
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[] {"registerOutput",newOutput});
            newOutput.notification(IEventdrivenOutput.messageType.DEBUG, new Object[] {"registerThisOutput"},this);
            myEventOutputs.put(newOutput.toString(), newOutput);
        }
    }

    public void removeOutput(IEventdrivenOutput myOutput)
    {
        mySoftware.removeOutput(myOutput);
        if(myEventOutputs.contains(myOutput))
        {
            myOutput.notification(IEventdrivenOutput.messageType.DEBUG, new Object[] {"removeThisOutput"},this);
            myEventOutputs.remove(myOutput.toString());
            notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[] {"removeOutput",myOutput});
        }
            else
                notifyMonitors(IEventdrivenOutput.messageType.ERROR, new Object[] {"removeOutputFailed", myOutput});
    }

    synchronized private void notifyMonitors(IEventdrivenOutput.messageType mType,Object[] parameters)
    {
        for(IEventdrivenOutput myOutput: myEventOutputs.values())
            myOutput.notification(mType, parameters, this);
    }

    /**
     *
     * @param newIDozeable returns if the asuro can doue sth. or not
     */
    @Override
    public boolean doze(IDozeable newIDozeable) {
        if(this.myDozePhial != null)
            return false;
        else
        {
            if(!refWorld.checkCollisionForStartDoze(newIDozeable.getClashPointsAbsolute((int)(anchor.x + heightVectorial.x + 0.5 * widthVectorial.x + anchor.getUnitVector().x + transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle).x),(int)(anchor.y + heightVectorial.y + 0.5 * widthVectorial.y+ + transform2Vector(refWorld.getSzenarioParams().getSizeOfPhials(),anchor.angle).y), 0), new ICollideable[] {this,newIDozeable}))
            {
                myDozePhial = newIDozeable;
                notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[] {"pickup phial",myDozePhial});
                return true;
            }
            else
                return false;
        }
    }


     public double getDistanceMeasurement()
    {
        for(double i = minAbstandsMessentfernung; i < maxAbstandsMessentfernung; i += AbstandsMessentfernungsStep)
        {
            if(refWorld.checkDistanceMeasurement(getDistanceMeasurementClashpoints(i))){
                return i;
            }
        }
        return 999;
    }

    private Point[] getDistanceMeasurementClashpoints(double intMm)
    {
        //Einheitsvektor des Längenvektors -> mit 2LE(mm) multiplizieren -> Punkt
        
        //TODO Streuungswinkel in Abstandsmessung einbauen
        updateTempVectors();
        return new Point[] {new Point(widthVectorial.x * 0.5 + anchor.x + heightVectorial.x + refWorld.convertMetric(intMm, unitOfLenght.mm, unitOfLenght.LE) * heightVectorial.getUnitVector().x, heightVectorial.y + anchor.y + refWorld.convertMetric(intMm, unitOfLenght.mm, unitOfLenght.LE) * heightVectorial.getUnitVector().y + widthVectorial.y * 0.5)};
    }

    @Override
    public Point[] getDozePoints() {
        return new Point[] {this.getSensorPosition(SENSOR1),this.getSensorPosition(SENSOR2),this.getSensorPosition(SENSOR3),this.getSensorPosition(SENSOR4),this.getSensorPosition(SENSOR5),this.getSensorPosition(SENSOR6)};
    }

    @Override
    public String getInstanceName() {
        return instanceName;
    }



    public void kill()
    {
        thisThread.interrupt();
        thisThread = null;
        myEventOutputs.clear();
        mySoftware.kill();
        try {
            this.finalize();
        } catch (Throwable ex) {
            Logger.getLogger(Asurohardware.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // <editor-fold desc="Cheating">
    /**
     * Empfängt eine Nachricht und speichert sie im buffer ab. Dort kann sie von der software verarbeitet werden. Nachrichten die von dieser Hardware gesendet wurden werden ignoriet
     * @param nachricht die Nachricht die empfangen wird
     */
    public void empfangeNachricht(AsuroNachricht nachricht) {
        if(!nachricht.getAsuroID().equals(strAsuroID)) //eigene Nachrichten verwerfen
            kommunikationsInputBuffer.add(nachricht);
    }

    /**
     * gibt die Breite des Asuros als Vektor zurück
     * @return Breite des Asuros in LE
     */
    public Point getWidthVector()
    {
        updateTempVectors();
        return widthVectorial;
    }

    /**
     * Gibt die Höhe des Asuros als Vektor zurück
     * @return Höhe des Vektors in LE
     */
    public Point getHeightVector()
    {
        updateTempVectors();
        return heightVectorial;
    }

    /**
     * gibt die Breite des Asuros als Vektor zurück
     * @return Breite des Asuros in mm
     */
    public Point getWidthVectorAsMM()
    {
        updateTempVectors();
        return new Point(refWorld.convertMetric(widthVectorial.x, unitOfLenght.LE, unitOfLenght.mm),refWorld.convertMetric(widthVectorial.y, unitOfLenght.LE, unitOfLenght.mm));
    }

    /**
     * Gibt die Höhe des Asuros als Vektor zurück
     * @return Höhe des Vektors in mm
     */
    public Point getHeightVectorAsMM()
    {
        updateTempVectors();
        return new Point(refWorld.convertMetric(heightVectorial.x, unitOfLenght.LE, unitOfLenght.mm),refWorld.convertMetric(heightVectorial.y, unitOfLenght.LE, unitOfLenght.mm));
    }

    /**
     * Liest alle emfpangen Nachrichten aus dem Buffer und löscht diese ggf.
     * @param delete gibt an ob der Buffer nach dem Lesen gelöscht werden soll
     * @return der Nachrichtenbuffer. Wenn keine Nachrichten vorhanden sind, dann <code>null</code>
     */
    public AsuroNachricht[] getKommunikationsBuffer(boolean delete)
    {
       if(kommunikationsInputBuffer.size() == 0)
       {
                   notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[] {"Kommunikationsbufferabfrage",0});
           return null;
       }
       else
       {
            AsuroNachricht[] tmpArr = new AsuroNachricht[kommunikationsInputBuffer.size()];
            for(int i = 0 ; i < tmpArr.length;i++)
                tmpArr[i] = kommunikationsInputBuffer.get(i);

                   notifyMonitors(IEventdrivenOutput.messageType.DEBUG, new Object[] {"Kommunikationsbufferabfrage",kommunikationsInputBuffer.size(),delete});
            if(delete)
                kommunikationsInputBuffer.clear();
            return tmpArr;
       }
    }

    /**
     * Gibt an, wieviele Nachrichten sich im Buffer befinden
     * @return die Anzahl der Nachrichten im Buffer
     */
    public int getKommunikationsBufferSize()
    {
        return kommunikationsInputBuffer.size();
    }

    /**
     * Gibt die Position der Hardware zurück, an der sich der Asuro gerade befindet. Angaben sind in cm.
     * @return
     */
    public Position getEigenePosition()
    {
        while(anchor.angle > 360)
            anchor.angle -= 360;
        while(anchor.angle < 0)
            anchor.angle += 360;
        return new Position(refWorld.convertMetric(anchor.x, unitOfLenght.LE, unitOfLenght.mm),refWorld.convertMetric(anchor.y, unitOfLenght.LE, unitOfLenght.mm),anchor.angle);
    }

     /**
     * Gibt die Position der Hardware zurück, an der sich der Asuro gerade befindet. Angaben sind in LE.
     * @return
     */
    public Position getEigenePositionAlsLE()
    {
        return anchor.clone();
    }

    /**
     * Gibt die Systemzeit in ms seit Start der Asurohardware zurück
     * @return Wert zwischen 0 und (2^62)-1, entspricht (Millisekunden)
     */
    public int getSystemZeit()
    {
        return (int)(System.currentTimeMillis() - intSystemTimer);
    }

    /**
     * Versendet eine Nachricht an alle anderen Asuros via refWorld
     * @param befehl der Befehl der Nachricht
     * @param parameter der Parameter der Nachricht
     * @see AsuroNachricht
     */
    public void sendeNachricht(AsuroNachricht.AsuroNachrichtBefehl befehl, Object parameter)
    {
        refWorld.versendeNachricht(new AsuroNachricht(strAsuroID,befehl,parameter));
        notifyMonitors(IEventdrivenOutput.messageType.INTERACTION, new Object[] {"sending Message",new AsuroNachricht(strAsuroID,befehl,parameter).toString()});
    }

    //</editor-fold>
}

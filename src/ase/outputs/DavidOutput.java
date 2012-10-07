package ase.outputs;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

/**
 *
 * @author Tobi
 */
public class DavidOutput implements IEventdrivenOutput{

    private String name;

        public String getInstanceName() {
        return name;
    }

    public DavidOutput(String name) {
        this.name = name;
    }

    synchronized public void notification(messageType mType, Object[] values,IOutputEventCreator acuator) {
        if(mType == messageType.ASUROSOFTWAREDEBUG)
        {
            String strMessage = acuator.getInstanceName() + " sendet: ";
            for(int i = 0; i < values.length; i++)
                strMessage += values[i].toString() + " ";
            System.out.println(strMessage);
        }
    }






    public void close() {

    }

    @Override
    public String toString() {
        return "SimpleConsoleOutput " + name;
    }





}

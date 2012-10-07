package ase.outputs;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

/**
 *
 * @author Tobi
 */
public class SimpleConsoleDebugOutput implements IEventdrivenOutput{

    private String name;

        public String getInstanceName() {
        return name;
    }

    public SimpleConsoleDebugOutput(String name) {
        this.name = name;
    }

    synchronized public void notification(messageType mType, Object[] values,IOutputEventCreator acuator) {
        if(mType == messageType.DEBUG || mType == messageType.ERROR)
        {
            String strMessage = acuator.getClass().getSimpleName() + "(" + acuator.getInstanceName() + ") sends " + mType.toString() + " ";
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

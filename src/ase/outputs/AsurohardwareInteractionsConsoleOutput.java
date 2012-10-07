package ase.outputs;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Tobi
 */
public class AsurohardwareInteractionsConsoleOutput implements IEventdrivenOutput{

    private String name;

        public String getInstanceName() {
        return name;
    }

    public AsurohardwareInteractionsConsoleOutput(String name) {
        this.name = name;
    }

    public AsurohardwareInteractionsConsoleOutput() {
    }

    

    synchronized public void notification(messageType mType, Object[] values,IOutputEventCreator acuator) {
        if(acuator.getClass().getSimpleName().equals("Asurohardware"))
            if(mType == messageType.INTERACTION)
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
        return "AsuroinstanceConsoleOutput " + name;
    }





}

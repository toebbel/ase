package ase.outputs;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Tobi
 */
public class AsuroinstanceCommentsConsoleOutput implements IEventdrivenOutput{

    private String name;

        public String getInstanceName() {
        return name;
    }

    public AsuroinstanceCommentsConsoleOutput(String name) {
        this.name = name;
    }

    synchronized public void notification(messageType mType, Object[] values,IOutputEventCreator acuator) {
        if(acuator.getClass().getSimpleName().equals("Asuroinstance") && mType == messageType.OTHERS)
        {
            if(values.length > 1)
                if(values[0].equals("asurocode comment"))
                {
                    String strMessage =  acuator.getInstanceName() + "  ";
                    for(int i = 0; i < values.length; i++)
                        strMessage += values[i].toString() + " ";
                    System.out.println(strMessage);
                }
        }
    }



    public void close() {

    }

    @Override
    public String toString() {
        return "AsuroinstanceConsoleOutput " + name;
    }





}

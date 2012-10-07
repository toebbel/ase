/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.outputs;

import ase.outputs.IEventdrivenOutput.messageType;

/**
 *
 * @author Tobi
 */
public class AsuroinstanceCommentsSimpleFileOutput extends SimpleFileOutput{

    public AsuroinstanceCommentsSimpleFileOutput(String name) {
        super(name);
    }

    @Override
    public void notification(messageType mType, Object[] values, IOutputEventCreator acuator) {
    if(acuator.getClass().getSimpleName().equals("Asuroinstance"))
        if(mType == messageType.OTHERS)
                if(values[0] == "asurocode comment")
                {
                    String strMessage =  acuator.getInstanceName() + " comments ";
                    for(int i = 1; i < values.length; i++)
                        strMessage += values[i].toString() + " ";
                    super.writeInFile(strMessage + "\n"); //output in Datei speichern
                }
    }
    



}

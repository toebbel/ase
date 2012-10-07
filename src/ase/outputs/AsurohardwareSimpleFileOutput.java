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
public class AsurohardwareSimpleFileOutput extends SimpleFileOutput{

    public AsurohardwareSimpleFileOutput(String name) {
        super(name);
    }

    @Override
    public void notification(messageType mType, Object[] values, IOutputEventCreator acuator) {
    if(acuator.getClass().getSimpleName().equals("Asurohardware"))
        super.notification(mType, values, acuator);
    }
    



}

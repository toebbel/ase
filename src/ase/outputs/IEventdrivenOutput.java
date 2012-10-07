package ase.outputs;


/**
 *
 * @author Tobias
 */
public interface IEventdrivenOutput extends ase.INameable{

    public void close();

    public void notification(messageType mType, Object[] values,IOutputEventCreator acuator);
    
    
    public static enum messageType{
        DEBUG,
        OTHERS,
        STATECHANGE,
        DESCISIONANDANSWER,
        ACTION,
        ERROR,
        INTERACTION,
        ASUROSOFTWAREDEBUG;
    }

}

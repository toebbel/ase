package ase.outputs;


public interface IOutputEventCreator extends ase.INameable{
    void removeOutput(IEventdrivenOutput myOutput);
    void registerOutput(IEventdrivenOutput myOutput);
}
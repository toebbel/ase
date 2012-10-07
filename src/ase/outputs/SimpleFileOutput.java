package ase.outputs;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

/**
 *
 * @author Tobi
 */
public class SimpleFileOutput implements IEventdrivenOutput, ase.IThreadControl {
    private String name;
    private PrintWriter out;
    private String strFilename;

    public String getInstanceName() {
        return name;
    }

    public SimpleFileOutput(String name) {
        this.name = name;
    }

    /**
     * Erstellt eine Datei im Ordner strFolder\SFO [Instanzname] at [datum].txt
     * @param strFolder der Ordner in dem die Datei erzeugt werden sol (muss exisiteren!)
     */
    public void initialize(String strFolder)
    {
        //Dateinamen generieren
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd kkmmss SS");
        strFilename = "SFO " + name + " at " + sdf.format(new Date())+ ".txt";

        //Datei öffnen
        try
        {
            out = new PrintWriter(new BufferedWriter(new FileWriter(new File(strFolder + strFilename))));
            out.println("Öffne Logdatei für SimpleFileMonitor " + name + ". Dateiname " + strFilename);
            System.out.println("Öffnen OK " + name);
        }
        catch(IOException e){
            System.out.println("Feher beim öffnen der Datei " + strFilename + " " + e.getMessage());
        }
    }

    public void notification(messageType mType, Object[] values, IOutputEventCreator acuator) {
        
            String strMessage = acuator.getClass().getSimpleName() + "(" + acuator.getInstanceName() + ") sends " + mType.toString() + " ";
            for(int i = 0; i < values.length; i++)
                strMessage += values[i].toString() + " ";
            writeInFile(strMessage);
    }

    /**
     * erstellt KEINE Zeilenumbrüche!!!
     * @param strMessage was in die Textdatei geschrieben werden soll
     */
    protected void writeInFile(String strMessage){
        if(out != null)
        {
            out.println(strMessage);
            out.flush();
        }
    }





    public void close() {
        out.println("Schließe Datei");
        out.flush();
        out.close();
        out = null;
    }



    public void kill() {
        close();
    }



    @Override
    public String toString() {
        return "SimpleFileOutput " + name;
    }

    


}

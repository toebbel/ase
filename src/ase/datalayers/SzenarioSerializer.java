/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ase.datalayers;
import java.io.*;

/**
 * Speichert SzenarioContainer als Datei ab und kann diese auch wieder öffnen
 * @author Tobi
 */
public  class SzenarioSerializer {

    public static void saveSzenario(SzenarioContainer szenario, String strPath) throws IOException
    {
        if(szenario != null)
        {
            File myFile = new File(strPath);
            ObjectOutputStream output = new ObjectOutputStream(new java.io.FileOutputStream(myFile));

            output.writeObject(szenario);
        }
        else
            System.out.println("SzenarioSerializer: failed to save; szenarioContainer was null!");
    }

    public static SzenarioContainer loadSzenario(String strPath) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        File myFile = new File(strPath);
        ObjectInputStream input = new ObjectInputStream(new FileInputStream(myFile));
        return (SzenarioContainer)(input.readObject());
    }
}

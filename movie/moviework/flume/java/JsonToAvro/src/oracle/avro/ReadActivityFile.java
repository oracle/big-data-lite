package oracle.avro;

import java.io.File;

import java.io.IOException;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;

/**
 * Simple class used to read the Activity avro file used by demo
 */
public class ReadActivityFile {
    String filename = "/home/oracle/movie/moviework/mapreduce/movieapp_3months.avro";
    int numrecs = 20;
    
    public ReadActivityFile() {
    }

    /**
     *Reads the avro file
     * @throws IOException
     */
    private void readFile() throws IOException {
        // Deserialize Activities from disk
        
        File file = new File(filename);
                
        DatumReader<Activity> activityDatumReader = new SpecificDatumReader<Activity>(Activity.class);
        DataFileReader<Activity> dataFileReader = new DataFileReader<Activity>(file, activityDatumReader);
    
        Activity activity = null;
        int i = 0;
        
        while (dataFileReader.hasNext() && i < numrecs) {
            i++;
            activity = dataFileReader.next(activity);
            System.out.println(activity);
        }
    }
    
    /**
     *Specify the file name and number of records to read
     * @param file
     * @param num
     */
    private void setParameters(String file, String num) {
        filename = file;
        numrecs  = Integer.parseInt(num);
    }

    public static void main(String[] args) {
        ReadActivityFile readActivityFile = new ReadActivityFile();
        
        if (args.length < 2) {
            System.out.println("Using default file name and number of records to read.");
        }
        else {
            // First argument is the file name
            // Second argument is the number of records to read
            readActivityFile.setParameters(args[0],args[1]);
        }
                   
        try {
            readActivityFile.readFile();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

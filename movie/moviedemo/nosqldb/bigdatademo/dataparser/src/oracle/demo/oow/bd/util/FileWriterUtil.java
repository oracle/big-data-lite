package oracle.demo.oow.bd.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import oracle.demo.oow.bd.constant.Constant;

public class FileWriterUtil {

    private static final String OUTPUT_FILE =
        File.separator + "u01" + File.separator + "Middleware" +
        File.separator + "logs";


    public static void writeOnFile(String line) {
        try {
            File file = new File(OUTPUT_FILE);
            boolean status = file.mkdirs();

            System.out.println(line);
            
            line = line.toLowerCase();
            BufferedWriter out =
                new BufferedWriter(new FileWriter(OUTPUT_FILE +
                                                  File.separator +
                                                  "activity.out", true));
            out.write(line);
            out.newLine();
            out.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    } //writeOnFile

    public static void main(String[] args) {
        FileWriterUtil.writeOnFile("Test");
    }
}

//based on http://hadooptutorial.info/reading-and-writing-sequencefile-example/

import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.Text;

import oracle.ord.hadoop.mapreduce.OrdImageWritable;

public class SequenceFileRead
{		
  public static void main(String[] args) throws IOException {
	String uri = args[0];
        String split = args[1];
	Configuration conf = new Configuration();
	Path path = new Path(uri);
	SequenceFile.Reader reader = null;
	try {		
	reader = new SequenceFile.Reader(conf, Reader.file(path));
        Text key = new Text();
        OrdImageWritable value = new OrdImageWritable();

        int num = 0;

	while (reader.next(key, value)) {
            System.out.println(key.toString() + " " + value.getByteLength());
            ImageIO.write(value.getImage(), "jpg", new File("image" +split+"_" + num++ + ".jpg"));
	}
	} finally {
		IOUtils.closeStream(reader);
		}		
	}
}

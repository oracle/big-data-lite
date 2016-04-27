import net.sourceforge.tess4j.*;
import oracle.ord.hadoop.mapreduce.OrdFrameProcessor;
import oracle.ord.hadoop.mapreduce.OrdImageWritable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;

import java.awt.image.BufferedImage;

public class Tess
        extends OrdFrameProcessor<Text, OrdImageWritable, Text, Text> {

    private Text m_frame_key_text = null;
    private Text m_frame_value_text = null;
    private OrdImageWritable m_frame_image = null;
    private Tesseract instance = null;


    public Tess(Configuration conf) {
        super(conf);
        instance = Tesseract.getInstance();  
        instance.setDatapath("/usr/share/tesseract");
        instance.setLanguage("eng");
        instance.setPageSegMode(3);

    }


    /**
 *    Implement the processFrame method to process the key-value pair, an image,
 *    in the mapper of a MapReduce job. 
    */
    @Override
    public void processFrame(Text key, OrdImageWritable value) {

        if (m_frame_key_text == null ||m_frame_value_text == null || m_frame_image == null) {
            m_frame_image = new OrdImageWritable();
            m_frame_key_text = new Text();
            m_frame_value_text = new Text();
        }

        m_frame_key_text.set(key);

        BufferedImage bi = value.getImage();

        String ocrString ="";

        try {
          ocrString = "OCR OUTPUT:"+instance.doOCR(bi);
          System.out.println("Testing. "+ocrString);
        } catch (Exception e) {
          System.out.println("Key:"+key+"  exception: "+e);
          ocrString = "Exception:"+e;
        }
        m_frame_value_text.set(ocrString);
    }

    /**
     * Implement the getKey method to return the key after processing an image 
     * in the mapper.
     */
    @Override
    public Text getKey() {
        return m_frame_key_text;
    }

    /**
     * Implement the getValue method to return the value after processing an
     * image in the mapper.
     */
    @Override
    public Text getValue() {
        return m_frame_value_text;
    }
 
}

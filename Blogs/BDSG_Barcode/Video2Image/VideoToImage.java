import oracle.ord.hadoop.mapreduce.OrdFrameProcessor;
import oracle.ord.hadoop.mapreduce.OrdImageWritable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;

import java.awt.image.BufferedImage;


public class VideoToImage
        extends OrdFrameProcessor<Text, OrdImageWritable, Text, OrdImageWritable> {

    private Text m_frame_key_text = null;
    private Text m_frame_value_text = null;
    private OrdImageWritable m_frame_image = null;


    public VideoToImage(Configuration conf) {
        super(conf);
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

//this is where we do our custom code.
//In this example, do a simple identity map.  Take the image and return it.

        BufferedImage bi = value.getImage();

        m_frame_image.setImage(bi);
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
    public OrdImageWritable getValue() {
        return m_frame_image;
    }
 
}

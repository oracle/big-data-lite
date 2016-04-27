
import oracle.ord.hadoop.mapreduce.OrdFrameProcessor;
import oracle.ord.hadoop.mapreduce.OrdImageWritable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;


public class QRImage
        extends OrdFrameProcessor<Text, OrdImageWritable, Text, OrdImageWritable> {

    private Text m_frame_key_text = null;
    private Text m_frame_value_text = null;
    private OrdImageWritable m_frame_image = null;


    public QRImage(Configuration conf) {
        super(conf);

    }

    /**
     * Implement the processFrame method to process the key-value pair, an image,
     * in the mapper of a MapReduce job. 
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
        String barcodeString ="";

        try {
          barcodeString = BarcodeProcessor.processImage(bi);
          System.out.println("Testing. "+barcodeString);
        } catch (Exception e) {
          System.out.println("Key:"+key+"  exception: "+e);
          barcodeString = "Exception:"+e;
        }

        m_frame_value_text.set(barcodeString);

        int width = bi.getWidth();
        int height = bi.getHeight();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.drawImage(bi, 0, 0, null);
        g2d.setPaint(Color.blue);
        g2d.setFont(new Font("Serif", Font.BOLD, 36));
        FontMetrics fm = g2d.getFontMetrics();
        int y = fm.getHeight();

        for (String line : barcodeString.split(" ---- ")) {
          int x = width - fm.stringWidth(line) - 5;
          g2d.drawString(line, x, y += fm.getHeight());
        }
        g2d.dispose();

        m_frame_image.setImage(bufferedImage);

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


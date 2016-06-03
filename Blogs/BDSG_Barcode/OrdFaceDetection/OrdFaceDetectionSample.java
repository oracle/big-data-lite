/* $Header: hadoop/jsrc/analytics/example/sample/OrdFaceDetectionSample.java /main/1 2015/11/07 13:33:08 jiezhan Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    java sample code to extend OrdFrameProcessor for face detection

   MODIFIED    (MM/DD/YY)
    jiezhan     11/04/15 - Face Detection Sample
    jiezhan     11/04/15 - Creation
 */

/**
 *  @version $Header: hadoop/jsrc/analytics/example/sample/OrdFaceDetectionSample.java /main/1 2015/11/07 13:33:08 jiezhan Exp $
 *  @author  jiezhan 
 *  @since   1.1
 */

 /* for the latest version of this sample java code, check out:
 /u01/oracle-spatial-graph/multimedia/example/sample/oracle/ord/hadoop/demo/OrdFaceDetectionSample.java
 */
 
 
import oracle.ord.hadoop.mapreduce.OrdFrameProcessor;
import oracle.ord.hadoop.mapreduce.OrdImageWritable;
import static oracle.ord.hadoop.mapreduce.OrdUtil.BufferedImageToMat;
import static oracle.ord.hadoop.mapreduce.OrdUtil.MatToBufferedImage;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.opencv.core.CvType;
import org.opencv.core.Core; //added this import
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class OrdFaceDetectionSample
        extends OrdFrameProcessor<Text, OrdImageWritable, Text, OrdImageWritable> {

    private Text m_frame_text = null;
    private OrdImageWritable m_frame_image = null;
    private CascadeClassifier faceDetector = null;

    private String CLASSIFIER_NAME = "/u01/oracle-spatial-graph/multimedia/example/facetrain/config/haarcascade_frontalface_alt2.xml";
//added full path for CLASSIFIER NAME
    private double scaleFactor = 1.1;
    private int minNeighbors = 1;
    private int flags = Objdetect.CASCADE_SCALE_IMAGE
            | Objdetect.CASCADE_DO_ROUGH_SEARCH;
    private Size minSize = new Size(100, 100);
    private Size maxSize = new Size(500, 500);

    public OrdFaceDetectionSample(Configuration conf) {
        super(conf);
System.loadLibrary(Core.NATIVE_LIBRARY_NAME); //added loadLibrary
        faceDetector = new CascadeClassifier(CLASSIFIER_NAME);

    }

    /**
     * Implement the processFrame method to process the key-value pair, an image,
     * in the mapper of a MapReduce job. This method detects faces in the input
     * image and draws a bounding box around each face.
     */
    @Override
    public void processFrame(Text key, OrdImageWritable value) {

        if (m_frame_text == null || m_frame_image == null) {
            m_frame_image = new OrdImageWritable();
            m_frame_text = new Text();
        }

        m_frame_text.set(key);

        Mat image = BufferedImageToMat(value.getImage());

        int width = image.width();
        int height = image.height();
        Mat grayImage = new Mat(width, height, CvType.CV_8UC1);
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Detect faces in the image.
        // MatOfRect is a special container class for Rect.
        MatOfRect faceDetections = new MatOfRect();

        faceDetector.detectMultiScale(grayImage, faceDetections, scaleFactor,
                minNeighbors, flags, minSize, maxSize);

        // Draw a bounding box around each face.
        for (Rect rect : faceDetections.toArray()) {

            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(
                    rect.x + rect.width, rect.y + rect.height), new Scalar(
                            255, 0, 0), 4);

        }

        m_frame_image.setImage(MatToBufferedImage(image));
    }

    /**
     * Implement the getKey method to return the key after processing an image 
     * in the mapper.
     */
    @Override
    public Text getKey() {
        return m_frame_text;
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


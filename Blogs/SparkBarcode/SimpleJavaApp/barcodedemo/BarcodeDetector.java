package barcodedemo;

/* 
 * This demo is based on the ZXing.org web application's processImage() method in the DecodeServlet.java file.
 * The original DecodeServet.processImage() can be found here: 
 * https://raw.githubusercontent.com/zxing/zxing/master/zxingorg/src/main/java/com/google/zxing/web/DecodeServlet.java
 *
 * 
 * The processImage() method has been modified to remove the dependency on http request and response objects.
 * Modifications made by David Bayard.
 *
 * The original processImage() was written by ZXing authors- including Sean Owen
 *
 * Find out more about ZXing at https://github.com/zxing/zxing
 *
 *
 */

/* Here follows the license/author information from the original DecodeServlet.java file... */ 

/*
 * Copyright 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// @author Sean Owen
// The above @author was from the original DecodeServlet.java
//

/* Also, here is the NOTICE from the ZXing github project: https://github.com/zxing/zxing/blob/master/NOTICE */ 

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
//import com.google.zxing.client.j2se.ImageReader;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;

//import com.google.common.io.Resources;
//import com.google.common.net.HttpHeaders;
//import com.google.common.net.MediaType;

//import java.awt.color.CMMException;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStreamWriter;
//import java.io.Writer;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
//import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
//import java.util.Locale;
import java.util.Map;
//import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class BarcodeDetector {

    private static final Logger log = Logger.getLogger(BarcodeDetector.class.getName());

    // No real reason to let people upload more than a 10MB image
//    private static final long MAX_IMAGE_SIZE = 10_000_000L;
    // No real reason to deal with more than maybe 10 megapixels
//    private static final int MAX_PIXELS = 10_000_000;
//    private static final byte[] REMAINDER_BUFFER = new byte[32768];
    private static final Map<DecodeHintType,Object> HINTS;
    private static final Map<DecodeHintType,Object> HINTS_PURE;

    static {
      HINTS = new EnumMap<>(DecodeHintType.class);
      HINTS.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
      HINTS.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
      HINTS_PURE = new EnumMap<>(HINTS);
      HINTS_PURE.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
    }
    
    public BarcodeDetector() {
        super();
    }

    private static void processImage(BufferedImage image) throws IOException {

      LuminanceSource source = new BufferedImageLuminanceSource(image);
      BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
      Collection<Result> results = new ArrayList<>(1);

      try {

        Reader reader = new MultiFormatReader();
        ReaderException savedException = null;
        try {
          // Look for multiple barcodes
          MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
          Result[] theResults = multiReader.decodeMultiple(bitmap, HINTS);
          if (theResults != null) {
            results.addAll(Arrays.asList(theResults));
          }
        } catch (ReaderException re) {
          savedException = re;
        }
    
        if (results.isEmpty()) {
          try {
            // Look for pure barcode
            Result theResult = reader.decode(bitmap, HINTS_PURE);
            if (theResult != null) {
              results.add(theResult);
            }
          } catch (ReaderException re) {
            savedException = re;
          }
        }
    
        if (results.isEmpty()) {
          try {
            // Look for normal barcode in photo
            Result theResult = reader.decode(bitmap, HINTS);
            if (theResult != null) {
              results.add(theResult);
            }
          } catch (ReaderException re) {
            savedException = re;
          }
        }
    
        if (results.isEmpty()) {
          try {
            // Try again with other binarizer
            BinaryBitmap hybridBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result theResult = reader.decode(hybridBitmap, HINTS);
            if (theResult != null) {
              results.add(theResult);
            }
          } catch (ReaderException re) {
            savedException = re;
          }
        }
    
        if (results.isEmpty()) {
          try {
            throw savedException == null ? NotFoundException.getNotFoundInstance() : savedException;
          } catch (FormatException | ChecksumException e) {
            log.info(e.getMessage());
           // errorResponse(request, response, "format");
          } catch (ReaderException e) { // Including NotFoundException
            log.info(e.getMessage());
           // errorResponse(request, response, "notfound");
          }
          return;
        }

      } catch (RuntimeException re) {
        // Call out unexpected errors in the log clearly
        log.log(Level.WARNING, "Unexpected exception from library", re);
        throw new RuntimeException(re);
      }
      
        System.out.println("Results from Barcode Detection:");
        for (Result result : results) {

          ParsedResult parsedResult = ResultParser.parseResult(result);
          System.out.println(
            " (format: " + result.getBarcodeFormat() +
            ", type: " + parsedResult.getType() + "):\n" +
            "Raw result:\n" +
            result.getText() + "\n" +
            "Parsed result:\n" +
            parsedResult.getDisplayResult());
          System.out.println("Found " + result.getResultPoints().length + " result points.");
          for (int i = 0; i < result.getResultPoints().length; i++) {
            ResultPoint rp = result.getResultPoints()[i];
            System.out.println("  Point " + i + ": (" + rp.getX() + ',' + rp.getY() + ')');
          }
        }

     /* String fullParameter = request.getParameter("full");
      boolean minimalOutput = fullParameter != null && !Boolean.parseBoolean(fullParameter);
      if (minimalOutput) {
        response.setContentType(MediaType.PLAIN_TEXT_UTF_8.toString());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (Writer out = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
          for (Result result : results) {
            out.write(result.getText());
            out.write('\n');
          }
        }
      } else {
        request.setAttribute("results", results);
        request.getRequestDispatcher("decoderesult.jspx").forward(request, response);
      }
          */
    }

    /* private static void errorResponse(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String key) throws ServletException, IOException {
      Locale locale = request.getLocale();
      if (locale == null) {
        locale = Locale.ENGLISH;
      }
      ResourceBundle bundle = ResourceBundle.getBundle("Strings", locale);
      String title = bundle.getString("response.error." + key + ".title");
      String text = bundle.getString("response.error." + key + ".text");
      request.setAttribute("title", title);
      request.setAttribute("text", text);
      request.getRequestDispatcher("response.jspx").forward(request, response);
    }
    */
    
    public static void main(String[] args) {
        BarcodeDetector bd = new BarcodeDetector();
        System.out.println("Simple Standalone Java Application for ZXing. Will process "+args[0]);
        
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(args[0]));
            BarcodeDetector.processImage(img);
        } catch (IOException e) {
            System.out.println("ex:"+e);
        }
        
      
    }
}

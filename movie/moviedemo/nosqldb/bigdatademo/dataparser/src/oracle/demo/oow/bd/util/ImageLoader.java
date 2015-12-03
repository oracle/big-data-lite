package oracle.demo.oow.bd.util;


import java.awt.image.BufferedImage;

import java.io.File;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;


public class ImageLoader {

    public ImageLoader() {
        super();
    }

    public static void saveImage(String imgUrl, String name) {
        URL url = null;
        //System.out.println(imgUrl);

        try {
            //Save image to disk
            name = name.replaceAll(" ", "_");
            name = name.replaceAll(":", "");
            name = name.replaceAll("&", "");
            name = name.replaceAll("!", "");
            //name = name.replaceAll("\\", "");

            url = new URL(imgUrl);
            BufferedImage bi = ImageIO.read(url);
            File f = new File(File.separator + name + ".jpg");
            ImageIO.write(bi, "jpg", f);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}


package oracle.demo.oow.bd.util;

import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtil {
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEmpty(String str) {
        boolean flag = false;
        if (str == null || str.trim().length() == 0 ||
            "null".equalsIgnoreCase(str.trim())) {
            flag = true;
        }
        return flag;
    }

    public static String getMessageDigest(String message) {
        byte[] bytesOfMessage = null;
        ;
        byte[] thedigest = null;

        try {
            bytesOfMessage = message.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            thedigest = md.digest(bytesOfMessage);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new String(thedigest);

    }//getMessageDigest


}

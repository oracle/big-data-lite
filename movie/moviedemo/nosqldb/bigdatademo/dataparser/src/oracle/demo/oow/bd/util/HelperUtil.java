package oracle.demo.oow.bd.util;

import java.util.Enumeration;
import java.util.Hashtable;

import oracle.demo.oow.bd.to.MovieTO;

import oracle.kv.Key;

public class HelperUtil {
    private static int idCount = 0;
    private static Hashtable<String, String> idHash =
        new Hashtable<String, String>();

    public static int getUniqueId(String key) {
        int id = 0;
        if (idHash.containsKey(key)) {
            id = Integer.parseInt(idHash.get(key));
        } else {
            id = ++idCount;
            idHash.put(key, String.valueOf(id));
        }

        return id;
    } //getUniqueId

    public static void printUniqueId() {
        Enumeration keys = idHash.keys();
        while (keys.hasMoreElements()) {
            System.out.println(keys.nextElement());
        } //while
    } //printUniqueId

    

}

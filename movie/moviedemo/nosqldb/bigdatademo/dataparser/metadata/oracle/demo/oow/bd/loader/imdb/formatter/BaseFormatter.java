package oracle.demo.oow.bd.loader.imdb.formatter;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Iterator;
import java.util.StringTokenizer;

import oracle.demo.oow.bd.dao.BaseDAO;
import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.util.KeyUtil;
import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.Direction;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;

public class BaseFormatter {

    private static int uniqueId = 2000000;
    private static String oldName = null;

    public BaseFormatter() {
        super();
    }

    public boolean hasCastInfo(String line) {
        boolean flag = false;
        String token = null;
        int count = 0;

        StringTokenizer st = new StringTokenizer(line, "\t");
        count = st.countTokens();

        if (count == 2) {
            token = st.nextToken();
            //if (token.indexOf(',') > -1)
            flag = true;
        }
        return flag;
    }

    protected int getUniqueCastId(String name) {
        int id = 0;
        //check if name exist in hash
        if (StringUtil.isNotEmpty(name) && name.equals(oldName)) {
            id = uniqueId;
        } else {
            id = ++uniqueId;
        }
        oldName = name;
        return id;
    } //getUniqueCastId

    protected String getTitleWithDate(String info) {
        int end = 0;
        String title = null;
        if (StringUtil.isNotEmpty(info)) {
            //get the title information with date
            end = info.indexOf(')');
            if (end > -1) {
                title = info.substring(0, end + 1);
            }
        } //EOF if
        return title;
    }

    protected String getFormatedName(String name) throws Exception {
        String firstName = null;
        String lastName = null;
        String nameTitle = "";

        StringTokenizer st = null;
        int start = -1;
        int end = -1;


        //check if name has comma
        if (StringUtil.isNotEmpty(name) && name.indexOf(',') > -1) {
            st = new StringTokenizer(name, ",");
            lastName = st.nextToken().trim();
            firstName = st.nextToken().trim();

            //first name can also have title like (I) or (II)
            start = firstName.lastIndexOf('(');
            end = firstName.lastIndexOf(')');
            if (start > -1 && end > -1) {
                nameTitle = firstName.substring(start + 1, end);
                firstName = firstName.substring(0, start).trim();
            }
            name = firstName + " " + lastName + " " + nameTitle;
            name = name.trim();

        }

        return name;

    } //getFormatedName

    public String getMovieKey(String title) {
        String key = null;
        String date = null;
        MovieTO movieTO = new MovieTO();
        int start = -1;
        int end = -1;

        if (StringUtil.isNotEmpty(title)) {
            start = title.indexOf('(');
            end = title.indexOf(')');

            if (start > -1 && end > -1) {
                date = title.substring(start + 1, end);
                title = title.substring(0, start).replace("\"", "").trim();
                //set the values to movieTO
                movieTO.setTitle(title.trim());
                movieTO.setDate(date);
                //now create key
                key = KeyUtil.getMovieKey(movieTO);
            } //EOF if(start > -1 && end > -1) {
        } //EOF if (StringUtil.isNotEmpty(title)) {

        return key;
    } //getMovieKey

    public static void writeMovieInfo(String filePath, String searchPrefix) {


        FileWriter infoFile = null;
        KeyValueVersion valueVersion = null;
        String value = null;
        int count = 0;

        try {
            infoFile = new FileWriter(filePath);
            System.out.println("Movies information is written into: '" +
                               filePath + "'");
            Key parentKey = Key.createKey(searchPrefix);

            Iterator<KeyValueVersion> iter =
                BaseDAO.getKVStore().storeIterator(Direction.UNORDERED,
                    /*batchSize*/0,
                    /*parentKey*/parentKey, /*subRange*/null, /*depth*/null);


            while (iter.hasNext()) {
                valueVersion = iter.next();

                if (valueVersion != null) {
                    value =
                            new String(valueVersion.getValue().toByteArray()).trim();
                    //print it on the screen

                    System.out.println(++count + " " + value);
                    //write to file
                    infoFile.write(value + "\n");
                }


            } //EOF while

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                infoFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    } //writeMovieCastInfo

    protected boolean isValidMovieLine(String line) {
        boolean flag = false;
        if (StringUtil.isNotEmpty(line) && line.indexOf('{') == -1 &&
            line.indexOf('}') == -1 && line.indexOf("(TV)") == -1 &&
            line.indexOf("(V)") == -1 && line.indexOf("(VG)") == -1) {
            flag = true;
        }
        return flag;
    }//isValidMovieLine


}

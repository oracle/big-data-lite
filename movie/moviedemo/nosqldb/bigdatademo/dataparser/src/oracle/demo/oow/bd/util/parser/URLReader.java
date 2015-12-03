package oracle.demo.oow.bd.util.parser;

import java.net.*;

import java.io.*;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLReader {

    private static String check = null;
    private static boolean inetFlag = false;

    /**
     * @param urlStr
     * @return
     */
    public static String getURLContent(String urlStr) {
        URL url;
        URLConnection conn;
        BufferedReader in;
        String html;
        StringBuffer sb = new StringBuffer();
        int timeoutMs = 10000;

        try {
            url = new URL(urlStr);
            conn = url.openConnection();
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((html = in.readLine()) != null) {
                sb.append(html);
                sb.append("\n");
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            //TODO - don't do anything
            //e.printStackTrace();
        }

        return sb.toString();
    } //getURLContent


    public static HashMap<String, String> getHttpLinks(String searchStr, String html) {
        HashMap<String, String> httpMap = new HashMap<String, String>();
        StringTokenizer st = new StringTokenizer(html, "\n");
        String line = null;

        while (st.hasMoreTokens()) {
            line = st.nextToken();
            if (line.indexOf(searchStr) > -1) {
                //parse href
                httpMap.putAll(hrefParser(line));
            }
        }
        return httpMap;

    }

    private static HashMap<String, String> hrefParser(String entirePage) {
        HashMap<String, String> httpMap = new HashMap<String, String>();

        Pattern p = Pattern.compile("<a.*?href=\"(.*?)\">(.*?)</a>");

        Matcher m = p.matcher(entirePage);
        String href = null;
        String title = null;

        while (m.find()) {
            href = m.group(1);
            title = m.group(2);

            httpMap.put(title, href); // get the string inside href="......" <-
            //System.out.println(title + "  " + href);
        }
        return httpMap;
    }

    public static boolean isInternetReachable() {

        if (check == null) {
            check = "";

            try {

                InetAddress address = InetAddress.getByName("oracle.com");

                if (address != null) {
                    inetFlag = true;
                }

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                System.err.println("ERROR: Internet Connection is down. Please check the connection and restart the application.");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } //EOF if

        return inetFlag;
    } //isInternetReachable

    public static void main(String[] args) throws Exception {

        String html = getURLContent("http://en.wikipedia.org/wiki/List_of_American_films_of_2000");
        System.out.println(html);
    } //main


}

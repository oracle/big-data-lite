package oracle.demo.oow.bd.loader.tmdb;


import java.io.FileWriter;

import java.io.IOException;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.util.StringUtil;
import oracle.demo.oow.bd.util.parser.URLReader;

public class MovieDownloader implements Constant {


    public static String getMovieInfo(String id) {
        String movieInfoUrl = getMovieInfoUrl(id);
        String jsonReply = URLReader.getURLContent(movieInfoUrl);
        return jsonReply;
    }

    public static String getMovieCast(String id) {
        String movieCastUrl = getMovieCastUrl(id);
        String jsonReply = URLReader.getURLContent(movieCastUrl);
        return jsonReply;
    }

    private static String getMovieInfoUrl(String id) {
        return Constant.TMDb_MOVIE_INFO_URL + id + Constant.TMDb_KEY;
    }

    private static String getMovieCastUrl(String id) {
        return Constant.TMDb_MOVIE_INFO_URL + id + Constant.TMDb_MOVIE_CAST + Constant.TMDb_KEY;
    }

    public static void downloadMovies() {
        int maxCount = 103050;
        int sno = 1;
        String jsonReply = null;
        FileWriter infoFile = null;
        String line = null;

        try {
            infoFile = new FileWriter(Constant.MOVIE_INFO_FILE_NAME);
            System.out.println("Movies information is written into: '" + Constant.MOVIE_INFO_FILE_NAME + "'");

            //Movie has unique ID & it goes like 1, 2, .. maxCount
            for (int id = 0; id < maxCount; id++) {
                jsonReply = getMovieInfo(String.valueOf(id));
                if (StringUtil.isNotEmpty(jsonReply)) {
                    line = jsonReply;
                    //print it on the screen
                    System.out.println(line);
                    //write to file
                    infoFile.write(line);
                }
            } //EOF for
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                infoFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    } //getMovies

    public static void downloadMoviesCast() {
        int maxCount = 103050;
        int sno = 1;
        String jsonReply = null;
        FileWriter castFile = null;
        String line = null;

        try {
            castFile = new FileWriter(Constant.MOVIE_CASTS_FILE_NAME);
            System.out.println("Movie Cast information is written into: '" + Constant.MOVIE_CASTS_FILE_NAME + "'");

            //Movie has unique ID & it goes like 1, 2, .. maxCount
            for (int id = 0; id < maxCount; id++) {
                jsonReply = getMovieCast(String.valueOf(id));
                if (StringUtil.isNotEmpty(jsonReply)) {
                    line = jsonReply;
                    //print it on the screen
                    System.out.println(line);
                    //write to file
                    castFile.write(line);
                }
            } //EOF for
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                castFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    } //getMovieCasts

    public static void main(String[] args) {
        //downloadMovies();
        downloadMoviesCast();
    } //main
}

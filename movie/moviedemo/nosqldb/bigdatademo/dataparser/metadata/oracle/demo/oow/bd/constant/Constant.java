package oracle.demo.oow.bd.constant;

import java.io.File;

public interface Constant {

    public static final char DELIMITER = '|';
    public static final int TARGET_NOSQL = 0;
    public static final int TARGET_RDBMS = 1;
    public static final int TARGET_BOTH = 2;
    
    public static final String EMPTY_PACKET = "";
    public static final String METADATA_DIR = "metadata";
    public static final String SCHEMA_DIR = "schemas";
    
    
    //public static final String TMDb_IMG_URL = "http://cf2.imgobject.com/t/p/original/";
    public static final String TMDb_IMG_URL =
        "http://cf2.imgobject.com/t/p/w92/";
    //This is Anuj's key. Other users should get their own keys
    public static final String TMDb_KEY =
        "?api_key=1af3d470678b6107247ce98ec7cef097";
    public static final String TMDb_MOVIE_INFO_URL =
        "http://api.themoviedb.org/3/movie/";
    public static final String TMDb_MOVIE_CAST = "/casts";

    public static final int MAGIC_NUM = 191;

    /** TMDb Movie File names **/
    public static final String MOVIE_INFO_FILE_NAME =
        METADATA_DIR + File.separator + "movie-info.out";
    public static final String MOVIE_CASTS_FILE_NAME =
        METADATA_DIR + File.separator + "movie-cast.out";
    public static final String MOVIE_IMG_FILE_NAME =
        METADATA_DIR + File.separator + "movie-img.out";
    
    /** Analytics File **/
    public static final String USER_ITEM_FILE_NAME =
        METADATA_DIR + File.separator + "user-item.out";

    /** Customer Profiles **/
    public static final String CUSTOMER_PROFILE_FILE_NAME =
        METADATA_DIR + File.separator + "customer.out";
    
    /** Make sure you have following movies under metadata/imdb/ directory
     * before you do an data formating: ratings.list, genres.list, plot.los
     *
     * By default these files are not downloaded under the desired directory
     * because they are so big but one can download them from the here:
     * ftp://ftp.funet.fi/pub/mirrors/ftp.imdb.com/pub/
     * **/
    public static final String IMDB = "imdb";
    public static final String IMDB_MOVIE_RATING_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator + "ratings.list";
    public static final String IMDB_MOVIE_GENRE_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator + "genres.list";
    public static final String IMDB_MOVIE_PLOT_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator + "plot.list";

    public static final String IMDB_MOVIE_INFO_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator +
        "movie-info.out";
    //"movie-info.out.test";

    public static final String IMDB_MOVIE_CAST_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator +
        "movie-cast.out";
    public static final String IMDB_MOVIE_CREW_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator +
        "movie-crew.out";

    public static final String IMDB_ACTOR_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator +
        //"actors-test.list";
        "actors.list";
    public static final String IMDB_ACTRESS_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator +
        "actresses.list";
    public static final String IMDB_DIRECTOR_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator +
        "directors.list";
        //"directors.list.test";
    public static final String IMDB_WRITER_FILE_NAME =
        METADATA_DIR + File.separator + IMDB + File.separator + "writers.list";
    public static final String NO_PROFILE_IMG =
        "http://cf2.themoviedb.org/assets/8ede7fdf39f4c/images/no-profile-w185.jpg";

    /**
     * Database related constants
     */

    public static final String JDBC_URL =
        "jdbc:oracle:thin:@localhost:1521:orcl";
    public static final String DB_DEMO_USER = "MOVIEDEMO";
    public static final String DEMO_PASSWORD = "welcome1";    
    
}

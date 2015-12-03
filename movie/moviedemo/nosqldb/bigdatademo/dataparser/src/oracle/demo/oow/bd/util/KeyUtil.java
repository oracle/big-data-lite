package oracle.demo.oow.bd.util;

import java.text.DateFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oracle.demo.oow.bd.constant.JsonConstant;
import oracle.demo.oow.bd.constant.KeyConstant;

import oracle.demo.oow.bd.dao.CustomerDAO;
import oracle.demo.oow.bd.dao.MovieDAO;
import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.to.CastTO;
import oracle.demo.oow.bd.to.CrewTO;
import oracle.demo.oow.bd.to.CastCrewTO;
import oracle.demo.oow.bd.to.CustomerTO;

import oracle.kv.Key;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Table;


/**
 * To read/write data from the kv-store you need to construct a {@code Key}
 * object that you would use to execute get/put operations.
 * <p>
 * This class defines all different kind of {@code Key}/s that are used in the
 * application. The general structure to represent the {@code Key} is that
 * the different key components are separated by '/' and the major key components
 * are separated from minor key components by '-'.
 * </p>
 * <p>
 * For example if my major key has two components 'USER', and 'userId' then I
 * will represent them as /USER/userId/ and if the minor key has one component
 * as 'timestamp' then to  represent the full key (major & minor) I would use
 * the notation:  /USER/userId/-/timestamp/
 * </p>
 *
 */
public class KeyUtil {

    /**
     * Each movie has multiple actor and actresses (Cast) who played a role in 
     * that movie. Before you create an association of the Cast with the Movie
     * you would need to store their profile information like Name, DOB etc 
     * and save the profile information under a unique key. 
     * <p>
     * This method defines Key structure for storing the information of an
     * individual cast uniquely identified as a 'castId'. The key will be 
     * constructed with a prefix 'CT' followed by 'castId' as the major key 
     * components and 'info' as the minor key component. 
     * <p>     
     * The value for this key would be JSON text representing Cast profile. To
     * learn more on the json structure see CastTO.
     * @param castId Unique ID of the actor/actoress
     * @return Key structure reprsented as /CT/castId/-/info/
     * @see CastTO
     */
    public static PrimaryKey getCastInfoKey(int castId,Table tblName) {
        String prefix = KeyConstant.CAST_TABLE;

        PrimaryKey key = null;
       

        if (castId > 0) {
            key = tblName.createPrimaryKey();
            key.put("id", castId);
        } //if (castId > 0) {
        return key;
    } //getCastInfoKey

    /**
     * Just like a each movie has Cast (Actor & Actresses) it also has Crew 
     * (Directors, & Writers). You would need to save their details too in the 
     * store just the way you stored Cast information.
     * <p>
     * This method defines Key structure for storing the information of an
     * individual Crew uniquely identified as a 'crewId'. The key will be 
     * constructed with a prefix 'CW' followed by 'crewId' as the major key 
     * components and 'info' as the minor key component. 
     * <p>     
     * The value for this key would be a JSON text representing Crew profile. To
     * learn more on the json structure see CrewTO.
     * @param crewId Unique ID of the actor/actoress
     * @return key reprsented as /CW/crewId/-/info/
     * @see CrewTO
     */
    public static PrimaryKey getCrewInfoKey(int crewId,Table crewTable) {
        String prefix = KeyConstant.CREW_TABLE;

        PrimaryKey key = null;
   

        if (crewId > 0) {
            
            key = crewTable.createPrimaryKey();
            
           
            key.put("id",crewId);

        } //if (crewId > 0) {
        return key;
    } //getCrewInfoKey

    /**
     * A movie may have many cast and crew and if you would like to find out 
     * who all cast/crew worked in a movie then you would need to create the 
     * association of movie with cast/crew.
     * <p>
     * This method creates a simple key structure with /MV/movieId/ as the major
     * key component and /cc as the minor component. The value saved under this
     * key would be JSON text representing CastCrewTO object.
     * <p>
     * 
     * @param movieId - Pass id of the movie for which you would like to know 
     * who all cast and crew members are. 
     * @return Key - structure representing /MV/movieId/-/cc/ which when passed
     * to get() operation, returns a Value component represents JSON text of
     * CastCrewTO object.
     * @see CastCrewTO
     */
    public static PrimaryKey getMovieCastCrewKey(int movieId,int ccId,Table movieTable) {
        String prefix = KeyConstant.MOVIE_CAST_CREW_TABLE;
        PrimaryKey key = null;
        List<String> majorComp = null;
         Table childTable = movieTable.getChildTable(MovieDAO.CHILD_TABLE);
        if (movieId > 0) {
            majorComp = new ArrayList<String>();

             key = childTable.createPrimaryKey();
             key.put("id",movieId);
             key.put("mid",ccId);
            //Add Major component
           
        } //if (movieId > 0) {
        return key;
    } //getMovieCastCrewKey

    /**
     * Each movie may belong to one or many genres. To create the association
     * of genres to the movies that belong to this genre, this key is used. This
     * represents the intersaction table where the primary key, in this case 
     * genreId is linked to multiple secondary keys, movieId. 
     * <p>
     * There is no value component for this key as one-to-many mapping itself
     * is the information and there is no need to save any further information.
     * <p>
     * 
     * @param genreId - Genre id this movie belongs to
     * @param movieId - of the movie that would be associated to the genreId
     * @return Key - structure representing /GN_MV/genreId/-/movieId/
     */
    public static Key getGenreMovieKey(int genreId, int movieId) {
        Key key = null;

        if (movieId > -1) {
            List<String> majorComponent = new ArrayList<String>();
            //TODO - Rerun Load by uncommenting this line
            majorComponent.add(KeyConstant.GENRE_MOVIE_TABLE);


            majorComponent.add(Integer.toString(genreId));
            if (movieId > 0) {
           //     key = Key.createKey(majorComponent, Integer.toString(movieId));
            } else {
            //    key = Key.createKey(majorComponent);
            }
        } //EOF if
        return key;
    } //getGenreMovieKey

    /**
     * Each genre has some name e.g. Animation, Drama, War, Action etc and also
     * a unique id. To create a cross reference between genre name and genre 
     * id this key is used.
     *<p>
     * This method creates a key with major components as /GN/genreName/ and 
     * saves JSON string representing GenreTO as its value.
     * 
     * @param genreName - name of the genre
     * @return Key - structure representing /GN/genreName/
     */
    public static IndexKey getGenreNameKey(String genreName,Table tblName) {
        IndexKey key = null;

        if (StringUtil.isNotEmpty(genreName)) {
            key = tblName.getIndex("genreNameIndex").createIndexKey();
            key.put("name",genreName);
        }

        return key;
    } //getGenreNameKey

    /**
     * Customer profile information like name, email, encrypted credential, etc
     * are saved in kv-store. When user login into the applications their
     * credential are authorized. 
     * <p>
     * Each customer profile is saved as a JSON string representing CustomerTO. 
     * When you need to fetch the profile just pass username to this method and 
     * execute get() operation on this key, which will return the JSON string
     * representing CustomerTO.
     * 
     * @param userName - of the user
     * @return Key - structure representing /CUST/username/
     * @see CustomerTO
     */
    public static Key getCustomerKey(String userName) {

        Key key = null;
        List<String> majorComponent = null;
        //username & password should not be null
        if (StringUtil.isNotEmpty(userName) ) {
            majorComponent = new ArrayList<String>();
            majorComponent.add(KeyConstant.CUSTOMER_TABLE);
            majorComponent.add(userName.toLowerCase());            
            
            key = Key.createKey(majorComponent, KeyConstant.INFO_COLUMN);
        }

        return key;
    }//getCustomerKey

    /**
     * Movie object
     * Returns key Structure as /MV/movieId/
     * @param movieId - TMDb movie ID
     * @return - Key component
     */
    public static PrimaryKey getMovieIdKey(int movieId,Table tblName) {
        PrimaryKey key = null;

        if (movieId > -1) {
            key = tblName.createPrimaryKey();
            key.put("id",movieId);

            
        }
        return key;
    } //getMovieIdKey

    /**
     * Returns a key with structure /MV/movieName/-/movieId
     * This method will be used to get the movie by name
     * @param movieName
     * @param movieId
     * @return
     */
    public static IndexKey getMovieNameKey(String movieName, int movieId,Table tblName) {
        IndexKey key = null;

        if (StringUtil.isNotEmpty(movieName)) {
            Index index = tblName.getIndex("movieName");
            key = index.createIndexKey();
//            
//            List<String> majorComponent = new ArrayList<String>();
//            majorComponent.add(KeyConstant.MOVIE_NAME_TABLE);
//            majorComponent.add(movieName.toLowerCase());
//            if (movieId > 0) {
//                key = Key.createKey(majorComponent, Integer.toString(movieId));
//            } else {
//                key = Key.createKey(majorComponent);
//            } //EOF if
//
        } //EOF if

        return key;
    } //getMovieNameKey

    /**
     * This method will return key=/CT/custId/-/genre
     * @param custId _ CustomerId
     * @return Key
     */
    public static PrimaryKey getCustomerGenresKey(int custId,Table customerTable) {
        PrimaryKey key = null;

        Table customerGenreTable = customerTable.getChildTable(CustomerDAO.CHILD_TABLE);
        key = customerGenreTable.createPrimaryKey();
        if(custId >-1){
            key.put("id",custId);
            key.put("cid",custId);
        }
        
        return key;
    } //getCustomerGenreMovieKey

    /**
     * This key is to store association between customer and the genres using
     * genreId.
     * key=/CT_GN_MV/custId/genreId/-/movieId
     * @param custId
     * @param genreId
     * @param movieId
     * @return Key
     */
    public static PrimaryKey getCustomerGenreMovieKey(int custId, int genreId,
                                               int movieId,Table customerGenreTable) {
        PrimaryKey key = null;

        
        if (genreId > 0) {
            
            key = customerGenreTable.createPrimaryKey();
            
            
            key.put(JsonConstant.ID,custId);
            key.put(JsonConstant.GENRE_ID, genreId);

            if (movieId > 0) {
                key.put(JsonConstant.MOVIE_ID, movieId);
            } 
        } //if (movieId > 0 )

        return key;
    } //getCustomerGenreMovieKey

    /**
     * Customer_Movie table is used to store rating that user gives to the movies
     * @param custId
     * @param movieId
     * @return Key
     */
    public static PrimaryKey getCustomerMovieKey(int custId, int movieId,Table activityTable) {
        PrimaryKey key = null;

        if (custId > 0 && movieId > 0) {
            key = activityTable.createPrimaryKey();
            key.put(JsonConstant.TABLE_ID,"CLICK");
            key.put(JsonConstant.CUST_ID,custId);
            key.put(JsonConstant.MOVIE_ID,movieId);
            
        } //if (movieId > 0 )

        return key;
    } //getCustomerMovieKey


    /**
     * This method returns the key structure to store what everyone is watching
     * @return key=/CM_CWL/YYYYMMDD-/timestamp
     */
    public static IndexKey getCommonWatchListKey(Table activityTable) {
        IndexKey key = null;
        long timeStamp = System.currentTimeMillis();
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String yyyyMMdd = formatter.format(new Date());

        key = activityTable.getIndex("customerWatchListIndex").createIndexKey();
        
        key.put("tableId",KeyConstant.COMMON_CURRENT_WATCH_LIST);
        key.put("time",yyyyMMdd);
        
        return key;
    } //getCommonWatchlistKey

    /**
     * This method returns the key to store user browsing history. Remember when
     * user click a movie and start to watch it, its history is stored in different
     * queue
     * @param custId
     * @param movieId
     * @return Key=/CT_BL/custId/-/timestamp
     */
    public static PrimaryKey getCustomerBrowseListKey(int custId, int movieId,Table tableName) {
        return getCustomerBrowseKey(custId, KeyConstant.CUSTOMER_BROWSE_LIST,
                                    movieId,tableName);
    } //getCustomerBrowselistKey

    /**
     * This method returns the key to record the current movie that customer is
     * watching.
     * @param custId
     * @param movieId
     * @return Key=/CT_CWL/custId/-/timestamp
     */
    public static PrimaryKey getCustomerCurrentWatchListKey(int custId, int movieId,Table tableName) {
        return getCustomerBrowseKey(custId,
                                    KeyConstant.CUSTOMER_CURRENT_WATCH_LIST,
                                    movieId,tableName);
    } //getCustomerCurrentWatchlistKey

    /**
     * This method records the movies that customer has been done watching.
     * @param custId
     * @param movieId
     * @return Key=/CT_HWL/-/timestamp
     */
    public static PrimaryKey getCustomerHistoricalWatchListKey(int custId,
                                                        int movieId,Table tableName) {
        return getCustomerBrowseKey(custId,
                                    KeyConstant.CUSTOMER_HISTORICAL_WATCH_LIST,
                                    movieId,tableName);
    } //getCustomerHistoricalWatchlistKey

    /**
     * A movie can be clicked from the app by the user to read the details. When
     * this activity happens a movie is recorded into browse-queue. 
     * <p>
     * This method is the parent method that takes id of the customer and the 
     * movie id that was browsed, or watched or completed as the input argument.
     * @param custId - customer id
     * @param tableName - name of the queue where this activity need to be 
     * recorded to
     * @param movieId - movie id
     * @return Key representing /queuePrefix/custId/-/movieId
     * primary keys are /tableId, custId, movieId
     * Shardkey tableId,custId
     */
    private static PrimaryKey getCustomerBrowseKey(int custId, String id,
                                            int movieId,Table tblName) {
        PrimaryKey key = null;



        if (custId > 0) {
            key  = tblName.createPrimaryKey();
            key.put("tableId", id);
            key.put("custId",custId);

            if (movieId > 0)
                 key.put("movieId",movieId);
            
        } //if (custId > 0 )

        return key;
    } //getCustomerBrowselistKey

    public static String getMovieKey(MovieTO movieTO) {
        String key = null;
        if (movieTO != null) {
            key = movieTO.getTitle() + "|" + movieTO.getDate();
            key = key != null ? key.toLowerCase().trim() : null;
        }
        return key;
    }

    /**
     * To store JSON text of the MovieTO you would need unique key representing
     * movie is as the major component. This key is used during IMDb data 
     * formating time.
     * @param movieKey - is passed and the string is just lowered case
     * @return lower case of the movie key string 
     */
    public static String getMovieKey(String movieKey) {

        if (StringUtil.isNotEmpty(movieKey)) {
            movieKey = movieKey.toLowerCase().trim();
        }
        return movieKey;
    } //getMovieKey
    
}

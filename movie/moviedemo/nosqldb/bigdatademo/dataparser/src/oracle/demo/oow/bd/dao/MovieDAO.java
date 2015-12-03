package oracle.demo.oow.bd.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;


import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import oracle.demo.oow.bd.constant.KeyConstant;
import oracle.demo.oow.bd.constant.MovieConstant;
import oracle.demo.oow.bd.to.ScoredGenreTO;
import oracle.demo.oow.bd.to.GenreTO;
import oracle.demo.oow.bd.to.MovieTO;

import oracle.demo.oow.bd.pojo.SearchCriteria;
import oracle.demo.oow.bd.to.ActivityTO;
import oracle.demo.oow.bd.to.CastCrewTO;
import oracle.demo.oow.bd.to.CastTO;
import oracle.demo.oow.bd.to.CrewTO;
import oracle.demo.oow.bd.to.CustomerGenreMovieTO;
import oracle.demo.oow.bd.to.CustomerGenreTO;

import oracle.demo.oow.bd.util.KeyUtil;
import oracle.demo.oow.bd.util.StringUtil;

import oracle.demo.oow.bd.util.parser.URLReader;


import oracle.kv.Value;
import oracle.kv.avro.JsonAvroBinding;
import oracle.kv.avro.JsonRecord;

import oracle.kv.table.IndexKey;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

import oracle.kv.table.TableIterator;

import org.apache.avro.Schema;

import org.codehaus.jackson.node.ObjectNode;


public class MovieDAO extends BaseDAO {


    private GenreDAO genreDAO = null;
    private static Table movieTable = null;
    public final static String TABLE_NAME="MOVIE";
    public final static String CHILD_TABLE = "CastCrew";


    /** Variables for JSONAvroBinding ***/

    private Schema movieSchema = null;
    private JsonAvroBinding movieBinding = null;

    public MovieDAO() {
        super();
        genreDAO = new GenreDAO();
        movieTable = getKVStore().getTableAPI().getTable(TABLE_NAME);
    }

    /**
     * This method inserts Movie information and index its ID as well as name.
     * Movie may belong to many genres so this method also create mapping between
     * genre and movie.
     * @param movieTO - This is a movie transfer object
     * @return - true if insertion is successful
     */
    public boolean insertMovie(MovieTO movieTO) {
        boolean flag = false;
        String name = null;

   

        int movieId;

        if (movieTO != null) {
            name = movieTO.getTitle();

            movieId = movieTO.getId();

            /**
             * Insert movie and index the movieId.
             * Key=/MV/movieId/
             */
            
            // serialize MovieTO to byte array using JSONAvroBinding
            Row row = movieTable.createRowFromJson(movieTO.toJsonString(), true);
            //Insert movieTO into the store
             getKVStore().getTableAPI().put(row, null,null);

            /**
             * Index movie name also
             * Key=/MV/movieName/-/movieId
            create secondary index on movieName
             */
//            key = KeyUtil.getMovieNameKey(name, movieId);
//            //Information is stored in the key itself
//            put(key, Integer.toString(movieId));

/*
 * insert into child table movie.genres
 */
//            Table genreChild = movieTable.getChildTable(CHILD_TABLE);
//            
//            for(GenreTO genres:movieTO.getGenres()){
//                Row rowChild = genreChild.createRow();
//                rowChild.put("id", movieTO.getId());
//                
//                rowChild.put("gid",genres.getId());
//                rowChild.put("name",genres.getName());
//                rowChild.put("cid",KeyConstant.GENRE_TABLE);
//                getTableAPI().put(rowChild, null, null);
//            }
            
            
            /**
             * A movie may belong to many genres, so for each genreId create
             * a mapping to movieId
             */
            
            GenreDAO genreDAO = new GenreDAO();
            genreDAO.insertMovieGenres(movieTO);

        }
        return flag;
    } //insertMovie


    /**
     * Movie names are also indexed while inserting the movie data therefore
     * movie can be fetched my full movie name (case insensitive).
     * @param movieName
     * @return List of MovieTO
     * //moviename is secondary index
     */
    public List<MovieTO> getMoviesByName(String movieName) {
        
        IndexKey key = KeyUtil.getMovieNameKey(movieName, 0,movieTable);
        List<MovieTO> movieTOList = new ArrayList<MovieTO>();
        MovieTO movieTO = null;
        
        String value = null;
        Row row;
        String movieIdStr = null;

        TableIterator<Row> rowIter = super.getKVStore().getTableAPI().tableIterator(key,null,null);
        while (rowIter.hasNext()) {
            row = rowIter.next();
            value = row.toJsonString(true);
            movieIdStr = row.get("movieID").toString();
            movieTO = getMovieById(movieIdStr);

            //add the movie to the list
            movieTOList.add(movieTO);
            //System.out.println(keyValue.getKey().getFullPath() + " \n" +  movieTO.getMovieJson() + "\n ");
        } //EOF while

        return movieTOList;
    }

    /**
     * This method returns movie related information when movieId is passed
     * @param movieIdStr - String version of movie ID
     * @return MovieTO
     */
    public MovieTO getMovieById(String movieIdStr) {
        int movieId = 0;
        if (StringUtil.isNotEmpty(movieIdStr)) {
            try {
                movieId = Integer.parseInt(movieIdStr);
            } catch (NumberFormatException ne) {
                movieId = 0;
            } //EOF try/catch


        } //EOF if
        return getMovieById(movieId);
    } //getMovie

    /**
     * This method fetches movie information for the movieId
     * @param movieId - unique id of the movie
     * @return MovieTO
     */
    public MovieTO getMovieById(int movieId) {

        List<CastTO> castList = null;
        List<CrewTO> crewList = null;
        CastDAO castDAO = new CastDAO();
        CrewDAO crewDAO = new CrewDAO();
        CastCrewTO castCrewTO = new CastCrewTO();
        MovieTO movieTO = null;

        PrimaryKey key = KeyUtil.getMovieIdKey(movieId,movieTable);
        Row row = super.getKVStore().getTableAPI().get(key,null);
        if (row != null) {

            //Deserialize the movie avro object
            movieTO = new MovieTO(row.toJsonString(true));

            //If internet connection is not successful then reset the movie-poster
            if (movieTO != null && !URLReader.isInternetReachable())
                movieTO.setPosterPath("");

            //Get Cast Inforamtion and set it to castCrewTO
            castList = castDAO.getMovieCasts(movieId);
            castCrewTO.setCastList(castList);

            //Get Crew Inforamtion and set it to castCrewTO
            crewList = crewDAO.getMovieCrews(movieId);
            castCrewTO.setCrewList(crewList);

            //set castCrewTO to movieTO
            movieTO.setCastCrewTO(castCrewTO);
        }

        return movieTO;
    } //getMovieById


    /**
     * This method inserts Movie information and index its ID as well as name.
     * Movie may belong to many genres so this method also create mapping between
     * genre and movie.
     * @param movieTO - This is a movie transfer object
     * @return - true if insertion is successful
     */
    public boolean insertMovieRDBMS(MovieTO movieTO) {
        boolean flag = false;
        Connection conn = null;

        String date = movieTO.getDate();
        if (date.equalsIgnoreCase("null"))
            return false;

        if (movieTO != null) {

            // Insert record into Oracle RDBMS
            try {
                conn = super.getOraConnect();

                String update =
                    "INSERT INTO MOVIES.MOVIE (" + "ID, TITLE, RELEASE_DATE, POPULARITY, RUNTIME, VOTE_COUNT) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(update);
                stmt.setInt(1, movieTO.getId());
                stmt.setString(2, movieTO.getTitle());
                stmt.setDate(3, Date.valueOf(date));
                stmt.setDouble(4, movieTO.getPopularity());
                stmt.setInt(5, movieTO.getRunTime());
                stmt.setInt(6, movieTO.getVoteCount());

                try {
                    stmt.execute();
                } catch (SQLException e) {
                    // Expect many duplicates.  Duplicate error code = 1
                    if (e.getErrorCode() != 1)
                        System.out.println(e.getErrorCode() + ":" + e.getMessage());
                }

                stmt.close();
                conn.commit();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;

            }


            /**
                 * A movie may belong to many genres, so for each genreId create
                 * a mapping to movieId
                 */
            GenreDAO genreDAO = new GenreDAO();
            genreDAO.insertMovieGenresRDBMS(movieTO);

        }
        return flag;
    } //insertMovie


    /**
     * This method loop through all the genres and fetch top N movies each genre
     * that matces the SeachCriteria
     */
    public void insertTopMoviesPerGenre(SearchCriteria searchCriteria, int custId) {
        List<GenreTO> genreList = genreDAO.getGenres();
        List<MovieTO> movieList = null;
        String jsonTxt = null;

        PrimaryKey key = null;
        int total = 0;
        List<ScoredGenreTO> sGenreList = new ArrayList<ScoredGenreTO>();
        CustomerGenreTO customerGenreTO = new CustomerGenreTO();
        ScoredGenreTO score = null;

        int genreId = 0;
        int movieId = 0;

        for (GenreTO genreTO : genreList) {

            System.out.println("Genre: " + genreTO.getId() + " " + genreTO.getName());
            genreId = genreTO.getId();

            //initialize Score object
            score = new ScoredGenreTO();
            score.setId(genreId);
            score.setName(genreTO.getName());

            //Genre top movies for the genreId
            movieList = genreDAO.getMoviesByGenre(genreId, searchCriteria);

            //Calculate the score of top N movies selected for the genreId
            total = 0;

            //Store CustomerId -> GenreId -> MovieId association
            for (MovieTO movieTO : movieList) {
                total += (movieTO.getVoteCount() * movieTO.getPopularity());
                movieId = movieTO.getId();
                //key = KeyUtil.getCustomerGenreMovieKey(custId, genreId, movieId);
                CustomerGenreMovieTO cgm = new CustomerGenreMovieTO();
                cgm.setId(custId);
                cgm.setGenreId(genreId);
                cgm.setMovieId(movieId);
                
                jsonTxt = cgm.getJsonTxt();
                Row row = getTable(CustomerDAO.TABLE_NAME+"."+CustomerDAO.CUSTOMER_GENRE_MOVIE_TABLE).createRowFromJson(jsonTxt, true);
                getTableAPI().put(row, null, null);
                
                System.out.println("\t" + movieTO.getMovieJsonTxt());
            } //for (MovieTO movieTO : movieList)

            //Add the total to the score
            score.setScore(total);

            //Add score to the list
            sGenreList.add(score);

            //sort the list in descending order
            Collections.sort(sGenreList);


        } //EOF for

        System.out.println("Sorted Genre List");
        //Store CustomerId -> GenreId association
        customerGenreTO.setId(custId);
        /**
         * Save CustomerGenreTO jsonTxt to key=/CT/custId/-genre
         */

        
        
        jsonTxt = MovieConstant.ORDERED_GENRE_LIST;

        Row row = getTable(CustomerDAO.TABLE_NAME+"."+"customerGenres").createRowFromJson(jsonTxt,false);
        row.put("id",custId);
        getTableAPI().put(row, null, null);
        
      //  System.out.println("\t" + jsonTxt);

    } //insertTopMoviesPerGenre

    /**
     * This method takes key as input parameter, where key is the full major path
     * and then it fetches all the records with same major key. The 'value' part
     * of the record is the movieId.
     * @param key as the complete major path
     * @return List of MovieTO
     */
    
    public List<MovieTO> getMoviesByKey(PrimaryKey key) {
        if (key == null)
            return new ArrayList<MovieTO>();
        List<MovieTO> movieTOList = new ArrayList<MovieTO>();
        Row row = null;
        String value = null;
        //String activityJsonTxt = null;
        
              
        TableIterator<Row> rows = getKVStore().getTableAPI().tableIterator(key, null, null);
        while (rows.hasNext()) {
            row = rows.next();
            getMovie(row, movieTOList);        
        
        } //EOF while

        //Sort them based on the order
        Collections.sort(movieTOList);

        return movieTOList;
        } //getMovies4Key
        
    
    public List<MovieTO> getMoviesByKey(IndexKey key) {
        
        //Check for null key
        if (key == null)
            return new ArrayList<MovieTO>();
        
        List<MovieTO> movieTOList = new ArrayList<MovieTO>();
        Row row = null;
        String value = null;
        //String activityJsonTxt = null;
        
              
        TableIterator<Row> rows = getKVStore().getTableAPI().tableIterator(key, null, null);
        while (rows.hasNext()) {
            row = rows.next();
            getMovie(row, movieTOList);        
    
        } //EOF while

        //Sort them based on the order
        Collections.sort(movieTOList);

        return movieTOList;
    } //getMovies4Key

    private void getMovie(Row row, List<MovieTO> movieTOList){
        ActivityTO activityTO = null;
        String value = row.toJsonString(true);
        MovieTO movieTO = null;
        MovieDAO movieDAO = new MovieDAO();
        ActivityDAO activityDAO = new ActivityDAO();
        int movieId = 0;
        long timeStamp;

        //The value part of these KV pair is ActivityTO JSONTxt
        activityTO = new ActivityTO(value);

        //System.out.println("JSONTxt: " + activityJsonTxt);

        if (activityTO != null) {
            
            movieId = activityTO.getMovieId();
            timeStamp = activityTO.getTimeStamp();

            //get the movieTO from movieId
            movieTO = movieDAO.getMovieById(movieId);
            if (movieTO != null) {
                //set the timeStamp to movieTO
                movieTO.setOrder(timeStamp);

                //add the movie to the list
                movieTOList.add(movieTO);
            }
        } //if(StringUtil.isNotEmpty(activityJsonTxt)
    }
    /**
     * This method returns a list of all the movie titles stored in the database
     * @return List of MovieTO
     */
    public List<MovieTO> getMovies() {

        PrimaryKey key = key = movieTable.createPrimaryKey();
        Row row = null;
        String value=null;
        
        MovieTO movieTO = null;
        List<MovieTO> movieList = new ArrayList<MovieTO>();
        

        TableIterator<Row> rows = getKVStore().getTableAPI().tableIterator(key, null, null);
        while (rows.hasNext()) {
            row = rows.next();
            value = row.toJsonString(true);
             
            if (value!=null) {
                movieTO = new MovieTO(value);
                
                if (movieTO != null) {
                    movieList.add(movieTO);
                }
            }

        } //EOF while
        return movieList;
    }

    /**
     * This method takes CustomerTO object and serialize it to Value object.
     * @param movieTO
     * @return
     */
    public Value toValue(MovieTO movieTO) {
        ObjectNode movieNode = null;
        JsonRecord jsonRecord = null;
        Value value = null;

        if (movieTO != null) {
            movieNode = movieTO.getMovieJson();
            jsonRecord = new JsonRecord(movieNode, movieSchema);
            // serialize movieTO to byte array using JSONAvroBinding
            value = movieBinding.toValue(jsonRecord);
        }

        return value;

    } //toValue

    /**
     * This method takes Value object as input and deserialize it into CustomerTO. The assumption here is that
     * Value that is passed as argument is serialized CustomerTO JSON object.
     * @param movieTOValue
     * @return CustomerTO
     */
    public MovieTO getMovieTO(Value movieTOValue) {

        MovieTO movieTO = null;
        ObjectNode movieNode = null;
        JsonRecord jsonRecord = null;

        if (movieTOValue != null) {
            jsonRecord = movieBinding.toObject(movieTOValue);
            movieNode = (ObjectNode)jsonRecord.getJsonNode();
            movieTO = new MovieTO(movieNode);
        }

        return movieTO;

    } //toObject

    public static void main(String[] args) {
        MovieDAO mDAO = new MovieDAO();
        //MovieTO movieTO = null;
        SearchCriteria sc = new SearchCriteria();

        //Make poster path as must have criteria
        sc.setReleasedYear(2008);

        MovieTO movieTO = mDAO.getMovieById(829);
        if (movieTO != null) {


            //mDAO.insertTopMoviesPerGenre(sc, 0);
            System.out.println(movieTO.getMovieJsonTxt() + "\n" +
                    movieTO.getCastCrewTO().getJsonTxt());


            if (movieTO != null) {
                CastCrewTO castCrewTO = movieTO.getCastCrewTO();
                List<CastTO> castList = castCrewTO.getCastList();
                if (castList != null) {
                    for (CastTO castTO : castList) {
                        System.out.println(castTO.getName() + " " + castTO.getCharacter() + " " + castTO.getOrder());
                    } //EOF for
                }

            } //EOF if
        } //movieTO!=null


    } //main


}

package oracle.demo.oow.bd.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.ArrayList;

import java.util.Iterator;

import java.util.List;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.constant.KeyConstant;
import oracle.demo.oow.bd.to.GenreTO;
import oracle.demo.oow.bd.to.MovieTO;

import oracle.demo.oow.bd.pojo.SearchCriteria;
import oracle.demo.oow.bd.to.GenreMovieTO;
import oracle.demo.oow.bd.util.KeyUtil;
import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.Direction;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;
import oracle.kv.avro.JsonAvroBinding;
import oracle.kv.avro.JsonRecord;

import org.apache.avro.Schema;

import org.codehaus.jackson.node.ObjectNode;

public class GenreDAO extends BaseDAO {

    private static final int TOP = 50;


    /** Variables for JSONAvroBinding ***/
    private Schema genreSchema = null;
    private JsonAvroBinding genreBinding = null;

    public GenreDAO() {
        super();
        genreSchema = parser.getTypes().get("oracle.avro.Genre");        
        genreBinding = catalog.getJsonBinding(genreSchema);
    }

    /**
     * This method insert movie to its different genre group, so that one can
     * query movies by genres. It also assign genres into the genre group so that
     * one can query all the unique genres that exist in the store at a point in
     * time.
     * @param movieTO
     */
    public void insertMovieGenres(MovieTO movieTO) {

        GenreTO genreTO = null;
        int genreId;
        int movieId;
        String genreName = null;        
        Value value = null;
        Version version = null;

        ArrayList<GenreTO> genreList = null;
        if (movieTO != null) {
            genreList = movieTO.getGenres();
            Iterator iter = genreList.iterator();
            movieId = movieTO.getId();

            while (iter.hasNext()) {
                genreTO = (GenreTO)iter.next();
                genreId = genreTO.getId();
                genreName = genreTO.getName();

                /**
                 * Insert Genre into the /GN table
                 */
                Key key = KeyUtil.getGenreNameKey(genreName);
                // serialize genreTO to byte array using JSONAvroBinding
                value = this.toValue(genreTO);
                //Insert genreTO into the store
                version = getKVStore().put(key, value);

                /**
                 * Map movie to its genre using key=/GN_MV/genreId-/movieId
                 */
                key = KeyUtil.getGenreMovieKey(genreId, movieId);
                value = Value.createValue(Constant.EMPTY_PACKET.getBytes());
                getKVStore().put(key, value);

            } //EOF while
        } //EOF if
    } //insertMovieGenres

    /**
     * This method returns all different Genres available in the store
     * @return List of GenreTO
     */
    public List<GenreTO> getGenres() {

        KeyValueVersion keyValue = null;
        Value genreTOValue = null;        
        GenreTO genreTO = null;

        List<GenreTO> genreList = new ArrayList<GenreTO>();
        Key key = Key.createKey(KeyConstant.GENRE_TABLE);
        Iterator<KeyValueVersion> keyValueIter = getKVStore().storeIterator(Direction.UNORDERED, 0, key, null, null);

        while (keyValueIter.hasNext()) {
            keyValue = keyValueIter.next();
            genreTOValue = keyValue.getValue();
            
            genreTO = this.getGenreTO(genreTOValue);
            //Add TO to the list
            genreList.add(genreTO);

            //System.out.println(genreTO.getId() + " " + genreTO.getGenreJsonTxt());
        } //EOF while

        return genreList;
    }

    /**
     * This method returns all the movieIds by genreIds
     * @return List of GenreMovieTO
     */
    public List<GenreMovieTO> getGenreMovies() {

        KeyValueVersion keyValue = null;
        Value value = null;
        String movieIdStr = null;
        String genreIdStr = null;
        GenreMovieTO genreMovieTO = null;

        List<GenreMovieTO> genreMovieList = new ArrayList<GenreMovieTO>();
        Key key = Key.createKey(KeyConstant.GENRE_MOVIE_TABLE);
        Iterator<KeyValueVersion> keyValueIter = getKVStore().storeIterator(Direction.UNORDERED, 0, key, null, null);

        while (keyValueIter.hasNext()) {
            keyValue = keyValueIter.next();
            value = keyValue.getValue();
            movieIdStr = keyValue.getKey().getMinorPath().get(0);
            genreIdStr = keyValue.getKey().getMajorPath().get(1);

            genreMovieTO = new GenreMovieTO();
            genreMovieTO.setMovieId(Integer.parseInt(movieIdStr));
            genreMovieTO.setGenreId(Integer.parseInt(genreIdStr));

            //Add TO to the list
            genreMovieList.add(genreMovieTO);

            //System.out.println(genreTO.getId() + " " + genreTO.getGenreJsonTxt());
        } //EOF while

        return genreMovieList;
    }

    /**
     * This method returns GenreTO for the genreName
     * @param genreName
     * @return GenreTO
     */
    public GenreTO getGenreByName(String genreName) {

        GenreTO genreTO = null;
        Value genreTOValue = null;

        if (StringUtil.isNotEmpty(genreName)) {
            Key key = KeyUtil.getGenreNameKey(genreName);
            ValueVersion vv = getKVStore().get(key);
            if (vv != null) {
                genreTOValue = vv.getValue();
                genreTO = this.getGenreTO(genreTOValue);

            } //if (key != null)
        }
        return genreTO;

    }

    /**
     * This method insert movie to its different genre group, so that one can
     * query movies by genres. It also assign genres into the genre group so that
     * one can query all the unique genres that exist in the store at a point in
     * time.
     * @param movieTO
     */
    public void insertMovieGenresRDBMS(MovieTO movieTO) {

        GenreTO genreTO = null;
        Connection conn = null;
        int genreId;
        int movieId;
        String genreName = null;
        String genreJson = null;

        ArrayList<GenreTO> genreList = null;
        if (movieTO != null) {
            genreList = movieTO.getGenres();
            Iterator iter = genreList.iterator();
            movieId = movieTO.getId();
            conn = super.getOraConnect();

            while (iter.hasNext()) {
                genreTO = (GenreTO)iter.next();
                genreId = genreTO.getId();
                genreName = genreTO.getName();
                genreJson = genreTO.getGenreJson().toString();

                /**
                 * Insert Genre into the genre table
                 */
                try {
                    String update = "INSERT INTO MOVIESITE.GENRE (" + "GENRE_ID, NAME) " + "VALUES (?, ?)";

                    PreparedStatement stmt = conn.prepareStatement(update);
                    stmt.setInt(1, genreId);
                    stmt.setString(2, genreName);

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
                    System.out.println(e.getErrorCode() + ":" + e.getMessage());
                }

                /**
                 * Map movie to its genre
                 */
                try {
                    String update = "INSERT INTO MOVIESITE.MOVIE_GENRE (" + "MOVIE_ID, GENRE_ID) " + "VALUES (?, ?)";

                    PreparedStatement stmt = conn.prepareStatement(update);
                    stmt.setInt(1, movieId);
                    stmt.setInt(2, genreId);

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
                    // Ignore duplicates
                    if (e.getErrorCode() != 1)
                        System.out.println(e.getErrorCode() + ":" + e.getMessage());
                }


            } //EOF while
        } //EOF if
    } //insertMovieGenres

    public List<MovieTO> getMoviesByGenre(int genreId) {
        return getMoviesByGenre(genreId, TOP, null);
    }

    /**
     * Overloaded method that returns top N movies for the genreID
     * @param genreId
     * @return
     */
    public List<MovieTO> getMoviesByGenre(int genreId, SearchCriteria sc) {
        return getMoviesByGenre(genreId, TOP, sc);
    }

    /**
     * This is a overloaded method that takes genreName as input and returns
     * list of movies in that genre
     * @param genreName
     * @return List of MovieTO
     */
    public List<MovieTO> getMoviesByGenre(String genreName, SearchCriteria sc) {
        GenreTO genreTO = this.getGenreByName(genreName);
        int genreId = -1;

        if (genreTO != null) {
            genreId = genreTO.getId();
        }
        return getMoviesByGenre(genreId, TOP, sc);
    }

    public List<MovieTO> getMoviesByGenre(int genreId, int movieCount) {
        return getMoviesByGenre(genreId, movieCount, null);
    }

    /**
     * This method takes searchCriteriaTO as an additional input to return
     * movies=movieCount that belong to genre=genreId
     * @param genreId
     * @param movieCount
     * @param sc
     * @return List of MovieTO
     */
    public List<MovieTO> getMoviesByGenre(int genreId, int movieCount, SearchCriteria sc) {

        int count = 0;
        KeyValueVersion keyValue = null;
        Key key = KeyUtil.getGenreMovieKey(genreId, 0);
        int movieId = 0;
        MovieDAO movieDAO = new MovieDAO();
        MovieTO movieTO = null;
        List<MovieTO> movieTOList = new ArrayList<MovieTO>();

        Iterator<KeyValueVersion> keyIter =
            getKVStore().multiGetIterator(Direction.FORWARD, 0, /*key*/key, null, null);

        //Read top 100 movies only
        while (keyIter.hasNext() && count < movieCount) {
            keyValue = keyIter.next();
            key = keyValue.getKey();
            //get the movieId from the minor key
            movieId = Integer.parseInt(key.getMinorPath().get(0));
            movieTO = movieDAO.getMovieById(movieId);

            //SearchCriteriaTO so only those movies get added
            //to the list that setify the criteria
            if (assertSearchCriteria(sc, movieTO)) {
                //add the movie to the list
                movieTOList.add(movieTO);
                count++;
            }

            //System.out.println(movieTO.getTitle());
        } //EOF while

        //TODO - sort the collection depending on popularity
        //Collections.sort(movieTOList);

        return movieTOList;

    }

    private boolean assertSearchCriteria(SearchCriteria sc, MovieTO movieTO) {
        boolean flag = true;
        if (sc != null && movieTO != null) {
            if (sc.isPosterPath() && StringUtil.isEmpty(movieTO.getPosterPath())) {
                flag = false;
            } else if (movieTO.getReleasedYear() < sc.getReleasedYear()) {
                flag = false;
            } else if (movieTO.getPopularity() < sc.getPopularity()) {
                flag = false;
            } else if (movieTO.getPopularity() < sc.getPopularity()) {
                flag = false;
            } else if (movieTO.getVoteCount() < sc.getVoteCount()) {
                flag = false;
            }
        } //EOF if

        return flag;
    }

    /**
     * This method takes CustomerTO object and serialize it to Value object.
     * @param genreTO
     * @return Value
     */
    public Value toValue(GenreTO genreTO) {
        ObjectNode genreNode = null;
        JsonRecord jsonRecord = null;
        Value value = null;

        if (genreTO != null) {
            genreNode = genreTO.getGenreJson();
            jsonRecord = new JsonRecord(genreNode, genreSchema);
            // serialize CustomerTO to byte array using JSONAvroBinding
            value = genreBinding.toValue(jsonRecord);
        }

        return value;

    } //toValue

    /**
     * This method takes Value object as input and deserialize it into GenreTO. The assumption here is that
     * Value that is passed as argument is serialized GenreTO JSON object.
     * @param genreTOValue
     * @return GenreTO
     */
    public GenreTO getGenreTO(Value genreTOValue) {

        GenreTO genreTO = null;
        ObjectNode genreNode = null;
        JsonRecord jsonRecord = null;

        if (genreTOValue != null) {
            jsonRecord = genreBinding.toObject(genreTOValue);
            genreNode = (ObjectNode)jsonRecord.getJsonNode();
            genreTO = new GenreTO(genreNode);
        }

        return genreTO;

    } //getGenreTO

    public static void main(String[] args) {
        GenreDAO genreDAO = new GenreDAO();

        //Show all the Genres
        List<GenreTO> genreList = genreDAO.getGenres();


        //Show all the genres

        for (GenreTO genreTO : genreList) {
            System.out.println(genreTO.toString());
        }

        GenreTO genreTO = genreDAO.getGenreByName("Chinatown");
        if (genreTO != null)
            System.out.println(genreTO.toString());

        /*
        List<GenreMovieTO> genreMovieList = genreDAO.getGenreMovies();

        //Show movies by genres
        for (GenreMovieTO genreMovieTO : genreMovieList) {
            System.out.println(genreMovieTO.toString());
        }
        */

    } //main

}

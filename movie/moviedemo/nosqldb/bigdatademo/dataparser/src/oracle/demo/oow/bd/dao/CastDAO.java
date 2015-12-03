package oracle.demo.oow.bd.dao;


import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;

import java.util.List;

import oracle.demo.oow.bd.to.CastCrewTO;
import oracle.demo.oow.bd.to.CastMovieTO;
import oracle.demo.oow.bd.to.CastTO;

import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.util.KeyUtil;

import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;

import oracle.kv.avro.JsonAvroBinding;

import oracle.kv.avro.JsonRecord;

import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

import org.apache.avro.Schema;

import org.codehaus.jackson.node.ObjectNode;

public class CastDAO extends BaseDAO {

    private static Table castTable = null;
    private static Table castCrew = null;
    private static Table movieTable = null;
    private final static String TABLE_NAME="CAST";
    
    
 

    public CastDAO() {
        super();  
        castTable = getKVStore().getTableAPI().getTable(TABLE_NAME);
        movieTable = getKVStore().getTableAPI().getTable(MovieDAO.TABLE_NAME);
        castCrew = getKVStore().getTableAPI().getTable(MovieDAO.TABLE_NAME+"."+MovieDAO.CHILD_TABLE);
        
        
    }

    /**
     * This method inserts Cast information and also index cast ID to the movieId
     * so that one can run queries on castId to fetch all the movies in which
     * a particular cast worked.
     * @param castTO - This is a cast transfer object
     * @return - true if insertion is successful
     */
    public boolean insertCastInfo(CastTO castTO) {
        boolean flag = false;
        int castId;
        Value value = null;
        Version version = null;

        if (castTO != null) {

            //System.out.println("CT/ " + jsonTxt);
            castId = castTO.getId();

            /**
             * Insert cast json string to the castId
             * Key=/CT/castId/-/info
             */
            PrimaryKey key = KeyUtil.getCastInfoKey(castId,castTable);
            // serialize castTO to byte array using JSONAvroBinding
            
            //Insert castTO into the store
            Row row = castTable.createRowFromJson(castTO.toJsonString(),true);
            
            version = getKVStore().getTableAPI().put(row, null,null);

            /**
             * Create a mapping between movieId & castId, so that one can find
             * the casts for a movie.
             * Key=/MVCC/movieId/-/cc
             */
            insertCast4Movies(castTO);

            //Flip the flag to true for success
            flag = true;

        }
        return flag;
    } //insertCastInfo

    /**
     * This method inserts Cast information and also index cast ID to the movieId
     * so that one can run queries on castId to fetch all the movies in which
     * a particular cast worked.
     * @param castTO - This is a cast transfer object
     * @return - true if insertion is successful
     */
    public boolean insertCastInfoRDBMS(CastTO castTO) {
        boolean flag = false;
        Connection conn = null;

        int castId;

        if (castTO != null) {

            // Insert record into Oracle RDBMS
            try {
                conn = super.getOraConnect();
                // jsonTxt = castTO.getJsonTxt();
                //System.out.println("CT/ " + jsonTxt);
                castId = castTO.getId();

                /**
               * Insert cast id & name
               */
                String update = "INSERT INTO MOVIESITE.CAST (CAST_ID, NAME) " + "VALUES (?, ?)";

                PreparedStatement stmt = conn.prepareStatement(update);
                stmt.setInt(1, castId);
                stmt.setString(2, castTO.getName());

                try {
                    stmt.execute();
                } catch (SQLException e) {
                    // Expect many duplicates.  Duplicate error code = 1
                    if (e.getErrorCode() != 1)
                        System.out.println(e.getErrorCode() + ":" + e.getMessage());
                }

                stmt.close();

            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;

            }


            /**
             * Create a mapping between movieId & castId, so that one can find
             * the casts for a movie.
             */
            insertCast4MoviesRDBMS(castTO);

        }
        return flag;
    } //insertCastInfo

    /**
     * This method loops through all the movieId stored in the CastTO and create
     * association between movieId and castId and saves CastCrewTO JSON txt as the
     * value. Key=/MV/movieId/-/cc
     * @param castTO
     */
    public void insertCast4Movies(CastTO castTO) {
        List<CastMovieTO> castMovieList = null;
        CastCrewTO castCrewTO = null;

        PrimaryKey key = null;
        int movieId = 0;
        int castId = 0;
        String jsonTxt = null;
        int order = -1;
        String character = null;

        if (castTO != null & castTO.getCastMovieList() != null) {
            castMovieList = castTO.getCastMovieList();
            castId = castTO.getId();

            for (CastMovieTO castMovieTO : castMovieList) {
                movieId = castMovieTO.getId();
                order = castMovieTO.getOrder();
                character = castMovieTO.getCharacter();

                //Get Cast detail for the movie and set it into the CastTO
                castTO.setOrder(order);
                castTO.setCharacter(character);

                //make movie to cast association only if order < 15
                if (order > -1 && order < 15) {
                    //get the key
                    
                    key = KeyUtil.getMovieCastCrewKey(movieId,movieId,movieTable);

                    //check if CastCrewTO jsonTxt already exit
                    System.out.println(key.toJsonString(true));
                        
                    Row jrow = getKVStore().getTableAPI().get(key, null);
                    if(jrow!=null)
                    jsonTxt = jrow.toJsonString(true);

                    //System.out.println("1: " + jsonTxt);

                    if (StringUtil.isNotEmpty(jsonTxt)) {
                        castCrewTO = new CastCrewTO(jsonTxt.trim());

                    } else {
                        castCrewTO = new CastCrewTO();
                        castCrewTO.setMovieId(movieId);
                    }

                    //add CastTO into castCrewTO
                    castCrewTO.addCastTO(castTO);

                    //Convert castCrewTO into json txt
                    jsonTxt = castCrewTO.getJsonTxt();
                    //inset movie-cast association into the KV-Store
                    //System.out.println(jsonTxt);
                    if(StringUtil.isNotEmpty(jsonTxt)){
                    Row row = castCrew.createRowFromJson(jsonTxt, false);
                    
                    getKVStore().getTableAPI().put(row, null,null);
                    }


                } //if (order > -1 && order < 15) {
            } //EOF for
        } //if (castTO != null & castTO.getCastMovieList() != null)
    } //insertCast4Movies

    /**
     * This method loops through all the movieId stored in the CastTO and create
     * association between movieId and castId.
     * @param castTO
     */
    public void insertCast4MoviesRDBMS(CastTO castTO) {
        List<CastMovieTO> castMovieList = null;


        int movieId = 0;
        int castId = 0;
        int order = -1;
        String character = null;
        Connection conn = super.getOraConnect();

        if (castTO != null & castTO.getCastMovieList() != null) {
            castMovieList = castTO.getCastMovieList();
            castId = castTO.getId();

            for (CastMovieTO castMovieTO : castMovieList) {
                movieId = castMovieTO.getId();
                order = castMovieTO.getOrder();
                character = castMovieTO.getCharacter();

                // truncate character length to 100
                if (character.length() > 100)
                    character = character.substring(0, 96) + "...";

                //Get Cast detail for the movie and set it into the CastTO
                castTO.setOrder(order);
                castTO.setCharacter(character);

                //make movie to cast association only if order < 15
                if (order > -1 && order < 15) {

                    try {
                        String update =
                            "INSERT INTO MOVIESITE.MOVIE_CAST (MOVIE_ID, CAST_ID, CHARACTER) " + "VALUES (?, ?, ?)";

                        PreparedStatement stmt = conn.prepareStatement(update);
                        stmt.setInt(1, movieId);
                        stmt.setInt(2, castId);
                        stmt.setString(3, character);

                        try {
                            stmt.execute();
                        } catch (SQLException e) {
                            // Expect many duplicates.  Duplicate error code = 1
                            if (e.getErrorCode() != 1)
                                System.out.println(e.getErrorCode() + ":" + e.getMessage());
                        }

                        stmt.close();

                    } catch (SQLException e) {
                        // Ignore duplicates
                        if (e.getErrorCode() != 1)
                            System.out.println(e.getErrorCode() + ":" + e.getMessage());
                    }

                } //if (order > -1 && order < 15) {
            } //EOF for
        } //if (castTO != null & castTO.getCastMovieList() != null)
    } //insertCast4Movies


    /**
     * This method returns CastTO when castId is passed
     * @param castId uniue Cast Id
     * @return CastTO
     */
    public CastTO getCastById(int castId) {
        CastTO castTO = null;
        String castTOValue = null;

        if (castId > 0) {
            /**
         * Insert cast json string to the castId
         * Key=/CT/castId/-/info
         */
            PrimaryKey key = KeyUtil.getCastInfoKey(castId,castTable);
            Row vv = getKVStore().getTableAPI().get(key,null);
            if (vv != null) {
                castTOValue = vv.toJsonString(true);
                castTO = new CastTO(castTOValue);

            } //if (vv != null)

        } //if (castId > 0)
        return castTO;
    } //getCastById

    /**
     * This method returns a list of Casts for the movieId passed
     * Key= /MV/movieId/-/cc
     * @param movieId - Unique Id of the movie
     * @return List of CastTO
     */
    public List<CastTO> getMovieCasts(int movieId) {

        List<CastTO> castList = null;

        CastCrewTO castCrewTO = null;
        String jsonTxt = null;
        PrimaryKey key = null;

        if (movieId > -1) {
            key = KeyUtil.getMovieCastCrewKey(movieId,movieId,movieTable);

            Row row= getKVStore().getTableAPI().get(key,null);
            //System.out.println(jsonTxt);
            if(row!=null)
            jsonTxt = row.toJsonString(true);
            if (StringUtil.isNotEmpty(jsonTxt)) {
                castCrewTO = new CastCrewTO(jsonTxt.trim());
                //Create CastCrewTO
                castList = castCrewTO.getCastList();
                /**
                 * Sort the Movie Cast based on the order
                 */
                Collections.sort(castList);
            } //if(StringUtil.isNotEmpty(jsonTxt))

        } //if (movieId > -1)


        return castList;
    } //getMovieCasts

    /**
     * This method returns all the movies that Cast worked in.
     * @param castId
     * @return List of MovieTO
     */
    public List<MovieTO> getMoviesByCast(int castId) {
        List<CastMovieTO> castMovieList = null;
        List<MovieTO> movieList = new ArrayList<MovieTO>();
        int movieId = 0;
        CastTO castTO = null;
        MovieTO movieTO = null;
        String castTOValue = null;
        MovieDAO movieDAO = new MovieDAO();

        PrimaryKey key = KeyUtil.getCastInfoKey(castId,castTable);
        Row vv = getKVStore().getTableAPI().get(key,null);
        if (vv != null) {
            castTOValue = vv.toJsonString(true);
            castTO = new CastTO(castTOValue);
            if (castTO != null) {
                castMovieList = castTO.getCastMovieList();

                for (CastMovieTO castMovieTO : castMovieList) {
                    movieId = castMovieTO.getId();
                    movieTO = movieDAO.getMovieById(movieId);

                    if (movieTO != null) {
                        //add to movieList
                        movieList.add(movieTO);
                    } //if(movieTO!=null)
                } //for
            } //if(cast!=null)
        } //if (vv != null)


        return movieList;

    }

    /**
     * This method takes CastTO object and serialize it to Value object.
     * @param castTO
     * @return Value
     */
//    protected Value toValue(CastTO castTO) {
//        ObjectNode castNode = null;
//        JsonRecord jsonRecord = null;
//        Value value = null;
//
//        if (castTO != null) {
//            castNode = castTO.geCastJson();
//            jsonRecord = new JsonRecord(castNode, castSchema);
//            // serialize CastTO to byte array using JSONAvroBinding
//            value = castBinding.toValue(jsonRecord);
//        }
//
//        return value;
//
//    } //toValue

    /**
     * This method takes Value object as input and deserialize it into CastTO. The assumption here is that
     * Value that is passed as argument is serialized CastTO JSON object.
     * @param castTOValue
     * @return CastTO
     */
//    protected CastTO getCastTO(Value castTOValue) {
//
//        CastTO castTO = null;
//        ObjectNode castNode = null;
//        JsonRecord jsonRecord = null;
//
//        if (castTOValue != null) {
//            jsonRecord = castBinding.toObject(castTOValue);
//            castNode = (ObjectNode)jsonRecord.getJsonNode();
//            castTO = new CastTO(castNode);
//        }
//
//        return castTO;
//
//    } //getCastTO

    public static void main(String[] args) {
        CastDAO castDAO = new CastDAO();
        String castJsonTxt =
            "{\"id\":558298,\"name\":\"Tom Hanks\",\"movies\":[{\"id\":11287,\"character\":\"Jimmy Dugan\",\"order\":1},{\"id\":1033932,\"character\":\"Himself - Narrator\",\"order\":1},{\"id\":13448,\"character\":\"Robert Langdon\",\"order\":1},{\"id\":568,\"character\":\"Jim Lovell\",\"order\":1},{\"id\":22159,\"character\":\"Rick Gassko\",\"order\":1},{\"id\":1044590,\"character\":\"Narrator\",\"order\":1},{\"id\":2280,\"character\":\"Josh Baskin\",\"order\":1},{\"id\":1046472,\"character\":\"Himself/Sherman McCoy/Jimmy Dugan/Forrest Gump/Jim Lovell/Paul Edgecomb/Chuck Noland\",\"order\":1},{\"id\":920,\"character\":\"Woody Car\",\"order\":41},{\"id\":8358,\"character\":\"Chuck Noland\",\"order\":8},{\"id\":34745,\"character\":\"Carl Hanratty\",\"order\":2},{\"id\":6538,\"character\":\"Charlie Wilson\",\"order\":1},{\"id\":56591,\"character\":\"Himself\",\"order\":1},{\"id\":43334,\"character\":\"Det. Pep Streebek\",\"order\":2},{\"id\":37641,\"character\":\"Mailbox Elvis\",\"order\":11},{\"id\":35866,\"character\":\"David Bradley\",\"order\":1},{\"id\":64685,\"character\":\"Thomas Schell\",\"order\":1},{\"id\":13,\"character\":\"Forrest Gump\",\"order\":1},{\"id\":1075538,\"character\":\"Himself\",\"order\":1},{\"id\":1078658,\"character\":\"Woody\",\"order\":1},{\"id\":65262,\"character\":\"Elliot\",\"order\":8},{\"id\":2565,\"character\":\"Joe\",\"order\":1},{\"id\":59861,\"character\":\"Larry Crowne\",\"order\":1},{\"id\":87061,\"character\":\"Himself\",\"order\":1},{\"id\":1105649,\"character\":\"Narrator\",\"order\":1},{\"id\":29968,\"character\":\"David Basner\",\"order\":1},{\"id\":9800,\"character\":\"Andrew Beckett\",\"order\":1},{\"id\":40820,\"character\":\"Steven Gold\",\"order\":2},{\"id\":20763,\"character\":\"Older Mike\",\"order\":1},{\"id\":1128170,\"character\":\"Himself\",\"order\":1},{\"id\":56235,\"character\":\"Narrator\",\"order\":1},{\"id\":4147,\"character\":\"Michael Sullivan\",\"order\":1},{\"id\":857,\"character\":\"Capt. John H. Miller\",\"order\":1},{\"id\":858,\"character\":\"Sam Baldwin\",\"order\":1},{\"id\":1138956,\"character\":\"Woody\",\"order\":1},{\"id\":2619,\"character\":\"Allen Bauer\",\"order\":1},{\"id\":9591,\"character\":\"Mr. White\",\"order\":6},{\"id\":11974,\"character\":\"Ray Peterson\",\"order\":1},{\"id\":9586,\"character\":\"Sherman McCoy\",\"order\":1},{\"id\":32562,\"character\":\"Himself\",\"order\":23},{\"id\":591,\"character\":\"Robert Langdon\",\"order\":1},{\"id\":16279,\"character\":\"Mr. Gable\",\"order\":6},{\"id\":497,\"character\":\"Paul Edgecomb\",\"order\":1},{\"id\":5516,\"character\":\"Professor G.H. Dorr\",\"order\":1},{\"id\":10905,\"character\":\"Richard Harlan Drew\",\"order\":1},{\"id\":10466,\"character\":\"Walter Fielding, Jr.\",\"order\":1},{\"id\":15302,\"character\":\"Himself\",\"order\":32},{\"id\":5255,\"character\":\"Hero Boy/Father/Conductor/Hobo/Scrooge/Santa Claus\",\"order\":1},{\"id\":1165,\"character\":\"Himself\",\"order\":1},{\"id\":1155082,\"character\":\"Narrator\",\"order\":1},{\"id\":1155358,\"character\":\"Himself\",\"order\":1},{\"id\":35,\"character\":\"Himself\",\"order\":18},{\"id\":594,\"character\":\"Viktor Navorski\",\"order\":1},{\"id\":1160021,\"character\":\"Himself\",\"order\":1},{\"id\":862,\"character\":\"Woody\",\"order\":1},{\"id\":863,\"character\":\"Woody\",\"order\":1},{\"id\":10193,\"character\":\"Woody\",\"order\":1},{\"id\":6951,\"character\":\"Det. Scott Turner\",\"order\":1},{\"id\":19259,\"character\":\"Lawrence Whatley Bourne III\",\"order\":1},{\"id\":13508,\"character\":\"Himself\",\"order\":1},{\"id\":1168880,\"character\":\"Himself\",\"order\":1},{\"id\":9489,\"character\":\"Joe Fox\",\"order\":1},{\"id\":1000249,\"character\":\"Himself - Crowd Member\",\"order\":1},{\"id\":1002407,\"character\":\"British officer\",\"order\":91},{\"id\":1004337,\"character\":\"Himself\",\"order\":8},{\"id\":1011368,\"character\":\"\",\"order\":1},{\"id\":1016788,\"character\":\"Himself\",\"order\":1}]}";
        CastTO castTO = new CastTO(castJsonTxt);
        //insert castTO into the store
        castDAO.insertCastInfo(castTO);
        castTO = castDAO.getCastById(558298);

        System.out.println("Cast by Id: " + castTO.getJsonTxt());


        System.out.println("Printing all the movies that belongs to castId");

        List<MovieTO> movieList = castDAO.getMoviesByCast(558298);
        for (MovieTO movieTO : movieList) {
            System.out.println("\t" + movieTO.getMovieJsonTxt());
        }

        System.out.println("Printing all the casts in movieId=857");

        List<CastTO> castList = castDAO.getMovieCasts(857);
        for (CastTO cTO : castList) {
            System.out.println("\t" + cTO.getName() + " " + cTO.getJsonTxt());
        }

    } //main

}

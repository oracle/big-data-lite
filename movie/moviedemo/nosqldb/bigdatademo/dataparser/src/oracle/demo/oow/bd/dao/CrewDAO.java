package oracle.demo.oow.bd.dao;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import oracle.demo.oow.bd.to.CastCrewTO;

import oracle.demo.oow.bd.to.CrewTO;
import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.util.KeyUtil;

import oracle.demo.oow.bd.util.StringUtil;



import oracle.kv.avro.JsonAvroBinding;

import oracle.kv.avro.JsonRecord;

import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

import org.apache.avro.Schema;

import org.codehaus.jackson.node.ObjectNode;

public class CrewDAO extends BaseDAO {

    private static Table crewTable = null;
    private static Table movieTable = null;
    private final static String TABLE_NAME="CREW";

    /** Variables for JSONAvroBinding ***/
    private Schema crewSchema = null;
    private JsonAvroBinding crewBinding = null;

    public CrewDAO() {
        super();
       crewTable = getKVStore().getTableAPI().getTable(TABLE_NAME);
        movieTable = getKVStore().getTableAPI().getTable(MovieDAO.TABLE_NAME);
    }

    /**
     * This method inserts Crew information and also index crew ID to the movieId
     * so that one can run queries on castId to fetch all the movies in which
     * a particular cast worked.
     * @param crewTO - This is a cast transfer object
     * @return - true if insertion is successful
     */
    public boolean insertCrewInfo(CrewTO crewTO) {
        boolean flag = false;
        String jsonTxt = null;
        int crewId;
        String value = null;
        

        if (crewTO != null) {

            jsonTxt = crewTO.getJsonTxt();
            crewId = crewTO.getId();

            /**
             * Insert cast json string to the castId
             * Key=/CW/crewId/-/info
             */
            
            // serialize crewTO to byte array using JSONAvroBinding
            
            Row row = crewTable.createRowFromJson(crewTO.toJsonString(),true);
            //Insert crewTO into the store
            getKVStore().getTableAPI().put(row, null,null);

            /**
             * Create a mapping between movieId & castId, so that one can find
             * the casts for a movie.
             * Key= /MVCC/movieId/-/cc
             */
            insertCrew4Movies(crewTO);

        }
        return flag;
    } //insertCastInfo

    /**
     * This method inserts Crew information and also index crew ID to the movieId
     * so that one can run queries on castId to fetch all the movies in which
     * a particular cast worked.
     * @param crewTO - This is a cast transfer object
     * @return - true if insertion is successful
     */
    public boolean insertCrewInfoRDBMS(CrewTO crewTO) {
        boolean flag = false;
        Connection conn = null;

        int crewId;

        if (crewTO != null) {

            try {
                conn = super.getOraConnect();

                crewId = crewTO.getId();

                /**
             * Insert cast id & name
             */
                String update = "INSERT INTO MOVIESITE.CREW (CREW_ID, NAME) " + "VALUES (?, ?)";

                PreparedStatement stmt = conn.prepareStatement(update);
                stmt.setInt(1, crewId);
                stmt.setString(2, crewTO.getName());

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
            insertCrew4MoviesRDBMS(crewTO);

        }
        return flag;
    } //insertCrewInfoRDBMS

    /**
     * This method loops through all the movieId stored in the CastTO and create
     * association between movieId and castId and saves CastTO's JSON txt as the
     * value. Key= /MV_CW/movieId/-/crewId
     * @param crewTO
     */
    public void insertCrew4Movies(CrewTO crewTO) {
        List<String> movieList = null;
        CastCrewTO castCrewTO = null;
        PrimaryKey key = null;
        int movieId = 0;
        int crewId = 0;
        String jsonTxt = null;

        if (crewTO != null & crewTO.getMovieList() != null) {

            movieList = crewTO.getMovieList();
            crewId = crewTO.getId();

            //We don't need cast movie list so make it null
            crewTO.setMovieList(null);

            for (String movieIdStr : movieList) {
                movieId = Integer.parseInt(movieIdStr);

                //get the key
                Table castCrew = movieTable.getChildTable(MovieDAO.CHILD_TABLE);
                key = KeyUtil.getMovieCastCrewKey(movieId,movieId,movieTable);

                //check if CastCrewTO jsonTxt already exit
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

                //System.out.println("CastCrewInfo: " + castCrewTO.getJsonTxt());
                //add CastTO into castCrewTO
                castCrewTO.addCrewTO(crewTO);

                //Convert castCrewTO into json txt
                jsonTxt = castCrewTO.getJsonTxt();

                System.out.println("JSON = " +jsonTxt);

                //inset movie-cast association into the KV-Store
                Row row = castCrew.createRowFromJson(jsonTxt, false);
                
                System.out.println("ROW = " + row.toJsonString(true));
                getKVStore().getTableAPI().put(row, null,null);

            } //if (order > -1 && order < 15) {
        } //EOF if (crewTO != null & crewTO.getMovieList() != null)

    } //insertCrew4Movies

    /**
     * This method loops through all the movieId stored in the CastTO and create
     * association between movieId and castId and saves CastTO's JSON txt as the
     * value. Key= /MV_CW/movieId/-/crewId
     * @param crewTO
     */
    public void insertCrew4MoviesRDBMS(CrewTO crewTO) {
        List<String> movieList = null;

        int movieId = 0;
        int crewId = 0;
        String job = null;
        Connection conn = super.getOraConnect();


        if (crewTO != null & crewTO.getMovieList() != null) {

            movieList = crewTO.getMovieList();
            crewId = crewTO.getId();

            //We don't need cast movie list so make it null
            crewTO.setMovieList(null);

            for (String movieIdStr : movieList) {
                movieId = Integer.parseInt(movieIdStr);
                job = crewTO.getJob();

                try {
                    String update = "INSERT INTO MOVIESITE.MOVIE_CREW (MOVIE_ID, CREW_ID, JOB) " + "VALUES (?, ?, ?)";

                    PreparedStatement stmt = conn.prepareStatement(update);
                    stmt.setInt(1, movieId);
                    stmt.setInt(2, crewId);
                    stmt.setString(3, job);

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
        } //EOF if (crewTO != null & crewTO.getMovieList() != null)

    } //insertCrew4MoviesRDBMS


    /**
     * This method returns CrewTO when crewId is passed
     * @param crewId uniue Cast Id
     * @return CrewTO
     */
    public CrewTO getCrewById(int crewId) {
        CrewTO crewTO = null;
       
        String crewTOValue = null;

        if (crewId > 0) {

            /**
         * Insert cast json string to the crewId
         * Key=/CW/crewId/-/info
         */
            PrimaryKey key = KeyUtil.getCrewInfoKey(crewId,crewTable);
            Row vv = getKVStore().getTableAPI().get(key,null);

            if (vv != null) {
                crewTOValue = vv.toJsonString(true);
                crewTO = new CrewTO(crewTOValue);
            } //if(StringUtil.isNotEmpty(jsonTxt))
        } //if (castId > 0)
        return crewTO;
    } //getCastById

    /**
     * This method returns a list of Casts for the movieId passed
     * Key= /MV_CW/movieId/-/crewId
     * @param movieId - Unique Id of the movie
     * @return List of CrewTO
     */
    public List<CrewTO> getMovieCrews(int movieId) {

        List<CrewTO> crewList = null;

        CastCrewTO castCrewTO = null;
        String jsonTxt = null;
        PrimaryKey key = null;

        if (movieId > -1) {
            key = KeyUtil.getMovieCastCrewKey(movieId,movieId,movieTable);

            Row row = getTableAPI().get(key, null);
            //System.out.println(jsonTxt);
            if(row!=null)
            jsonTxt  = row.toJsonString(true);
            if (StringUtil.isNotEmpty(jsonTxt)) {
                castCrewTO = new CastCrewTO(jsonTxt.trim());
                //Create CastCrewTO
                crewList = castCrewTO.getCrewList();
            } //if(StringUtil.isNotEmpty(jsonTxt))

        } //if (movieId > -1)


        return crewList;
    } //getMovieCrews

    /**
     * This method returns all the movies that Crew worked in.
     * @param crewId
     * @return List of MovieTO
     */
    public List<MovieTO> getMoviesByCrew(int crewId) {
        List<String> movieIdList = null;
        List<MovieTO> movieList = new ArrayList<MovieTO>();
        CrewTO crewTO = null;
        MovieTO movieTO = null;
        MovieDAO movieDAO = new MovieDAO();

        PrimaryKey key = KeyUtil.getCrewInfoKey(crewId,crewTable);
        Row vv = super.getKVStore().getTableAPI().get(key,null);
        String value = null;
        
        //System.out.println("--> : " + jsonTxt);
        if (vv != null) {
            value = vv.toJsonString(true);
            
            crewTO = new CrewTO(value);
            movieIdList = crewTO.getMovieList();

            for (String movieIdStr : movieIdList) {
                movieTO = movieDAO.getMovieById(movieIdStr);
                if (movieTO != null) {
                    //add to movieList
                    movieList.add(movieTO);
                } //if(movieTO!=null)
            } //for
        } //EOF if
        return movieList;

    } //getMoviesByCrew

   

    public static void main(String[] args) {
        CrewDAO crewDAO = new CrewDAO();
        String jsonTxt =
            "{\"id\":224040,\"name\":\"Steven Spielberg\",\"job\":\"Director\",\"movies\":[{\"id\":11519},{\"id\":1033932},{\"id\":11352},{\"id\":97105},{\"id\":11831},{\"id\":1040226},{\"id\":34745},{\"id\":840},{\"id\":1064297},{\"id\":10110},{\"id\":1067965},{\"id\":1070353},{\"id\":38621},{\"id\":879},{\"id\":217},{\"id\":89},{\"id\":87},{\"id\":578},{\"id\":329},{\"id\":180},{\"id\":612},{\"id\":1128278},{\"id\":857},{\"id\":424},{\"id\":100929},{\"id\":17578},{\"id\":873},{\"id\":1151948},{\"id\":330},{\"id\":5121},{\"id\":594},{\"id\":1156994},{\"id\":15301},{\"id\":57212},{\"id\":74},{\"id\":1023111}]}";
        CrewTO crewTO = new CrewTO(jsonTxt.trim());

        //insert crewTO into the store
        crewDAO.insertCrewInfo(crewTO);

        crewTO = crewDAO.getCrewById(224040);
        System.out.println(crewTO.getJsonTxt());

        System.out.println("Printing all the casts in movieId=857");
        List<CrewTO> crewList = crewDAO.getMovieCrews(857);
        for (CrewTO cwTO : crewList) {
            System.out.println("\t" + cwTO.getName() + " " + cwTO.getJsonTxt());
        }

        //Find all the movies by director Steven Spielberg
        List<MovieTO> movieList = crewDAO.getMoviesByCrew(224040);
        for (MovieTO movieTO : movieList) {
            System.out.println(movieTO.getMovieJsonTxt());
        }

    crewDAO.insertCrew4Movies(crewTO);

    } //main

}//CrewDAO

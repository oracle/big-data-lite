package oracle.demo.oow.bd.dao;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import oracle.demo.oow.bd.constant.KeyConstant;
import oracle.demo.oow.bd.to.CastTO;
import oracle.demo.oow.bd.to.CastCrewTO;

import oracle.demo.oow.bd.to.CrewTO;

import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

public class CastCrewDAO extends BaseDAO {
    public CastCrewDAO() {
        super();
    }

    /**
     * This method inserts Cast information and also index cast ID to the movieId
     * so that one can run queries on castId to fetch all the movies in which
     * a particular cast worked.
     * @param castTO - This is a cast transfer object
     * @return - true if insertion is successful
     */
    public boolean insertMovieCast(CastCrewTO castTO) {
        boolean flag = false;
        String jsonTxt = null;

        int movieId;

        if (castTO != null) {

            jsonTxt = castTO.getJsonTxt();
            movieId = castTO.getMovieId();

            /**
             * Insert cast json string to the movieId
             * Key=/MV/movieId/-/cJson
             */
            Key key = this.getMovieCastKey(movieId);
            Value value = Value.createValue(jsonTxt.getBytes());
            getKVStore().put(key, value);

            /**
             * Create a mapping between actorId/crewId and movieId so that one
             * can later search all the movies a actor/crew might have worked in.
             */

            //genreDAO.insertMovieGenres(movieTO);

        }
        return flag;
    } //insertMovieCast

    public void insertMovieCastCrewRDBMS(CastCrewTO castCrewTO) {

        int id = 0;
        int movieId = 0;
        String name = null;
        String character = null;
        String job = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        String update = null;

        if (castCrewTO != null) {
            List<CastTO> castList = castCrewTO.getCastList();
            List<CrewTO> crewList = castCrewTO.getCrewList();
            movieId = castCrewTO.getMovieId();
            conn = super.getOraConnect();
            try {
                /**
             * This loop will insert CAST information in the CAST_CREW table
             * and will create an entry in the intersection table MOVIE_CAST
             */
                for (CastTO castTO : castList) {
                    id = castTO.getId();
                    name = castTO.getName();
                    //character = castTO.getCharacter();

                    /**
                 * Insert cast info CAST_CREW table
                 */

                    update =
                            "INSERT INTO CAST_CREW (ID, NAME)  VALUES (?, ?)";
                    try {
                        stmt = conn.prepareStatement(update);
                        stmt.setInt(1, id);
                        stmt.setString(2, name);

                        stmt.execute();

                    } catch (SQLException e) {
                        // Expect many duplicates.  Duplicate error code = 1
                        if (e.getErrorCode() != 1)
                            System.out.println(e.getErrorCode() + ":" +
                                               e.getMessage());
                        
                    } finally {
                        stmt.close();
                    }


                    /**
                 * Map MOVIE to its CAST
                 */

                    update =
                            "INSERT INTO MOVIE_CAST (MOVIE_ID, CAST_ID, CHARACTER) " +
                            "VALUES (?, ?, ?)";
                    try {
                        stmt = conn.prepareStatement(update);
                        stmt.setInt(1, movieId);
                        stmt.setInt(2, id);
                        stmt.setString(3, character);

                        stmt.execute();
                    } catch (SQLException e) {
                        // Expect many duplicates.  Duplicate error code = 1
                        if (e.getErrorCode() != 1)
                            System.out.println(e.getErrorCode() + ":" +
                                               e.getMessage());
                        
                    } finally {
                        stmt.close();
                    }


                } //for(CastTO castTO: castList)

                /**
                 * This loop will insert CREW information into CAST_CREW table first
                 * then will add an entry in the intersection table MOVIE_CREW
                 */
                for (CrewTO crewTO : crewList) {
                    id = crewTO.getId();
                    name = crewTO.getName();
                    job = crewTO.getJob();

                    /**
                     * Insert cast info CAST_CREW table
                    */

                    update =
                            "INSERT INTO CAST_CREW (ID, NAME)  VALUES (?, ?)";
                    try {
                        stmt = conn.prepareStatement(update);
                        stmt.setInt(1, id);
                        stmt.setString(2, name);

                        stmt.execute();
                    } catch (SQLException e) {
                        // Expect many duplicates.  Duplicate error code = 1
                        if (e.getErrorCode() != 1)
                            System.out.println(e.getErrorCode() + ":" +
                                               e.getMessage());
                        
                    } finally {
                        stmt.close();
                    }
                    //conn.commit();


                    /**
                 * Map MOVIE to its CREW
                 */

                    update =
                            "INSERT INTO MOVIE_CREW (MOVIE_ID, CREW_ID, JOB) " +
                            "VALUES (?, ?, ?)";
                    try {
                        stmt = conn.prepareStatement(update);
                        stmt.setInt(1, movieId);
                        stmt.setInt(2, id);
                        stmt.setString(3, job);

                        stmt.execute();                        

                    } catch (SQLException e) {
                        // Expect many duplicates.  Duplicate error code = 1
                        if (e.getErrorCode() != 1)
                            System.out.println(e.getErrorCode() + ":" +
                                               e.getMessage());
                        
                    } finally {
                        stmt.close();
                    }


                } //for (CrewTO crewTO : crewList)
            } catch (SQLException e) {
                // Ignore duplicates
                if (e.getErrorCode() != 1)
                    System.out.println(e.getErrorCode() + ":" +
                                       e.getMessage());
            } finally {
                try {
                    conn.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } //if (castCrewTO != null)


    } //insertMovieCastCrewRDBMS

    public CastCrewTO getMovieCast(int movieId) {
        CastCrewTO castTO = null;
        if (movieId > -1) {
            Key key = this.getMovieCastKey(movieId);
            ValueVersion valueVersion = super.getKVStore().get(key);
            Value value = valueVersion.getValue();
            String castJsonTxt = new String(value.getValue());
            castTO = new CastCrewTO(castJsonTxt);
        }
        return castTO;
    }

    /**
     * Returns key Structure as /MV/movieId/-/cJson
     * @param movieId - TMDb movie ID
     * @return - Key component
     */
    private Key getMovieCastKey(int movieId) {
        Key key = null;

        if (movieId > -1) {
            List<String> majorComponent = new ArrayList<String>();
            majorComponent.add(KeyConstant.MOVIE_TABLE);
            majorComponent.add(Integer.toString(movieId));

            //key = Key.createKey(majorComponent, KeyConstant.CAST_JSON);
        }
        return key;
    } //getMovieCastKey

    /**
     * Returns key Structure as /CT_MV/castId/-/movieId
     * @param castId - TMDb cast ID
     * @param movieId - TMDb movie ID
     * @return - Key component
     */
    private Key getCastMovieKey(int castId, int movieId) {
        Key key = null;

        if (movieId > -1 && castId > -1) {
            List<String> majorComponent = new ArrayList<String>();
            majorComponent.add(KeyConstant.CAST_MOVIE_TABLE);
            majorComponent.add(Integer.toString(castId));

            key = Key.createKey(majorComponent, Integer.toString(movieId));
        }
        return key;
    } //getMovieCastKey
   
}


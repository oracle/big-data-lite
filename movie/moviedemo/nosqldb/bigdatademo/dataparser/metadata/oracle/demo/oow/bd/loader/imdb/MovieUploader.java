package oracle.demo.oow.bd.loader.imdb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;

import java.util.Hashtable;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.dao.CastDAO;
import oracle.demo.oow.bd.dao.CrewDAO;
import oracle.demo.oow.bd.dao.MovieDAO;
import oracle.demo.oow.bd.loader.CustomerProfileLoader;
import oracle.demo.oow.bd.loader.analytics.UserItemLoader;
import oracle.demo.oow.bd.pojo.SearchCriteria;
import oracle.demo.oow.bd.to.CastTO;
import oracle.demo.oow.bd.to.CrewTO;
import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.util.KeyUtil;
import oracle.demo.oow.bd.util.StringUtil;

public class MovieUploader {

    public MovieUploader() {
        super();
    }

    /**
     * This method is implemented just so cross referencing with IMDb movie data
     * if possible. A hashtable with movie name and poster path is created
     * which will then be read during IMDb movie loading.
     * @return Hashtable with movie title as the key and movie poster as the
     * value.
     * @throws IOException
     */
    public Hashtable<String, String> getMovieHash() throws IOException {
        Hashtable<String, String> movieHash = new Hashtable<String, String>();
        FileReader fr = null;
        String poster = null;

        try {
            fr = new FileReader(Constant.MOVIE_INFO_FILE_NAME);
            BufferedReader br = new BufferedReader(fr);
            String jsonTxt = null;
            MovieTO movieTO = null;

            //Each line in the file is the JSON string

            //Construct MovieTO from JSON object
            System.out.println("Loading TMDb data into memory ...");

            while ((jsonTxt = br.readLine()) != null) {

                try {
                    movieTO = new MovieTO(jsonTxt);
                } catch (Exception e) {
                    e.getMessage();
                }

                if (movieTO != null && !movieTO.isAdult()) {


                    poster = movieTO.getPosterPath();

                    if (StringUtil.isNotEmpty(poster)) {

                        //to reduce the size make overview as null
                        movieTO.setOverview("");

                        //System.out.println(++count + " " +  movieTO.getMovieJsonTxt());

                        //** Save the movie into List                     */
                        movieHash.put(KeyUtil.getMovieKey(movieTO.getTitle()),
                                      movieTO.getMovieJsonTxt());
                    } //EOF if
                } //EOF if

            } //EOF while
            System.out.println("Done Loading.");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            fr.close();
        }
        return movieHash;

    }


    /**
     * This method reads the file with MOVIE records and load it into kv-store
     * one movie at a time
     * @throws IOException
     */
    public void uploadMovieInfo(int targetDatabase,
                                int maxMovies) throws IOException {
        FileReader fr = null;
        try {
            fr = new FileReader(Constant.IMDB_MOVIE_INFO_FILE_NAME);
            BufferedReader br = new BufferedReader(fr);
            String jsonTxt = null;
            MovieTO movieTO = null;
            MovieDAO movieDAO = new MovieDAO();
            int count = 1;

            //Each line in the file is the JSON string

            //Construct MovieTO from JSON object
            while ((jsonTxt = br.readLine()) != null) {

                if (maxMovies <= 0 || (maxMovies > 0 && count < maxMovies)) {
                    try {
                        movieTO = new MovieTO(jsonTxt.trim());
                    } catch (Exception e) {
                        System.out.println("ERROR: Not able to parse the json string: \t" +
                                           jsonTxt);
                    }

                    if (movieTO != null && !movieTO.isAdult()) {
                        System.out.println(count++ + " " +
                                           movieTO.getMovieJsonTxt());
                        /**
                     * Save the movie into the kv-store or rdbms
                     */

                        if (targetDatabase == Constant.TARGET_BOTH ||
                            targetDatabase == Constant.TARGET_NOSQL)
                            movieDAO.insertMovie(movieTO);

                        if (targetDatabase == Constant.TARGET_BOTH ||
                            targetDatabase == Constant.TARGET_RDBMS)
                            movieDAO.insertMovieRDBMS(movieTO);

                    } //EOF if
                } //if(maxMovies>0 && count < maxMovies)
            } //EOF while


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            fr.close();
        }


    } //uploadMovies

    /**
     * This method reads the file with MOVIE-CAST records and load it into kv-store
     * one movie at a time
     * @throws IOException
     */
    public void uploadMovieCast(int targetDatabase) throws IOException {
        FileReader fr = null;
        try {
            fr = new FileReader(Constant.IMDB_MOVIE_CAST_FILE_NAME);
            BufferedReader br = new BufferedReader(fr);
            String jsonTxt = null;
            CastTO castTO = null;
            int count = 1;

            //Each line in the file is the JSON string

            //Construct MovieTO from JSON object
            while ((jsonTxt = br.readLine()) != null) {
                try {
                    castTO = new CastTO(jsonTxt.trim());
                } catch (Exception e) {
                    System.out.println("ERROR: Not able to parse the json string: \t" +
                                       jsonTxt);
                }

                if (castTO != null) {
                    System.out.println(count++ + " " + castTO.getJsonTxt());
                    /**
                     * Save the movie into the kv-store
                     */
                    CastDAO castDAO = new CastDAO();

                    if (targetDatabase == Constant.TARGET_BOTH ||
                        targetDatabase == Constant.TARGET_NOSQL)
                        castDAO.insertCastInfo(castTO);

                    if (targetDatabase == Constant.TARGET_BOTH ||
                        targetDatabase == Constant.TARGET_RDBMS)
                        castDAO.insertCastInfoRDBMS(castTO);


                } //EOF if

            } //EOF while
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            fr.close();
        }


    } //uploadMovies

    /**
     * This method reads the file with movie Crew records and load it into kv-store
     * one movie at a time
     * @throws IOException
     */
    public void uploadMovieCrew(int targetDatabase) throws IOException {
        FileReader fr = null;
        try {
            fr = new FileReader(Constant.IMDB_MOVIE_CREW_FILE_NAME);
            BufferedReader br = new BufferedReader(fr);
            String jsonTxt = null;
            CrewTO crewTO = null;
            int count = 1;

            //Each line in the file is the JSON string

            //Construct MovieTO from JSON object
            while ((jsonTxt = br.readLine()) != null) {
                try {
                    crewTO = new CrewTO(jsonTxt.trim());
                } catch (Exception e) {
                    System.out.println("ERROR: Not able to parse the json string: \t" +
                                       jsonTxt);
                }

                if (crewTO != null) {
                    System.out.println(count++ + " " + crewTO.getJsonTxt());
                    /**
                     * Save the movie into the kv-store
                     */
                    CrewDAO crewDAO = new CrewDAO();

                    if (targetDatabase == Constant.TARGET_BOTH ||
                        targetDatabase == Constant.TARGET_NOSQL)
                        crewDAO.insertCrewInfo(crewTO);

                    if (targetDatabase == Constant.TARGET_BOTH ||
                        targetDatabase == Constant.TARGET_RDBMS)
                        crewDAO.insertCrewInfoRDBMS(crewTO);

                } //EOF if

            } //EOF while
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            fr.close();
        }


    } //uploadMovieCrew

    /**
     * Run main method to upload movie info, cast & crew information into
     * Oracle NoSQL Database. There are total of six steps defined in this
     * method. Each method loads data from external flat file into the NoSQL
     * database or RDBMS.
     * <p>
     * If TARGET_NOSQL is passed as an argument to the method calls in step 1-4,
     * it means data will be persisted to NoSQL DB, where as when TARGET_RDBMS
     * is passed it means data will be stored in ORA.
     * <p>
     * For Hand-on-Lab you just need to run step 1 & 2 only. You can simply run
     * the main method without passing any argument and that would upload
     * 5000 movies into your NoSQL DB instance
     * <p>
     * If you pass an argument while running this class that would run all six 
     * steps, which means all 200+ thousand movies will be uploaded with all 
     * the cast and crew information, customer profiles will be created and
     * individual movie recommendations will be made for all 100 customer 
     * profiles.
     * @param args
     */
    public static void main(String[] args) {
        MovieUploader mu = new MovieUploader();
        MovieDAO movieDAO = new MovieDAO();
        boolean isHOL = true;
        int movieCount = 5000;

        /**
         * If running this class you pass any argument that would mean, you are
         * setting up the environment for DEMO (not for the HOL)
         */
        if (args.length > 0) {
            isHOL = false;
            movieCount = 0;
        }


        try {
            /**
             * Step 1 - Upload movie info.
             * You can set how many movies do you want to upload into kv-store.
             * There close to quater million movies in all. Default is set to
             * 5000 movies, which if set to 0, would mean upload all the movies.
             */
            mu.uploadMovieInfo(Constant.TARGET_NOSQL, movieCount);
            // mu.uploadMovieInfo(Constant.TARGET_RDBMS);

            /**
             * Step 2 - insert top movies from each genre to default customerId=0             *
             */
            SearchCriteria sc = new SearchCriteria();
            //Make poster path as must have criteria
            sc.setReleasedYear(1985);
            movieDAO.insertTopMoviesPerGenre(sc, 0);

            /**
             * Steps 3 - 6 will be run only when you pass an argument while 
             * running this class. Passing an argument would mean this is not a 
             * Hand-On-Lab but a full-fledged DEMO.
             */
            if (!isHOL) {
                /**
                * Step 3 - upload Cast information
                */
                mu.uploadMovieCast(Constant.TARGET_NOSQL);
                // mu.uploadMovieCast(Constant.TARGET_RDBMS);

                /**
                * Step 4 - upload Crew Information
                */
                mu.uploadMovieCrew(Constant.TARGET_NOSQL);
                //mu.uploadMovieCrew(Constant.TARGET_RDBMS);

                /**
                * Step 5 - Upload Customer profile information
                */
                CustomerProfileLoader cl = new CustomerProfileLoader();
                cl.uploadProfile(false);

                /**
                * Step 6 - Upload User-Item recommendations for all 100 user
                * accounts
                */
                UserItemLoader uil = new UserItemLoader();
                uil.fileReader();
            } //if(!isHOL)

        } catch (IOException e) {
            e.printStackTrace();
        }
    } //main

}

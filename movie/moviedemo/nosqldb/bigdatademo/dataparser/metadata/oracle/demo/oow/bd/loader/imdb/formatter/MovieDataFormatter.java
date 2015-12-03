package oracle.demo.oow.bd.loader.imdb.formatter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.dao.BaseDAO;
import oracle.demo.oow.bd.to.GenreTO;
import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.util.HelperUtil;
import oracle.demo.oow.bd.util.KeyUtil;
import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.Direction;
import oracle.kv.KeyValueVersion;

public class MovieDataFormatter {

    private static int IMDB_MOVIE_ID_COUNTER = 1000000;
    private static Hashtable<String, String> movieHash = null;

    private int count = 0;

    public MovieDataFormatter() {
        super();
    }


    /**
     * This method reads ratings.list file from metadata\imdb folder and
     * associate poster_path from TMDb to IMDb movies.
     * @throws IOException
     */
    public Hashtable<String, String> readIMDbMovieFile(String fileName) throws IOException {

        FileReader fr = null;
        MovieTO movieTO = null;
        String title = null;
        GenreTO genreTO = null;
        String key = null;
        int count = 0;
        boolean begin = false;
        String plot = "";
        String jsonTxt = null;


        oracle.demo.oow.bd.loader.imdb.MovieUploader tmdbUploader =
            new oracle.demo.oow.bd.loader.imdb.MovieUploader();


        try {


            fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line = null;

            //Each line in the file is the JSON string
            /**
             * First load TMDb data so paster_path association can be made
             */            
            movieHash =
                    movieHash == null ? tmdbUploader.getMovieHash() : movieHash;


            //Construct MovieTO from JSON object
            while ((line = br.readLine()) != null) {

                /**
                 * Make sure line is not empty, doesn't have '{' as that would
                 * mean it is a serial not a movie, shouldn't have word 'TV'
                 * as that would mean it is a TV serial and then '(' which would
                 * mean that it has year released information also
                 */
                if (StringUtil.isNotEmpty(line)) {
                    if (begin ||
                        (line.indexOf("(") > -1 && line.indexOf("{") == -1 &&
                         line.indexOf("(TV)") == -1 &&
                         line.indexOf("(V)") == -1 &&
                         line.indexOf("(VG)") == -1)) {

                        /**This is first cycle of association where TMDb poster_path
                    * is matched to IMDb movies
                    **/
                        if (Constant.IMDB_MOVIE_RATING_FILE_NAME.equals(fileName)) {
                            movieTO = this.getMovieInfo(line);
                            this.associatePoster2Movie(movieHash, movieTO);
                        }
                        /**
                     * this is second pass where genre information is matched to
                     * the movies.
                     */
                        else if (Constant.IMDB_MOVIE_GENRE_FILE_NAME.equals(fileName)) {
                            movieTO = this.getMovieGenre(line);


                            if (movieTO != null &&
                                StringUtil.isNotEmpty(movieTO.getTitle())) {
                                title = movieTO.getTitle();
                                if (movieTO.getGenres().size() > 0) {
                                    genreTO = movieTO.getGenres().get(0);
                                    //In IMDb genres.list file genre are repeated so cleanse it
                                    key = KeyUtil.getMovieKey(movieTO);

                                    this.associateGenre2Movie(key, genreTO);

                                } //if(movieTO.genres().size()>0
                            } //if (movieTO != null)
                        } /**
                     * this is third pass where plot information is matched to
                     * the movies.
                     */
                        else if (Constant.IMDB_MOVIE_PLOT_FILE_NAME.equals(fileName)) {


                            //begin=true means the line you are reading is the plot
                            //information
                            if (begin) {
                                if (line.indexOf("BY:") > -1) {
                                    begin = false;
                                    key = KeyUtil.getMovieKey(movieTO);

                                    //fetch movie from KV-store and assign plot
                                    jsonTxt = BaseDAO.get(key);
                                    if (StringUtil.isNotEmpty(jsonTxt)) {
                                        //System.out.println(jsonTxt);
                                        movieTO = new MovieTO(jsonTxt);

                                        //set plot information to json object
                                        movieTO.setOverview(plot.trim());
                                        jsonTxt = movieTO.getMovieJsonTxt();

                                        //set new jsonTxt back into KV-Store
                                        BaseDAO.put(key, jsonTxt);
                                        System.out.println(++count + " " +
                                                           jsonTxt);
                                    }
                                } else {
                                    plot += line.replaceAll("PL:", "");
                                }


                            } //EOF if(begin)

                            if (line.indexOf("MV:") > -1) {
                                begin = true;
                                plot = "";
                                movieTO =
                                        this.getMovieGenre(line.replaceAll("MV:",
                                                                           ""));
                                //System.out.println("Titile: " +movieTO.getTitle());

                            } //if(line.indexOf("MV:") > -1)

                        } //if(Constant.IMDB_MOVIE_PLOT_FILE_NAME.equal(fileName))


                    } //EOF if(line.indexOf("(") > -1
                } //if(StringUtil.isNotEmpty(line)

            } //EOF while


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            fr.close();
        }
        return movieHash;

    } //readIMDbMovieFile


    private void associateGenre2Movie(String key, GenreTO genreTO) {

        String jsonTxt = null;
        MovieTO movieTO = null;

        if (genreTO != null && StringUtil.isNotEmpty(key)) {
            /**
             * Get movie JSON from KV Store
             */
            jsonTxt = BaseDAO.get(key);
            if (StringUtil.isNotEmpty(jsonTxt)) {

                movieTO = new MovieTO(jsonTxt.trim());
                movieTO.addGenreTO(genreTO);

                jsonTxt = movieTO.getMovieJsonTxt();
                /**
                 * Write movie json into the KV Store for further association.
                **/
                BaseDAO.put(key, jsonTxt);
                System.out.println(jsonTxt);
            }
        }
    } //associateGenre2Movie


    private void associatePoster2Movie(Hashtable<String, String> movieHash,
                                       MovieTO movieTO) {


        MovieTO tmpMovieTO = null;
        String jsonTxt = null;

        //Get the IMDb movie title and search it into TMDb
        String key = KeyUtil.getMovieKey(movieTO.getTitle());

        if (StringUtil.isNotEmpty(key)) {

            //System.out.println("Key: " + key);

            //Check if poster URL can be found from TMDb
            if (movieHash.containsKey(key)) {

                //get JSON text from the hashtable
                jsonTxt = movieHash.get(key);

                tmpMovieTO = new MovieTO(jsonTxt);
                movieTO.setId(tmpMovieTO.getId());
                movieTO.setPosterPath(tmpMovieTO.getPosterPath());


            } else {
                movieTO.setId(IMDB_MOVIE_ID_COUNTER++);
                movieTO.setPosterPath("");

            }

            jsonTxt = movieTO.getMovieJsonTxt();
            key = KeyUtil.getMovieKey(movieTO);

            /**
             * Write movie json into the KV Store for further association.
            **/
            BaseDAO.put(key, jsonTxt);
            System.out.println(++count + " " + jsonTxt);

        } //if (StringUtil.isNotEmpty(key))
    } //associatePoster2Movie


    private void printMovies(Hashtable<String, String> movieHash) {
        Enumeration keys = movieHash.keys();
        String key = null;
        while (keys.hasMoreElements()) {
            key = (String)keys.nextElement();
            System.out.println(key);
        }

    }

    private MovieTO getMovieGenre(String line) {

        GenreTO genreTO = new GenreTO();
        MovieTO movieTO = new MovieTO();

        //System.out.println(line);

        int start = 0;
        int end = 0;
        int len = 0;

        String title = "";
        String date = null;
        String genre = null;

        //take the date out from the title and save it separately
        start = line.lastIndexOf('(');
        end = line.lastIndexOf(')');
        len = line.length();

        try {
            if (start > -1 && end > -1) {
                title = line.substring(0, start);
                title = title.replace('\"', ' ');
                movieTO.setTitle(title.trim());

                date = line.substring(start + 1, end);
                movieTO.setDate(date);

                genre = line.substring(end + 1, len);
                genre = genre.replace('|', ' ');
                genreTO.setName(genre.trim());
                movieTO.addGenreTO(genreTO);


            } //EOF if
        } catch (Exception e) {
            e.getMessage();
        }


        return movieTO;
    }

    private MovieTO getMovieInfo(String line) {
        MovieTO movieTO = null;
        String token = null;
        int count = 0;
        int vote = 0;
        int index = 0;
        int len = 0;
        double rank = 0;
        String title = "";
        String date = null;
        StringTokenizer st = new StringTokenizer(line, " ");

        try {
            movieTO = new MovieTO();
            while (st.hasMoreTokens()) {
                count++;
                token = st.nextToken();
                switch (count) {

                case 1:
                    break;
                case 2:
                    vote = Integer.parseInt(token);
                    movieTO.setVoteCount(vote);
                    break;
                case 3:
                    rank = Double.parseDouble(token);
                    movieTO.setPopularity(rank);
                    break;
                default:
                    title += token + " ";
                    break;
                } //EOF switch
            } //EOF while


            //take the date out from the title and save it separately
            index = title.lastIndexOf('(');
            len = title.length();

            if (index > -1) {
                date = title.substring(index + 1, len - 2);
                movieTO.setDate(date);

                title = title.substring(0, index);
                title = title.replace('\"', ' ');
                movieTO.setTitle(title.trim());
            } //EOF if


        } catch (Exception e) {
            e.getMessage();
        }

        return movieTO;

    }

    public static void writeMovieInfo() {


        FileWriter infoFile = null;
        KeyValueVersion valueVersion = null;
        String value = null;
        int count = 0;

        try {
            infoFile = new FileWriter(Constant.IMDB_MOVIE_INFO_FILE_NAME);
            System.out.println("Movies information is written into: '" +
                               Constant.IMDB_MOVIE_INFO_FILE_NAME + "'");

            Iterator<KeyValueVersion> iter =
                BaseDAO.getKVStore().storeIterator(Direction.UNORDERED,
                    /*batchSize*/0,
                    /*parentKey*/null, /*subRange*/null, /*depth*/null);
            

            while (iter.hasNext()) {
                valueVersion = iter.next();

                if (valueVersion != null) {
                    value =
                            new String(valueVersion.getValue().toByteArray()).trim();
                    //print it on the screen
                    System.out.println(++count + " " + value);
                    //write to file
                    infoFile.write(value + "\n");
                }


            } //EOF while

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                infoFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    } //writeMovieInfo

    public static void main(String[] args) {
        String line =
            "0000000124  597012   8.9  \"Ça ira (Il fiume della rivolta) (1994)";
        //String line = "      ........18       7   9.9  \"3 Audrey\" (2012)";

        MovieDataFormatter mvdf = new MovieDataFormatter();
        MovieTO movieTO = mvdf.getMovieInfo(line);
        //System.out.println(movieTO.getMovieJsonTxt());

        line =
"Ça ira (Il fiume della rivolta) (1964)			History";
        movieTO = mvdf.getMovieGenre(line);
        //System.out.println(movieTO.getMovieJsonTxt());


        //System.exit(0);

        try {

            /**
             * First load movie information and associate poster_path
             */
            mvdf.readIMDbMovieFile(Constant.IMDB_MOVIE_RATING_FILE_NAME);
            /**
             * Next associate genre information to the movies by reading genres
             * from imdb/genres.list
             */
            mvdf.readIMDbMovieFile(Constant.IMDB_MOVIE_GENRE_FILE_NAME);
            /**
             * Next associate plot information to the movies by reading plot
             * information from plot.list
             */
            mvdf.readIMDbMovieFile(Constant.IMDB_MOVIE_PLOT_FILE_NAME);

            /**
             * Write JSON object to file now             *
             */
            mvdf.writeMovieInfo();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Just to print what all IDs exist
        HelperUtil.printUniqueId();
    } //main

}

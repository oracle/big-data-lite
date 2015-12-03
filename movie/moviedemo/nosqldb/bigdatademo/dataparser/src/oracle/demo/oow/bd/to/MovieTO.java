package oracle.demo.oow.bd.to;

import java.io.IOException;

import java.util.ArrayList;

import java.util.Hashtable;

import java.util.Iterator;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.constant.JsonConstant;

import oracle.demo.oow.bd.dao.MovieDAO;

import oracle.demo.oow.bd.pojo.RatingType;

import oracle.demo.oow.bd.util.HelperUtil;
import oracle.demo.oow.bd.util.StringUtil;

import org.codehaus.jackson.node.ArrayNode;


/**
 * Oracle MoviePlex app persist information about hundred of thousands of movies
 * and this class is used to capture details about a movie like movie name,
 * movie id, date it was released, overview of the movie, popularity, number of
 * votes it has, url of the movie poster and also list of genres this movie
 * belong to.
 * <p>
 * All the above attributes of the movies can be accessed by simple getter/setter
 * methods but in addition movie data can also be serialized into JSON text,
 * which can be saved in kv-store and then can be retrieved from there to be
 * later converted into the MovieTO by simply passing the JSON text to one of the
 * constructor.
 */
public class MovieTO extends BaseTO implements Comparable<MovieTO> {


    /** Unique Movie Id default to -1 **/
    private int id = -1;

    /** Movie title **/
    private String title = null;

    /** Movie plot describing what this movie is about**/
    private String overview = "";

    /** Date when this movies is released **/
    private String date = null;

    /** Whether this movie is adult or not. Later all adult movies are filtered
     * out **/
    private boolean adult;

    /** Movie run time. Mostly described in minutes**/
    private int runTime;

    /** Number of votes this movie has.  **/
    private int voteCount;

    /** Each movie suppose to have movie poster. This attribute is used to
     * save the poster URL **/
    private String posterPath = "";

    /** On scale of 10 how popular this movie is **/
    private double popularity;

    /** A movie can belong to one or many genres. This list is used to save all
     * the genres this movie belongs to **/
    private ArrayList<GenreTO> genres = new ArrayList<GenreTO>();

    /** Each movie has number of cast as well as crew members. This object stores
     * information about cast and crew for this movie. **/
    private CastCrewTO castCrewTO = null;

    /** Where popularity represent the average rating for this movie based on
     * number of votes this movie has, userRating represent rating given by
     * the customer for this movie. **/
    private RatingType userRating = null;

    /** Again to sort the collection of movies based on the user-defined score **/
    private long order = 1;


    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to MovieTO **/
    private ObjectNode objectNode = null;



    /** Constructor without any argument to initialize a new object with defalt
     * values for the attibutes.
     */
    public MovieTO() {
        super();
    }

    /**
     * This constructor takes JSON text as an input argument and constructs
     * JSON object from this string object. Once JSON object is constructed
     * values are read and set into MovieTO object.
     * @param movieJsonTxt
     */
    public MovieTO(String movieJsonTxt) {
        super();
        try {
            objectNode = super.parseJson(movieJsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } 
        this.setMovieJson(objectNode);

    }
    
    /**
     * If you have JSON object (instead of JSON text) then you can simply
     * pass to this constructor that will then execute getter operation to
     * extract values for different parameters.
     * @param objectNode - JSON representation of MovieTO
     */
    public MovieTO(ObjectNode objectNode) {
        super();
        this.setMovieJson(objectNode);
    }
    
    /** Set movie title **/
    public void setTitle(String title) {
        this.title = title;
    }

    /** Get movie title **/
    public String getTitle() {
        return title;
    }

    /** Set movie plot **/
    public void setOverview(String overview) {
        this.overview = overview;
    }

    /** Set movie plot **/
    public String getOverview() {
        return overview;
    }

    /** Set the date when this movie was released **/
    public void setDate(String date) {
        int index = -1;
        if (StringUtil.isNotEmpty(date)) {
            index = date.indexOf("/");
            date = index > -1 ? date.substring(0, index) : date;
        }
        this.date = date;
    }

    /** Get the release date for this movie **/
    public String getDate() {
        return date;
    }

    /** Set true if movie is adult otherwise false **/
    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    /** Returns true if movie is adult **/
    public boolean isAdult() {
        return adult;
    }

    /** Set movie run time **/
    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    /** Get the lenth of the movie in minutes **/
    public int getRunTime() {
        return runTime;
    }

    /** Set number of votes for this movie **/
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    /** Get the vote count for this movie **/
    public int getVoteCount() {
        return voteCount;
    }

    /** Set URL of the movie poster **/
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    /** Get poster URL **/
    public String getPosterPath() {
        return posterPath;
    }

    /** Add list of genres this movie belongs to **/
    public void setGenres(ArrayList<GenreTO> genres) {
        this.genres = genres;
    }

    /** Get list of genres for this movie **/
    public ArrayList<GenreTO> getGenres() {
        return genres;
    }

    /** Get movie popularity **/
    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    /** Get movie popularity **/
    public double getPopularity() {
        return popularity;
    }

    /**
     * Based on when the movie is released and how popular the movie is, price
     * to rent the movie is determined.
     * @return movie rental price
     */
    public double getPrice() {
        double price = 0;
        try {
            int year = Integer.parseInt(this.getDate());
            if (year >= 2010 && this.getPopularity() >= 8) {
                price = 3.99;
            } else if (year <= 2010 && this.getPopularity() >= 8) {
                price = 2.99;
            } else {
                price = 1.99;
            }
        } catch (Exception e) {
            System.out.println("ERROR: Release date " + this.getDate() + " for movie " + this.getTitle() +
                               " is not a valid.");
        }

        return price;
    } //getPrice


    /**
     * One can set list of genres for this movie or add genre one at a time.
     * This method is a helper method which enable adding GenreTO for this movie
     *
     * @param genreTO defining one of the genre this movie belongs to.
     */
    public void addGenreTO(GenreTO genreTO) {
        //add unique genre only
        Hashtable<String, String> uniqueHash = new Hashtable<String, String>();
        String key = null;
        int id = -1;

        //construct hash with all the existing genres this movie belongs to
        for (GenreTO gTO : this.genres) {
            key = gTO.getName();
            uniqueHash.put(key, "");
        } //EOF for

        //Now compare and add iff doesn't exist already.
        key = genreTO.getName();
        id = genreTO.getId();

        if (id < 0 && !uniqueHash.containsKey(key)) {
            //get unique id for the genre if the ID is 0
            genreTO.setId(HelperUtil.getUniqueId(key));
        }

        genres.add(genreTO);

    } //addGenreTO

    /** Set movie id **/
    public void setId(int id) {
        this.id = id;
    }

    /** Get movie id **/
    public int getId() {
        return id;
    }

    /** Get the year when this movie was released **/

    public int getReleasedYear() {
        int year = 0;
        try {
            year = Integer.parseInt(date);
        } catch (Exception e) {
            e.getMessage();
        }
        return year;
    } //getReleasedYear

    /** Set CastCrewTO to this object **/
    public void setCastCrewTO(CastCrewTO castCrewTO) {
        this.castCrewTO = castCrewTO;
    }

    /** Get CastCrewTO object that has information about all the cast and
     * crew for this movie **/
    public CastCrewTO getCastCrewTO() {
        return castCrewTO;
    }

    /** This method compared this movie object with the other movie object
     * passed as an attribute and rank the movie based on the order value
     * in dessending order **/
    public int compareTo(MovieTO movieTO) {

        long order = movieTO.getOrder();
        //dessending order
        return (int)(order - this.order);

    }

    /**
     * User can also set the movie rating. This method is to set such rating.
     * @param userRating of RatingType
     */
    public void setUserRating(RatingType userRating) {
        this.userRating = userRating;
    }

    /**
     * Get user rating
     * @return RatingType
     */
    public RatingType getUserRating() {
        return userRating;
    }

    /**
     * Set the movie order for sorting them in a collection
     * @param order
     */
    public void setOrder(long order) {
        this.order = order;
    }

    /**
     * Get movie order
     * @return movie order
     */
    public long getOrder() {
        return order;
    }

    /** If you have a JSON object then parameter and their values can be read
     * from  the JSON object and passed into the MovieTO object for simple
     * data access using getter/setter methods. **/
    public void setMovieJson(ObjectNode objectNode) {

        GenreTO genreTO = null;
        this.objectNode = objectNode;
        ObjectNode genreNode = null;

        try {
            /**
         * Set all other attributes from the json object
         */
            String title = objectNode.get(JsonConstant.TITLE).getTextValue();
            String overview = objectNode.get(JsonConstant.OVERVIEW).getTextValue();
            int runTime = 0;
            String runTimeStr = objectNode.get(JsonConstant.RUNTIME).getTextValue();
            int id = objectNode.get(JsonConstant.ID).getIntValue();
            int voteCount = objectNode.get(JsonConstant.VOTE).getIntValue();
            String posterPath = objectNode.get(JsonConstant.POSTER).getTextValue();
            double popularity = objectNode.get(JsonConstant.POPULARITY).getDoubleValue();
            String date = objectNode.get(JsonConstant.RELEASE_DATE).getTextValue();

            try {
                runTime = Integer.parseInt(runTimeStr);
            } catch (NumberFormatException ne) {
                runTime = 0;
            }

            //Save movie info into TO
            this.setAdult(adult);
            this.setTitle(title);
            this.setOverview(overview);
            this.setRunTime(runTime);
            this.setVoteCount(voteCount);
            this.setPosterPath(posterPath);
            this.setPopularity(popularity);
            this.setDate(date);
            this.setId(id);

            //JSONArray generes = movieJson.getJSONArray(JsonConstant.GENRES);
            Iterator iter = objectNode.get(JsonConstant.GENRES).iterator();

            while (iter.hasNext()) {
                genreNode = (ObjectNode)iter.next();
                genreTO = new GenreTO(genreNode);                
                
                //set GenreTO to movieTO
                this.addGenreTO(genreTO);

                //System.out.println("Id: " + genreTO.getId() + " Name: " + genreTO.getName());

            } //EOF for
        } catch (Exception e) {
            e.getMessage();
        }
        /*

        System.out.println("ID: "  + id + " Adult: " + adult + " Title: " + title +
                           " Runtime: " + runTime + " voteAvg: " +
                           voteAvg + " voteCount: " + voteCount +
                           " \nposterPath: " + posterPath +
                           " Popularity: " + popularity + " Date: " +
                           date + "\nOverview: " + overview);
        */
    } //setMovieJson


    /**
     * This is a helper method that creates Json string from MovieTO object.
     * JSON text representing movie attributes then can be persisted in kv-store
     *
     * @return JSON text with all the movie attributes.
     */
    public String getMovieJsonTxt() {
        return this.getMovieJson().toString();
    }

    /** This method returns JSON object with all the movie attributes and its
     * values**/
    public ObjectNode getMovieJson() {
        ObjectNode movieJson = super.getObjectNode();
        ArrayNode genreArray = super.getArrayNode();
        ObjectNode genreJson = null;

        movieJson.put(JsonConstant.ID, this.getId());
        movieJson.put(JsonConstant.TITLE, this.getTitle());
        movieJson.put(JsonConstant.RELEASE_DATE, this.getDate());        
        movieJson.put(JsonConstant.VOTE, this.getVoteCount());
        movieJson.put(JsonConstant.POPULARITY, this.getPopularity());
        movieJson.put(JsonConstant.POSTER, this.getPosterPath());
        movieJson.put(JsonConstant.RUNTIME, this.getRunTime());
        movieJson.put(JsonConstant.OVERVIEW, this.getOverview());

        for (GenreTO genreTO : genres) {
            genreJson = genreTO.getGenreJson();
            genreArray.add(genreJson);
        } //EOF for

        //set genres to movie json object
       movieJson.put(JsonConstant.GENRES, genreArray);

        return movieJson;
    }

    public String toString() {
        String description = this.overview.length() >= 2048 ? this.overview.substring(0, 2048) : this.overview;
        String movieStr =
            "" + this.getId() + Constant.DELIMITER + this.title + Constant.DELIMITER + this.date + Constant.DELIMITER +
            this.popularity + Constant.DELIMITER + this.voteCount + Constant.DELIMITER + description;

        return movieStr;
    }

    /**
     * Main method for unit testing the class
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        MovieDAO dao = new MovieDAO();
        MovieTO movieTO = dao.getMovieById(827);

        System.out.println(movieTO.getMovieJsonTxt());


    } //main


    @Override
    public String toJsonString() {
        // TODO Implement this method
        return getMovieJsonTxt();
    }
}

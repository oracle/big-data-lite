package oracle.demo.oow.bd.dao;

import java.util.List;

import oracle.demo.oow.bd.pojo.ActivityType;
import oracle.demo.oow.bd.pojo.BooleanType;
import oracle.demo.oow.bd.pojo.RatingType;

import oracle.demo.oow.bd.to.ActivityTO;
import oracle.demo.oow.bd.to.MovieTO;

import oracle.demo.oow.bd.util.FileWriterUtil;
import oracle.demo.oow.bd.util.KeyUtil;

import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.avro.JsonAvroBinding;

import oracle.kv.avro.JsonRecord;

import org.apache.avro.Schema;

import org.codehaus.jackson.node.ObjectNode;

public class ActivityDAO extends BaseDAO {

    MovieDAO movieDAO = null;

    /** Variables for JSONAvroBinding ***/
    private Schema activitySchema = null;
    private JsonAvroBinding activityBinding = null;

    public ActivityDAO() {
        super();
        movieDAO = new MovieDAO();
        activitySchema = parser.getTypes().get("oracle.avro.Activity");
        activityBinding = catalog.getJsonBinding(activitySchema);
    }

    /**
     * This method returns the ActivityTO for the movie saved in the Currently
     * watching list. When a user pause a movie they were watching the position
     * of the movie is saved as ActivityTO, so if you want to find out what
     * position the movie was paused at you need to get the object from the same
     * currently watching list.
     * If ActivityTO is null that means movie has not been started yet.
     * @param custId
     * @param movieId
     * @return ActivityTO
     */
    public ActivityTO getActivityTO(int custId, int movieId) {
        ActivityTO activityTO = null;
        Value value = null;
        ValueVersion vv = null;
        Key key = null;
        
        //update the current position of the movie into current watch list
        key = KeyUtil.getCustomerCurrentWatchListKey(custId, movieId);
        vv = this.getKVStore().get(key);

        //System.out.println("Position in the jsonTxt: " + jsonTxt);

        if (vv!=null) {
            value = vv.getValue();
            activityTO = this.getActivityTO(value);
        }

        return activityTO;
    } //getActivityTO

    public List<MovieTO> getCustomerCurrentWatchList(int custId) {
        Key key = KeyUtil.getCustomerCurrentWatchListKey(custId, 0);

        List<MovieTO> movieList = movieDAO.getMoviesByKey(key);
        return movieList;
    }

    public List<MovieTO> getCustomerBrowseList(int custId) {
        Key key = KeyUtil.getCustomerBrowseListKey(custId, 0);
        List<MovieTO> movieList = movieDAO.getMoviesByKey(key);
        return movieList;
    }

    public List<MovieTO> getCustomerHistoricWatchList(int custId) {
        Key key = KeyUtil.getCustomerHistoricalWatchListKey(custId, 0);
        List<MovieTO> movieList = movieDAO.getMoviesByKey(key);
        return movieList;
    }

    public List<MovieTO> getCommonPlayList() {
        Key key = KeyUtil.getCommonWatchListKey();
        List<MovieTO> movieList = movieDAO.getMoviesByKey(key);
        return movieList;
    }


    public void insertCustomerActivity(ActivityTO activityTO) {
        int custId = 0;
        int movieId = 0;
        ActivityType activityType = null;
        Key key = null;
        Value value = null;
        String jsonTxt = null;

        CustomerDAO customerDAO = new CustomerDAO();

        if (activityTO != null) {
            jsonTxt = activityTO.getJsonTxt();
            System.out.println("User Activity| " + jsonTxt);
            
            value = this.toValue(activityTO);            
                
            /**
             * This system out should write the content to the application log
             * file.
             */
            FileWriterUtil.writeOnFile(jsonTxt);
            
            custId = activityTO.getCustId();
            movieId = activityTO.getMovieId();

            if (custId > 0 && movieId > 0) {
                activityType = activityTO.getActivity();

                switch (activityType) {
                case STARTED_MOVIE:
                    //insert movie entry into customer's currently watching list
                    key = KeyUtil.getCustomerCurrentWatchListKey(custId, movieId);
                    super.getKVStore().put(key, value);
                    
                    //delete movie from the browse list
                    key = KeyUtil.getCustomerBrowseListKey(custId, movieId);
                    delete(key);
                    break;
                case PAUSED_MOVIE:
                    //update the current position of the movie into current watch list
                    key = KeyUtil.getCustomerCurrentWatchListKey(custId, movieId);
                    super.getKVStore().put(key, value);
                    break;
                case COMPLETED_MOVIE:
                    //inset movie entry into historical play list
                    key = KeyUtil.getCustomerHistoricalWatchListKey(custId, movieId);
                    super.getKVStore().put(key, value);
                    //delete movie entry from currently watching list
                    key = KeyUtil.getCustomerCurrentWatchListKey(custId, movieId);
                    delete(key);
                    break;
                case RATE_MOVIE:
                    //insert user rating for the movie in the CT_MV table
                    customerDAO.insertMovieRating(custId, movieId, activityTO);
                    break;
                case BROWSED_MOVIE:
                    //insert browse information
                    key = KeyUtil.getCustomerBrowseListKey(custId, movieId);
                    super.getKVStore().put(key, value);
                    break;

                }
            } //if (custId > 0 && movieId > 0)

        } //if (activityTO != null)

    } //insetCustomerActivity

    private void flushQueues(int custId) {
        //delete everything from Recently watched queue
        Key key = KeyUtil.getCustomerHistoricalWatchListKey(custId, 0);
        multiDelete(key);

        //delete everything from currently watching queue
        key = KeyUtil.getCustomerCurrentWatchListKey(custId, 0);
        multiDelete(key);

        //delete everything from browse queue
        key = KeyUtil.getCustomerBrowseListKey(custId, 0);
        multiDelete(key);

    }

    private void printMovies(List<MovieTO> movieList) {
        for (MovieTO movieTO : movieList) {
            System.out.println("\t" + movieTO.getMovieJsonTxt());
        }
    } //printMovies

    private void testActivityInsertion() {
        ActivityTO activityTO = new ActivityTO();
        int custId = 123;
        ActivityDAO aDAO = new ActivityDAO();

        activityTO.setRating(RatingType.THREE);
        activityTO.setRecommended(BooleanType.NO);
        activityTO.setCustId(custId);
        activityTO.setPrice(1.99);
        activityTO.setMovieId(80806);

        try {

            //Add something to browse list
            activityTO.setActivity(ActivityType.BROWSED_MOVIE);
            aDAO.insertCustomerActivity(activityTO);
            System.out.println("Browsed List ...");
            List<MovieTO> movieList = aDAO.getCustomerBrowseList(custId);
            aDAO.printMovies(movieList);

            //insert into currently watching list
            activityTO.setActivity(ActivityType.STARTED_MOVIE);
            aDAO.insertCustomerActivity(activityTO);

            //insert one more movie
            Thread.sleep(100);
            activityTO.setTimeStamp(System.currentTimeMillis());
            activityTO.setMovieId(857);

            aDAO.insertCustomerActivity(activityTO);


            //Third movie
            Thread.sleep(100);
            activityTO.setTimeStamp(System.currentTimeMillis());
            activityTO.setMovieId(1032843);
            aDAO.insertCustomerActivity(activityTO);


            //System.out.println("JsonTxt before: " + activityTO.getJsonTxt());
            //Lets not stop the movie and update the move where it was stopped
            activityTO.setPosition(20);
            activityTO.setActivity(ActivityType.PAUSED_MOVIE);
            Thread.sleep(100);
            activityTO.setTimeStamp(System.currentTimeMillis());
            aDAO.insertCustomerActivity(activityTO);
            //System.out.println("JsonTxt after : " + activityTO.getJsonTxt());

            //also pause second movie
            activityTO.setPosition(55);
            activityTO.setMovieId(857);
            activityTO.setActivity(ActivityType.PAUSED_MOVIE);
            Thread.sleep(100);
            activityTO.setTimeStamp(System.currentTimeMillis());
            aDAO.insertCustomerActivity(activityTO);

            System.out.println("Current watch List ...");
            movieList = aDAO.getCustomerCurrentWatchList(custId);
            aDAO.printMovies(movieList);


            //Finish watching the movie
            activityTO.setActivity(ActivityType.COMPLETED_MOVIE);
            aDAO.insertCustomerActivity(activityTO);

            System.out.println("Current watch List after movie completion ...");
            movieList = aDAO.getCustomerCurrentWatchList(custId);
            aDAO.printMovies(movieList);

            System.out.println("Historical watch List ...");
            movieList = aDAO.getCustomerHistoricWatchList(custId);
            aDAO.printMovies(movieList);

            System.out.println("Browsed List ...");
            movieList = aDAO.getCustomerBrowseList(custId);
            aDAO.printMovies(movieList);

            //insert movie rating
            activityTO.setActivity(ActivityType.RATE_MOVIE);
            activityTO.setRating(RatingType.FIVE);
            activityTO.setMovieId(857);
            aDAO.insertCustomerActivity(activityTO);

            //validate the rating is indeed saved
            Key key = KeyUtil.getCustomerMovieKey(custId, 857);
            String jsonTxt = get(key);
            activityTO = new ActivityTO(jsonTxt);
            System.out.println("Rating is: " + activityTO.getRating() + " " + jsonTxt);

            System.out.println("Get Paused movie for the customer ...");
            activityTO = aDAO.getActivityTO(custId, 1032843);
            System.out.println("\t" + activityTO.getJsonTxt());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method takes ActivityTO object and serialize it to Value object.
     * @param activityTO
     * @return
     */
    public Value toValue(ActivityTO activityTO) {
        ObjectNode actNode = null;
        JsonRecord jsonRecord = null;
        Value value = null;

        if (activityTO != null) {
            actNode = activityTO.getActivityJson();
            jsonRecord = new JsonRecord(actNode, activitySchema);
            // serialize CustomerTO to byte array using JSONAvroBinding
            value = activityBinding.toValue(jsonRecord);
        }

        return value;

    } //toValue

    /**
     * This method takes Value object as input and deserialize it into ActivityTO. The assumption here is that
     * Value that is passed as argument is serialized ActivityTO JSON object.
     * @param actTOValue
     * @return ActivityTO
     */
    public ActivityTO getActivityTO(Value actTOValue) {

        ActivityTO actTO = null;
        ObjectNode actNode = null;
        JsonRecord jsonRecord = null;

        if (actTOValue != null) {
            jsonRecord = activityBinding.toObject(actTOValue);
            actNode = (ObjectNode)jsonRecord.getJsonNode();
            actTO = new ActivityTO(actNode);
        }

        return actTO;

    } //toCustomerTO

    public static void main(String[] args) {

        ActivityDAO aDAO = new ActivityDAO();
        int custId = 123;

        aDAO.flushQueues(custId);
        aDAO.testActivityInsertion();

        List<MovieTO> movieList = aDAO.getCustomerBrowseList(custId);

        System.out.println("Browse List");
        aDAO.printMovies(movieList);

        System.out.println("Historical List");
        movieList = aDAO.getCustomerHistoricWatchList(custId);
        aDAO.printMovies(movieList);

        System.out.println("Current Watch List");
        movieList = aDAO.getCustomerCurrentWatchList(custId);
        aDAO.printMovies(movieList);

        //System.exit(0);


    }
}//ActivityDAO

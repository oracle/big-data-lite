package oracle.demo.oow.bd.dao;

import java.util.List;

import oracle.demo.oow.bd.constant.KeyConstant;
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

import oracle.kv.table.IndexKey;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

import org.apache.avro.Schema;

import org.codehaus.jackson.node.ObjectNode;

public class ActivityDAO extends BaseDAO {

    MovieDAO movieDAO = null;
    
    private static Table activityTable = null;
    public final static String TABLE_NAME="ACTIVITY";


    public ActivityDAO() {
        super();
        movieDAO = new MovieDAO();
        activityTable = getKVStore().getTableAPI().getTable(TABLE_NAME);
        
//        activitySchema = parser.getTypes().get("oracle.avro.Activity");
//        activityBinding = catalog.getJsonBinding(activitySchema);
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
       
        Row vv = null;
        PrimaryKey key = null;
        
        //update the current position of the movie into current watch list
        
        key = KeyUtil.getCustomerCurrentWatchListKey(custId, movieId,activityTable);
        vv = this.getKVStore().getTableAPI().get(key,null);

        //System.out.println("Position in the jsonTxt: " + jsonTxt);

        if (vv!=null) {
          String  value = vv.toJsonString(true);
            activityTO = new ActivityTO(value);
        }

        return activityTO;
    } //getActivityTO

    public List<MovieTO> getCustomerCurrentWatchList(int custId) {
        PrimaryKey key = KeyUtil.getCustomerCurrentWatchListKey(custId, 0,activityTable);

        List<MovieTO> movieList = movieDAO.getMoviesByKey(key);
        return movieList;
    }

    public List<MovieTO> getCustomerBrowseList(int custId) {
        PrimaryKey key = KeyUtil.getCustomerBrowseListKey(custId, 0,activityTable);
        List<MovieTO> movieList = movieDAO.getMoviesByKey(key);
        return movieList;
    }

    public List<MovieTO> getCustomerHistoricWatchList(int custId) {
        PrimaryKey key = KeyUtil.getCustomerHistoricalWatchListKey(custId, 0,activityTable);
        List<MovieTO> movieList = movieDAO.getMoviesByKey(key);
        return movieList;
    }

    public List<MovieTO> getCommonPlayList() {
        IndexKey key = KeyUtil.getCommonWatchListKey(activityTable);
        List<MovieTO> movieList = movieDAO.getMoviesByKey(key);
        return movieList;
    }


    public void insertCustomerActivity(ActivityTO activityTO) {
        int custId = 0;
        int movieId = 0;
        ActivityType activityType = null;
        PrimaryKey key = null;
        Row value = null;
        String jsonTxt = null;

        CustomerDAO customerDAO = new CustomerDAO();

        if (activityTO != null) {
            jsonTxt = activityTO.getJsonTxt();
            System.out.println("User Activity| " + jsonTxt);
            
                     
                
            /**
             * This system out should write the content to the application log
             * file.
             */
            FileWriterUtil.writeOnFile(activityTO.getActivityJsonOriginal().toString());
            
            custId = activityTO.getCustId();
            movieId = activityTO.getMovieId();

            if (custId > 0 && movieId > 0) {
                activityType = activityTO.getActivity();

                switch (activityType) {
                case STARTED_MOVIE:
                    //insert movie entry into customer's currently watching list
                    //key = KeyUtil.getCustomerCurrentWatchListKey(custId, movieId,activityTable);
                    activityTO.setTableId(KeyConstant.CUSTOMER_CURRENT_WATCH_LIST);
                    value = activityTable.createRowFromJson(activityTO.toJsonString(),false);  
                    super.getKVStore().getTableAPI().put(value, null,null);
                    
                    //delete movie from the browse list
                    key = KeyUtil.getCustomerBrowseListKey(custId, movieId,activityTable);
                    super.getKVStore().getTableAPI().delete(key, null,null);
                    //delete(new Key());
                    break;
                case PAUSED_MOVIE:
                    //update the current position of the movie into current watch list
                    activityTO.setTableId(KeyConstant.CUSTOMER_CURRENT_WATCH_LIST);
                    value = activityTable.createRowFromJson(activityTO.toJsonString(),false);  
                    super.getKVStore().getTableAPI().put(value, null,null);
                    break;
                case COMPLETED_MOVIE:
                    //inset movie entry into historical play list
                    activityTO.setTableId( KeyConstant.CUSTOMER_HISTORICAL_WATCH_LIST);
                    value = activityTable.createRowFromJson(activityTO.toJsonString(),false);  
                    
                    super.getKVStore().getTableAPI().put(value, null,null);
                    //delete movie entry from currently watching list
                    key = KeyUtil.getCustomerBrowseListKey(custId, movieId,activityTable);
                    super.getKVStore().getTableAPI().delete(key, null,null);
                    break;
                case RATE_MOVIE:
                    //insert user rating for the movie in the CT_MV table
                    customerDAO.insertMovieRating(custId, movieId, activityTO);
                    break;
                case BROWSED_MOVIE:
                    //insert browse information
                    activityTO.setTableId( KeyConstant.CUSTOMER_BROWSE_LIST);
                    value = activityTable.createRowFromJson(activityTO.toJsonString(),false);  
                    
                    super.getKVStore().getTableAPI().put(value, null,null);
                    
                    
                    break;

                }
            } //if (custId > 0 && movieId > 0)

        } //if (activityTO != null)

    } //insetCustomerActivity

    private void flushQueues(int custId) {
        //delete everything from Recently watched queue
        PrimaryKey key = KeyUtil.getCustomerHistoricalWatchListKey(custId, 0,activityTable);
        multiDelete(key);

        //delete everything from currently watching queue
        key = KeyUtil.getCustomerCurrentWatchListKey(custId, 0,activityTable);
        multiDelete(key);

        //delete everything from browse queue
        key = KeyUtil.getCustomerBrowseListKey(custId, 0,activityTable);
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
            
            PrimaryKey key = KeyUtil.getCustomerMovieKey(custId, 857,activityTable);
            Row row= getTableAPI().get(key, null);
            String jsonTxt =null;
            if(row!=null){
                jsonTxt = row.toJsonString(true);
            }
            activityTO = new ActivityTO(jsonTxt);
            System.out.println("Rating is: " + activityTO.getRating() + " " + jsonTxt);

            System.out.println("Get Paused movie for the customer ...");
            activityTO = aDAO.getActivityTO(custId, 1032843);
            System.out.println("\t" + activityTO.getJsonTxt());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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

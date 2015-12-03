package oracle.demo.oow.bd.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import oracle.demo.oow.bd.config.StoreConfig;
import oracle.demo.oow.bd.constant.KeyConstant;

import oracle.demo.oow.bd.dao.CustomerDAO;

import oracle.demo.oow.bd.dao.MovieDAO;
import oracle.demo.oow.bd.to.CustomerTO;

import oracle.demo.oow.bd.to.MovieTO;

import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;


/**
 * The purpose of this class is to illustrate some basic capablities in NoSQL DB
 * You can use this class when demonstrating the Oracle MoviePlex application to
 * illustrate some concepts, like:
 *    What is a key?
 *    What is a value?
 *    How do you update a key/value?
 */
public class Demo {
    KVStore kvstore = null;
    private ObjectNode custNode = null;
    private ObjectMapper jsonMapper = new ObjectMapper();
    CustomerDAO custDAO = new CustomerDAO();
    MovieDAO movieDAO = new MovieDAO();
        
    private static final String CUSTOMER_INFO = "1";
    private static final String MOVIE_INFO = "2";        
    private static final String CLEAR_LISTS = "3";    
    
    /**
     *   Prompt the user for an activity:
     *   1.  Show the profile info for a customer.  The key is the user name - the value
     *       contains things like email, full name, a numeric user id, etc.
     *   2.  Show information about a movie (plot summary, etc.)
     *   3.  Delete the list of movies that have been recently browsed & watched
     */
    public Demo() {
                
        try {
            System.out.println("==================");
            System.out.println("= Oracle NoSQL DB");
            System.out.println("==================");
            System.out.print("Connecting...");
            
            // Make a connection to the KV store
            kvstore = KVStoreFactory.getStore(new KVStoreConfig(StoreConfig.KVSTORE_NAME,
                                                                        StoreConfig.KVSTORE_URL));
        } catch (Exception e) {
            System.out.println("");
            System.out.println("ERROR: Please make sure Oracle NoSQL Database is up and running at '" +
                               StoreConfig.KVSTORE_URL +
                               "' with store name as: '" +
                               StoreConfig.KVSTORE_NAME + "'");
            return;
        }
        
        System.out.println(" connected!");
     
        
        /* Collect user input from the console  
         * There are 3 valid activities:
         * 1. Show a customer profile
         * 2. Show information about a movie
         * 3. Clear the browsed and watched lists         
         */ 
        
        List<String> validActions = Arrays.asList(CUSTOMER_INFO,MOVIE_INFO,CLEAR_LISTS);        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            while (true) {
                String action=null, userName=null, movieName=null;
                String defaultAction = CUSTOMER_INFO;
                String defaultUserName = "guest1";
                String defaultMovieName = "The Avengers";
                System.out.println();
                System.out.println("=======================================");
                System.out.println("What do you want to do?");
                System.out.println("1.  Show customer profile");
                System.out.println("2.  Show information about a movie");
                System.out.println("3.  Clear browsed and watched lists");
                System.out.println();
                System.out.print("Enter action [" + defaultAction + "]       :  ");
                action = br.readLine();

                // default to show customer profile
                if (!validActions.contains(action))
                    action=defaultAction;
                
                if (action.equalsIgnoreCase(CUSTOMER_INFO) || action.equalsIgnoreCase(CLEAR_LISTS)) {
                    System.out.print("Enter customer [" + defaultUserName + "]:  ");
                    userName = br.readLine();
                    
                    // default to guest1
                    if (userName.trim().equalsIgnoreCase(""))
                        userName = defaultUserName;
                }
                else {
                    System.out.print("Enter Movie Name [" + defaultMovieName + "]:  ");
                    movieName = br.readLine();
                    
                    // default to The Avengers
                    if (movieName.trim().equalsIgnoreCase(""))
                        movieName = defaultMovieName;                    
                }
                                
                System.out.println("");
                
                // Run appropriate method based on the activity selection
                if (action.equals(CUSTOMER_INFO))
                    showCustomerValue(userName);
                else if (action.equals(CLEAR_LISTS))
                    clearLists(userName);
                else
                    showMovieValue(movieName);
                
            }
        } catch (Exception e) {
            System.out.println("Unable to read input from console.");
            System.exit(1);
        }
        

    }
    
    /**
     * Displays information about a movie
     * @param movieName
     */
    public void showMovieValue(String movieName) {
                    
        KeyValueVersion keyValue = null;
        Value value = null;
        String movieIdStr = null;
        Key movieIdKey = null;
        MovieTO movieTO = null;
        
        // The DB has an index on Movie Name.  You will use this index to
        // retrieve the ID for the Movie Name.  Note, there may be multiple
        // movies with the same name
        Key movieNameKey = getMovieKey(movieName, KeyConstant.MOVIE_NAME_TABLE);

        // Find movie id's for this movie name
        Iterator<KeyValueVersion> keyIter = kvstore.multiGetIterator(
                                  Direction.FORWARD, 0,
                                  movieNameKey, null, null);
        
        // Are there any movies available?
        if (!keyIter.hasNext()) {
            System.out.println("Movie \"" + movieName + "\" not found.");
            System.out.println("");
            return;
        }

        String snippet="";
        
        System.out.println("Movie Information");
        System.out.println("--------------------");
        System.out.println("(Note:  there may be multiple movies w/the same name.)");
        System.out.println("");
        System.out.println("Movie Name Key    : " + movieNameKey.toString());

        // Will loop over all movies with the same name                
        while (keyIter.hasNext()) {
            keyValue = keyIter.next();
          
            // the value is the movie id
            value = keyValue.getValue();
            movieIdStr = new String(value.getValue());
        
            // get the movieid key for this movieid value
            movieIdKey = getMovieKey(movieIdStr, KeyConstant.MOVIE_TABLE);
            
            long start = Calendar.getInstance().getTimeInMillis();
            
            ValueVersion vv = kvstore.get(movieIdKey);
            
            long elapsed = Calendar.getInstance().getTimeInMillis() - start;
            
            // Get the information about the movie            
            movieTO = movieDAO.getMovieTO(vv.getValue());

            System.out.println("Movie Name Value  : " + movieIdStr);
            System.out.println("");
            System.out.println("Movie ID Key      : " + movieIdKey.toString());
            System.out.println("Movie ID Value    : " + movieTO.getMovieJsonTxt());    
            System.out.println();
            System.out.println("elapsed: " + elapsed + "ms");   
            System.out.println("");
            
            snippet = snippet + "  movieIdKey   = Key.fromString(\"" + movieIdKey.toString() + "\");\n";
            snippet = snippet + "  movieIdValue = kvstore.get(movieIdKey);\n";
           
            
        } //EOF while

        System.out.println("snippet:");
        System.out.println("  movieNameKey = Key.fromString(\"" + movieNameKey.toString() + "\");");            
        System.out.println("  movieNameVal = kvstore.get(movieNameKey);");                        
        System.out.println();
        System.out.println(snippet);                        
    }
    
    
    /**
     * Display the value (i.e. profile details) for the specified user
     * @param userName
     */
    public void showCustomerValue(String userName) {
        
        long start;
        Value custValue = null;
        ValueVersion vv = null;
        CustomerTO custTO = null;

        
        // The key is represented as /CUST/username/-/info
        Key key = getCustomerKey(userName);
                
        // Time how long it takes to process 
        start = Calendar.getInstance().getTimeInMillis();
        
        // Get the value version for the key                
        vv = kvstore.get(key);
        
        long elapsed = Calendar.getInstance().getTimeInMillis() - start;
        
        if (vv == null) {
            System.out.println("Customer \"" + userName + "\" not found.");
            System.out.println();
        }
        else {
            // Get a string representation of the byte array
            custValue = vv.getValue();
            custTO = custDAO.getCustomerTO(custValue);
            
            System.out.println("Customer Information");
            System.out.println("--------------------");
            System.out.println("key    : " + key.toString());
            System.out.println("value  : " + custTO.getJsonTxt());      
            System.out.println();
            System.out.println("elapsed: " + elapsed + "ms");   
            System.out.println("");
            System.out.println("snippet:");
            System.out.println("  key   = Key.fromString(\"" + key.toString() + "\");");            
            System.out.println("  value = kvstore.get(key);");                        
            System.out.println("");

        }                
    }
    
    /**
     * Clear the user's browse and watch history
     * @param userName
     */
    private void clearLists(String userName) {
                
        // The key is represented as /CUST/username/-/info 
        Key customerKey = getCustomerKey(userName);
        Value custValue = null;
        ValueVersion vv = null;
        CustomerTO custTO = null;
        
        if (customerKey == null) {
            System.out.println("Customer \"" + userName + "\" not found.");
            System.out.println();
            return;
        }
        
        // The lists use the userid - not the username - in the key
        // This userid is in the customer profile - or the value for the customer key
        // Below, we'll parse the JSON value and extract the userid
        
        // Find the value for the customer key        
        vv = kvstore.get(customerKey);
        
        if (vv == null)
            return;
        
        // get Customer AVRO object
        custValue = vv.getValue();
                
        // get the custid ("id") from the JSON value
        custTO = custDAO.getCustomerTO(custValue);
        
        int custId = custTO.getId();
        
        // Movie Lists that will be cleared
        List<String> movieLists = Arrays.asList(KeyConstant.CUSTOMER_CURRENT_WATCH_LIST,
                                                KeyConstant.CUSTOMER_HISTORICAL_WATCH_LIST,
                                                KeyConstant.CUSTOMER_BROWSE_LIST); 
        List<String> movieListDesc = Arrays.asList("Current Watch List",
                                                "Historical Watch List",
                                                "Browsing History"); 

        int i = 0;
        for (String movieList : movieLists) {
            
            Key key = getMovieListKey(custId, movieList);    

            // Time how long it takes to delete
            long start = Calendar.getInstance().getTimeInMillis();
            if (key != null)
                kvstore.multiDelete(key, null, null);
            
            long elapsed = Calendar.getInstance().getTimeInMillis() - start;
            
            System.out.println("Clear " + movieListDesc.get(i));
                                
            System.out.println("---------------------------");
            System.out.println("key    : " + key.toString());            
            System.out.println("elapsed: " + elapsed + "ms");   
            System.out.println("");
            System.out.println("snippet:");
            System.out.println("  key   = Key.fromString(\"" + key.toString() + "\");");            
            System.out.println("  kvstore.multiDelete(key, null, null);");
            System.out.println("");
            
            i++;
                        
        }        
                                
    }
    
    /**
     * Return the customer key based on the user name
     * @param userName
     * @return customer key
     */
    private Key getCustomerKey(String userName) {
        
        // The key is represented as /CUST/username/-/info
        
        List<String> majorComponent = null;
        
        majorComponent = new ArrayList<String>();
        majorComponent.add(KeyConstant.CUSTOMER_TABLE);
        majorComponent.add(userName.toLowerCase());    

        return Key.createKey(majorComponent, KeyConstant.INFO_COLUMN);            

    }
    
    /**
     * Return the key for one of the movie lists:  Browse, Current Watch, Historical Watch
     * @param custId
     * @param tableName The name of the list
     * @return
     */
    private Key getMovieListKey(int custId, String tableName) {
        
        // Create the major component using the appropriate prefix
        List<String> majorComponent = new ArrayList<String>();
        majorComponent.add(tableName);
        majorComponent.add(Integer.toString(custId));

        Key key = Key.createKey(majorComponent);

        return key;
    }
    
    /**
     * Get the movie key 
     * @param movieName or movie ID
     * @param movieTable - identifies whether to return the Movie Name key or Movie ID key
     * @return movieKey - Key for the movie, which is the movie name key or movie ID key
     */
    private Key getMovieKey(String movieName, String movieTable) {
        Key key = null;

        List<String> majorComponent = new ArrayList<String>();

        majorComponent.add(movieTable);
        majorComponent.add(movieName.toLowerCase());

        key = Key.createKey(majorComponent);
        
        return key;
    } //getMovieKey    
    


    public static void main(String[] args) {
        Demo demo = new Demo();
    }
}
package oracle.demo.oow.bd.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import oracle.demo.oow.bd.config.StoreConfig;
import oracle.demo.oow.bd.constant.KeyConstant;

import oracle.demo.oow.bd.dao.BaseDAO;

import oracle.demo.oow.bd.to.CustomerTO;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.table.IndexKey;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;
import oracle.kv.table.TableIterator;


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
        
    private static final String CUSTOMER_INFO = "1";
    private static final String MOVIE_INFO = "2";        
    private static final String CLEAR_LISTS = "3"; 
    
    private static final String MOVIE_TABLE="MOVIE";
    private static final String CUSTOMER_TABLE="CUSTOMER";
    private static final String ACTIVITY_TABLE="ACTIVITY";
    
    TableAPI hTableAPI;
    Table hCustomerTable;
    Table hMovieTable;
    Table hActivityTable;
        
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
        
        // Create handle to both the table api, the tables being queried and the Index keys
        hTableAPI = kvstore.getTableAPI();

        hCustomerTable = hTableAPI.getTable(CUSTOMER_TABLE);        
        hMovieTable = hTableAPI.getTable(MOVIE_TABLE);
        hActivityTable = hTableAPI.getTable(ACTIVITY_TABLE);
     
        
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
                    // Enter a movie to look up
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
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Displays information about a movie
     * @param movieName
     */
    public void showMovieValue(String movieName) {
                            
        // The DB has an index on Movie Name.  You will use this index to
        // retrieve records for that movie.  Note, there may be multiple
        // movies with the same name
        IndexKey movieNameKey = hMovieTable.getIndex("movieNameIndex").createIndexKey();
        movieNameKey.put("original_title",movieName);
        
        TableIterator<Row> rowIter = hTableAPI.tableIterator(movieNameKey, null, null);
        
        // Are there any movies available?
        if (!rowIter.hasNext()) {
            System.out.println("Movie \"" + movieName + "\" not found.");
            System.out.println("");
            rowIter.close();
            return;
        }

        String snippet="";
        
        System.out.println("Movie Information");
        System.out.println("--------------------");
        System.out.println("(Note:  there may be multiple movies w/the same name.)");
        System.out.println("");
        System.out.println("Movie Index  : " + movieNameKey.toString());

        // Will loop over all movies with the same name                
        while (rowIter.hasNext()) {
            Row row = rowIter.next();
          
            // Capture a JSON document for the row of data returned 
            long start = Calendar.getInstance().getTimeInMillis();
            String value = row.toJsonString(true);             
            long elapsed = Calendar.getInstance().getTimeInMillis() - start;
            
            // Get specific attributes from the movie record         
            System.out.println("Movie Name   : " + row.get("original_title"));
            System.out.println("Key          : " + row.get("id"));
            System.out.println("Year         : " + row.get("release_date"));
            System.out.println("JSON         : " + value);    
            System.out.println();
            System.out.println("elapsed: " + elapsed + "ms");   
            System.out.println("");
           
            
        } //EOF while

        rowIter.close();
        
        System.out.println("snippet:");
        System.out.println("  IndexKey movieNameKey = hMovieTable.getIndex(\"movieNameIndex\").createIndexKey();");
        System.out.println("  movieNameKey.put(\"original_title\",\"" + movieName + "\");");
        System.out.println("  // Loop over each movie with that name");
        System.out.println("  while (iter.hasNext()) {");
        System.out.println("     row   = iter.Next()");
        System.out.println("     value = row.toJsonString(true);");
        System.out.println("     year  = row.get(\"release_date\");");
        System.out.println("  };");                        
        System.out.println();
    }
    
    
    /**
     * Display the value (i.e. profile details) for the specified user
     * @param userName
     */
    public void showCustomerValue(String userName) {
        
        long start;
        String custValue = null;
        
        // There is an index on the user name.  F
        IndexKey key = hCustomerTable.getIndex("userNameIdx").createIndexKey();
        key.put("username",userName);
                
        // Time how long it takes to process 
        start = Calendar.getInstance().getTimeInMillis();
        
        // Get the value version for the key                
        TableIterator<Row> rows = hTableAPI.tableIterator(key, null, null);
        
        long elapsed = Calendar.getInstance().getTimeInMillis() - start;
        
        
        if (rows == null || !rows.hasNext()) {
            System.out.println("Customer \"" + userName + "\" not found.");
            System.out.println();
        }
        else {
            // Only retrieve the first user with that name
            Row row = rows.next();
            custValue = row.toJsonString(true);
            
            System.out.println("Customer Information");
            System.out.println("--------------------");
            System.out.println("name index : " + key.toString());
            System.out.println("value      : " + custValue);      
            System.out.println();
            System.out.println("elapsed: " + elapsed + "ms");   
            System.out.println("");
            System.out.println("snippet:");
            System.out.println("  IndexKey key = hCustomerTable.getIndex(\"userNameIdx\").createIndexKey();"); 
            System.out.println("  key.put(\"username\",\"" + userName + "\");");
            System.out.println("  TableIterator<Row> rows= hTableAPI.tableIterator(key, null, null);");
            System.out.println("  value = rowIter.next().toJsonString(true);");                        
            System.out.println("");

        }                
        
        rows.close();
    }
    
    /**
     * Clear the user's browse and watch history
     * @param userName
     */
    private void clearLists(String userName) {
        // The userName field is indexed.  Get the customer id (the primary key) for this userName
        // Use the customer id to clear the lists (e.g. recently browsed, watch history)

        IndexKey customerKey = hCustomerTable.getIndex("userNameIdx").createIndexKey();
        customerKey.put("username",userName);
        
        TableIterator<Row> rows = hTableAPI.tableIterator(customerKey, null, null);
        
        // Check if there is a record for this username
        if (!rows.hasNext()) {
            System.out.println("Customer \"" + userName + "\" not found.");
            System.out.println();
            rows.close();
            return;
        }
        
        // Get the customer id - which uniquely identifies that customer
        int custId = rows.next().get("id").asInteger().get();
        rows.close();                
        
        // Movie Lists that will be cleared
        List<String> movieLists = Arrays.asList(KeyConstant.CUSTOMER_CURRENT_WATCH_LIST,
                                                KeyConstant.CUSTOMER_HISTORICAL_WATCH_LIST,
                                                KeyConstant.CUSTOMER_BROWSE_LIST); 
        List<String> movieListDesc = Arrays.asList("Current Watch List",
                                                "Historical Watch List",
                                                "Browsing History"); 
        
        int i = 0;
        
        for (String movieList : movieLists) {
            
            PrimaryKey key = getMovieListKey(custId, movieList);    

            // Time how long it takes to delete
            long start = Calendar.getInstance().getTimeInMillis();

            // delete  the records for this customer / list
            if (key != null)
                hTableAPI.multiDelete(key, null, null);
            
            long elapsed = Calendar.getInstance().getTimeInMillis() - start;
            
            System.out.println("Clear " + movieListDesc.get(i));
                                
            System.out.println("---------------------------");
            System.out.println("key    : " + key.toString());            
            System.out.println("elapsed: " + elapsed + "ms");   
            System.out.println("");
            System.out.println("snippet:");
            System.out.println("  key  = hActivityTable.createPrimaryKey();"); 
            System.out.println("  key.put(\"tableId\", \"" + movieList + "\");"); 
            System.out.println("  key.put(\"custId\"," + custId + ");");
            System.out.println("  kvstore.multiDelete(key);");
            System.out.println("");
            
            i++;                        
        }        
                                
    }
    
            
    /**
     * Return the key for one of the movie lists:  Browse, Current Watch, Historical Watch
     * @param custId
     * @param tableName The name of the list
     * @return
     */
    private PrimaryKey getMovieListKey(int custId, String tableName) {
        
        PrimaryKey key  = hActivityTable.createPrimaryKey();
        key.put("tableId", tableName);
        key.put("custId",custId);
        return key;
    }
    
    
    public static void main(String[] args) {
        Demo demo = new Demo();
    }
}

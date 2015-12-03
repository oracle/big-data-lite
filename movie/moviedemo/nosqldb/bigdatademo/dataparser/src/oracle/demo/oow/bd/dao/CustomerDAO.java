package oracle.demo.oow.bd.dao;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import oracle.demo.oow.bd.constant.KeyConstant;
import oracle.demo.oow.bd.to.ActivityTO;
import oracle.demo.oow.bd.to.CustomerGenreMovieTO;
import oracle.demo.oow.bd.to.CustomerGenreTO;
import oracle.demo.oow.bd.to.CustomerTO;
import oracle.demo.oow.bd.to.GenreMovieTO;
import oracle.demo.oow.bd.to.GenreTO;
import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.to.ScoredGenreTO;
import oracle.demo.oow.bd.util.KeyUtil;
import oracle.demo.oow.bd.util.StringUtil;


import oracle.kv.Direction;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.Version;
import oracle.kv.avro.JsonAvroBinding;
import oracle.kv.avro.JsonRecord;

import oracle.kv.table.IndexKey;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

import oracle.kv.table.TableIterator;

import org.apache.avro.Schema;

import org.codehaus.jackson.node.ObjectNode;


public class CustomerDAO extends BaseDAO {

    private static Table customerTable = null;
    public final static String TABLE_NAME="CUSTOMER";
    public final static String CHILD_TABLE="customerGenres";
    public final static String CUSTOMER_GENRE_MOVIE_TABLE = "customerGenreMovie";
    
    private static int MOVIE_MAX_COUNT = 25;
    private static int GENRE_MAX_COUNT = 10;

    private static final String PASSWORD = StringUtil.getMessageDigest("welcome1");
    private static final String USERNAME = "guest";

    /** Variables for JSONAvroBinding ***/
    private Schema customerSchema = null;
    private JsonAvroBinding customerBinding = null;

    public CustomerDAO() {
        super();
    customerTable = getKVStore().getTableAPI().getTable(TABLE_NAME);
    }


    /**
     * Overloaded method that defaults max number of movies to MAX_COUNT limit.
     * This method fetches recommended movies for the customer. If the
     * recommendation is not yet made for the customer for the genreId then
     * DEFAULT recommendation is used.
     * @param custId
     * @param genreId
     * @return
     */
    public List<MovieTO> getMovies4CustomerByGenre(int custId, int genreId) {
        return getMovies4CustomerByGenre(custId, genreId, MOVIE_MAX_COUNT);
    }

    /**
     * This  method fetches recommended movies for the customer. If the
     * recommendation is not yet made for the customer for the genreId then
     * DEFAULT recommendation is used.
     * @param custId
     * @param genreId
     * * @param maxCount
     * @return List of MovieTO
     */
    public List<MovieTO> getMovies4CustomerByGenre(int custId, int genreId, int maxCount) {
        List<MovieTO> movieList = new ArrayList<MovieTO>();
        MovieTO movieTO = null;
        ActivityTO activityTO = null;
        MovieDAO movieDAO = new MovieDAO();
        Row row = null;
        String value = null;
        List<String> minorComp = null;
        String movieIdStr = null;
        int movieId = 0;
        int count = 0;

        PrimaryKey key = KeyUtil.getCustomerGenreMovieKey(custId, genreId, 0,getTable(TABLE_NAME+"."+CUSTOMER_GENRE_MOVIE_TABLE));

        TableIterator<Row> keyIter = getTableAPI().tableIterator(key, null, null);

        /** Check to make sure there are some movies returned otherwise select
         * again as a DEFAULT user
         **/
        if (!keyIter.hasNext()) {
            key = KeyUtil.getCustomerGenreMovieKey(0, genreId, 0,getTable(TABLE_NAME+"."+CUSTOMER_GENRE_MOVIE_TABLE));
            keyIter = getTableAPI().tableIterator(key, null, null);
        }
        //Now the best attempt is made to get the recommended movies for the
        //customer is done lets fetch the MovieTO
        while (keyIter.hasNext()) {

            row = keyIter.next();
            value = row.toJsonString(true);
            CustomerGenreMovieTO custGM = new CustomerGenreMovieTO(value);

            
            movieId = custGM.getMovieId();

            //get MovieTO by movieId
            movieTO = movieDAO.getMovieById(movieId);

            if (movieTO != null) {

                /**
                 * Check to see if movie poster is available. If it is then give a
                 * score of 100 otherwise 0. This would help ordering movies with
                 * posters on the top of the list.
                 */
                if (StringUtil.isNotEmpty(movieTO.getPosterPath())) {
                    movieTO.setOrder(100);
                } else {
                    movieTO.setOrder(0);
                }

                //Check to see if user has already rated this movie
                activityTO = this.getMovieRating(custId, movieId);
                if (activityTO != null) {
                    movieTO.setUserRating(activityTO.getRating());
                }

                //add movieTO to the list
                movieList.add(movieTO);

                //check if count is less than or equals to maxCount or not
                if (++count >= maxCount) {
                    break;
                }
            } //if(movieTO!=null)

        } //EOF while

        //Sort the movie list
        Collections.sort(movieList);
        return movieList;
    } //getMovie4CustomerByGenre


    public List<GenreMovieTO> getMovies4Customer(int custId) {
        return getMovies4Customer(custId, MOVIE_MAX_COUNT, GENRE_MAX_COUNT);
    }

    /**
     * This method returns top N movies for M ordered genres recommended to the
     * customer.
     *
     * @param custId
     * @param movieMaxCount
     * @param genreMaxCount
     * @return
     */
    public List<GenreMovieTO> getMovies4Customer(int custId, int movieMaxCount, int genreMaxCount) {


        List<GenreMovieTO> genreMovieList = new ArrayList<GenreMovieTO>();
        int genreId = 0;
        String name = null;
        GenreMovieTO genreMovieTO = null;
        GenreTO genreTO = null;
        List<MovieTO> movieList = null;
        int count = 0;

        /**
         * Get ordered list of genres for the customer. key=/CT/custId/-/genre
         */
        PrimaryKey key = KeyUtil.getCustomerGenresKey(custId,customerTable);
        Row row = getKVStore().getTableAPI().get(key, null);
        //If there is no ordered list of genres available for the customer
        //get the default ordered list
        String jsonTxt=null;
        if(row!=null)
        jsonTxt = row.toJsonString(true);
        if (StringUtil.isEmpty(jsonTxt)) {
            key = KeyUtil.getCustomerGenresKey(0,customerTable);
            
            row= getKVStore().getTableAPI().get(key,null);
            jsonTxt = (row!=null?row.toJsonString(true):null);
        } //if (StringUtil.isEmpty(jsonTxt))

        if (StringUtil.isNotEmpty(jsonTxt)) {
            //jsonTxt is CustomerGenreTO type so convert it into TO
            //System.out.println("getMovies4Customer " + jsonTxt);
            CustomerGenreTO customerGenreTO = new CustomerGenreTO(jsonTxt);

            for (ScoredGenreTO scoredGenreTO : customerGenreTO.getScoredGenreList()) {

                count++;
                //create genreMovieTO object
                genreMovieTO = new GenreMovieTO();
                //Get values from ScoredGenreTO and assign it to GenreTO
                genreId = scoredGenreTO.getId();
                name = scoredGenreTO.getName();

                //create GenreTO
                genreTO = new GenreTO();
                genreTO.setId(genreId);
                genreTO.setName(name);

                //get Movie list by genre
                movieList = this.getMovies4CustomerByGenre(custId, genreId, movieMaxCount);

                //set GenreTO & MovieTO list to GenreMovieTO
                genreMovieTO.setGenreTO(genreTO);
                genreMovieTO.setMovieList(movieList);

                //add genreMovieTO to the list
                genreMovieList.add(genreMovieTO);

                //Break the loop if you have got M top genres from the list
                if (count >= genreMaxCount) {
                    break;
                }

            } //for (ScoredGenreTO scoredGenreTO
        } else {
            System.out.println("Error: Default recommendation data is not fed into DB yet:\nPlease run MovieDAO.insertTopMoviesPerGenre() method first to seed the default recommendation.");
        } //if(StringUtil.isNotEmpty(jsonTxt))

        return genreMovieList;
    } //getCustomerMoviesByGenre

    /**
     * This method returns ActivitTO that has customer's movie rating
     * @param custId
     * @param movieId
     * @return ActivityTO
     */
    public ActivityTO getMovieRating(int custId, int movieId) {
        Table activityTable = getTable(ActivityDAO.TABLE_NAME);
        PrimaryKey key = KeyUtil.getCustomerMovieKey(custId, movieId,activityTable);
        Row row = getTableAPI().get(key,null);
        String jsonTxt = null;
        if(row!=null)
        jsonTxt = row.toJsonString(true);
        ActivityTO activityTO = null;
        if (StringUtil.isNotEmpty(jsonTxt)) {
            activityTO = new ActivityTO(jsonTxt); 
        }
        return activityTO;
    } //getMovieRating

    /**
     * This method persists user's movie rating as activityTO jsonTxt
     * @param custId
     * @param movieId
     * @param activityTO
     */
    public void insertMovieRating(int custId, int movieId, ActivityTO activityTO) {
        if (activityTO != null) {
            String jsonTxt = activityTO.getJsonTxt();
            Row row = getTable(ActivityDAO.TABLE_NAME).createRowFromJson(jsonTxt, true);
            getTableAPI().put(row, null,null);
        }
    } //insertMovieRating

    /**
     * This method inserts customer profile information into the store if it
     * does not exist already.
     * @param customerTO contains customer's name, email, username, password &
     * customer ID information.
     * @return Version of the new value, or null if an existing value is present
     * and the put is unsuccessful.
     */
    public Version insertCustomerProfile(CustomerTO customerTO, boolean force) {

        
        Row row=null;
        Version version = null;

        if (customerTO != null) {
            // key=/CUST/userName
            //key = KeyUtil.getCustomerKey(customerTO.getUserName());
            // serialize CustomerTO to byte array using JSONAvroBinding
            System.out.println("Customer => " + customerTO.toJsonString());
            row = customerTable.createRowFromJson(customerTO.toJsonString(), true);

            if (force) {
                //Insert profile no matter it already existed
                version = getKVStore().getTableAPI().put(row, null,null);
            } else {
                //Insert if profile by same userName & password doesn't exist
                version = getKVStore().getTableAPI().putIfAbsent(row,null,null);
            }
        } //if(customerTO!=null){
        return version;
    } //insertCustomerProfile

    /**
     * This method returns all the customer profiles stored in the database
     * @return List of CustomerTO
     */
    public List<CustomerTO> getCustomerProfiles() {
        List<CustomerTO> customerList = new ArrayList<CustomerTO>();
        PrimaryKey key = customerTable.createPrimaryKey();
        
        CustomerTO customerTO = null;
        String value = null;
        Row row = null;
        TableIterator<Row> rows =  getTableAPI().tableIterator(key, null,null);
        while (rows.hasNext()) {
            row = rows.next();
            if(row!=null)
            value = row.toJsonString(true);
            if (value != null) {
                customerTO = new CustomerTO(value);
                //add customerTO to the array list
                customerList.add(customerTO);                
                //System.out.println(customerTO.getJsonTxt());
            }
        } //EOF while

        return customerList;
    } //getCustomerProfiles

    /**
     * This method deletes all the customer profiles and their associations
     * with movies.
     */
    public void deleteCustomerProfiles() {
        PrimaryKey key = customerTable.createPrimaryKey();
        
        CustomerTO customerTO = null;
        String value = null;
        Row row = null;
        BaseDAO.multiDelete(key);

    } //deleteCustomerProfiles

    /**
     * This method validates if username and password are valid. If they are then it return CustomerTO
     * otherwise null.
     * @param username
     * @param password
     * @return CustomerTO
     */
    public CustomerTO getCustomerByCredential(String username, String password) {
        IndexKey key = null;
        

        CustomerTO customerTO = null;
        //Apply MD5 hashing on the password String
        //password = StringUtil.getMessageDigest(password);

        if (StringUtil.isNotEmpty(username) && StringUtil.isNotEmpty(password)) {
            key = customerTable.getIndex("userNameIdx").createIndexKey();
            key.put("username", username);
            
            TableIterator<Row>  vv = getTableAPI().tableIterator(key, null, null);

            if (vv.hasNext()) {
                //deserialize the value object
                Row row = vv.next();
                if(row!= null){
                
                customerTO = new CustomerTO(row.toJsonString(true));
                }

                /**
                 * Check to make sure password is same too and if it is then
                 * return the CutomerTO otherwise null
                 */
                if (!customerTO.getPassword().equals(password)) {
                    customerTO = null;
                } //if (!customerTO.getPassword().equals(password)) {
            } //if (StringUtil.isNotEmpty(jsonTxt))
        }
        return customerTO;
    }

    /**
     * This method reads all the customers from the database and construct a
     * collection of CustomerTO with user credential informations too.
     * @return CustomerTO
     * @throws SQLException
     */
    public CustomerTO getCustomerTO(int custId) throws SQLException {

        Connection conn = null;
        List<CustomerTO> customerList = new ArrayList<CustomerTO>();
        CustomerTO customerTO = null;
        Statement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = super.getOraConnect();
            stmt = conn.createStatement();

            String sql = "SELECT CUST_ID, FIRST_NAME, EMAIL FROM CUSTOMER WHERE CUST_ID = " + custId;
            rs = stmt.executeQuery(sql);
            //STEP 5: Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                int id = rs.getInt("CUST_ID");
                String first = rs.getString("FIRST_NAME");
                String email = rs.getString("EMAIL");
                String userName = (USERNAME + ++count).toLowerCase();

                //create new object
                customerTO = new CustomerTO();
                customerTO.setId(id);
                customerTO.setEmail(email);
                customerTO.setName(first);
                //credentials
                customerTO.setUserName(userName);
                customerTO.setPassword(PASSWORD);


                //add to the list
                customerList.add(customerTO);

                //Display values
                System.out.println(customerTO.getJsonTxt());

            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            rs.close();
            stmt.close();
        }

        return customerTO;
    } //getCustomers




    public static void main(String[] args) {

        CustomerDAO customerDAO = new CustomerDAO();
 
      List<MovieTO> mt =   customerDAO.getMovies4CustomerByGenre(7,14);
      for(MovieTO m:mt){
          System.out.println(m.toJsonString());
      }
        
        //System.exit(0);
        /*
        List<GenreMovieTO> genreMovieList = null;
        GenreTO genreTO = null;
        List<MovieTO> movieList = null;
        Key key = null;
        */
        String jsonTxt = null;
        int custId = 0;
        CustomerTO custTO = new CustomerTO();
        custTO.setEmail("adam@o.com");
        custTO.setId(1024758);
        custTO.setName("adam");
        custTO.setUserName("guest1");
        custTO.setPassword("Welcome1");
        //custTO.setPassword("Welcome1");

        jsonTxt = custTO.getJsonTxt();

        System.out.println("Customer JSON: " + jsonTxt);

        //Insert customer profile
        customerDAO.insertCustomerProfile(custTO, true);

        custTO = customerDAO.getCustomerByCredential("guest1", "welcome1");
        if (custTO != null) {
            custId = custTO.getId();
            System.out.println("Customer is Valid: " + custTO.getJsonTxt());
            /*
            // Get the movie genre ordered list for this customer //
            key = KeyUtil.getCustomerGenresKey(custId);

            jsonTxt = customerDAO.get(key);
            System.out.println("Ordered genre List: " + jsonTxt);

            genreMovieList = customerDAO.getMovies4Customer(custId, 30, 6);
            for (GenreMovieTO genreMovieTO : genreMovieList) {
                genreTO = genreMovieTO.getGenreTO();
                int categoryId = genreTO.getId();
                String genreName = genreTO.getName();
                System.out.println(genreName);

                movieList = customerDAO.getMovies4CustomerByGenre(custId, categoryId);
                for (MovieTO movieTO : movieList) {
                    System.out.println("\t" + movieTO.getTitle() + " " + movieTO.getPosterPath());
                } //for

            } //for
    */
        } else {
            System.out.println("Customer is not Valid");
        }


    } //main

}//CustomerDAO

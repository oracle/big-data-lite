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
import oracle.demo.oow.bd.to.CustomerGenreTO;
import oracle.demo.oow.bd.to.CustomerTO;
import oracle.demo.oow.bd.to.GenreMovieTO;
import oracle.demo.oow.bd.to.GenreTO;
import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.to.ScoredGenreTO;
import oracle.demo.oow.bd.util.KeyUtil;
import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.Direction;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;

import oracle.kv.avro.JsonAvroBinding;
import oracle.kv.avro.JsonRecord;

import org.apache.avro.Schema;

import org.codehaus.jackson.node.ObjectNode;

public class CustomerDAO extends BaseDAO {

    private static int MOVIE_MAX_COUNT = 25;
    private static int GENRE_MAX_COUNT = 10;
    
    private static final String PASSWORD = StringUtil.getMessageDigest("Welcome1");
    private static final String USERNAME = "guest";
    
    /** Variables for JSONAvroBinding ***/        
    private Schema customerSchema = null;
    private JsonAvroBinding customerBinding = null;
    
    public CustomerDAO() {
        super();
        customerSchema = parser.getTypes().get("oracle.avro.Customer");        
        customerBinding = catalog.getJsonBinding(customerSchema);
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
        KeyValueVersion keyValue = null;
        Value value = null;
        List<String> minorComp = null;
        String movieIdStr = null;
        int movieId = 0;
        int count = 0;

        Key key = KeyUtil.getCustomerGenreMovieKey(custId, genreId, 0);

        Iterator<KeyValueVersion> keyIter = super.getKVStore().multiGetIterator(Direction.FORWARD, 0,
                /*key*/key, null, null);

        /** Check to make sure there are some movies returned otherwise select
         * again as a DEFAULT user
         **/
        if (!keyIter.hasNext()) {
            key = KeyUtil.getCustomerGenreMovieKey(0, genreId, 0);
            keyIter = super.getKVStore().multiGetIterator(Direction.FORWARD, 0,
                        /*key*/key, null, null);
        }
        //Now the best attempt is made to get the recommended movies for the
        //customer is done lets fetch the MovieTO
        while (keyIter.hasNext()) {

            keyValue = keyIter.next();
            value = keyValue.getValue();
            key = keyValue.getKey();

            //movieId is set as the minor component.
            minorComp = key.getMinorPath();
            //get the movieId from the minorComp
            movieIdStr = minorComp.get(0);
            //Convert String into castId
            movieId = Integer.parseInt(movieIdStr.trim());

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
        Key key = KeyUtil.getCustomerGenresKey(custId);
        String jsonTxt = get(key);
        //If there is no ordered list of genres available for the customer
        //get the default ordered list
        if (StringUtil.isEmpty(jsonTxt)) {
            key = KeyUtil.getCustomerGenresKey(0);
            jsonTxt = get(key);
        } //if (StringUtil.isEmpty(jsonTxt))

        if (StringUtil.isNotEmpty(jsonTxt)) {
            //jsonTxt is CustomerGenreTO type so convert it into TO
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
        Key key = KeyUtil.getCustomerMovieKey(custId, movieId);
        String jsonTxt = get(key);
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
            Key key = KeyUtil.getCustomerMovieKey(custId, movieId);
            String jsonTxt = activityTO.getJsonTxt();
            put(key, jsonTxt);
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

        Key key = null;
        Value value = null;
        Version version = null;
       
        if (customerTO != null) {            
            // key=/CUST/userName
            key = KeyUtil.getCustomerKey(customerTO.getUserName());
            // serialize CustomerTO to byte array using JSONAvroBinding
            value = this.toValue(customerTO);

            if (force) {
                //Insert profile no matter it already existed
                version = getKVStore().put(key, value);
            } else {
                //Insert if profile by same userName & password doesn't exist
                version = getKVStore().putIfAbsent(key, value);
            }
        } //if(customerTO!=null){
        return version;
    } //insertCustomerProfile

    /**
     * This method deletes all the customer profiles and their associations
     * with movies.
     */
    public void deleteCustomerProfiles() {
        Key key = Key.createKey(KeyConstant.CUSTOMER_TABLE);
        KeyValueVersion keyValue = null;

        Iterator<KeyValueVersion> keyIter = super.getKVStore().storeIterator(Direction.UNORDERED, 0,
                /*key*/key, null, null);
        while (keyIter.hasNext()) {
            keyValue = keyIter.next();
            delete(keyValue.getKey());
        } //EOF while
    } //deleteCustomerProfiles

    /**
     * This method validates if username and password are valid. If they are then it return CustomerTO 
     * otherwise null.
     * @param username
     * @param password
     * @return CustomerTO
     */
    public CustomerTO getCustomerByCredential(String username, String password) {
        Key key = null;
        ValueVersion vv = null;
        
        CustomerTO customerTO = null;
        //Apply MD5 hashing on the password String
        password = StringUtil.getMessageDigest(password);

        if (StringUtil.isNotEmpty(username) && StringUtil.isNotEmpty(password)) {
            key = KeyUtil.getCustomerKey(username);
            vv = getKVStore().get(key);

            if (vv != null) {
                //deserialize the value object
                customerTO = this.getCustomerTO(vv.getValue());
                
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
        custTO.setId(4);
        custTO.setName("adam");
        custTO.setUserName("guest1");
        custTO.setPassword(StringUtil.getMessageDigest("Welcome1"));
        //custTO.setPassword("Welcome1");

        jsonTxt = custTO.getJsonTxt();

        System.out.println("Customer JSON: " + jsonTxt);

        //Insert customer profile
        customerDAO.insertCustomerProfile(custTO, true);

        custTO = customerDAO.getCustomerByCredential("guest1", "Welcome1");
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
    
    /**
     * This method takes CustomerTO object and serialize it to Value object.
     * @param customerTO
     * @return
     */
    public Value toValue(CustomerTO customerTO){
        ObjectNode customerNode = null;
        JsonRecord jsonRecord = null;
        Value value = null;
        
        if (customerTO != null) {
            customerNode = customerTO.geCustomertJson();
            jsonRecord = new JsonRecord(customerNode, customerSchema);            
            // serialize CustomerTO to byte array using JSONAvroBinding
            value = customerBinding.toValue(jsonRecord);
        }
        
        return value;

    }//toValue
    
    /**
     * This method takes Value object as input and deserialize it into CustomerTO. The assumption here is that
     * Value that is passed as argument is serialized CustomerTO JSON object.
     * @param custTOValue
     * @return CustomerTO
     */
    public CustomerTO getCustomerTO(Value custTOValue){
        
        CustomerTO custTO = null;
        ObjectNode customerNode = null;
        JsonRecord jsonRecord = null;
        
        if(custTOValue!=null){
            jsonRecord = customerBinding.toObject(custTOValue);
            customerNode = (ObjectNode) jsonRecord.getJsonNode();
            custTO = new CustomerTO(customerNode);
        }
        
        return custTO; 
        
    }//toCustomerTO

}//CustomerDAO

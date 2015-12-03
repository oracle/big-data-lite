package oracle.demo.oow.bd.loader.analytics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import oracle.demo.oow.bd.constant.Constant;

import oracle.demo.oow.bd.constant.MovieConstant;
import oracle.demo.oow.bd.dao.BaseDAO;
import oracle.demo.oow.bd.dao.CustomerDAO;
import oracle.demo.oow.bd.dao.GenreDAO;
import oracle.demo.oow.bd.to.CustomerGenreMovieTO;
import oracle.demo.oow.bd.to.CustomerGenreTO;
import oracle.demo.oow.bd.to.CustomerTO;
import oracle.demo.oow.bd.to.GenreTO;
import oracle.demo.oow.bd.to.ScoredGenreTO;
import oracle.demo.oow.bd.util.KeyUtil;

import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.Key;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

public class UserItemLoader extends BaseDAO{

    private static List<GenreTO> genreList = null;
    private static Hashtable<String, GenreTO> genreByIdHash = null;

    public UserItemLoader() {
        super();
    }
    
    /**
     * Retrieves recommended movies for a particular customer from the Oracle Database.
     * Recommended movies have been generated using Oracle Advanced Analytics.
     * The results are found in a view ODMUSER.CUSTOMER_REC_MOVIES_BY_GENRE.
     * 
     * New recommendations will be saved in Oracle NoSQL Database for use by Movie Application.
     * @param custId
     * @throws SQLException
     */
    public void refreshCustomerRecommendations (int custId) throws SQLException {
        
        // First, delete current recommendations
        deleteUserItems(custId);
        
        // Make a connection to the database to retrieve new recommendations
        
        Connection conn = null;
        PreparedStatement stmt = null;

        int maxMovies = 25; // max of 25 recommendations for a customer
        int oldGenreId = 0;
        int genreId = 0;
        int genreRank = 0;        
        int movieRank = 0;
        int movieId   = 0;
        int numMovies = 0;
        
        ScoredGenreTO scoredGenreTO = null;
        int movieIds[] = new int[maxMovies];
        List<ScoredGenreTO> genreList = new ArrayList<ScoredGenreTO>();                
        String name = null;
        
        // Create the customer transfer object and set its customer id
        CustomerGenreTO customerGenreTO = new CustomerGenreTO();
        customerGenreTO.setId(custId);
                                                                
                
        try {
            // Connect to Oracle Database and retrieve up to 25 movies for the
            // customers top 6 genres
            conn = BaseDAO.getOraConnect();
            String sql = "SELECT CUST_ID,\n" + 
                            "  GENRE_ID,\n" + 
                            "  GENRE_RANK,\n" + 
                            "  REC_MOVIE_ID,\n" + 
                            "  MOVIE_RANK_IN_GENRE,\n" + 
                            "  RULE_CONFIDENCE\n" + 
                            "FROM ODMUSER.CUSTOMER_REC_MOVIES_BY_GENRE\n" +
                            "WHERE cust_id = ?\n" +
                            "  AND MOVIE_RANK_IN_GENRE <= ?\n" +
                            "ORDER BY CUST_ID, GENRE_RANK, MOVIE_RANK_IN_GENRE";
            
            System.out.println(sql);
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, custId);
            stmt.setInt(2, maxMovies);
            ResultSet rs = stmt.executeQuery();
            
            /*
             * Loop over the result set - which is sorted by genre and movie
             * in order of importance.
             * These will be saved into a genreList array 
             */
            while (rs.next()) {
                                               
                genreId   = rs.getInt("GENRE_ID");
                name      = this.getGenreNameById(genreId);
                genreRank = rs.getInt("GENRE_RANK");
                movieRank = rs.getInt("MOVIE_RANK_IN_GENRE") - 1; //make this 0 baseed
                movieId   = rs.getInt("REC_MOVIE_ID");

                // Group top genres
                if (oldGenreId != genreId) {
                    // if this is not the first genre - then save the list of movies for
                    // the genre
                    if (oldGenreId != 0) {
                        
                        // "resize" the movieIds array based on the actual number of 
                        // recommended movies (movieRank will contain the lowest ranked movie
                        // in the genre - aka the number of movies in the genre
                        int[] recMovieIds = new int[numMovies];
                        System.arraycopy(movieIds, 0, recMovieIds, 0, numMovies);
                        
                        // Set the top movieIds for this genre
                        scoredGenreTO.setMovieIds(recMovieIds);   
                        
                    }

                    //set values
                    scoredGenreTO = new ScoredGenreTO();
                    scoredGenreTO.setId(genreId);
                    scoredGenreTO.setName(name);
                    scoredGenreTO.setScore(genreRank);
                    
                    // Track the list of genres for this customer
                    genreList.add(scoredGenreTO);
                    
                    // Initialize the list of movies for the genre
                    movieIds  = new int[maxMovies]; 
                    numMovies = 0;
                    
                    // Reset old id
                    oldGenreId = genreId;
                }
                
                // Track the list of recommended movies for this genre
                movieIds[movieRank] = movieId;
                numMovies++;
                                                
            }
            
            // "resize" the movieIds array based on the actual number of 
            // recommended movies (movieRank will contain the lowest ranked movie
            // in the genre - aka the number of movies in the genre
            int[] recMovieIds = new int[numMovies];
            System.arraycopy(movieIds, 0, recMovieIds, 0, numMovies);
            
            // Set the recommended movies for the last genre
            scoredGenreTO.setMovieIds(movieIds);
            
            customerGenreTO.setScoredGenreList(genreList);
            
            //insert recommendation into the store
            this.insertMovies4CustomerByGenre(customerGenreTO);

        }
            
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        finally {
            if (stmt != null)
                stmt.close();
        }
               
    }
    

    public void fileReader() throws IOException {
        FileReader fr = null;
        int custId = 0;
        int oldCustId = 0;
        
        int len = 0;
        int count = 0;
        int rank = 0;
        int genresPerCustomer = 6;

        StringTokenizer st = null;
        int genreId = 0;
        int movieId = 0;
        ScoredGenreTO scoredGenreTO = null;
        int[] movieIds = null;
        List<ScoredGenreTO> genreList = new ArrayList<ScoredGenreTO>();
        CustomerGenreTO customerGenreTO = null;
        String name = null;

        try {
            fr = new FileReader(Constant.USER_ITEM_FILE_NAME);
            BufferedReader br = new BufferedReader(fr);
            String line = null;

            //Construct MovieTO from JSON object
            System.out.println("Loading User-Item data  ...");

            while ((line = br.readLine()) != null) {

                //initialize
                scoredGenreTO = new ScoredGenreTO();
                movieIds = new int[25];

                try {

                    //System.out.println("Line-> " + line);
                    st = new StringTokenizer(line, ",");
                    len = st.countTokens();

                    //There should be exactly 25 recommended movies for this
                    //genre otherwise skip
                    if (len == 53) {
                        custId = Integer.parseInt(st.nextToken());
                        genreId = Integer.parseInt(st.nextToken());
                        name = this.getGenreNameById(genreId);
                        rank = Integer.parseInt(st.nextToken());

                        //set values                        
                        scoredGenreTO.setId(genreId);
                        scoredGenreTO.setName(name);

                        //Group genres by customer IDs
                        if (oldCustId != custId) {

                            // If the old customer had less than 6 genres, insert the
                            // customers movies here
                            if (genreList.size() < genresPerCustomer && genreList.size() > 0) {
                                customerGenreTO.setScoredGenreList(genreList);
                                this.insertMovies4CustomerByGenre(customerGenreTO);
                            }
                            
                            // process the new customer
                            System.out.println("custId: " + custId);
                            customerGenreTO = new CustomerGenreTO();
                            //Add custId & List<scoreGenreTO> to customerGenreTO
                            customerGenreTO.setId(custId);
                            // customerGenreTO.setScoredGenreList(genreList);


                            //assign current customerId to oldCustomerId
                            oldCustId = custId;

                            //insert recommendation into the store
                            // this.insertMovies4CustomerByGenre(customerGenreTO);
                            //re-initialize
                            genreList = new ArrayList<ScoredGenreTO>();

                            //print only 100 unique customers
                            if (++count > 100)
                                break;


                        } //if (oldCustId != custId)
                        

                        //First 25 are movieIds
                        for (int i = 0; i < 25; i++) {
                            movieId = Integer.parseInt(st.nextToken());
                            movieIds[i] = movieId;
                        } //EOF for

                      
                        //set array to TO
                        scoredGenreTO.setMovieIds(movieIds);
                        scoredGenreTO.setScore(rank);

                        //add scoredGenreTO to list
                        genreList.add(scoredGenreTO);
                        
                        // Insert rec if the number of genres = max per customer
                        if (genreList.size() == genresPerCustomer) {
                            customerGenreTO.setScoredGenreList(genreList);
                            this.insertMovies4CustomerByGenre(customerGenreTO);
                        }

                    } //if(len==53)

                } catch (Exception e) {
                    e.getMessage();
                }

            } //EOF while


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            fr.close();
        }
    } //fileReader


    public void insertMovies4CustomerByGenre(CustomerGenreTO customerGenreTO) {

        PrimaryKey key = null;
        int count = 0;
        int genreId = 0;
        int movieId = 0;
        int custId = 0;
        String jsonTxt = null;

        List<ScoredGenreTO> genreList = null;

        if (customerGenreTO != null) {
            genreList = customerGenreTO.getScoredGenreList();
            custId = customerGenreTO.getId();

            
            if (genreList != null) {
                //sort the list in ascending order
                Collections.sort(genreList);
                
                /**
                 * Save ordered list of genres for each customer.
                 * Save jsonTxt from CustomerGenreTO.getJsonTxt() to
                 * key=/CT/custId/-genre
                 */
                Table customerTable = getKVStore().getTableAPI().getTable(CustomerDAO.TABLE_NAME+"."+CustomerDAO.CHILD_TABLE);
                
                jsonTxt = customerGenreTO.getJsonTxt();
                
                
                Row row = customerTable.createRowFromJson(jsonTxt, false);
                row.put("cid",customerGenreTO.getId());
                System.out.println("\t" + row.toJsonString(true));
                
                getKVStore().getTableAPI().put(row, null, null);
                

                Key key1=null;
                for (ScoredGenreTO genreTO : genreList) {
                    if (count++ < 6) {
                        genreId = genreTO.getId();

                        System.out.print("\t" + genreId + " " +
                                         genreTO.getScore() + "\t");
                        int[] movieIds = genreTO.getMovieIds();
                        for (int i = 0; i < movieIds.length; i++) {
                            movieId = movieIds[i];
                            
                            CustomerGenreMovieTO cgm = new CustomerGenreMovieTO();
                            cgm.setId(custId);
                            cgm.setGenreId(genreId);
                            cgm.setMovieId(movieId);
                            
                            jsonTxt = cgm.getJsonTxt();
                             row = getTable(CustomerDAO.TABLE_NAME+"."+CustomerDAO.CUSTOMER_GENRE_MOVIE_TABLE).createRowFromJson(jsonTxt, true);
                            getTableAPI().put(row, null, null);
                            
//                           key1 = KeyUtil.getCustomerGenreMovieKey(custId, genreId, movieId);
//                            BaseDAO.put(key1, Integer.toString(movieId));

                            System.out.print(movieIds[i] + " ");
                        }
                        System.out.println("");
                    } else {
                        break;
                    } //EOF if (count++ < 6)
                } //EOF for
            } //if (genreList != null)
        } //if (genreList != null)

    }
    
    /**
     * This method deletes all the previous user-item recommendations made for
     * a specific user
     */
    public void deleteUserItems(int custId) {

        PrimaryKey key = null;
        CustomerGenreTO customerGenreTO = null;
        String jsonTxt = null;
        List<ScoredGenreTO> list = null;
        int genreId = 0;


        //Get ordered list of genres recommended to user
        key = KeyUtil.getCustomerGenresKey(custId,getTable(CustomerDAO.TABLE_NAME));
        
        Row row= getTableAPI().get(key, null);

        if(row!=null){
            jsonTxt = row.toJsonString(true);
        }
        if (StringUtil.isNotEmpty(jsonTxt)) {
            customerGenreTO = new CustomerGenreTO(jsonTxt);
            list = customerGenreTO.getScoredGenreList();
            //itterate through genre list and delte all the movies under
            //that genre for the user
            for (ScoredGenreTO genreTO : list) {
                genreId = genreTO.getId();
                //Delete ordered list of genre for the customer
                BaseDAO.multiDelete(key);

                //delete all the movie recommendations for the user
            
            
            
                PrimaryKey key1 = KeyUtil.getCustomerGenreMovieKey(custId, genreId, 0,getTable(CustomerDAO.TABLE_NAME+"."+CustomerDAO.CUSTOMER_GENRE_MOVIE_TABLE));
                BaseDAO.multiDelete(key1);
                
                System.out.println("Delete genre: " + genreId + " for customer: " + custId);
            }//for (ScoredGenreTO genreTO : list)
        } //if(StringUtil.isNotEmpty()

    } //deleteUserItems    

    /**
     * This method deletes all the previous user-item recommendations made for
     * the user;
     */
    public void deleteUserItems() {

        CustomerDAO customerDAO = new CustomerDAO();
        List<CustomerTO> customerList = customerDAO.getCustomerProfiles();
        for (CustomerTO customerTO : customerList) {
            if (customerTO != null) {
                deleteUserItems(customerTO.getId());
            } //if (customerTO != null)
        } //for
    } //deleteUserItems

    /**
     * Helper method that returns GenreName for GenreId
     * @param genreId
     * @return Genre Name
     */
    private static String getGenreNameById(int genreId) {
        GenreDAO genreDAO = new GenreDAO();
        if (genreList == null) {
            genreList = genreDAO.getGenres();
            genreByIdHash = new Hashtable<String, GenreTO>();
            for (GenreTO genreTO : genreList) {
                String genreIdStr = Integer.toString(genreTO.getId());
                genreByIdHash.put(genreIdStr, genreTO);
            }
        } //if(genreList==null)
        return genreByIdHash.get(Integer.toString(genreId)).getName();
    } //getGenreNameById

    public static void main(String[] args) {
        UserItemLoader utl = new UserItemLoader();
        try {
            //delete previous list of recommended movies for the customer
            utl.deleteUserItems();
            utl.deleteUserItems(1255601);
            
            //System.exit(0);
            //load new recommendation
            utl.fileReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } //main

}//UserItemLoader

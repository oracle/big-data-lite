package oracle.demo.oow.bd.loader.analytics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;

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
import oracle.demo.oow.bd.to.CustomerGenreTO;
import oracle.demo.oow.bd.to.CustomerTO;
import oracle.demo.oow.bd.to.GenreTO;
import oracle.demo.oow.bd.to.ScoredGenreTO;
import oracle.demo.oow.bd.util.KeyUtil;

import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.Key;

public class UserItemLoader {

    private static List<GenreTO> genreList = null;
    private static Hashtable<String, GenreTO> genreByIdHash = null;

    public UserItemLoader() {
        super();
    }

    public void fileReader() throws IOException {
        FileReader fr = null;
        int custId = 0;
        int oldCustId = 0;
        int genCustId = -1;
        int len = 0;
        int count = 0;
        int rank = 0;

        StringTokenizer st = null;
        int genreId = 0;
        int movieId = 0;
        ScoredGenreTO scoredGenreTO = null;
        int[] movieIds = null;
        List<ScoredGenreTO> genreList = null;
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
                            genCustId++;
                            System.out.println("custId: " + genCustId);
                            customerGenreTO = new CustomerGenreTO();
                            //Add custId & List<scoreGenreTO> to customerGenreTO
                            customerGenreTO.setId(genCustId);
                            customerGenreTO.setScoredGenreList(genreList);


                            //assign current customerId to oldCustomerId
                            oldCustId = custId;

                            //insert recommendation into the store
                            this.insertMovies4CustomerByGenre(customerGenreTO);
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

        Key key = null;
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
                key = KeyUtil.getCustomerGenresKey(custId);
                jsonTxt = customerGenreTO.getJsonTxt();
                BaseDAO.put(key, jsonTxt);
                System.out.println("\t" + jsonTxt);


                for (ScoredGenreTO genreTO : genreList) {
                    if (count++ < 6) {
                        genreId = genreTO.getId();

                        System.out.print("\t" + genreId + " " +
                                         genreTO.getScore() + "\t");
                        int[] movieIds = genreTO.getMovieIds();
                        for (int i = 0; i < movieIds.length; i++) {
                            movieId = movieIds[i];
                            key =
						KeyUtil.getCustomerGenreMovieKey(custId, genreId, movieId);
                            BaseDAO.put(key, Integer.toString(movieId));

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
     * the user;
     */
    public void deleteUserItems() {

        Key key = null;
        CustomerGenreTO customerGenreTO = null;
        String jsonTxt = null;
        List<ScoredGenreTO> list = null;
        int genreId = 0;

        for (int custId = 0; custId < 101; custId++) {

            //Get ordered list of genres recommended to user
            key = KeyUtil.getCustomerGenresKey(custId);
            jsonTxt = BaseDAO.get(key);

            if (StringUtil.isNotEmpty(jsonTxt)) {
                customerGenreTO = new CustomerGenreTO(jsonTxt);
                list = customerGenreTO.getScoredGenreList();
                //itterate through genre list and delte all the movies under
                //that genre for the user
                for (ScoredGenreTO genreTO : list) {
                    genreId = genreTO.getId();
                    //Delete ordered list of genre for the customer
                    BaseDAO.delete(key);

                    //delete all the movie recommendations for the user
                    key = KeyUtil.getCustomerGenreMovieKey(custId, genreId, 0);
                    BaseDAO.delete(key);

                    System.out.println("Delete genre: " + genreId + " for customer: " + custId);
                }//for (ScoredGenreTO genreTO : list)
            } //if(StringUtil.isNotEmpty()
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

            //System.exit(0);
            //load new recommendation
            utl.fileReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } //main

}//UserItemLoader

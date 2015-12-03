package oracle.demo.oow.bd.loader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.dao.CustomerDAO;
import oracle.demo.oow.bd.to.CustomerTO;

import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.Version;

/**
 * 'Oracle Movieplex' application would need an authorized customer access to
 * browse the movie store. If customer do not have their user profile
 * information in the kv-store then they can not login to the application to
 * rent the movies.
 * <p>
 * This class creates user profiles information by loading the content
 * from DEMOHOME/dataparser/metadata/customer.out file into the kv-store. Each
 * row in the file represent the user-profile information as JSON string and
 * this string will be saved in the kv-store by constructing a unique {@code Key}
 * for each individual user.
 *
 */
public class CustomerProfileLoader {
    public CustomerProfileLoader() {
        super();
    }

    /**
     * This method opens the DEMOHOME/dataparser/metadata/customer.out file and
     * read row by row the whole file until EOF. Each row in the file describes
     * information regarding the user (like name of the customer, his email,
     * username, password and customer id) as a JSON string.
     * <p>
     * Serialization and deserialization of the JSON string is done in the
     * CustomerTO class. So you read the JSON string and create an object by
     * passing the string to the constructor.
     * <p>
     * If 'new CustomerTO(jsonTxt)' returns 'null' then that would mean
     * JSON string was not a valid string representation of CustomerTO but if it
     * is then you can construct CustomerTO object from the text and can call
     * getter/setter methods.
     * @param force is of boolean type. If it is true that means you would like
     * to force insert the profiles wheter they exist before or not. It should
     * be set as false if you would like to make sure profiles are not 
     * overwritten if they already exist.
     * 
     * @throws IOException If DEMOHOME/dataparser/metadata/customer.out doesn't
     * exist for any reason.
     * @see CustomerTO
     */
    public void uploadProfile(boolean force) throws IOException {
        FileReader fr = null;
        Version version = null;
        CustomerDAO custDAO = new CustomerDAO();

        try {
                        
            /**
             * Open the customer.out file for read.
             */
            fr = new FileReader(Constant.CUSTOMER_PROFILE_FILE_NAME);
            BufferedReader br = new BufferedReader(fr);
            String jsonTxt = null;
            //String password = StringUtil.getMessageDigest(Constant.DEMO_PASSWORD);
            String password = Constant.DEMO_PASSWORD;
            CustomerTO custTO = null;
            int count = 1;
            
            /**
             * Loop through the file until EOF. Save the content of each row in
             * the jsonTxt string.
             */
            while ((jsonTxt = br.readLine()) != null) {
                
                if (jsonTxt.trim().length() == 0)
                    continue;
                
                try {
                    /**
                     * Construct the CustomerTO by passing the jsonTxt as an
                     * input argument to its constructor. If the jsonTxt can be
                     * deserialized into CustomerTO then a valid object will be
                     * returned but if it fails to desiralize it for any reason
                     * the null pointer will be returned.
                     */
                    custTO = new CustomerTO(jsonTxt.trim());
                    
                    //Set password to each CutomerTO
                    custTO.setPassword(password);
                } catch (Exception e) {
                    System.out.println("ERROR: Not able to parse the json string: \t" +
                                       jsonTxt);
                }

                /**
                 * Make sure that custTO is not null, which means the jsonTxt
                 * read from the customer.out was successfully converted into
                 * CustomerTO object.
                 */
                if (custTO != null) {

                    /**
                     * Persist user-profile information into kv-store. All the
                     * Customer specific read/write operations are defined in
                     * CustomerDAO class.
                     */                    
                    version = custDAO.insertCustomerProfile(custTO, force);

                    /**
                     * If username & password doesn't exist already in the
                     * kv-store then the new profile will be created and
                     * the 'version' object would have the Version of the new
                     * key-value pair, but if the profile already exist in the
                     * store with the same credential then null will be returned
                     * and exception can be handled appropriately.
                     */
                    if (version != null) {
                        System.out.println(count++ + " " +
                                           custTO.getJsonTxt());
                    } else {
                        System.out.println("WARNING: User account for '" +
                                           custTO.getUserName() +
                                           "' couldn't be created because " +
                                           "username/password combination already exist.");
                    }


                } //EOF if

            } //EOF while
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            fr.close();
        }
    } //uploadProfile

    /**
     * To start loading the data, you need to just run this class. No additional
     * input arguments are required to run this class.
     * @param args
     */
    public static void main(String[] args) {
        CustomerProfileLoader cl = new CustomerProfileLoader();
        //if any argument passed this class that means you would like to force
        //insert the profiles. By default you don't pass any arguments.
        boolean force = args.length>0?true:false;
        
        try {
            cl.uploadProfile(force);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

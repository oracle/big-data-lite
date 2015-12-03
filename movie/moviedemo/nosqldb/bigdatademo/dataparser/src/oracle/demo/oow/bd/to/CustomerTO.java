package oracle.demo.oow.bd.to;

import java.io.IOException;

import oracle.demo.oow.bd.constant.JsonConstant;

import oracle.demo.oow.bd.util.StringUtil;


import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

/**
 * Each customer that access the Oracle MoviePlex app need to have a valid
 * profile maintained in the kv-store. Customer information like name, email,
 * username, password etc are all saved as part of their profile.
 * <p>
 * This class is a transfer object that has simple getter/setter methods to
 * access the values to/from this object. In addition this class also takes care
 * of serialization/de-serialization of data from a JSON string and into
 * CustomerTO and vice versa.
 * <p>
 *
 */
public class CustomerTO extends BaseTO implements Comparable<CustomerTO> {


    /** Unique Customer Id **/
    private int id;

    /** Customer Name **/
    private String name;

    /** Unique username with which user is going to login to the application **/
    private String userName = "";

    /** Password used to login with the username**/
    private String password = "";

    /** User email  **/
    private String email;

    /** For sorting purpose you can set score of a customer **/
    private double score;

    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to CustomerTO **/
    private ObjectNode objectNode = null;

       /**
     * No argument constructor can be used to construct an empty object.
     */
    public CustomerTO() {
        super();
    }

    /**
     * Construct CustomerTO object from a JSON text. JSON text is first
     * converted into JSON Object and then simple getter methods are used to
     * extract values for different paramters.
     * @param custJsonTxt
     */
    public CustomerTO(String custJsonTxt) {
        super();
        try {
            objectNode = super.parseJson(custJsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        this.setJsonObject(objectNode);

    }

    /**
     * If you have JSON object (instead of JSON text) then you can simply
     * pass to this constructor that will then execute getter operation to
     * extract values for different parameters.
     * @param objectNode - JSON representation of CustomerTO
     */
    public CustomerTO(ObjectNode objectNode) {
        super();
        this.setJsonObject(objectNode);
    }

    /** set customer id **/
    public void setId(int id) {
        this.id = id;
    }

    /** get customer id **/
    public int getId() {
        return id;
    }

    /** set customer name **/
    public void setName(String name) {
        this.name = name;
    }

    /** set customer name **/
    public String getName() {
        return name;
    }

    /** set customer login username **/
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** get customer login username **/
    public String getUserName() {
        return userName;
    }

    /** set customer login password **/
    public void setPassword(String password) {
        this.password = password;
    }

    /** set customer login password **/
    public String getPassword() {
        return password;
    }

    /** set customer email **/
    public void setEmail(String email) {
        this.email = email;
    }

    /** set customer email **/
    public String getEmail() {
        return email;
    }

    /** For ordering objects in collection score is used to rant them in
     * dessending order **/
    public void setScore(double score) {
        this.score = score;
    }

    /** Get customer rating score **/
    public double getScore() {
        return score;
    }

    /**
     * All the customer related information is saved into this object using
     * simple getter/setter methods and when you would like to serialize the
     * content of this object into simple string text, then you use this method.
     * <p>
     * This method construct a JSON object from the CustomerTO and then
     * uses JSONObject.toString() method to convert the object into string text.
     *
     * @return JSON String representing CustomerTO
     */
    public String getJsonTxt() {
        return this.geCustomertJson().toString();
    }

    /**
     * This method construct JSON object and put param=values for all the
     * attributes defined in the CustomerTO
     * @return JSON object representing CutomerTO
     */
    public ObjectNode geCustomertJson() {
        objectNode =super.getObjectNode();
        objectNode.put(JsonConstant.ID, this.getId());
        objectNode.put(JsonConstant.NAME, this.getName());
        objectNode.put(JsonConstant.EMAIL, this.getEmail());
        objectNode.put(JsonConstant.USERNAME, this.getUserName());
        objectNode.put(JsonConstant.PASSWORD, this.getPassword());
        return objectNode;
    }

    /**
     * This method reads the content from the input JSON object and sets the
     * value for each and every attribute defined in CustomerTO
     * @param objectNode which is JSON object consisting param=values
     */
    public void setJsonObject(ObjectNode objectNode) {
        this.objectNode = objectNode;
        String Name = objectNode.get(JsonConstant.NAME).getTextValue();
        int custId = objectNode.get(JsonConstant.ID).getIntValue();
        String email = objectNode.get(JsonConstant.EMAIL).getTextValue();
        String userName = objectNode.get(JsonConstant.USERNAME).getTextValue();
        String password = objectNode.get(JsonConstant.PASSWORD).getTextValue();

        this.setId(custId);
        this.setName(Name.trim());
        this.setEmail(email);
        this.setUserName(userName);
        this.setPassword(password);
    }


    /**
     * This method is used when one this CustomerTO is compared with other
     * object of same type. Score attribute is used to compare two objects
     * and ranked in dessending order.
     * @param custTO - Other CustomerTO object that this object will be compared
     * with
     * @return int
     */
    public int compareTo(CustomerTO custTO) {

        double score = custTO.getScore();
        //dessending order
        return (int)(score - this.score);

    }

    /**
     * Main method is used to run unit test on SerDe operations i.e. from
     * CustomerTO to JSON text and from JSON text to again CustomerTO.
     * @param args
     */
    public static void main(String[] args) {
        CustomerTO custTO = new CustomerTO();
        custTO.setEmail("adam@o.com");
        custTO.setId(4);
        custTO.setName("adam");
        custTO.setUserName("guest1");
        custTO.setPassword(StringUtil.getMessageDigest("Welcome1"));

        String jsonTxt = custTO.getJsonTxt();

        custTO = new CustomerTO(jsonTxt);
        jsonTxt = custTO.getJsonTxt();

        System.out.println(jsonTxt);

    }

    @Override
    public String toJsonString() {
        // TODO Implement this method
        return getJsonTxt();
    }
}

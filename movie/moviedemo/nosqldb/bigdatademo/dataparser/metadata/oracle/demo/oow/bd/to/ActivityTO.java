package oracle.demo.oow.bd.to;

import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

import oracle.demo.oow.bd.constant.JsonConstant;
import oracle.demo.oow.bd.pojo.ActivityType;
import oracle.demo.oow.bd.pojo.BooleanType;
import oracle.demo.oow.bd.pojo.RatingType;

public class ActivityTO {

    //defaults
    private ActivityType activity = ActivityType.LIST_MOVIES;
    private RatingType rating = RatingType.NO_RATING;
    private BooleanType recommended = BooleanType.YES;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");

    private int custId;
    private int movieId;
    private int genreId;
    private long timeStamp = 0;
    private double price;
    private int position;

    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to ActivityTO **/
    private ObjectNode objectNode = null;

    private ObjectMapper jsonMapper = new ObjectMapper();
    private JsonNodeFactory factory = JsonNodeFactory.instance;

    public ActivityTO() {
        super();
        this.setTimeStamp(System.currentTimeMillis());
    }

    public ActivityTO(ObjectNode activityNode) {
        super();
        this.setActivityJson(activityNode);

    }

    public ActivityTO(String actJsonTxt) {
        super();
        try {
            objectNode = (ObjectNode)jsonMapper.readTree(actJsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setActivityJson(objectNode);

    }

    public void setActivity(ActivityType activity) {
        this.activity = activity;
    }

    public ActivityType getActivity() {
        return activity;
    }

    public void setRating(RatingType rating) {
        this.rating = rating;
    }

    public RatingType getRating() {
        return rating;
    }

    public void setRecommended(BooleanType recommended) {
        this.recommended = recommended;
    }

    public BooleanType isRecommended() {
        return recommended;
    }

    public void setCustId(int custId) {
        this.custId = custId;
    }

    public int getCustId() {
        return custId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Helper method converts Formatted Date String into milliseconds
     * @param dateStr
     * @return
     */
    public long getTimeStamp(String dateStr) {
        Date date = null;
        long timeStamp = 0;

        try {
            date = dateFormat.parse(dateStr);
            timeStamp = date.getTime();
        } catch (ParseException e) {
            //System.out.println("Exception :" + e);
            timeStamp = Long.parseLong(dateStr);
        }

        return timeStamp;
    }

    /**
     * Helper method to return time stamp as a formatted string.
     */
    public String getFormattedTime() {
        String formattedTime = dateFormat.format(timeStamp);
        /**
         * TODO - The month and day should always be Oct-01, so that BI reporting
         * do not have gaps in them.
         */
        formattedTime = formattedTime.substring(0, 5) + "10-01" + formattedTime.substring(10, formattedTime.length());
        return formattedTime;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public int getGenreId() {
        return genreId;
    }

    public String getJsonTxt() {
        return this.getActivityJson().toString();
    }

    public ObjectNode getActivityJson() {
        this.objectNode = new ObjectNode(factory);

        objectNode.put(JsonConstant.CUST_ID, this.getCustId());
        if (this.getGenreId() > 0) {
            objectNode.put(JsonConstant.GENRE_ID, this.getGenreId());
        }
        objectNode.put(JsonConstant.MOVIE_ID, this.getMovieId());
        objectNode.put(JsonConstant.ACTIVITY, this.getActivity().getValue());
        objectNode.put(JsonConstant.RECOMMENDED, this.isRecommended().getValue());
        objectNode.put(JsonConstant.TIME, this.getFormattedTime());
        
        //Adding optional parameters
        objectNode.put(JsonConstant.PRICE, this.getPrice());
        objectNode.put(JsonConstant.RATING, this.getRating().getValue());
        objectNode.put(JsonConstant.POSITION, this.getPosition());
      
        return objectNode;
    }

    public void setActivityJson(ObjectNode objectNode) {
        this.objectNode = objectNode;
        ActivityType aType = null;

        int custId = objectNode.get(JsonConstant.CUST_ID).getIntValue();
        int movieId = objectNode.get(JsonConstant.MOVIE_ID).getIntValue();
        int activityType = objectNode.get(JsonConstant.ACTIVITY).getIntValue();
        String recommended = objectNode.get(JsonConstant.RECOMMENDED).getTextValue();
        String dateStr = objectNode.get(JsonConstant.TIME).getTextValue();
        double price = objectNode.get(JsonConstant.PRICE).getDoubleValue();
        int rating = objectNode.get(JsonConstant.RATING).getIntValue();
        int position = objectNode.get(JsonConstant.POSITION).getIntValue();
        
        //create ActivityType object
        aType = ActivityType.getType(activityType);

        //now set it to this object
        this.setCustId(custId);
        this.setMovieId(movieId);
        this.setTimeStamp(this.getTimeStamp(dateStr));
        this.setActivity(aType);
        this.setRecommended(BooleanType.getType(recommended));
        
        //Adding the optional one
        this.setPrice(price);
        this.setRating(RatingType.getType(rating));
        this.setPosition(position);
        
        //Set genreId only if it is present in the jsonObject
        if (objectNode.has(JsonConstant.GENRE_ID)) {
            int genreId = objectNode.get(JsonConstant.GENRE_ID).getIntValue();
            this.setGenreId(genreId);
        }

    } //setJsonObject

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public static void main(String[] args) {
        ActivityTO activityTO = new ActivityTO();

        System.out.println("Formated Time: " + activityTO.getFormattedTime());

        activityTO.setActivity(ActivityType.PURCHASED_MOVIE);
        activityTO.setRating(RatingType.THREE);
        activityTO.setRecommended(BooleanType.NO);

        activityTO.setCustId(1232);
        activityTO.setMovieId(9999);
        activityTO.setPrice(1.99);
        activityTO.setGenreId(2);

        //this assignment check jsonTxt to TO conversion as well as TO to jsonTxt
        String jsonTxt = activityTO.getJsonTxt();
        System.out.println("Before: " + jsonTxt);

        activityTO = new ActivityTO(jsonTxt);

        System.out.println("After: " + activityTO.getJsonTxt());

    }


}

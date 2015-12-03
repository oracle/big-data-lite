package oracle.demo.oow.bd.to;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

import oracle.demo.oow.bd.constant.JsonConstant;

import org.codehaus.jackson.node.ArrayNode;

public class CustomerGenreMovieTO extends BaseTO{

    private int id;
    private int genreId;

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getMovieId() {
        return movieId;
    }
    private int movieId;
    

    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to CustomerGenreTO **/
    private ObjectNode custGenreNode = null;




    public CustomerGenreMovieTO() {
        super();
    }

    public CustomerGenreMovieTO(String jsonTxt) {
        try {
            custGenreNode = super.parseJson(jsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } 
        this.setJsonObject(custGenreNode);

    }

    public void setJsonObject(ObjectNode custGenreNode) {
        this.custGenreNode = custGenreNode;
        
        int custId = custGenreNode.get(JsonConstant.ID).getIntValue();
        int genreId = custGenreNode.get(JsonConstant.GENRE_ID).getIntValue();
        int movieId = custGenreNode.get(JsonConstant.MOVIE_ID).getIntValue();
        this.setId(custId);
        this.setGenreId(genreId);
        this.setMovieId(movieId);

    } //setJsonObject


   
    public String getJsonTxt() {
        return this.getJsonObject().toString();
    }


 

    public ObjectNode getJsonObject() {
        this.custGenreNode = super.getObjectNode();

        ArrayNode genreArray = super.getArrayNode();
        custGenreNode.put(JsonConstant.ID, this.getId());
        custGenreNode.put(JsonConstant.GENRE_ID, this.getGenreId());
        custGenreNode.put(JsonConstant.MOVIE_ID, this.getMovieId());

        
        return custGenreNode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static void main(String[] args) {
        CustomerGenreMovieTO cgTO = new CustomerGenreMovieTO();
        cgTO.setId(1);
        cgTO.setGenreId(10);
        cgTO.setMovieId(12);


        String jsonTxt = cgTO.getJsonTxt();
        cgTO = new CustomerGenreMovieTO(jsonTxt);

        System.out.println(cgTO.getJsonTxt());

    }

    @Override
    public String toJsonString() {
        // TODO Implement this method
        return getJsonTxt();
    }
}

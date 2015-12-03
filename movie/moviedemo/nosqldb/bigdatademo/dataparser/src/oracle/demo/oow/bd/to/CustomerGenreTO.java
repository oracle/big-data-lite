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

public class CustomerGenreTO extends BaseTO{

    private int id;
    private List<ScoredGenreTO> scoredGenreList = new ArrayList<ScoredGenreTO>();

    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to CustomerGenreTO **/
    private ObjectNode custGenreNode = null;




    public CustomerGenreTO() {
        super();
    }

    public CustomerGenreTO(String jsonTxt) {
        try {
            custGenreNode = super.parseJson(jsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } 
        this.setJsonObject(custGenreNode);

    }

    public void setJsonObject(ObjectNode custGenreNode) {
        this.custGenreNode = custGenreNode;
        ScoredGenreTO scoredGenreTO = null;
        int custId = custGenreNode.get(JsonConstant.ID).getIntValue();
        Iterator genres = custGenreNode.get(JsonConstant.GENRES).iterator();
        ObjectNode scoredGenreNode = null;

        //Get all the Actors from the array
        while (genres.hasNext()) {
            scoredGenreNode = (ObjectNode)genres.next();
            scoredGenreTO = new ScoredGenreTO(scoredGenreNode);

            //Add actorTO to the actorList
            this.addScoredGenreTO(scoredGenreTO);

        } //EOF for

        this.setId(custId);

    } //setJsonObject


    public void addScoredGenreTO(ScoredGenreTO scoredGenreTO) {
        this.getScoredGenreList().add(scoredGenreTO);
    }

    public String getJsonTxt() {
        return this.getJsonObject().toString();
    }


    public void setScoredGenreList(List<ScoredGenreTO> scoredGenreList) {
        this.scoredGenreList = scoredGenreList;
    }

    public List<ScoredGenreTO> getScoredGenreList() {
        return scoredGenreList;
    }


    public ObjectNode getJsonObject() {
        this.custGenreNode = super.getObjectNode();

        ArrayNode genreArray = super.getArrayNode();
        custGenreNode.put(JsonConstant.ID, this.getId());

        for (ScoredGenreTO scoredGenreTO : this.getScoredGenreList()) {
            genreArray.add(scoredGenreTO.getJsonObject());
        } //EOF for

        //set cast to this object
        custGenreNode.put(JsonConstant.GENRES, genreArray);
        return custGenreNode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static void main(String[] args) {
        CustomerGenreTO cgTO = new CustomerGenreTO();
        cgTO.setId(1);

        ScoredGenreTO scTO = new ScoredGenreTO();
        scTO.setId(12);
        scTO.setName("Drama");
        scTO.setScore(34432);

        //Add to CutomerGenreTO
        cgTO.addScoredGenreTO(scTO);

        scTO = new ScoredGenreTO();
        scTO.setId(13);
        scTO.setName("Thriller");
        scTO.setScore(1234);

        //Add to CutomerGenreTO
        cgTO.addScoredGenreTO(scTO);

        String jsonTxt = cgTO.getJsonTxt();
        cgTO = new CustomerGenreTO(jsonTxt);

        System.out.println(cgTO.getJsonTxt());

    }

    @Override
    public String toJsonString() {
        // TODO Implement this method
        return getJsonTxt();
    }
}

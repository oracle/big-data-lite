package oracle.demo.oow.bd.to;

import java.io.IOException;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

import oracle.demo.oow.bd.constant.JsonConstant;

public class ScoredGenreTO extends BaseTO implements Comparable<ScoredGenreTO> {
    private int id;
    private int score;
    private String name;
    
    private int[] movieIds = new int[25];

    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to ScoredGenreTO **/
    private ObjectNode scoredGenreNode = null;


    
    public ScoredGenreTO() {
        super();
    }

    public ScoredGenreTO(String genreJsonTxt) {
        super();
        try {
            scoredGenreNode = super.parseJson(genreJsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } 
        this.setJsonObject(scoredGenreNode);

    }

    public ScoredGenreTO(ObjectNode scoredGenreNode) {
        super();
        this.setJsonObject(scoredGenreNode);

    }

    public String getJsonTxt() {
        return this.getJsonObject().toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }


    public int compareTo(ScoredGenreTO score) {
        double total = score.getScore();
        //ascending order
        return (int)(this.getScore() - total);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setMovieIds(int[] movieIds) {
        this.movieIds = movieIds;
    }

    public int[] getMovieIds() {
        return movieIds;
    }

    public ObjectNode getJsonObject() {
        scoredGenreNode = super.getObjectNode();
        scoredGenreNode.put(JsonConstant.ID, this.getId());
        scoredGenreNode.put(JsonConstant.NAME, this.getName());
        scoredGenreNode.put(JsonConstant.SCORE, this.getScore());
        return scoredGenreNode;
    }

    public void setJsonObject(ObjectNode scoredGenreNode) {
        this.scoredGenreNode = scoredGenreNode;
        String genreName = scoredGenreNode.get(JsonConstant.NAME).getTextValue();
        int genereId = scoredGenreNode.get(JsonConstant.ID).getIntValue();
        int score = scoredGenreNode.get(JsonConstant.SCORE).getIntValue();

        this.setId(genereId);
        this.setName(genreName.trim());
        this.setScore(score);
    }

    public static void main(String[] args) {
        ScoredGenreTO sgTO = new ScoredGenreTO();
        sgTO.setId(1);
        sgTO.setName("Drama");
        sgTO.setScore(34432);

        String jsonTxt = sgTO.getJsonTxt();
        System.out.println(jsonTxt);

    }

    @Override
    public String toJsonString() {
        // TODO Implement this method
        return getJsonTxt();
    }
}



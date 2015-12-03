package oracle.demo.oow.bd.to;

import java.io.IOException;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.constant.JsonConstant;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

public class GenreTO {
    private int id;
    private String name;

    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to GenreTO **/
    private ObjectNode objectNode = null;

    private ObjectMapper jsonMapper = new ObjectMapper();
    private JsonNodeFactory factory = JsonNodeFactory.instance;

    public GenreTO() {
        super();
    }

    public GenreTO(String genreJsonTxt) {
        super();
        try {
            objectNode = (ObjectNode)jsonMapper.readTree(genreJsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setGenreJson(objectNode);

    }

    public GenreTO(ObjectNode genreNode) {
        super();
        this.setGenreJson(genreNode);
        this.objectNode = genreNode;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    public void setGenreJson(ObjectNode objectNode) {
        this.objectNode = objectNode;
        String genreName = objectNode.get(JsonConstant.NAME).getTextValue();
        int genereId = objectNode.get(JsonConstant.ID).getIntValue();

        this.setId(genereId);
        this.setName(genreName.trim());
    }

    public String getGenreJsonTxt() {
        return this.getGenreJson().toString();
    }

    public ObjectNode getGenreJson() {
        objectNode = new ObjectNode(factory);

        objectNode.put(JsonConstant.ID, this.getId());
        objectNode.put(JsonConstant.NAME, this.getName());

        return objectNode;
    }

    public String toString() {
        String genreStr = "" + this.getId() + Constant.DELIMITER + this.getName();

        return genreStr;
    }
}

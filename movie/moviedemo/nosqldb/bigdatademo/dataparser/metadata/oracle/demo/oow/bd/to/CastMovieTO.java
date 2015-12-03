package oracle.demo.oow.bd.to;

import java.io.IOException;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

import oracle.demo.oow.bd.constant.JsonConstant;

public class CastMovieTO {
    private int id;
    private int order;
    private String character;
    private String jsonTxt = null;
    
    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to CastTO **/
    private ObjectNode castMovieNode = null;

    private ObjectMapper jsonMapper = new ObjectMapper();
    private JsonNodeFactory factory = JsonNodeFactory.instance;
    

    public CastMovieTO() {
        super();
    }

    public CastMovieTO(ObjectNode castMovieNode) {
        super();
        this.setJsonObject(castMovieNode);

    }

    public CastMovieTO(String jsonTxt) {
        super();
        try {
            castMovieNode = (ObjectNode)jsonMapper.readTree(jsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setJsonObject(castMovieNode);

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getCharacter() {
        return character;
    }


    public void setJsonTxt(String jsonTxt) {
        this.jsonTxt = jsonTxt;
    }

    public String getJsonTxt() {
        return this.getJsonObject().toString();
        
    }

    public void setJsonObject(ObjectNode castMovieNode) {
        this.castMovieNode = castMovieNode;
        int id = castMovieNode.get(JsonConstant.ID).getIntValue();
        int order = castMovieNode.get(JsonConstant.ORDER).getIntValue();
        String character = castMovieNode.get(JsonConstant.CHARACTER).getTextValue();

        this.setCharacter(character);
        this.setId(id);
        this.setOrder(order);

    }

    public ObjectNode getJsonObject() {
        ObjectNode castMovieJson = new ObjectNode(factory);        

        castMovieJson.put(JsonConstant.ID, this.getId());
        castMovieJson.put(JsonConstant.ORDER, this.getOrder());
        castMovieJson.put(JsonConstant.CHARACTER, this.getCharacter());
        
        return castMovieJson;
    }


}

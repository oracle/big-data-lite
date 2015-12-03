package oracle.demo.oow.bd.to;

import java.io.IOException;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.demo.oow.bd.constant.JsonConstant;
import oracle.demo.oow.bd.util.StringUtil;

import org.codehaus.jackson.node.ArrayNode;

public class CastTO extends BaseTO implements Comparable<CastTO>  {
    private int id = -1;
    private String name = null;
    private int order;
    private String character;

    private List<CastMovieTO> castMovieList = new ArrayList<CastMovieTO>();
    
    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to CastTO **/
    private ObjectNode castNode = null;

    

    public CastTO() {
        super();
    }

    public CastTO(String castJsonTxt) {
        super();
        try {
            castNode = super.parseJson(castJsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setCastJson(castNode);
    }

    public CastTO(ObjectNode castNode) {
        super();
        this.setCastJson(castNode);

    }

    public void setCastJson(ObjectNode castNode) {
        this.castNode = castNode;
        CastMovieTO castMovieTO = null;
        ObjectNode castMovieNode = null;
        
        String castName = castNode.get(JsonConstant.NAME).getTextValue();
        int castId = castNode.get(JsonConstant.ID).getIntValue();

        try {
            Iterator iter = castNode.get(JsonConstant.MOVIES).iterator();

            //Get all the Actors from the array
            while(iter.hasNext()) {
                castMovieNode = (ObjectNode)iter.next();
                castMovieTO = new CastMovieTO(castMovieNode);

                //Add actorTO to the actorList
                this.addCastMovieTO(castMovieTO);

            } //EOF for
        } catch (Exception e) {
            //if Movie tag do not exist then order & character should not exist
            this.order = castNode.get(JsonConstant.ORDER).getIntValue();
            this.character = castNode.get(JsonConstant.CHARACTER).getTextValue();
        }
        this.setId(castId);
        this.setName(castName);

        /*
        System.out.println(this.getOrder() + " " + this.getId() + " " +
                           this.getName() + " " + this.getCharacter() + " " +
                           this.getProfilePath());
        */
    } //setCastJson


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public ObjectNode geCastJson() {
        this.castNode = super.getObjectNode();
        ObjectNode castMovieNode = null;
        ArrayNode movieArray = super.getArrayNode();

        castNode.put(JsonConstant.ID, this.getId());
        castNode.put(JsonConstant.NAME, this.getName());

        if (StringUtil.isNotEmpty(this.character) && this.order > 0) {            
            castNode.put(JsonConstant.ORDER, this.getOrder());
            castNode.put(JsonConstant.CHARACTER, this.getCharacter());
        } else {
            for (CastMovieTO castMovieTO : this.getCastMovieList()) {
                castMovieNode = super.getObjectNode();
                
                castMovieNode.put(JsonConstant.ID, castMovieTO.getId());
                castMovieNode.put(JsonConstant.CHARACTER,
                                  castMovieTO.getCharacter());
                castMovieNode.put(JsonConstant.ORDER, castMovieTO.getOrder());

                movieArray.add(castMovieNode);
            } //EOF for

            //set cast to this object
            castNode.put(JsonConstant.MOVIES, movieArray);
            
        }
        return castNode;
    }



    public String getJsonTxt() {
        return this.geCastJson().toString();
    }

    public void setCastMovieList(List<CastMovieTO> castMovieList) {
        this.castMovieList = castMovieList;
    }

    public List<CastMovieTO> getCastMovieList() {
        return this.castMovieList;
    }

    public void addCastMovieTO(CastMovieTO castMovieTO) {
        this.castMovieList.add(castMovieTO);
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getCharacter() {
        return character;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public int compareTo(CastTO castTO) {
        
        int order = 0;
        int thisOrder = 0;

        if (castTO.getOrder() > 0 && this.getOrder() > 0) {
            order = castTO.getOrder();
            thisOrder = this.getOrder();
        }
        //assending order
        return (thisOrder - order);

    } //compareTo


    public void setCastMovieTO(CastMovieTO castMovieTO) {
        this.castMovieList = new ArrayList<CastMovieTO>();
        this.castMovieList.add(castMovieTO);
    }


    public static void main(String[] args) {

        //String castJsonTxt ="{\"id\":54768,\"name\":\"Turo Pajala\",\"character\":\"Taisto Olavi Kasurinen\",\"order\":0,\"profile_path\":null}";

        CastTO castTO = new CastTO();
        CastMovieTO castMovieTO = new CastMovieTO();

        castTO.setId(12);
        castTO.setName("Me and I");

        castMovieTO.setCharacter("Lead");
        castMovieTO.setId(1001);
        castMovieTO.setOrder(1);
        castTO.addCastMovieTO(castMovieTO);
        
        //ADD ONE MORE MOVIE
        castMovieTO = new CastMovieTO();
        castMovieTO.setCharacter("Second movie");
        castMovieTO.setId(100123);
        castMovieTO.setOrder(-1);
        
        castTO.addCastMovieTO(castMovieTO);

        String jsonTxt = castTO.getJsonTxt();


        System.out.println(jsonTxt);

    } //main


    @Override
    public String toJsonString() {
        // TODO Implement this method
        return getJsonTxt();
    }
}


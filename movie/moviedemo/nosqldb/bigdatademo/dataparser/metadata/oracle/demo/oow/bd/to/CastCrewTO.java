package oracle.demo.oow.bd.to;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;

import oracle.demo.oow.bd.constant.JsonConstant;

import org.codehaus.jackson.node.ArrayNode;

public class CastCrewTO {

    private List<CastTO> castList = new ArrayList<CastTO>();
    private List<CrewTO> crewList = new ArrayList<CrewTO>();
    Hashtable<String, String> uniqueCastCrew = new Hashtable<String, String>();    
    
    private int movieId = -1;
    
    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to CustomerTO **/
    private ObjectNode castCrewNode = null;

    private ObjectMapper jsonMapper = new ObjectMapper();
    private JsonNodeFactory factory = JsonNodeFactory.instance;
    

    public CastCrewTO() {
        super();
    }

    public CastCrewTO(String jsonTxt) {
        super();
        try {
            castCrewNode = (ObjectNode)jsonMapper.readTree(jsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setJsonObject(castCrewNode);
    }

    public CastCrewTO(ObjectNode castCrewNode) {
        this.setJsonObject(castCrewNode);
    }

    public void setCastList(List<CastTO> castList) {

        this.castList = castList;
    }

    public void addCastTO(CastTO castTO) {

        String castIdStr = String.valueOf(castTO.getId());

        if (!uniqueCastCrew.containsKey(castIdStr)) {
            this.castList.add(castTO);
        } //if
        uniqueCastCrew.put(castIdStr, castIdStr);

    } //addCastTO

    public List<CastTO> getCastList() {
        return castList;
    }

    public void setCrewList(List<CrewTO> crewList) {
        this.crewList = crewList;
    }

    public void addCrewTO(CrewTO crewTO) {
        String crewIdStr = String.valueOf(crewTO.getId());

        if (!uniqueCastCrew.containsKey(crewIdStr)) {
            this.crewList.add(crewTO);
        } //if
        uniqueCastCrew.put(crewIdStr, crewIdStr);

    }

    public List<CrewTO> getCrewList() {
        return crewList;
    }

    public void setJsonObject(ObjectNode castCrewNode) {
        CastTO actorTO = null;
        CrewTO crewTO = null;

        this.castCrewNode = castCrewNode;

        int movieId = castCrewNode.get("id").getIntValue();
        Iterator casts = castCrewNode.get(JsonConstant.CAST).iterator();
        Iterator crews = castCrewNode.get(JsonConstant.CREW).iterator();

        ObjectNode node = null;

        //Get all the Actors from the array
        while(casts.hasNext()) {
            //get cast information from the array
            node = (ObjectNode)casts.next();
            actorTO = new CastTO(node);

            //Add actorTO to the actorList
            this.addCastTO(actorTO);

        } //EOF for

        //Get all the Crew from the array
        while(crews.hasNext()) {
            //get crew information from the array
            node = (ObjectNode)crews.next();
            crewTO = new CrewTO(node);

            //Add crewTO to the crewList
            this.addCrewTO(crewTO);

        } //EOF for

        //Add to this object
        this.setMovieId(movieId);
    }

    public ObjectNode getJsonObject() {
        castCrewNode = new ObjectNode(factory);
        ObjectNode node = null;
        ArrayNode castArray = new ArrayNode(factory);
        ArrayNode crewArray = new ArrayNode(factory);

        castCrewNode.put(JsonConstant.ID, this.getMovieId());

        if (castList != null) {
            for (CastTO castTO : castList) {
                node = castTO.geCastJson();
                castArray.add(node);
            } //EOF for
        }
        if (crewList != null) {
            for (CrewTO crewTO : crewList) {
                node = crewTO.getCrewJson();
                crewArray.add(node);
            } //EOF for
        }
        //set cast to this object
        castCrewNode.put(JsonConstant.CAST, castArray);

        //set crew to this object
        castCrewNode.put(JsonConstant.CREW, crewArray);
        return castCrewNode;
    }


    public String getJsonTxt() {
        return this.getJsonObject().toString();

    } //getJsonTxt

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getMovieId() {
        return movieId;
    }

    public static void main(String[] args) {

        CastCrewTO ccTO = new CastCrewTO();
        ccTO.setMovieId(1234);

        CastTO castTO = null;
        CrewTO crewTO = null;

        castTO = new CastTO();
        castTO.setId(12);
        castTO.setName("My Name");
        castTO.setOrder(1);
        castTO.setCharacter("Foo");
        ccTO.addCastTO(castTO);

        castTO = new CastTO();
        castTO.setId(14);
        castTO.setName("Your Name");
        castTO.setOrder(2);
        castTO.setCharacter("Bar");
        ccTO.addCastTO(castTO);

        crewTO = new CrewTO();
        crewTO.setId(32);
        crewTO.setJob(JsonConstant.DIRECTOR);
        crewTO.setName("Steven Jaxon");
        ccTO.addCrewTO(crewTO);

        String jsonTxt = ccTO.getJsonTxt();
        System.out.println(jsonTxt);
        ccTO = new CastCrewTO(jsonTxt);

        System.out.println(ccTO.getJsonTxt());

        jsonTxt =
                "{\"id\":857,\"cast\":[{\"id\":558298,\"name\":\"Tom Hanks\",\"order\":1,\"character\":\"Capt. John H. Miller\"}],\"crew\":[]}";

        ccTO = new CastCrewTO(jsonTxt);
        System.out.println(ccTO.getJsonTxt());

    } //main
}

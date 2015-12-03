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

public class CrewTO extends BaseTO{
    private int id = -1;
    private String name = null;
    private String department = null;
    private String job = null;

    private List<String> movieList = new ArrayList<String>();

    /** For SerDe purpose JSON object is used to write data into json text and
     * from json text to CrewTO **/
    private ObjectNode crewNode = null;

    public CrewTO() {
        super();

    }

    public CrewTO(ObjectNode crewNode) {
        super();
        this.setCrewJson(crewNode);
    }

    public CrewTO(String jsonTxt) {
        super();
        try {
            crewNode = super.parseJson(jsonTxt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setCrewJson(crewNode);
    }

    public String getJsonTxt() {
        return this.getCrewJson().toString();
    }

    public void setCrewJson(ObjectNode crewNode) {
        this.crewNode = crewNode;
        ObjectNode movieNode = null;
        int movieId;

        String crewName = crewNode.get(JsonConstant.NAME).getTextValue();
        String job = crewNode.get(JsonConstant.JOB).getTextValue();
        int crewId = crewNode.get(JsonConstant.ID).getIntValue();

        try {
            Iterator movies = crewNode.get(JsonConstant.MOVIES).iterator();

            //Get all the Actors from the array
            while (movies.hasNext()) {
                movieNode = (ObjectNode)movies.next();
                movieId = movieNode.get(JsonConstant.ID).getIntValue();

                //Add actorTO to the actorList
                this.addMovieId(movieId);

            } //EOF for
        } catch (Exception e) {
            //If MOVIE array do not exist then that is still okay
            e.getMessage();
        }

        this.setId(crewId);
        this.setName(crewName);
        this.setJob(job);

    }

    public ObjectNode getCrewJson() {
        this.crewNode = super.getObjectNode();

        ObjectNode movieNode = null;
        ArrayNode movieArray = super.getArrayNode();

        crewNode.put(JsonConstant.ID, this.getId());
        crewNode.put(JsonConstant.NAME, this.getName());
        crewNode.put(JsonConstant.JOB, this.getJob());

        if (getMovieList() != null && getMovieList().size() > 0) {
            for (String movieId : this.getMovieList()) {
                movieNode = super.getObjectNode();
                movieNode.put(JsonConstant.ID, Integer.parseInt(movieId));
                movieArray.add(movieNode);
            } //EOF for

            //set cast to this object
            crewNode.put(JsonConstant.MOVIES, movieArray);
        }
        return crewNode;
    }

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

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getJob() {
        return job;
    }

    public List<String> getMovieList() {
        return this.movieList;
    }

    public void setMovieList(List<String> movieList) {
        this.movieList = movieList;
    }

    public void addMovieId(int movieId) {
        this.movieList.add(Integer.toString(movieId));
    }

    public void setMovieId(int movieId) {
        //Create a new List to hold just the above one movieId
        this.movieList = new ArrayList<String>();
        this.movieList.add(Integer.toString(movieId));
    }

    public static void main(String[] args) {

        CrewTO crewTO = new CrewTO();

        crewTO.setId(12);
        crewTO.setName("AJ");
        crewTO.setJob(JsonConstant.DIRECTOR);

        crewTO.setMovieList(null);
        //crewTO.addMovieId(123233);
        //crewTO.addMovieId(123234);

        //Create jsonTxt
        String jsonTxt = crewTO.getJsonTxt();
        System.out.println("1: " + jsonTxt);

        //From JSON create CrewTO
        crewTO = new CrewTO(jsonTxt);
        //And now display
        System.out.println("2: " + jsonTxt);


    } //main

    @Override
    public String toJsonString() {
        // TODO Implement this method
        return getJsonTxt();
    }
}

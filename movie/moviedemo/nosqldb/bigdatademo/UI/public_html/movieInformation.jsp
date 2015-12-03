<%@ page import="oracle.demo.oow.bd.dao.MovieDAO" %>
<%@ page import="oracle.demo.oow.bd.dao.CustomerDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="oracle.demo.oow.bd.to.MovieTO" %>
<%@ page import="oracle.demo.oow.bd.to.CastTO" %>
<%@ page import="oracle.demo.oow.bd.to.GenreTO" %>
<%@ page import="oracle.demo.oow.bd.to.CrewTO" %>
<%@ page import="oracle.demo.oow.bd.constant.Constant" %>
<%@ page import="oracle.kv.Key" %>
<%@ page import="oracle.demo.oow.bd.util.KeyUtil" %>
<%@ page import="oracle.demo.oow.bd.to.ActivityTO" %>
<%@ page import="oracle.demo.oow.bd.dao.BaseDAO" %>
<%@ page import="oracle.demo.oow.bd.pojo.ActivityType" %>
<%@ page import="oracle.demo.oow.bd.dao.ActivityDAO" %>
<%@ page import="oracle.demo.oow.bd.dao.CustomerRatingDAO" %>

<%
String movieId = request.getParameter("id");
String show = request.getParameter("show");

int userId = (Integer)request.getSession().getAttribute("userId");
boolean useMoviePosters = (Boolean)request.getSession().getAttribute("useMoviePosters");	

if (movieId != null){

  CustomerDAO cdao = new CustomerDAO();
  ActivityTO activityTO = cdao.getMovieRating(userId,Integer.parseInt(movieId));
  if (activityTO != null && activityTO.getRating()!=null){}
    else
      activityTO = null;
  
  /////// ACTIVITY ////////
  ActivityTO activityTO2 = new ActivityTO();
  activityTO2.setActivity(ActivityType.BROWSED_MOVIE);
  activityTO2.setMovieId(Integer.parseInt(movieId));
  activityTO2.setCustId(userId);
  String catId = request.getParameter("catid");
  if (catId != null){
    int categoryId = Integer.parseInt(catId);
    activityTO2.setGenreId(categoryId);
  }
  
  
  //activityTO.setPrice(1.99);
  ActivityDAO aDAO = new ActivityDAO();
  aDAO.insertCustomerActivity(activityTO2);
  
  CustomerRatingDAO rating = new CustomerRatingDAO();
  rating.insertCustomerRating(userId, Integer.parseInt(movieId), 2);
  
  MovieDAO movieDAO = new MovieDAO();
  MovieTO movieTO = movieDAO.getMovieById(movieId);
  String overview = movieTO.getOverview();
  
  if(overview.length()>=1000){
    overview = overview.substring(0,1000) + "...";
  }
  
  List<GenreTO> genreTO = movieTO.getGenres();
  String genres = "";
  for (int i=0; i < genreTO.size(); i++){
      GenreTO gen = genreTO.get(i);
      genres = genres + gen.getName() + ", ";
  }
  genres = genres.substring(0, genres.length()-2);
  String cast = "";
  String director = "";
  String writer = "";
  String shortActors = "";
  List<CrewTO> crews = movieTO.getCastCrewTO().getCrewList();
  List<CastTO> actors = movieTO.getCastCrewTO().getCastList();
  if (actors != null){
    for (int i=0; i < actors.size(); i++){
        CastTO act = actors.get(i);
        cast = cast + "<a href="+ act.getId() +"> " + act.getName() + "</a>, ";
        if (i<6) shortActors = shortActors + "<a type='actor' href="+ act.getId() +"> " + act.getName() + "</a>, ";
    }
  }
  if (crews != null){
    for (int i=0; i < crews.size(); i++){
        CrewTO c = crews.get(i);
        if (c.getJob().equalsIgnoreCase("Director")){director = director + "<a type='crew' href="+ c.getId() +"> " + c.getName() + "</a>, "; }
        if (c.getJob().equalsIgnoreCase("Writer")){writer = writer + "<a type='crew' href="+ c.getId() +"> " + c.getName() + "</a>, "; }
        cast = cast + c.getName() + ", ";
    }
  }
  if (!cast.equalsIgnoreCase(""))cast = cast.substring(0, cast.length()-2);
  if (!director.equalsIgnoreCase(""))director = director.substring(0, director.length()-2);
  if (!writer.equalsIgnoreCase(""))writer = writer.substring(0, writer.length()-2);
  if (!shortActors.equalsIgnoreCase(""))shortActors = shortActors.substring(0, shortActors.length()-2);
%>

<html>
  <body>
  <div class="information" id="<%= movieId %>">
    <div class="title">
    <span class="movieTitle" ><%= movieTO.getTitle()%>  <span class="movieRelease"><%='('+movieTO.getDate().substring(0,4)+')' %> </span></span><br>
    </div>
    <div class="clear"></div>
    <div style="margin-top: 25px;">
        <div class="leftInfoPanel">
          <div style="font-weight:bolder;">Rent for $<%= movieTO.getPrice()  %></div>
          
            <% if (movieTO.getPosterPath().equalsIgnoreCase("") || !useMoviePosters) { %>
            <div class="videoContainer" style="position:relative;">
            <img width="184px" height="276px"
                 src="images/genericfilm.png"
                 name="genericfilm.png"
                 style="display: block; " class="reflected"></img>
            <div class="tit3" style="top:150px;left:-20px;padding:5px;display:table;width:140px;height:90px;margin-top:0px;">
              <div style="display:table-cell;vertical-align:middle;"><%= movieTO.getTitle() %></div>
            </div>
            
             
            <% } else { %>
            <div class="videoContainer">
            <img width="184px" height="276px"
                 src="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>"
                 name="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>"
                 style="display: block; " class="reflected"></img>
                                     
            <% } %>
            
               <br>
            <a href="#movie" class="playButton" movieid="<%= movieId %>"></a>
            </div>
            <div>
            <%
            double avg;
            if(activityTO!=null){
              avg = activityTO.getRating().getValue();
            }else
              avg =  movieTO.getPopularity()/2;
            %>
              <input name="star1" type="radio" class="starMovie star required" value="1" <% if (avg > 0 && avg <= 1.5 ) out.print("checked='checked'"); %>/>
              <input name="star1" type="radio" class="starMovie star" value="2" <% if (avg > 1.5 && avg <= 2.5 ) out.print("checked='checked'"); %>/>
              <input name="star1" type="radio" class="starMovie star" value="3" <% if (avg > 2.5 && avg <= 3.5 ) out.print("checked='checked'"); %>/>
              <input name="star1" type="radio" class="starMovie star" value="4" <% if (avg > 3.5 && avg <= 4.5 ) out.print("checked='checked'"); %> />
              <input name="star1" type="radio" class="starMovie star" value="5" <% if (avg > 4.5) out.print("checked='checked'"); %> />
            </div>
          </div>
          
          <% if(activityTO!=null) {%>
              <script type="text/javascript">setRating(true, "starMovie" ,<%=movieId%>);</script>
          <% }else { %>
              <script type="text/javascript">setRating(false, "starMovie" ,<%=movieId%>);</script>
          <% }%>
          
          <div class="rightInfoPanel" >
            <div><label>Overview: </label> <span><%= overview %></span></div>
            <div><label>Cast: </label> <span><%= shortActors %></span></div>
            <div><label>Director: </label> <span><%= director %></span></div>
            <div><label>Writer: </label> <span><%= writer %></span></div>
          </div>
        </div>
    </div>
    <div class="clear"></div>
   </div>
   <% if(show.equalsIgnoreCase("1")) { %>
    <div id="moviesByCast" class="moviesByCast"></div>
    <% } %>
   </body>
</html>

<% } %>
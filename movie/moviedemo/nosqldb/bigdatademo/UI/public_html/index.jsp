<!DOCTYPE html>
<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page pageEncoding="UTF-8" %>
<%@ page import="oracle.demo.oow.bd.dao.GenreDAO" %>
<%@ page import="oracle.demo.oow.bd.dao.CustomerDAO" %>
<%@ page import="oracle.demo.oow.bd.dao.ActivityDAO" %>
<%@ page import="oracle.demo.oow.bd.to.GenreTO" %>
<%@ page import="oracle.demo.oow.bd.to.GenreMovieTO" %>
<%@ page import="oracle.demo.oow.bd.to.ActivityTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="oracle.demo.oow.bd.to.MovieTO" %>
<%@ page import="oracle.demo.oow.bd.constant.Constant" %>
<%@ page import="oracle.demo.oow.bd.util.YouTubeUtil" %>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <link href="css/style.css" rel="stylesheet" type="text/css"></link>
    <link href="css/jquery.rating.css" rel="stylesheet" type="text/css"></link>
    <link href="css/dd.css" rel="stylesheet" type="text/css"></link>
    <script type="text/javascript" src="js/jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="js/jquery.jcarousel.min.js"></script>
    <script type="text/javascript" src="js/jquery.rating.pack.js"></script>
    <script type="text/javascript" src="js/jquery.dd.js"></script>
    <script type="text/javascript" src="js/scripts.js"></script>
    <script type="text/javascript">var cats = new Array();</script>
    <title>index</title>
  </head>
  <body>

  <%
    int userId = (Integer)request.getSession().getAttribute("userId");
    boolean useMoviePosters = (Boolean)request.getSession().getAttribute("useMoviePosters");
    
    CustomerDAO customerDAO = new CustomerDAO();
    List<GenreMovieTO> genreMovieList = null;
    ActivityDAO aDAO = new ActivityDAO();
    int movN = 0;
    
    GenreDAO genreDAO = new GenreDAO();
    List<GenreTO> genresShow =  genreDAO.getGenres();
  %>
  <div id="loading"><img src="images/wait.gif" alt="Loading..."></img></div>
  <div class="header">
    <div id="logo"><img src="images/movielogo_gray_index2.PNG" alt="Logo"></img></div>
    <div class="customerInformation">
    Welcome <%= (String)request.getSession().getAttribute("name") %> | <a href="logout">Logout</a>
    </div>
  </div>
  
  <div id="main">
    
    <div class="wbox dark">
      <div id="basedMood">
      <div class="titbar">
        <div class="tit">Movies based on Mood <input type="button" value="Get Similar Mood Movies" id="getMoviesMood"></div>
      </div>
      <div class="slidebox" id="slideboxMood">
      </div>
      </div>
      </div>
    
    
    
    
    <div id="firstPart">
    <%
      ///////////////////////CONTINUE WATCHING///////////////////////
      List<MovieTO> movieTOList1 = null;
      movieTOList1 = aDAO.getCustomerCurrentWatchList(userId);
      
      if (movieTOList1.size()>0){
    %>
    <div class="wbox dark">
      <div id="continueWatching">
      <div class="titbar">
        <div class="tit">Continue Watching</div>
      </div>
      <script type="text/javascript">$(document).ready(function(){createCarousel("CW");});</script>
      
      <div class="slidebox">
        <% if(movieTOList1.size() > 5) { %>
        <a id="prevSlideCW" class="arrow lef" ><span></span></a>
        <a id="nextSlideCW" class="arrow rig"><span></span></a>
        <% } %>
        <div class="box">
          <div class="in jspScrollable" style="overflow: hidden; padding: 0px; width: 782px; outline: none; height:230px;">
          <div class="jspContainer" style="width: 782px; height: 230px; ">
            <div class="rightpane"></div><div class="leftpane"></div>
            <div class="jspPane" style="padding: 0px; width: 782px; left: 0px; ">
                <div class="boxes">
                <ul id="mycarouselCW" class="jcarousel-skin-name">
                 <%
                    for (int i =0; i < movieTOList1.size(); i++){
                        MovieTO movieTO = movieTOList1.get(i);
                        %>
                     <li style="height:240px;" class="item-box jcarousel-item-<%= i %>" movieid="<%= movieTO.getId() %>" rel="movieInformation.jsp?id=<%= movieTO.getId() %>">
                       <a href="#">
                      <span class="img loaded">
                          <% if (movieTO.getPosterPath().equalsIgnoreCase("") || !useMoviePosters) { %>
                          <div class="" style="height: 184px; overflow: hidden; position: relative; " title="<%= movieTO.getTitle() %>">
                          <img src="images/genericfilm.png" name="genericfilm.png" align="display: block; " class="reflected">
                          <div class="tit3"><%= movieTO.getTitle() %></div>                          
                          
                          <% } else { %>
                          <div class="" style="height: 184px; overflow: hidden; ">
                          <img src="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" name="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" style="display: block; " class="reflected">

                          <% } %>
                          </div>
                      </span>
                      <br><span class="tit2"><%= movieTO.getTitle() %></span>
                      <%
                      ActivityTO activityTO = new ActivityDAO().getActivityTO(userId, movieTO.getId());
                      int position = 0;
                      int total = 226;
                      if(activityTO!=null) position = activityTO.getPosition();
                      int actual = position*100/total;
                      String youtubeKey = YouTubeUtil.getKey(movieTO.getId()); 
                      %>
                      <div class="meter" yt="<%=youtubeKey%>"><span style="width: <%=actual%>%"></span></div>
                    </a>
                  </li>
                <% } %>
                </ul>
                </div>
              </div>
            </div>
          </div>
          </div>
          </div>
        <div class="clear"></div>
      </div>
      </div>
    <% }%>
    
    <%
      ///////////////////////RECENTLY BROWSED///////////////////////
      movieTOList1 = aDAO.getCustomerBrowseList(userId);
      
      if (movieTOList1.size()>0){
    %>
    <div class="wbox dark">
      <div id="continueWatching">
      <div class="titbar">
        <div class="tit">Recently Browsed</div>
      </div>
      <script type="text/javascript">$(document).ready(function(){createCarousel("RW");});</script>
      
      <div class="slidebox">
         <% if(movieTOList1.size() > 5) { %>
        <a id="prevSlideRW" class="arrow lef" ><span></span></a>
        <a id="nextSlideRW" class="arrow rig"><span></span></a>
        <% } %>
        <div class="box">
          <div class="in jspScrollable" style="overflow: hidden; padding: 0px; width: 782px; outline: none; ">
          <div class="jspContainer" style="width: 782px; height: 216px; ">
            <div class="rightpane"></div><div class="leftpane"></div>
            <div class="jspPane" style="padding: 0px; width: 782px; left: 0px; ">
                <div class="boxes">
                <ul id="mycarouselRW" class="jcarousel-skin-name">
                 <%
                    for (int i =0; i < movieTOList1.size(); i++){
                        MovieTO movieTO = movieTOList1.get(i);
                        %>
                     <li class="item-box jcarousel-item-<%= i %>"  rel="movieInformation.jsp?id=<%= movieTO.getId() %>">
                       <a href="#">
                      <span class="img loaded">
                          <% if (movieTO.getPosterPath().equalsIgnoreCase("") || !useMoviePosters) { %>
                          <div class="" style="height: 184px; overflow: hidden; position: relative; " title="<%= movieTO.getTitle() %>">
                          <img src="images/genericfilm.png" name="genericfilm.png" align="display: block; " class="reflected">
                          <div class="tit3"><%= movieTO.getTitle() %></div>                          
                          
                          <% } else { %>
                          <div class="" style="height: 184px; overflow: hidden; ">
                          <img src="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" name="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" style="display: block; " class="reflected">

                          <% } %>
                          </div>
                      </span>
                      <br><span class="tit2"><%= movieTO.getTitle() %></span><br>
                    </a>
                  </li>
                <% } %>
                </ul>
                </div>
              </div>
            </div>
          </div>
          </div>
          </div>
        <div class="clear"></div>
      </div>
      </div>
    <% }%>
    </div>
    
    <div class="space"></div>
    
    <div class="clear"></div>
    <div class="titbar"><div class="tit titMain">Top Movies for <%= (String)request.getSession().getAttribute("name") %> by Genre</div></div>
    <% 
    genreMovieList = customerDAO.getMovies4Customer( userId,30,6);
    for (GenreMovieTO genreMovieTO : genreMovieList) {
        GenreTO genreTO = genreMovieTO.getGenreTO();
        int categoryId = genreTO.getId();
        String categoryName = genreTO.getName();
     %>
    <div class="wbox dark">
      <script type="text/javascript">cats[<%= movN %>] = "<%= categoryId %>";</script>
      <% movN++; %>
      <div id="category<%= categoryId %>_row">
      <div class="titbar">
        <div class="tit"><%= categoryName %></div>
      </div>
      
      <div class="slidebox">
        <a id="prevSlide<%= categoryId %>" class="arrow lef" ><span></span></a>
        <a id="nextSlide<%= categoryId %>" class="arrow rig"><span></span></a>
        <div class="box">
          <div class="in jspScrollable" style="overflow: hidden; padding: 0px; width: 782px; outline: none; ">
          <div class="jspContainer" style="width: 782px; height: 216px; ">
            <div class="rightpane"></div><div class="leftpane"></div>
            <div class="jspPane" style="padding: 0px; width: 782px; left: 0px; ">
                <div class="boxes">
                <ul id="mycarousel<%= categoryId %>" class="jcarousel-skin-name">
                 <%
                    List<MovieTO> movieTOListEXT = null;
                    
                    movieTOListEXT = customerDAO.getMovies4CustomerByGenre(userId,categoryId);
                    for (int i =0; i < movieTOListEXT.size(); i++){
                        MovieTO movieTO = movieTOListEXT.get(i);
                        
                        %>
                     <li class="item-box jcarousel-item-<%= i %>"  rel="movieInformation.jsp?id=<%= movieTO.getId() %>&catid=<%= categoryId %>">
                       <a href="#">
                      <span class="img loaded">                        
                          <% if (movieTO.getPosterPath().equalsIgnoreCase("") || !useMoviePosters) { %>
                          <div class="" style="height: 184px; overflow: hidden; position: relative; " title="<%= movieTO.getTitle() %>">
                          <img src="images/genericfilm.png" name="genericfilm.png" align="display: block; " class="reflected">
                          <div class="tit3"><%= movieTO.getTitle() %></div>                          
                          
                          <% } else { %>
                          <div class="" style="height: 184px; overflow: hidden; ">
                          <img src="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" name="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" style="display: block; " class="reflected">

                          <% } %>
                          </div>
                      </span>
                      <br><span class="tit2"><%= movieTO.getTitle() %></span><br>
                    </a>
                  </li>
                <% } %>
                </ul>
                </div>
              </div>
            </div>
          </div>
          </div>
          </div>
        <div class="clear"></div>
      </div>
      
      </div>
    <%  }
    %>
    
    <div class="space"></div>
    
    <div id="lastPart">
     <%
      ///////////////////////WHAT OTHERS WATHCHING///////////////////////
      movieTOList1 = aDAO.getCommonPlayList();
      if (movieTOList1.size()>0){
    %>
    <div class="wbox dark">
      <div id="continueWatching">
      <div class="titbar">
        <div class="tit">What Others Watching</div>
      </div>
      <script type="text/javascript">$(document).ready(function(){createCarousel("CP");});</script>
      
      <div class="slidebox">
        <% if(movieTOList1.size() > 5) { %>
        <a id="prevSlideCP" class="arrow lef" ><span></span></a>
        <a id="nextSlideCP" class="arrow rig"><span></span></a>
        <% } %>
        <div class="box">
          <div class="in jspScrollable" style="overflow: hidden; padding: 0px; width: 782px; outline: none; ">
          <div class="jspContainer" style="width: 782px; height: 216px; ">
            <div class="rightpane"></div><div class="leftpane"></div>
            <div class="jspPane" style="padding: 0px; width: 782px; left: 0px; ">
                <div class="boxes">
                <ul id="mycarouselCP" class="jcarousel-skin-name">
                 <%
                    for (int i =0; i < movieTOList1.size(); i++){
                        MovieTO movieTO = movieTOList1.get(i);
                        %>
                     <li class="item-box jcarousel-item-<%= i %>"  rel="movieInformation.jsp?id=<%= movieTO.getId() %>">
                       <a href="#">
                      <span class="img loaded">
                          <% if (movieTO.getPosterPath().equalsIgnoreCase("") || !useMoviePosters) { %>
                          <div class="" style="height: 184px; overflow: hidden; position: relative; " title="<%= movieTO.getTitle() %>">
                          <img src="images/genericfilm.png" name="genericfilm.png" align="display: block; " class="reflected">
                          <div class="tit3"><%= movieTO.getTitle() %></div>                          
                          
                          <% } else { %>
                          <div class="" style="height: 184px; overflow: hidden; ">
                          <img src="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" name="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" style="display: block; " class="reflected">

                          <% } %>
                          </div>
                      </span>
                      <br><span class="tit2"><%= movieTO.getTitle() %></span><br>
                    </a>
                  </li>
                <% } %>
                </ul>
                </div>
              </div>
            </div>
          </div>
          </div>
          </div>
        <div class="clear"></div>
      </div>
      </div>
    <% }%>
    
    
      <%
      ///////////////////////PRIVIOUSLY WATCHED//////////////////////
      movieTOList1 = aDAO.getCustomerHistoricWatchList(userId);
      if (movieTOList1.size()>0){
    %>
    <div class="wbox dark">
      <div id="continueWatching">
      <div class="titbar">
        <div class="tit">Previously Watched</div>
      </div>
      <script type="text/javascript">$(document).ready(function(){createCarousel("PW");});</script>
      
      <div class="slidebox">
        <% if(movieTOList1.size() > 5) { %>
        <a id="prevSlidePW" class="arrow lef" ><span></span></a>
        <a id="nextSlidePW" class="arrow rig"><span></span></a>
        <% } %>
        <div class="box">
          <div class="in jspScrollable" style="overflow: hidden; padding: 0px; width: 782px; outline: none; height:230px; ">
          <div class="jspContainer" style="width: 782px; height: 230px; ">
            <div class="rightpane"></div><div class="leftpane"></div>
            <div class="jspPane" style="padding: 0px; width: 782px; left: 0px; ">
                <div class="boxes">
                <ul id="mycarouselPW" class="jcarousel-skin-name">
                 <%
                    for (int i =0; i < movieTOList1.size(); i++){
                        MovieTO movieTO = movieTOList1.get(i);
                        %>
                     <li style="height:240px;" class="item-box jcarousel-item-<%= i %>"  rel="movieInformation.jsp?id=<%= movieTO.getId() %>">
                       <a href="popup">
                      <span class="img loaded">
                          <% if (movieTO.getPosterPath().equalsIgnoreCase("") || !useMoviePosters) { %>
                          <div class="" style="height: 184px; overflow: hidden; position: relative; " title="<%= movieTO.getTitle() %>">
                          <img src="images/genericfilm.png" name="genericfilm.png" align="display: block; " class="reflected">
                          <div class="tit3"><%= movieTO.getTitle() %></div>                          
                          
                          <% } else { %>
                          <div class="" style="height: 184px; overflow: hidden; ">
                          <img src="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" name="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>" style="display: block; " class="reflected">

                          <% } %>
                          </div>
                      </span>
                      <br><span class="tit2"><%= movieTO.getTitle() %></span>
                    </a>
                    <% 
                    ActivityTO activityTO = customerDAO.getMovieRating(userId,movieTO.getId());
                    double avg = 1;
                    boolean rated = false;
                    if(activityTO != null && activityTO.getRating()!=null){
                      avg = activityTO.getRating().getValue();
                      rated = true;
                    }else
                      avg =  movieTO.getPopularity()/2;
                    %>
                        <input name="star-pw<%=i%>" type="radio" class="star-pw<%=i%> star required" value="1" <% if (avg > 0 && avg <= 1.5 ) out.print("checked='checked'"); %>/>
                        <input name="star-pw<%=i%>" type="radio" class="star-pw<%=i%> star" value="2" <% if (avg > 1.5 && avg <= 2.5 ) out.print("checked='checked'"); %>/>
                        <input name="star-pw<%=i%>" type="radio" class="star-pw<%=i%> star" value="3" <% if (avg > 2.5 && avg <= 3.5 ) out.print("checked='checked'"); %>/>
                        <input name="star-pw<%=i%>" type="radio" class="star-pw<%=i%> star" value="4" <% if (avg > 3.5 && avg <= 4.5 ) out.print("checked='checked'"); %> />
                        <input name="star-pw<%=i%>" type="radio" class="star-pw<%=i%> star" value="5" <% if (avg > 4.5) out.print("checked='checked'"); %> />
                        <script type="text/javascript">setRating(<%=rated%>,"star-pw<%=i%>",<%=movieTO.getId()%>);</script>

                    </li>
                <% } %>
                </ul>
                </div>
              </div>
            </div>
          </div>
          </div>
          </div>
        <div class="clear"></div>
      </div>
      </div>
    <% }%>
    </div>
    
    
  </div>
  
  <div id="movie" class="movie">
    <a href="#" class="close">
     <img src="images/close.png" id="closeMovie"/>
    </a>
    <div style="clear:both;"></div>
    <div id="player-content">
      <div id="player">
      </div>
      <div id="player-script"></div>
    </div>
  </div>
  <div id="movie-information" class="movie-information">
    <a href="#" class="close">
     <img src="images/close.png" id="closeInformation"/>
    </a>
    <div id="movie-content"></div>
  </div>
  <div class="footer" style="color:#333; text-align: center;">
      We would like to thank Wikipedia for sharing the movie information data and TMDb for sharing the movie poster URLs. Movie images provided by the TMDb API but is not endorsed or certified by TMDb 
  </div>
  </body>
</html>
<script>
  
  var tag = document.createElement('script');
  tag.src = "http://www.youtube.com/player_api";
  var firstScriptTag = document.getElementsByTagName('script')[0];
  firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
  var player;
  function onYouTubePlayerAPIReady() {
    player = new YT.Player('player', {
      height: '390',
      width: '640',
      videoId: 'vM81xWzJAmg',
      events: {
        'onReady': onPlayerReady,
        'onStateChange': onPlayerStateChange
      }
    });
  }
</script>

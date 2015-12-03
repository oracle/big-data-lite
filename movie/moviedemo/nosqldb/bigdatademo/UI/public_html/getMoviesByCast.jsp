<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="oracle.demo.oow.bd.dao.CastDAO" %>
<%@ page import="oracle.demo.oow.bd.dao.CrewDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="oracle.demo.oow.bd.to.MovieTO" %>
<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="oracle.demo.oow.bd.constant.Constant" %>
<%@ page import="oracle.demo.oow.bd.to.ActivityTO" %>
<%@ page import="oracle.demo.oow.bd.pojo.ActivityType" %>
<%@ page import="oracle.demo.oow.bd.dao.ActivityDAO" %>

<%
String castId = request.getParameter("id");
String type = request.getParameter("type");
boolean useMoviePosters = (Boolean)request.getSession().getAttribute("useMoviePosters");				  

if (castId!=null){
  List<MovieTO> movieList = new ArrayList<MovieTO>();
  if (type.equalsIgnoreCase("actor")){
    CastDAO castDao = new CastDAO();
    movieList = castDao.getMoviesByCast(Integer.parseInt(castId)); 
  }else {
    CrewDAO crewDao = new CrewDAO();
    movieList = crewDao.getMoviesByCrew(Integer.parseInt(castId));
  }
  
  int userId = (Integer)request.getSession().getAttribute("userId");
  //TODO
  /////// ACTIVITY ////////
  ActivityTO activityTO = new ActivityTO();
  activityTO.setActivity(ActivityType.SEARCH_MOVIE);
  //activityTO.setMovieId(Integer.parseInt(movieId));
  activityTO.setCustId(userId);
  //activityTO.setPrice(1.99);
  ActivityDAO aDAO = new ActivityDAO();
  aDAO.insertCustomerActivity(activityTO);
  
  %>
    <div class="wbox dark">
      <div class="slidebox">
        <a id="prevSlideCast" class="arrow lef" ><span></span></a>
        <a id="nextSlideCast" class="arrow rig"><span></span></a>
        <div class="box">
          <div class="in jspScrollable" style="overflow: hidden; padding: 0px; width: 500px; outline: none; ">
          <div class="jspContainer" style="width: 500px; height: 216px; ">
            <div class="rightpane"></div><div class="leftpane"></div>
            <div class="jspPane" style="padding: 0px; width: 500px; left: 0px; ">
                <div class="boxes">
                <ul id="mycarouselCast" class="jcarousel-skin-name">
                 <%
                    for (int i =0; i < movieList.size(); i++){
                        MovieTO movieTO = movieList.get(i);
                        %>
                     <li class="item-box jcarousel-item-<%= i %>"  rel="movieInformation.jsp?id=<%= movieTO.getId() %>">
                       <a href="#">
                      <span class="img loaded">
                                              
                        <% if (movieTO.getPosterPath().equalsIgnoreCase("") || !useMoviePosters){ %>
                          <div class="" style="height: 120px; overflow: hidden; position: relative; " title="<%= movieTO.getTitle() %>">
                          <img src="images/genericfilm.png" name="genericimage.png" style="display: block; " class="reflected">
                          <div class="tit3" style="margin-top:0px;top:65px;width:75px;height:40px;"><%= movieTO.getTitle() %></div>
                       
                       <% }else { %>
                          <div class="" style="height: 120px; overflow: hidden; ">
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
<% } %>





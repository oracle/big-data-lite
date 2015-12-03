<%@ page import="oracle.demo.oow.bd.dao.CustomerRatingDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="oracle.demo.oow.bd.constant.Constant" %>
<%@ page import="oracle.demo.oow.bd.to.MovieTO" %>
<%
int userId = (Integer)request.getSession().getAttribute("userId");
boolean useMoviePosters = (Boolean)request.getSession().getAttribute("useMoviePosters");				  

CustomerRatingDAO dao = new CustomerRatingDAO();
List<MovieTO> movieList = new ArrayList<MovieTO>();
movieList = dao.getMoviesByMood(userId);
//now when database is not running getMoviesByMood return null
if (movieList==null){
  out.print("error");
}else{
if (movieList.size() != 0){ %>
  <% if(movieList.size() > 5) { %>
        <a id="prevSlideMOOD" class="arrow lef" ><span></span></a>
        <a id="nextSlideMOOD" class="arrow rig"><span></span></a>
        <% } %>
        <script type="text/javascript">$(document).ready(function(){createCarousel("MOOD");createBubble(0,0,"#mycarouselMOOD");});</script>
        <div class="box">
          <div class="in jspScrollable" style="overflow: hidden; padding: 0px; width: 782px; outline: none; ">
          <div class="jspContainer" style="width: 782px; height: 216px; ">
            <div class="rightpane"></div><div class="leftpane"></div>
            <div class="jspPane" style="padding: 0px; width: 782px; left: 0px; ">
                <div class="boxes">
                <ul id="mycarouselMOOD" class="jcarousel-skin-name">
                 <%
                    for (int i =0; i < movieList.size(); i++){
                        MovieTO movieTO = movieList.get(i);
                        %>
                     <li class="item-box jcarousel-item-<%= i %>"  rel="movieInformation.jsp?id=<%= movieTO.getId() %>">
                       <a href="#">
                      <span class="img loaded">
                                                
                        <% if (movieTO.getPosterPath().equalsIgnoreCase("") || !useMoviePosters) { %>
                            <div class="" style="height: 184px; overflow: hidden; position: relative;" title="<%= movieTO.getTitle() %>">
                            <img width="184px" height="276px"
                                src="images/genericfilm.png"
                                name="genericfilm.png"
                                style="display: block; " class="reflected">
                            </img>
                            <div class="tit3"><%= movieTO.getTitle() %></div>

             
                        <% } else { %>   
                            <div class="" style="height: 184px; overflow: hidden; ">  
                            <img width="184px" height="276px"
                                src="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>"
                                name="<%= Constant.TMDb_IMG_URL + movieTO.getPosterPath() %>"
                                style="display: block; " class="reflected">
                            </img>
                                                             
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
<%} } %>
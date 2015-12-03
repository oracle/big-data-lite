<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="java.util.List" %>
<%@ page import="oracle.demo.oow.bd.to.MovieTO" %>
<%@ page import="oracle.demo.oow.bd.constant.Constant" %>
<%@ page import="oracle.demo.oow.bd.dao.ActivityDAO" %>
<%@ page import="oracle.demo.oow.bd.to.ActivityTO" %>
<%@ page import="oracle.demo.oow.bd.dao.CustomerDAO" %>

  <%
      int userId = (Integer)request.getSession().getAttribute("userId");
      boolean useMoviePosters = (Boolean)request.getSession().getAttribute("useMoviePosters");				  
      ActivityDAO aDAO = new ActivityDAO();
      List<MovieTO> movieTOList1 = null;
      
      ///////////////////////WHAT OTHERS WATHCHING///////////////////////
      movieTOList1 = aDAO.getCommonPlayList();
      if (movieTOList1.size()>0){
    %>
    <div class="wbox dark">
      <div id="continueWatching">
      <div class="titbar">
        <div class="tit">What Others Watching</div>
      </div>
      <script type="text/javascript">$(document).ready(function(){createCarousel("CP");createBubble(0,0,"#mycarouselCP");});</script>
      
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
                    CustomerDAO customerDAO = new CustomerDAO();
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
                        <script type="text/javascript">$(document).ready(function(){setRating(<%=rated%>,"star-pw<%=i%>",<%=movieTO.getId()%>);});</script>
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
      <script type="text/javascript">$(document).ready(function(){createCarousel("PW");createBubble(0,0,"#mycarouselPW");});</script>
    
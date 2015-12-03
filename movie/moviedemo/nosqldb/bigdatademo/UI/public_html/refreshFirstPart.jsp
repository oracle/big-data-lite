<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="java.util.List" %>
<%@ page import="oracle.demo.oow.bd.to.MovieTO" %>
<%@ page import="oracle.demo.oow.bd.to.ActivityTO" %>
<%@ page import="oracle.demo.oow.bd.constant.Constant" %>
<%@ page import="oracle.demo.oow.bd.dao.ActivityDAO" %>
<% 
Thread.sleep(2000);
%>
  <%
      int userId = (Integer)request.getSession().getAttribute("userId");
      boolean useMoviePosters = (Boolean)request.getSession().getAttribute("useMoviePosters");				  
      ActivityDAO aDAO = new ActivityDAO();
      
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
      <script type="text/javascript">$(document).ready(function(){createCarousel("CW");continueWatching();});</script>
      
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
                      %>
                      <div class="meter"><span style="width: <%=actual%>%"></span></div>
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
      <script type="text/javascript">$(document).ready(function(){createCarousel("RW");createBubble(0,0,"#mycarouselRW");});</script>
      
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
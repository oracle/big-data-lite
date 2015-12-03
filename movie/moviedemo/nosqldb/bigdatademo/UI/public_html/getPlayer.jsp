<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Random" %>
<%@ page import="oracle.kv.Key" %>
<%@ page import="oracle.demo.oow.bd.util.KeyUtil" %>
<%@ page import="oracle.demo.oow.bd.util.YouTubeUtil" %>
<%@ page import="oracle.demo.oow.bd.to.ActivityTO" %>
<%@ page import="oracle.demo.oow.bd.dao.BaseDAO" %>
<%@ page import="oracle.demo.oow.bd.dao.ActivityDAO" %>
<%
String movieId = request.getParameter("id");
int userId = (Integer)request.getSession().getAttribute("userId");
if (movieId != ""){
    ActivityTO activityTO = new ActivityDAO().getActivityTO(userId, Integer.parseInt(movieId));
    int position = 0;
    if(activityTO!=null) position = activityTO.getPosition();
    String youtubeKey = YouTubeUtil.getKey(Integer.parseInt(movieId)); 
    %>
<!--
<script type="text/javascript">$(document).ready(function(){player.pauseVideo();player.loadVideoById("<%= youtubeKey %>", <%= position %>, "");});</script>
-->
<script type="text/javascript">loadYTVideo("<%= youtubeKey %>",<%= position %>);</script>
<%
} %>
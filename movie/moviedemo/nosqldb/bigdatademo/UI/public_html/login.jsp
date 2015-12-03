<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html;charset=windows-1252"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
    <link href="css/style.css" rel="stylesheet" type="text/css"></link>
    <title>login</title>
  </head>
  <body style="background-color:#202020; background-image: url('images/Oraclemovieplex_1000x750.png'); background-position:center;">
  <div class="container">
    <div id="content">
      <div id="login-box" class="login-popup" style="display: block; position: absolute;top: 25px;right: 25px;width: 250px;left: auto;">
        <%
        if (request.getParameter("error")!=null){%>
            <div class="error">Wrong Username or Password</div>
         <%
         }
        %>
        <form method="post" class="signin" action="login">
            <fieldset class="textbox">
            <label class="username">
            <span>Username or email</span>
            <input id="username" name="username" value="" type="text" autocomplete="on" placeholder="Username">
            </label>
            
            <label class="password">
            <span>Password</span>
            <input id="password" name="password" value="" type="password" placeholder="Password">
            </label>
            
            <button class="submit button" type="submit">Sign in</button>
            <p>
            <a class="forgot" href="#">Forgot your password?</a>
            </p>
            <p>
            <span><input type="checkbox" name="useMoviePosters" value="useMoviePosters" checked="yes"> Use movie posters</span>
            </p>
            </fieldset>
        </form>
      </div>
    </div>
  </div>
  </body>
</html>
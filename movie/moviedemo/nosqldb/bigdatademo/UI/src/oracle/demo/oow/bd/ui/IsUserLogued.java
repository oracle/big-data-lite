package oracle.demo.oow.bd.ui;

import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.demo.oow.bd.dao.CustomerDAO;

public class IsUserLogued implements Filter {
  private FilterConfig _filterConfig = null;
  private String loginPage = "login.jsp";
  private String setActivity = "movieActivity.jsp";
  
  public void init(FilterConfig filterConfig) throws ServletException {
    _filterConfig = filterConfig;
  }

  public void destroy() {
    _filterConfig = null;
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest)servletRequest;
    HttpServletResponse response = (HttpServletResponse)servletResponse;
    String requestURI = request.getRequestURI();
    if ((!requestURI.endsWith(loginPage)) && (!requestURI.endsWith(setActivity)) && (!requestURI.endsWith("test.jsp"))){
      try{
        Date date = new Date();
        String name = (String)request.getSession().getAttribute("username");
        Date time = (Date)request.getSession().getAttribute("time");
        long diffInMinutes = (date.getTime() - time.getTime()) / (1000*60);
        if (name != null && diffInMinutes <= 30){
          request.getSession().setAttribute("time",new Date());
          chain.doFilter(request, response);
        }else
            //System.out.println(" redirecting back ");
          response.sendRedirect(loginPage);
      }
      catch (Exception e){
      e.printStackTrace();
        response.sendRedirect(loginPage);
      }
    }else
      chain.doFilter(request, response);
  }
}

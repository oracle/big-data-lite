package oracle.demo.oow.bd.ui;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.http.*;

import oracle.demo.oow.bd.dao.ActivityDAO;
import oracle.demo.oow.bd.dao.CustomerDAO;
import oracle.demo.oow.bd.pojo.ActivityType;
import oracle.demo.oow.bd.to.ActivityTO;
import oracle.demo.oow.bd.to.CustomerTO;

public class logout extends HttpServlet {
  private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
  private String loginPage = "login.jsp";

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      HttpSession session = request.getSession();
      if (!session.isNew()) {
          int userId = (Integer)request.getSession().getAttribute("userId");
          /////// ACTIVITY ////////
          ActivityTO activityTO = new ActivityTO();
          activityTO.setActivity(ActivityType.LOGIN);
          activityTO.setCustId(userId);
          //activityTO.setPrice(1.99);
          ActivityDAO aDAO = new ActivityDAO();
          aDAO.insertCustomerActivity(activityTO);
        
        
          session.invalidate();
          session = request.getSession();
      }
      response.sendRedirect(loginPage);
    }catch (Exception e){
        response.sendRedirect(loginPage);
    }
  }
}

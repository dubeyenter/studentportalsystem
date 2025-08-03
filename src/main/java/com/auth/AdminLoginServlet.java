package com.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class AdminLoginServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "studentportal";
    private static final String DB_PASS = "admin";

    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);

            String sql = "SELECT * FROM ADMIN WHERE EMAIL = ? AND PASSWORD = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Login success — store admin info in session
                HttpSession session = request.getSession();
                session.setAttribute("adminEmail", email);
                session.setAttribute("adminName", rs.getString("FIRST_NAME") + " " + rs.getString("LAST_NAME"));
                session.setAttribute("role", rs.getString("ROLE"));

                response.sendRedirect("html/admindashboard.html"); // Redirect to dashboard
            } else {
                // Login failed
            	 out.println("<script type='text/javascript'>");
                 out.println("alert('Invalid Email or Password!');");
                 out.println("window.location.href = 'html/AdminLogin.html';");
                 out.println("</script>");
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("admin_login.html?error=2");
        }
    }
}

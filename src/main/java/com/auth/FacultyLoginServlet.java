package com.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class FacultyLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Get form parameters
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Load JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Connect to the database
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");

            // Prepare SQL query to verify credentials
            String sql = "SELECT faculty_id, first_name, last_name FROM faculty WHERE email = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Credentials matched - Create Session
                HttpSession session = request.getSession();
                session.setAttribute("faculty_id", rs.getInt("faculty_id"));
                session.setAttribute("first_name", rs.getString("first_name"));
                session.setAttribute("last_name", rs.getString("last_name"));
                session.setAttribute("email", email);

                // Redirect to Faculty Dashboard
                response.sendRedirect("html/FacultyDashboard.html");
            } else {
                // Invalid credentials
                out.println("<script type='text/javascript'>");
                out.println("alert(' Invalid Email or Password. Please try again.');");
                out.println("window.location.href='html/FacultyLogin.html';");
                out.println("</script>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}

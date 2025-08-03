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

public class StudentLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Database connection details
    private static final String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "studentportal";
    private static final String DB_PASSWORD = "admin"; 

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Retrieve user input
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Load Oracle JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Establish connection
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);

            // Prepare SQL query
            String sql = "SELECT student_id, first_name FROM students WHERE email = ? AND password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            // Execute query
            rs = stmt.executeQuery();

            if (rs.next()) {
                // Store student_id as String in session
                HttpSession session = request.getSession();
                session.setAttribute("studentId", String.valueOf(rs.getInt("student_id")));
                session.setAttribute("studentName", rs.getString("first_name"));

                // Redirect to Student Dashboard
                response.sendRedirect("html/StudentDashboard.html");
            } else {
                // Login failed, show alert and redirect
                out.println("<script type='text/javascript'>");
                out.println("alert('Invalid Email or Password!');");
                out.println("window.location.href = 'html/studentlogin.html';");
                out.println("</script>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h3 style='color:red;'>Database Connection Error!</h3>");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

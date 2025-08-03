package com.registration;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FacultyRegistrationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Retrieve form data
        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String department = request.getParameter("department");
        String dob = request.getParameter("dob");
        String hireDate = request.getParameter("hire_date");

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Load JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Connect to the database
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");

            // Insert faculty data
            String sql = "INSERT INTO faculty (first_name, last_name, email, password, department, dob, hire_date) " +
                         "VALUES (?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'))";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setString(5, department);
            pstmt.setString(6, dob);
            pstmt.setString(7, hireDate);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                response.sendRedirect("html/FacultyLogin.html");
            } else {
                out.println("<h3>Registration Failed. Please try again.</h3>");
            }
        } catch (Exception e) {
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}

package com.marks;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UploadMarksServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Retrieve form data
        String enrollmentNumber = request.getParameter("enrollmentNumber");
        String testName = request.getParameter("testName");
        String marks = request.getParameter("marks");

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Load JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Connect to Database
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");

            // Insert Query using Sequence
            String sql = "INSERT INTO marks (id, enrollment_number, test_name, marks, test_type) "
                       + "VALUES (marks_seq.NEXTVAL, ?, ?, ?, 'Offline Test')";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, enrollmentNumber);
            pstmt.setString(2, testName);
            pstmt.setString(3, marks);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                out.println("<h3 style='color:green;'>Marks Uploaded Successfully!</h3>");
                response.sendRedirect("../html/UploadMarks.html");
            } else {
                out.println("<h3 style='color:red;'>Failed to Upload Marks. Please Try Again!</h3>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h3 style='color:red;'>Error: " + e.getMessage() + "</h3>");
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
}

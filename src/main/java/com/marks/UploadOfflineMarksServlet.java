package com.marks;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class UploadOfflineMarksServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if the user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("faculty_id") == null) {
            response.sendRedirect("FacultyLogin.html");
            return;
        }

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
            
            // Connect to the database
            conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");
            
            // Insert or update offline test marks
            String sql = "MERGE INTO offline_marks om " +
                        "USING (SELECT ? AS enrollment_number, ? AS test_name, ? AS marks FROM dual) new_data " +
                        "ON (om.enrollment_number = new_data.enrollment_number AND om.test_name = new_data.test_name) " +
                        "WHEN MATCHED THEN UPDATE SET om.marks = new_data.marks " +
                        "WHEN NOT MATCHED THEN INSERT (enrollment_number, test_name, marks) " +
                        "VALUES (new_data.enrollment_number, new_data.test_name, new_data.marks)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, enrollmentNumber);
            pstmt.setString(2, testName);
            pstmt.setString(3, marks);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Failed to upload offline marks.");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
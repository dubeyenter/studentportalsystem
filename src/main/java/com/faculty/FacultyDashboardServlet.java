package com.faculty;

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
import org.json.JSONObject;

public class FacultyDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if the user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("faculty_id") == null) {
            response.sendRedirect("FacultyLogin.html");
            return;
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        int totalAnnouncements = 0;
        int totalStudents = 0;
        int totalOnlineMarks = 0;
        int totalOfflineMarks = 0;
        
        try {
            // Load JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");
            
            // Query 1: Count Total Announcements
            String sql1 = "SELECT COUNT(*) FROM ANNOUNCEMENTS";
            pstmt = conn.prepareStatement(sql1);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalAnnouncements = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            
            // Query 2: Count Total Students
            String sql2 = "SELECT COUNT(*) FROM STUDENTS";
            pstmt = conn.prepareStatement(sql2);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalStudents = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            
            // Query 3: Count Total Online Marks Uploaded
            String sql3 = "SELECT COUNT(*) FROM TEST_RESULTS";
            pstmt = conn.prepareStatement(sql3);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalOnlineMarks = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            
            // Query 4: Count Total Offline Marks Uploaded
            String sql4 = "SELECT COUNT(*) FROM OFFLINE_MARKS";
            pstmt = conn.prepareStatement(sql4);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalOfflineMarks = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            
            // Get the faculty's name from the session
            String firstName = (String) session.getAttribute("first_name");
            String lastName = (String) session.getAttribute("last_name");
            String facultyName = (firstName != null && lastName != null) ? firstName + " " + lastName : "Faculty Member";

            // Sending JSON Response
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("totalAnnouncements", totalAnnouncements);
            jsonResponse.put("totalStudents", totalStudents);
            jsonResponse.put("totalOnlineMarks", totalOnlineMarks);
            jsonResponse.put("totalOfflineMarks", totalOfflineMarks);
            jsonResponse.put("facultyName", facultyName); // Add the faculty's name
            
            out.print(jsonResponse.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
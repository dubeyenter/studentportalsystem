package com.faculty;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

public class PostAnnouncementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if the user is logged in (faculty)
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("faculty_id") == null) {
            response.sendRedirect("FacultyLogin.html");
            return;
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        Integer facultyId = (Integer) session.getAttribute("faculty_id");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Connect to the database
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");

            // Fetch announcements posted by this faculty member
            String sql = "SELECT announcement_id, announcement_text, announcement_date FROM announcements WHERE faculty_id = ? ORDER BY announcement_date DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, facultyId);
            rs = pstmt.executeQuery();

            List<JSONObject> announcements = new ArrayList<>();
            while (rs.next()) {
                JSONObject announcement = new JSONObject();
                announcement.put("announcementId", rs.getInt("announcement_id"));
                announcement.put("announcementText", rs.getString("announcement_text"));
                announcement.put("announcementDate", rs.getDate("announcement_date") != null ? rs.getDate("announcement_date").toString() : "N/A");
                announcements.add(announcement);
            }

            // Return the announcements as a JSON array
            JSONArray result = new JSONArray(announcements);
            out.print(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if the user is logged in (faculty)
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("faculty_id") == null) {
            response.sendRedirect("FacultyLogin.html");
            return;
        }

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String announcementText = request.getParameter("announcementText");
        Integer facultyId = (Integer) session.getAttribute("faculty_id");

        if (announcementText == null || announcementText.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("Announcement text is required.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Connect to the database
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");

            // Insert the new announcement
            String sql = "INSERT INTO announcements (announcement_id, faculty_id, announcement_text, announcement_date) " +
                        "VALUES (announcement_seq.NEXTVAL, ?, ?, SYSDATE)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, facultyId);
            pstmt.setString(2, announcementText);
            pstmt.executeUpdate();

            out.print("Announcement posted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("Error: " + e.getMessage());
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
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

public class ViewAllMarksServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if the user is logged in (faculty or admin)
        HttpSession session = request.getSession(false);
        if (session == null || (session.getAttribute("faculty_id") == null && session.getAttribute("admin_id") == null)) {
            response.sendRedirect("FacultyLogin.html");
            return;
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String enrollmentNumber = request.getParameter("enrollmentNumber");
        if (enrollmentNumber == null || enrollmentNumber.trim().isEmpty()) {
            System.out.println("ViewAllMarksServlet: Enrollment number is missing or empty");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Enrollment number is required.\"}");
            return;
        }
        System.out.println("ViewAllMarksServlet: Fetching marks for enrollmentNumber=" + enrollmentNumber);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Connect to the database
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");
            System.out.println("ViewAllMarksServlet: Database connection established");

            // List to store all marks (online and offline)
            List<JSONObject> allMarks = new ArrayList<>();

            // Fetch online marks (from TEST_RESULTS, joined with TESTS only)
            String onlineMarksSql = 
                "SELECT t.test_name, t.test_date, tr.marks_obtained " +
                "FROM test_results tr " +
                "JOIN tests t ON tr.test_id = t.test_id " +
                "WHERE tr.enrollment_number = ? " +
                "ORDER BY t.test_date";
            pstmt = conn.prepareStatement(onlineMarksSql);
            pstmt.setString(1, enrollmentNumber);
            rs = pstmt.executeQuery();

            int onlineMarksCount = 0;
            while (rs.next()) {
                JSONObject mark = new JSONObject();
                mark.put("testName", rs.getString("test_name"));
                mark.put("testDate", rs.getDate("test_date") != null ? rs.getDate("test_date").toString() : "N/A");
                mark.put("marks", rs.getInt("marks_obtained"));
                mark.put("type", "Online");
                allMarks.add(mark);
                onlineMarksCount++;
            }
            System.out.println("ViewAllMarksServlet: Found " + onlineMarksCount + " online marks for enrollmentNumber=" + enrollmentNumber);
            rs.close();
            pstmt.close();

            // Fetch offline marks (from OFFLINE_MARKS directly)
            String offlineMarksSql = 
                "SELECT om.test_name, om.upload_date, om.marks " +
                "FROM offline_marks om " +
                "WHERE om.enrollment_number = ? " +
                "ORDER BY om.upload_date";
            pstmt = conn.prepareStatement(offlineMarksSql);
            pstmt.setString(1, enrollmentNumber);
            rs = pstmt.executeQuery();

            int offlineMarksCount = 0;
            while (rs.next()) {
                JSONObject mark = new JSONObject();
                mark.put("testName", rs.getString("test_name"));
                mark.put("testDate", rs.getDate("upload_date") != null ? rs.getDate("upload_date").toString() : "N/A");
                mark.put("marks", rs.getInt("marks"));
                mark.put("type", "Offline");
                allMarks.add(mark);
                offlineMarksCount++;
            }
            System.out.println("ViewAllMarksServlet: Found " + offlineMarksCount + " offline marks for enrollmentNumber=" + enrollmentNumber);
            rs.close();
            pstmt.close();

            // Build the JSON response as a flat array
            JSONArray result = new JSONArray(allMarks);
            System.out.println("ViewAllMarksServlet: Response JSON=" + result.toString());

            out.print(result.toString());
        } catch (Exception e) {
            System.out.println("ViewAllMarksServlet: Error - " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
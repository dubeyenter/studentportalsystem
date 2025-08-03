package com.student;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class AnnouncementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Database connection details
    private static final String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "studentportal";
    private static final String DB_PASSWORD = "admin";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        JSONArray announcementsArray = new JSONArray();

        try {
            // Load JDBC driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Establish connection
            Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT announcement_text, announcement_date FROM announcements ORDER BY announcement_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            // Fetch announcements
            while (rs.next()) {
                JSONObject announcement = new JSONObject();
                announcement.put("text", rs.getString("announcement_text"));
                announcement.put("date", rs.getString("announcement_date"));
                announcementsArray.put(announcement);
            }

            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        out.print(announcementsArray.toString());
        out.flush();
    }
}

package com.marks;

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
import com.google.gson.Gson;

// Model Class to Hold Marks Data
class MarksData {
    String studentName;
    String testName;
    int marks;
    
    public MarksData(String studentName, String testName, int marks) {
        this.studentName = studentName;
        this.testName = testName;
        this.marks = marks;
    }
}


public class ViewOnlineMarksServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<MarksData> marksList = new ArrayList<>();
        
        try {
            // Load JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");
            
            // Fetch Online Test Marks
            String sql = "SELECT s.first_name || ' ' || s.last_name AS student_name, t.test_name, t.marks " +
                         "FROM students s " +
                         "JOIN test_results t ON s.enrollment_number = t.enrollment_number";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String studentName = rs.getString("student_name");
                String testName = rs.getString("test_name");
                int marks = rs.getInt("marks");
                marksList.add(new MarksData(studentName, testName, marks));
            }
            
            // Convert List to JSON
            String jsonResponse = new Gson().toJson(marksList);
            out.print(jsonResponse);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            out.print("[]");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}

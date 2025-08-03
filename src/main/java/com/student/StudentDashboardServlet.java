package com.student;

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

public class StudentDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // JDBC Database Credentials
    private static final String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String JDBC_USER = "studentportal";
    private static final String JDBC_PASSWORD = "admin";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        // Check if session exists and student ID is present
        if (session == null || session.getAttribute("studentId") == null) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Session expired. Please log in again.");
            out.print(errorResponse.toString());
            out.flush();
            return;
        }

        // Convert studentId safely from session
        String studentIdStr = (String) session.getAttribute("studentId");
        int studentId;
        try {
            studentId = Integer.parseInt(studentIdStr);
        } catch (NumberFormatException e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Invalid student ID.");
            out.print(errorResponse.toString());
            out.flush();
            return;
        }

        JSONObject studentData = new JSONObject();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Load Oracle JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            // Fetch Student Data
            String sql = "SELECT first_name, last_name, email, course, enrollment_date, dob FROM students WHERE student_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                studentData.put("firstName", rs.getString("first_name"));
                studentData.put("lastName", rs.getString("last_name"));
                studentData.put("email", rs.getString("email"));
                studentData.put("course", rs.getString("course"));
                studentData.put("enrollmentDate", rs.getString("enrollment_date"));
                studentData.put("dob", rs.getString("dob"));
            } else {
                studentData.put("error", "No student record found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            studentData.put("error", "Database connection error.");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        out.print(studentData.toString());
        out.flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate(); // Invalidate the session
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Logged out successfully");
                jsonResponse.put("redirect", "index.html");
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "No active session found");
            }
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid action");
        }

        out.print(jsonResponse.toString());
        out.flush();
    }
}
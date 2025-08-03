package com.course;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import com.google.gson.Gson;

public class CourseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> courseList = new ArrayList<>();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");

            String courseIdStr = request.getParameter("courseId");
            String sql;
            if (courseIdStr != null) {
                int courseId = Integer.parseInt(courseIdStr);
                sql = "SELECT COURSE_ID, SUBJECT_CODE, SUBJECT_NAME, COURSE, SEMESTER, FACULTY_ID FROM COURSES WHERE COURSE_ID = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, courseId);
            } else {
                sql = "SELECT COURSE_ID, SUBJECT_CODE, SUBJECT_NAME, COURSE, SEMESTER, FACULTY_ID FROM COURSES";
                ps = conn.prepareStatement(sql);
            }
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> course = new HashMap<>();
                course.put("courseId", rs.getInt("COURSE_ID"));
                course.put("subjectCode", rs.getString("SUBJECT_CODE"));
                course.put("subjectName", rs.getString("SUBJECT_NAME"));
                course.put("course", rs.getString("COURSE"));
                course.put("semester", rs.getInt("SEMESTER"));
                course.put("facultyId", rs.getInt("FACULTY_ID"));
                courseList.add(course);
            }

            if (courseIdStr != null && courseList.isEmpty()) {
                out.print("{\"error\": \"No course found for courseId: " + courseIdStr + "\"}");
            } else {
                String json = new Gson().toJson(courseList);
                out.print(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
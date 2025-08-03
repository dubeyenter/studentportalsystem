package com.course;

import java.io.*;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UpdateCourseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "studentportal";
    private static final String DB_PASSWORD = "admin";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	System.out.println("UpdateCourseServlet called");
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String courseIdStr = request.getParameter("courseId");
        String subjectCode = request.getParameter("subjectCode");
        String subjectName = request.getParameter("subjectName");
        String facultyIdStr = request.getParameter("facultyId");
        String course = request.getParameter("course");
        String semesterStr = request.getParameter("semester");

        if (courseIdStr == null || subjectCode == null || subjectName == null || facultyIdStr == null ||
            course == null || semesterStr == null) {
            out.write("invalid_input");
            return;
        }

        Connection conn = null;
        try {
            int courseId = Integer.parseInt(courseIdStr);
            int facultyId = Integer.parseInt(facultyIdStr);
            int semester = Integer.parseInt(semesterStr);

            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            String sql = "UPDATE COURSES SET SUBJECT_CODE = ?, SUBJECT_NAME = ?, COURSE = ?, SEMESTER = ?, FACULTY_ID = ? WHERE COURSE_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, subjectCode);
                ps.setString(2, subjectName);
                ps.setString(3, course);
                ps.setInt(4, semester);
                ps.setInt(5, facultyId);
                ps.setInt(6, courseId);

                int result = ps.executeUpdate();
                System.out.println("Update executed - Rows affected: " + result + " for COURSE_ID: " + courseId);
                if (result > 0) {
                    conn.commit();
                    System.out.println("Transaction committed for COURSE_ID: " + courseId);
                    out.write("success");
                } else {
                    conn.rollback();
                    System.out.println("Transaction rolled back for COURSE_ID: " + courseId + " - No rows updated");
                    out.write("no_update: No rows updated for COURSE_ID: " + courseId);
                }
            }
        } catch (NumberFormatException e) {
            out.write("invalid_input");
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back due to exception: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.out.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            e.printStackTrace();
            out.write("error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Connection closed");
                } catch (SQLException e) {
                    System.out.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }
}
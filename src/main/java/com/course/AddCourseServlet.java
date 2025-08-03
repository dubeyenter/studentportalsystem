package com.course;

import java.io.*;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddCourseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "studentportal";
    private static final String DB_PASSWORD = "admin";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String subjectCode = request.getParameter("subjectCode");
        String subjectName = request.getParameter("subjectName");
        String course = request.getParameter("course");
        String semesterStr = request.getParameter("semester");
        String facultyIdStr = request.getParameter("facultyId");

        if (subjectCode == null || subjectName == null || course == null || semesterStr == null || facultyIdStr == null) {
            out.write("invalid_input");
            return;
        }

        try {
            int semester = Integer.parseInt(semesterStr);
            int facultyId = Integer.parseInt(facultyIdStr);

            Class.forName("oracle.jdbc.driver.OracleDriver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                conn.setAutoCommit(false);

                // Get next sequence value for COURSE_ID
                int courseId = getNextCourseId(conn);
                String sql = "INSERT INTO COURSES (COURSE_ID, SUBJECT_CODE, SUBJECT_NAME, COURSE, SEMESTER, FACULTY_ID) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, courseId);
                    ps.setString(2, subjectCode);
                    ps.setString(3, subjectName);
                    ps.setString(4, course);
                    ps.setInt(5, semester);
                    ps.setInt(6, facultyId);

                    int result = ps.executeUpdate();
                    if (result > 0) {
                        conn.commit();
                        out.write("success");
                    } else {
                        conn.rollback();
                        out.write("fail");
                    }
                }
            }
        } catch (NumberFormatException e) {
            out.write("invalid_input");
        } catch (Exception e) {
            e.printStackTrace();
            out.write("error: " + e.getMessage());
        }
    }

    private int getNextCourseId(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT course_seq.NEXTVAL FROM DUAL")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Failed to generate COURSE_ID");
        }
    }
}
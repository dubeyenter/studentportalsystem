package com.course;

import java.io.*;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DeleteCourseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "studentportal";
    private static final String DB_PASSWORD = "admin";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String courseIdStr = request.getParameter("courseId");

        if (courseIdStr == null) {
            out.write("invalid_input");
            return;
        }

        try {
            int courseId = Integer.parseInt(courseIdStr);

            Class.forName("oracle.jdbc.driver.OracleDriver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                conn.setAutoCommit(false);

                String sql = "DELETE FROM COURSES WHERE COURSE_ID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, courseId);
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
}
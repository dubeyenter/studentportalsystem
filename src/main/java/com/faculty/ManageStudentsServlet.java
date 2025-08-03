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

public class ManageStudentsServlet extends HttpServlet {
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

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Load JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");

            // Get search query
            String searchQuery = request.getParameter("search");
            String sql;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                sql = "SELECT s.enrollment_number, s.first_name, s.last_name, s.email, c.course_name, " +
                      "(SELECT tr.marks_obtained " +
                      " FROM test_results tr " +
                      " WHERE tr.enrollment_number = s.enrollment_number " +
                      " AND tr.test_id = (SELECT MAX(test_id) FROM test_results WHERE enrollment_number = s.enrollment_number)) AS online_marks, " +
                      "(SELECT om.marks " +
                      " FROM offline_marks om " +
                      " WHERE om.enrollment_number = s.enrollment_number " +
                      " AND om.id = (SELECT MAX(id) FROM offline_marks WHERE enrollment_number = s.enrollment_number)) AS offline_marks " +
                      "FROM students s " +
                      "LEFT JOIN student_courses sc ON s.enrollment_number = sc.enrollment_number " +
                      "LEFT JOIN courses c ON sc.course_id = c.course_id " +
                      "WHERE s.enrollment_number LIKE ? OR s.first_name LIKE ? OR s.last_name LIKE ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, "%" + searchQuery + "%");
                pstmt.setString(2, "%" + searchQuery + "%");
                pstmt.setString(3, "%" + searchQuery + "%");
            } else {
                sql = "SELECT s.enrollment_number, s.first_name, s.last_name, s.email, c.course_name, " +
                      "(SELECT tr.marks_obtained " +
                      " FROM test_results tr " +
                      " WHERE tr.enrollment_number = s.enrollment_number " +
                      " AND tr.test_id = (SELECT MAX(test_id) FROM test_results WHERE enrollment_number = s.enrollment_number)) AS online_marks, " +
                      "(SELECT om.marks " +
                      " FROM offline_marks om " +
                      " WHERE om.enrollment_number = s.enrollment_number " +
                      " AND om.id = (SELECT MAX(id) FROM offline_marks WHERE enrollment_number = s.enrollment_number)) AS offline_marks " +
                      "FROM students s " +
                      "LEFT JOIN student_courses sc ON s.enrollment_number = sc.enrollment_number " +
                      "LEFT JOIN courses c ON sc.course_id = c.course_id";
                pstmt = conn.prepareStatement(sql);
            }

            rs = pstmt.executeQuery();

            List<JSONObject> students = new ArrayList<>();
            while (rs.next()) {
                JSONObject student = new JSONObject();
                student.put("studentId", rs.getString("enrollment_number"));
                student.put("firstName", rs.getString("first_name"));
                student.put("lastName", rs.getString("last_name"));
                student.put("email", rs.getString("email"));
                student.put("course", rs.getString("course_name"));
                student.put("onlineMarks", rs.getObject("online_marks") != null ? rs.getInt("online_marks") : null);
                student.put("offlineMarks", rs.getObject("offline_marks") != null ? rs.getInt("offline_marks") : null);
                students.add(student);
            }

            JSONArray jsonArray = new JSONArray(students);
            out.print(jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
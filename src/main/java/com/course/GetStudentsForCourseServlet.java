package com.course;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetStudentsForCourseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JSONArray studentArray = new JSONArray();

        String course = request.getParameter("course");
        String semester = request.getParameter("semester");
        String subject = request.getParameter("subject");

        if (subject != null && subject.contains(" - ")) {
            subject = subject.split(" - ")[0].trim(); // Extract subject code
        }

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");

            // Updated SQL query, ensuring proper matching for the subject code
            String query = "SELECT S.ENROLLMENT_NUMBER, S.FIRST_NAME, S.LAST_NAME " +
                           "FROM STUDENTS S " +
                           "JOIN STUDENT_COURSES SC ON S.ENROLLMENT_NUMBER = SC.ENROLLMENT_NUMBER " +
                           "JOIN COURSES C ON SC.COURSE_ID = C.COURSE_ID " +
                           "WHERE C.COURSE = ? AND C.SEMESTER = ? AND C.SUBJECT_CODE = ?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, course);
            ps.setInt(2, Integer.parseInt(semester));  // Ensure semester is an integer
            ps.setString(3, subject);  // Ensure subject code is passed correctly

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                JSONObject student = new JSONObject();
                student.put("enrollmentNumber", rs.getString("ENROLLMENT_NUMBER"));
                student.put("name", rs.getString("FIRST_NAME") + " " + rs.getString("LAST_NAME"));
                studentArray.put(student);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the JSON response
        out.print(studentArray.toString());
        out.flush();
    }
}

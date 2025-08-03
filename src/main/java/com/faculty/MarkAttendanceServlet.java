package com.faculty;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.*;

public class MarkAttendanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try {
            JSONObject body = new JSONObject(sb.toString());
            int courseId = body.getInt("courseId");
            String dateStr = body.getString("date");
            JSONArray presentStudents = body.getJSONArray("presentStudents");

            Date sqlDate = Date.valueOf(dateStr);

            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");

            for (int i = 0; i < presentStudents.length(); i++) {
                String enrollment = presentStudents.getString(i);

                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO ATTENDANCE (ATTENDANCE_ID, ENROLLMENT_NUMBER, COURSE_ID, ATTENDANCE_DATE, STATUS) " +
                    "VALUES (ATTENDANCE_SEQ.NEXTVAL, ?, ?, ?, ?)"
                );
                ps.setString(1, enrollment);
                ps.setInt(2, courseId);
                ps.setDate(3, sqlDate);
                ps.setString(4, "Present");

                ps.executeUpdate();
                ps.close();
            }

            con.close();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Attendance recorded.");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to record attendance.");
        }
    }
}

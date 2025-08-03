package com.attendance;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class MarkAttendanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "studentportal";
    private static final String DB_PASSWORD = "admin";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        // Get form data
        String attendanceDate = request.getParameter("attendanceDate");
        String subjectCode = request.getParameter("subjectCode");
        String course = request.getParameter("courseId");  // course code or name passed directly
        String[] presentStudents = request.getParameterValues("presentStudents");
        String[] absentStudents = request.getParameterValues("absentStudents");
        String attendanceSource = request.getParameter("attendanceSource");  // Faculty or Facial Recognition

        Connection con = null;
        PreparedStatement psCheck = null;
        PreparedStatement psInsert = null;

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Check and insert present students
            if (presentStudents != null) {
                for (String studentId : presentStudents) {
                    // Check if attendance already exists for the student and date
                    String checkQuery = "SELECT COUNT(*) FROM ATTENDANCE WHERE ENROLLMENT_NUMBER = ? AND ATTENDANCE_DATE = ? AND COURSE_ID = ? AND SUBJECT_CODE = ?";
                    psCheck = con.prepareStatement(checkQuery);
                    psCheck.setString(1, studentId);
                    psCheck.setDate(2, java.sql.Date.valueOf(attendanceDate));
                    psCheck.setString(3, course);  // Use the course code directly as a string
                    psCheck.setString(4, subjectCode);  // Use subjectCode in the query

                    ResultSet rsCheck = psCheck.executeQuery();
                    rsCheck.next();
                    int count = rsCheck.getInt(1);

                    if (count == 0) {
                        String insertQuery = "INSERT INTO ATTENDANCE (ENROLLMENT_NUMBER, COURSE_ID, SUBJECT_CODE, ATTENDANCE_DATE, STATUS, ATTENDANCE_SOURCE) VALUES (?, ?, ?, ?, 'Present', ?)";
                        psInsert = con.prepareStatement(insertQuery);
                        psInsert.setString(1, studentId);
                        psInsert.setString(2, course);  // Use the course code directly as a string
                        psInsert.setString(3, subjectCode);  // Use subjectCode in the insert query
                        psInsert.setDate(4, java.sql.Date.valueOf(attendanceDate));
                        psInsert.setString(5, attendanceSource);  // Set the source (Faculty or Facial Recognition)
                        psInsert.executeUpdate();
                    }
                }
            }

            // Check and insert absent students
            if (absentStudents != null) {
                for (String studentId : absentStudents) {
                    // Check if attendance already exists for the student and date
                    String checkQuery = "SELECT COUNT(*) FROM ATTENDANCE WHERE ENROLLMENT_NUMBER = ? AND ATTENDANCE_DATE = ? AND COURSE_ID = ? AND SUBJECT_CODE = ?";
                    psCheck = con.prepareStatement(checkQuery);
                    psCheck.setString(1, studentId);
                    psCheck.setDate(2, java.sql.Date.valueOf(attendanceDate));
                    psCheck.setString(3, course);  // Use the course code directly as a string
                    psCheck.setString(4, subjectCode);  // Use subjectCode in the query

                    ResultSet rsCheck = psCheck.executeQuery();
                    rsCheck.next();
                    int count = rsCheck.getInt(1);

                    if (count == 0) {
                        String insertQuery = "INSERT INTO ATTENDANCE (ENROLLMENT_NUMBER, COURSE_ID, SUBJECT_CODE, ATTENDANCE_DATE, STATUS, ATTENDANCE_SOURCE) VALUES (?, ?, ?, ?, 'Absent', ?)";
                        psInsert = con.prepareStatement(insertQuery);
                        psInsert.setString(1, studentId);
                        psInsert.setString(2, course);  // Use the course code directly as a string
                        psInsert.setString(3, subjectCode);  // Use subjectCode in the insert query
                        psInsert.setDate(4, java.sql.Date.valueOf(attendanceDate));
                        psInsert.setString(5, attendanceSource);  // Set the source (Faculty or Facial Recognition)
                        psInsert.executeUpdate();
                    }
                }
            }

            out.println("Attendance successfully recorded!");

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            out.println("Error recording attendance: " + e.getMessage());
        } finally {
            try {
                if (psCheck != null) psCheck.close();
                if (psInsert != null) psInsert.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

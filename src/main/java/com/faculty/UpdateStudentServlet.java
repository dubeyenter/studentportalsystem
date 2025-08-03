// we will use this servelt with admindashboard as this access to change the details of student should not be given to facultys
package com.faculty;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class UpdateStudentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if the user is logged in and is an admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin_id") == null) {
            System.out.println("UpdateStudentServlet: Access denied - User is not an admin");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("Access denied: Only admins can update student details.");
            return;
        }

        // Retrieve parameters
        String enrollmentNumber = request.getParameter("studentId");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");

        System.out.println("UpdateStudentServlet: Received parameters - enrollmentNumber=" + enrollmentNumber +
                          ", firstName=" + firstName + ", lastName=" + lastName);

        // Validate parameters
        if (enrollmentNumber == null || firstName == null || lastName == null ||
            enrollmentNumber.trim().isEmpty() || firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            System.out.println("UpdateStudentServlet: Invalid parameters");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid parameters: enrollmentNumber, firstName, and lastName are required.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Connect to the database
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin");
            System.out.println("UpdateStudentServlet: Database connection established");

            // Update student details
            String sql = "UPDATE students SET first_name = ?, last_name = ? WHERE enrollment_number = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, enrollmentNumber);

            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("UpdateStudentServlet: Rows updated = " + rowsUpdated);
            if (rowsUpdated > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("Student updated successfully.");
            } else {
                System.out.println("UpdateStudentServlet: No student found with enrollment_number=" + enrollmentNumber);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("Student not found.");
            }
        } catch (Exception e) {
            System.out.println("UpdateStudentServlet: Error - " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.println("Error: " + e.getMessage());
            out.flush();
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
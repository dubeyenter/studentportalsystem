package com.registration;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@MultipartConfig
public class StudentRegistrationServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve form fields
        String firstName = request.getParameter("first_name");
        String lastName = request.getParameter("last_name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String course = request.getParameter("course");
        String dob = request.getParameter("dob");
        String enrollmentNumber = request.getParameter("enrollment_number");

        // Log parameters for debugging
        System.out.println("Enrollment Number: " + enrollmentNumber);
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Email: " + email);
        System.out.println("Course: " + course);
        System.out.println("DOB: " + dob);

        // Validate enrollment number
        if (enrollmentNumber == null || enrollmentNumber.trim().isEmpty()) {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<script type=\"text/javascript\">");
            out.println("alert('Enrollment Number is required!');");
            out.println("window.location.href='/StudentPortalSystem/html/studentRegistration.html';");
            out.println("</script>");
            return;
        }

        // Validate and parse DOB
        LocalDate dobDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dobDate = LocalDate.parse(dob, formatter);
            // Validate DOB range (not in the future, not too old)
            LocalDate today = LocalDate.now();
            if (dobDate.isAfter(today)) {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println("<script type=\"text/javascript\">");
                out.println("alert('Date of Birth cannot be in the future!');");
                out.println("window.location.href='/StudentPortalSystem/html/studentRegistration.html';");
                out.println("</script>");
                return;
            }
            if (dobDate.isBefore(today.minusYears(100))) {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println("<script type=\"text/javascript\">");
                out.println("alert('Date of Birth is too far in the past!');");
                out.println("window.location.href='/StudentPortalSystem/html/studentRegistration.html';");
                out.println("</script>");
                return;
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<script type=\"text/javascript\">");
            out.println("alert('Invalid Date of Birth format! Please use YYYY-MM-DD.');");
            out.println("window.location.href='/StudentPortalSystem/html/studentRegistration.html';");
            out.println("</script>");
            return;
        }

        // Retrieve multiple facial data values
        String[] facialDataArray = request.getParameterValues("facial_data[]");
        String saveDirectory = getServletContext().getRealPath("/faces");
        File uploadDir = new File(saveDirectory);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            System.out.println("Directory created: " + saveDirectory + " (" + created + ")");
        }

        // Validate facial data
        if (facialDataArray == null || facialDataArray.length == 0) {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<script type=\"text/javascript\">");
            out.println("alert('At least one face capture is required!');");
            out.println("window.location.href='/StudentPortalSystem/html/studentRegistration.html';");
            out.println("</script>");
            return;
        }

        // Save each facial image
        List<String> imagePaths = new ArrayList<>();
        for (int i = 0; i < facialDataArray.length; i++) {
            String facialData = facialDataArray[i];
            if (facialData != null && !facialData.isEmpty()) {
                try {
                    // Generate unique filename with index
                    String fileName = "face_" + enrollmentNumber + "_" + (i + 1) + ".png";
                    byte[] decodedImg = Base64.getDecoder().decode(facialData.split(",")[1]);
                    Path filePath = Paths.get(saveDirectory, fileName);
                    Files.write(filePath, decodedImg);
                    imagePaths.add("/faces/" + fileName);
                    System.out.println("Saved image: " + filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<script type=\"text/javascript\">");
                    out.println("alert('Failed to save facial data: " + e.getMessage() + "');");
                    out.println("window.location.href='/StudentPortalSystem/html/studentRegistration.html';");
                    out.println("</script>");
                    return;
                }
            }
        }

        // Database operations
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<script type=\"text/javascript\">");
            out.println("alert('Database driver not found: " + e.getMessage() + "');");
            out.println("window.location.href='/StudentPortalSystem/html/studentRegistration.html';");
            out.println("</script>");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "studentportal", "admin")) {
            // Insert into students table
            String insertStudentSQL = "INSERT INTO students (first_name, last_name, email, password, course, dob, enrollment_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertStudentSQL)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, email);
                stmt.setString(4, password);
                stmt.setString(5, course);
                stmt.setDate(6, java.sql.Date.valueOf(dobDate)); // Use java.sql.Date
                stmt.setString(7, enrollmentNumber);
                stmt.executeUpdate();
                System.out.println("Inserted student record for enrollment_number: " + enrollmentNumber);
            }

            // Delete existing facial data for this enrollment_number to avoid duplicates
            String deleteFaceSQL = "DELETE FROM student_faces WHERE enrollment_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteFaceSQL)) {
                stmt.setString(1, enrollmentNumber);
                stmt.executeUpdate();
                System.out.println("Deleted existing facial data for enrollment_number: " + enrollmentNumber);
            }

            // Insert each facial image into student_faces table
            String insertFaceSQL = "INSERT INTO student_faces (enrollment_number, image_path) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertFaceSQL)) {
                for (String imagePath : imagePaths) {
                    stmt.setString(1, enrollmentNumber);
                    stmt.setString(2, imagePath);
                    stmt.executeUpdate();
                    System.out.println("Inserted facial data: " + imagePath);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<script type=\"text/javascript\">");
            out.println("alert('Registration failed: " + e.getMessage() + "');");
            out.println("window.location.href='/StudentPortalSystem/html/studentRegistration.html';");
            out.println("</script>");
            return;
        }

        // Redirect to success page
        response.sendRedirect("html/studentlogin.html");
    }
}
package com.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the current session (if it exists)
        HttpSession session = request.getSession(false);
        String redirectUrl = "FacultyLogin.html"; // Default redirect

        if (session != null) {
            // Check if the user is a faculty or student
            if (session.getAttribute("faculty_id") != null) {
                redirectUrl = "FacultyLogin.html";
            } else if (session.getAttribute("studentId") != null) {
                redirectUrl = "StudentLogin.html";
            }
            // Invalidate the session to clear all attributes
            session.invalidate();
        }

        // Redirect to the appropriate login page
        response.sendRedirect(redirectUrl);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Handle POST requests the same way as GET
        doGet(request, response);
    }
}
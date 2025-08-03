package com.attendance;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import org.bytedeco.opencv.opencv_objdetect.*;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.IntBuffer;
import java.io.IOException;
import java.net.URLEncoder;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

public class FacialAttendanceSystem {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "studentportal";
    private static final String DB_PASS = "admin";
    private static final String CASCADE_PATH = "webapp/haarcascade_frontalface_default.xml";
    private static final String FACES_DIR = "webapp/faces/";
    private static final String TRAINED_DIR = "webapp/trained_faces/";
    private static final String MODEL_PATH = TRAINED_DIR + "recognizer.yml";
    private static final String SERVLET_URL = "http://localhost:8080/StudentPortalSystem/MarkAttendance";
    private static final int COURSE_ID = 8; // Hardcoded for testing (MCA101)
    private static final String SUBJECT_CODE = "MCA101"; // Hardcoded for testing
    private static final String ATTENDANCE_DATE = java.time.LocalDate.now().toString();

    public static void main(String[] args) throws Exception {
        // Initialize face detector and recognizer
        CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_PATH);
        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        File modelFile = new File(MODEL_PATH);

        // Load student images and labels for COURSE_ID
        Map<Integer, String> labelToEnrollment = new HashMap<>();
        List<Mat> images = new ArrayList<>();
        MatOfInt labels = new MatOfInt();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT SF.ID, SF.ENROLLMENT_NUMBER, SF.IMAGE_PATH " +
                 "FROM STUDENT_FACES SF " +
                 "JOIN STUDENT_COURSES SC ON SF.ENROLLMENT_NUMBER = SC.ENROLLMENT_NUMBER " +
                 "WHERE SC.COURSE_ID = ?")) {
            stmt.setInt(1, COURSE_ID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("ID");
                String enrollmentNumber = rs.getString("ENROLLMENT_NUMBER");
                String imagePath = rs.getString("IMAGE_PATH").replace("/images/", "");
                Mat image = imread(FACES_DIR + imagePath, IMREAD_GRAYSCALE);
                if (!image.empty()) {
                    images.add(image);
                    labels.push_back(new MatOfInt(id));
                    labelToEnrollment.put(id, enrollmentNumber);
                }
            }
        }

        // Train or load model
        if (!modelFile.exists() && !images.isEmpty()) {
            recognizer.train(images, labels);
            recognizer.save(MODEL_PATH);
            System.out.println("Model trained and saved to " + MODEL_PATH);
        } else if (modelFile.exists()) {
            recognizer.read(MODEL_PATH);
            System.out.println("Model loaded from " + MODEL_PATH);
        } else {
            System.out.println("No face images found for training for COURSE_ID: " + COURSE_ID);
            return;
        }

        // Initialize camera
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0); // Default webcam
        grabber.start();

        CanvasFrame canvas = new CanvasFrame("Facial Attendance", CanvasFrame.getDefaultGamma());
        while (canvas.isVisible()) {
            Frame frame = grabber.grab();
            Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);
            Mat gray = new Mat();
            cvtColor(mat, gray, COLOR_BGR2GRAY);

            // Detect faces
            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(gray, faces);

            for (int i = 0; i < faces.size(); i++) {
                Rect face = faces.get(i);
                Mat faceMat = gray.submat(face);

                // Recognize face
                IntPointer label = new IntPointer(1);
                DoublePointer confidence = new DoublePointer(1);
                recognizer.predict(faceMat, label, confidence);

                if (confidence.get() < 50.0) { // Adjust threshold
                    int predictedLabel = label.get();
                    String enrollmentNumber = labelToEnrollment.get(predictedLabel);
                    if (enrollmentNumber != null) {
                        // Mark attendance via servlet
                        if (markAttendance(enrollmentNumber)) {
                            System.out.println("Attendance marked for " + enrollmentNumber + " (COURSE_ID: " + COURSE_ID + ", SUBJECT_CODE: " + SUBJECT_CODE + ")");
                        } else {
                            System.out.println("Failed to mark attendance for " + enrollmentNumber);
                        }
                    }
                }

                // Draw rectangle (for debugging)
                rectangle(mat, face, Scalar.RED);
            }

            canvas.showImage(new OpenCVFrameConverter.ToMat().convert(mat));
            if (canvas.waitKey(33) >= 0) break;
        }

        grabber.release();
        canvas.dispose();
    }

    private static boolean markAttendance(String enrollmentNumber) {
        try {
            URL url = new URL(SERVLET_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String params = "attendanceDate=" + URLEncoder.encode(ATTENDANCE_DATE, "UTF-8") +
                           "&courseId=" + COURSE_ID +
                           "&subjectCode=" + URLEncoder.encode(SUBJECT_CODE, "UTF-8") +
                           "&presentStudents=" + URLEncoder.encode(enrollmentNumber, "UTF-8") +
                           "&attendanceSource=FACIAL";
            conn.getOutputStream().write(params.getBytes());
            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
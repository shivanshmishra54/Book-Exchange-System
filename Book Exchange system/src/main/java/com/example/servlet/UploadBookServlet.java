package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/UploadBookServlet")
@MultipartConfig
public class UploadBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    // Files save karne ke liye folders ke path
    private static final String UPLOAD_DIRECTORY = "C:/book_exchange_uploads";
    private static final String COVER_DIRECTORY = UPLOAD_DIRECTORY + "/covers"; // Cover images ke liye naya folder

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.html");
            return;
        }
        int uploaderId = (int) session.getAttribute("userId");

        // Form se saari details lena
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String genre = request.getParameter("genre");
        String publicationType = request.getParameter("publication_type");
        String language = request.getParameter("language");
        boolean isDirectUse = "true".equals(request.getParameter("is_direct_use"));
        boolean isOffline = !isDirectUse;

        // Zaroori folders (uploads aur covers) banayein agar woh pehle se nahi hain
        new File(UPLOAD_DIRECTORY).mkdirs();
        new File(COVER_DIRECTORY).mkdirs();

       
        Part coverImagePart = request.getPart("coverImage");
        String coverImageName = Paths.get(coverImagePart.getSubmittedFileName()).getFileName().toString();
        String dbCoverImagePath = null; // Default value null rakhein

        // Check karein ki user ne cover image upload ki hai ya nahi
        if (coverImageName != null && !coverImageName.isEmpty()) {
            Path coverImagePath = Paths.get(COVER_DIRECTORY, coverImageName);
            try (InputStream input = coverImagePart.getInputStream()) {
                Files.copy(input, coverImagePath, StandardCopyOption.REPLACE_EXISTING);
            }
            // Path ko database me save karne ke liye format karein
            dbCoverImagePath = coverImagePath.toString().replace('\\', '/');
        }

        // 2. Book File (PDF, optional) ko handle karein
        Part bookFilePart = request.getPart("bookFile");
        String bookFileName = Paths.get(bookFilePart.getSubmittedFileName()).getFileName().toString();
        String dbBookFilePath = null; // Default null rakhein
        if (bookFileName != null && !bookFileName.isEmpty()) {
            Path bookFilePath = Paths.get(UPLOAD_DIRECTORY, bookFileName);
            try (InputStream input = bookFilePart.getInputStream()) {
                Files.copy(input, bookFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            dbBookFilePath = bookFilePath.toString().replace('\\', '/');
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                
                // 3. Database me saari details save karein (image_path ke saath) - UPDATED SQL
                String insertBookSql = "INSERT INTO BOOKS (title, author, genre, publication_type, language, file_path, image_path, is_offline, is_direct_use, uploaded_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                int newBookId = 0;
                try (PreparedStatement ps = con.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, title);
                    ps.setString(2, author);
                    ps.setString(3, genre);
                    ps.setString(4, publicationType);
                    ps.setString(5, language);
                    ps.setString(6, dbBookFilePath);   // PDF ka path
                    ps.setString(7, dbCoverImagePath); // Cover Image ka path
                    ps.setBoolean(8, isOffline);
                    ps.setBoolean(9, isDirectUse);
                    ps.setInt(10, uploaderId);
                    
                    ps.executeUpdate();
                    
                    // Nayi book ki ID haasil karein
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            newBookId = rs.getInt(1);
                        }
                    }
                }

                if (newBookId > 0) {
                    // Matching users ko notification bhejein
                    notifyMatchingUsers(con, newBookId, title, genre, language, uploaderId);
                }

                // Safalta-purvak upload hone par 'My Books' page par bhej dein
                response.sendRedirect("my_books.html");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new ServletException("Database error during book upload", e);
        }
    }

    private void notifyMatchingUsers(Connection con, int bookId, String bookTitle, String genre, String language, int uploaderId) throws SQLException {
        List<Integer> userIdsToNotify = new ArrayList<>();
        String findUsersSql = "SELECT user_id, preferences_json FROM USER_PREFERENCES WHERE user_id != ?";
        
        try (PreparedStatement ps = con.prepareStatement(findUsersSql)) {
            ps.setInt(1, uploaderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String prefsJson = rs.getString("preferences_json");
                    if (prefsJson != null && !prefsJson.isEmpty()) {
                        if (prefsJson.contains("\"" + genre + "\"") || prefsJson.contains("\"" + language + "\"")) {
                            userIdsToNotify.add(rs.getInt("user_id"));
                        }
                    }
                }
            }
        }

        if (!userIdsToNotify.isEmpty()) {
            String notificationMessage = "A new book you might like has been added: '" + bookTitle + "'";
            String insertNotificationSql = "INSERT INTO NOTIFICATIONS (recipient_id, sender_id, message, type, link) VALUES (?, ?, ?, 'NEW_BOOK_MATCH', ?)";
            
            try (PreparedStatement psNotify = con.prepareStatement(insertNotificationSql)) {
                for (Integer recipientId : userIdsToNotify) {
                    psNotify.setInt(1, recipientId);
                    psNotify.setInt(2, uploaderId);
                    psNotify.setString(3, notificationMessage);
                    psNotify.setString(4, String.valueOf(bookId));
                    psNotify.addBatch();
                }
                psNotify.executeBatch();
            }
        }
    }
}
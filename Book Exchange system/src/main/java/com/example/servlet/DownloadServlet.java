package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        int bookId = Integer.parseInt(request.getParameter("book_id"));
        
        String filePath = null;
        String fileName = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                
                // 1. Database me download record karein
                String insertSql = "INSERT INTO DOWNLOADS (user_id, book_id) VALUES (?, ?)";
                try (PreparedStatement psInsert = con.prepareStatement(insertSql)) {
                    psInsert.setInt(1, userId);
                    psInsert.setInt(2, bookId);
                    psInsert.executeUpdate();
                }

                // 2. Book ka file path fetch karein
                String selectSql = "SELECT file_path, title FROM BOOKS WHERE book_id = ?";
                try (PreparedStatement psSelect = con.prepareStatement(selectSql)) {
                    psSelect.setInt(1, bookId);
                    try (ResultSet rs = psSelect.executeQuery()) {
                        if (rs.next()) {
                            filePath = rs.getString("file_path");
                            fileName = rs.getString("title"); // Title ko file name banayein
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new ServletException("Database error during download", e);
        }

        // 3. File ko user ko download karwayein
        if (filePath != null) {
            File downloadFile = new File(filePath);
            if (downloadFile.exists()) {
                response.setContentType("application/octet-stream");
                String headerKey = "Content-Disposition";
                // File extension ko preserve karein
                String originalFileName = downloadFile.getName();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String headerValue = String.format("attachment; filename=\"%s%s\"", fileName, fileExtension);
                response.setHeader(headerKey, headerValue);

                try (OutputStream outStream = response.getOutputStream();
                     FileInputStream inStream = new FileInputStream(downloadFile)) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead = -1;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found!");
        }
    }
}
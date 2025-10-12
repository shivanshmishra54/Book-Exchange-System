package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/RequestBookServlet")
public class RequestBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int requesterId = (int) session.getAttribute("userId");
        String requesterUsername = (String) session.getAttribute("username");
        int bookId = Integer.parseInt(request.getParameter("book_id"));
        String message = request.getParameter("message");
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                
                int bookOwnerId = -1;
                String bookTitle = "";

                // 1. Book owner ki ID aur book ka title pata karein
                String getBookInfoSql = "SELECT uploaded_by, title FROM BOOKS WHERE book_id = ?";
                try (PreparedStatement ps = con.prepareStatement(getBookInfoSql)) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bookOwnerId = rs.getInt("uploaded_by");
                            bookTitle = rs.getString("title");
                        }
                    }
                }

                if (bookOwnerId == -1) {
                    throw new SQLException("Book owner not found.");
                }

                // 2. REQUESTS table me ek nayi entry karein
                String insertRequestSql = "INSERT INTO REQUESTS (book_id, requester_id, status) VALUES (?, ?, 'pending')";
                int newRequestId = -1;
                try (PreparedStatement ps = con.prepareStatement(insertRequestSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, requesterId);
                    ps.executeUpdate();
                    
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            newRequestId = generatedKeys.getInt(1);
                        }
                    }
                }
                
                if (newRequestId == -1) {
                    throw new SQLException("Failed to create request.");
                }

                // 3. Book owner ke liye NOTIFICATIONS table me entry karein
                String notificationMessage = requesterUsername + " has requested your book: '" + bookTitle + "'";
                String insertNotificationSql = "INSERT INTO NOTIFICATIONS (recipient_id, sender_id, message, type, link) VALUES (?, ?, ?, 'REQUEST_RECEIVED', ?)";
                try (PreparedStatement ps = con.prepareStatement(insertNotificationSql)) {
                    ps.setInt(1, bookOwnerId);
                    ps.setInt(2, requesterId);
                    ps.setString(3, notificationMessage);
                    ps.setString(4, String.valueOf(newRequestId)); // link me request_id save karein
                    ps.executeUpdate();
                }

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Request sent successfully!");
                out.print(gson.toJson(jsonResponse));
            }

        } catch (ClassNotFoundException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", e.getMessage());
            out.print(gson.toJson(jsonResponse));
            e.printStackTrace();
        }
    }
}
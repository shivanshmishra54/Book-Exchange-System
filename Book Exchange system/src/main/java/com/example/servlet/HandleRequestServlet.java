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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/HandleRequestServlet")
public class HandleRequestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // --- YEH VARIABLES MISSING THE ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";
    // ------------------------------------

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int ownerId = (int) session.getAttribute("userId");
        int requestId = Integer.parseInt(request.getParameter("request_id"));
        String status = request.getParameter("status"); // "approved" ya "rejected"

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                // 1. Request ka status update karein
                String updateRequestSql = "UPDATE REQUESTS SET status = ?, action_date = NOW() WHERE request_id = ?";
                try (PreparedStatement ps = con.prepareStatement(updateRequestSql)) {
                    ps.setString(1, status);
                    ps.setInt(2, requestId);
                    ps.executeUpdate();
                }

                // 2. Requester ko notification bhejein
                int requesterId = -1;
                String bookTitle = "";
                String getInfoSql = "SELECT r.requester_id, b.title FROM REQUESTS r JOIN BOOKS b ON r.book_id = b.book_id WHERE r.request_id = ?";
                try (PreparedStatement ps = con.prepareStatement(getInfoSql)) {
                    ps.setInt(1, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            requesterId = rs.getInt("requester_id");
                            bookTitle = rs.getString("title");
                        }
                    }
                }
                
                if (requesterId != -1) {
                    String notificationMessage = "Your request for '" + bookTitle + "' has been " + status + ".";
                    String notificationType = "approved".equals(status) ? "REQUEST_APPROVED" : "REQUEST_REJECTED";
                    
                    String insertNotificationSql = "INSERT INTO NOTIFICATIONS (recipient_id, sender_id, message, type, link) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(insertNotificationSql)) {
                        ps.setInt(1, requesterId);
                        ps.setInt(2, ownerId);
                        ps.setString(3, notificationMessage);
                        ps.setString(4, notificationType);
                        ps.setString(5, String.valueOf(requestId));
                        ps.executeUpdate();
                    }
                }

                jsonResponse.addProperty("success", true);
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
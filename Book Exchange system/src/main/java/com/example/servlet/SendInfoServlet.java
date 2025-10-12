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

@WebServlet("/SendInfoServlet")
public class SendInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int senderId = (int) session.getAttribute("userId");
        int requestId = Integer.parseInt(request.getParameter("request_id"));

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

                // 1. Sender (book owner) ki contact details fetch karein
                String senderName = "", senderEmail = "", senderMobile = "";
                String getSenderInfoSql = "SELECT first_name, last_name, email, mobile FROM USERS WHERE user_id = ?";
                try (PreparedStatement ps = con.prepareStatement(getSenderInfoSql)) {
                    ps.setInt(1, senderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            senderName = rs.getString("first_name") + " " + rs.getString("last_name");
                            senderEmail = rs.getString("email");
                            senderMobile = rs.getString("mobile");
                        }
                    }
                }
                
                // 2. Requester ki ID aur book ka title fetch karein
                int requesterId = -1;
                String bookTitle = "";
                String getRequestInfoSql = "SELECT r.requester_id, b.title FROM REQUESTS r JOIN BOOKS b ON r.book_id = b.book_id WHERE r.request_id = ?";
                try (PreparedStatement ps = con.prepareStatement(getRequestInfoSql)) {
                    ps.setInt(1, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            requesterId = rs.getInt("requester_id");
                            bookTitle = rs.getString("title");
                        }
                    }
                }

                if (requesterId != -1) {
                    // 3. Requester ke liye ek nayi notification banayein jisme sender ki info ho
                    String notificationMessage = "Contact details for your approved request for '" + bookTitle + "': \n" +
                                                 "Name: " + senderName + "\n" +
                                                 "Email: " + senderEmail + "\n" +
                                                 "Mobile: " + senderMobile;
                    
                    String insertNotificationSql = "INSERT INTO NOTIFICATIONS (recipient_id, sender_id, message, type, link) VALUES (?, ?, ?, 'INFO_SHARED', ?)";
                    try (PreparedStatement ps = con.prepareStatement(insertNotificationSql)) {
                        ps.setInt(1, requesterId);
                        ps.setInt(2, senderId);
                        ps.setString(3, notificationMessage);
                        ps.setString(4, String.valueOf(requestId));
                        ps.executeUpdate();
                    }
                    
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Information sent!");
                    
                } else {
                    throw new SQLException("Requester not found for this request.");
                }

                out.print(gson.toJson(jsonResponse));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", e.getMessage());
            out.print(gson.toJson(jsonResponse));
            e.printStackTrace();
        }
    }
}
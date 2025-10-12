

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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet("/NotificationServlet")
public class NotificationServlet extends HttpServlet {
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
        int recipientId = (int) session.getAttribute("userId");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // CORRECTED and IMPROVED SQL Query
        String sql = "SELECT n.notification_id, n.message, n.type, n.link, n.created_at, " +
                     "r.request_id, r.book_id, b.is_direct_use " +
                     "FROM NOTIFICATIONS n " +
                     "LEFT JOIN REQUESTS r ON (n.type LIKE 'REQUEST_%' AND n.link = CAST(r.request_id AS CHAR)) " +
                     "LEFT JOIN BOOKS b ON r.book_id = b.book_id " +
                     "WHERE n.recipient_id = ? AND n.is_read = 0 " +
                     "ORDER BY n.created_at DESC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                
                ps.setInt(1, recipientId);
                
                JsonArray notificationsArray = new JsonArray();
                Gson gson = new Gson();

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        JsonObject notif = new JsonObject();
                        notif.addProperty("notification_id", rs.getInt("notification_id"));
                        notif.addProperty("message", rs.getString("message"));
                        notif.addProperty("type", rs.getString("type"));
                        notif.addProperty("created_at", rs.getTimestamp("created_at").toString());
                        
                        String type = rs.getString("type");

                        if ("REQUEST_RECEIVED".equals(type) || "REQUEST_APPROVED".equals(type) || "REQUEST_REJECTED".equals(type)) {
                            notif.addProperty("request_id", rs.getInt("request_id"));
                            notif.addProperty("is_direct_use", rs.getBoolean("is_direct_use"));
                            // For Download button on approved request, we need book_id
                            if("REQUEST_APPROVED".equals(type)){
                                notif.addProperty("link", rs.getInt("book_id")); 
                            }
                        } else if ("NEW_BOOK_MATCH".equals(type) || "INFO_SHARED".equals(type)) {
                            notif.addProperty("link", rs.getString("link"));
                        }
                        
                        notificationsArray.add(notif);
                    }
                }
                
                PrintWriter out = response.getWriter();
                out.print(gson.toJson(notificationsArray));
                out.flush();
            }
        } catch (ClassNotFoundException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
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
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/AppearanceServlet")
public class AppearanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int userId = (int) session.getAttribute("userId");
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();
        
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // 1. Purani preferences fetch karein
            String currentPrefsJson = "{}";
            String selectSql = "SELECT preferences_json FROM USER_PREFERENCES WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentPrefsJson = rs.getString("preferences_json");
                    }
                }
            }
            
            JsonObject preferences = JsonParser.parseString(currentPrefsJson).getAsJsonObject();
            String newThemeColor = JsonParser.parseString(requestBody).getAsJsonObject().get("themeColor").getAsString();
            
            // 2. Theme color add/update karein
            preferences.addProperty("themeColor", newThemeColor);
            
            // 3. Nayi preferences save karein
            String updateSql = "INSERT INTO USER_PREFERENCES (user_id, preferences_json) VALUES (?, ?) ON DUPLICATE KEY UPDATE preferences_json = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setInt(1, userId);
                ps.setString(2, preferences.toString());
                ps.setString(3, preferences.toString());
                ps.executeUpdate();
            }

            jsonResponse.addProperty("success", true);
            out.print(gson.toJson(jsonResponse));

        } catch (SQLException e) {
            e.printStackTrace();
            // ... Error handling
        }
    }
}
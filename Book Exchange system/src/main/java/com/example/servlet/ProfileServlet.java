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

@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    // GET: User profile data fetch karne ke liye
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonObject errorObj = new JsonObject();
            errorObj.addProperty("error", "User not logged in");
            out.print(gson.toJson(errorObj));
            return;
        }
        
        int userId = (Integer) session.getAttribute("userId");
        
        String sql = "SELECT first_name, last_name, username, email, mobile FROM USERS WHERE user_id = ?";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        JsonObject profile = new JsonObject();
                        profile.addProperty("first_name", rs.getString("first_name"));
                        profile.addProperty("last_name", rs.getString("last_name"));
                        profile.addProperty("username", rs.getString("username"));
                        profile.addProperty("email", rs.getString("email"));
                        profile.addProperty("mobile", rs.getString("mobile"));
                        out.print(gson.toJson(profile));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        JsonObject errorObj = new JsonObject();
                        errorObj.addProperty("error", "User not found");
                        out.print(gson.toJson(errorObj));
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorObj = new JsonObject();
            errorObj.addProperty("error", "Database error: " + e.getMessage());
            out.print(gson.toJson(errorObj));
            e.printStackTrace();
        }
    }

    // POST: User profile data update karne ke liye
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.addProperty("error", "User not logged in");
            out.print(gson.toJson(jsonResponse));
            return;
        }
        int userId = (Integer) session.getAttribute("userId");
        
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JsonObject profileData = gson.fromJson(requestBody, JsonObject.class);

        String email = profileData.get("email").getAsString();
        String mobile = profileData.get("mobile").getAsString();
        String password = profileData.has("password") ? profileData.get("password").getAsString() : null;

        StringBuilder sqlBuilder = new StringBuilder("UPDATE USERS SET email = ?, mobile = ?");
        if (password != null && !password.isEmpty()) {
            sqlBuilder.append(", password_hash = ?");
        }
        sqlBuilder.append(" WHERE user_id = ?");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = con.prepareStatement(sqlBuilder.toString())) {
                
                int paramIndex = 1;
                ps.setString(paramIndex++, email);
                ps.setString(paramIndex++, mobile);
                if (password != null && !password.isEmpty()) {
                    ps.setString(paramIndex++, password);
                }
                ps.setInt(paramIndex, userId);
                
                int rowsAffected = ps.executeUpdate();
                
                if (rowsAffected > 0) {
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Profile updated successfully!");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("error", "Update failed.");
                }
                out.print(gson.toJson(jsonResponse));
            }
        } catch (ClassNotFoundException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Database error: " + e.getMessage());
            out.print(gson.toJson(jsonResponse));
            e.printStackTrace();
        }
    }
}
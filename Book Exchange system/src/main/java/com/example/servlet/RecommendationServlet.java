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
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

@WebServlet("/RecommendationServlet")
public class RecommendationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int userId = (int) session.getAttribute("userId");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Step 1: User ki preferences fetch karo
            String prefsJsonString = "";
            String prefsSql = "SELECT preferences_json FROM USER_PREFERENCES WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(prefsSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        prefsJsonString = rs.getString("preferences_json");
                    }
                }
            }
            
            JsonArray recommendedBooks = new JsonArray();

            if (prefsJsonString != null && !prefsJsonString.isEmpty()) {
                // Step 2: JSON ko parse karo
                JsonObject prefs = JsonParser.parseString(prefsJsonString).getAsJsonObject();
                JsonArray topics = prefs.getAsJsonArray("topics");
                JsonArray languages = prefs.getAsJsonArray("languages");

                // Step 3: Dynamic SQL query banao
                StringBuilder recommendationSql = new StringBuilder(
                    "SELECT b.book_id, b.title, b.author, b.is_offline, b.is_direct_use, u.username AS uploader " +
                    "FROM BOOKS b LEFT JOIN USERS u ON b.uploaded_by = u.user_id WHERE (1=0 "); // 1=0 trick for easy OR conditions

                if (topics != null && topics.size() > 0) {
                    for (JsonElement topic : topics) {
                        recommendationSql.append(" OR b.genre = ?");
                    }
                }
                if (languages != null && languages.size() > 0) {
                    for (JsonElement lang : languages) {
                        recommendationSql.append(" OR b.language = ?");
                    }
                }
                recommendationSql.append(") AND b.uploaded_by != ? ORDER BY RAND() LIMIT 10"); // User ko apni hi book recommend na ho

                // Step 4: Query execute karo
                try (PreparedStatement ps = con.prepareStatement(recommendationSql.toString())) {
                    int paramIndex = 1;
                    if (topics != null) {
                        for (JsonElement topic : topics) {
                            ps.setString(paramIndex++, topic.getAsString());
                        }
                    }
                    if (languages != null) {
                        for (JsonElement lang : languages) {
                            ps.setString(paramIndex++, lang.getAsString());
                        }
                    }
                    ps.setInt(paramIndex, userId);

                    try (ResultSet rs = ps.executeQuery()) {
                         while (rs.next()) {
                            JsonObject book = new JsonObject();
                            book.addProperty("title", rs.getString("title"));
                            book.addProperty("author", rs.getString("author"));
                            book.addProperty("imageUrl", "https://via.placeholder.com/220x180?text=" + rs.getString("title").replace(" ", "+"));
                            book.addProperty("is_offline", rs.getBoolean("is_offline"));
                            book.addProperty("is_direct_use", rs.getBoolean("is_direct_use"));
                            book.addProperty("uploader", rs.getString("uploader"));
                            recommendedBooks.add(book);
                        }
                    }
                }
            }

            out.print(gson.toJson(recommendedBooks));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
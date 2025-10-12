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

@WebServlet("/DownloadsHistoryServlet")
public class DownloadsHistoryServlet extends HttpServlet {
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

        String sql = "SELECT b.book_id, b.title, b.author, d.download_date " +
                     "FROM DOWNLOADS d JOIN BOOKS b ON d.book_id = b.book_id " +
                     "WHERE d.user_id = ? ORDER BY d.download_date DESC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                
                ps.setInt(1, userId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    JsonArray downloadedBooks = new JsonArray();
                    Gson gson = new Gson();
                    while (rs.next()) {
                        JsonObject book = new JsonObject();
                        book.addProperty("book_id", rs.getInt("book_id"));
                        book.addProperty("title", rs.getString("title"));
                        book.addProperty("author", rs.getString("author"));
                        book.addProperty("download_date", rs.getTimestamp("download_date").toString());
                        book.addProperty("imageUrl", "https://via.placeholder.com/220x180?text=" + rs.getString("title").replace(" ", "+"));
                        downloadedBooks.add(book);
                    }
                    PrintWriter out = response.getWriter();
                    out.print(gson.toJson(downloadedBooks));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("query");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // SQL query jo title, author, aur genre me search karegi
        String sql = "SELECT b.book_id, b.title, b.author, b.is_direct_use, b.is_offline, u.username AS uploader " +
                     "FROM BOOKS b LEFT JOIN USERS u ON b.uploaded_by = u.user_id " +
                     "WHERE b.title LIKE ? OR b.author LIKE ? OR b.genre LIKE ?";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                String searchQuery = "%" + query + "%";
                ps.setString(1, searchQuery);
                ps.setString(2, searchQuery);
                ps.setString(3, searchQuery);

                try (ResultSet rs = ps.executeQuery()) {
                    JsonArray booksArray = new JsonArray();
                    Gson gson = new Gson();
                    while (rs.next()) {
                        JsonObject book = new JsonObject();
                        book.addProperty("book_id", rs.getInt("book_id"));
                        book.addProperty("title", rs.getString("title"));
                        book.addProperty("author", rs.getString("author"));
                        book.addProperty("imageUrl", "https://via.placeholder.com/220x180?text=" + rs.getString("title").replace(" ", "+"));
                        book.addProperty("is_direct_use", rs.getBoolean("is_direct_use"));
                        book.addProperty("is_offline", rs.getBoolean("is_offline"));
                        book.addProperty("uploader", rs.getString("uploader"));
                        booksArray.add(book);
                    }
                    PrintWriter out = response.getWriter();
                    out.print(gson.toJson(booksArray));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
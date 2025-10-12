package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
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

@WebServlet("/MyBooksServlet")
public class MyBooksServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        }
        int userId = (int) session.getAttribute("userId");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

       
        String sql = "SELECT book_id, title, author, publication_type, image_path, is_direct_use FROM BOOKS WHERE uploaded_by = ? ORDER BY book_id DESC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                
                ps.setInt(1, userId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    JsonArray booksArray = new JsonArray();
                    Gson gson = new Gson();

                    while (rs.next()) {
                        JsonObject book = new JsonObject();
                        book.addProperty("book_id", rs.getInt("book_id"));
                        book.addProperty("title", rs.getString("title"));
                        book.addProperty("author", rs.getString("author"));
                        book.addProperty("publication_type", rs.getString("publication_type"));
                        book.addProperty("is_direct_use", rs.getBoolean("is_direct_use"));

                        // UPDATED: Asli image URL banane ka logic
                        String imagePath = rs.getString("image_path");
                        if (imagePath != null && !imagePath.isEmpty()) {
                            // Absolute path se sirf file ka naam nikal rahe hain
                            String imageName = new File(imagePath).getName();
                             // Web-accessible URL bana rahe hain
                            book.addProperty("imageUrl", "uploads/covers/" + imageName);
                        } else {
                            // Agar image nahi hai, toh placeholder dikhayein
                            book.addProperty("imageUrl", "https://via.placeholder.com/220x180?text=No+Cover");
                        }
                        
                        booksArray.add(book);
                    }

                    PrintWriter out = response.getWriter();
                    out.print(gson.toJson(booksArray));
                    out.flush();
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; 
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/ChoiceServlet")
public class ChoiceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";
    
    private static final String UPSERT_SQL = 
        "INSERT INTO USER_PREFERENCES (user_id, preferences_json) " +
        "VALUES (?, ?) " +
        "ON DUPLICATE KEY UPDATE preferences_json = VALUES(preferences_json)";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendRedirect("index.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.html");
            return;
        }
        int userId = (int) session.getAttribute("userId"); 
        
        String preferencesJson = request.getParameter("userPreferences"); 
        
        if (preferencesJson == null || preferencesJson.trim().isEmpty()) {
            response.sendRedirect("choice.html?status=error&message=No_choices_selected"); 
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            ps = con.prepareStatement(UPSERT_SQL);
            ps.setInt(1, userId);                   
            ps.setString(2, preferencesJson);       
            
            ps.executeUpdate();
            response.sendRedirect("dashboard.html?status=preferences_saved"); 


        } catch (ClassNotFoundException e) {
            response.getWriter().println("<h2>Error: JDBC Driver not found. Check your libraries.</h2>");
            e.printStackTrace();
        } catch (SQLException e) {
            response.getWriter().println("<html><body><div align='center'>");
            response.getWriter().println("<h2 style='color:red;'>Error: Database access failed during preference storage.</h2>");
            response.getWriter().println("</div></body></html>");
            e.printStackTrace();
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException ignore) {}
            try { if (con != null) con.close(); } catch (SQLException ignore) {}
        }
    }
    
   
}
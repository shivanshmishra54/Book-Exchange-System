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

@WebServlet("/CheckUsernameServlet")
public class CheckUsernameServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem"; 
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; 
    
    private static final String SQL_CHECK_USERNAME = 
        "SELECT 1 FROM USERS WHERE username = ?"; 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        
        String username = request.getParameter("username"); 
        
        if (username == null || username.trim().isEmpty()) {
            out.write("invalid");
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            
            ps = con.prepareStatement(SQL_CHECK_USERNAME);
            ps.setString(1, username);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                out.write("unavailable");
            } else {
                out.write("available");
            }
            
        } catch (Exception e) {
            System.err.println("Database error during username check: " + e.getMessage());
            out.write("error");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignore) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignore) {}
            try { if (con != null) con.close(); } catch (SQLException ignore) {}
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
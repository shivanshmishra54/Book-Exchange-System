package com.example.servlet;

import jakarta.servlet.RequestDispatcher;
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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem"; 
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; 
    
    private static final String SQL_QUERY = "SELECT user_id FROM USERS WHERE username = ? AND password_hash = ?"; 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String logoutParam = request.getParameter("logout");
        if (logoutParam != null && logoutParam.equals("true")) {
            HttpSession session = request.getSession(false); // Existing session lein, naya na banaye
            if (session != null) {
                session.invalidate(); // Session ko destroy karein
            }
            response.sendRedirect("login.html"); // Login page par bhej dein
            return;
        }
        // Agar logout parameter nahi hai to index par bhej dein
        RequestDispatcher rd = request.getRequestDispatcher("index.html");
        rd.forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String username = request.getParameter("username");
        String password = request.getParameter("password"); 

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            
            ps = con.prepareStatement(SQL_QUERY);
            ps.setString(1, username);
            ps.setString(2, password); 
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                
                int userId = rs.getInt("user_id");
                
                HttpSession session = request.getSession();
                session.setAttribute("userId", userId);
                session.setAttribute("username", username);
                
                RequestDispatcher rd = request.getRequestDispatcher("dashboard.html");
                rd.forward(request, response);
                
            } else {
                
                out.println("<html><body><div align='center'>");
                out.println("<h2 style='color:red;'>Login Failed! Invalid credentials.</h2>");
                out.println("<p><a href='login.html'>Click here to Try Again</a></p>");
                out.println("</div></body></html>");
            }
            
        } catch (ClassNotFoundException e) {
            out.println("<h2>Error: JDBC Driver not found. Check your libraries.</h2>");
            e.printStackTrace();
        } catch (SQLException e) {
            out.println("<h2>Error: Database access failed. Check connection details or SQL query.</h2>");
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignore) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignore) {}
            try { if (con != null) con.close(); } catch (SQLException ignore) {}
        }
    
    
    }
}
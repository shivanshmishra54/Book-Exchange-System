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
import java.sql.SQLException;

@WebServlet("/SignUpServlet")
public class SignUpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookexchangesystem"; 
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; 

    private static final String SQL_QUERY = "INSERT INTO USERS (first_name, last_name, username, email, mobile, password_hash) VALUES (?, ?, ?, ?, ?, ?)";

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.html");
	}

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String username = request.getParameter("username");
            String email = request.getParameter("email");
            String mobile = request.getParameter("mobile"); 
            String password = request.getParameter("password"); 
            
            ps = con.prepareStatement(SQL_QUERY);
            
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, username);
            ps.setString(4, email);
            ps.setString(5, mobile);
            ps.setString(6, password); 

            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
            	HttpSession session = request.getSession();
            	response.sendRedirect("choice.html");
                return;
            } else {
            	out.println("<html><body><div align='center'>");
            	out.println("<h2 style='color:red;'>Sign Up Failed! Could not create account.</h2>");
            	out.println("<p>Click here to <a href='signup.html'>Try Again</a></p>");
            	out.println("</div></body></html>");
            }
            
		} catch (ClassNotFoundException e) {
            out.println("<h2>Error: JDBC Driver not found. Check your libraries.</h2>");
            e.printStackTrace();
        } catch (SQLException e) {
            String errorMessage = "Sign Up Failed! Username or Email is already taken.";
            
            out.println("<html><body><div align='center'>");
            out.println("<h2 style='color:red;'>" + errorMessage + "</h2>");
            out.println("<p>Click here to <a href='signup.html'>Try Again</a></p>");
            out.println("</div></body></html>");
            e.printStackTrace();
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException ignore) {}
            try { if (con != null) con.close(); } catch (SQLException ignore) {}
        }
	}
}
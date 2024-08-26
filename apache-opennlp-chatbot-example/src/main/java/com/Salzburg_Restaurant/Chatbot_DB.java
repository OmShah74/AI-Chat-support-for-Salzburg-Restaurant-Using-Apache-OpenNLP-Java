package com.Salzburg_Restaurant;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Chatbot_DB {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3308/Chatbot_DB","root","12345");
		System.out.println("Connection created.");
	}
}

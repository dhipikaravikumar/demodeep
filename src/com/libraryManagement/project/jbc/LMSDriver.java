/**

 * 
 */
package com.libraryManagement.project.jbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner; 
import java.sql.Statement;


import com.libraryManagement.project.jbc.Administrator;
import com.libraryManagement.project.jbc.Borrower;
import com.libraryManagement.project.jbc.Librarian;


/**
 * @author Samay Seidu-Sofo
 * ProjectName: Library Management System
 * 
 */
public class LMSDriver {

	
	private static final String driver ="com.mysql.cj.jdbc.Driver";
	private static final String url="jdbc:mysql://localhost:3306/library";
	private static final String username="root";
	private static final String password="admin";
	
	
	private static Connection connection; 
	private static Scanner scanner; 
	private static ResultSet resultSet;
	private static PreparedStatement pst;

	
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		// TODO Auto-generated method stub
		
		
		
		//Create Connection:
		//Create a statement 
		//Registering Driver :
	try {
			Class.forName(driver);
			
			connection = DriverManager.getConnection(url, username, password);
			//Statement statement = connection.createStatement();
			scanner = new Scanner(System.in);
			mainMenu();
		
			/**resultSet=pst.getGeneratedKeys();
			while(resultSet.next()) {
				System.out.println(resultSet.getInt(1));
			}**/
		
				scanner.close();
			
			
				} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
			//connection.rollback();
		}
		
		
		
	}
	
	public static void mainMenu() {
		System.out.println("*******************************************************");
		System.out.println("Welcome to GCIT Library Management System!!"+"By:SAMAY");
		System.out.println("Which category of a user are you?");
		System.out.println("*******************************************************");
		System.out.println("(1) Librarian=>");
		System.out.println("(2) Administrator=>");
		System.out.println("(3) Borrower=>");
		System.out.println("(4) Quit Program!!");
		System.out.println("*******************************************************");
		
		while(scanner.hasNext()) {
			int option=scanner.nextInt();
			if(option==1) {
				
				new Librarian(connection);
				
			}else if(option==2) {
				new Administrator(connection);
				
			}else if(option==3) {
				new Borrower(connection);
				
			}else if(option==4) {
				scanner.close();
				return; 
			}
			else {
				System.out.println("***********************************************");
				System.out.println("You entered invalid input!! Please try again!!");
				System.out.println("***********************************************");
				mainMenu();//This takes you back to the mainMenu if input is invalid
			}
		}
		
	}

}

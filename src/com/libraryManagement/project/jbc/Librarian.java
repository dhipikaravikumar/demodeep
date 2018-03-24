/**
 * 
 */
package com.libraryManagement.project.jbc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.libraryManagement.project.jbc.LMSDriver;


/**
 * @author gcit
 *
 */
public class Librarian {

	private Connection connection;
	private static PreparedStatement pst;
	private static ResultSet resultSet;
	private static Branch branch;
	private static Scanner scan;
	
	public Librarian(Connection connection) {
		this.connection = connection;
		scan = new Scanner(System.in);
		librarianMenu();
		scan.close();
	}
	
	private void librarianMenu() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("**************");
		System.out.println("Librarian Menu");
		System.out.println("**************");
		System.out.println("1) Enter the branch you manage");
		System.out.println("2) Return to previous page!!");
		
		String answer = scan.nextLine();
		if (answer.equals("1")) {
			branchMenu();
		}
		else if (answer.equals("2")) {
			LMSDriver.mainMenu();
		}
		else {
			System.out.println("********************************");
			System.out.println("Invalid Input. Please try again.");
			System.out.println("*********************************");
			librarianMenu();
		}
	}
	
	private void branchMenu() {
		System.out.println("---------------------------------------------------------");
		System.out.println("---------------------------------------------------------");
		System.out.println("****************************");
		System.out.println("Library Branches");
		System.out.println("Which branch do you work at?");
		System.out.println("****************************");
		int i = 1;
		List<String> branchArray = new LinkedList<String>();
		try {
			String selectQuery1 = "SELECT * FROM tbl_library_branch";
			pst = connection.prepareStatement(selectQuery1);
			resultSet = pst.executeQuery();
			
			while (resultSet.next()) {
				
				System.out.println(i + ") " + resultSet.getString("branchName"));
				branchArray.add(resultSet.getString("branchName"));
				
				i++;
			}
			System.out.println("*****************************");
			System.out.println(i + ") Return to previous page!!");
			System.out.println("*****************************");
			String line = scan.nextLine();
			if (isInteger(line)) {
				int answer = Integer.parseInt(line);
				if (answer > 0 && answer < i) {
					String branchName = branchArray.get(answer - 1);
					int branchId = getBranchId(branchName);
					String selectQuery2 = "SELECT branchAddress FROM tbl_library_branch WHERE branchId=?";
					pst = connection.prepareStatement(selectQuery2);
					pst.setInt(1, branchId);
					resultSet = pst.executeQuery();
					String branchAddress = "";
					if (resultSet.next()) {
						branchAddress = resultSet.getString("branchAddress");
					}
					branch = new Branch(branchId, branchName, branchAddress);
					librarianAccess();
				}
				else if (answer == i) {
					librarianMenu();
				}
				else {
					System.out.println("********************************");
					System.out.println("Invalid Input. Please try again.");
					System.out.println("*********************************");
					branchMenu();
				}
			}
			else {
				System.out.println("********************************");
				System.out.println("Invalid Input. Please try again.");
				System.out.println("********************************");
				branchMenu();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private int getBranchId(String branchName) {
		String selectQuery = "SELECT * FROM tbl_library_branch WHERE branchName=?";
		try {
			pst = connection.prepareStatement(selectQuery);
			pst.setString(1, branchName);
			resultSet = pst.executeQuery();
			if (resultSet.next()) {
				return resultSet.getInt("branchId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private void librarianAccess() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("You are now in "+"[[" + branch.getBranchName() +"]]" +" branch librarian access. ");
		System.out.println("************************************");
		System.out.println("1) Update the details of the library");
		System.out.println("2) Add copies of book to the branch");
		System.out.println("3) Return to previous page!!");
		
		String answer = scan.nextLine();
		if (answer.equals("1")) {
			updateLibrary();
		}
		else if (answer.equals("2")) {
			bookCopiesManagement();
		}
		else if (answer.equals("3")) {
			branchMenu();
		}
		else {
			System.out.println("********************************");
			System.out.println("Invalid Input. Please try again.");
			System.out.println("********************************");
			librarianAccess();
		}
	}
	
	
	private void updateLibrary() {
		try {
			System.out.println("----------------------------------------------------------");
			System.out.println("----------------------------------------------------------");
			System.out.println("***********************************************************");
			System.out.println("NOTE:You have chosen to update the branch with :");
			System.out.println( "Branch Id: " + branch.getBranchId() + "  Branch Name: " + branch.getBranchName() + ".");
			System.out.println("(1) Enter ‘quit’ at any prompt to cancel operation.");
			System.out.println("(2) Please enter new branch name or enter N/A for no change: ");
			System.out.println("***********************************************************");
			String answer1 = scan.nextLine();
			if (!answer1.toLowerCase().equals("quit")) {
				if (answer1.toLowerCase().equals("n/a")) {
					answer1 = branch.getBranchName();
				}
				System.out.println("Please enter new branch address or enter N/A for no change:");
				String answer2 = scan.nextLine();
				if (!answer2.toLowerCase().equals("quit")) {
					if (answer2.toLowerCase().equals("n/a")) {
						answer2 = branch.getBranchAddress();
					}
					String updateQuery;
					updateQuery = "UPDATE tbl_library_branch SET branchName=?, branchAddress=? WHERE branchId=? ";
					pst = connection.prepareStatement(updateQuery);
					pst.setString(1, answer1);
					pst.setString(2, answer2);
					pst.setInt(3, branch.getBranchId());
					pst.executeUpdate();
					
					branch.setBranchName(answer1);
					branch.setBranchAddress(answer2);
					
					System.out.println("Branch successfully updated.");
					System.out.println("New branch name: " + branch.getBranchName());
					System.out.println("New branch address: " + branch.getBranchAddress());
				}
			}
			librarianAccess();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private void bookCopiesManagement() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		String branchName = branch.getBranchName();
		String selectQuery1 = "SELECT tbl_book.title FROM tbl_book JOIN tbl_book_copies ON "
				+ "tbl_book.bookId=tbl_book_copies.bookId JOIN tbl_library_branch ON "
				+ "tbl_book_copies.branchId = tbl_library_branch.branchId WHERE branchName=?";
		try {
			pst = connection.prepareStatement(selectQuery1);
			pst.setString(1, branchName);
			resultSet = pst.executeQuery();
			System.out.println("----------------------------------------------------------");
			System.out.println("Pick the book you want to add copies of, to your branch:");
			System.out.println("----------------------------------------------------------");
			int i = 1;
			List<String> bookArray = new LinkedList<String>();
			while (resultSet.next()) {
				System.out.println(i + ") " + resultSet.getString("title"));
				bookArray.add(resultSet.getString("title"));
				i++;
			}
			System.out.println(i + ") Quit to cancel operation.");
			String line = scan.nextLine();
			if (isInteger(line)) {
				int answer1 = Integer.parseInt(line);
				if (answer1 > 0 && answer1 < i) {
					String bookTitle = bookArray.get(answer1 - 1);
					int bookId = 0;
					String selectBookIdQuery = "SELECT * FROM tbl_book WHERE title=?";
					pst = connection.prepareStatement(selectBookIdQuery);
					pst.setString(1, bookTitle);
					resultSet = pst.executeQuery();
					if (resultSet.next()) {
						bookId = resultSet.getInt("bookId");
					}
					System.out.println(bookTitle);
					String selectQuery2 = "SELECT tbl_book_copies.noOfCopies FROM tbl_book_copies WHERE branchId=? AND bookId=?";
					pst = connection.prepareStatement(selectQuery2);
					pst.setInt(1, branch.getBranchId());
					pst.setInt(2, bookId);
					resultSet = pst.executeQuery();
					int noOfCopies = 0;
					if (resultSet.next()) {
						noOfCopies = resultSet.getInt("noOfCopies");
					}
					
					System.out.println("Current number of copies: " + noOfCopies);
					System.out.println("Enter a new number of copies.");
					String line2 = scan.nextLine();
					if (isInteger(line2)) {
						noOfCopies = Integer.parseInt(line2);
						String updateQuery = "UPDATE tbl_book_copies SET noOfCopies=? WHERE bookId=? AND branchId=?";
						pst = connection.prepareStatement(updateQuery);
						pst.setInt(1, noOfCopies);
						pst.setInt(2, bookId);
						pst.setInt(3, branch.getBranchId());
						pst.executeUpdate();
						System.out.println("Update Complete");
						librarianAccess();
					}
					else {
						System.out.println("Invalid Input. Please try again.");
						bookCopiesManagement();
					}
				}
				else if (answer1 == i) {
					librarianAccess();
				}
				else {
					System.out.println("Invalid Input. Please try again.");
					bookCopiesManagement();
				}
			}
			else {
				System.out.println("Invalid Input. Please try again.");
				bookCopiesManagement();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isInteger(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
}








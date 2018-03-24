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
public class Borrower {
	
	
	private Connection connection;
	private static PreparedStatement pst;
	private static ResultSet resultSet;
	
	private static String borrowerName;
	private static int borrowerCardNo;
	private static boolean checkOut;
	private Book book;
	private Branch branch;
	private static Scanner scan;
	
	public Borrower(Connection connection) {
		this.connection = connection;
		scan = new Scanner(System.in);
		borrowerMenu();
		scan.close();
	}
	
	private void borrowerMenu() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("*************");
		System.out.println("Borrower Menu");
		System.out.println("*************");
		System.out.println("Enter your card number.");
		System.out.println("Enter 0 to quit.");
		System.out.println("***********************");
		String line = scan.nextLine();
		if (isInteger(line)) {
			int cardNo = Integer.parseInt(line);
			if (cardNo == 0) {
				LMSDriver.mainMenu();
				return;
			}
			String selectQuery1 = "SELECT * FROM tbl_borrower WHERE cardNo=?";
			try {
				pst = connection.prepareStatement(selectQuery1);
				pst.setInt(1, cardNo);
				resultSet = pst.executeQuery(); 
				if (resultSet.next()) {
					String name = resultSet.getString("name");
					borrowerCardNo = cardNo;
					borrowerName = name;
					System.out.println("You are logedIn as: " + name);
					borrowerAccess();
				}
				else {
					System.out.println("Your card number does not match our record, please try again.");
					borrowerMenu();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Invalid Input. Please try again.");
			borrowerMenu();
		}
	}
	
	private void borrowerAccess() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		
		System.out.println("Welcome back " + borrowerName + ", what would you like to do? ");
		System.out.println("1) Check out a book");
		System.out.println("2) Return a book");
		System.out.println("3) Return to previous page");
		String answer = scan.nextLine();
		if (answer.equals("1")) {
			checkOut = true;
			branchMenu();
		}
		else if (answer.equals("2")) {
			checkOut = false;
			branchMenu();
		}
		else if (answer.equals("3")) {
			borrowerMenu();
		}
		else {
			System.out.println("Invalid Input. Please try again.");
			borrowerAccess();
		}	
	}
	
	private void bookCheckOut() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("*************************************");
	
		System.out.println("Pick the book you want to check out. ");
		System.out.println("*************************************");
		String selectQuery = "SELECT * FROM tbl_book JOIN tbl_book_copies ON tbl_book.bookId=tbl_book_copies.bookId WHERE branchId=? AND noOfCopies>0";
		try {
			pst = connection.prepareStatement(selectQuery);
			pst.setInt(1, branch.getBranchId());
			resultSet = pst.executeQuery();
			List<String> bookArray = new LinkedList<String>();
			int i = 1;
			while (resultSet.next()) {
				bookArray.add(resultSet.getString("title"));
				System.out.println(i + ") " + resultSet.getString("title"));
				i++;
			}
			System.out.println("******************************");
			System.out.println(i + ") Return to previous page.");
			System.out.println("******************************");
			String line = scan.nextLine();
			if (isInteger(line)) {
				int answer = Integer.parseInt(line);
				if (answer > 0 && answer < i) {
					String title = bookArray.get(answer - 1);
					String selectQuery2 = "SELECT * FROM tbl_book WHERE title=?";
					pst = connection.prepareStatement(selectQuery2);
					pst.setString(1, title);
					resultSet = pst.executeQuery();
					if (resultSet.next()) {
						book = new Book(resultSet.getInt("bookId"), title, resultSet.getInt("pubId"));
						
						
						// Check if the book of same title is already loaned out
						String selectQuery3 = "SELECT * FROM tbl_book_loans WHERE bookId=? AND branchId=? AND cardNo=? AND dateIn IS NULL";
						pst = connection.prepareStatement(selectQuery3);
						pst.setInt(1, book.getBookId());
						pst.setInt(2, branch.getBranchId());
						pst.setInt(3, borrowerCardNo);
						resultSet = pst.executeQuery();
						if (!resultSet.next()) {
							
							// Add data into tbl_book_loans
							try {
								String insertQuery = "INSERT INTO  tbl_book_loans(bookId, branchId, cardNo, dateOut, dueDate) VALUES(?,?,?,CURDATE(),DATE_ADD(CURDATE(), INTERVAL 30 DAY))";
								pst = connection.prepareStatement(insertQuery);
								pst.setInt(1, book.getBookId());
								pst.setInt(2, branch.getBranchId());
								pst.setInt(3, borrowerCardNo);
								pst.executeUpdate();		
							} catch (SQLException e) {
								String updateQuery = "UPDATE tbl_book_loans SET dateOut=CURDATE(), dueDate=DATE_ADD(CURDATE(), INTERVAL 30 DAY), dateIn=NULL WHERE bookId=? AND branchId=? AND cardNo=?";
								pst = connection.prepareStatement(updateQuery);
								pst.setInt(1, book.getBookId());
								pst.setInt(2, branch.getBranchId());
								pst.setInt(3, borrowerCardNo);
								pst.executeUpdate();
								
							}
							
							// Update data from tbl_book_copies
							String updateQuery2 = "UPDATE tbl_book_copies SET noOfCopies=noOfCopies-1 WHERE bookId=? AND branchId=?";
							pst = connection.prepareStatement(updateQuery2);
							pst.setInt(1, book.getBookId());
							pst.setInt(2, branch.getBranchId());
							pst.executeUpdate();
							System.out.println("**************************************************************");
							System.out.println("You have successfully check out " + book.getBookName() + ". ");
							System.out.println("**************************************************************");
							borrowerAccess();
						}
						else {
							System.out.println("*****************************************************************************************************");
							System.out.println("You have already loaned out " + title + ". You cannot check out the same book twice at the same time.");
							System.out.println("******************************************************************************************************");
							bookCheckOut();
						}
					}
					else {
						System.out.println("********************");
						System.out.println("An error has occured.");
						System.out.println("**********************");
					}
					
				}
				else if (answer == i) {
					borrowerAccess();
				}
				else {
					System.out.println("*******************************");
					System.out.println("Invalid Input. Please try again.");
					System.out.println("*******************************");
					bookCheckOut();
				}
			}
			else {
				System.out.println("*******************************");
				System.out.println("Invalid Input. Please try again.");
				System.out.println("********************************");
				bookCheckOut();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void bookReturn() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("*********************************");
	
		System.out.println("Pick the book you want to return. ");
		System.out.println("*********************************");
		String selectQuery = "SELECT * FROM tbl_book_loans JOIN tbl_book ON tbl_book_loans.bookId=tbl_book.bookId "
				+ "WHERE cardNo=? AND branchId=? AND dateIn IS NULL";
		try {
			pst = connection.prepareStatement(selectQuery);
			pst.setInt(1, borrowerCardNo);
			pst.setInt(2, branch.getBranchId());
			resultSet = pst.executeQuery();
			List<String> bookArray = new LinkedList<String>();
			int i = 1;
			while (resultSet.next()) {
				bookArray.add(resultSet.getString("title"));
				System.out.println(i + ") " + resultSet.getString("title") + ". Due date: " + resultSet.getString("dueDate"));
				i++;
			}
			if (i == 1) {
				System.out.println("****************************************************");
				System.out.println("You don't have any books check out from this branch.");
				System.out.println("****************************************************");
			}
			System.out.println("*****************************");
			System.out.println(i + ") Return to previous page.");
			System.out.println("*****************************");
			String line = scan.nextLine();
			if (isInteger(line)) {
				int answer = Integer.parseInt(line);
				if (answer > 0 && answer < i) {
					String selectQuery2 = "SELECT * FROM tbl_book WHERE title=?";
					pst = connection.prepareStatement(selectQuery2);
					pst.setString(1, bookArray.get(answer - 1));
					resultSet = pst.executeQuery();
					if (resultSet.next()) {
						
						// Update table tbl_book_loans. Change the dateIn to today's date
						book = new Book(resultSet.getInt("bookId"), bookArray.get(answer - 1), resultSet.getInt("pubId"));
						String updateQuery = "UPDATE tbl_book_loans SET dateIn=CURDATE() WHERE bookId=? AND branchId=? AND cardNo=?";
						pst = connection.prepareStatement(updateQuery);
						pst.setInt(1, book.getBookId());
						pst.setInt(2, branch.getBranchId());
						pst.setInt(3, borrowerCardNo);
						pst.executeUpdate();
						
						
						// Update table tbl_book_copies. Add 1 to noOfCopies.
						String updateQuery2 = "UPDATE tbl_book_copies SET noOfCopies=noOfCopies+1 WHERE bookId=? AND branchId=?";
						pst = connection.prepareStatement(updateQuery2);
						pst.setInt(1, book.getBookId());
						pst.setInt(2, branch.getBranchId());
						pst.executeUpdate();
						System.out.println("*******************************************************************");
						System.out.println("You have successfully returned the book " + book.getBookName() + ". ");
						System.out.println("*******************************************************************");
						borrowerAccess();
					}
				}
				else if (answer == i) {
					borrowerAccess();
				}
				else {
					System.out.println("Invalid Input. Please try again.");
					bookReturn();
				}
			}
			else {
				System.out.println("Invalid Input. Please try again.");
				bookReturn();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void branchMenu() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("*************************************************");
		System.out.println("Library Branches");
		System.out.println("Which branch do you want to check out books from?");
		System.out.println("*************************************************");
		try {
			String selectQuery1 = "SELECT * FROM tbl_library_branch";
			pst = connection.prepareStatement(selectQuery1);
			resultSet = pst.executeQuery();
			int i = 1;
			List<String> branchArray = new LinkedList<String>();
			while (resultSet.next()) {
				System.out.println(i + ") " + resultSet.getString("branchName"));
				branchArray.add(resultSet.getString("branchName"));
				i++;
			}
			System.out.println(i + ") Return to previous page.");
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
					if (checkOut) {
						bookCheckOut();
					}
					else {
						bookReturn();
					}
				}
				else if (answer == i) {
					borrowerMenu();
				}
				else {
					System.out.println("********************************");
					System.out.println("Invalid Input. Please try again.");
					System.out.println("********************************");
					branchMenu();
				}
			}
			else {
				System.out.println("*******************************");
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
	
	private boolean isInteger(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
}

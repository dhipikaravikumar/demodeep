/**
 * 
 */
package com.libraryManagement.project.jbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.Scanner;

import com.libraryManagement.project.jbc.LMSDriver;

/**
 * @author gcit
 */
public class Administrator {

	private Connection connection;
	// private Object ;
	private static PreparedStatement pSt;
	private static ResultSet rSet;
	private static State st;
	private static Scanner scanner;

	public Administrator(Connection connection) {

		this.connection = connection;
		scanner = new Scanner(System.in);
		adminMenu();
		scanner.close();
	}

	// Enumeration of states that is used to ADD,UPDATE, DELETE a book.
	private enum State {
		ADD, UPDATE, DELETE;
	}

	private void adminMenu() {

		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("****************************************");
		System.out.println("Administrator Menu:");
		System.out.println("(1) Add/Update/Delete Books and Author:");
		System.out.println("(2) Add/Update/Delete Library Branches:");
		System.out.println("(3) Add/Update/Delete Borrowers:");
		System.out.println("(4) Add/Update/Delete Publishers:");
		System.out.println("(5) Over-ride Due Date for a Book Loan:");
		System.out.println("(6) Quit to Previous Page:");
		System.out.println("****************************************");

		String options = scanner.nextLine();
		if (options.equals("1")) {
			bookManagement();

		} else if (options.equals("2")) {
			libraryManagement();

		} else if (options.equals("3")) {
			borrowerManagement();

		} else if (options.equals("4")) {
			publisherManagement();

		} else if (options.equals("5")) {
			bookLoansManagement();

		} else if (options.equals("6")) {
			LMSDriver.mainMenu();

		} else {
			System.out.println("You Entered an Invalid Option,Please try again!!");
			adminMenu();

		}
	}

	private void bookManagement() {

		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");

		System.out.println("Book Management Menu Options:");
		System.out.println("(1) Add a book:");
		System.out.println("(2) Update a book:");
		System.out.println("(3) Delete a book:");
		System.out.println("(4) Quit to previous page:");

		String options = scanner.nextLine();
		if (options.equals("1")) {

			st = State.ADD;
			addBookPrompt();

		} else if (options.equals("2")) {
			st = State.UPDATE;
			updateDeleteBookPrompt();

		} else if (options.equals("3")) {

			st = State.DELETE;
			updateDeleteBookPrompt();

		} else if (options.equals("4")) {
			adminMenu();

		} else {
			System.out.println("You entered an Invalid Input, Please try again!!");
			bookManagement();
		}
	}

	private void updateDeleteBookPrompt() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");

		if (st == State.UPDATE) {
			System.out.println("Please choose the book you wish to UPDATE:");

		} else {
			System.out.println("Please choose the book you wish to DELETE:");

		}
		try {
			List<String> bookArray = new LinkedList<String>();
			String selectQuery = "SELECT* FROM tbl_book";
			pSt = connection.prepareStatement(selectQuery);
			rSet = pSt.executeQuery();

			int x = 1;
			while (rSet.next()) {
				System.out.println(x + ")" + rSet.getString("title"));
				bookArray.add(rSet.getString("title"));

				x++;
			}
			System.out.println(x + ")Quit to Previous Page:");
			String line = scanner.nextLine();
			if (isInteger(line)) {
				int options = Integer.parseInt(line);
				if (options > 0 && options < x) {
					String bookTitle = bookArray.get(options - 1);

					int bookId = 0;

					String selectBookIdQuery = "SELECT*FROM tbl_book WHERE title=?";

					pSt = connection.prepareStatement(selectBookIdQuery);
					pSt.setString(1, bookTitle);
					if (rSet.next()) {
						bookId = rSet.getInt("bookId");

					}
					if (st == State.UPDATE) {
						updatePrompt(bookId, bookTitle);
					} else {
						deletePrompt(bookId, bookTitle);

					}
				} else if (options == x) {
					bookManagement();
				} else {
					System.out.println("You entered an Invalid Input,Please try again:");
					updateDeleteBookPrompt();

				}
			} else {
				System.out.println("You entered an Invalid Input,Please try again:");
				updateDeleteBookPrompt();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updatePrompt(int bookId, String bookTitle) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");

		String authorName = getAuthorName(bookId);
		String publisherName = getPublisherName(bookId);
		String bookGenre = getBookGenre(bookId);
		System.out.println("Please choose from the given OPTIONS what you would like to UPDATE:");

		System.out.println("(1) Book Title:" + bookTitle);
		System.out.println("(2) Author:" + authorName);
		System.out.println("(3) Publisher:" + publisherName);
		System.out.println("(4) Genre:" + bookGenre);
		System.out.println("(5) Quit to the Previous Page!!");

		String line = scanner.nextLine();

		if (isInteger(line)) {

			int options = Integer.parseInt(line);
			if (options == 1) {
				updateBookTitle(bookId);

			} else if (options == 2) {
				updateBookAuthor(bookId);
				deleteAuthor(authorName);

			} else if (options == 3) {
				updateBookPublisher(bookId);
				deleteAuthor(authorName);

			} else if (options == 4) {
				updateGenre(bookId);
				deleteGenre(bookGenre);

			} else if (options == 5) {
				updateDeleteBookPrompt();

			} else {
				System.out.println("You entered an Invalid Input, Please try again!!");
				updatePrompt(bookId, bookTitle);
			}

		}
	}

	private void updateBookTitle(int bookId) {
		System.out.println("Please enter the new book title you wish to UPDATE:");
		String bookName = scanner.nextLine();
		if (bookName.length() > 45) {
			System.out.println("CAUTION!! Your input cannot exceed 45 characters.");
			updateBookTitle(bookId);

		}
		if (bookName.length() == 0) {
			System.out.println("CAUTION!! Input CANNOT be empty:");
			updateBookTitle(bookId);

		} else {

			if (getBookId(bookName) == 0) {
				updateTitle(bookId, bookName);
				updatePrompt(bookId, getBookTitle(bookId));

			} else {
				System.out.println("CAUTION!! Title already exists.");
				updateBookTitle(bookId);
			}
		}

	}

	private void updateBookAuthor(int bookId) {

		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");

		System.out.println("Choose a new author:");
		String authorName = listAuthors();
		updateAuthorName(bookId, authorName);
		updatePrompt(bookId, getBookTitle(bookId));

	}

	private String listAuthors() {
		String selectQuery = "SELECT*FROM tbl_author";
		List<String> authorList = new LinkedList<String>();
		int x = 1;

		try {
			pSt = connection.prepareStatement(selectQuery);
			rSet = pSt.executeQuery();
			while (rSet.next()) {
				authorList.add(rSet.getString("authorName"));
				System.out.println(x + ")" + rSet.getString("authorName"));
				x++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println(x + ") Add a new author-->");
		String line = scanner.nextLine();
		if (isInteger(line)) {
			int options = Integer.parseInt(line);
			if (options > 0 && options < x) {
				return authorList.get(options - 1);

			} else if (options == x) {
				System.out.println("Please enter your desired new authors name-->");
				String authorName = scanner.nextLine();
				addAuthor(authorName);
				return authorName;

			} else {
				System.out.println("You entered an Invalid Input,Please try again");
				return listAuthors();

			}
		} else {
			System.out.println("You entered an Invalid Input,Please try again");
			return listAuthors();
		}

	}

	private void deleteAuthor(String authorName) {

		String selectQuery = "SELECT* FROM tbl_author JOIN tbl_book_authors ON"
				+ "tbl_author.authorId=tbl_book_authors.authorId WHERE authorName=? ";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, authorName);
			pSt.executeQuery();

		} catch (SQLException e) {
			e.printStackTrace();

		}

	}

	private void updateBookPublisher(int bookId) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("Please choose a new publisher-->");
		String publisherName = listPublishers();
		int publisherId = getPublisherId(publisherName);
		updatePublisher(publisherId, bookId);
		updatePrompt(bookId, getBookTitle(bookId));

	}

	private String listPublishers() {

		String selectQuery = "SELECT *FROM tbl_publisher";
		int x = 1;
		List<String> publisherArray = new LinkedList<String>();
		try {

			pSt = connection.prepareStatement(selectQuery);
			rSet = pSt.executeQuery();
			while (rSet.next()) {
				publisherArray.add(rSet.getString("publisherName"));
				System.out.println(x + ")" + rSet.getString("publisherName"));
				x++;

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (st == State.UPDATE) {
			System.out.println(x + ") Please add a new publisher-->");

		}

		String line = scanner.nextLine();
		if (isInteger(line)) {
			int options = Integer.parseInt(line);
			if (options > 0 && options < x) {
				return publisherArray.get(options - 1);

			} else if (options == x && st == State.UPDATE) {

				System.out.println("Please enter the name of the new publisher-->");
				String publisherName = scanner.nextLine();

				System.out.println("Please enter the publisher's address-->");
				String publisherAddress = scanner.nextLine();

				System.out.println("Please enter the publisher's phone-->");
				String publisherPhone = scanner.nextLine();
				addPublisher(publisherName, publisherAddress, publisherPhone);
				return publisherName;

			} else {
				System.out.println("You entered an Invalid Input, Please try again!!");
				return listPublishers();

			}
		} else {
			System.out.println("You entered an Invalid Input, Please try again!!");
			return listPublishers();
		}
	}

	private void deletePublisher(String publisherName) {

		int publisherId = getPublisherId(publisherName);

		String selectQuery = "SELECT*FROM tbl_book WHERE pubId=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, publisherId);
			rSet = pSt.executeQuery();
			if (!rSet.next()) {
				String deleteQuery = "SELECT FROM tbl_publisher WHERE publisherId=?";

				pSt = connection.prepareStatement(deleteQuery);
				pSt.setInt(1, publisherId);
				pSt.executeQuery();

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void updateGenre(int bookId) {

		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");

		System.out.println("Please choose a new genre-->");
		String genre = listGenre();
		updateGenre(bookId, genre);
		updatePrompt(bookId, getBookTitle(bookId));

	}

	private String listGenre() {

		String selectQuery = "SELECT *FROM tbl_genre ";
		int x = 1;
		List<String> genreArray = new LinkedList<String>();

		try {
			pSt = connection.prepareStatement(selectQuery);
			rSet = pSt.executeQuery();
			while (rSet.next()) {
				genreArray.add(rSet.getString("genre_name"));
				System.out.println(x + ")" + rSet.getString("genre_name"));
				x++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(x + ") Please add a new genre-->");
		String line = scanner.nextLine();
		if (isInteger(line)) {
			int options = Integer.parseInt(line);
			if (options > 0 && options < x) {
				return genreArray.get(options - 1);

			} else if (options == x) {
				System.out.println("Please enter the new genre name-->");
				String genre = scanner.nextLine();
				addGenre(genre);
				return genre;

			} else {
				System.out.println("CAUTION!! Invalid Input, Please try again-->");
				return listGenre();

			}

		} else {
			System.out.println("CAUTION!! Invalid Input, Please try again-->");
			return listGenre();
		}
	}

	private void deleteGenre(String genre) {
		int genreId = getGenreId(genre);
		String selectQuery = "SELECT * FROM tbl_book_genres WHERE genre_id=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, genreId);
			rSet = pSt.executeQuery();
			if (!rSet.next()) {
				String deleteQuery = "DELETE FROM tbl_genre WHERE genre_id=?";
				pSt = connection.prepareStatement(deleteQuery);
				pSt.setInt(1, genreId);
				pSt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void deletePrompt(int bookId, String title) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("Deleting " + title + ", are you sure? (Y/N)");
		String options = scanner.nextLine();
		if (options.toLowerCase().equals("y")) {
			deleteBook(bookId, title);
		} else if (options.toLowerCase().equals("n")) {
			bookManagement();
		} else {
			System.out.println("CAUTION!! Invalid Input. Please try again-->");
			deletePrompt(bookId, title);
		}
	}

	private void deleteBook(int bookId, String title) {
		try {
			String selectQuery = "SELECT * FROM tbl_book_loans WHERE bookId=? AND dateIn IS NULL";
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, bookId);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				System.out.println("This book has copies loaned out, you cannot delete it.");
				adminMenu();
			} else {
				String authorName = getAuthorName(bookId);
				String pubName = getPublisherName(bookId);
				String genre = getBookGenre(bookId);

				String deleteBookQuery = " DELETE FROM tbl_book WHERE bookId=?";
				pSt = connection.prepareStatement(deleteBookQuery);
				pSt.setInt(1, bookId);
				pSt.executeUpdate();

				deleteAuthor(authorName);
				deletePublisher(pubName);
				deleteGenre(genre);

				System.out.println(title + " is deleted from the system.");
				adminMenu();
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	private void libraryManagement() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("******************************");
		System.out.println("Library Management");
		System.out.println("(1) Add a branch");
		System.out.println("(2) Update a branch");
		System.out.println("(3) Delete a branch");
		System.out.println("(4) Return to previous page.");
		System.out.println("******************************");
		String options = scanner.nextLine();
		if (options.equals("1")) {
			st = State.ADD;
			System.out.println("Choose a brach to add.");
			addBranch();
		} else if (options.equals("2")) {
			st = State.UPDATE;
			System.out.println("Choose a branch to update.");
			listBranch();
		} else if (options.equals("3")) {
			st = State.DELETE;
			System.out.println("Choose a branch to delete.");
			listBranch();
		} else if (options.equals("4")) {
			adminMenu();
		} else {
			System.out.println("Invalid Input. Please try again.");
			libraryManagement();
		}
	}

	private void addBranch() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("Please enter the branch's name-->");
		String branchName = scanner.nextLine();
		String selectQuery = "SELECT * FROM tbl_library_branch WHERE branchName=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, branchName);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				System.out.println("CAUTION!! Branch name already exist, choose another name-->");
				addBranch();
			} else {
				System.out.println("Please enter the branch's address-->");
				String branchAddress = scanner.nextLine();
				String insertQuery = "INSERT INTO tbl_library_branch (branchName, branchAddress) VALUES(?,?)";
				pSt = connection.prepareStatement(insertQuery);
				pSt.setString(1, branchName);
				pSt.setString(2, branchAddress);
				pSt.executeUpdate();
				List<Integer> bookIdArray = new LinkedList<Integer>();
				String selectBookIdQuery = "SELECT * FROM tbl_book";
				pSt = connection.prepareStatement(selectBookIdQuery);
				rSet = pSt.executeQuery();
				while (rSet.next()) {
					bookIdArray.add(rSet.getInt("bookId"));
				}
				int branchId = getBranchId(branchName);
				String insertCopiesQuery = "INSERT INTO tbl_book_copies VALUES(?,?,0)";
				for (int i = 0; i < bookIdArray.size(); i++) {
					pSt = connection.prepareStatement(insertCopiesQuery);
					pSt.setInt(1, bookIdArray.get(i));
					pSt.setInt(2, branchId);
					pSt.executeUpdate();
				}
				System.out.println("The branch " + branchName + " is successfully added into the system.");
				libraryManagement();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private int getBranchId(String branchName) {
		String selectQuery = "SELECT * FROM tbl_library_branch WHERE branchName=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, branchName);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getInt("branchId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void listBranch() {
		String selectQuery = "SELECT * FROM tbl_library_branch";
		int x = 1;
		List<String> branchArray = new LinkedList<String>();
		try {
			pSt = connection.prepareStatement(selectQuery);
			rSet = pSt.executeQuery();
			while (rSet.next()) {
				System.out.println(x + ") " + rSet.getString("branchName"));
				branchArray.add(rSet.getString("branchName"));
				x++;
			}
			System.out.println(x + ") Return to previous page.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String line = scanner.nextLine();
		if (isInteger(line)) {
			int options = Integer.parseInt(line);
			if (options > 0 && options < x) {
				int branchId = getBranchId(branchArray.get(options - 1));
				if (st == State.UPDATE) {
					updateBranch(branchId);
				} else {
					deleteBranch(branchId);
				}
			} else if (options == x) {
				libraryManagement();
			} else {
				System.out.println("CAUTION!! Invalid Input, Please try again.");
				listBranch();
			}
		} else {
			System.out.println("Invalid Input. Please try again.");
			listBranch();
		}
	}

	private void deleteBranch(int branchId) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");

		String selectQuery = "SELECT * FROM tbl_book_loans WHERE branchId=? AND dateIn IS NULL";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, branchId);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				System.out.println("This branch has books loaned out, you cannot delete it. ");
				libraryManagement();
			} else {
				String branchName = getBranchName(branchId);
				System.out.println("Deleting the branch: " + branchName);
				System.out.println("Are you sure? (Y/N)");
				String options = scanner.nextLine();
				if (options.toLowerCase().equals("y")) {
					deleteBookLoans(branchId);
					deleteBookCopies(branchId);
					String deleteQuery = "DELETE FROM tbl_library_branch WHERE branchId=?";
					pSt = connection.prepareStatement(deleteQuery);
					pSt.setInt(1, branchId);
					pSt.executeUpdate();
					System.out.println("The branch " + branchName + " is now deleted from the system.");
					libraryManagement();
				} else if (options.toLowerCase().equals("n")) {
					libraryManagement();
				} else {
					System.out.println("Invalid Input. Please try again.");
					deleteBranch(branchId);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void deleteBookLoans(int branchId) {
		String deleteQuery = "DELETE FROM tbl_book_loans WHERE branchId=?";
		try {
			pSt = connection.prepareStatement(deleteQuery);
			pSt.setInt(1, branchId);
			pSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void deleteBookCopies(int branchId) {
		String deleteQuery = "DELETE FROM tbl_book_copies WHERE branchId=?";
		try {
			pSt = connection.prepareStatement(deleteQuery);
			pSt.setInt(1, branchId);
			pSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updateBranch(int branchId) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("1) Update branch name. Name: " + getBranchName(branchId));
		System.out.println("(2) Update branch address. Address: " + getBranchAddress(branchId));
		System.out.println("(3) Return to previous page.");
		String options = scanner.nextLine();
		if (options.equals("1")) {
			System.out.println("Please enter a new branch name-->");
			System.out.println("Please enter 'quit' to return to previous-->");
			updateBranchName(branchId);
		} else if (options.equals("2")) {
			System.out.println("Please enter a new branch address-->");
			System.out.println("Please enter 'quit' to return to previous-->");
			udpateBranchAddress(branchId);
		} else if (options.equals("3")) {
			libraryManagement();
		} else {
			System.out.println("CAUTION!! Invalid Input. Please try again.");
			updateBranch(branchId);
		}
	}

	private String getBranchName(int branchId) {
		String selectQuery = "SELECT * FROM tbl_library_branch WHERE branchId=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, branchId);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("branchName");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String getBranchAddress(int branchId) {
		String selectQuery = "SELECT * FROM tbl_library_branch WHERE branchId=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, branchId);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("branchAddress");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void updateBranchName(int branchId) {
		String branchName = scanner.nextLine();
		if (branchName.toLowerCase().equals("quit")) {
			libraryManagement();
		} else {
			String selectQuery = "SELECT * FROM tbl_library_branch WHERE branchName=?";
			try {
				pSt = connection.prepareStatement(selectQuery);
				pSt.setString(1, branchName);
				rSet = pSt.executeQuery();
				if (rSet.next()) {
					System.out.println("This branch name already exists. Choose another one.");
					updateBranchName(branchId);
				} else {
					String updateQuery = "UPDATE tbl_library_branch SET branchName=? WHERE branchId=?";
					pSt = connection.prepareStatement(updateQuery);
					pSt.setString(1, branchName);
					pSt.setInt(2, branchId);
					pSt.executeUpdate();
					System.out.println("Update complete.");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void udpateBranchAddress(int branchId) {
		String branchAddress = scanner.nextLine();
		if (branchAddress.toLowerCase().equals("quit")) {
			libraryManagement();
		} else {
			try {
				String updateQuery = "UPDATE tbl_library_branch SET branchAddress=? WHERE branchId=?";
				pSt = connection.prepareStatement(updateQuery);
				pSt.setString(1, branchAddress);
				pSt.setInt(2, branchId);
				pSt.executeUpdate();
				System.out.println("Update complete.");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void publisherManagement() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("Choose a publisher to update.");
		st = State.UPDATE;
		String pubName = listPublishers();
		int pubId = getPublisherId(pubName);
		updatePubPrompt(pubId);
	}

	private void updatePubPrompt(int pubId) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("************************************");
		System.out.println("(1) Update publisher name.");
		System.out.println("(2) Update publisher address.");
		System.out.println("(3) Update publisher phone number.");
		System.out.println("(4) Return to previous page.");
		System.out.println("************************************");
		String options = scanner.nextLine();
		if (options.equals("1")) {
			updatePublisherName(pubId);
		} else if (options.equals("2")) {
			// getPubAddress();

		} else if (options.equals("3")) {
			updatePublisherName(pubId);
		} else if (options.equals("4")) {
			publisherManagement();
		} else {
			System.out.println("Invalid Input. Please try again.");
			updatePubPrompt(pubId);
		}
	}

	private void updatePublisherName(int pubId) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("Enter a new publisher name.");
		System.out.println("Enter 'quit' to return to previous page.");
		String pubName = scanner.nextLine();
		String selectQuery = "SELECT * FROM tbl_publisher WHERE pubName=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, pubName);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				System.out.println("Invalid Input. Please try again.");
				updatePublisherName(pubId);
			} else {

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void borrowerManagement() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("********************");
		System.out.println("Borrower Management");
		System.out.println("********************");
		System.out.println("(1) Add a borrower");
		System.out.println("(2) Update a borrower");
		System.out.println("(3) Delete a borrower");
		System.out.println("(4) Return to previous page!!");
		System.out.println("*****************************");
		if (scanner.hasNextInt()) {
			int options = scanner.nextInt();
			if (options == 1) {
				addBorrower();
			} else if (options == 2) {
				updateBorrowerPrompt();
			} else if (options == 3) {
				deleteBorrowerPrompt();
			} else if (options == 4) {
				adminMenu();
			} else {
				System.out.println("CAUTION!! Invalid Input. Please try again.");
				borrowerManagement();
			}
		} else {
			System.out.println("CAUTION!! Invalid Input. Please try again.");
			borrowerManagement();
		}
	}

	private void addBorrower() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("**************************");
		System.out.println("Enter the borrower's name.");
		System.out.println("Enter quit at any prompt to exit.");
		System.out.println("*********************************");
		try {
			String name = scanner.nextLine();
			if (name.toLowerCase().equals("quit")) {
				borrowerManagement();
				scanner.close();
				return;
			}
			String selectQuery = "SELECT * FROM tbl_borrower WHERE name=?";
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, name);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				System.out.println("Borrower with this name already exist, enter a new name.");
				addBorrower();
			}
			System.out.println("Please enter the borrower's address-->");
			String address = scanner.nextLine();
			if (address.toLowerCase().equals("quit")) {
				borrowerManagement();
				scanner.close();
				return;
			}
			System.out.println("Enter the borrower's phone number.");
			String phone = scanner.nextLine();
			if (phone.toLowerCase().equals("quit")) {
				borrowerManagement();
				scanner.close();
				return;
			}
			String insertQuery = "INSERT INTO tbl_borrower (name, address, phone) VALUES (?,?,?)";
			pSt = connection.prepareStatement(insertQuery);
			pSt.setString(1, name);
			pSt.setString(2, address);
			pSt.setString(3, phone);
			pSt.executeUpdate();
			System.out.println("New borrower added.");
			borrowerManagement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updateBorrowerPrompt() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("Choose a borrower to update.");
		String selectQuery = "SELECT * FROM tbl_borrower";
		try {
			pSt = connection.prepareStatement(selectQuery);
			rSet = pSt.executeQuery();
			List<Integer> cardArray = new LinkedList<Integer>();
			int i = 1;
			while (rSet.next()) {
				System.out.println(i + ") " + rSet.getString("name"));
				cardArray.add(rSet.getInt("cardNo"));
				i++;
			}
			System.out.println(i + ") Return to previous page.");
			Scanner scan = new Scanner(System.in);
			if (scan.hasNextInt()) {
				int options = scan.nextInt();
				if (options > 0 && options < i) {
					int cardNo = cardArray.get(options - 1);
					updateBorrower(cardNo);
				} else if (options == i) {
					borrowerManagement();
				} else {
					System.out.println("Invalid Input. Please try again.");
					updateBorrowerPrompt();
				}
			} else {
				System.out.println("Invalid Input. Please try again.");
				updateBorrowerPrompt();
			}
			scan.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updateBorrower(int cardNo) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("*************************************");
		System.out.println("Which information do ou want to update.");
		System.out.println("(1) Name: " + getBorrowerName(cardNo));
		System.out.println("(2) Address: " + getBorrowerAddress(cardNo));
		System.out.println("(3) Phone: " + getBorrowerPhone(cardNo));
		System.out.println("(4) Return to previous page.");
		System.out.println("*************************************");
		if (scanner.hasNextInt()) {
			int options = scanner.nextInt();
			if (options == 1) {
				updateName(cardNo);
			} else if (options == 2) {
				updateAddress(cardNo);
			} else if (options == 3) {
				updatePhone(cardNo);
			} else if (options == 4) {
				borrowerManagement();
			} else {
				System.out.println("CAUTION!! Invalid Input. Please try again.");
				updateBorrower(cardNo);
			}
		} else {
			System.out.println("Invalid Input. Please try again.");
			updateBorrower(cardNo);
		}
	}

	private void updateName(int cardNo) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("Enter a new name.");
		String name = scanner.nextLine();
		String selectQuery = "SELECT * FROM tbl_borrower WHERE name=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, name);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				System.out.println("A borrower with this name already exist. Please choose another name.");
				updateBorrower(cardNo);
			}
			String updateQuery = "UPDATE tbl_borrower SET name=? WHERE cardNo=?";
			pSt = connection.prepareStatement(updateQuery);
			pSt.setString(1, name);
			pSt.setInt(2, cardNo);
			pSt.executeUpdate();
			System.out.println("Update complete.");
			updateBorrower(cardNo);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updateAddress(int cardNo) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("*********************************************");
		System.out.println("Please enter a new address.");
		System.out.println("Please enter quit to return to previous page.");
		System.out.println("*********************************************");
		String address = scanner.nextLine();
		if (address.toLowerCase().equals("quit")) {
			updateBorrower(cardNo);
		}
		try {
			String updateQuery = "UPDATE tbl_borrower SET address=? WHERE cardNo=?";
			pSt = connection.prepareStatement(updateQuery);
			pSt.setString(1, address);
			pSt.setInt(2, cardNo);
			pSt.executeUpdate();
			System.out.println("Update complete.");
			updateBorrower(cardNo);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updatePhone(int cardNo) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("**************************************");
		System.out.println("Enter a new phone number.");
		System.out.println("Enter quit to return to previous page.");
		System.out.println("**************************************");
		String phone = scanner.nextLine();
		if (phone.toLowerCase().equals("quit")) {
			updateBorrower(cardNo);
		}
		try {
			String updateQuery = "UPDATE tbl_borrower SET phone=? WHERE cardNo=?";
			pSt = connection.prepareStatement(updateQuery);
			pSt.setString(1, phone);
			pSt.setInt(2, cardNo);
			pSt.executeUpdate();
			System.out.println("Update complete.");
			updateBorrower(cardNo);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void deleteBorrowerPrompt() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("**********************************************************");
		System.out.println("Please choose a borrower to delete.");
		System.out.println("**********************************************************");
		String selectQuery = "SELECT * FROM tbl_borrower";
		try {
			pSt = connection.prepareStatement(selectQuery);
			rSet = pSt.executeQuery(selectQuery);
			List<Integer> cardArray = new LinkedList<Integer>();
			int i = 1;
			while (rSet.next()) {
				System.out.println(i + ") " + rSet.getString("name"));
				cardArray.add(rSet.getInt("cardNo"));
				i++;
			}
			System.out.println("**********************************************************");
			System.out.println(i + ") Return to previous page.");
			System.out.println("**********************************************************");
			if (scanner.hasNextInt()) {
				int options = scanner.nextInt();
				if (options > 0 && options < i) {
					int cardNo = cardArray.get(options - 1);
					deleteBorrower(cardNo);
				} else if (options == i) {
					borrowerManagement();
				} else {
					System.out.println("**********************************************************");
					System.out.println("CAUTION!! Invalid Input. Please try again.");
					System.out.println("**********************************************************");
					deleteBorrowerPrompt();
				}
			} else {
				System.out.println("**********************************************************");
				System.out.println("CAUTION!! Invalid Input. Please try again.");
				System.out.println("**********************************************************");
				deleteBorrowerPrompt();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void deleteBorrower(int cardNo) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("Deleting the borrower... " + getBorrowerName(cardNo));
		String selectQuery = "SELECT * FROM tbl_book_loans WHERE cardNo=? AND dateIn IS NULL";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, cardNo);
			rSet = pSt.executeQuery(selectQuery);
			if (rSet.next()) {
				System.out.println("*******************************************************************");
				System.out.println("This borrower has books loaned out, you cannot delete this borrower.");
				System.out.println("*******************************************************************");
				adminMenu();
				return;
			}
			System.out.println("*******************");
			System.out.println("Are you sure? (Y/N)");
			System.out.println("*******************");
			String options = scanner.nextLine();
			if (options.toLowerCase().equals("y")) {

				String deleteBorrowerLoanQuery = "DELETE FROM tbl_book_loans WHERE cardNo=?";
				pSt = connection.prepareStatement(deleteBorrowerLoanQuery);
				pSt.setInt(1, cardNo);
				pSt.executeQuery();
				String deleteBorrowerQuery = "DELETE FROM tbl_borrower WHERE cardNo=?";
				pSt = connection.prepareStatement(deleteBorrowerQuery);
				pSt.setInt(1, cardNo);
				pSt.executeQuery(deleteBorrowerLoanQuery);
				System.out.println("***************************************");
				System.out.println("The borrower is deleted from the system.");
				System.out.println("***************************************");
				borrowerManagement();
			} else if (options.toLowerCase().equals("n")) {
				borrowerManagement();
			} else {
				System.out.println("******************************************");
				System.out.println("CAUTION!! Invalid Input. Please try again.");
				System.out.println("******************************************");
				deleteBorrower(cardNo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void bookLoansManagement() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("**********************************************************");
		System.out.println("Override Book Loan Due Date");
		System.out.println("**********************************************************");
		String selectQuery = "SELECT * FROM tbl_book_loans WHERE dateIn IS NULL";
		try {
			pSt = connection.prepareStatement(selectQuery);
			rSet = pSt.executeQuery();
			List<Integer> bookIdArray = new LinkedList<Integer>();
			List<Integer> branchIdArray = new LinkedList<Integer>();
			List<Integer> cardNoArray = new LinkedList<Integer>();
			int i = 1;
			while (rSet.next()) {
				bookIdArray.add(rSet.getInt("bookId"));
				branchIdArray.add(rSet.getInt("branchId"));
				cardNoArray.add(rSet.getInt("cardNo"));

				System.out.println(i + ") bookId: " + rSet.getInt("bookId") + " branchId: " + rSet.getInt("branchId")
						+ " cardNo: " + rSet.getInt("cardNo") + " due date: " + rSet.getString("dueDate"));
				i++;
			}
			System.out.println(i + ") Return to previous page!!");
			System.out.println("**********************************************************");

			if (scanner.hasNextInt()) {
				int options = scanner.nextInt();
				if (options > 0 && options < i) {
					int bookId = bookIdArray.get(options - 1);
					int branchId = branchIdArray.get(options - 1);
					int cardNo = cardNoArray.get(options - 1);
					overrideDueDate(bookId, branchId, cardNo);
				} else if (options == i) {
					adminMenu();
				} else {
					System.out.println("CAUTION!! Invalid Input. Please try again.");
					bookLoansManagement();
				}
			} else {
				System.out.println("CAUTION!! Invalid Input. Please try again.");
				bookLoansManagement();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void overrideDueDate(int bookId, int branchId, int cardNo) {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("******************************");
		System.out.println("Please enter the new due date.");
		System.out.println("******************************");
		String dueDate = scanner.nextLine();
		try {
			String updateQuery = "UPDATE tbl_book_loans SET dueDate=? WHERE bookId=? AND branchId=? AND cardNo=?";
			pSt = connection.prepareStatement(updateQuery);
			pSt.setString(1, dueDate);
			pSt.setInt(2, bookId);
			pSt.setInt(3, branchId);
			pSt.setInt(4, cardNo);
			pSt.executeUpdate(updateQuery);

			System.out.println("Override complete.");
			adminMenu();
		} catch (SQLException e) {
			System.out.println("******************************************");
			System.out.println("CAUTION!! Invalid Input. Please try again.");
			System.out.println("******************************************");
			overrideDueDate(bookId, branchId, cardNo);
		}
	}

	private void addBookPrompt() {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("***********************");
		System.out.println("Enter the book's title.");
		System.out.println("***********************");
		String title = scanner.nextLine();
		if (title.equals("0")) {
			bookManagement();
			return;
		}
		try {
			String selectTitleQuery = "SELECT * FROM tbl_book WHERE title=?";
			pSt = connection.prepareStatement(selectTitleQuery);
			pSt.setString(1, title);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				System.out.println("******************************************************************");
				System.out.println("The book with the title " + title + " already exist in the system.");
				System.out.println("******************************************************************");
				addBookPrompt();
			} else {
				System.out.println("Choose the author.");
				String author = listAuthors();
				if (author.equals("0")) {
					bookManagement();
					return;
				}
				System.out.println("***************************************");
				System.out.println("Enter the name of the book's publisher.");
				System.out.println("***************************************");
				String pubName = listPublishers();
				System.out.println("***********************");
				System.out.println("Enter the book's genre.");
				System.out.println("***********************");
				String genre = listGenre();
				if (genre.equals("0")) {
					bookManagement();
					return;
				}
				addBookReview(title, author, pubName, genre);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void addBookReview(String title, String author, String pubName, String genre) {
		System.out.println("*******************************************************************");
		System.out.println("You have successfully added the book " + title + ". ");
		System.out.println("Book title: " + title);
		System.out.println("Book author: " + author);
		System.out.println("Publisher: " + pubName);
		System.out.println("Genre: " + genre);
		System.out.println("*******************************************************************");
		int authorId = getAuthorId(author);
		int pubId = getPublisherId(pubName);
		int genreId = getGenreId(genre);

		addBook(title, pubId);
		int bookId = getBookId(title);
		labelAuthor(bookId, authorId);
		labelGenre(bookId, genreId);
		addBookCopies(bookId);
		bookManagement();
	}

	private boolean isInteger(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private String getBorrowerName(int cardNo) {
		String selectQuery = "SELECT * FROM tbl_borrower WHERE cardNo=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, cardNo);
			rSet = pSt.executeQuery(selectQuery);
			if (rSet.next()) {
				return rSet.getString("name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String getBorrowerAddress(int cardNo) {
		String selectQuery = "SELECT * FROM tbl_borrower WHERE cardNo=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, cardNo);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("address");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String getBorrowerPhone(int cardNo) {
		String selectQuery = "SELECT * FROM tbl_borrower WHERE cardNo=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, cardNo);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("phone");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void addBookCopies(int bookId) {
		List<Integer> branchIdArray = new LinkedList<Integer>();
		try {
			String selectQuery = "SELECT * FROM tbl_library_branch";
			pSt = connection.prepareStatement(selectQuery);
			rSet = pSt.executeQuery();
			while (rSet.next()) {
				branchIdArray.add(rSet.getInt("branchId"));
			}
			String insertQuery = "INSERT INTO tbl_book_copies VALUES(?,?,0)";
			pSt = connection.prepareStatement(insertQuery);
			pSt.setInt(1, bookId);
			for (int i = 0; i < branchIdArray.size(); i++) {
				pSt.setInt(2, branchIdArray.get(i));
				pSt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void addBook(String title, int pubId) {
		String insertQuery = "INSERT INTO tbl_book (title, pubId) VALUES(?,?)";
		try {
			pSt = connection.prepareStatement(insertQuery);
			pSt.setString(1, title);
			pSt.setInt(2, pubId);
			pSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private int getBookId(String title) {
		String selectQuery = "SELECT * FROM tbl_book WHERE title=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, title);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getInt("bookId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private String getBookTitle(int bookId) {
		String selectQuery = "SELECT * FROM tbl_book WHERE bookId=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, bookId);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("title");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void updateTitle(int bookId, String title) {
		try {
			String updateQuery = "UPDATE tbl_book SET title=? WHERE bookId=?";
			pSt = connection.prepareStatement(updateQuery);
			pSt.setString(1, title);
			pSt.setInt(2, bookId);
			pSt.executeUpdate();
			System.out.println("Update complete.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void labelAuthor(int bookId, int authorId) {
		try {
			if (st == State.ADD) {
				String insertQuery = "INSERT INTO tbl_book_authors VALUES(?,?)";
				pSt = connection.prepareStatement(insertQuery);
				pSt.setInt(1, bookId);
				pSt.setInt(2, authorId);
			} else if (st == State.UPDATE) {
				String updateQuery = "UPDATE tbl_book_authors SET authorId=? WHERE bookId=?";
				pSt = connection.prepareStatement(updateQuery);
				pSt.setInt(1, authorId);
				pSt.setInt(2, bookId);
			}
			pSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void labelGenre(int bookId, int genreId) {
		try {
			if (st == State.ADD) {
				String insertQuery = "INSERT INTO tbl_book_genres VALUES(?,?)";
				pSt = connection.prepareStatement(insertQuery);
				pSt.setInt(1, genreId);
				pSt.setInt(2, bookId);
				pSt.executeUpdate();
			} else if (st == State.UPDATE) {
				String updateQuery = "UPDATE tbl_book_genres SET genre_id=? WHERE bookId=?";
				pSt = connection.prepareStatement(updateQuery);
				pSt.setInt(1, genreId);
				pSt.setInt(2, bookId);
				pSt.executeUpdate();
				System.out.println("Update complete.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String getPublisherPhone(String pubName) {
		String selectQuery = "SELECT * FROM tbl_publisher WHERE publisherName=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, pubName);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("publisherPhone");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String getPublisherAddress(String pubName) {
		String selectQuery = "SELECT * FROM tbl_publisher WHERE publisherName=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, pubName);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("publisherAddress");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private int getPublisherId(String pubName) {
		String selectQuery = "SELECT * FROM tbl_publisher WHERE publisherName=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, pubName);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getInt("publisherId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private String getPublisherName(int bookId) {
		try {
			String selectQuery = "SELECT * FROM tbl_publisher JOIN tbl_book ON tbl_publisher.publisherId=tbl_book.pubId WHERE bookId=?";
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, bookId);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("publisherName");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void addPublisher(String pubName, String pubAddress, String pubPhone) {
		String insertQuery = "INSERT INTO tbl_publisher (publisherName, publisherAddress, publisherPhone) VALUES(?,?,?)";
		try {
			pSt = connection.prepareStatement(insertQuery);
			pSt.setString(1, pubName);
			pSt.setString(2, pubAddress);
			pSt.setString(3, pubPhone);
			pSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updatePublisher(int pubId, int bookId) {
		String updateQuery = "UPDATE tbl_book SET pubId=? WHERE bookId=?";
		try {
			pSt = connection.prepareStatement(updateQuery);
			pSt.setInt(1, pubId);
			pSt.setInt(2, bookId);
			pSt.executeUpdate();
			System.out.println("Update complete.");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private int getGenreId(String genre) {
		String selectQuery = "SELECT * FROM tbl_genre WHERE genre_name=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, genre);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getInt("genre_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private String getBookGenre(int bookId) {
		try {
			String selectQuery = "SELECT * FROM tbl_genre JOIN tbl_book_genres ON tbl_genre.genre_id=tbl_book_genres.genre_id WHERE bookId=?";
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, bookId);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("genre_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void addGenre(String genre) {
		String insertQuery = "INSERT INTO tbl_genre (genre_name) VALUE(?)";
		try {
			pSt = connection.prepareStatement(insertQuery);
			pSt.setString(1, genre);
			pSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updateGenre(int bookId, String genre) {
		int genreId = getGenreId(genre);
		if (genreId == 0) {
			addGenre(genre);
			genreId = getGenreId(genre);
		}
		labelGenre(bookId, genreId);
	}

	private int getAuthorId(String author) {
		String selectAuthorQuery = "SELECT * FROM tbl_author WHERE authorName=?";
		try {
			pSt = connection.prepareStatement(selectAuthorQuery);
			pSt.setString(1, author);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getInt("authorId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void addAuthor(String author) {
		String insertAuthorQuery = "INSERT INTO tbl_author (authorName) VALUES (?)";
		try {
			pSt = connection.prepareStatement(insertAuthorQuery);
			pSt.setString(1, author);
			pSt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String getAuthorName(int bookId) {
		String selectQuery = "SELECT * FROM tbl_author JOIN tbl_book_authors ON tbl_author.authorId=tbl_book_authors.authorId WHERE bookId=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setInt(1, bookId);
			rSet = pSt.executeQuery();
			if (rSet.next()) {
				return rSet.getString("authorName");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void updateAuthorName(int bookId, String authorName) {
		String selectQuery = "SELECT * FROM tbl_author WHERE authorName=?";
		try {
			pSt = connection.prepareStatement(selectQuery);
			pSt.setString(1, authorName);
			rSet = pSt.executeQuery();
			int authorId = 0;
			if (!rSet.next()) {
				addAuthor(authorName);
			}
			authorId = getAuthorId(authorName);
			labelAuthor(bookId, authorId);
			System.out.println("*****************");
			System.out.println("Update complete!!");
			System.out.println("******************");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

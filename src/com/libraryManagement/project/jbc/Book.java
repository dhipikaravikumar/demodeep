/**
 * 
 */
package com.libraryManagement.project.jbc;

/**
 * @author gcit
 *
 */
public class Book {
	
	private String bookName;
	private int bookId;
	private int pubId;
	
	public Book(int bookId, String bookName, int pubId) {
		this.bookName = bookName;
		this.bookId = bookId;
		this.pubId = pubId;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public int getPubId() {
		return pubId;
	}

	public void setPubId(int pubId) {
		this.pubId = pubId;
	}
	
	
}



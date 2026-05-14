package com.pageturner.service;

import com.pageturner.model.Book;
import java.util.List;

public interface BookService {
    List<Book> getAllBooks();
    Book getBookById(Long id);
    Book saveBook(Book book);
    void deleteBook(Long id);
    List<Book> getBooksByGenre(String genre);
    List<Book> searchBooks(String query);
    List<Book> getNewArrivals();
    long getTotalBooksCount();
}

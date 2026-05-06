package com.pageturner.service.impl;

import com.pageturner.model.Book;
import com.pageturner.repository.BookRepository;
import com.pageturner.service.BookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Book not found"));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<Book> getBooksByGenre(String genre) {
        return bookRepository.findByGenreIgnoreCase(genre);
    }

    @Override
    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

    @Override
    public List<Book> getNewArrivals() {
        return bookRepository.findAllByOrderByCreatedAtDesc().stream().limit(8).toList();
    }

    @Override
    public long getTotalBooksCount() {
        return bookRepository.count();
    }
}

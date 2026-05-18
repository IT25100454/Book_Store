package com.pageturner.service;

import com.pageturner.model.Author;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

public interface AuthorService {
    List<Author> getAllAuthors();
    Author getAuthorById(Long id);
    Author saveAuthor(Author author);
    void deleteAuthor(Long id);
    List<Author> getAuthorsByNationality(String nationality);
    List<Author> searchAuthors(String query);
}

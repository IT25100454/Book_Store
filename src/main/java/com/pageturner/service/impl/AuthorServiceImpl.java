package com.pageturner.service.impl;

import com.pageturner.model.Author;
import com.pageturner.repository.AuthorRepository;

import com.pageturner.service.AuthorService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    @Override
    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
    }

    @Override
    @Transactional
    public Author saveAuthor(Author author) {

        if (author.getPhotoUrl() != null &&
                author.getPhotoUrl().length() > 510) {

            throw new IllegalArgumentException(
                    "\n\nPhoto URL is greater than 510 characters"
            );
        }

        return authorRepository.save(author);
    }

    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        authorRepository.deleteById(id);
    }

    @Override
    public List<Author> getAuthorsByNationality(String nationality) {
        return authorRepository.findByNationalityIgnoreCase(nationality);
    }

    @Override
    public List<Author> searchAuthors(String query) {
        return authorRepository.findByNameContainingIgnoreCase(query);
    }
}

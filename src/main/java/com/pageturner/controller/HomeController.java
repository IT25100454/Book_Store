package com.pageturner.controller;

import com.pageturner.model.Book;
import com.pageturner.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final BookService bookService;

    public HomeController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("newArrivals", bookService.getNewArrivals());
        model.addAttribute("featuredBooks", bookService.getAllBooks().stream().limit(4).collect(Collectors.toList()));
        return "index";
    }

    @GetMapping("/books")
    public String listBooks(
            @RequestParam(name="search", required = false) String search,
            @RequestParam(name="genre", required = false) String genre,
            @RequestParam(name="sort", required = false) String sort,
            Model model) {
        
        List<Book> books;
        
        if (search != null && !search.isEmpty()) {
            books = bookService.searchBooks(search);
            model.addAttribute("searchQuery", search);
        } else if (genre != null && !genre.isEmpty()) {
            books = bookService.getBooksByGenre(genre);
            model.addAttribute("selectedGenre", genre);
        } else {
            books = bookService.getAllBooks();
        }

        // Sorting
        if ("price_asc".equals(sort)) {
            books.sort((b1, b2) -> b1.getPrice().compareTo(b2.getPrice()));
        } else if ("price_desc".equals(sort)) {
            books.sort((b1, b2) -> b2.getPrice().compareTo(b1.getPrice()));
        } else if ("newest".equals(sort)) {
            books.sort((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()));
        } else if ("title".equals(sort)) {
            books.sort((b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()));
        }
        
        List<String> allGenres = bookService.getAllBooks().stream()
                .map(Book::getGenre)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        model.addAttribute("books", books);
        model.addAttribute("allGenres", allGenres);
        model.addAttribute("sort", sort);
        return "books/list";
    }

    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with ID: " + id);
        }
        model.addAttribute("book", book);
        return "books/detail";
    }
}

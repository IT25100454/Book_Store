package com.pageturner.controller;

import com.pageturner.model.Author;
import com.pageturner.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/authors")
public class AuthorController {

    @Autowired
    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping("/")
    public ModelAndView listAuthors(Model model, RedirectAttributes redirectAttributes) {
        try {
            ModelAndView modelAndView = new ModelAndView("admin/authors/list");
            model.addAttribute("authors", authorService.getAllAuthors());
            return modelAndView;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not load authors: " + e.getMessage());
            return new ModelAndView("index");
        }
    }

    @GetMapping("/add")
    public ModelAndView showAddAuthorForm(Model model) {
        model.addAttribute("author", new Author());
        return new ModelAndView("admin/authors/form");
    }

    @PostMapping("/save")
    public String saveAuthor(@ModelAttribute Author author, RedirectAttributes redirectAttributes) {
        String photoUrl = author.getPhotoUrl();
        Long id = author.getId();
        if(photoUrl.length() > 510) {
            redirectAttributes.addFlashAttribute("error", "Photo URL is Too Long");
            return "redirect:/admin/authors";
        }
        authorService.saveAuthor(author);
        redirectAttributes.addFlashAttribute("success", "Author saved successfully.");
        return "redirect:/admin/authors";
    }

    @GetMapping("/edit/{id}")
    public String showEditAuthorForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("author", authorService.getAuthorById(id));
            return "admin/authors/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not load author: " + e.getMessage());
            return "redirect:/admin/authors";
        }
    }

    @GetMapping("/authors/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            authorService.deleteAuthor(id);
            redirectAttributes.addFlashAttribute("success", "Author deleted successfully.");
        } catch(Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete author.");
        }
        return "redirect:/admin/authors";
    }
}

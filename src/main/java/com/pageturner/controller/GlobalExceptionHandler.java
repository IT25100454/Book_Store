package com.pageturner.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, Model model, HttpServletResponse response) {
        model.addAttribute("status", ex.getStatusCode().value());
        model.addAttribute("error", ex.getReason());
        response.setStatus(ex.getStatusCode().value());
        if (ex.getStatusCode().value() == 404) {
            return "error/404";
        }
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAllExceptions(Exception ex, Model model) {
        ex.printStackTrace();
        model.addAttribute("status", 500);
        model.addAttribute("error", ex.getMessage());
        return "error/500";
    }
}

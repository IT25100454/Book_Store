package com.pageturner.controller;

import com.pageturner.model.Book;
import com.pageturner.model.CartItem;
import com.pageturner.service.BookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final BookService bookService;

    public CartController(BookService bookService) {
        this.bookService = bookService;
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = getCart(session);
        BigDecimal total = cart.stream()
                .map(item -> item.getBook().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("bookId") Long bookId, @RequestParam("quantity") int quantity, HttpSession session, RedirectAttributes redirectAttributes) {
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            redirectAttributes.addFlashAttribute("error", "Book not found.");
            return "redirect:/books";
        }
        List<CartItem> cart = getCart(session);
        
        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();
                
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity <= 0) {
                cart.remove(item);
            } else if (newQuantity <= book.getStockQuantity()) {
                item.setQuantity(newQuantity);
            } else {
                item.setQuantity(book.getStockQuantity());
            }
        } else {
            if (quantity > book.getStockQuantity()) {
                quantity = book.getStockQuantity();
            }
            if (quantity > 0) {
                cart.add(new CartItem(book, quantity));
            }
        }
        
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam("bookId") Long bookId, @RequestParam("quantity") int quantity, HttpSession session) {
        List<CartItem> cart = getCart(session);
        for (CartItem item : cart) {
            if (item.getBook().getId().equals(bookId)) {
                if (quantity <= 0) {
                    cart.remove(item);
                } else {
                    int maxStock = item.getBook().getStockQuantity();
                    item.setQuantity(Math.min(quantity, maxStock));
                }
                break;
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("bookId") Long bookId, HttpSession session) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getBook().getId().equals(bookId));
        return "redirect:/cart";
    }
}

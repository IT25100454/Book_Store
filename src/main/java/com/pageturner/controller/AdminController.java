package com.pageturner.controller;

import com.pageturner.model.Book;
import com.pageturner.model.User;
import com.pageturner.service.BookService;
import com.pageturner.service.OrderService;
import com.pageturner.service.ReportService;
import com.pageturner.service.UserService;
import com.pageturner.service.NotificationService;
import com.pageturner.model.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final List<String> ORDER_STATUSES =
            Arrays.asList("Pending", "Processing", "Shipped", "Delivered", "Cancelled");

    private final BookService bookService;
    private final OrderService orderService;
    private final UserService userService;
    private final ReportService reportService;
    private final NotificationService notificationService;

    public AdminController(BookService bookService, OrderService orderService, UserService userService, ReportService reportService, NotificationService notificationService) {
        this.bookService = bookService;
        this.orderService = orderService;
        this.userService = userService;
        this.reportService = reportService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String dashboard(Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("totalOrders", orderService.getTotalOrdersCount());
            model.addAttribute("totalRevenue", orderService.getTotalRevenue());
            model.addAttribute("totalBooks", bookService.getTotalBooksCount());
            model.addAttribute("totalUsers", userService.getTotalUsersCount());

            List<Book> lowStockBooks = bookService.getAllBooks().stream()
                    .filter(b -> b.getStockQuantity() < 5)
                    .toList();
            model.addAttribute("lowStockBooks", lowStockBooks);

            Map<String, BigDecimal> monthlySales = new LinkedHashMap<>();
            monthlySales.put("Jan", new BigDecimal("1200.50"));
            monthlySales.put("Feb", new BigDecimal("1850.00"));
            monthlySales.put("Mar", new BigDecimal("2400.75"));
            monthlySales.put("Apr", new BigDecimal("1950.25"));
            monthlySales.put("May", new BigDecimal("2800.00"));
            monthlySales.put("Jun", orderService.getTotalRevenue());
            model.addAttribute("chartLabels", monthlySales.keySet());
            model.addAttribute("chartData", monthlySales.values());

            return "admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not load admin dashboard: " + e.getMessage());
            return "redirect:/";
        }
    }

    // --- Book Management ---

    @GetMapping("/books")
    public String listBooks(Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("books", bookService.getAllBooks());
            return "admin/books/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not load books: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @GetMapping("/books/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/books/form";
    }

    @PostMapping("/books/save")
    public String saveBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("success", "Book saved successfully.");
        return "redirect:/admin/books";
    }

    @GetMapping("/books/edit/{id}")
    public String showEditBookForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("book", bookService.getBookById(id));
            return "admin/books/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not load book: " + e.getMessage());
            return "redirect:/admin/books";
        }
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "Book deleted successfully.");
        } catch(Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete book. It may be part of an order.");
        }
        return "redirect:/admin/books";
    }

    // --- Order Management ---

    @GetMapping("/orders")
    public String listOrders(Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("orders", orderService.getAllOrders());
            model.addAttribute("statuses", ORDER_STATUSES);
            return "admin/orders/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not load orders: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(
            @RequestParam("orderId") Long orderId,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, status);
            Order updatedOrder = orderService.getOrderById(orderId);
            if (updatedOrder != null) {
                notificationService.notifyOrderStatusChange(updatedOrder);
                updatedOrder.getItems().forEach(item -> {
                    if (item.getBook().getStockQuantity() < 5) {
                        userService.getAllUsers().stream()
                                .filter(u -> "ROLE_ADMIN".equals(u.getRole()))
                                .forEach(admin -> notificationService.notifyAdminLowStock(item.getBook(), admin));
                    }
                });
            }
            redirectAttributes.addFlashAttribute("success", "Order #" + orderId + " status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // --- User Management ---

    @GetMapping("/users")
    public String listUsers(Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("users", userService.getAllUsers());
            return "admin/users/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not load users: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/users/toggle-active")
    public String toggleUserActive(@RequestParam("userId") Long userId, RedirectAttributes redirectAttributes, java.security.Principal principal) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found.");
                return "redirect:/admin/users";
            }
            if (user.getUsername().equals(principal.getName())) {
                redirectAttributes.addFlashAttribute("error", "You cannot deactivate your own account.");
                return "redirect:/admin/users";
            }
            user.setActive(!user.isActive());
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "User status updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/promote")
    public String promoteToAdmin(@RequestParam("userId") Long userId, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found.");
                return "redirect:/admin/users";
            }
            user.setRole("ROLE_ADMIN");
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "User promoted to Admin.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to promote user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id,
                             org.springframework.security.core.Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            // Prevent admin from deleting their own account
            User currentAdmin = userService.findByUsername(authentication.getName());
            if (currentAdmin.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error",
                        "You cannot delete your own admin account from here.");
                return "redirect:/admin/users";
            }
            // Prevent deleting the last admin
            long adminCount = userService.countByRole("ROLE_ADMIN");
            User targetUser = userService.findById(id);
            if (targetUser.getRole().equals("ROLE_ADMIN") && adminCount <= 1) {
                redirectAttributes.addFlashAttribute("error",
                        "Cannot delete the last admin account.");
                return "redirect:/admin/users";
            }
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success",
                    "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // --- Reports ---
    @GetMapping("/export-inventory")
    public String exportInventory(RedirectAttributes redirectAttributes) {
        boolean success = reportService.exportInventoryReport();
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Inventory report exported to data/inventory_report.txt");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to export inventory report.");
        }
        return "redirect:/admin";
    }
}

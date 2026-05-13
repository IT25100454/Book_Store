package com.pageturner.controller;

import com.pageturner.model.Order;
import com.pageturner.model.User;
import com.pageturner.service.OrderService;
import com.pageturner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UserController {

    private final UserService userService;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, OrderService orderService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.orderService = orderService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String userProfile(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/login";
        }
        List<Order> orders = orderService.getUserOrders(user);
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        return "user/profile";
    }

    @PostMapping("/profile/delete-account")
    public String deleteAccount(@RequestParam("confirmPassword") String confirmPassword,
                                Authentication authentication,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName());

            // Verify password before deleting
            if (!passwordEncoder.matches(confirmPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error",
                        "Incorrect password. Account deletion cancelled.");
                return "redirect:/profile";
            }

            // Prevent admin from deleting own account this way
            if (user.getRole().equals("ROLE_ADMIN")) {
                redirectAttributes.addFlashAttribute("error",
                        "Admin accounts cannot be self-deleted. Contact system administrator.");
                return "redirect:/profile";
            }

            userService.deleteUser(user.getId());

            // Invalidate session and logout
            session.invalidate();
            SecurityContextHolder.clearContext();

            return "redirect:/login?accountDeleted=true";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to delete account: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Authentication authentication,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());

        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "Current password is incorrect");
            return "redirect:/profile";
        }

        // Validate new password length
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("passwordError", "New password must be at least 8 characters");
            return "redirect:/profile";
        }

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Passwords do not match");
            return "redirect:/profile";
        }

        // Validate new password is different from current
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "New password must be different from your current password");
            return "redirect:/profile";
        }

        // Change the password
        userService.changePassword(user, newPassword);

        // Invalidate session and logout for security
        session.invalidate();
        SecurityContextHolder.clearContext();

        return "redirect:/login?passwordChanged=true";
    }
}

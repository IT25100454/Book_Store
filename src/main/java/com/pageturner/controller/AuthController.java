package com.pageturner.controller;

import com.pageturner.model.User;
import com.pageturner.service.UserService;
import com.pageturner.model.PendingRegistration;
import com.pageturner.service.OtpService;
import com.pageturner.service.NotificationService;
import com.pageturner.service.EmailNotificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;

    public AuthController(UserService userService, OtpService otpService, PasswordEncoder passwordEncoder, NotificationService notificationService, EmailNotificationService emailNotificationService) {
        this.userService = userService;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.emailNotificationService = emailNotificationService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute User user, @RequestParam("confirmPassword") String confirmPassword, RedirectAttributes redirectAttributes) {
        if (!user.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }
        
        if (userService.findByUsername(user.getUsername()) != null) {
            redirectAttributes.addFlashAttribute("error", "Username already exists.");
            return "redirect:/register";
        }
        
        // As requested: Validate email is not already registered
        if (userService.getAllUsers().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()))) {
            redirectAttributes.addFlashAttribute("error", "An account with this email already exists");
            return "redirect:/register";
        }

        otpService.generateAndSendOtp(user.getEmail(), user.getUsername(), user.getName(), passwordEncoder.encode(user.getPassword()), user.getAddress());
        return "redirect:/verify-otp?email=" + user.getEmail();
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam("email") String email, Model model) {
        if (!otpService.hasPendingRegistration(email)) {
            return "redirect:/register";
        }
        model.addAttribute("email", email);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtpSubmit(@RequestParam("email") String email, @RequestParam("otp") String otp, RedirectAttributes redirectAttributes) {
        try {
            if (otpService.verifyOtp(email, otp)) {
                PendingRegistration pending = otpService.getPendingRegistration(email);
                
                User newUser = new User();
                newUser.setUsername(pending.getUsername());
                newUser.setEmail(pending.getEmail());
                newUser.setName(pending.getName());
                // Password is already encoded
                newUser.setPassword(pending.getEncodedPassword()); 
                newUser.setAddress(pending.getAddress());
                newUser.setRole("ROLE_USER");
                
                // Save user explicitly ignoring standard encoding because it's already encoded
                userService.updateUser(newUser);
                
                // Notifications
                notificationService.notifyWelcome(newUser);
                emailNotificationService.sendWelcomeEmail(newUser);

                otpService.clearPendingRegistration(email);
                
                redirectAttributes.addFlashAttribute("success", "Registration successful. Please login.");
                return "redirect:/login?registered=true";
            } else {
                redirectAttributes.addFlashAttribute("error", "Incorrect code, please try again");
                return "redirect:/verify-otp?email=" + email;
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Code expired. Please register again");
            return "redirect:/register";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        if (otpService.hasPendingRegistration(email)) {
            otpService.resendOtp(email);
            redirectAttributes.addFlashAttribute("success", "A new OTP has been sent to your email.");
            return "redirect:/verify-otp?email=" + email;
        }
        return "redirect:/register";
    }
}

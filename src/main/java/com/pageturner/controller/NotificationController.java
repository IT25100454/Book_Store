package com.pageturner.controller;

import com.pageturner.model.User;
import com.pageturner.service.NotificationService;
import com.pageturner.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/notifications")
    public String viewAll(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("notifications", notificationService.getUserNotifications(user));
        notificationService.markAllAsRead(user);
        return "notifications";
    }

    @PostMapping("/notifications/read/{id}")
    @ResponseBody
    public ResponseEntity<?> markRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications/count")
    @ResponseBody
    public ResponseEntity<Long> getCount(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.ok(0L);
        }
        User user = userService.findByUsername(auth.getName());
        return ResponseEntity.ok(notificationService.getUnreadCount(user));
    }
}

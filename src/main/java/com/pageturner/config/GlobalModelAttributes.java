package com.pageturner.config;

import com.pageturner.model.User;
import com.pageturner.service.NotificationService;
import com.pageturner.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final NotificationService notificationService;
    private final UserService userService;

    public GlobalModelAttributes(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @ModelAttribute
    public void addNotificationCount(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByUsername(auth.getName());
            if (user != null) {
                model.addAttribute("unreadNotificationCount", notificationService.getUnreadCount(user));
            }
        }
    }
}

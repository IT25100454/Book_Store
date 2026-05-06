package com.pageturner.config;

import com.pageturner.model.User;
import com.pageturner.service.NotificationService;
import com.pageturner.service.ThemeService;
import com.pageturner.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final NotificationService notificationService;
    private final UserService userService;
    private final ThemeService themeService;

    public GlobalModelAttributes(NotificationService notificationService, UserService userService, ThemeService themeService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.themeService = themeService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Authentication auth, Model model) {
        var activeThemeOption = themeService.getActiveTheme();
        model.addAttribute("activeTheme", activeThemeOption.id());
        model.addAttribute("activeThemeOption", activeThemeOption);
        model.addAttribute("activeThemeCss", themeService.toCssVariables(activeThemeOption.settings() != null ? activeThemeOption.settings() : themeService.defaultSettings()));
        model.addAttribute("availableThemes", themeService.getThemes());

        if (auth != null && auth.isAuthenticated()) {
            User user = userService.findByUsername(auth.getName());
            if (user != null) {
                model.addAttribute("unreadNotificationCount", notificationService.getUnreadCount(user));
            }
        }
    }
}

package com.pageturner.controller;

import com.pageturner.service.ThemeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/theme")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public String themeManager(Model model) {
        model.addAttribute("themes", themeService.getThemes());
        return "admin/theme";
    }

    @PostMapping
    public String updateTheme(@RequestParam("theme") String theme, HttpSession session, RedirectAttributes redirectAttributes) {
        String activeTheme = themeService.normalizeTheme(theme);
        session.setAttribute("activeTheme", activeTheme);
        redirectAttributes.addFlashAttribute("success", "Theme updated successfully.");
        return "redirect:/admin/theme";
    }
}

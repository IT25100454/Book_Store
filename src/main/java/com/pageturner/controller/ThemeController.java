package com.pageturner.controller;

import com.pageturner.model.ThemeOption;
import com.pageturner.model.ThemeSettings;
import com.pageturner.service.ThemeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/theme")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public String themeManager(@RequestParam(name = "edit", required = false) String editTheme, Model model) {
        ThemeOption selectedTheme = themeService.getTheme(editTheme).orElseGet(themeService::getActiveTheme);
        ThemeSettings selectedSettings = selectedTheme.settings() != null ? selectedTheme.settings() : themeService.defaultSettings();
        model.addAttribute("themes", themeService.getThemes());
        model.addAttribute("builtInThemes", themeService.getBuiltInThemes());
        model.addAttribute("customThemes", themeService.getCustomThemes());
        model.addAttribute("themeCssById", themeService.getThemes().stream()
                .collect(Collectors.toMap(ThemeOption::id, theme -> themeService.toCssVariables(theme.settings() != null ? theme.settings() : themeService.defaultSettings()))));
        model.addAttribute("selectedTheme", selectedTheme);
        model.addAttribute("selectedSettings", selectedSettings);
        model.addAttribute("selectedThemeCss", themeService.toCssVariables(selectedSettings));
        return "admin/theme";
    }

    @PostMapping
    public String updateTheme(@RequestParam("theme") String theme, RedirectAttributes redirectAttributes) {
        themeService.activateTheme(theme);
        redirectAttributes.addFlashAttribute("success", "Theme activated successfully.");
        return "redirect:/admin/theme";
    }

    @PostMapping("/activate")
    public String activateTheme(@RequestParam("theme") String theme, RedirectAttributes redirectAttributes) {
        themeService.activateTheme(theme);
        redirectAttributes.addFlashAttribute("success", "Theme activated successfully.");
        return "redirect:/admin/theme?edit=" + themeService.normalizeTheme(theme);
    }

    @PostMapping("/save")
    public String saveTheme(@RequestParam Map<String, String> form, RedirectAttributes redirectAttributes) {
        ThemeSettings settings = themeService.settingsFromForm(form);
        ThemeOption saved = themeService.saveCustomTheme(form.get("themeId"), form.get("name"), form.get("description"), settings);
        redirectAttributes.addFlashAttribute("success", "Theme saved. You can preview or activate it now.");
        return "redirect:/admin/theme?edit=" + saved.id();
    }

    @PostMapping("/duplicate")
    public String duplicateTheme(@RequestParam("theme") String theme, RedirectAttributes redirectAttributes) {
        ThemeOption copy = themeService.duplicateTheme(theme);
        redirectAttributes.addFlashAttribute("success", "Theme duplicated into an editable custom theme.");
        return "redirect:/admin/theme?edit=" + copy.id();
    }

    @PostMapping("/reset")
    public String resetTheme(RedirectAttributes redirectAttributes) {
        themeService.activateTheme(ThemeService.DEFAULT_THEME);
        redirectAttributes.addFlashAttribute("success", "Theme reset to Modern Light.");
        return "redirect:/admin/theme";
    }

    @PostMapping("/delete")
    public String deleteTheme(@RequestParam("theme") String theme, RedirectAttributes redirectAttributes) {
        themeService.deleteCustomTheme(theme);
        redirectAttributes.addFlashAttribute("success", "Custom theme removed.");
        return "redirect:/admin/theme";
    }
}

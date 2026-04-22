package com.pageturner.service;

import com.pageturner.model.ThemeOption;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThemeService {
    public static final String DEFAULT_THEME = "theme-modern-light";

    private static final List<ThemeOption> THEMES = List.of(
            new ThemeOption("theme-modern-light", "Modern Light", "Clean commercial interface with soft depth and balanced spacing."),
            new ThemeOption("theme-minimal-editorial", "Minimal Editorial", "High-whitespace layout with larger typography and restrained surfaces."),
            new ThemeOption("theme-dark-premium", "Dark Premium", "Dark cinematic interface with warm accents and deeper component contrast.")
            
    );

    public List<ThemeOption> getThemes() {
        return THEMES;
    }

    public String normalizeTheme(String themeId) {
        return THEMES.stream()
                .map(ThemeOption::id)
                .filter(id -> id.equals(themeId))
                .findFirst()
                .orElse(DEFAULT_THEME);
    }
}

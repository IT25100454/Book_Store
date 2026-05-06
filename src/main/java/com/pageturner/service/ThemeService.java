package com.pageturner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pageturner.model.ThemeOption;
import com.pageturner.model.ThemeSettings;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ThemeService {
    public static final String DEFAULT_THEME = "theme-modern-light";

    private final ObjectMapper objectMapper;
    private final Path dataDir = Path.of("data");
    private final Path customThemesFile = dataDir.resolve("custom-themes.json");
    private final Path activeThemeFile = dataDir.resolve("active-theme.txt");

    private static final List<ThemeOption> BUILT_IN_THEMES = List.of(
            new ThemeOption("theme-modern-light", "Modern Light", "Clean commercial interface with soft depth and balanced spacing.", false,
                    new ThemeSettings("#18202a", "#6f7f63", "#6f7f63", "#f7f8fb", "#ffffff", "#18202a", "#475467", "rgba(24,32,42,0.18)", "#166534", "#92400e", "#991b1b", "#1d4ed8", "#6f7f63", 18, "pill", 24, "comfortable", "balanced", "soft", "frosted", "outlined", 28, "solid", 70, "balanced")),
            new ThemeOption("theme-minimal-editorial", "Minimal Editorial", "High-whitespace layout with larger typography and restrained surfaces.", false,
                    new ThemeSettings("#15110d", "#9f6b3e", "#9f6b3e", "#f6f1e8", "#fbf8f1", "#15110d", "#5f584d", "rgba(21,17,13,0.22)", "#166534", "#854d0e", "#991b1b", "#1e40af", "#9f6b3e", 4, "square", 8, "spacious", "large", "flat", "minimal", "underline", 8, "hairline", 45, "open")),
            new ThemeOption("theme-dark-premium", "Dark Premium", "Dark cinematic interface with warm accents and deeper component contrast.", false,
                    new ThemeSettings("#f4eadb", "#d4a373", "#d4a373", "#11100e", "#191714", "#f4eadb", "#d7c8b5", "rgba(244,234,219,0.18)", "#86efac", "#fbbf24", "#fca5a5", "#93c5fd", "#d4a373", 14, "glass", 42, "compact", "dramatic", "glass", "glass", "filled", 42, "glow", 80, "dense"))
    );

    public ThemeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ThemeOption> getThemes() {
        List<ThemeOption> themes = new ArrayList<>(BUILT_IN_THEMES);
        themes.addAll(loadCustomThemes());
        return themes;
    }

    public List<ThemeOption> getBuiltInThemes() {
        return BUILT_IN_THEMES;
    }

    public List<ThemeOption> getCustomThemes() {
        return loadCustomThemes();
    }

    public String getActiveThemeId() {
        try {
            if (Files.exists(activeThemeFile)) {
                return normalizeTheme(Files.readString(activeThemeFile, StandardCharsets.UTF_8).trim());
            }
        } catch (IOException ignored) {
            // Fall back to default when the preference file is unavailable.
        }
        return DEFAULT_THEME;
    }

    public ThemeOption getActiveTheme() {
        return getTheme(getActiveThemeId()).orElseGet(() -> getTheme(DEFAULT_THEME).orElse(BUILT_IN_THEMES.get(0)));
    }

    public ThemeSettings defaultSettings() {
        return BUILT_IN_THEMES.get(0).settings();
    }

    public Optional<ThemeOption> getTheme(String themeId) {
        String normalized = normalizeTheme(themeId);
        return getThemes().stream().filter(theme -> theme.id().equals(normalized)).findFirst();
    }

    public String normalizeTheme(String themeId) {
        if (themeId == null || themeId.isBlank()) {
            return DEFAULT_THEME;
        }
        return getThemes().stream()
                .map(ThemeOption::id)
                .filter(id -> id.equals(themeId))
                .findFirst()
                .orElse(DEFAULT_THEME);
    }

    public void activateTheme(String themeId) {
        ensureDataDir();
        try {
            Files.writeString(activeThemeFile, normalizeTheme(themeId), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save active theme.", e);
        }
    }

    public ThemeOption saveCustomTheme(String originalId, String name, String description, ThemeSettings settings) {
        List<ThemeOption> customThemes = loadCustomThemes();
        String id = originalId != null && originalId.startsWith("custom-") ? originalId : createId(name);
        ThemeOption saved = new ThemeOption(id, safeName(name), safeDescription(description), true, settings);

        customThemes.removeIf(theme -> theme.id().equals(id));
        customThemes.add(saved);
        saveCustomThemes(customThemes);
        return saved;
    }

    public ThemeOption duplicateTheme(String themeId) {
        ThemeOption source = getTheme(themeId).orElseGet(this::getActiveTheme);
        String name = source.name() + " Copy";
        ThemeOption copy = new ThemeOption(createId(name), name, source.description(), true, source.settings());
        List<ThemeOption> customThemes = loadCustomThemes();
        customThemes.add(copy);
        saveCustomThemes(customThemes);
        return copy;
    }

    public void deleteCustomTheme(String themeId) {
        List<ThemeOption> customThemes = loadCustomThemes();
        boolean removed = customThemes.removeIf(theme -> theme.custom() && theme.id().equals(themeId));
        if (removed) {
            saveCustomThemes(customThemes);
            if (getActiveThemeId().equals(themeId)) {
                activateTheme(DEFAULT_THEME);
            }
        }
    }

    public ThemeSettings settingsFromForm(Map<String, String> form) {
        return new ThemeSettings(
                color(form, "primaryColor", "#18202a"),
                color(form, "secondaryColor", "#6f7f63"),
                color(form, "accentColor", "#6f7f63"),
                color(form, "backgroundColor", "#f7f8fb"),
                color(form, "surfaceColor", "#ffffff"),
                color(form, "textColor", "#18202a"),
                color(form, "mutedTextColor", "#475467"),
                form.getOrDefault("borderColor", "rgba(24,32,42,0.18)"),
                color(form, "successColor", "#166534"),
                color(form, "warningColor", "#92400e"),
                color(form, "dangerColor", "#991b1b"),
                color(form, "infoColor", "#1d4ed8"),
                color(form, "focusColor", "#6f7f63"),
                number(form, "borderRadius", 18, 0, 36),
                choice(form, "buttonStyle", "pill"),
                number(form, "shadowIntensity", 24, 0, 60),
                choice(form, "spacingDensity", "comfortable"),
                choice(form, "typographyScale", "balanced"),
                choice(form, "cardStyle", "soft"),
                choice(form, "navbarStyle", "frosted"),
                choice(form, "formStyle", "outlined"),
                number(form, "glassLevel", 28, 0, 80),
                choice(form, "borderStyle", "solid"),
                number(form, "animationIntensity", 70, 0, 100),
                choice(form, "layoutDensity", "balanced")
        );
    }

    public String toCssVariables(ThemeSettings settings) {
        if (settings == null) {
            return "";
        }
        int radius = settings.borderRadius();
        int shadow = settings.shadowIntensity();
        int glass = settings.glassLevel();
        int animation = settings.animationIntensity();
        double spacing = switch (settings.spacingDensity()) {
            case "compact" -> 0.86;
            case "spacious" -> 1.18;
            default -> 1.0;
        };
        double type = switch (settings.typographyScale()) {
            case "large" -> 1.08;
            case "dramatic" -> 1.16;
            default -> 1.0;
        };
        int buttonRadius = switch (settings.buttonStyle()) {
            case "square" -> 4;
            case "soft" -> Math.max(10, radius);
            case "glass" -> Math.max(14, radius);
            default -> 999;
        };
        double cardAlpha = switch (settings.cardStyle()) {
            case "flat" -> 1.0;
            case "glass" -> Math.max(0.22, glass / 100.0);
            default -> 0.92;
        };
        int borderWidth = "hairline".equals(settings.borderStyle()) ? 1 : ("glow".equals(settings.borderStyle()) ? 2 : 1);
        double formAlpha = switch (settings.formStyle()) {
            case "filled" -> 1.0;
            case "underline" -> 0.0;
            default -> 0.84;
        };
        return String.join("",
                "--ink:", settings.textColor(), ";",
                "--muted:", settings.mutedTextColor(), ";",
                "--paper:", settings.backgroundColor(), ";",
                "--paper-soft:", settings.surfaceColor(), ";",
                "--line:", settings.borderColor(), ";",
                "--primary-color:", settings.primaryColor(), ";",
                "--secondary-color:", settings.secondaryColor(), ";",
                "--accent-color:", settings.accentColor(), ";",
                "--bg-color:", settings.backgroundColor(), ";",
                "--text-color:", settings.textColor(), ";",
                "--color-success:", settings.successColor(), ";",
                "--color-warning:", settings.warningColor(), ";",
                "--color-danger:", settings.dangerColor(), ";",
                "--color-info:", settings.infoColor(), ";",
                "--focus-ring:", settings.focusColor(), ";",
                "--radius-control:", radius + "px;",
                "--radius-card:", Math.max(0, radius + 4) + "px;",
                "--button-radius:", buttonRadius + "px;",
                "--shadow-strength:", String.valueOf(shadow), ";",
                "--glass-alpha:", String.valueOf(Math.max(0.08, Math.min(0.92, glass / 100.0))), ";",
                "--card-alpha:", String.valueOf(cardAlpha), ";",
                "--border-width:", borderWidth + "px;",
                "--form-alpha:", String.valueOf(formAlpha), ";",
                "--spacing-scale:", String.valueOf(spacing), ";",
                "--type-scale:", String.valueOf(type), ";",
                "--motion-scale:", String.valueOf(Math.max(0.1, animation / 100.0)), ";"
        );
    }

    private List<ThemeOption> loadCustomThemes() {
        try {
            if (!Files.exists(customThemesFile)) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(Files.readString(customThemesFile, StandardCharsets.UTF_8), new TypeReference<List<ThemeOption>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void saveCustomThemes(List<ThemeOption> themes) {
        ensureDataDir();
        try {
            Files.writeString(customThemesFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(themes), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save custom themes.", e);
        }
    }

    private void ensureDataDir() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create data directory.", e);
        }
    }

    private String createId(String name) {
        String base = Normalizer.normalize(safeName(name), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "theme";
        }
        String id = "custom-" + base;
        int suffix = 2;
        List<String> existing = getThemes().stream().map(ThemeOption::id).toList();
        while (existing.contains(id)) {
            id = "custom-" + base + "-" + suffix++;
        }
        return id;
    }

    private String safeName(String name) {
        return name == null || name.isBlank() ? "Custom Theme" : name.trim();
    }

    private String safeDescription(String description) {
        return description == null || description.isBlank() ? "Custom bookstore theme created in the admin editor." : description.trim();
    }

    private String color(Map<String, String> form, String key, String fallback) {
        String value = form.get(key);
        return value != null && value.matches("^#[0-9a-fA-F]{6}$") ? value : fallback;
    }

    private int number(Map<String, String> form, String key, int fallback, int min, int max) {
        try {
            int value = Integer.parseInt(form.getOrDefault(key, String.valueOf(fallback)));
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String choice(Map<String, String> form, String key, String fallback) {
        String value = form.get(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}

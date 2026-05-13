document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("themeEditorForm");
    const preview = document.querySelector(".theme-live-preview");
    if (!form || !preview) {
        return;
    }

    const get = name => {
        const field = form.elements[name];
        return field ? field.value : "";
    };

    function number(name, fallback) {
        const value = Number.parseInt(get(name), 10);
        return Number.isFinite(value) ? value : fallback;
    }

    function computeCss() {
        const radius = number("borderRadius", 18);
        const shadow = number("shadowIntensity", 24);
        const glass = number("glassLevel", 28);
        const animation = number("animationIntensity", 70);
        const spacing = get("spacingDensity") === "compact" ? 0.86 : get("spacingDensity") === "spacious" ? 1.18 : 1;
        const type = get("typographyScale") === "dramatic" ? 1.16 : get("typographyScale") === "large" ? 1.08 : 1;
        const buttonRadius = get("buttonStyle") === "square" ? 4 : get("buttonStyle") === "soft" ? Math.max(10, radius) : get("buttonStyle") === "glass" ? Math.max(14, radius) : 999;
        const cardAlpha = get("cardStyle") === "flat" ? 1 : get("cardStyle") === "glass" ? Math.max(0.22, glass / 100) : 0.92;
        const borderWidth = get("borderStyle") === "glow" ? 2 : 1;
        const formAlpha = get("formStyle") === "filled" ? 1 : get("formStyle") === "underline" ? 0 : 0.84;

        return [
            ["--ink", get("textColor")],
            ["--muted", get("mutedTextColor")],
            ["--paper", get("backgroundColor")],
            ["--paper-soft", get("surfaceColor")],
            ["--line", get("borderColor")],
            ["--primary-color", get("primaryColor")],
            ["--secondary-color", get("secondaryColor")],
            ["--accent-color", get("accentColor")],
            ["--bg-color", get("backgroundColor")],
            ["--text-color", get("textColor")],
            ["--color-success", get("successColor")],
            ["--color-warning", get("warningColor")],
            ["--color-danger", get("dangerColor")],
            ["--color-info", get("infoColor")],
            ["--focus-ring", get("focusColor")],
            ["--radius-control", radius + "px"],
            ["--radius-card", Math.max(0, radius + 4) + "px"],
            ["--button-radius", buttonRadius + "px"],
            ["--shadow-strength", shadow],
            ["--glass-alpha", Math.max(0.08, Math.min(0.92, glass / 100))],
            ["--card-alpha", cardAlpha],
            ["--border-width", borderWidth + "px"],
            ["--form-alpha", formAlpha],
            ["--spacing-scale", spacing],
            ["--type-scale", type],
            ["--motion-scale", Math.max(0.1, animation / 100)]
        ];
    }

    function updatePreview() {
        computeCss().forEach(([key, value]) => preview.style.setProperty(key, value));
    }

    form.querySelectorAll("input, select, textarea").forEach(field => {
        field.addEventListener("input", updatePreview);
        field.addEventListener("change", updatePreview);
    });

    updatePreview();
});

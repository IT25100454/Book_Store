package com.pageturner.model;

public record ThemeSettings(
        String primaryColor,
        String secondaryColor,
        String accentColor,
        String backgroundColor,
        String surfaceColor,
        String textColor,
        String mutedTextColor,
        String borderColor,
        String successColor,
        String warningColor,
        String dangerColor,
        String infoColor,
        String focusColor,
        int borderRadius,
        String buttonStyle,
        int shadowIntensity,
        String spacingDensity,
        String typographyScale,
        String cardStyle,
        String navbarStyle,
        String formStyle,
        int glassLevel,
        String borderStyle,
        int animationIntensity,
        String layoutDensity
) {
}

package com.pageturner.model;

public record ThemeOption(String id, String name, String description, boolean custom, ThemeSettings settings) {
    public ThemeOption(String id, String name, String description) {
        this(id, name, description, false, null);
    }
}

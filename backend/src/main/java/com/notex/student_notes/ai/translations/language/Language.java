package com.notex.student_notes.ai.translations.language;

public enum Language {
    PL("Polish"),
    EN("English"),
    DE("German"),
    FR("French"),
    ES("Spanish"),
    IT("Italian"),
    RU("Russian");
    private final String displayName;
    Language(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}

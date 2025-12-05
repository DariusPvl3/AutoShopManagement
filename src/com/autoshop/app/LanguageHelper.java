package com.autoshop.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageHelper {
    private static ResourceBundle bundle;
    private static final List<Runnable> listeners = new ArrayList<>();
    private static Locale currentLocale = Locale.ENGLISH;

    static {
        // Default to English on startup
        setLocale(Locale.ENGLISH);
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale; // Update it
        bundle = ResourceBundle.getBundle("messages", locale);
        notifyListeners();
    }

    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "Key not found: " + key;
        }
    }

    // Register a view to be updated when language changes
    public static void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private static void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public static Locale getCurrentLocale() { return currentLocale; }
}
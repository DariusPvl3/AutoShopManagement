package com.autoshop.app.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PreferencesHelper {
    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_LANGUAGE = "app.language";

    // Save "en" or "ro" to file
    public static void saveLanguage(String langCode) {
        Properties props = new Properties();
        props.setProperty(KEY_LANGUAGE, langCode);

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "AutoShop Scheduler Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load "en" or "ro" (Default to "en" if file is missing)
    public static String loadLanguage() {
        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
            return props.getProperty(KEY_LANGUAGE, "en"); // Default to English
        } catch (IOException e) {
            return "en"; // File doesn't exist yet
        }
    }
}
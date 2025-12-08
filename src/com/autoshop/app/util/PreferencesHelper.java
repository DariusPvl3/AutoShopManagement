package com.autoshop.app.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

public class PreferencesHelper {
    private static final Preferences prefs = Preferences.userNodeForPackage(PreferencesHelper.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_LANGUAGE = "app.language";

    // --- LANGUAGE PREFERENCES ---

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

    // --- NOTIFICATION PREFERENCES ---

    public static boolean isNotificationEnabled() {
        return prefs.getBoolean("notif_enabled", true); // Default to ON
    }

    public static void setNotificationEnabled(boolean enabled) {
        prefs.putBoolean("notif_enabled", enabled);
    }

    public static int getNotificationLeadTime() {
        return prefs.getInt("notif_lead_time", 15); // Default 15 minutes
    }

    public static void setNotificationLeadTime(int minutes) {
        prefs.putInt("notif_lead_time", minutes);
    }
}
package com.autoshop.app;

import com.autoshop.app.component.NotificationService;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.PreferencesHelper;
import com.autoshop.app.view.MainFrame;

import javax.swing.*;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Setup Look & Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // 2. Initialize Database
                DatabaseHelper.createNewTable();
                DatabaseHelper.autoUpdateStatuses();

                // 3. Start Background Services
                NotificationService.start();

                // 4. Load Language Preferences
                String lang = PreferencesHelper.loadLanguage();
                LanguageHelper.setLocale("ro".equals(lang) ? new Locale("ro") : Locale.ENGLISH);

                // 5. Launch UI
                new MainFrame().setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Critical Error during startup: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
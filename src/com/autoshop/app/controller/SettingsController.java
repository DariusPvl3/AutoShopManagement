package com.autoshop.app.controller;

import com.autoshop.app.component.ThemedDialog;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.PreferencesHelper;
import com.autoshop.app.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class SettingsController {
    private final Component parentView;

    public SettingsController(Component parentView) {
        this.parentView = parentView;
    }

    public void backupData() {
        String defaultName = "appointments_backup_" + Utils.getCurrentTimeStamp() + ".db";
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        chooser.setDialogTitle(LanguageHelper.getString("title.backup"));

        if (chooser.showSaveDialog(parentView) == JFileChooser.APPROVE_OPTION) {
            try {
                Utils.copyFile(new File("appointments.db"), chooser.getSelectedFile());
                ThemedDialog.showMessage(parentView,
                        LanguageHelper.getString("title.success"),
                        LanguageHelper.getString("backup.success"));
            } catch (IOException ex) {
                ThemedDialog.showMessage(parentView,
                        LanguageHelper.getString("title.error"),
                        LanguageHelper.getString("backup.error"));
            }
        }
    }

    public void restoreData() {
        boolean confirmed = ThemedDialog.showConfirm(parentView,
                LanguageHelper.getString("title.confirm"),
                LanguageHelper.getString("msg.warn.restore"));

        if (!confirmed) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(LanguageHelper.getString("title.restore"));

        if (chooser.showOpenDialog(parentView) == JFileChooser.APPROVE_OPTION) {
            try {
                Utils.copyFile(chooser.getSelectedFile(), new File("appointments.db"));
                ThemedDialog.showMessage(parentView,
                        LanguageHelper.getString("title.success"),
                        LanguageHelper.getString("restore.success"));
                System.exit(0); // Force restart to reload DB connection
            } catch (IOException ex) {
                ThemedDialog.showMessage(parentView,
                        LanguageHelper.getString("title.error"),
                        LanguageHelper.getString("restore.error"));
            }
        }
    }

    public void setLanguage(String code, Locale locale) {
        LanguageHelper.setLocale(locale);
        PreferencesHelper.saveLanguage(code);
    }

    public void setNotificationsEnabled(boolean enabled) {
        PreferencesHelper.setNotificationEnabled(enabled);
    }

    public void setNotificationTime(int minutes) {
        PreferencesHelper.setNotificationLeadTime(minutes);
    }

    // Getters for initial state
    public String getCurrentLanguage() { return PreferencesHelper.loadLanguage(); }
    public boolean isNotifEnabled() { return PreferencesHelper.isNotificationEnabled(); }
    public int getNotifTime() { return PreferencesHelper.getNotificationLeadTime(); }
}
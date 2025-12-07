package com.autoshop.app.view;

import com.autoshop.app.component.ButtonStyler;
import com.autoshop.app.component.RoundedButton;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.PreferencesHelper;
import com.autoshop.app.util.Theme;
import com.autoshop.app.util.Utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class SettingsView extends JPanel {

    private static final Color GRAY_BTN = new Color(149, 165, 166);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font BORDER_FONT = new Font("SansSerif", Font.BOLD, 18);

    private JLabel titleLabel;
    private JButton backupBtn, restoreBtn, btnEn, btnRo;
    private JPanel dataPanel, langPanel;

    public SettingsView() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.OFF_WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        setupListeners();

        LanguageHelper.addListener(this::updateText);
        updateText();

        highlightLanguage(PreferencesHelper.loadLanguage());
    }

    // --- UI CONSTRUCTION ---

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BLACK);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        titleLabel = new JLabel();
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Theme.TEXT_LIGHT);
        header.add(titleLabel, BorderLayout.WEST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 20));
        content.setBackground(Theme.OFF_WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(createDataPanel());
        content.add(createLanguagePanel());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.OFF_WHITE);
        wrapper.add(content, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel createDataPanel() {
        dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        dataPanel.setBackground(Theme.WHITE);

        backupBtn = new RoundedButton("Backup");
        ButtonStyler.apply(backupBtn, GRAY_BTN);

        restoreBtn = new RoundedButton("Restore");
        ButtonStyler.apply(restoreBtn, Theme.RED);

        dataPanel.add(backupBtn);
        dataPanel.add(restoreBtn);
        return dataPanel;
    }

    private JPanel createLanguagePanel() {
        langPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        langPanel.setBackground(Theme.WHITE);

        btnEn = new RoundedButton("English");
        btnRo = new RoundedButton("Română");

        btnEn.setIcon(Utils.loadIcon("/resources/uk.png", 24, 16));
        btnRo.setIcon(Utils.loadIcon("/resources/ro.png", 24, 22));
        btnEn.setIconTextGap(10);
        btnRo.setIconTextGap(10);

        ButtonStyler.apply(btnEn, GRAY_BTN);
        ButtonStyler.apply(btnRo, GRAY_BTN);

        langPanel.add(btnEn);
        langPanel.add(btnRo);
        return langPanel;
    }

    // --- LOGIC ---

    private void performBackup() {
        String defaultName = "appointments_backup_" + Utils.getCurrentTimeStamp() + ".db";
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        chooser.setDialogTitle("Save Backup File");

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Utils.copyFile(new File("appointments.db"), chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Backup created successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Backup Failed: " + ex.getMessage());
            }
        }
    }

    private void performRestore() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Restoring will DELETE all current data.\nAre you sure?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Backup File");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Utils.copyFile(chooser.getSelectedFile(), new File("appointments.db"));
                JOptionPane.showMessageDialog(this, "Restore Successful!\nPlease restart the app.");
                System.exit(0);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Restore Failed: " + ex.getMessage());
            }
        }
    }

    private void switchLanguage(String code, Locale locale) {
        LanguageHelper.setLocale(locale);
        PreferencesHelper.saveLanguage(code);
        highlightLanguage(code);
    }

    private void highlightLanguage(String lang) {
        if ("ro".equals(lang)) {
            ButtonStyler.apply(btnRo, Theme.RED);
            ButtonStyler.apply(btnEn, GRAY_BTN);
        } else {
            ButtonStyler.apply(btnEn, Theme.RED);
            ButtonStyler.apply(btnRo, GRAY_BTN);
        }
    }

    // --- LISTENERS ---

    private void setupListeners() {
        backupBtn.addActionListener(_ -> performBackup());
        restoreBtn.addActionListener(_ -> performRestore());
        btnEn.addActionListener(_ -> switchLanguage("en", Locale.ENGLISH));
        btnRo.addActionListener(_ -> switchLanguage("ro", new Locale("ro")));
    }

    private void updateText() {
        titleLabel.setText(LanguageHelper.getString("btn.settings"));
        backupBtn.setText(LanguageHelper.getString("btn.backup"));
        restoreBtn.setText(LanguageHelper.getString("btn.restore"));

        setPanelBorder(dataPanel, LanguageHelper.getString("lbl.data_mng"));
        setPanelBorder(langPanel, LanguageHelper.getString("lbl.language"));
    }

    private void setPanelBorder(JPanel panel, String title) {
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Theme.BLACK, 1),
                        title, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                        BORDER_FONT, Theme.BLACK),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }
}
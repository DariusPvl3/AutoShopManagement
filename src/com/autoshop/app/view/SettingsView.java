package com.autoshop.app.view;

import com.autoshop.app.component.ButtonStyler;
import com.autoshop.app.component.RedCheckBox;
import com.autoshop.app.component.RoundedButton;
import com.autoshop.app.component.ThemedDialog; // Make sure this is imported
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

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font BORDER_FONT = new Font("SansSerif", Font.BOLD, 18);

    private JLabel titleLabel;
    private JButton backupBtn, restoreBtn, btnEn, btnRo;
    private JPanel dataPanel, langPanel;
    private JPanel notifPanel;
    private JCheckBox enableNotifBox;
    private JSpinner timeSpinner;
    private JLabel minsLabel;

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
        JPanel content = new JPanel(new GridLayout(3, 1, 0, 20)); // Changed rows to 3
        content.setBackground(Theme.OFF_WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(createDataPanel());
        content.add(createLanguagePanel());
        content.add(createNotificationPanel());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.OFF_WHITE);
        wrapper.add(content, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel createDataPanel() {
        dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        dataPanel.setBackground(Theme.OFF_WHITE);

        backupBtn = new RoundedButton("Backup");
        ButtonStyler.apply(backupBtn, Theme.GRAY);

        restoreBtn = new RoundedButton("Restore");
        ButtonStyler.apply(restoreBtn, Theme.RED);

        dataPanel.add(backupBtn);
        dataPanel.add(restoreBtn);
        return dataPanel;
    }

    private JPanel createLanguagePanel() {
        langPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        langPanel.setBackground(Theme.OFF_WHITE);

        btnEn = new RoundedButton("English");
        btnRo = new RoundedButton("Română");

        // Ensure these paths are correct in your project structure
        btnEn.setIcon(Utils.loadIcon("/resources/uk.png", 24, 16));
        btnRo.setIcon(Utils.loadIcon("/resources/ro.png", 24, 22));
        btnEn.setIconTextGap(10);
        btnRo.setIconTextGap(10);

        ButtonStyler.apply(btnEn, Theme.GRAY);
        ButtonStyler.apply(btnRo, Theme.GRAY);

        langPanel.add(btnEn);
        langPanel.add(btnRo);
        return langPanel;
    }

    private JPanel createNotificationPanel() {
        notifPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        notifPanel.setBackground(Theme.OFF_WHITE);

        // Using your Custom RedCheckBox
        enableNotifBox = new RedCheckBox("Enable Alerts");
        enableNotifBox.setSelected(PreferencesHelper.isNotificationEnabled());

        int savedTime = PreferencesHelper.getNotificationLeadTime();
        timeSpinner = new JSpinner(new SpinnerNumberModel(savedTime, 1, 120, 1));
        timeSpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
        timeSpinner.setPreferredSize(new Dimension(60, 30));
        Utils.addMouseScrollToSpinner(timeSpinner);

        minsLabel = new JLabel("minutes before");
        minsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        notifPanel.add(enableNotifBox);
        notifPanel.add(Box.createHorizontalStrut(20));
        notifPanel.add(timeSpinner);
        notifPanel.add(minsLabel);

        enableNotifBox.addActionListener(e -> {
            boolean isSelected = enableNotifBox.isSelected();
            PreferencesHelper.setNotificationEnabled(isSelected);
            timeSpinner.setEnabled(isSelected);
        });

        timeSpinner.addChangeListener(e -> {
            int val = (Integer) timeSpinner.getValue();
            PreferencesHelper.setNotificationLeadTime(val);
        });

        // Initial State
        timeSpinner.setEnabled(enableNotifBox.isSelected());

        return notifPanel;
    }

    // --- LOGIC ---

    private void performBackup() {
        String defaultName = "appointments_backup_" + Utils.getCurrentTimeStamp() + ".db";
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        chooser.setDialogTitle(LanguageHelper.getString("title.backup")); // Localized title

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Utils.copyFile(new File("appointments.db"), chooser.getSelectedFile());
                ThemedDialog.showMessage(this, LanguageHelper.getString("title.success"), LanguageHelper.getString("backup.success"));
            } catch (IOException ex) {
                ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString("backup.error"));
            }
        }
    }

    private void performRestore() {
        // --- CHANGED: Now using ThemedDialog instead of JOptionPane ---
        // Note: You should add "msg.warn.restore" to your language files
        // English: "WARNING: Restoring will DELETE all current data.\nAre you sure?"
        // Romanian: "ATENȚIE: Restaurarea va ȘTERGE toate datele curente.\nSunteți sigur?"

        boolean confirmed = ThemedDialog.showConfirm(this,
                LanguageHelper.getString("title.confirm"),
                LanguageHelper.getString("msg.warn.restore"));

        if (!confirmed) return; // Stop if user clicked "No"

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(LanguageHelper.getString("title.restore"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Utils.copyFile(chooser.getSelectedFile(), new File("appointments.db"));

                // Success message
                ThemedDialog.showMessage(this, LanguageHelper.getString("title.success"), LanguageHelper.getString("restore.success"));

                // Close app to force reload of DB connection
                System.exit(0);
            } catch (IOException ex) {
                ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString("restore.error"));
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
            ButtonStyler.apply(btnEn, Theme.GRAY);
        } else {
            ButtonStyler.apply(btnEn, Theme.RED);
            ButtonStyler.apply(btnRo, Theme.GRAY);
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
        setPanelBorder(notifPanel, LanguageHelper.getString("lbl.notifications"));

        enableNotifBox.setText(LanguageHelper.getString("lbl.enable_alerts"));
        minsLabel.setText(LanguageHelper.getString("lbl.minutes_before"));
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
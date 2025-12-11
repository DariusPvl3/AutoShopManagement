package com.autoshop.app.view;

import com.autoshop.app.component.ButtonStyler;
import com.autoshop.app.component.RedCheckBox;
import com.autoshop.app.component.RoundedButton;
import com.autoshop.app.controller.SettingsController;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;
import com.autoshop.app.util.Utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Locale;

public class SettingsView extends JPanel {

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font BORDER_FONT = new Font("SansSerif", Font.BOLD, 18);

    private final SettingsController controller;

    // Components
    private JLabel titleLabel, minsLabel;
    private JButton backupBtn, restoreBtn, btnEn, btnRo;
    private JPanel dataPanel, langPanel, notifPanel;
    private JCheckBox enableNotifBox;
    private JSpinner timeSpinner;

    public SettingsView() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.OFF_WHITE);

        // 1. Init Controller
        this.controller = new SettingsController(this);

        // 2. Build Layout
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        // 3. Setup Logic
        setupListeners();

        // 4. Initial State
        highlightLanguage(controller.getCurrentLanguage());
        LanguageHelper.addListener(this::updateText);
        updateText();
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
        JPanel content = new JPanel(new GridLayout(3, 1, 0, 20));
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

        enableNotifBox = new RedCheckBox("Enable Alerts");
        enableNotifBox.setSelected(controller.isNotifEnabled());

        int savedTime = controller.getNotifTime();
        timeSpinner = new JSpinner(new SpinnerNumberModel(savedTime, 1, 120, 1));
        timeSpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
        timeSpinner.setPreferredSize(new Dimension(60, 30));
        Utils.addMouseScrollToSpinner(timeSpinner);
        timeSpinner.setEnabled(enableNotifBox.isSelected());

        minsLabel = new JLabel("minutes before");
        minsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        notifPanel.add(enableNotifBox);
        notifPanel.add(Box.createHorizontalStrut(20));
        notifPanel.add(timeSpinner);
        notifPanel.add(minsLabel);

        return notifPanel;
    }

    // --- LISTENERS ---

    private void setupListeners() {
        backupBtn.addActionListener(_ -> controller.backupData());
        restoreBtn.addActionListener(_ -> controller.restoreData());

        btnEn.addActionListener(_ -> {
            controller.setLanguage("en", Locale.ENGLISH);
            highlightLanguage("en");
        });

        btnRo.addActionListener(_ -> {
            controller.setLanguage("ro", new Locale("ro"));
            highlightLanguage("ro");
        });

        enableNotifBox.addActionListener(_ -> {
            boolean isSelected = enableNotifBox.isSelected();
            controller.setNotificationsEnabled(isSelected);
            timeSpinner.setEnabled(isSelected);
        });

        timeSpinner.addChangeListener(_ -> controller.setNotificationTime((Integer) timeSpinner.getValue()));
    }

    // --- VIEW HELPERS ---

    private void highlightLanguage(String lang) {
        if ("ro".equals(lang)) {
            ButtonStyler.apply(btnRo, Theme.RED);
            ButtonStyler.apply(btnEn, Theme.GRAY);
        } else {
            ButtonStyler.apply(btnEn, Theme.RED);
            ButtonStyler.apply(btnRo, Theme.GRAY);
        }
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
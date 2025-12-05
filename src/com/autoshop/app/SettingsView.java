package com.autoshop.app;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class SettingsView extends JPanel {

    private JLabel titleLabel;
    private JButton backupBtn, restoreBtn;
    private JPanel dataPanel;

    public SettingsView() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        titleLabel = new JLabel();
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(80, 80, 80));
        add(titleLabel, BorderLayout.NORTH);

        // Content Panel (Grid for different setting sections)
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 20));

        // --- DATA MANAGEMENT SECTION ---
        dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));

        backupBtn = new RoundedButton("Backup Database");
        ButtonStyler.apply(backupBtn, new Color(46, 204, 113)); // Green

        restoreBtn = new RoundedButton("Restore Database");
        ButtonStyler.apply(restoreBtn, new Color(231, 76, 60)); // Red

        dataPanel.add(backupBtn);
        dataPanel.add(restoreBtn);

        content.add(dataPanel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);

        // --- LISTENERS ---

        backupBtn.addActionListener(_ -> performBackup());
        restoreBtn.addActionListener(_ -> performRestore());

        // --- LANGUAGE SECTION ---
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        langPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Language / Limba"));

        JButton btnEn = new RoundedButton("English");
        JButton btnRo = new RoundedButton("Română");

        ButtonStyler.apply(btnEn, new Color(52, 152, 219));
        ButtonStyler.apply(btnRo, new Color(231, 76, 60)); // Red-ish for RO

        langPanel.add(btnEn);
        langPanel.add(btnRo);

        content.add(langPanel); // Add to grid

        // --- LISTENERS ---
        btnEn.addActionListener(e -> LanguageHelper.setLocale(Locale.ENGLISH));
        btnRo.addActionListener(e -> LanguageHelper.setLocale(new Locale("ro")));

        LanguageHelper.addListener(this::updateText);
        updateText();
    }

    private void performBackup() {
        String defaultName = "appointments_backup_" + Utils.getCurrentTimeStamp() + ".db";

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        chooser.setDialogTitle("Save Backup File");

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            File source = new File("appointments.db"); // The live DB

            try {
                Utils.copyFile(source, dest);
                JOptionPane.showMessageDialog(this, "Backup created successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Backup Failed: " + ex.getMessage());
            }
        }
    }

    private void performRestore() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Restoring will DELETE all current data and replace it with the backup.\nAre you sure?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Backup File to Restore");

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File source = chooser.getSelectedFile();
                File dest = new File("appointments.db");

                try {
                    Utils.copyFile(source, dest);

                    JOptionPane.showMessageDialog(this, "Restore Successful!\nPlease restart the application to see changes.");
                    System.exit(0); // Force restart to reload DB connection
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Restore Failed: " + ex.getMessage());
                }
            }
        }
    }

    private void updateText(){
        titleLabel.setText(LanguageHelper.getString("lbl.settings"));
        backupBtn.setText(LanguageHelper.getString("btn.backup"));
        restoreBtn.setText(LanguageHelper.getString("btn.restore"));
        dataPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                LanguageHelper.getString("lbl.data_mng")));
    }
}
package com.autoshop.app;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SettingsView extends JPanel {

    public SettingsView() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("Settings & Maintenance");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(80, 80, 80));
        add(title, BorderLayout.NORTH);

        // Content Panel (Grid for different setting sections)
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 20));

        // --- DATA MANAGEMENT SECTION ---
        JPanel dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        dataPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Data Management"));

        JButton backupBtn = new RoundedButton("Backup Database");
        ButtonStyler.apply(backupBtn, new Color(46, 204, 113)); // Green

        JButton restoreBtn = new RoundedButton("Restore Database");
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
}
package com.autoshop.app;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private JButton homeButton, appointmentButton, searchButton, settingsButton;

    public MainFrame() {
        setTitle("AutoShop Scheduler V1.3.1");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        DashboardView dashboardView = new DashboardView();
        AppointmentView appointmentView = new AppointmentView();
        SearchView searchView = new SearchView();
        SettingsView settingsView = new SettingsView();

        mainPanel.add(dashboardView, "HOME");
        mainPanel.add(appointmentView, "APPOINTMENTS");
        mainPanel.add(searchView, "SEARCH");
        mainPanel.add(settingsView, "SETTINGS");

        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        homeButton = new JButton("Home");
        appointmentButton = new JButton("Appointments");
        searchButton = new JButton("Search");
        settingsButton = new JButton("Settings");

        styleMenuButton(homeButton);
        styleMenuButton(appointmentButton);
        styleMenuButton(searchButton);
        styleMenuButton(settingsButton);

        homeButton.addActionListener(_ -> cardLayout.show(mainPanel, "HOME"));
        appointmentButton.addActionListener(_ -> cardLayout.show(mainPanel, "APPOINTMENTS"));
        searchButton.addActionListener(_ -> cardLayout.show(mainPanel, "SEARCH"));
        settingsButton.addActionListener(_ -> cardLayout.show(mainPanel, "SETTINGS"));

        // --- CROSS-VIEW WIRING ---

        // 1. Dashboard -> Appointments (Double Click Table)
        dashboardView.setOnJumpRequest(id -> {
            cardLayout.show(mainPanel, "APPOINTMENTS");
            appointmentView.selectAppointmentById(id);
        });

        // 2. Search -> Appointments (Double Click Table)
        searchView.setOnJumpRequest(id -> {
            cardLayout.show(mainPanel, "APPOINTMENTS");
            appointmentView.selectAppointmentById(id);
        });

        // 3. Dashboard -> Search (Right Click Calendar)
        dashboardView.setOnSearchDateRequest(date -> {
            cardLayout.show(mainPanel, "SEARCH");
            searchView.searchByDate(date);
        });

        // 4. Dashboard -> Create Appointment
        dashboardView.setOnCreateRequest(date -> {
            cardLayout.show(mainPanel, "APPOINTMENTS");
            appointmentView.prepareNewAppointment(date);
        });

        menuPanel.add(homeButton);
        menuPanel.add(appointmentButton);
        menuPanel.add(searchButton);
        menuPanel.add(settingsButton);

        add(menuPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "HOME");

        LanguageHelper.addListener(this::updateText);
        updateText();
    }

    private void styleMenuButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                DatabaseHelper.createNewTable();
                DatabaseHelper.autoUpdateStatuses();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }

    private void updateText() {
        homeButton.setText(LanguageHelper.getString("btn.home"));
        appointmentButton.setText(LanguageHelper.getString("btn.appointments"));
        searchButton.setText(LanguageHelper.getString("btn.search"));
        settingsButton.setText(LanguageHelper.getString("btn.settings"));
        setTitle(LanguageHelper.getString("app.title"));
    }
}
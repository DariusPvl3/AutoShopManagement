package com.autoshop.app;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("AutoShop Scheduler V1.2");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        DashboardView dashboardView = new DashboardView();
        AppointmentView appointmentView = new AppointmentView();
        SearchView searchView = new SearchView();

        mainPanel.add(dashboardView, "HOME");
        mainPanel.add(appointmentView, "APPOINTMENTS");
        mainPanel.add(searchView, "SEARCH");

        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton homeButton = new JButton("Home");
        JButton appointmentButton = new JButton("Appointments");
        JButton searchButton = new JButton("Search");
        JButton settingsButton = new JButton("Settings");

        styleMenuButton(homeButton);
        styleMenuButton(appointmentButton);
        styleMenuButton(searchButton);
        styleMenuButton(settingsButton);

        homeButton.addActionListener(e -> cardLayout.show(mainPanel, "HOME"));
        appointmentButton.addActionListener(e -> cardLayout.show(mainPanel, "APPOINTMENTS"));
        searchButton.addActionListener(e -> cardLayout.show(mainPanel, "SEARCH"));
        settingsButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings coming in V1.3!"));

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
}
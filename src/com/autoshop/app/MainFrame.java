package com.autoshop.app;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("AutoShop Scheduler V1.1");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        DashboardView dashboardView = new DashboardView();
        AppointmentView appointmentView = new AppointmentView();

        mainPanel.add(dashboardView, "HOME");
        mainPanel.add(appointmentView, "APPOINTMENTS");

        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton homeButton = new JButton("Home");
        JButton appointmentButton = new JButton("Appointments");
        JButton searchButton = new JButton("Search");

        styleMenuButton(homeButton);
        styleMenuButton(appointmentButton);
        styleMenuButton(searchButton);

        homeButton.addActionListener(e -> cardLayout.show(mainPanel, "HOME"));
        appointmentButton.addActionListener(e -> cardLayout.show(mainPanel, "APPOINTMENTS"));

        menuPanel.add(homeButton);
        menuPanel.add(appointmentButton);
        menuPanel.add(searchButton);

        add(menuPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "HOME");
    }

    private void styleMenuButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        //btn.setBackground(new Color(230, 230, 230)); // Light Grey
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                com.formdev.flatlaf.FlatDarkLaf.setup();
                DatabaseHelper.createNewTable();
                DatabaseHelper.autoUpdateStatuses();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }
}
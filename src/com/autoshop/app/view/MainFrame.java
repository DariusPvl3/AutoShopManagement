package com.autoshop.app.view;

import com.autoshop.app.component.ButtonStyler;
import com.autoshop.app.component.CustomTitleBar;
import com.autoshop.app.component.NotificationService;
import com.autoshop.app.component.RoundedButton;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.PreferencesHelper;
import com.autoshop.app.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    // Buttons
    private JButton homeButton, appointmentButton, searchButton, settingsButton;

    // Views
    private DashboardView dashboardView;
    private AppointmentView appointmentView;
    private SearchView searchView;
    private SettingsView settingsView;

    public MainFrame() {
        initFrame();

        // 1. Create Views
        initViews();

        // 2. Create Layout
        JPanel rootContainer = new JPanel(new BorderLayout());
        rootContainer.setBorder(BorderFactory.createLineBorder(Theme.BLACK, 1));

        rootContainer.add(createTopSection(), BorderLayout.NORTH);
        rootContainer.add(createCenterSection(), BorderLayout.CENTER);

        setContentPane(rootContainer);

        // 3. Setup Logic
        setupNavigation();
        setupWiring();

        LanguageHelper.addListener(this::updateText);
        updateText();
    }

    // --- INITIALIZATION ---

    private void initFrame() {
        setUndecorated(true);
        setTitle("AutoShop Scheduler V1.3.3");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setAppIcon();
    }

    private void initViews() {
        dashboardView = new DashboardView();
        appointmentView = new AppointmentView();
        searchView = new SearchView();
        settingsView = new SettingsView();
    }

    // --- UI CONSTRUCTION ---

    private JPanel createTopSection() {
        JPanel topSection = new JPanel(new BorderLayout());

        // A. Title Bar
        topSection.add(new CustomTitleBar(this, "AutoShop Scheduler V1.3.4"), BorderLayout.NORTH);

        // B. Menu
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        menuPanel.setBackground(Theme.BLACK);

        homeButton = createMenuButton("Home");
        appointmentButton = createMenuButton("Appointments");
        searchButton = createMenuButton("Search");
        settingsButton = createMenuButton("Settings");

        menuPanel.add(homeButton);
        menuPanel.add(appointmentButton);
        menuPanel.add(searchButton);
        menuPanel.add(settingsButton);

        topSection.add(menuPanel, BorderLayout.CENTER);
        return topSection;
    }

    private JPanel createCenterSection() {
        mainPanel.add(dashboardView, "HOME");
        mainPanel.add(appointmentView, "APPOINTMENTS");
        mainPanel.add(searchView, "SEARCH");
        mainPanel.add(settingsView, "SETTINGS");
        return mainPanel;
    }

    private JButton createMenuButton(String text) {
        JButton btn = new RoundedButton(text);
        ButtonStyler.apply(btn, Theme.RED);
        btn.setMargin(new Insets(8, 20, 8, 20));
        return btn;
    }

    // --- LOGIC & WIRING ---

    private void setupNavigation() {
        homeButton.addActionListener(_ -> cardLayout.show(mainPanel, "HOME"));
        appointmentButton.addActionListener(_ -> cardLayout.show(mainPanel, "APPOINTMENTS"));
        searchButton.addActionListener(_ -> cardLayout.show(mainPanel, "SEARCH"));
        settingsButton.addActionListener(_ -> cardLayout.show(mainPanel, "SETTINGS"));

        // Default View
        cardLayout.show(mainPanel, "HOME");
    }

    private void setupWiring() {
        // Dashboard -> Appointment (Edit)
        dashboardView.setOnJumpRequest(id -> {
            cardLayout.show(mainPanel, "APPOINTMENTS");
            appointmentView.selectAppointmentById(id);
        });

        // Dashboard -> Appointment (Create)
        dashboardView.setOnCreateRequest(date -> {
            cardLayout.show(mainPanel, "APPOINTMENTS");
            appointmentView.prepareNewAppointment(date);
        });

        // Dashboard -> Search
        dashboardView.setOnSearchDateRequest(date -> {
            cardLayout.show(mainPanel, "SEARCH");
            searchView.searchByDate(date);
        });

        // Search -> Appointment
        searchView.setOnJumpRequest(id -> {
            cardLayout.show(mainPanel, "APPOINTMENTS");
            appointmentView.selectAppointmentById(id);
        });
    }

    // --- HELPERS ---

    private void updateText() {
        homeButton.setText(LanguageHelper.getString("btn.home"));
        appointmentButton.setText(LanguageHelper.getString("btn.appointments"));
        searchButton.setText(LanguageHelper.getString("btn.search"));
        settingsButton.setText(LanguageHelper.getString("btn.settings"));
    }

    private void setAppIcon() {
        java.net.URL iconURL = getClass().getResource("/resources/logo.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        } else {
            System.err.println("Logo not found! Check path or Rebuild Project.");
        }
    }

    // --- MAIN ENTRY POINT ---

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                DatabaseHelper.createNewTable();
                DatabaseHelper.autoUpdateStatuses();

                NotificationService.start();

                String lang = PreferencesHelper.loadLanguage();
                LanguageHelper.setLocale("ro".equals(lang) ? new Locale("ro") : Locale.ENGLISH);
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }
}
package com.autoshop.app.view;

import com.autoshop.app.component.*;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    // Buttons
    private JButton homeButton, appointmentButton, searchButton, settingsButton, helpButton;

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
        WindowResizeHelper.install(this);
        setTitle("AutoShop Scheduler V1.5.0");

        // Calculate Screen Bounds (Respecting Taskbar)
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Insets scnMax = toolkit.getScreenInsets(getGraphicsConfiguration());

        int width = screenSize.width - scnMax.left - scnMax.right;
        int height = screenSize.height - scnMax.top - scnMax.bottom;

        setBounds(scnMax.left, scnMax.top, width, height);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                shutdownApplication();
            }
        });

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
        topSection.add(new CustomTitleBar(this, "AutoShop Scheduler V1.5.0"), BorderLayout.NORTH);

        // B. Menu
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        menuPanel.setBackground(Theme.BLACK);

        homeButton = createMenuButton("Home");
        appointmentButton = createMenuButton("Appointments");
        searchButton = createMenuButton("Search");
        settingsButton = createMenuButton("Settings");

        helpButton = new RoundedButton("?");
        ButtonStyler.apply(helpButton, Theme.GRAY);
        helpButton.setMargin(new Insets(8, 15, 8, 15)); // Smaller padding
        helpButton.setToolTipText("Shortcuts & Help");

        menuPanel.add(homeButton);
        menuPanel.add(appointmentButton);
        menuPanel.add(searchButton);
        menuPanel.add(settingsButton);
        menuPanel.add(Box.createHorizontalStrut(50));
        menuPanel.add(helpButton);

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
        homeButton.addActionListener(e -> cardLayout.show(mainPanel, "HOME"));
        appointmentButton.addActionListener(e -> cardLayout.show(mainPanel, "APPOINTMENTS"));
        searchButton.addActionListener(e -> cardLayout.show(mainPanel, "SEARCH"));
        settingsButton.addActionListener(e -> cardLayout.show(mainPanel, "SETTINGS"));

        helpButton.addActionListener(e -> showHelpDialog());

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
            System.err.println("Warning: Logo not found at /resources/logo.png");
        }
    }

    public void shutdownApplication() {
        NotificationService.stop();
        dispose();
        System.exit(0);
    }

    // --- HELP DIALOG LOGIC ---
    private void showHelpDialog() {
        // 1. Build Localized HTML Content
        String title = LanguageHelper.getString("help.title");
        String shortcutsTitle = LanguageHelper.getString("help.shortcuts");
        String tipsTitle = LanguageHelper.getString("help.tips");

        String sb = "<html><body style='width: 350px; font-family: SansSerif; font-size: 14px;'>" + // width controls wrapping

                // Shortcuts Section
                "<h3 style='color: #E74C3C;'>" + shortcutsTitle + "</h3>" +
                "<table border='0' cellpadding='3'>" +
                row("Ctrl + S", "help.add") +
                row("Ctrl + E", "help.update") +
                row("Ctrl + R", "help.reset") +
                row("Delete", "help.delete") +
                row("Enter", "help.search") +
                "</table>" +

                // Tips Section
                "<br><h3 style='color: #E74C3C;'>" + tipsTitle + "</h3>" +
                "<ul>" +
                "<li>" + LanguageHelper.getString("help.tip.status") + "</li>" +
                "<li>" + LanguageHelper.getString("help.tip.jump") + "</li>" +
                "<li>" + LanguageHelper.getString("help.tip.scroll") + "</li>" +
                "</ul>" +
                "</body></html>";

        // 2. Create Custom Dialog (Better than ThemedDialog for HTML)
        JDialog dialog = new JDialog(this, title, true); // true = modal
        dialog.setLayout(new BorderLayout());

        // Content Label (Renders HTML)
        JLabel contentLabel = new JLabel(sb);
        contentLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        contentLabel.setVerticalAlignment(SwingConstants.TOP);

        // Close Button
        JButton okButton = new RoundedButton("OK");
        ButtonStyler.apply(okButton, Theme.BLACK);
        okButton.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(okButton);

        // Assemble
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(contentLabel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.pack(); // <--- Auto-sizes the window to fit the content
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    // Helper to build a table row
    private String row(String keys, String langKey) {
        return "<tr>" +
                "<td style='font-weight: bold; color: #333;'>" + keys + "</td>" +
                "<td style='padding-left: 10px;'>" + LanguageHelper.getString(langKey) + "</td>" +
                "</tr>";
    }
}
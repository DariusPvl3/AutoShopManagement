package com.autoshop.app.view;

import com.autoshop.app.controller.AppointmentController;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;
import com.autoshop.app.view.manager.AppointmentFormManager;
import com.autoshop.app.view.manager.AppointmentTableManager;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class AppointmentView extends JPanel {

    // The Three Pillars of our Architecture
    private final AppointmentFormManager formManager;
    private final AppointmentTableManager tableManager;
    private final AppointmentController controller;

    private Timer refreshTimer;

    public AppointmentView() {
        setLayout(new BorderLayout());

        // 1. Initialize Components & Logic
        this.formManager = new AppointmentFormManager();
        this.tableManager = new AppointmentTableManager(this);
        this.controller = new AppointmentController(formManager, tableManager, this);

        // 2. Build UI Layout
        buildLayout();

        // 3. Wire Up Events (The Controller connects them)
        setupListeners();

        // 4. Initial Load
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                tableManager.refreshData();
            }
        });

        // 5. Language Support
        LanguageHelper.addListener(() -> {
            formManager.updateText();
            tableManager.updateHeaders();
        });

        // Initial text update
        formManager.updateText();
        tableManager.updateHeaders();

        // 6. Auto-Refresh Timer (30s)
        setupAutoRefresh();
    }

    private void buildLayout() {
        // --- Form Section ---
        JPanel formsWrapper = new JPanel();
        formsWrapper.setLayout(new BoxLayout(formsWrapper, BoxLayout.Y_AXIS));
        formsWrapper.setBackground(Theme.OFF_WHITE);

        JPanel topRow = new JPanel(new GridLayout(1, 2, 20, 0));
        topRow.setBackground(Theme.OFF_WHITE);
        topRow.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        topRow.add(formManager.createClientPanel());
        topRow.add(formManager.createCarPanel());

        formsWrapper.add(topRow);
        formsWrapper.add(formManager.createAppointmentPanel());

        // --- Split Pane ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JScrollPane formScroll = new JScrollPane(formsWrapper);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        splitPane.setTopComponent(formScroll);
        splitPane.setBottomComponent(tableManager.getScrollPane());
        splitPane.setDividerLocation(600);

        add(splitPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        // Buttons -> Controller
        formManager.getAddButton().addActionListener(e -> controller.addAppointment());
        formManager.getUpdateButton().addActionListener(e -> controller.updateAppointment());
        formManager.getDeleteButton().addActionListener(e -> controller.deleteAppointment());
        formManager.getClearButton().addActionListener(e -> controller.clear());
        formManager.getSelectPhotoButton().addActionListener(e -> controller.selectPhoto());
        formManager.getViewPhotoButton().addActionListener(e -> controller.viewPhoto());

        // Table Selection -> Controller
        tableManager.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controller.onSelectionChanged();
            }
        });
    }

    private void setupAutoRefresh() {
        refreshTimer = new Timer(30000, e -> {
            JTable t = tableManager.getTable();
            if (t != null && !t.isEditing()) {
                tableManager.refreshData();
            }
        });
        refreshTimer.start();
    }

    // --- Public Methods needed by MainFrame ---

    public void selectAppointmentById(int id) {
        tableManager.selectById(id, foundAppointment -> {
            formManager.loadAppointment(foundAppointment);
        });
    }

    public void prepareNewAppointment(Date date) {
        // Delegate to the Manager
        formManager.prepareForNew(date);
    }
}
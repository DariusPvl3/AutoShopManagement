package com.autoshop.app.view;

import com.autoshop.app.controller.AppointmentController;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;
import com.autoshop.app.view.manager.AppointmentFormManager;
import com.autoshop.app.view.manager.AppointmentTableManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;

public class AppointmentView extends JPanel {

    // The Three Pillars of our Architecture
    private final AppointmentFormManager formManager;
    private final AppointmentTableManager tableManager;
    private final AppointmentController controller;

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
        setupShortcuts();

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

    private void setupShortcuts() {
        // Get the InputMap for the entire panel (WHEN_IN_FOCUSED_WINDOW is the secret sauce)
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        // 1. CTRL + S -> ADD
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (formManager.getAddButton().isEnabled()) {
                    controller.addAppointment();
                }
            }
        });

        // 2. CTRL + E -> UPDATE
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "update");
        actionMap.put("update", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (formManager.getUpdateButton().isEnabled()) {
                    controller.updateAppointment();
                }
            }
        });

        // 3. CTRL + R -> RESET / CLEAR
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "reset");
        actionMap.put("reset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.clear();
            }
        });

        // 4. DELETE -> DELETE APPOINTMENT
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (formManager.getDeleteButton().isEnabled()) {
                    controller.deleteAppointment();
                }
            }
        });
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
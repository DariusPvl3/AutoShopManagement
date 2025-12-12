package com.autoshop.app.view;

import com.autoshop.app.component.*;
import com.autoshop.app.controller.DashboardController;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;
import com.autoshop.app.view.manager.CalendarHelper;
import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.function.Consumer;

public class DashboardView extends JPanel {
    private static final Font STAT_FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 20);
    private static final Color HEADER_COLOR = new Color(80, 80, 80);

    private final DashboardController controller;

    private DefaultTableModel tableModel;
    private JTable agendaTable;
    private JCalendar calendar;
    private JLabel tableHeader, todayLabel, activeLabel;

    public DashboardView() {
        setLayout(new BorderLayout(15, 15));
        setBorder(null);
        setBackground(Theme.OFF_WHITE);

        // 1. Init UI Components (Models/Labels)
        initDataComponents();

        // 2. Init Controller (Passes logic to update labels/table)
        this.controller = new DashboardController(this, tableModel,
                text -> todayLabel.setText(text),
                text -> activeLabel.setText(text)
        );

        // 3. Build Layout
        add(createStatsPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);

        // 4. Setup
        setupListeners();

        // Reload data every time the view is shown (switched to)
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                controller.loadData();
            }
        });

        // 5. Initial Load & Language
        LanguageHelper.addListener(this::updateText);
        updateText(); // Loads data internally via controller

        // 6. Auto-Refresh Logic
        setupAutoRefresh();
    }

    private void initDataComponents() {
        String[] columns = {"Client Name", "Phone", "License Plate", "Brand", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        todayLabel = new JLabel();
        todayLabel.setFont(STAT_FONT);
        todayLabel.setForeground(Theme.TEXT_LIGHT);

        activeLabel = new JLabel();
        activeLabel.setFont(STAT_FONT);
        activeLabel.setForeground(Theme.TEXT_LIGHT);
    }

    // --- UI CONSTRUCTION ---

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel labelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelsPanel.setOpaque(false);
        labelsPanel.add(todayLabel);
        labelsPanel.add(Box.createHorizontalStrut(30));
        labelsPanel.add(activeLabel);

        DigitalClock clock = new DigitalClock();
        clock.setForeground(Theme.TEXT_LIGHT);

        panel.add(labelsPanel, BorderLayout.WEST);
        panel.add(clock, BorderLayout.EAST);
        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.OFF_WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Table
        gbc.gridx = 0; gbc.weightx = 0.70;
        panel.add(createTableSection(), gbc);

        // Calendar
        gbc.gridx = 1; gbc.weightx = 0.30;
        gbc.insets = new Insets(0, 20, 0, 0);
        panel.add(createCalendarSection(), gbc);

        return panel;
    }

    private JPanel createTableSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.OFF_WHITE);

        tableHeader = new JLabel();
        tableHeader.setFont(HEADER_FONT);
        tableHeader.setForeground(HEADER_COLOR);
        panel.add(tableHeader, BorderLayout.NORTH);

        agendaTable = SwingTableStyler.create(tableModel, 5);
        agendaTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(agendaTable);
        scrollPane.getViewport().setBackground(Theme.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCalendarSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        JLabel calHeader = new JLabel("Calendar");
        calHeader.setFont(HEADER_FONT);
        calHeader.setForeground(HEADER_COLOR);
        panel.add(calHeader, BorderLayout.NORTH);

        calendar = new JCalendar();
        CalendarCustomizer.styleCalendar(calendar);

        CalendarHelper.attachInteraction(calendar,
                controller::countAppointmentsOnDate, // Count provider
                controller::triggerSearch,           // View action
                controller::triggerCreate            // Create action
        );

        panel.add(calendar, BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC ---

    private void setupListeners() {
        // Right Click Menu
        StatusMenuHelper.attach(agendaTable, controller.getAppointmentList(), controller::loadData, this);

        // Double Click Jump
        agendaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    controller.handleJumpRequest(agendaTable.getSelectedRow());
                }
            }
        });
    }

    private void setupAutoRefresh() {
        new Timer(30000, e -> {
            if (agendaTable != null && !agendaTable.isEditing()) {
                int selectedRow = agendaTable.getSelectedRow();
                // Simple refresh logic (Controller handles data, View handles table state)
                controller.loadData();
                if (selectedRow != -1 && selectedRow < agendaTable.getRowCount()) {
                    agendaTable.setRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        }).start();
    }

    private void updateText() {
        tableHeader.setText(LanguageHelper.getString("hdr.dashboard"));
        controller.loadData(); // Reloads data and updates labels

        if (tableModel != null) {
            String[] cols = {
                    LanguageHelper.getString("col.client"), LanguageHelper.getString("col.phone"),
                    LanguageHelper.getString("col.plate"), LanguageHelper.getString("col.brand"),
                    LanguageHelper.getString("col.date"), LanguageHelper.getString("col.status")
            };
            tableModel.setColumnIdentifiers(cols);
            agendaTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

            calendar.setLocale(LanguageHelper.getCurrentLocale());
            StatusMenuHelper.attach(agendaTable, controller.getAppointmentList(), controller::loadData, this);
        }
    }

    // --- PROXY SETTERS ---
    public void setOnCreateRequest(Consumer<Date> c) { controller.setOnCreateRequest(c); }
    public void setOnJumpRequest(Consumer<Integer> c) { controller.setOnJumpRequest(c); }
    public void setOnSearchDateRequest(Consumer<Date> c) { controller.setOnSearchDateRequest(c); }
}
package com.autoshop.app;

import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class DashboardView extends JPanel {
    private ArrayList<Appointment> appointmentList = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JLabel appointmentsTodayLabel = new JLabel();
    JLabel activeLabel = new JLabel();
    private JTable agendaTable = new JTable();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public DashboardView() {
        this.setLayout(new BorderLayout());

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        appointmentsTodayLabel = new JLabel("Appointments Today: 0");
        appointmentsTodayLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        activeLabel = new JLabel("Appointments Active: 0");
        activeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        statsPanel.add(appointmentsTodayLabel);
        statsPanel.add(activeLabel);
        this.add(statsPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2));
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        agendaTable.setFont(new Font("SansSerif", Font.BOLD, 14));

        String[] columns = {"Client Name", "Phone", "License Plate", "Brand", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        agendaTable = new JTable(tableModel);
        agendaTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        StatusMenuHelper.attach(agendaTable, appointmentList, this::loadDataFromDB, this);

        scrollPane.setViewportView(agendaTable);
        contentPanel.add(scrollPane);

        JCalendar calendar = new JCalendar();
        contentPanel.add(calendar);

        this.add(contentPanel, BorderLayout.CENTER);

        loadDataFromDB();

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadDataFromDB();
            }
        });
    }

    // Inside DashboardView.java

    private void loadDataFromDB() {
        try {
            // 1. Run Automation
            DatabaseHelper.autoUpdateStatuses();

            // 2. Fetch Data
            appointmentList.clear(); // Empty the bucket
            appointmentList.addAll(DatabaseHelper.getDashboardAppointments(new java.util.Date()));

            // 3. Get Active Stats
            java.util.List<Appointment> activeList = DatabaseHelper.getActiveAppointments();

            // 4. Update Labels
            long todayCount = appointmentList.stream()
                    .filter(a -> isToday(a.getDate()))
                    .count();

            appointmentsTodayLabel.setText("Appointments Today: " + todayCount);
            activeLabel.setText("Appointments Active: " + activeList.size());

            // 5. Sort (Active first, then by date)
            appointmentList.sort((a, b) -> {
                boolean aActive = a.getStatus() == AppointmentStatus.IN_PROGRESS;
                boolean bActive = b.getStatus() == AppointmentStatus.IN_PROGRESS;
                if (aActive && !bActive) return -1;
                if (!aActive && bActive) return 1;
                return a.getDate().compareTo(b.getDate());
            });

            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        Collections.sort(appointmentList, Comparator.comparing(Appointment::getDate));
        tableModel.setRowCount(0);
        for (Appointment appt : appointmentList) {
            tableModel.addRow(new Object[]{
                    appt.getClientName(),
                    appt.getClientPhone(),
                    appt.getCarLicensePlate(),
                    appt.getCarBrand(),
                    dateFormat.format(appt.getDate()),
                    appt.getStatus()
            });
        }
        if (agendaTable.getRowCount() > 0) {
            agendaTable.setRowSelectionInterval(0, 0);
        }
    }

    // Helper to check if a date is today (for the label count)
    private boolean isToday(java.util.Date date) {
        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar target = java.util.Calendar.getInstance();
        target.setTime(date);
        return today.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR);
    }
}

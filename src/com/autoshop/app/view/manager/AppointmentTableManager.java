package com.autoshop.app.view.manager;

import com.autoshop.app.component.*;
import com.autoshop.app.model.Appointment;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AppointmentTableManager {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<Appointment> appointmentList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final Component parent; // For dialogs

    // =================================================================================================================
    //  SECTION 1: UI AND TABLE INITIALIZATION
    //  These methods are called by AppointmentView to assemble the screen.
    // =================================================================================================================

    public AppointmentTableManager(Component parent) {
        this.parent = parent;

        // 1. Initialize Model
        String[] columns = {"Client Name", "Phone", "License Plate", "Brand", "Model", "Year", "Date", "Description", "Repairs", "Parts used", "Observations", "Status"};
        this.tableModel = new DefaultTableModel(columns, 0);

        // 2. Initialize Table
        this.table = SwingTableStyler.create(tableModel, 11);
        this.table.getColumnModel().getColumn(11).setCellRenderer(new StatusCellRenderer());

        // 3. Attach Status Menu Logic (Right Click)
        StatusMenuHelper.attach(table, appointmentList, this::refreshData, parent);
    }

    public JScrollPane getScrollPane() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        scroll.setMinimumSize(new Dimension(150, 150));
        return scroll;
    }

    public JTable getTable() {
        return table;
    }

    // =================================================================================================================
    //  SECTION 2: DATA HANDLING
    //  Displaying up-to-date appointments in the table
    // =================================================================================================================

    public void refreshData() {
        try {
            DatabaseHelper.autoUpdateStatuses();
            List<Appointment> newData = DatabaseHelper.getAllAppointments();

            // Update internal list
            appointmentList.clear();
            appointmentList.addAll(newData);

            // Sort
            appointmentList.sort(Comparator.comparing(Appointment::getDate));

            // Update UI
            updateTableModel();

        } catch (SQLException e) {
            e.printStackTrace(); // Log error
        }
    }

    private void updateTableModel() {
        tableModel.setRowCount(0);
        for (Appointment a : appointmentList) {
            tableModel.addRow(new Object[]{
                    a.getClientName(), a.getClientPhone(), a.getCarLicensePlate(),
                    a.getCarBrand(), a.getCarModel(), a.getCarYear(),
                    dateFormat.format(a.getDate()), a.getProblemDescription(), a.getRepairs(),
                    a.getPartsUsed(), a.getObservations(), a.getStatus()
            });
        }
    }

    // =================================================================================================================
    //  SECTION 3: SELECTION HELPERS
    //  Methods that help with retrieving appointment data directly from visual table
    // =================================================================================================================

    public Appointment getSelectedAppointment() {
        int row = table.getSelectedRow();
        if (row != -1 && row < appointmentList.size()) {
            return appointmentList.get(row);
        }
        return null;
    }

    public void clearSelection() {
        table.clearSelection();
    }

    // Helper for "Select by ID" logic
    // Update this method in AppointmentTableManager.java
    // You will need to import java.util.function.Consumer;

    // In AppointmentTableManager.java

    public void selectById(int id, java.util.function.Consumer<Appointment> onFound) {
        refreshData(); // 1. Refresh data to ensure we have the latest DB state

        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < appointmentList.size(); i++) {
                if (appointmentList.get(i).getAppointmentID() == id) {

                    // 2. Visually highlight the row
                    table.setRowSelectionInterval(i, i);
                    table.scrollRectToVisible(table.getCellRect(i, 0, true));

                    // 3. FORCE CALLBACK: Send the found object back immediately
                    if (onFound != null) {
                        onFound.accept(appointmentList.get(i));
                    }
                    return;
                }
            }
            // Optional: Handle "Not Found" case here
        });
    }

    // --- DUPLICATE CHECKING ---
    public int findDuplicateId(String phone, String plate, java.util.Date date, String desc) {
        for (Appointment a : appointmentList) {
            // Compare formatted dates to ignore seconds/milliseconds differences
            String d1 = dateFormat.format(a.getDate());
            String d2 = dateFormat.format(date);

            // This handles cases where a.getClientPhone() is NULL safely.
            // (null == null) -> true
            // (null == "0744...") -> false
            boolean samePhone = Objects.equals(a.getClientPhone(), phone);

            // We should use it for other fields too, just to be safe
            boolean samePlate = Objects.equals(a.getCarLicensePlate(), plate);
            boolean sameDesc = Objects.equals(a.getProblemDescription(), desc);

            if (samePhone && samePlate && d1.equals(d2) && sameDesc) {
                return a.getAppointmentID();
            }
        }
        return -1;
    }

    // =================================================================================================================
    //  SECTION 4: LANGUAGE HELPER
    //  Method used to update the table header according to preferred language.
    // =================================================================================================================

    public void updateHeaders() {
        String[] cols = {
                LanguageHelper.getString("col.client"), LanguageHelper.getString("col.phone"),
                LanguageHelper.getString("col.plate"), LanguageHelper.getString("col.brand"),
                LanguageHelper.getString("col.model"), LanguageHelper.getString("col.year"),
                LanguageHelper.getString("col.date"), LanguageHelper.getString("col.problem"),
                LanguageHelper.getString("col.repairs"), LanguageHelper.getString("col.parts_used"),
                LanguageHelper.getString("col.observations"), LanguageHelper.getString("col.status")
        };
        tableModel.setColumnIdentifiers(cols);
        table.getColumnModel().getColumn(11).setCellRenderer(new StatusCellRenderer());
    }
}
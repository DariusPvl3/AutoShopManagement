package com.autoshop.app.controller;

import com.autoshop.app.component.ThemedDialog;
import com.autoshop.app.model.Appointment;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class DashboardController {
    private final Component view;
    private final DefaultTableModel tableModel;
    private final List<Appointment> appointmentList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Cache for Calendar counts
    private List<Appointment> calendarCache = new ArrayList<>();

    // UI Updaters (Passed from View)
    private final Consumer<String> updateTodayLabel;
    private final Consumer<String> updateActiveLabel;

    // Navigation Callbacks
    private Consumer<Integer> onJumpRequest;
    private Consumer<Date> onSearchDateRequest;
    private Consumer<Date> onCreateRequest;

    public DashboardController(Component view, DefaultTableModel tableModel,
                               Consumer<String> updateTodayLabel,
                               Consumer<String> updateActiveLabel) {
        this.view = view;
        this.tableModel = tableModel;
        this.updateTodayLabel = updateTodayLabel;
        this.updateActiveLabel = updateActiveLabel;
    }

    public void loadData() {
        try {
            DatabaseHelper.autoUpdateStatuses();

            // 1. Load Dashboard Table Data (Today + Active)
            List<Appointment> dashData = DatabaseHelper.getDashboardAppointments(new Date());
            appointmentList.clear();
            appointmentList.addAll(dashData);
            appointmentList.sort(Comparator.comparing(Appointment::getDate));

            refreshTable();

            // 2. Update Stats Labels
            List<Appointment> activeList = DatabaseHelper.getActiveAppointments();
            long todayCount = appointmentList.stream().filter(a -> Utils.isToday(a.getDate())).count();

            updateTodayLabel.accept(LanguageHelper.getString("dsb.today") + todayCount);
            updateActiveLabel.accept(LanguageHelper.getString("dsb.active") + activeList.size());

            // 3. Load Calendar Cache (All appointments for counting)
            this.calendarCache = DatabaseHelper.getAllAppointments();

        } catch (SQLException e) {
            e.printStackTrace();
            ThemedDialog.showMessage(view, "Error", "Database Error: " + e.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Appointment a : appointmentList) {

            // --- 1. Mask the "PENDING-..." Plate ---
            String displayPlate = a.getCarLicensePlate();
            if (displayPlate != null && displayPlate.startsWith("PENDING-")) {
                displayPlate = "-";
            }

            // --- 2. Mask the NULL Phone ---
            String displayPhone = a.getClientPhone();
            if (displayPhone == null || displayPhone.isEmpty()) {
                displayPhone = "-";
            }

            // --- 3. Add Row with Clean Data ---
            tableModel.addRow(new Object[]{
                    a.getClientName(),
                    displayPhone,
                    displayPlate,
                    a.getCarBrand(),
                    a.getCarModel(),
                    dateFormat.format(a.getDate()),
                    a.getProblemDescription(),
                    a.getStatus()
            });
        }
    }

    // Logic for Calendar Helper
    public int countAppointmentsOnDate(Date date) {
        int count = 0;
        Calendar c1 = Calendar.getInstance(); c1.setTime(date);
        Calendar c2 = Calendar.getInstance();

        for(Appointment a : calendarCache) {
            c2.setTime(a.getDate());
            if(c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                    c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
                count++;
            }
        }
        return count;
    }

    public void handleJumpRequest(int row) {
        if (row != -1 && row < appointmentList.size() && onJumpRequest != null) {
            onJumpRequest.accept(appointmentList.get(row).getAppointmentID());
        }
    }

    // Getters for View to attach list
    public List<Appointment> getAppointmentList() { return appointmentList; }

    // Setters for Navigation
    public void setOnCreateRequest(Consumer<Date> c) { this.onCreateRequest = c; }
    public void setOnJumpRequest(Consumer<Integer> c) { this.onJumpRequest = c; }
    public void setOnSearchDateRequest(Consumer<Date> c) { this.onSearchDateRequest = c; }

    // Proxy methods to trigger navigation
    public void triggerCreate(Date d) { if(onCreateRequest != null) onCreateRequest.accept(d); }
    public void triggerSearch(Date d) { if(onSearchDateRequest != null) onSearchDateRequest.accept(d); }
}
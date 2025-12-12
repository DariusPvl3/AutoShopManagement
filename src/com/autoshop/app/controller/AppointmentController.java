package com.autoshop.app.controller;

import com.autoshop.app.component.ThemedDialog;
import com.autoshop.app.model.Appointment;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Utils; // Import Utils
import com.autoshop.app.view.manager.AppointmentFormManager;
import com.autoshop.app.view.manager.AppointmentTableManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.Date;

public class AppointmentController {
    private final AppointmentFormManager formManager;
    private final AppointmentTableManager tableManager;
    private final Component parentView;

    public AppointmentController(AppointmentFormManager formManager, AppointmentTableManager tableManager, Component parentView) {
        this.formManager = formManager;
        this.tableManager = tableManager;
        this.parentView = parentView;
    }

    public void addAppointment() {
        // 1. Validate & Format Inputs
        // We create a temporary object or vars to hold cleaned data
        ValidationResult data = validateAndFormat(false);
        if (data == null) return; // Validation failed, stop.

        // 2. Check for Duplicates (Logic restored)
        int duplicateId = tableManager.findDuplicateId(data.phone, data.plate, data.date, data.problem);
        if (duplicateId != -1) {
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.duplicate"),
                    LanguageHelper.getString("msg.err.duplicate"));
            // Highlight existing
            tableManager.selectById(duplicateId, null);
            return;
        }

        String portablePhotoPath = com.autoshop.app.util.StorageHelper.copyToAppStorage(data.photo);

        // 3. Create Object
        Appointment newAppt = new Appointment(
                data.name, data.phone, data.plate, data.brand, data.model,
                data.year, portablePhotoPath, data.date, data.problem,
                data.repairs, data.parts, data.obs
        );

        // 4. Database Action
        try {
            DatabaseHelper.addAppointmentTransaction(newAppt);
            tableManager.refreshData();
            formManager.clearAll();
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.success"),
                    LanguageHelper.getString("msg.success.add"));
        } catch (SQLException e) {
            e.printStackTrace();
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.error"),
                    e.getMessage());
        }
    }

    public void updateAppointment() {
        Appointment selected = tableManager.getSelectedAppointment();
        if (selected == null) return;

        boolean confirmed = ThemedDialog.showConfirm(parentView,
                LanguageHelper.getString("title.confirm"),
                LanguageHelper.getString("msg.confirm.update"));

        if (!confirmed) return;

        // 1. Validate & Format Inputs
        ValidationResult data = validateAndFormat(true);
        if (data == null) return;

        // 2. Update Object
        selected.setClientName(data.name);
        selected.setClientPhone(data.phone);
        selected.setCarLicensePlate(data.plate);
        selected.setCarBrand(data.brand);
        selected.setCarModel(data.model);
        selected.setCarYear(data.year);
        selected.setDate(data.date);
        selected.setProblemDescription(data.problem);
        selected.setRepairs(data.repairs);
        selected.setPartsUsed(data.parts);
        selected.setObservations(data.obs);
        String portablePhotoPath = com.autoshop.app.util.StorageHelper.copyToAppStorage(data.photo);
        selected.setCarPhotoPath(portablePhotoPath);

        // 3. Database Action
        try {
            DatabaseHelper.updateAppointmentTransaction(selected);
            tableManager.refreshData();
            formManager.clearAll();
            tableManager.clearSelection();
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.success"),
                    LanguageHelper.getString("msg.success.update"));
        } catch (SQLException e) {
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.error"),
                    e.getMessage());
        }
    }

    // ... (deleteAppointment, onSelectionChanged, clear methods remain the same) ...
    public void deleteAppointment() {
        Appointment selected = tableManager.getSelectedAppointment();
        if (selected == null) {
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.error"),
                    LanguageHelper.getString("msg.err.select"));
            return;
        }
        if (ThemedDialog.showConfirm(parentView, LanguageHelper.getString("title.confirm"), LanguageHelper.getString("msg.confirm.delete"))) {
            try {
                DatabaseHelper.deleteAppointment(selected.getAppointmentID());
                tableManager.refreshData();
                formManager.clearAll();
                tableManager.clearSelection();
            } catch (SQLException e) {
                ThemedDialog.showMessage(parentView, LanguageHelper.getString("title.error"), e.getMessage());
            }
        }
    }

    public void onSelectionChanged() {
        Appointment selected = tableManager.getSelectedAppointment();
        if (selected != null) formManager.loadAppointment(selected);
    }

    public void clear() {
        formManager.clearAll();
        tableManager.clearSelection();
    }

    // --- HELPER: FLEXIBLE VALIDATION ---
    private ValidationResult validateAndFormat(boolean allowPast) {
        // A. Extract Data
        String rawName = formManager.getClientName();
        String rawPhone = formManager.getClientPhone();
        String rawPlate = formManager.getPlate();
        Date date = formManager.getDate();

        // B. Formatting
        String name = (rawName == null || rawName.trim().isEmpty()) ? "Unknown Client" : Utils.toTitleCase(rawName);
        String phone = (rawPhone == null || rawPhone.trim().isEmpty()) ? null : rawPhone; // Null for DB
        String plate = (rawPlate == null || rawPlate.trim().isEmpty()) ? generateTempPlate() : Utils.formatPlate(rawPlate);

        String brand = Utils.toTitleCase(formManager.getBrand());
        String model = Utils.toTitleCase(formManager.getModel());

        // C. Critical Validations (Reduced to minimum)

        // 1. Date is mandatory
        if (date == null) {
            showError("msg.req.name_date"); // You might want to rename this key to "msg.req.date"
            return null;
        }

        // 2. Prevent past dates (only for new appointments)
        if (!allowPast && date.before(new Date())) {
            showError("msg.err.past");
            return null;
        }

        // 3. Validate Phone ONLY if entered
        if (phone != null && !Utils.isValidPhone(phone)) {
            showError("msg.err.phone");
            return null;
        }

        // 4. Validate Plate ONLY if it's a real plate (not our temp one)
        if (!plate.startsWith("PENDING-") && !Utils.isValidPlate(plate)) {
            showError("msg.err.plate");
            return null;
        }

        // D. Return Safe Data
        return new ValidationResult(name, phone, plate, brand, model, formManager.getYear(),
                formManager.getPhotoPath(), date, formManager.getProblem(),
                formManager.getRepairs(), formManager.getParts(), formManager.getObservations());
    }

    private String generateTempPlate() {
        // Generates a unique string like "PENDING-1712345678"
        return "PENDING-" + System.currentTimeMillis();
    }

    private void showError(String langKey) {
        ThemedDialog.showMessage(parentView,
                LanguageHelper.getString("title.error"),
                LanguageHelper.getString(langKey));
    }

    // Simple Data Carrier for validated input
    private record ValidationResult(
            String name, String phone, String plate, String brand, String model,
            int year, String photo, Date date, String problem, String repairs, String parts, String obs
    ) {}

    public void selectPhoto() {
        JFileChooser chooser = new JFileChooser();
        // Filter for formats
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (JPG, PNG, WEBP)", "jpg", "png", "jpeg", "webp"));

        if (chooser.showOpenDialog(parentView) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            formManager.setPhoto(file);
        }
    }

    public void viewPhoto() {
        String path = formManager.getPhotoPath();
        if (path == null || path.isEmpty()) return;

        File file = new File(path);
        if (!file.exists()) {
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.error"),
                    LanguageHelper.getString("msg.err.no_file"));
            return;
        }

        try {
            // 1. Read Image
            java.awt.image.BufferedImage rawImage = javax.imageio.ImageIO.read(file);
            if (rawImage == null) {
                ThemedDialog.showMessage(parentView, "Error", "Cannot load image format.");
                return;
            }

            // 2. Scale Logic (Fit to screen/dialog)
            int maxWidth = 800;
            int maxHeight = 600;
            int imgW = rawImage.getWidth();
            int imgH = rawImage.getHeight();

            if (imgW > maxWidth || imgH > maxHeight) {
                double scale = Math.min((double) maxWidth / imgW, (double) maxHeight / imgH);
                imgW = (int) (imgW * scale);
                imgH = (int) (imgH * scale);
            }

            Image scaledImage = rawImage.getScaledInstance(imgW, imgH, Image.SCALE_SMOOTH);

            // 3. Show Dialog
            JDialog photoDialog = new JDialog();
            photoDialog.setTitle("Photo Viewer");
            photoDialog.setLayout(new BorderLayout());
            photoDialog.add(new JScrollPane(new JLabel(new ImageIcon(scaledImage))), BorderLayout.CENTER);
            photoDialog.setSize(imgW + 50, imgH + 80);
            photoDialog.setLocationRelativeTo(parentView);
            photoDialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            ThemedDialog.showMessage(parentView, "Error", "Failed to load image: " + e.getMessage());
        }
    }
}
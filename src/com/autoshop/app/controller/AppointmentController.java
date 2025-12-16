package com.autoshop.app.controller;

import com.autoshop.app.component.ThemedDialog;
import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.Part; // Import Part
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.StorageHelper; // Ensure StorageHelper is imported
import com.autoshop.app.util.Utils;
import com.autoshop.app.view.manager.AppointmentFormManager;
import com.autoshop.app.view.manager.AppointmentTableManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List; // Import List

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
        ValidationResult data = validateAndFormat(false);
        if (data == null) return; // Validation failed, stop.

        // 2. Check for Duplicates
        // Note: We don't check parts for duplicates, just core details
        int duplicateId = tableManager.findDuplicateId(data.phone, data.plate, data.date, data.problem);
        if (duplicateId != -1) {
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.duplicate"),
                    LanguageHelper.getString("msg.err.duplicate"));
            tableManager.selectById(duplicateId, null);
            return;
        }

        // Copy Photo (USB Support)
        String portablePhotoPath = StorageHelper.copyToAppStorage(data.photo);

        // 3. Create Object (Now passing List<Part>)
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

        selected.setPartList(data.parts); // UPDATED: Set the List<Part>

        selected.setObservations(data.obs);

        // Copy Photo (USB Support)
        String portablePhotoPath = StorageHelper.copyToAppStorage(data.photo);
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
        // 1. Name: Save "-" if empty
        String name = (rawName == null || rawName.trim().isEmpty()) ? "-" : Utils.toTitleCase(rawName);

        // 2. Phone: Keep NULL (Database handles this fine)
        String phone = (rawPhone == null || rawPhone.trim().isEmpty()) ? null : rawPhone;

        // 3. Plate: Keep PENDING-Timestamp (Critical for DB uniqueness)
        String plate = (rawPlate == null || rawPlate.trim().isEmpty()) ? generateTempPlate() : Utils.formatPlate(rawPlate);

        // 4. Brand/Model: Save "-" if empty
        String brand = (formManager.getBrand() == null || formManager.getBrand().isEmpty()) ? "-" : Utils.toTitleCase(formManager.getBrand());
        String model = (formManager.getModel() == null || formManager.getModel().isEmpty()) ? "-" : Utils.toTitleCase(formManager.getModel());

        // 5. Repairs/Observations
        String repairs = (formManager.getRepairs() == null || formManager.getRepairs().isEmpty()) ? "-" : Utils.toTitleCase(formManager.getRepairs());
        String observations = (formManager.getObservations() == null || formManager.getObservations().isEmpty()) ? "-" : Utils.toTitleCase(formManager.getObservations());

        // C. Critical Validations
        if (date == null) {
            showError("msg.req.name_date");
            return null;
        }

        if (!allowPast && date.before(new Date())) {
            showError("msg.err.past");
            return null;
        }

        if (phone != null && !Utils.isValidPhone(phone)) {
            showError("msg.err.phone");
            return null;
        }

        if (!plate.startsWith("PENDING-") && !Utils.isValidPlate(plate)) {
            showError("msg.err.plate");
            return null;
        }

        if (formManager.hasUnsavedPartInput()) {
            formManager.forceAddCurrentPart();
        }
        List<Part> parts = formManager.getParts();

        // D. Return Safe Data
        return new ValidationResult(
                name, phone, plate, brand, model, formManager.getYear(),
                formManager.getPhotoPath(), date, formManager.getProblem(),
                repairs, parts, observations
        );
    }

    private String generateTempPlate() {
        return "PENDING-" + System.currentTimeMillis();
    }

    private void showError(String langKey) {
        ThemedDialog.showMessage(parentView,
                LanguageHelper.getString("title.error"),
                LanguageHelper.getString(langKey));
    }

    // UPDATED: 'parts' is now List<Part>, not String
    private record ValidationResult(
            String name, String phone, String plate, String brand, String model,
            int year, String photo, Date date, String problem, String repairs,
            List<Part> parts, String obs
    ) {}

    public void selectPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (JPG, PNG, WEBP)", "jpg", "png", "jpeg", "webp"));

        if (chooser.showOpenDialog(parentView) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            formManager.setPhoto(file);
        }
    }

    // UPDATED: Includes USB relative path fix and WEBP check
    public void viewPhoto() {
        String relativePath = formManager.getPhotoPath();
        if (relativePath == null || relativePath.isEmpty()) return;

        // Resolve absolute path for USB support
        String absolutePath = StorageHelper.getAbsolutePath(relativePath);

        File file = new File(absolutePath);
        if (!file.exists()) {
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.error"),
                    LanguageHelper.getString("msg.err.no_file"));
            return;
        }

        try {
            // Check for WEBP
            if (file.getName().toLowerCase().endsWith(".webp")) {
                ThemedDialog.showMessage(parentView, "Info",
                        "Preview not supported for WEBP files.\nThe file is saved safely!");
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                return;
            }

            // Read Image
            java.awt.image.BufferedImage rawImage = javax.imageio.ImageIO.read(file);
            if (rawImage == null) {
                ThemedDialog.showMessage(parentView, "Error", "Cannot load image format.");
                return;
            }

            // Scale Logic
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

            // Show Dialog
            JDialog photoDialog = new JDialog();
            photoDialog.setTitle("Photo Viewer - " + file.getName());
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
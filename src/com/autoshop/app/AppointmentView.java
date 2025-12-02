package com.autoshop.app;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

// CHANGE 1: We extend JPanel
public class AppointmentView extends JPanel {

    // --- 1. DATA (No more static) ---
    private ArrayList<Appointment> appointmentList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // --- 2. GUI COMPONENTS (No more static, No Frame) ---
    private DefaultTableModel tableModel;
    private JTable table;

    // Inputs
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField carLicensePlateField;
    private JComboBox<String> carBrandBox;
    private JTextField carModelField;
    private JTextField carYearField;
    private JButton selectPhotoButton;
    private String currentPhotoPath = "";
    private JLabel photoLabel;
    private JTextField problemDescriptionField;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;

    // Buttons
    private JButton addButton;
    private JButton clearButton;
    private JButton updateButton;
    private JButton deleteButton;

    // --- CONSTRUCTOR (This replaces main and createAndShowGUI) ---
    public AppointmentView() {
        // 1. Set the layout of THIS panel
        this.setLayout(new BorderLayout());

        // 2. Initialize
        initializeComponents();

        // 3. Build the sub-panels
        JPanel topPanel = createInputPanel();
        JScrollPane centerPanel = createTablePanel();
        JPanel bottomPanel = createBottomPanel();

        // 4. Add them to THIS panel (instead of frame.add)
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // 5. Start Logic
        setupListeners();
        loadDataFromDB();
        clearInputs();

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadDataFromDB();
            }
        });
    }

    // --- COMPONENT INITIALIZATION ---
    private void initializeComponents() {
        Font inputFont = new Font("SansSerif", Font.PLAIN, 14);

        nameField = new JTextField(12); nameField.setFont(inputFont);
        phoneField = new JTextField(12);  phoneField.setFont(inputFont);
        carLicensePlateField = new JTextField(12);  carLicensePlateField.setFont(inputFont);
        carModelField = new JTextField(12);   carModelField.setFont(inputFont);
        problemDescriptionField = new JTextField(12);  problemDescriptionField.setFont(inputFont);

        String[] carBrands = {"Audi", "BMW", "Chevrolet", "Citroen", "Dacia", "Fiat",
                "Ford", "Honda", "Hyundai", "Kia", "Land Rover", "Mazda",
                "Mercedes", "Mitsubishi", "Nissan", "Opel", "Peugeot",
                "Renault", "Seat", "Skoda", "Suzuki", "Toyota",
                "Volkswagen", "Volvo"};

        carBrandBox = new JComboBox<>(carBrands);
        carBrandBox.setEditable(true);
        carBrandBox.setFont(inputFont);
        AutoCompletion.enable(carBrandBox);

        carYearField = new JTextField(12); carYearField.setFont(inputFont);
        selectPhotoButton = new JButton("Select Photo");
        photoLabel = new JLabel("No Photo Selected");
        photoLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));

        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(new Dimension(130, 25));
        dateChooser.setFont(inputFont);

        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new Date());
        timeSpinner.setPreferredSize(new Dimension(80, 25));
        timeSpinner.setFont(inputFont);

        addButton = new JButton("Add Appointment");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        clearButton = new JButton("Clear");
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        updateButton = new JButton("Update Appointment");
        updateButton.setBackground(new Color(52, 152, 219));
        updateButton.setForeground(Color.WHITE);
        updateButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateButton.setEnabled(false);

        deleteButton = new JButton("Delete Appointment");
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        String[] columns = {"Client Name", "Phone", "License Plate", "Brand", "Model", "Year", "Date", "Description", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setFont(inputFont);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());
    }

    private JPanel createInputPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel clientPanel = createStyledPanel("Client Details");
        clientPanel.add(new JLabel("Name:"));
        clientPanel.add(nameField);
        clientPanel.add(Box.createVerticalStrut(10));
        clientPanel.add(new JLabel("Phone:"));
        clientPanel.add(phoneField);

        JPanel carPanel = createStyledPanel("Car Details");
        carPanel.add(new JLabel("Brand:"));
        carPanel.add(carBrandBox);
        carPanel.add(new JLabel("Model:"));
        carPanel.add(carModelField);
        carPanel.add(new JLabel("License Plate:"));
        carPanel.add(carLicensePlateField);
        carPanel.add(new JLabel("Year:"));
        carPanel.add(carYearField);

        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        photoPanel.add(selectPhotoButton);
        photoPanel.add(Box.createVerticalStrut(10));
        photoPanel.add(photoLabel);
        carPanel.add(new JLabel("Car registration:"));
        carPanel.add(photoPanel);

        JPanel apptPanel = createStyledPanel("Appointment Info");
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dateTimePanel.add(dateChooser);
        dateTimePanel.add(new JLabel("  at  "));
        dateTimePanel.add(timeSpinner);

        apptPanel.add(new JLabel("Date & Time:"));
        apptPanel.add(dateTimePanel);
        apptPanel.add(Box.createVerticalStrut(5));
        apptPanel.add(new JLabel("Problem:"));
        apptPanel.add(problemDescriptionField);
        apptPanel.add(Box.createVerticalStrut(10));

        JPanel buttonContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonContainer.add(addButton);
        buttonContainer.add(clearButton);
        apptPanel.add(buttonContainer);

        mainPanel.add(clientPanel);
        mainPanel.add(carPanel);
        mainPanel.add(apptPanel);

        return mainPanel;
    }

    private JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        title, 0, 0, new Font("SansSerif", Font.BOLD, 16)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }

    private JScrollPane createTablePanel() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 15),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        return scroll;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        panel.add(updateButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(deleteButton);
        panel.add(Box.createHorizontalStrut(15));
        return panel;
    }

    // --- LOGIC & LISTENERS (Removed 'static', replaced 'frame' with 'this') ---
    private void setupListeners() {
        addButton.addActionListener(e -> addAppointment());
        clearButton.addActionListener(e -> clearInputs());
        deleteButton.addActionListener(e -> deleteAppointment());
        updateButton.addActionListener(e -> updateAppointment());

        StatusMenuHelper.attach(table, appointmentList, this::loadDataFromDB, this);

        selectPhotoButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images",  "jpg", "png", "jpeg"));
            // CHANGE: Use 'this' as parent
            int result = chooser.showOpenDialog(this);
            if(result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                currentPhotoPath = selectedFile.getAbsolutePath();
                photoLabel.setText(selectedFile.getName());
                photoLabel.setForeground(new Color(46, 204, 113));
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadAppointmentToForm(table.getSelectedRow());
            }
        });

        addChangeListeners();
    }

    private boolean validateAndFormatInput() {
        String rawName = nameField.getText().trim();
        nameField.setText(Utils.toTitleCase(rawName));

        String rawPlate = carLicensePlateField.getText();
        carLicensePlateField.setText(Utils.formatPlate(rawPlate));

        String rawModel = carModelField.getText().trim();
        carModelField.setText(Utils.toTitleCase(rawModel));

        String rawBrand = (String) carBrandBox.getSelectedItem();
        if (rawBrand != null && !rawBrand.isEmpty()) {
            String fixedBrand = Utils.toTitleCase(rawBrand);
            carBrandBox.getEditor().setItem(fixedBrand);
        }

        if (nameField.getText().isEmpty() || dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Client Name and Date are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Utils.isValidPhone(phoneField.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid Phone Number!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!carLicensePlateField.getText().matches("^[A-Z]{1,2}-[0-9]{2,3}-[A-Z]{3}$")) {
            JOptionPane.showMessageDialog(this, "Invalid License Plate!\nFormat required: TM-12-ABC", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void addAppointment() {
        if (!validateAndFormatInput()) return;

        String name = nameField.getText();
        String phone = phoneField.getText();
        String plate = carLicensePlateField.getText();
        String brand = (String) carBrandBox.getSelectedItem();
        String model = carModelField.getText().replace(";", ",");

        int year = 0;
        try {
            year = Integer.parseInt(carYearField.getText().trim());
        } catch (NumberFormatException e) {
            // keep 0
        }

        String carRegPhoto = currentPhotoPath;
        String desc = problemDescriptionField.getText().replace(";", ",");
        Date finalDate = getMergedDateFromInput();

        if (finalDate.before(new Date())) {
            JOptionPane.showMessageDialog(this, "Cannot schedule in the past!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int duplicateRow = findDuplicateRow(phone, plate, finalDate, desc);
        if (duplicateRow != -1) {
            JOptionPane.showMessageDialog(this, "Appointment already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            table.setRowSelectionInterval(duplicateRow, duplicateRow);
            table.scrollRectToVisible(table.getCellRect(duplicateRow, 0, true));
            return;
        }

        Appointment newAppt = new Appointment(name, phone, plate, brand, model, year, carRegPhoto, finalDate, desc);

        try {
            DatabaseHelper.addAppointmentTransaction(newAppt);
            loadDataFromDB();
            clearInputs();
            JOptionPane.showMessageDialog(this, "Appointment Scheduled!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private void updateAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        if (JOptionPane.showConfirmDialog(this, "Update this appointment?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        if (!validateAndFormatInput()) return;

        Appointment currentAppt = appointmentList.get(selectedRow);

        currentAppt.setClientName(nameField.getText());
        currentAppt.setClientPhone(phoneField.getText());
        currentAppt.setCarLicensePlate(carLicensePlateField.getText());
        currentAppt.setCarBrand((String) carBrandBox.getSelectedItem());
        currentAppt.setCarModel(carModelField.getText());
        currentAppt.setDate(getMergedDateFromInput());
        currentAppt.setProblemDescription(problemDescriptionField.getText());

        try {
            currentAppt.setCarYear(Integer.parseInt(carYearField.getText().trim()));
        } catch(Exception e) {}
        currentAppt.setCarPhotoPath(currentPhotoPath);

        try {
            DatabaseHelper.updateAppointmentTransaction(currentAppt);
            loadDataFromDB();
            clearInputs();
            JOptionPane.showMessageDialog(this, "Updated Successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating: " + e.getMessage());
        }
    }

    private void deleteAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an appointment first!");
            return;
        }

        int response = JOptionPane.showConfirmDialog(this, "Delete this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            try {
                Appointment appt = appointmentList.get(selectedRow);
                DatabaseHelper.deleteAppointment(appt.getAppointmentID());
                loadDataFromDB();
                clearInputs();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting: " + e.getMessage());
            }
        }
    }

    private void clearInputs() {
        table.clearSelection();
        updateButton.setEnabled(false);

        nameField.setText("");
        phoneField.setText("");
        carLicensePlateField.setText("");
        carBrandBox.setSelectedIndex(-1);
        carModelField.setText("");
        carYearField.setText("");
        currentPhotoPath = "";
        photoLabel.setText("No Photo");
        problemDescriptionField.setText("");
        dateChooser.setDate(new Date());
        timeSpinner.setValue(new Date());
        nameField.requestFocus();
    }

    private void loadDataFromDB() {
        try {
            DatabaseHelper.autoUpdateStatuses();

            appointmentList.clear();
            appointmentList.addAll(DatabaseHelper.getAllAppointments());
            appointmentList.sort((a, b) -> a.getDate().compareTo(b.getDate()));
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading database: " + e.getMessage());
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
                    appt.getCarModel(),
                    appt.getCarYear(),
                    dateFormat.format(appt.getDate()),
                    appt.getProblemDescription(),
                    appt.getStatus()
            });
        }
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private void addChangeListeners() {
        Runnable enableUpdate = () -> {
            if (table.getSelectedRow() != -1) {
                updateButton.setEnabled(true);
            }
        };

        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { enableUpdate.run(); }
            public void removeUpdate(DocumentEvent e) { enableUpdate.run(); }
            public void changedUpdate(DocumentEvent e) { enableUpdate.run(); }
        };

        nameField.getDocument().addDocumentListener(docListener);
        phoneField.getDocument().addDocumentListener(docListener);
        carLicensePlateField.getDocument().addDocumentListener(docListener);
        carModelField.getDocument().addDocumentListener(docListener);
        carYearField.getDocument().addDocumentListener(docListener);
        problemDescriptionField.getDocument().addDocumentListener(docListener);
        ((JTextField) carBrandBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(docListener);

        carBrandBox.addActionListener(e -> enableUpdate.run());
        dateChooser.addPropertyChangeListener("date", e -> enableUpdate.run());
        timeSpinner.addChangeListener(e -> enableUpdate.run());
    }

    private void loadAppointmentToForm(int rowIndex) {
        Appointment appt = appointmentList.get(rowIndex);
        updateButton.setEnabled(false);

        nameField.setText(appt.getClientName());
        phoneField.setText(appt.getClientPhone());
        carLicensePlateField.setText(appt.getCarLicensePlate());
        carBrandBox.setSelectedItem(appt.getCarBrand());
        carModelField.setText(appt.getCarModel());
        carYearField.setText(String.valueOf(appt.getCarYear()));
        currentPhotoPath = appt.getCarPhotoPath();
        if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
            photoLabel.setText(new File(currentPhotoPath).getName());
        } else {
            photoLabel.setText("No Photo");
        }

        problemDescriptionField.setText(appt.getProblemDescription());
        dateChooser.setDate(appt.getDate());
        timeSpinner.setValue(appt.getDate());
    }

    private Date getMergedDateFromInput() {
        Date date = dateChooser.getDate();
        Date time = (Date) timeSpinner.getValue();
        return Utils.combineDateAndTime(date, time);
    }

    private int findDuplicateRow(String phone, String carLicensePlate, Date date, String problemDescription) {
        int row = -1;
        for (int i = 0; i < appointmentList.size(); i++) {
            Appointment appt = appointmentList.get(i);
            if (appt.getClientPhone().equals(phone) &&
                    appt.getCarLicensePlate().equals(carLicensePlate) &&
                    dateFormat.format(appt.getDate()).equals(dateFormat.format(date)) &&
                    appt.getProblemDescription().equals(problemDescription)) {
                row = i;
                break;
            }
        }
        return row;
    }
}
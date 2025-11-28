package com.autoshop.app;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.toedter.calendar.JDateChooser;

public class Main {

    // --- 1. DATA & CONFIG ---
    private static ArrayList<Appointment> appointmentList = new ArrayList<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // --- 2. GUI COMPONENTS (Class Level so all methods can see them) ---
    private static JFrame frame;
    private static DefaultTableModel tableModel;
    private static JTable table;

    // Inputs
    private static JTextField nameField;
    private static JTextField phoneField;
    private static JTextField carLicensePlateField;
    private static JComboBox<String> carBrandBox;
    private static JTextField carModelField;
    private static JTextField carYearField;
    private static JButton selectPhotoButton;
    private static String currentPhotoPath = "";
    private static JLabel photoLabel;
    private static JTextField problemDescriptionField;
    private static JDateChooser dateChooser;
    private static JSpinner timeSpinner;

    // Buttons
    private static JButton addButton;
    private static JButton clearButton;
    private static JButton updateButton;
    private static JButton deleteButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                com.formdev.flatlaf.FlatDarkLaf.setup();
                DatabaseHelper.createNewTable();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error! " + e.getMessage());
            }
            new Main().createAndShowGUI();
        });
    }

    public void createAndShowGUI() {
        // A. Setup Frame
        frame = new JFrame("Service Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        // B. Initialize Components
        initializeComponents();

        // C. Build Layouts
        JPanel topPanel = createInputPanel();
        JScrollPane centerPanel = createTablePanel();
        JPanel bottomPanel = createBottomPanel();

        // D. Add Logic
        setupListeners();
        loadDataFromDB();

        // E. Assemble Frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // --- COMPONENT INITIALIZATION ---
    private static void initializeComponents() {
        // Text Fields
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

        // Date & Time
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

        // Buttons
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
        updateButton.setEnabled(false); // Disabled by default

        deleteButton = new JButton("Delete Appointment");
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Table
        String[] columns = {"com.autoshop.app.Client Name", "Phone", "License Plate", "Brand", "Model", "Year", "Date", "Description", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        // Table styling
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setFont(inputFont);
    }

    // --- LAYOUT METHODS ---
    private static JPanel createInputPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- SECTION 1: CLIENT ---
        JPanel clientPanel = createStyledPanel("Client Details");
        clientPanel.add(new JLabel("Name:"));
        clientPanel.add(nameField);
        clientPanel.add(Box.createVerticalStrut(10)); // Spacer
        clientPanel.add(new JLabel("Phone:"));
        clientPanel.add(phoneField);

        // --- SECTION 2: CAR ---
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

        // --- SECTION 3: APPOINTMENT ---
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

        // Add sections to main panel
        mainPanel.add(clientPanel);
        mainPanel.add(carPanel);
        mainPanel.add(apptPanel);

        return mainPanel;
    }

    private static JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        title,
                        0,
                        0,
                        new Font("SansSerif", Font.BOLD, 16)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }

    private static JScrollPane createTablePanel() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 15),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        return scroll;
    }

    private static JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        panel.add(updateButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(deleteButton);
        panel.add(Box.createHorizontalStrut(15));
        return panel;
    }

    // --- LOGIC & LISTENERS ---
    private static void setupListeners() {
        addButton.addActionListener(e -> addAppointment());
        clearButton.addActionListener(e -> clearInputs());
        deleteButton.addActionListener(e -> deleteAppointment());
        updateButton.addActionListener(e -> updateAppointment());
        selectPhotoButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images",  "jpg", "png", "jpeg"));

           int result = chooser.showOpenDialog(frame);
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
        enableEnterKeyNavigation();
    }

    private static boolean validateAndFormatInput() {
        // Capitalize name
        String rawName = nameField.getText().trim();
        nameField.setText(Utils.toTitleCase(rawName));

        // License Plate: Uppercase, replace spaces (e.g. "tm 12 abc" -> "TM-12-ABC")
        String rawPlate = carLicensePlateField.getText();
        carLicensePlateField.setText(Utils.formatPlate(rawPlate));

        // Model: Capitalize (Golf -> Golf, golf -> Golf)
        String rawModel = carModelField.getText().trim();
        carModelField.setText(Utils.toTitleCase(rawModel));

        // Brand: Get from Combo Box
        String rawBrand = (String) carBrandBox.getSelectedItem();
        if (rawBrand != null && !rawBrand.isEmpty()) {
            String fixedBrand = Utils.toTitleCase(rawBrand);
            carBrandBox.getEditor().setItem(fixedBrand);
        }

        // 2. VALIDATION (Check for errors)

        // Check Empty Fields
        if (nameField.getText().isEmpty() || dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(frame, "Client Name and Date are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Utils.isValidPhone(phoneField.getText())) {
            JOptionPane.showMessageDialog(frame, "Invalid Phone Number!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check License Plate (Romanian Standard)
        // Regex: 1-2 letters (County), 2-3 digits, 3 letters
        if (!carLicensePlateField.getText().matches("^[A-Z]{1,2}-[0-9]{2,3}-[A-Z]{3}$")) {
            JOptionPane.showMessageDialog(frame, "Invalid License Plate!\nFormat required: TM-12-ABC", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private static void addAppointment() {
        if (!validateAndFormatInput()) return;

        // 1. Prepare Data
        String name = nameField.getText();
        String phone = phoneField.getText();
        String plate = carLicensePlateField.getText();
        String brand = (String) carBrandBox.getSelectedItem();
        String model = carModelField.getText().replace(";", ",");
        int year = Integer.parseInt(carYearField.getText());
        String carRegPhoto = currentPhotoPath;
        String desc = problemDescriptionField.getText().replace(";", ",");
        Date finalDate = getMergedDateFromInput();

        // 2. Date Check
        if (finalDate.before(new Date())) {
            JOptionPane.showMessageDialog(frame, "Cannot schedule in the past!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int duplicateRow = findDuplicateRow(phone, plate, finalDate, desc);
        if (duplicateRow != -1) {
            JOptionPane.showMessageDialog(frame, "Appointment already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            table.setRowSelectionInterval(duplicateRow, duplicateRow);
            table.scrollRectToVisible(table.getCellRect(duplicateRow, 0, true));
            return;
        }

        // 3. Create Object (Using the new UI-friendly constructor)
        Appointment newAppt = new Appointment(name, phone, plate, brand, model, year, carRegPhoto, finalDate, desc);

        // 4. Save to DB (The Transaction)
        try {
            DatabaseHelper.addAppointmentTransaction(newAppt);

            // Reload to see the changes (and get the new IDs)
            loadDataFromDB();
            clearInputs();
            JOptionPane.showMessageDialog(frame, "Appointment Scheduled!");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
        }
    }
    private static void updateAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        if (JOptionPane.showConfirmDialog(frame, "Update this appointment?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        if (!validateAndFormatInput()) return;

        // 1. Get the existing object (so we have the IDs)
        Appointment currentAppt = appointmentList.get(selectedRow);

        // 2. Update the fields with new UI data
        currentAppt.setClientName(nameField.getText());
        currentAppt.setClientPhone(phoneField.getText());
        currentAppt.setCarLicensePlate(carLicensePlateField.getText());
        currentAppt.setCarBrand((String) carBrandBox.getSelectedItem());
        currentAppt.setCarModel(carModelField.getText());
        currentAppt.setDate(getMergedDateFromInput());
        currentAppt.setProblemDescription(problemDescriptionField.getText());
        // (Status remains whatever it was, or you can add a dropdown for it later)

        // 3. Send to DB
        try {
            DatabaseHelper.updateAppointmentTransaction(currentAppt);
            loadDataFromDB();
            clearInputs();
            JOptionPane.showMessageDialog(frame, "Updated Successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error updating: " + e.getMessage());
        }
    }

    private static void deleteAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select an appointment first!");
            return;
        }

        int response = JOptionPane.showConfirmDialog(frame, "Delete this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            try {
                // Get the ID from the list
                Appointment appt = appointmentList.get(selectedRow);

                // Delete from DB
                DatabaseHelper.deleteAppointment(appt.getAppointmentID());

                // Reload
                loadDataFromDB();
                clearInputs();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error deleting: " + e.getMessage());
            }
        }
    }
    private static void clearInputs() {
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

    // --- DATA PERSISTENCE ---
    private static void loadDataFromDB() {
        try{
            appointmentList = (ArrayList<Appointment>) DatabaseHelper.getAllAppointments();
            appointmentList.sort((a, b) -> a.getDate().compareTo(b.getDate()));
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading database: " + e.getMessage());
        }
    }

    private static void refreshTable() {
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
        if(table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    // --- HELPER FUNCTIONS ---

    private static void enableEnterKeyNavigation(){
        JComponent[] order = {
                nameField,
                phoneField,
                carLicensePlateField,
                carBrandBox,
                problemDescriptionField,
                addButton
        };
        for (JComponent component : order) {
            if(component instanceof JTextField){
                ((JTextField) component).addActionListener(e -> component.transferFocus());
            } else if(component instanceof JComboBox){
                Component editor = ((JComboBox<?>) component).getEditor().getEditorComponent();
                if (editor instanceof JTextField) {
                    ((JTextField) editor).addActionListener(e -> {
                        ((JComboBox<?>) component).hidePopup();
                        component.transferFocus();
                    });
                }
            }
        }
    }

    private static void addChangeListeners(){
        Runnable enableUpdate = () -> {
            if(table.getSelectedRow() != -1){
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
        problemDescriptionField.getDocument().addDocumentListener(docListener);
        ((JTextField)carBrandBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(docListener);

        carBrandBox.addActionListener(e -> enableUpdate.run());
        dateChooser.addPropertyChangeListener("date", e -> enableUpdate.run());
        timeSpinner.addChangeListener(e -> enableUpdate.run());
    }

    private static void loadAppointmentToForm(int rowIndex) {
        Appointment appt = appointmentList.get(rowIndex);
        updateButton.setEnabled(false);

        nameField.setText(appt.getClientName());
        phoneField.setText(appt.getClientPhone());
        carLicensePlateField.setText(appt.getCarLicensePlate());
        carBrandBox.setSelectedItem(appt.getCarBrand());
        carModelField.setText(appt.getCarModel());
        carYearField.setText(String.valueOf(appt.getCarYear()));
        currentPhotoPath = appt.getCarPhotoPath();
        if(currentPhotoPath != null && !currentPhotoPath.isEmpty()){
            photoLabel.setText(new File(currentPhotoPath).getName());
        } else {
            photoLabel.setText("No Photo");
        }

        problemDescriptionField.setText(appt.getProblemDescription());
        dateChooser.setDate(appt.getDate());
        timeSpinner.setValue(appt.getDate());
    }

    private static Date getMergedDateFromInput() {
        Date date = dateChooser.getDate();
        Date time = (Date) timeSpinner.getValue();
        return Utils.combineDateAndTime(date, time);
    }

    private static int findDuplicateRow(String phone, String carLicensePlate, Date date, String problemDescription) {
        int row = -1;
        for(int i = 0; i < appointmentList.size(); i++){
            Appointment appt = appointmentList.get(i);
            if(appt.getClientPhone().equals(phone) &&
            appt.getCarLicensePlate().equals(carLicensePlate) &&
            dateFormat.format(appt.getDate()).equals(dateFormat.format(date)) &&
            appt.getProblemDescription().equals(problemDescription)){
                row = i;
                break;
            }
        }
        return row;
    }
}
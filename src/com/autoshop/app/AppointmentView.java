package com.autoshop.app;

import com.toedter.calendar.JDateChooser;
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

public class AppointmentView extends JPanel {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AppointmentView.class.getName());

    private final ArrayList<Appointment> appointmentList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

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

    // --- CONSTRUCTOR ---
    public AppointmentView() {
        this.setLayout(new BorderLayout());

        initializeComponents();

        // 1. Build the sub-panels
        JPanel topPanel = createInputPanel();
        JScrollPane centerPanel = createTablePanel();
        JPanel bottomPanel = createBottomPanel();

        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // 2. Start Logic
        setupListeners();
        setUpShortcuts();
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
        Font inputFont = new Font("SansSerif", Font.PLAIN, 16);

        nameField = new JTextField(12); nameField.setFont(inputFont);
        phoneField = new JTextField(12);  phoneField.setFont(inputFont);
        carLicensePlateField = new JTextField(12);  carLicensePlateField.setFont(inputFont);
        carModelField = new JTextField(12);   carModelField.setFont(inputFont);
        problemDescriptionField = new JTextField(12);  problemDescriptionField.setFont(inputFont);

        String[] carBrands = {"Audi", "BMW", "Chevrolet", "Citroen", "Dacia", "Fiat",
                "Ford", "Honda", "Hyundai", "Jeep", "Kia", "Land Rover", "Mazda",
                "Mercedes", "Mitsubishi", "Mini", "Nissan", "Opel", "Peugeot",
                "Renault", "Seat", "Skoda", "Suzuki", "Tesla", "Toyota",
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
        dateChooser.setPreferredSize(new Dimension(130, 30));
        CalendarCustomizer.styleDateChooser(dateChooser);
        dateChooser.setFont(inputFont);

        SpinnerDateModel model = new SpinnerDateModel();
        model.setCalendarField(Calendar.MINUTE);

        timeSpinner = new JSpinner(model);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);

        JFormattedTextField tf = ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField();
        tf.setEditable(true);
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setFont(inputFont);

        timeSpinner.setValue(new Date());
        timeSpinner.setPreferredSize(new Dimension(80, 25));
        timeSpinner.setFont(inputFont);

        Utils.addMouseScrollToSpinner(timeSpinner);

        addButton = new RoundedButton("Add Appointment");
        ButtonStyler.apply(addButton, new Color(46, 204, 113)); // Green

        clearButton = new RoundedButton("Clear");
        ButtonStyler.apply(clearButton, new Color(149, 165, 166)); // Concrete Grey

        updateButton = new RoundedButton("Update Appointment");
        ButtonStyler.apply(updateButton, new Color(52, 152, 219)); // Blue
        updateButton.setEnabled(false); // Keep disabled logic

        deleteButton = new RoundedButton("Delete Appointment");
        ButtonStyler.apply(deleteButton, new Color(231, 76, 60)); // Red

        String[] columns = {"Client Name", "Phone", "License Plate", "Brand", "Model", "Year", "Date", "Description", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
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

        JPanel appointmentPanel = createStyledPanel("Appointment Info");
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dateTimePanel.add(dateChooser);
        dateTimePanel.add(new JLabel("  at  "));
        dateTimePanel.add(timeSpinner);

        appointmentPanel.add(new JLabel("Date & Time:"));
        appointmentPanel.add(dateTimePanel);
        appointmentPanel.add(Box.createVerticalStrut(5));
        appointmentPanel.add(new JLabel("Problem:"));
        appointmentPanel.add(problemDescriptionField);
        appointmentPanel.add(Box.createVerticalStrut(10));

        JPanel buttonContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonContainer.add(addButton);
        buttonContainer.add(clearButton);
        appointmentPanel.add(buttonContainer);

        mainPanel.add(clientPanel);
        mainPanel.add(carPanel);
        mainPanel.add(appointmentPanel);

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

    // --- LOGIC & LISTENERS ---
    private void setupListeners() {
        addButton.addActionListener(_ -> addAppointment());
        clearButton.addActionListener(_ -> clearInputs());
        deleteButton.addActionListener(_ -> deleteAppointment());
        updateButton.addActionListener(_ -> updateAppointment());

        StatusMenuHelper.attach(table, appointmentList, this::loadDataFromDB, this);

        selectPhotoButton.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images",  "jpg", "png", "jpeg"));
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
            return true;
        }

        if (!Utils.isValidPhone(phoneField.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid Phone Number!", "Error", JOptionPane.ERROR_MESSAGE);
            return true;
        }

        if (!carLicensePlateField.getText().matches("^[A-Z]{1,2}-[0-9]{2,3}-[A-Z]{3}$")) {
            JOptionPane.showMessageDialog(this, "Invalid License Plate!\nFormat required: TM-12-ABC", "Error", JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    private void addAppointment() {
        if (validateAndFormatInput()) return;

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
        Date finalDate = Utils.combineDateAndTime(dateChooser.getDate(), (Date) timeSpinner.getValue());

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

        Appointment newAppointment = new Appointment(name, phone, plate, brand, model, year, carRegPhoto, finalDate, desc);

        try {
            DatabaseHelper.addAppointmentTransaction(newAppointment);
            loadDataFromDB();
            clearInputs();
            JOptionPane.showMessageDialog(this, "Appointment Scheduled!");
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error adding appointment", e);
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private void updateAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        if (JOptionPane.showConfirmDialog(this, "Update this appointment?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        if (validateAndFormatInput()) return;

        Appointment currentAppointment = appointmentList.get(selectedRow);

        currentAppointment.setClientName(nameField.getText());
        currentAppointment.setClientPhone(phoneField.getText());
        currentAppointment.setCarLicensePlate(carLicensePlateField.getText());
        currentAppointment.setCarBrand((String) carBrandBox.getSelectedItem());
        currentAppointment.setCarModel(carModelField.getText());
        currentAppointment.setDate(Utils.combineDateAndTime(dateChooser.getDate(), (Date) timeSpinner.getValue()));
        currentAppointment.setProblemDescription(problemDescriptionField.getText());

        try {
            currentAppointment.setCarYear(Integer.parseInt(carYearField.getText().trim()));
        } catch(Exception _) {}
        currentAppointment.setCarPhotoPath(currentPhotoPath);

        try {
            DatabaseHelper.updateAppointmentTransaction(currentAppointment);
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
                Appointment appointment = appointmentList.get(selectedRow);
                DatabaseHelper.deleteAppointment(appointment.getAppointmentID());
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
            appointmentList.sort(Comparator.comparing(Appointment::getDate));
            refreshTable();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error adding appointment", e);
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private void refreshTable() {
        appointmentList.sort(Comparator.comparing(Appointment::getDate));
        tableModel.setRowCount(0);
        for (Appointment appointment : appointmentList) {
            tableModel.addRow(new Object[]{
                    appointment.getClientName(),
                    appointment.getClientPhone(),
                    appointment.getCarLicensePlate(),
                    appointment.getCarBrand(),
                    appointment.getCarModel(),
                    appointment.getCarYear(),
                    dateFormat.format(appointment.getDate()),
                    appointment.getProblemDescription(),
                    appointment.getStatus()
            });
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

        carBrandBox.addActionListener(_ -> enableUpdate.run());
        dateChooser.addPropertyChangeListener("date", _ -> enableUpdate.run());
        timeSpinner.addChangeListener(_ -> enableUpdate.run());
    }

    private void loadAppointmentToForm(int rowIndex) {
        Appointment appointment = appointmentList.get(rowIndex);
        updateButton.setEnabled(false);

        nameField.setText(appointment.getClientName());
        phoneField.setText(appointment.getClientPhone());
        carLicensePlateField.setText(appointment.getCarLicensePlate());
        carBrandBox.setSelectedItem(appointment.getCarBrand());
        carModelField.setText(appointment.getCarModel());
        carYearField.setText(String.valueOf(appointment.getCarYear()));
        currentPhotoPath = appointment.getCarPhotoPath();
        if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
            photoLabel.setText(new File(currentPhotoPath).getName());
        } else {
            photoLabel.setText("No Photo");
        }

        problemDescriptionField.setText(appointment.getProblemDescription());
        dateChooser.setDate(appointment.getDate());
        timeSpinner.setValue(appointment.getDate());
    }

    private int findDuplicateRow(String phone, String carLicensePlate, Date date, String problemDescription) {
        int row = -1;
        for (int i = 0; i < appointmentList.size(); i++) {
            Appointment appointment = appointmentList.get(i);
            if (appointment.getClientPhone().equals(phone) &&
                    appointment.getCarLicensePlate().equals(carLicensePlate) &&
                    dateFormat.format(appointment.getDate()).equals(dateFormat.format(date)) &&
                    appointment.getProblemDescription().equals(problemDescription)) {
                row = i;
                break;
            }
        }
        return row;
    }

    // Public method so MainFrame can call it
    public void selectAppointmentById(int appointmentId) {
        // 1. Force Reload (Sync with DB)
        try {
            appointmentList.clear();
            appointmentList.addAll(DatabaseHelper.getAllAppointments());
            refreshTable();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error adding appointment", e);
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }

        // 2. Find and Select
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < appointmentList.size(); i++) {
                if (appointmentList.get(i).getAppointmentID() == appointmentId) {
                    // Highlight the row
                    table.setRowSelectionInterval(i, i);
                    table.scrollRectToVisible(table.getCellRect(i, 0, true));

                    loadAppointmentToForm(i);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Appointment not found (ID " + appointmentId + ")");
        });
    }

    public void prepareNewAppointment(Date date) {
        // 1. Set the specific date passed from Dashboard
        if (date != null) {
            dateChooser.setDate(date);
        }

        // 2. Default time to 08:00 (Start of work day)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        timeSpinner.setValue(cal.getTime());

        // 3. Focus Name field for instant typing
        nameField.requestFocus();
    }

    private void setUpShortcuts(){
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        // 1. Define the Key Strokes
        KeyStroke addKey = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        KeyStroke clearKey = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        KeyStroke updateKey = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        KeyStroke deleteKey = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0);

        // 2. Map Keys to Action Names
        inputMap.put(addKey, "addAppointment");
        inputMap.put(clearKey, "clearFields");
        inputMap.put(updateKey, "updateAppointment");
        inputMap.put(deleteKey, "deleteAppointment");

        // 3. Map Action Names to Actual Logic
        actionMap.put("addAppointment", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Visual feedback: click the button programmatically so it flashes
                if (addButton.isEnabled()) addButton.doClick();
            }
        });

        actionMap.put("clearFields", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (clearButton.isEnabled()) clearButton.doClick();
            }
        });

        actionMap.put("updateAppointment", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (updateButton.isEnabled()) updateButton.doClick();
            }
        });

        actionMap.put("deleteAppointment", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (deleteButton.isEnabled()) deleteButton.doClick();
            }
        });
    }
}
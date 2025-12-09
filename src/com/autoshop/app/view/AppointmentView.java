package com.autoshop.app.view;

import com.autoshop.app.component.*;
import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.Car;
import com.autoshop.app.model.Client;
import com.autoshop.app.util.*;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class AppointmentView extends JPanel {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AppointmentView.class.getName());
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 12);
    private static final Font INPUT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    private final ArrayList<Appointment> appointmentList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private DefaultTableModel tableModel;
    private JTable table;
    private JPanel clientPanel, carPanel, appointmentPanel;

    private JTextField problemDescriptionField;
    private JComboBox<String> nameField, phoneField, carLicensePlateField, carModelField, carBrandBox;
    private JButton selectPhotoButton, viewPhotoButton, addButton, clearButton, updateButton, deleteButton;
    private String currentPhotoPath = "";
    private JLabel photoLabel;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner, carYearField;
    private JTextArea repairsField, partsUsedField, observationsField;

    private JLabel nameLabel, phoneLabel, plateLabel, brandLabel, modelLabel, yearLabel, photoTitleLabel, dateLabel, problemLabel, repairsLabel, partsUsedLabel, observationsLabel, atLabel;

    public AppointmentView() {
        setLayout(new BorderLayout());
        initializeComponents();

        // --- MAIN LAYOUT (Vertical Split) ---
        // Top Half: Input Forms
        // Bottom Half: Table

        // 1. Wrapper for all Forms
        JPanel formsWrapper = new JPanel();
        formsWrapper.setLayout(new BoxLayout(formsWrapper, BoxLayout.Y_AXIS));
        formsWrapper.setBackground(Theme.OFF_WHITE);

        // A. Top Row (Client & Car)
        JPanel topRow = new JPanel(new GridLayout(1, 2, 20, 0)); // 1 Row, 2 Cols, 20px gap
        topRow.setBackground(Theme.OFF_WHITE);
        topRow.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10)); // Padding

        clientPanel = createClientPanel();
        carPanel = createCarPanel();

        topRow.add(clientPanel);
        topRow.add(carPanel);

        // B. Appointment Panel (Includes Buttons)
        appointmentPanel = createAppointmentPanel();

        // Add to wrapper
        formsWrapper.add(topRow);
        formsWrapper.add(appointmentPanel);

        // 2. Split Pane Setup
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Wrap forms in ScrollPane (in case screen is small verticaly)
        JScrollPane formScroll = new JScrollPane(formsWrapper);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.setMinimumSize(new Dimension(200, 200));

        splitPane.setTopComponent(formScroll);
        splitPane.setBottomComponent(createTablePanel());

        // Give the forms enough space, leave the rest for table
        splitPane.setDividerSize(20);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.0); // Table gets the resize priority

        add(splitPane, BorderLayout.CENTER);

        setupListeners();
        setUpShortcuts();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { loadDataFromDB(); }
        });

        LanguageHelper.addListener(this::updateText);
        updateText();
        clearInputs();
    }

    // --- INITIALIZATION ---

    private void initializeComponents() {
        initLabels();
        initInputs();
        initButtons();
        initTable();
    }

    private void initLabels() {
        nameLabel = createLabel();
        phoneLabel = createLabel();
        plateLabel = createLabel();
        brandLabel = createLabel();
        modelLabel = createLabel();
        yearLabel = createLabel();
        photoTitleLabel = createLabel();
        dateLabel = createLabel();
        problemLabel = createLabel();
        repairsLabel = createLabel();
        partsUsedLabel = createLabel();
        observationsLabel = createLabel();
        atLabel = createLabel();

        photoLabel = new JLabel("No Photo Selected");
        photoLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
    }

    private void initInputs() {
        nameField = new JComboBox<>();
        phoneField = new JComboBox<>();
        carLicensePlateField = new  JComboBox<>();
        carModelField = new  JComboBox<>();
        problemDescriptionField = createTextField();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        SpinnerNumberModel yearModel = new SpinnerNumberModel(currentYear, 1900, currentYear + 1, 1);
        carYearField = new JSpinner(yearModel);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(carYearField, "#");
        carYearField.setEditor(editor);
        styleSpinner(carYearField);

        styleComboBox(nameField);
        styleComboBox(phoneField);
        styleComboBox(carLicensePlateField);
        styleComboBox(carModelField);

        AutoCompletion.enable(nameField, text -> {
            try {
                List<Client> clients = DatabaseHelper.getClientsByName(text);
                List<String> names = new ArrayList<>();
                for (Client client : clients) {
                    names.add(client.getClientName());
                }
                return names;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return new ArrayList<>();
            }
        });
        nameField.setEditable(true);
        AutoCompletion.enable(phoneField, text -> {
            try {
                List<Client> clients = DatabaseHelper.getClientsByPhone(text);
                List<String> phones = new ArrayList<>();
                for (Client client : clients) {
                    phones.add(client.getClientPhone());
                }
                return phones;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return new ArrayList<>();
            }
        });
        phoneField.setEditable(true);
        AutoCompletion.enable(carLicensePlateField, text -> {
            try {
                List<Car> cars = DatabaseHelper.getCarDetailsByPlate(text);
                List<String> plates = new ArrayList<>();
                for (Car car : cars) {
                    plates.add(car.getLicensePlate());
                }
                return plates;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return new ArrayList<>();
            }
        });
        carLicensePlateField.setEditable(true);
        AutoCompletion.enable(carModelField, text -> {
            try {
                Object selectedBrandObj = carBrandBox.getSelectedItem();
                String selectedBrand = (selectedBrandObj != null) ? selectedBrandObj.toString() : "";
                if (selectedBrand.isEmpty()) return new ArrayList<>();
                List<Car> cars = DatabaseHelper.getCarModelsByBrand(selectedBrand, text);
                List<String> models = new ArrayList<>();
                for (Car car : cars) {
                    if (!models.contains(car.getCarModel())) {
                        models.add(car.getCarModel());
                    }
                }
                return models;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return new ArrayList<>();
            }
        });
        carModelField.setEditable(true);

        repairsField = createTextArea();
        partsUsedField = createTextArea();
        observationsField = createTextArea();

        String[] carBrands = {"Audi", "BMW", "Chevrolet", "Citroen", "Dacia", "Fiat", "Ford", "Honda",
                "Hyundai", "Jeep", "Kia", "Land Rover", "Mazda", "Mercedes", "Mitsubishi",
                "Mini", "Nissan", "Opel", "Peugeot", "Renault", "Seat", "Skoda", "Suzuki",
                "Tesla", "Toyota", "Volkswagen", "Volvo"};
        carBrandBox = new JComboBox<>(carBrands);
        carBrandBox.setEditable(true);
        styleComboBox(carBrandBox);
        AutoCompletion.enable(carBrandBox);

        selectPhotoButton = new RoundedButton("Select Photo");
        ButtonStyler.apply(selectPhotoButton, Theme.RED);

        viewPhotoButton = new RoundedButton("View Photo");
        ButtonStyler.apply(viewPhotoButton, Theme.GRAY);
        viewPhotoButton.setEnabled(false);

        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(new Dimension(130, 30));
        dateChooser.setFont(INPUT_FONT);
        CalendarCustomizer.styleDateChooser(dateChooser);

        setupTimeSpinner();
    }

    private void setupTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        model.setCalendarField(Calendar.MINUTE);
        timeSpinner = new JSpinner(model);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setPreferredSize(new Dimension(80, 25));
        styleSpinner(timeSpinner);
    }

    private void initButtons() {
        addButton = new RoundedButton("Add");
        ButtonStyler.apply(addButton, Theme.RED);

        clearButton = new RoundedButton("Clear");
        ButtonStyler.apply(clearButton, Theme.GRAY);

        updateButton = new RoundedButton("Update");
        ButtonStyler.apply(updateButton, Theme.BLACK);
        updateButton.setEnabled(false);

        deleteButton = new RoundedButton("Delete");
        ButtonStyler.apply(deleteButton, Theme.RED);
    }

    private void initTable() {
        String[] columns = {"Client Name", "Phone", "License Plate", "Brand", "Model", "Year", "Date", "Description", "Repairs", "Parts used", "Observations", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = SwingTableStyler.create(tableModel, 11);
        table.getColumnModel().getColumn(11).setCellRenderer(new StatusCellRenderer());
    }

    // --- PANEL CREATION (The Key Changes) ---

    private JPanel createClientPanel() {
        // Simple Grid for Name/Phone
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 20));
        panel.setBackground(Theme.WHITE);
        panel.setBorder(createStyledBorder("sect.client"));

        panel.add(createFieldGroup(nameLabel, nameField));
        panel.add(createFieldGroup(phoneLabel, phoneField));

        return panel;
    }

    private JPanel createCarPanel() {
        // GridBagLayout to align specific pairs
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.WHITE);
        panel.setBorder(createStyledBorder("sect.car"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Brand | Model
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 0.5; panel.add(createFieldGroup(brandLabel, carBrandBox), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; panel.add(createFieldGroup(modelLabel, carModelField), gbc);

        // Row 1: License Plate | Year
        gbc.gridy = 1;
        gbc.gridx = 0; panel.add(createFieldGroup(plateLabel, carLicensePlateField), gbc);
        gbc.gridx = 1; panel.add(createFieldGroup(yearLabel, carYearField), gbc);

        // Row 2: Photo (Centered, Span 2)
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.gridwidth = 2;

        JPanel photoGroup = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoGroup.setBackground(Theme.WHITE);
        photoGroup.add(selectPhotoButton);
        photoGroup.add(viewPhotoButton);
        photoLabel.setFont(INPUT_FONT);
        photoGroup.add(photoLabel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.WHITE);
        photoTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrapper.add(photoTitleLabel, BorderLayout.NORTH);
        wrapper.add(photoGroup, BorderLayout.CENTER);

        panel.add(wrapper, gbc);

        return panel;
    }

    private JPanel createAppointmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.WHITE);
        // Use EmptyBorder for outer padding, StyledBorder for title
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                createStyledBorder("sect.appointment")
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15); // Good spacing
        gbc.fill = GridBagConstraints.BOTH;      // Fill cells
        gbc.weightx = 0.5;                       // Equal width columns

        // --- ROW 0: Date (Left) | Problem (Right) ---
        gbc.gridy = 0;

        // Left: Date
        gbc.gridx = 0;
        JPanel dateGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        dateGroup.setBackground(Theme.WHITE);
        dateGroup.add(dateChooser);
        dateGroup.add(atLabel);
        dateGroup.add(timeSpinner);

        JPanel dateWrapper = new JPanel(new BorderLayout(0, 5));
        dateWrapper.setBackground(Theme.WHITE);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dateWrapper.add(dateLabel, BorderLayout.NORTH);
        dateWrapper.add(dateGroup, BorderLayout.CENTER);
        panel.add(dateWrapper, gbc);

        // Right: Problem
        gbc.gridx = 1;
        panel.add(createFieldGroup(problemLabel, problemDescriptionField), gbc);

        // --- ROW 1: Repairs (Left) | Parts Used (Right) ---
        gbc.gridy = 1;
        gbc.weighty = 1.0; // Allow text areas to grow vertically

        gbc.gridx = 0;
        panel.add(createFieldGroup(repairsLabel, repairsField), gbc);

        gbc.gridx = 1;
        panel.add(createFieldGroup(partsUsedLabel, partsUsedField), gbc);

        // --- ROW 2: Observations (Left) | Buttons (Right) ---
        gbc.gridy = 2;

        gbc.gridx = 0;
        panel.add(createFieldGroup(observationsLabel, observationsField), gbc);

        // Right: Buttons (2x2 Grid)
        gbc.gridx = 1;

        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 10, 10)); // 2x2 Grid
        buttonGrid.setBackground(Theme.WHITE);
        buttonGrid.add(addButton);
        buttonGrid.add(clearButton);
        buttonGrid.add(updateButton);
        buttonGrid.add(deleteButton);

        // Align button grid to bottom of cell
        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setBackground(Theme.WHITE);
        btnWrapper.add(buttonGrid, BorderLayout.SOUTH);

        panel.add(btnWrapper, gbc);

        return panel;
    }

    // Helper to stack Label on top of Component
    private JPanel createFieldGroup(JComponent label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(Theme.WHITE);
        if (label instanceof JLabel) {
            ((JLabel)label).setHorizontalAlignment(SwingConstants.CENTER);
        }
        p.add(label, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JScrollPane createTablePanel() {
        JScrollPane scroll = new JScrollPane(table);
        // Clean border for the table
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        scroll.setMinimumSize(new Dimension(150, 150));
        return scroll;
    }

    // --- ACTIONS (Unchanged) ---

    private void addAppointment() {
        if (validateAndFormatInput()) return;

        String name = (String) nameField.getEditor().getItem();
        String phone = (String) phoneField.getEditor().getItem();
        String plate = (String) carLicensePlateField.getEditor().getItem();
        String brand = (String) carBrandBox.getSelectedItem();
        String model = carModelField.getEditor().getItem().toString().replace(";", ",");
        String desc = problemDescriptionField.getText().replace(";", ",");
        String repairs = repairsField.getText().replace(";", ",");
        String partsUsed = partsUsedField.getText().replace(";", ",");
        String observations = observationsField.getText().replace(";", ",");
        Date finalDate = Utils.combineDateAndTime(dateChooser.getDate(), (Date) timeSpinner.getValue());

        int year = (Integer) carYearField.getValue();

        if (finalDate.before(new Date())) {
            showError("msg.err.past");
            return;
        }

        int duplicateRow = findDuplicateRow(phone, plate, finalDate, desc);
        if (duplicateRow != -1) {
            ThemedDialog.showMessage(this, LanguageHelper.getString("title.duplicate"), LanguageHelper.getString("msg.err.duplicate"));
            table.setRowSelectionInterval(duplicateRow, duplicateRow);
            table.scrollRectToVisible(table.getCellRect(duplicateRow, 0, true));
            return;
        }

        Appointment newAppt = new Appointment(name, phone, plate, brand, model, year, currentPhotoPath, finalDate, desc, repairs, partsUsed, observations);

        try {
            DatabaseHelper.addAppointmentTransaction(newAppt);
            loadDataFromDB();
            clearInputs();
            ThemedDialog.showMessage(this, LanguageHelper.getString("title.success"), LanguageHelper.getString("msg.success.add"));
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error adding", e);
            ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString("msg.err.db") + "\n" + e.getMessage());
        }
    }

    private void updateAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        boolean confirmed = ThemedDialog.showConfirm(this,
                LanguageHelper.getString("title.confirm"),
                LanguageHelper.getString("msg.confirm.update"));

        if (!confirmed) return;

        if (validateAndFormatInput()) return;

        Appointment appt = appointmentList.get(selectedRow);
        appt.setClientName(nameField.getEditor().getItem().toString());
        appt.setClientPhone(phoneField.getEditor().getItem().toString());
        appt.setCarLicensePlate(carLicensePlateField.getEditor().getItem().toString());
        appt.setCarBrand((String) carBrandBox.getSelectedItem());
        appt.setCarModel(carModelField.getEditor().getItem().toString());
        appt.setDate(Utils.combineDateAndTime(dateChooser.getDate(), (Date) timeSpinner.getValue()));
        appt.setProblemDescription(problemDescriptionField.getText());
        appt.setRepairs(repairsField.getText());
        appt.setPartsUsed(partsUsedField.getText());
        appt.setObservations(observationsField.getText());
        appt.setCarPhotoPath(currentPhotoPath);
        appt.setCarYear((Integer) carYearField.getValue());

        try {
            DatabaseHelper.updateAppointmentTransaction(appt);
            loadDataFromDB();
            clearInputs();
            ThemedDialog.showMessage(this, LanguageHelper.getString("title.success"), LanguageHelper.getString("msg.success.update"));
        } catch (SQLException e) {
            ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString("msg.err.update") + " " + e.getMessage());
        }
    }

    private void deleteAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString("msg.err.select"));
            return;
        }

        boolean confirmed = ThemedDialog.showConfirm(this,
                LanguageHelper.getString("title.confirm"),
                LanguageHelper.getString("msg.confirm.delete"));

        if (confirmed) {
            try {
                DatabaseHelper.deleteAppointment(appointmentList.get(selectedRow).getAppointmentID());
                loadDataFromDB();
                clearInputs();
            } catch (SQLException e) {
                ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString("msg.err.delete") + " " + e.getMessage());
            }
        }
    }

    private void clearInputs() {
        table.clearSelection();
        updateButton.setEnabled(false);
        nameField.setSelectedItem("");
        phoneField.setSelectedItem("");
        carLicensePlateField.setSelectedItem("");
        carBrandBox.setSelectedIndex(-1);
        carModelField.setSelectedItem("");
        carYearField.setValue(2000);
        currentPhotoPath = "";
        photoLabel.setText(LanguageHelper.getString("msg.no_photo"));
        problemDescriptionField.setText("");
        partsUsedField.setText("");
        observationsField.setText("");
        repairsField.setText("");
        dateChooser.setDate(new Date());
        timeSpinner.setValue(new Date());
        nameField.requestFocus();
    }

    // --- DATA & LOGIC ---

    private void loadDataFromDB() {
        try {
            DatabaseHelper.autoUpdateStatuses();
            appointmentList.clear();
            appointmentList.addAll(DatabaseHelper.getAllAppointments());
            refreshTable();
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error loading DB", e);
            ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString("msg.err.db") + e.getMessage());
        }
    }

    private void refreshTable() {
        appointmentList.sort(Comparator.comparing(Appointment::getDate));
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

    private boolean validateAndFormatInput() {
        nameField.setSelectedItem(Utils.toTitleCase(nameField.getEditor().getItem().toString().trim()));
        carLicensePlateField.setSelectedItem(Utils.formatPlate(carLicensePlateField.getEditor().getItem().toString()));
        carModelField.setSelectedItem(Utils.toTitleCase(carModelField.getEditor().getItem().toString().trim()));

        String rawBrand = (String) carBrandBox.getSelectedItem();
        if (rawBrand != null && !rawBrand.isEmpty()) {
            carBrandBox.getEditor().setItem(Utils.toTitleCase(rawBrand));
        }

        if (nameField.getEditor().getItem().toString().isEmpty() || dateChooser.getDate() == null) {
            showError("msg.req.name_date");
            return true;
        }
        if (!Utils.isValidPhone(phoneField.getEditor().getItem().toString())) {
            showError("msg.err.phone");
            return true;
        }
        if (!carLicensePlateField.getEditor().getItem().toString().matches("^[A-Z]{1,2}-[0-9]{2,3}-[A-Z]{3}$")) {
            showError("msg.err.plate");
            return true;
        }
        return false;
    }

    // --- LISTENERS & SHORTCUTS ---

    private void setupListeners() {
        addButton.addActionListener(_ -> addAppointment());
        clearButton.addActionListener(_ -> clearInputs());
        deleteButton.addActionListener(_ -> deleteAppointment());
        updateButton.addActionListener(_ -> updateAppointment());

        StatusMenuHelper.attach(table, appointmentList, this::loadDataFromDB, this);

        // 1. Phone Selected -> Fill Name
        phoneField.addActionListener(e -> {
            // Only trigger if the popup is not visible (selection made) or Enter pressed
            if (!phoneField.isPopupVisible()) {
                String selectedPhone = (String) phoneField.getEditor().getItem();
                if (selectedPhone != null && !selectedPhone.isEmpty()) {
                    try {
                        List<Client> clients = DatabaseHelper.getClientsByPhone(selectedPhone);
                        if (!clients.isEmpty()) {
                            nameField.setSelectedItem(clients.get(0).getClientName());
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // 2. Name Selected -> Fill Phone (Only if phone is empty, to avoid overwriting)
        nameField.addActionListener(e -> {
            if (!nameField.isPopupVisible()) {
                String selectedName = (String) nameField.getEditor().getItem();
                String currentPhone = (String) phoneField.getEditor().getItem();

                if (selectedName != null && !selectedName.isEmpty() && (currentPhone == null || currentPhone.isEmpty())) {
                    try {
                        List<Client> clients = DatabaseHelper.getClientsByName(selectedName);
                        if (!clients.isEmpty()) {
                            phoneField.setSelectedItem(clients.get(0).getClientPhone());
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // 3. License Plate Selected -> Fill Car Details (Brand, Model, Year)
        carLicensePlateField.addActionListener(e -> {
            if (!carLicensePlateField.isPopupVisible()) {
                String selectedPlate = (String) carLicensePlateField.getEditor().getItem();
                if (selectedPlate != null && !selectedPlate.isEmpty()) {
                    try {
                        List<Car> cars = DatabaseHelper.getCarDetailsByPlate(selectedPlate);
                        if (!cars.isEmpty()) {
                            Car car = cars.get(0);
                            carBrandBox.setSelectedItem(car.getCarBrand());
                            carModelField.setSelectedItem(car.getCarModel());
                            carYearField.setValue(car.getYear());
                            String path = car.getPhotoPath();

                            if (path != null && !path.isEmpty()) {
                                currentPhotoPath = path;
                                photoLabel.setText(new File(path).getName());
                                photoLabel.setForeground(new Color(46, 204, 113));
                                viewPhotoButton.setEnabled(true);
                            } else {
                                currentPhotoPath = "";
                                photoLabel.setText(LanguageHelper.getString("msg.no_photo"));
                                photoLabel.setForeground(Color.BLACK);
                                viewPhotoButton.setEnabled(false);
                            }

                            // Optional: If we found the car, we likely know the client too
                            // Could add logic here to reverse-lookup the client ID if needed.
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        selectPhotoButton.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                currentPhotoPath = file.getAbsolutePath();
                photoLabel.setText(file.getName());
                photoLabel.setForeground(new Color(46, 204, 113));
                viewPhotoButton.setEnabled(true);
            }
        });

        viewPhotoButton.addActionListener(_ -> {
            if(currentPhotoPath == null || currentPhotoPath.isEmpty()) return;

            File file = new  File(currentPhotoPath);
            if(!file.exists()){
                ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString("msg.err.no_file"));
                return;
            }

            ImageIcon originalIcon = new ImageIcon(file.getAbsolutePath());

            int maxWidth = 800;
            int maxHeight = 600;
            int imgWidth = originalIcon.getIconWidth();
            int imgHeight = originalIcon.getIconHeight();

            if(imgWidth > maxWidth || imgHeight > maxHeight) {
                double widthRatio = (double) imgWidth / maxWidth;
                double heightRatio = (double) imgHeight / maxHeight;
                double scale = Math.min(widthRatio, heightRatio);
                imgWidth = (int) (imgWidth * scale);
                imgHeight = (int) (imgHeight * scale);
            }

            Image scaledImage = originalIcon.getImage().getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH);

            JDialog photoDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), LanguageHelper.getString("title.photo_viewer"), true);
            photoDialog.setLayout(new BorderLayout());

            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            photoDialog.add(new JScrollPane(imageLabel), BorderLayout.CENTER);

            photoDialog.setSize(imgWidth + 50, imgHeight + 80);
            photoDialog.setLocationRelativeTo(this);
            photoDialog.setVisible(true);
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                loadAppointmentToForm(table.getSelectedRow());
            }
        });

        Runnable enableUpdate = () -> {
            if (table.getSelectedRow() != -1) updateButton.setEnabled(true);
        };

        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { enableUpdate.run(); }
            public void removeUpdate(DocumentEvent e) { enableUpdate.run(); }
            public void changedUpdate(DocumentEvent e) { enableUpdate.run(); }
        };

        ((JTextField) nameField.getEditor().getEditorComponent()).getDocument().addDocumentListener(docListener);
        ((JTextField) phoneField.getEditor().getEditorComponent()).getDocument().addDocumentListener(docListener);
        ((JTextField) carLicensePlateField.getEditor().getEditorComponent()).getDocument().addDocumentListener(docListener);
        ((JTextField) carModelField.getEditor().getEditorComponent()).getDocument().addDocumentListener(docListener);
        problemDescriptionField.getDocument().addDocumentListener(docListener);
        repairsField.getDocument().addDocumentListener(docListener);
        partsUsedField.getDocument().addDocumentListener(docListener);
        observationsField.getDocument().addDocumentListener(docListener);
        ((JTextField) carBrandBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(docListener);
        carBrandBox.addActionListener(_ -> enableUpdate.run());
        dateChooser.addPropertyChangeListener("date", _ -> enableUpdate.run());
        timeSpinner.addChangeListener(_ -> enableUpdate.run());
    }

    private void setUpShortcuts() {
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK), "add");
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK), "clear");
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK), "update");
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0), "delete");

        actionMap.put("add", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { if (addButton.isEnabled()) addButton.doClick(); }
        });
        actionMap.put("clear", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { if (clearButton.isEnabled()) clearButton.doClick(); }
        });
        actionMap.put("update", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { if (updateButton.isEnabled()) updateButton.doClick(); }
        });
        actionMap.put("delete", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { if (deleteButton.isEnabled()) deleteButton.doClick(); }
        });
    }

    // --- HELPER METHODS ---

    private void loadAppointmentToForm(int rowIndex) {
        Appointment appt = appointmentList.get(rowIndex);
        updateButton.setEnabled(false);

        nameField.setSelectedItem(appt.getClientName());
        phoneField.setSelectedItem(appt.getClientPhone());
        carLicensePlateField.setSelectedItem(appt.getCarLicensePlate());
        carBrandBox.setSelectedItem(appt.getCarBrand());
        carModelField.setSelectedItem(appt.getCarModel());
        carYearField.setValue(appt.getCarYear());
        currentPhotoPath = appt.getCarPhotoPath();

        photoLabel.setText((currentPhotoPath != null && !currentPhotoPath.isEmpty())
                ? new File(currentPhotoPath).getName() : LanguageHelper.getString("msg.no_photo"));
        viewPhotoButton.setEnabled((currentPhotoPath != null && !currentPhotoPath.isEmpty()));

        problemDescriptionField.setText(appt.getProblemDescription());
        repairsField.setText(appt.getRepairs());
        partsUsedField.setText(appt.getPartsUsed());
        observationsField.setText(appt.getObservations());
        dateChooser.setDate(appt.getDate());
        timeSpinner.setValue(appt.getDate());
    }

    private int findDuplicateRow(String phone, String plate, Date date, String desc) {
        for (int i = 0; i < appointmentList.size(); i++) {
            Appointment a = appointmentList.get(i);
            if (a.getClientPhone().equals(phone) && a.getCarLicensePlate().equals(plate) &&
                    dateFormat.format(a.getDate()).equals(dateFormat.format(date)) &&
                    a.getProblemDescription().equals(desc)) {
                return i;
            }
        }
        return -1;
    }

    public void selectAppointmentById(int id) {
        loadDataFromDB();
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < appointmentList.size(); i++) {
                if (appointmentList.get(i).getAppointmentID() == id) {
                    table.setRowSelectionInterval(i, i);
                    table.scrollRectToVisible(table.getCellRect(i, 0, true));
                    loadAppointmentToForm(i);
                    return;
                }
            }
            ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString("msg.err.not_found") + " (ID: " + id + ")");
        });
    }

    public void prepareNewAppointment(Date date) {
        if (date != null) dateChooser.setDate(date);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        timeSpinner.setValue(cal.getTime());
        nameField.requestFocus();
    }

    private void showError(String langKey) {
        ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), LanguageHelper.getString(langKey));
    }

    private void updateText() {
        addButton.setText(LanguageHelper.getString("btn.add"));
        updateButton.setText(LanguageHelper.getString("btn.update"));
        deleteButton.setText(LanguageHelper.getString("btn.delete"));
        clearButton.setText(LanguageHelper.getString("btn.clear"));
        selectPhotoButton.setText(LanguageHelper.getString("btn.select_photo"));
        viewPhotoButton.setText(LanguageHelper.getString("btn.view_photo"));

        nameLabel.setText(LanguageHelper.getString("lbl.name"));
        phoneLabel.setText(LanguageHelper.getString("lbl.phone"));
        plateLabel.setText(LanguageHelper.getString("lbl.plate"));
        brandLabel.setText(LanguageHelper.getString("lbl.brand"));
        modelLabel.setText(LanguageHelper.getString("lbl.model"));
        yearLabel.setText(LanguageHelper.getString("lbl.year"));
        photoTitleLabel.setText(LanguageHelper.getString("lbl.photo"));
        dateLabel.setText(LanguageHelper.getString("lbl.date"));
        problemLabel.setText(LanguageHelper.getString("lbl.problem"));
        repairsLabel.setText(LanguageHelper.getString("lbl.repairs"));
        partsUsedLabel.setText(LanguageHelper.getString("lbl.parts_used"));
        observationsLabel.setText(LanguageHelper.getString("lbl.observations"));
        atLabel.setText(LanguageHelper.getString("lbl.at"));

        if(currentPhotoPath.isEmpty()) photoLabel.setText(LanguageHelper.getString("msg.no_photo"));

        if (tableModel != null) {
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
            StatusMenuHelper.attach(table, appointmentList, this::loadDataFromDB, this);
        }

        clientPanel.setBorder(createStyledBorder("sect.client"));
        carPanel.setBorder(createStyledBorder("sect.car"));
        appointmentPanel.setBorder(createStyledBorder("sect.appointment"));
    }

    // UI Helpers

    private void addLabeledComponent(JPanel panel, JComponent label, JComponent field) {
        addCentered(panel, label);
        panel.add(Box.createVerticalStrut(5));
        addCentered(panel, field);
    }

    private void addCentered(JPanel panel, JComponent comp) {
        comp.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (comp instanceof JLabel) ((JLabel) comp).setHorizontalAlignment(SwingConstants.CENTER);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, comp.getPreferredSize().height));
        panel.add(comp);
    }

    private Border createStyledBorder(String titleKey) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Theme.BLACK, 1),
                        LanguageHelper.getString(titleKey),
                        0, 0, new Font("SansSerif", Font.BOLD, 16), Theme.BLACK),
                BorderFactory.createEmptyBorder(15, 35, 15, 35)
        );
    }

    private JLabel createLabel() {
        JLabel l = new JLabel();
        l.setFont(LABEL_FONT);
        return l;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField(12);
        tf.setFont(INPUT_FONT);
        return tf;
    }

    private JTextArea createTextArea() {
        JTextArea ta = new JTextArea(2, 1);
        ta.setFont(INPUT_FONT);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return ta;
    }

    private void styleComboBox(JComboBox<String> box) {
        // 1. Apply the Custom UI
        box.setUI(new ModernComboBoxUI());

        // 2. Basic Setup
        box.setEditable(true);
        box.setFont(INPUT_FONT);

        // 3. Style the internal text field
        // Note: setting the UI re-creates the editor, so we fetch it again here
        JTextField editor = (JTextField) box.getEditor().getEditorComponent();
        editor.setFont(INPUT_FONT);

        // 4. Apply the Border
        // This puts the gray border around the *text* part.
        // The arrow button sits inside the ComboBox but outside this editor border in BasicUI layout.
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // 5. Backgrounds
        box.setBackground(Color.WHITE);
        editor.setBackground(Color.WHITE);
    }

    private void styleSpinner(JSpinner spinner) {
        // 1. Apply Red Buttons UI
        spinner.setUI(new ModernSpinnerUI());

        spinner.setFont(INPUT_FONT);

        // 2. Fix the Comma (1,999 -> 1999) ONLY if it is a Number Model
        if (spinner.getModel() instanceof SpinnerNumberModel) {
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
            spinner.setEditor(editor);
        }

        // 3. Style the internal text field
        // We get the editor's text field regardless of whether it's Date or Number
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setFont(INPUT_FONT);
            textField.setHorizontalAlignment(SwingConstants.CENTER);
            textField.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }

        // 4. Apply the external border
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // 5. Mouse Scroll
        Utils.addMouseScrollToSpinner(spinner);
    }
}
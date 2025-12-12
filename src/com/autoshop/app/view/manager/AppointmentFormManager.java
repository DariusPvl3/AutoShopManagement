package com.autoshop.app.view.manager;

import com.autoshop.app.component.*;
import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.Car;
import com.autoshop.app.model.Client;
import com.autoshop.app.util.*;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class AppointmentFormManager {
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 12);
    private static final Font INPUT_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private String currentPhotoPath = "";

    // Components
    private JComboBox<String> nameField, phoneField, carLicensePlateField, carModelField, carBrandBox;
    private JTextField problemDescriptionField;
    private JSpinner timeSpinner, carYearField;
    private JTextArea repairsField, partsUsedField, observationsField;
    private JDateChooser dateChooser;
    private JLabel nameLabel, phoneLabel, plateLabel, brandLabel, modelLabel, yearLabel, photoTitleLabel, dateLabel, problemLabel, repairsLabel, partsUsedLabel, observationsLabel, atLabel, photoLabel;
    private JButton selectPhotoButton, viewPhotoButton, addButton, clearButton, updateButton, deleteButton;

    public AppointmentFormManager() {
        initLabels();
        initInputs();
        initButtons();
        setupAutoFillListeners();
    }

    // =================================================================================================================
    //  SECTION 1: PUBLIC LAYOUT API
    //  These methods are called by AppointmentView to assemble the screen.
    // =================================================================================================================

    public JPanel createClientPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 20));
        panel.setBackground(Theme.WHITE);
        panel.setBorder(createStyledBorder("sect.client"));

        panel.add(createFieldGroup(nameLabel, nameField));
        panel.add(createFieldGroup(phoneLabel, phoneField));
        return panel;
    }

    public JPanel createCarPanel() {
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

    public JPanel createAppointmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                createStyledBorder("sect.appointment")
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;

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
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        panel.add(createFieldGroup(repairsLabel, repairsField), gbc);

        gbc.gridx = 1;
        panel.add(createFieldGroup(partsUsedLabel, partsUsedField), gbc);

        // --- ROW 2: Observations (Left) | Buttons (Right) ---
        gbc.gridy = 2;

        gbc.gridx = 0;
        panel.add(createFieldGroup(observationsLabel, observationsField), gbc);

        // Right: Buttons
        gbc.gridx = 1;
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonGrid.setBackground(Theme.WHITE);
        buttonGrid.add(addButton);
        buttonGrid.add(clearButton);
        buttonGrid.add(updateButton);
        buttonGrid.add(deleteButton);

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setBackground(Theme.WHITE);
        btnWrapper.add(buttonGrid, BorderLayout.SOUTH);

        panel.add(btnWrapper, gbc);
        return panel;
    }

    // =================================================================================================================
    //  SECTION 2: PUBLIC DATA API
    //  Getters for extracting data from the form.
    // =================================================================================================================

    public String getClientName() { return (String) nameField.getEditor().getItem(); }
    public String getClientPhone() { return (String) phoneField.getEditor().getItem(); }
    public String getPlate() { return (String) carLicensePlateField.getEditor().getItem(); }
    public String getBrand() { return (String) carBrandBox.getSelectedItem(); }
    public String getModel() { return (String) carModelField.getEditor().getItem(); }
    public int getYear() { return (Integer) carYearField.getValue(); }
    public String getPhotoPath() { return currentPhotoPath; }

    public String getProblem() { return problemDescriptionField.getText(); }
    public String getRepairs() { return repairsField.getText(); }
    public String getParts() { return partsUsedField.getText(); }
    public String getObservations() { return observationsField.getText(); }

    public Date getDate() {
        return Utils.combineDateAndTime(dateChooser.getDate(), (Date) timeSpinner.getValue());
    }

    public JButton getAddButton() { return addButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getSelectPhotoButton() { return selectPhotoButton; }
    public JButton getViewPhotoButton() { return viewPhotoButton; }

    // =================================================================================================================
    //  SECTION 3: PUBLIC FORM LOGIC
    //  Methods to control form state (Clear / Load / Prepare).
    // =================================================================================================================

    public void clearAll() {
        nameField.setSelectedItem("");
        phoneField.setSelectedItem("");
        carLicensePlateField.setSelectedItem("");
        carBrandBox.setSelectedIndex(-1);
        carModelField.setSelectedItem("");
        carYearField.setValue(Calendar.getInstance().get(Calendar.YEAR));
        currentPhotoPath = "";
        photoLabel.setText(LanguageHelper.getString("msg.no_photo"));
        photoLabel.setForeground(Color.BLACK);

        problemDescriptionField.setText("");
        partsUsedField.setText("");
        observationsField.setText("");
        repairsField.setText("");
        dateChooser.setDate(new Date());
        timeSpinner.setValue(new Date());

        updateButton.setEnabled(false);
        viewPhotoButton.setEnabled(false);
        nameField.requestFocus();
    }

    public void loadAppointment(Appointment appointment) {
        nameField.setSelectedItem(appointment.getClientName());
        phoneField.setSelectedItem(appointment.getClientPhone());
        carLicensePlateField.setSelectedItem(appointment.getCarLicensePlate());
        carBrandBox.setSelectedItem(appointment.getCarBrand());
        carModelField.setSelectedItem(appointment.getCarModel());
        carYearField.setValue(appointment.getCarYear());

        currentPhotoPath = appointment.getCarPhotoPath();
        if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
            java.io.File file = new java.io.File(currentPhotoPath);
            String fileName = file.getName();
            photoLabel.setText(truncate(fileName));
            photoLabel.setToolTipText(fileName);
            photoLabel.setForeground(new Color(46, 204, 113));
            viewPhotoButton.setEnabled(true);
        } else {
            photoLabel.setText(LanguageHelper.getString("msg.no_photo"));
            photoLabel.setForeground(Color.BLACK);
            viewPhotoButton.setEnabled(false);
        }

        problemDescriptionField.setText(appointment.getProblemDescription());
        repairsField.setText(appointment.getRepairs());
        partsUsedField.setText(appointment.getPartsUsed());
        observationsField.setText(appointment.getObservations());
        dateChooser.setDate(appointment.getDate());
        timeSpinner.setValue(appointment.getDate());

        updateButton.setEnabled(true);
    }

    public void prepareForNew(Date date) {
        // 1. Set the Date (if provided)
        dateChooser.setDate(Objects.requireNonNullElseGet(date, Date::new));

        // 2. Reset Time to 08:00 AM
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        timeSpinner.setValue(cal.getTime());

        // 3. Focus Name Field for immediate typing
        nameField.requestFocus();
    }

    // =================================================================================================================
    //  SECTION 4: PRIVATE INITIALIZATION
    //  Setup logic for components.
    // =================================================================================================================

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

    private void initButtons() {
        addButton = new RoundedButton("Add");
        ButtonStyler.apply(addButton, Theme.RED);

        clearButton = new RoundedButton("Clear");
        ButtonStyler.apply(clearButton, Theme.GRAY);

        updateButton = new RoundedButton("Update");
        ButtonStyler.apply(updateButton, Theme.GRAY);
        updateButton.setEnabled(false);

        deleteButton = new RoundedButton("Delete");
        ButtonStyler.apply(deleteButton, Theme.RED);
    }

    private void initInputs() {
        nameField = new JComboBox<>();
        phoneField = new JComboBox<>();
        carLicensePlateField = new JComboBox<>();
        carModelField = new JComboBox<>();
        problemDescriptionField = createTextField();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        SpinnerNumberModel yearModel = new SpinnerNumberModel(currentYear, 1900, currentYear + 1, 1);
        carYearField = new JSpinner(yearModel);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(carYearField, "#");
        carYearField.setEditor(editor);
        styleSpinner(carYearField);
        Utils.addMouseScrollToSpinner(carYearField, null);

        // Hide Arrows for autocomplete fields
        styleComboBox(nameField, false);
        styleComboBox(phoneField, false);
        styleComboBox(carLicensePlateField, false);
        styleComboBox(carModelField, false);

        // --- Autocomplete Setup ---
        setupAutocomplete();

        // --- Other Inputs ---
        repairsField = createTextArea();
        partsUsedField = createTextArea();
        observationsField = createTextArea();

        String[] carBrands = {"Audi", "BMW", "Chevrolet", "Citroen", "Dacia", "Fiat", "Ford", "Honda",
                "Hyundai", "Jeep", "Kia", "Land Rover", "Mazda", "Mercedes", "Mitsubishi",
                "Mini", "Nissan", "Opel", "Peugeot", "Renault", "Seat", "Skoda", "Suzuki",
                "Tesla", "Toyota", "Volkswagen", "Volvo"};
        carBrandBox = new JComboBox<>(carBrands);
        carBrandBox.setEditable(true);
        styleComboBox(carBrandBox, true);
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

    private void setupAutocomplete() {
        AutoCompletion.enable(nameField, text -> {
            try {
                List<Client> clients = DatabaseHelper.getClientsByName(text);
                List<String> names = new ArrayList<>();
                for (Client client : clients) names.add(client.getClientName());
                return names;
            } catch (SQLException ex) { return new ArrayList<>(); }
        });
        nameField.setEditable(true);

        AutoCompletion.enable(phoneField, text -> {
            try {
                String cleanPhone = Utils.normalizePhone(text);
                List<Client> clients = DatabaseHelper.getClientsByPhone(cleanPhone);
                List<String> phones = new ArrayList<>();
                for (Client client : clients) phones.add(client.getClientPhone());
                return phones;
            } catch (SQLException ex) { return new ArrayList<>(); }
        });
        phoneField.setEditable(true);

        AutoCompletion.enable(carLicensePlateField, text -> {
            try {
                List<Car> cars = DatabaseHelper.getCarDetailsByPlate(text);
                List<String> plates = new ArrayList<>();
                for (Car car : cars) plates.add(car.getLicensePlate());
                return plates;
            } catch (SQLException ex) { return new ArrayList<>(); }
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
                    if (!models.contains(car.getCarModel())) models.add(car.getCarModel());
                }
                return models;
            } catch (SQLException ex) { return new ArrayList<>(); }
        });
        carModelField.setEditable(true);
    }

    private void setupTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        model.setCalendarField(Calendar.MINUTE);
        timeSpinner = new JSpinner(model);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setPreferredSize(new Dimension(80, 25));

        styleSpinner(timeSpinner);

        final Date[] trackingDate = { (Date) timeSpinner.getValue() };

        timeSpinner.addChangeListener(e -> {
            Date newTime = (Date) timeSpinner.getValue();
            Date currentSelectedDate = dateChooser.getDate();

            if (currentSelectedDate != null) {
                Calendar oldCal = Calendar.getInstance(); oldCal.setTime(trackingDate[0]);
                Calendar newCal = Calendar.getInstance(); newCal.setTime(newTime);

                int oldH = oldCal.get(Calendar.HOUR_OF_DAY);
                int newH = newCal.get(Calendar.HOUR_OF_DAY);

                if (oldH == 23 && newH == 0) {
                    Calendar dateCal = Calendar.getInstance(); dateCal.setTime(currentSelectedDate);
                    dateCal.add(Calendar.DAY_OF_MONTH, 1);
                    dateChooser.setDate(dateCal.getTime());
                } else if (oldH == 0 && newH == 23) {
                    Calendar dateCal = Calendar.getInstance(); dateCal.setTime(currentSelectedDate);
                    dateCal.add(Calendar.DAY_OF_MONTH, -1);
                    dateChooser.setDate(dateCal.getTime());
                }
            }
            trackingDate[0] = newTime;
        });

        Utils.addMouseScrollToSpinner(timeSpinner, null);

        JFormattedTextField timeField = ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField();
        timeField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) return;
                String text = timeField.getText();
                if (timeField.getSelectedText() != null) return;
                if (text.length() == 2 && !text.contains(":")) {
                    e.consume();
                    timeField.setText(text + ":" + c);
                }
            }
        });

        JTextField dateField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        dateField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) return;
                String text = dateField.getText();
                if (dateField.getSelectedText() != null) return;
                if ((text.length() == 2 || text.length() == 5) && !text.endsWith("/")) {
                    e.consume();
                    dateField.setText(text + "/" + c);
                }
            }
        });
    }

    private void setupAutoFillListeners() {
        // 1. Phone Selected -> Fill Name
        phoneField.addActionListener(e -> {
            if (!phoneField.isPopupVisible()) {
                String selectedPhone = (String) phoneField.getEditor().getItem();
                if (selectedPhone != null && !selectedPhone.isEmpty()) {
                    try {
                        // Normalize first to ensure matching
                        String cleanPhone = Utils.normalizePhone(selectedPhone);
                        java.util.List<Client> clients = DatabaseHelper.getClientsByPhone(cleanPhone);
                        if (!clients.isEmpty()) {
                            // Avoid overwriting if name is already set?
                            nameField.setSelectedItem(clients.get(0).getClientName());
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // 2. Name Selected -> Fill Phone
        nameField.addActionListener(e -> {
            if (!nameField.isPopupVisible()) {
                String selectedName = (String) nameField.getEditor().getItem();
                String currentPhone = (String) phoneField.getEditor().getItem();

                // Only fill phone if it's currently empty (to prevent overwriting a valid number if names are duplicates)
                if (selectedName != null && !selectedName.isEmpty() && (currentPhone == null || currentPhone.isEmpty())) {
                    try {
                        java.util.List<Client> clients = DatabaseHelper.getClientsByName(selectedName);
                        if (!clients.isEmpty()) {
                            phoneField.setSelectedItem(clients.get(0).getClientPhone());
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // 3. License Plate Selected -> Fill Car Details
        carLicensePlateField.addActionListener(e -> {
            if (!carLicensePlateField.isPopupVisible()) {
                String selectedPlate = (String) carLicensePlateField.getEditor().getItem();
                if (selectedPlate != null && !selectedPlate.isEmpty()) {
                    try {
                        java.util.List<Car> cars = DatabaseHelper.getCarDetailsByPlate(selectedPlate);
                        if (!cars.isEmpty()) {
                            Car car = cars.get(0);

                            // Fill fields
                            carBrandBox.setSelectedItem(car.getCarBrand());
                            carModelField.setSelectedItem(car.getCarModel());
                            carYearField.setValue(car.getYear());

                            // Handle Photo
                            String path = car.getPhotoPath();
                            if (path != null && !path.isEmpty()) {
                                currentPhotoPath = path;
                                photoLabel.setText(new java.io.File(path).getName());
                                photoLabel.setForeground(new Color(46, 204, 113));
                                viewPhotoButton.setEnabled(true);
                            } else {
                                currentPhotoPath = "";
                                photoLabel.setText(LanguageHelper.getString("msg.no_photo"));
                                photoLabel.setForeground(Color.BLACK);
                                viewPhotoButton.setEnabled(false);
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    // =================================================================================================================
    //  SECTION 5: PRIVATE UI HELPERS
    //  Factories for styling components.
    // =================================================================================================================

    private JPanel createFieldGroup(JComponent label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(Theme.WHITE);
        if (label instanceof JLabel) ((JLabel) label).setHorizontalAlignment(SwingConstants.CENTER);
        p.add(label, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
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

    private Border createStyledBorder(String titleKey) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Theme.BLACK, 1),
                        LanguageHelper.getString(titleKey),
                        0, 0, new Font("SansSerif", Font.BOLD, 16), Theme.BLACK),
                BorderFactory.createEmptyBorder(15, 35, 15, 35)
        );
    }

    private void styleComboBox(JComboBox<String> box, boolean showArrow) {
        if (showArrow) {
            box.setUI(new ModernComboBoxUI());
        } else {
            box.setUI(new ModernComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton btn = new JButton();
                    btn.setPreferredSize(new Dimension(0, 0));
                    btn.setVisible(false);
                    return btn;
                }
            });
        }
        box.setEditable(true);
        box.setFont(INPUT_FONT);
        JTextField editor = (JTextField) box.getEditor().getEditorComponent();
        editor.setFont(INPUT_FONT);
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        box.setBackground(Color.WHITE);
        editor.setBackground(Color.WHITE);
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setUI(new ModernSpinnerUI());
        spinner.setFont(INPUT_FONT);
        if (spinner.getModel() instanceof SpinnerNumberModel) {
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
            spinner.setEditor(editor);
        }
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setFont(INPUT_FONT);
            textField.setHorizontalAlignment(SwingConstants.CENTER);
            textField.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
    }

    // =================================================================================================================
    //  SECTION 6: LANGUAGE TRANSLATION
    //  Updating labels and buttons text based on preferred language
    // =================================================================================================================

    public void updateText() {
        // Update Labels
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

        // Update Buttons
        addButton.setText(LanguageHelper.getString("btn.add"));
        updateButton.setText(LanguageHelper.getString("btn.update"));
        deleteButton.setText(LanguageHelper.getString("btn.delete"));
        clearButton.setText(LanguageHelper.getString("btn.clear"));
        selectPhotoButton.setText(LanguageHelper.getString("btn.select_photo"));
        viewPhotoButton.setText(LanguageHelper.getString("btn.view_photo"));

        // Update Photo Label if empty
        if (currentPhotoPath.isEmpty()) {
            photoLabel.setText(LanguageHelper.getString("msg.no_photo"));
        }
    }

    public void setPhoto(java.io.File file) {
        if (file != null) {
            this.currentPhotoPath = file.getAbsolutePath();
            this.photoLabel.setText(truncate(file.getName()));
            this.photoLabel.setToolTipText(file.getName());
            this.photoLabel.setForeground(new Color(46, 204, 113));
            this.viewPhotoButton.setEnabled(true);
        }
    }

    private String truncate(String text) {
        if (text == null) return "";
        if (text.length() > 20) {
            return text.substring(0, 17) + "...";
        }
        return text;
    }
}
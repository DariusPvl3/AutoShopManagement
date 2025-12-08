package com.autoshop.app.view;

import com.autoshop.app.component.*;
import com.autoshop.app.model.Appointment;
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

public class AppointmentView extends JPanel {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AppointmentView.class.getName());
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 12);
    private static final Font INPUT_FONT = new Font("SansSerif", Font.PLAIN, 16);

    private final ArrayList<Appointment> appointmentList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private DefaultTableModel tableModel;
    private JTable table;
    private JPanel clientPanel, carPanel, appointmentPanel;

    private JTextField nameField, phoneField, carLicensePlateField, carModelField, carYearField, problemDescriptionField;
    private JComboBox<String> carBrandBox;
    private JButton selectPhotoButton, addButton, clearButton, updateButton, deleteButton;
    private String currentPhotoPath = "";
    private JLabel photoLabel;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;

    private JLabel nameLabel, phoneLabel, plateLabel, brandLabel, modelLabel, yearLabel, photoTitleLabel, dateLabel, problemLabel, atLabel;

    public AppointmentView() {
        setLayout(new BorderLayout());
        initializeComponents();
        add(createInputPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

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
        atLabel = createLabel();

        photoLabel = new JLabel("No Photo Selected");
        photoLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
    }

    private void initInputs() {
        nameField = createTextField();
        phoneField = createTextField();
        carLicensePlateField = createTextField();
        carModelField = createTextField();
        problemDescriptionField = createTextField();
        carYearField = createTextField();

        String[] carBrands = {"Audi", "BMW", "Chevrolet", "Citroen", "Dacia", "Fiat", "Ford", "Honda",
                "Hyundai", "Jeep", "Kia", "Land Rover", "Mazda", "Mercedes", "Mitsubishi",
                "Mini", "Nissan", "Opel", "Peugeot", "Renault", "Seat", "Skoda", "Suzuki",
                "Tesla", "Toyota", "Volkswagen", "Volvo"};
        carBrandBox = new JComboBox<>(carBrands);
        carBrandBox.setEditable(true);
        carBrandBox.setFont(INPUT_FONT);
        AutoCompletion.enable(carBrandBox);

        selectPhotoButton = new JButton("Select Photo");

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
        timeSpinner.setFont(INPUT_FONT);

        JFormattedTextField tf = ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField();
        tf.setEditable(true);
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setFont(INPUT_FONT);

        timeSpinner.setValue(new Date());
        Utils.addMouseScrollToSpinner(timeSpinner);
    }

    private void initButtons() {
        addButton = new RoundedButton("Add Appointment");
        ButtonStyler.apply(addButton, Theme.RED);

        clearButton = new RoundedButton("Clear");
        ButtonStyler.apply(clearButton, Theme.GRAY);

        updateButton = new RoundedButton("Update Appointment");
        ButtonStyler.apply(updateButton, Theme.BLACK);
        updateButton.setEnabled(false);

        deleteButton = new RoundedButton("Delete Appointment");
        ButtonStyler.apply(deleteButton, Theme.RED);
    }

    private void initTable() {
        String[] columns = {"Client Name", "Phone", "License Plate", "Brand", "Model", "Year", "Date", "Description", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = SwingTableStyler.create(tableModel, 8);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());
    }

    // --- VIEW CONSTRUCTION ---

    private JPanel createInputPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        mainPanel.setBackground(Theme.BLACK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Client Card
        clientPanel = setupCardPanel();
        addLabeledComponent(clientPanel, nameLabel, nameField);
        clientPanel.add(Box.createVerticalStrut(15));
        addLabeledComponent(clientPanel, phoneLabel, phoneField);
        clientPanel.add(Box.createVerticalGlue());

        // 2. Car Card
        carPanel = setupCardPanel();
        addLabeledComponent(carPanel, brandLabel, carBrandBox);
        carPanel.add(Box.createVerticalStrut(15));
        addLabeledComponent(carPanel, modelLabel, carModelField);
        carPanel.add(Box.createVerticalStrut(15));
        addLabeledComponent(carPanel, plateLabel, carLicensePlateField);
        carPanel.add(Box.createVerticalStrut(15));
        addLabeledComponent(carPanel, yearLabel, carYearField);
        carPanel.add(Box.createVerticalStrut(15));

        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        photoPanel.setBackground(Theme.WHITE);
        photoPanel.add(selectPhotoButton);
        photoPanel.add(photoLabel);
        photoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        addCentered(carPanel, photoTitleLabel);
        carPanel.add(Box.createVerticalStrut(5));
        addCentered(carPanel, photoPanel);
        carPanel.add(Box.createVerticalGlue());

        // 3. Appointment Card
        appointmentPanel = setupCardPanel();
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        dateTimePanel.setBackground(Theme.WHITE);
        dateTimePanel.add(dateChooser);
        dateTimePanel.add(atLabel);
        dateTimePanel.add(timeSpinner);
        dateTimePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        dateTimePanel.setPreferredSize(new Dimension(300, 35));

        addCentered(appointmentPanel, dateLabel);
        appointmentPanel.add(Box.createVerticalStrut(5));
        addCentered(appointmentPanel, dateTimePanel);
        appointmentPanel.add(Box.createVerticalStrut(15));
        addLabeledComponent(appointmentPanel, problemLabel, problemDescriptionField);
        appointmentPanel.add(Box.createVerticalStrut(30));

        JPanel buttonContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonContainer.setBackground(Theme.WHITE);
        buttonContainer.add(addButton);
        buttonContainer.add(clearButton);
        buttonContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addCentered(appointmentPanel, buttonContainer);
        appointmentPanel.add(Box.createVerticalGlue());

        mainPanel.add(clientPanel);
        mainPanel.add(carPanel);
        mainPanel.add(appointmentPanel);
        return mainPanel;
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

    private JPanel setupCardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.WHITE);
        panel.setOpaque(true);
        panel.add(Box.createVerticalStrut(15));
        return panel;
    }

    // --- ACTIONS (ADD, UPDATE, DELETE, CLEAR) ---

    private void addAppointment() {
        if (validateAndFormatInput()) return;

        String name = nameField.getText();
        String phone = phoneField.getText();
        String plate = carLicensePlateField.getText();
        String brand = (String) carBrandBox.getSelectedItem();
        String model = carModelField.getText().replace(";", ",");
        String desc = problemDescriptionField.getText().replace(";", ",");
        Date finalDate = Utils.combineDateAndTime(dateChooser.getDate(), (Date) timeSpinner.getValue());

        int year = 0;
        try { year = Integer.parseInt(carYearField.getText().trim()); } catch (NumberFormatException ignored) {}

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

        Appointment newAppt = new Appointment(name, phone, plate, brand, model, year, currentPhotoPath, finalDate, desc);

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
        appt.setClientName(nameField.getText());
        appt.setClientPhone(phoneField.getText());
        appt.setCarLicensePlate(carLicensePlateField.getText());
        appt.setCarBrand((String) carBrandBox.getSelectedItem());
        appt.setCarModel(carModelField.getText());
        appt.setDate(Utils.combineDateAndTime(dateChooser.getDate(), (Date) timeSpinner.getValue()));
        appt.setProblemDescription(problemDescriptionField.getText());
        appt.setCarPhotoPath(currentPhotoPath);

        try { appt.setCarYear(Integer.parseInt(carYearField.getText().trim())); } catch(Exception ignored) {}

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

        // REPLACED: Confirm Dialog
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
        nameField.setText("");
        phoneField.setText("");
        carLicensePlateField.setText("");
        carBrandBox.setSelectedIndex(-1);
        carModelField.setText("");
        carYearField.setText("");
        currentPhotoPath = "";
        photoLabel.setText(LanguageHelper.getString("msg.no_photo"));
        problemDescriptionField.setText("");
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
                    dateFormat.format(a.getDate()), a.getProblemDescription(), a.getStatus()
            });
        }
    }

    private boolean validateAndFormatInput() {
        nameField.setText(Utils.toTitleCase(nameField.getText().trim()));
        carLicensePlateField.setText(Utils.formatPlate(carLicensePlateField.getText()));
        carModelField.setText(Utils.toTitleCase(carModelField.getText().trim()));

        String rawBrand = (String) carBrandBox.getSelectedItem();
        if (rawBrand != null && !rawBrand.isEmpty()) {
            carBrandBox.getEditor().setItem(Utils.toTitleCase(rawBrand));
        }

        if (nameField.getText().isEmpty() || dateChooser.getDate() == null) {
            showError("msg.req.name_date");
            return true;
        }
        if (!Utils.isValidPhone(phoneField.getText())) {
            showError("msg.err.phone");
            return true;
        }
        if (!carLicensePlateField.getText().matches("^[A-Z]{1,2}-[0-9]{2,3}-[A-Z]{3}$")) {
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

        selectPhotoButton.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                currentPhotoPath = file.getAbsolutePath();
                photoLabel.setText(file.getName());
                photoLabel.setForeground(new Color(46, 204, 113));
            }
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

        nameField.setText(appt.getClientName());
        phoneField.setText(appt.getClientPhone());
        carLicensePlateField.setText(appt.getCarLicensePlate());
        carBrandBox.setSelectedItem(appt.getCarBrand());
        carModelField.setText(appt.getCarModel());
        carYearField.setText(String.valueOf(appt.getCarYear()));
        currentPhotoPath = appt.getCarPhotoPath();

        photoLabel.setText((currentPhotoPath != null && !currentPhotoPath.isEmpty())
                ? new File(currentPhotoPath).getName() : LanguageHelper.getString("msg.no_photo"));

        problemDescriptionField.setText(appt.getProblemDescription());
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

        nameLabel.setText(LanguageHelper.getString("lbl.name"));
        phoneLabel.setText(LanguageHelper.getString("lbl.phone"));
        plateLabel.setText(LanguageHelper.getString("lbl.plate"));
        brandLabel.setText(LanguageHelper.getString("lbl.brand"));
        modelLabel.setText(LanguageHelper.getString("lbl.model"));
        yearLabel.setText(LanguageHelper.getString("lbl.year"));
        photoTitleLabel.setText(LanguageHelper.getString("lbl.photo"));
        dateLabel.setText(LanguageHelper.getString("lbl.date"));
        problemLabel.setText(LanguageHelper.getString("lbl.problem"));
        atLabel.setText(LanguageHelper.getString("lbl.at"));

        if(currentPhotoPath.isEmpty()) photoLabel.setText(LanguageHelper.getString("msg.no_photo"));

        if (tableModel != null) {
            String[] cols = {
                    LanguageHelper.getString("col.client"), LanguageHelper.getString("col.phone"),
                    LanguageHelper.getString("col.plate"), LanguageHelper.getString("col.brand"),
                    LanguageHelper.getString("col.model"), LanguageHelper.getString("col.year"),
                    LanguageHelper.getString("col.date"), LanguageHelper.getString("col.problem"),
                    LanguageHelper.getString("col.status")
            };
            tableModel.setColumnIdentifiers(cols);
            table.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());
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
}
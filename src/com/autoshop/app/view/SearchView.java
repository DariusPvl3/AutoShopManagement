package com.autoshop.app.view;

import com.autoshop.app.component.*;
import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;

public class SearchView extends JPanel {

    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 12);
    private static final Font INPUT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    private JLabel keywordLabel, fromLabel, toLabel, statusLabel;
    private JTextField searchField;
    private JDateChooser dateFrom, dateTo;
    private JComboBox<Object> statusFilterBox;
    private JButton searchButton, resetButton;

    private JTable resultsTable;
    private DefaultTableModel tableModel;

    private final ArrayList<Appointment> resultsList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private Consumer<Integer> onJumpRequest;

    public SearchView() {
        setLayout(new BorderLayout());

        // 1. Initialize Components
        initComponents();

        // 2. Build Layout
        add(createFilterPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        // 3. Setup Logic
        setupListeners();
        setUpShortcuts();

        LanguageHelper.addListener(this::updateText);
        updateText();
    }

    // --- INITIALIZATION ---

    private void initComponents() {
        keywordLabel = createLabel();
        fromLabel = createLabel();
        toLabel = createLabel();
        statusLabel = createLabel();

        searchField = new JTextField(15);
        searchField.setFont(INPUT_FONT);

        statusFilterBox = new JComboBox<>();
        statusFilterBox.setRenderer(new StatusListRenderer()); // Ensure this class exists or defined below
        statusFilterBox.setFont(INPUT_FONT);
        populateStatusBox();

        dateFrom = createDateChooser();
        dateTo = createDateChooser();

        searchButton = new RoundedButton("Search");
        ButtonStyler.apply(searchButton, Theme.RED);

        resetButton = new RoundedButton("Reset");
        ButtonStyler.apply(resetButton, new Color(149, 165, 166));
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(Theme.BLACK);

        panel.add(keywordLabel);
        panel.add(searchField);
        panel.add(statusLabel);
        panel.add(statusFilterBox);
        panel.add(fromLabel);
        panel.add(dateFrom);
        panel.add(toLabel);
        panel.add(dateTo);
        panel.add(searchButton);
        panel.add(resetButton);

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Client", "Phone", "Plate", "Brand", "Model", "Year", "Date", "Problem", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        resultsTable = SwingTableStyler.create(tableModel, 8);
        resultsTable.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());

        return new JScrollPane(resultsTable);
    }

    // --- LOGIC ---

    private void performSearch() {
        String keyword = searchField.getText().trim();
        Date from = dateFrom.getDate();
        Date to = dateTo.getDate();

        Object selected = statusFilterBox.getSelectedItem();
        AppointmentStatus status = (selected instanceof AppointmentStatus) ? (AppointmentStatus) selected : null;

        try {
            resultsList.clear();
            resultsList.addAll(DatabaseHelper.searchAppointments(keyword, status, from, to));
            refreshTable();

            if (resultsList.isEmpty()) {
                JOptionPane.showMessageDialog(this, LanguageHelper.getString("msg.err.not_found"),
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Search Error: " + e.getMessage());
        }
    }

    private void resetSearch() {
        searchField.setText("");
        statusFilterBox.setSelectedIndex(0);
        dateFrom.setDate(null);
        dateTo.setDate(null);
        resultsList.clear();
        refreshTable();
    }

    public void searchByDate(Date date) {
        searchField.setText("");
        dateFrom.setDate(date);
        dateTo.setDate(date);
        performSearch();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Appointment a : resultsList) {
            tableModel.addRow(new Object[]{
                    a.getClientName(), a.getClientPhone(), a.getCarLicensePlate(),
                    a.getCarBrand(), a.getCarModel(), a.getCarYear(),
                    dateFormat.format(a.getDate()), a.getProblemDescription(), a.getStatus()
            });
        }
    }

    // --- LISTENERS ---

    private void setupListeners() {
        searchButton.addActionListener(_ -> performSearch());
        searchField.addActionListener(_ -> performSearch());
        resetButton.addActionListener(_ -> resetSearch());

        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = resultsTable.getSelectedRow();
                    if (row != -1 && onJumpRequest != null) {
                        onJumpRequest.accept(resultsList.get(row).getAppointmentID());
                    }
                }
            }
        });
    }

    private void setUpShortcuts() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "search");
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK), "reset");

        actionMap.put("search", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { if(searchButton.isEnabled()) searchButton.doClick(); }
        });
        actionMap.put("reset", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { if(resetButton.isEnabled()) resetButton.doClick(); }
        });
    }

    // --- HELPERS ---

    private void updateText() {
        keywordLabel.setText(LanguageHelper.getString("lbl.keyword"));
        fromLabel.setText(LanguageHelper.getString("lbl.from"));
        toLabel.setText(LanguageHelper.getString("lbl.to"));
        statusLabel.setText(LanguageHelper.getString("col.status"));
        searchButton.setText(LanguageHelper.getString("btn.search"));
        resetButton.setText(LanguageHelper.getString("btn.reset"));

        // Preserve selection while updating combo box language
        Object selection = statusFilterBox.getSelectedItem();
        populateStatusBox();
        if (selection instanceof AppointmentStatus) statusFilterBox.setSelectedItem(selection);
        else statusFilterBox.setSelectedIndex(0);

        if (tableModel != null) {
            String[] cols = {
                    LanguageHelper.getString("col.client"), LanguageHelper.getString("col.phone"),
                    LanguageHelper.getString("col.plate"), LanguageHelper.getString("col.brand"),
                    LanguageHelper.getString("col.model"), LanguageHelper.getString("col.year"),
                    LanguageHelper.getString("col.date"), LanguageHelper.getString("col.problem"),
                    LanguageHelper.getString("col.status")
            };
            tableModel.setColumnIdentifiers(cols);
            resultsTable.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());
            resultsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        }
    }

    private void populateStatusBox() {
        statusFilterBox.removeAllItems();
        statusFilterBox.addItem(LanguageHelper.getString("lbl.all_statuses"));
        for (AppointmentStatus s : AppointmentStatus.values()) statusFilterBox.addItem(s);
    }

    private JLabel createLabel() {
        JLabel l = new JLabel();
        l.setFont(LABEL_FONT);
        l.setForeground(Theme.WHITE);
        return l;
    }

    private JDateChooser createDateChooser() {
        JDateChooser dc = new JDateChooser();
        dc.setPreferredSize(new Dimension(130, 30));
        CalendarCustomizer.styleDateChooser(dc);
        return dc;
    }

    public void setOnJumpRequest(Consumer<Integer> callback) { this.onJumpRequest = callback; }

    // Internal Renderer for Status ComboBox
    private static class StatusListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof AppointmentStatus) {
                AppointmentStatus status = (AppointmentStatus) value;
                setText(LanguageHelper.getString(status.getLangKey()));
                setForeground(status.getColor());
            }
            return this;
        }
    }
}
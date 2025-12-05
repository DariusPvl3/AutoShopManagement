package com.autoshop.app;

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

    private JLabel keywordLabel, fromLabel, toLabel, statusLabel;
    private final JTextField searchField;
    private final JDateChooser dateFrom;
    private final JDateChooser dateTo;
    private final JTable resultsTable;
    private final JComboBox<Object> statusFilterBox;
    private JButton searchButton;
    private JButton resetButton;
    private final DefaultTableModel tableModel;
    private final ArrayList<Appointment> resultsList = new ArrayList<>();
    private Consumer<Integer> onJumpRequest;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public SearchView() {
        setLayout(new BorderLayout());
        keywordLabel = new JLabel();
        fromLabel = new JLabel();
        toLabel = new JLabel();
        statusLabel = new JLabel();
        keywordLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        fromLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        toLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        // --- 1. FILTERS PANEL (North) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        searchField = new JTextField(15);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        statusFilterBox = new JComboBox<>();
        statusFilterBox.setRenderer(new StatusListRenderer());
        statusFilterBox.addItem("All Statuses");
        for (AppointmentStatus s : AppointmentStatus.values()) {
            statusFilterBox.addItem(s);
        }
        statusFilterBox.setFont(new Font("SansSerif", Font.PLAIN, 14));

        dateFrom = new JDateChooser();
        dateFrom.setPreferredSize(new Dimension(130, 30));
        CalendarCustomizer.styleDateChooser(dateFrom);

        dateTo = new JDateChooser();
        dateTo.setPreferredSize(new Dimension(130, 30));
        CalendarCustomizer.styleDateChooser(dateTo);

        searchButton = new RoundedButton("Search");
        ButtonStyler.apply(searchButton, new Color(52, 152, 219)); // Blue

        resetButton = new RoundedButton("Reset");
        ButtonStyler.apply(resetButton, new Color(149, 165, 166)); // Grey

        filterPanel.add(keywordLabel);
        filterPanel.add(searchField);
        statusLabel.setText("Status");
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilterBox);
        filterPanel.add(fromLabel);
        filterPanel.add(dateFrom);
        filterPanel.add(toLabel);
        filterPanel.add(dateTo);
        filterPanel.add(searchButton);
        filterPanel.add(resetButton);

        add(filterPanel, BorderLayout.NORTH);

        // --- 2. RESULTS TABLE (Center) ---
        String[] columns = {"Client", "Phone", "Plate", "Brand", "Model", "Year", "Date", "Problem", "Status"};
        tableModel = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultsTable.setRowHeight(25);
        resultsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        resultsTable.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());

        // Double-Click to Jump
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = resultsTable.getSelectedRow();
                    if (row != -1 && onJumpRequest != null) {
                        int id = resultsList.get(row).getAppointmentID();
                        onJumpRequest.accept(id);
                    }
                }
            }
        });

        add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        // --- 3. LISTENERS ---
        searchButton.addActionListener(_ -> performSearch());

        searchField.addActionListener(_ -> performSearch());

        resetButton.addActionListener(_ -> {
            searchField.setText("");
            statusFilterBox.setSelectedIndex(0);
            dateFrom.setDate(null);
            dateTo.setDate(null);
            resultsList.clear();
            refreshTable();
        });

        setUpShortcuts();
        LanguageHelper.addListener(this::updateText);
        updateText();
    }

    public void setOnJumpRequest(Consumer<Integer> callback) {
        this.onJumpRequest = callback;
    }

    public void searchByDate(Date date) {
        searchField.setText("");
        dateFrom.setDate(date);
        dateTo.setDate(date); // Same day range
        performSearch();
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        Date from = dateFrom.getDate();
        Date to = dateTo.getDate();

        Object selected = statusFilterBox.getSelectedItem();
        AppointmentStatus status = null;
        if (selected instanceof AppointmentStatus) {
            status = (AppointmentStatus) selected;
        }

        try {
            resultsList.clear();
            resultsList.addAll(DatabaseHelper.searchAppointments(keyword, status, from, to));
            refreshTable();

            if (resultsList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No appointments found matching your criteria.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Search Error: " + e.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Appointment appointment : resultsList) {
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

    private void setUpShortcuts(){
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        // 1. Define Key Strokes
        KeyStroke searchKey = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0);
        KeyStroke clearKey = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK);

        // 2. Map Keys to Action Names
        inputMap.put(searchKey, "searchAppointments");
        inputMap.put(clearKey, "clearFields");

        // 3. Map Action to actual logic
        actionMap.put("searchAppointments", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e){
                if(searchButton.isEnabled()) searchButton.doClick();
            }
        });

        actionMap.put("clearFields", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e){
                if(resetButton.isEnabled()) resetButton.doClick();
            }
        });
    }

    private void updateText() {
        keywordLabel.setText(LanguageHelper.getString("lbl.keyword"));
        fromLabel.setText(LanguageHelper.getString("lbl.from"));
        toLabel.setText(LanguageHelper.getString("lbl.to"));
        searchButton.setText(LanguageHelper.getString("btn.search"));
        resetButton.setText(LanguageHelper.getString("btn.reset"));

        Object currentSelection = statusFilterBox.getSelectedItem();
        statusFilterBox.removeAllItems();
        statusFilterBox.addItem(LanguageHelper.getString("lbl.all_statuses"));
        for(AppointmentStatus status : AppointmentStatus.values()){
            statusFilterBox.addItem(status);
        }
        if(currentSelection instanceof AppointmentStatus){
            statusFilterBox.setSelectedItem(currentSelection);
        } else {
            statusFilterBox.setSelectedIndex(0);
        }

        // Table Headers
        if (tableModel != null) {
            String[] cols = {
                    LanguageHelper.getString("col.client"),
                    LanguageHelper.getString("col.phone"),
                    LanguageHelper.getString("col.plate"),
                    LanguageHelper.getString("col.brand"),
                    LanguageHelper.getString("col.model"),
                    LanguageHelper.getString("col.year"),
                    LanguageHelper.getString("col.date"),
                    LanguageHelper.getString("col.problem"),
                    LanguageHelper.getString("col.status")
            };
            tableModel.setColumnIdentifiers(cols);
            resultsTable.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());
            resultsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        }
    }
}
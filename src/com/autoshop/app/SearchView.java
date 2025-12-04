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

    private JTextField searchField;
    private JDateChooser dateFrom;
    private JDateChooser dateTo;
    private JTable resultsTable;
    private JComboBox<Object> statusFilterBox;
    private DefaultTableModel tableModel;
    private ArrayList<Appointment> resultsList = new ArrayList<>();
    private Consumer<Integer> onJumpRequest;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public SearchView() {
        setLayout(new BorderLayout());

        // --- 1. FILTERS PANEL (North) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        searchField = new JTextField(15);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        statusFilterBox = new JComboBox<>();
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

        JButton searchBtn = new RoundedButton("Search");
        ButtonStyler.apply(searchBtn, new Color(52, 152, 219)); // Blue

        JButton resetBtn = new RoundedButton("Reset");
        ButtonStyler.apply(resetBtn, new Color(149, 165, 166)); // Grey

        filterPanel.add(new JLabel("Keyword:"));
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilterBox);
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(dateFrom);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(dateTo);
        filterPanel.add(searchBtn);
        filterPanel.add(resetBtn);

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
        searchBtn.addActionListener(e -> performSearch());

        searchField.addActionListener(e -> performSearch());

        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterBox.setSelectedIndex(0);
            dateFrom.setDate(null);
            dateTo.setDate(null);
            resultsList.clear();
            refreshTable();
        });
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
        for (Appointment appt : resultsList) {
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
    }
}
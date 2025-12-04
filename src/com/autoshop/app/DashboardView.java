package com.autoshop.app;

import com.toedter.calendar.JCalendar;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;

public class DashboardView extends JPanel {
    private ArrayList<Appointment> appointmentList = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JLabel appointmentsTodayLabel = new JLabel();
    private JLabel activeLabel = new JLabel();
    private JTable agendaTable = new JTable();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Callbacks
    private Consumer<Integer> onJumpRequest;      // Jump to Edit
    private Consumer<Date> onSearchDateRequest;   // Jump to Search
    private Consumer<Date> onCreateRequest;       // Jump to Create

    // Cache
    private java.util.List<Appointment> calendarCache = new ArrayList<>();
    private JCalendar calendar;

    public DashboardView() {
        this.setLayout(new BorderLayout(15, 15));
        this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- 1. TOP STATS ---
        JPanel statsPanel = new JPanel(new BorderLayout());
        JPanel labelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        appointmentsTodayLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        activeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        labelsPanel.add(appointmentsTodayLabel);
        labelsPanel.add(Box.createHorizontalStrut(30));
        labelsPanel.add(activeLabel);
        statsPanel.add(labelsPanel, BorderLayout.WEST);
        statsPanel.add(new DigitalClock(), BorderLayout.EAST);
        this.add(statsPanel, BorderLayout.NORTH);

        // --- 2. CONTENT AREA ---
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // --- SECTION A: AGENDA TABLE ---
        JPanel tableSection = new JPanel(new BorderLayout(0, 10));
        JLabel tableHeader = new JLabel("Appointments Today");
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 20));
        tableHeader.setForeground(new Color(80, 80, 80));
        tableSection.add(tableHeader, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane();
        agendaTable.setFont(new Font("SansSerif", Font.PLAIN, 16));
        agendaTable.setRowHeight(35);
        agendaTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));

        String[] columns = {"Client Name", "Phone", "License Plate", "Brand", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        agendaTable.setModel(tableModel);
        agendaTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        // Table Listeners
        StatusMenuHelper.attach(agendaTable, appointmentList, this::loadDataFromDB, this);
        agendaTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = agendaTable.getSelectedRow();
                    if (row != -1 && onJumpRequest != null) {
                        int id = appointmentList.get(row).getAppointmentID();
                        onJumpRequest.accept(id);
                    }
                }
            }
        });

        scrollPane.setViewportView(agendaTable);
        tableSection.add(scrollPane, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.70;
        contentPanel.add(tableSection, gbc);

        // --- SECTION B: CALENDAR ---
        JPanel calendarSection = new JPanel(new BorderLayout(0, 10));
        JLabel calHeader = new JLabel("Calendar");
        calHeader.setFont(new Font("SansSerif", Font.BOLD, 20));
        calHeader.setForeground(new Color(80, 80, 80));
        calendarSection.add(calHeader, BorderLayout.NORTH);

        calendar = new JCalendar();
        CalendarCustomizer.styleCalendar(calendar);

        attachClickMenu();

        // Repaint Listener (Selection Border)
        calendar.addPropertyChangeListener("calendar", e -> {
            SwingUtilities.invokeLater(() -> CalendarCustomizer.paintDates(calendar));
        });

        calendarSection.add(calendar, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.30;
        gbc.insets = new Insets(0, 20, 0, 0);
        contentPanel.add(calendarSection, gbc);

        this.add(contentPanel, BorderLayout.CENTER);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { loadDataFromDB(); }
        });
        loadDataFromDB();
    }

    // --- CLICK MENU LOGIC ---
    private void attachClickMenu() {
        JPopupMenu calendarMenu = new JPopupMenu();

        // Item 1: View (Existing)
        JMenuItem viewDayItem = new JMenuItem("View Appointments");
        viewDayItem.setFont(new Font("SansSerif", Font.BOLD, 14));
        calendarMenu.add(viewDayItem);

        // Item 2: Create (New)
        JMenuItem createItem = new JMenuItem("Create New Appointment");
        createItem.setFont(new Font("SansSerif", Font.BOLD, 14));
        // Add a little separator
        calendarMenu.add(new JSeparator());
        calendarMenu.add(createItem);

        // View Action
        viewDayItem.addActionListener(e -> {
            if (onSearchDateRequest != null) onSearchDateRequest.accept(calendar.getDate());
        });

        // Create Action
        createItem.addActionListener(e -> {
            if (onCreateRequest != null) onCreateRequest.accept(calendar.getDate());
        });

        // Mouse Listener (Simple Click)
        java.awt.event.MouseAdapter clickHandler = new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { handle(e); }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) { handle(e); }

            private void handle(java.awt.event.MouseEvent e) {
                // Handle Left or Right Click
                if (SwingUtilities.isLeftMouseButton(e) || e.isPopupTrigger()) {
                    Component c = e.getComponent();
                    if (c instanceof JButton) {
                        JButton dayBtn = (JButton) c;
                        String text = dayBtn.getText();
                        if (text == null || !text.matches("\\d+")) return;

                        // 1. Force Selection
                        int day = Integer.parseInt(text);
                        java.util.Calendar cal = calendar.getCalendar();
                        cal.set(java.util.Calendar.DAY_OF_MONTH, day);
                        calendar.setDate(cal.getTime());

                        // 2. Prepare Menu
                        int count = countAppointmentsOnDate(calendar.getDate());
                        if (count > 0) {
                            viewDayItem.setText("View " + count + " Appointment(s)");
                            viewDayItem.setForeground(new Color(0, 150, 0));
                        } else {
                            viewDayItem.setText("View Day (Empty)");
                            viewDayItem.setForeground(Color.GRAY);
                        }

                        // 3. Show Menu
                        calendarMenu.show(c, e.getX(), e.getY());
                    }
                }
            }
        };

        JPanel dayPanel = calendar.getDayChooser().getDayPanel();
        for (Component comp : dayPanel.getComponents()) {
            if (comp instanceof JButton) comp.addMouseListener(clickHandler);
        }
    }

    // --- SETTERS ---
    public void setOnCreateRequest(Consumer<Date> callback) { this.onCreateRequest = callback; }
    public void setOnJumpRequest(Consumer<Integer> callback) { this.onJumpRequest = callback; }
    public void setOnSearchDateRequest(Consumer<Date> callback) { this.onSearchDateRequest = callback; }

    // --- HELPERS ---
    private void loadDataFromDB() {
        try {
            DatabaseHelper.autoUpdateStatuses();
            appointmentList.clear();
            appointmentList.addAll(DatabaseHelper.getDashboardAppointments(new java.util.Date()));

            java.util.List<Appointment> activeList = DatabaseHelper.getActiveAppointments();
            long todayCount = appointmentList.stream().filter(a -> Utils.isToday(a.getDate())).count();

            appointmentsTodayLabel.setText("Appointments Today: " + todayCount);
            activeLabel.setText("Active: " + activeList.size());

            // Sorting logic
            appointmentList.sort((a, b) -> a.getDate().compareTo(b.getDate()));
            refreshTable();

            // Cache for popup menu counting
            this.calendarCache = DatabaseHelper.getAllAppointments();
            SwingUtilities.invokeLater(() -> CalendarCustomizer.paintDates(calendar));

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Appointment appt : appointmentList) {
            tableModel.addRow(new Object[]{
                    appt.getClientName(),
                    appt.getClientPhone(),
                    appt.getCarLicensePlate(),
                    appt.getCarBrand(),
                    dateFormat.format(appt.getDate()),
                    appt.getStatus()
            });
        }
    }

    private int countAppointmentsOnDate(java.util.Date date) {
        int count = 0;
        java.util.Calendar c1 = java.util.Calendar.getInstance(); c1.setTime(date);
        java.util.Calendar c2 = java.util.Calendar.getInstance();
        for(Appointment a : calendarCache) {
            c2.setTime(a.getDate());
            if(c1.get(java.util.Calendar.YEAR) == c2.get(java.util.Calendar.YEAR) &&
                    c1.get(java.util.Calendar.DAY_OF_YEAR) == c2.get(java.util.Calendar.DAY_OF_YEAR)) count++;
        }
        return count;
    }
}
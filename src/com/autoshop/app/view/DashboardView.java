package com.autoshop.app.view;

import com.autoshop.app.component.*;
import com.autoshop.app.model.Appointment;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;
import com.autoshop.app.util.Utils;
import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class DashboardView extends JPanel {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DashboardView.class.getName());
    private static final Font STAT_FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 20);
    private static final Color HEADER_COLOR = new Color(80, 80, 80);

    private final ArrayList<Appointment> appointmentList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private DefaultTableModel tableModel;
    private JTable agendaTable;
    private JCalendar calendar;
    private JLabel tableHeader, todayLabel, activeLabel;
    private Timer refreshTimer;

    // Callbacks
    private Consumer<Integer> onJumpRequest;
    private Consumer<Date> onSearchDateRequest;
    private Consumer<Date> onCreateRequest;

    // Cache
    private List<Appointment> calendarCache = new ArrayList<>();

    public DashboardView() {
        setLayout(new BorderLayout(15, 15));
        setBorder(null);
        setBackground(Theme.OFF_WHITE);

        add(createStatsPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);

        setupListeners();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { loadDataFromDB(); }
        });

        LanguageHelper.addListener(this::updateText);
        updateText();
        loadDataFromDB();

        // --- AUTO-REFRESH LOGIC ---
        // Refreshes every 30 seconds to update statuses visually
        refreshTimer = new Timer(30000, e -> {
            // For safety, the table will not refresh if the user is currently editing
            if (agendaTable != null && !agendaTable.isEditing()) {
                // We keep the current selection so the list doesn't jump
                int selectedRow = agendaTable.getSelectedRow();
                int selectedId = -1;
                if (selectedRow != -1 && selectedRow < appointmentList.size()) {
                    selectedId = appointmentList.get(selectedRow).getAppointmentID();
                }
                loadDataFromDB();
                // Restore selection
                if (selectedId != -1) {
                    for (int i = 0; i < appointmentList.size(); i++) {
                        if (appointmentList.get(i).getAppointmentID() == selectedId) {
                            agendaTable.setRowSelectionInterval(i, i);
                            break;
                        }
                    }
                }
            }
        });
        refreshTimer.start();
    }

    // --- UI CONSTRUCTION ---

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel labelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelsPanel.setOpaque(false);

        todayLabel = new JLabel();
        todayLabel.setFont(STAT_FONT);
        todayLabel.setForeground(Theme.TEXT_LIGHT);

        activeLabel = new JLabel();
        activeLabel.setFont(STAT_FONT);
        activeLabel.setForeground(Theme.TEXT_LIGHT);

        labelsPanel.add(todayLabel);
        labelsPanel.add(Box.createHorizontalStrut(30));
        labelsPanel.add(activeLabel);

        DigitalClock clock = new DigitalClock();
        clock.setForeground(Theme.TEXT_LIGHT);

        panel.add(labelsPanel, BorderLayout.WEST);
        panel.add(clock, BorderLayout.EAST);
        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.OFF_WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Table Section (70% width)
        gbc.gridx = 0;
        gbc.weightx = 0.70;
        panel.add(createTableSection(), gbc);

        // Calendar Section (30% width)
        gbc.gridx = 1;
        gbc.weightx = 0.30;
        gbc.insets = new Insets(0, 20, 0, 0);
        panel.add(createCalendarSection(), gbc);

        return panel;
    }

    private JPanel createTableSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.OFF_WHITE);

        tableHeader = new JLabel();
        tableHeader.setFont(HEADER_FONT);
        tableHeader.setForeground(HEADER_COLOR);
        panel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"Client Name", "Phone", "License Plate", "Brand", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        // Use Factory Method
        agendaTable = SwingTableStyler.create(tableModel, 5);
        agendaTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(agendaTable);
        scrollPane.getViewport().setBackground(Theme.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCalendarSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));

        JLabel calHeader = new JLabel("Calendar");
        calHeader.setFont(HEADER_FONT);
        calHeader.setForeground(HEADER_COLOR);
        panel.add(calHeader, BorderLayout.NORTH);

        calendar = new JCalendar();
        CalendarCustomizer.styleCalendar(calendar);
        attachClickMenu();

        panel.add(calendar, BorderLayout.CENTER);
        return panel;
    }

    // --- LISTENERS & LOGIC ---

    private void setupListeners() {
        StatusMenuHelper.attach(agendaTable, appointmentList, this::loadDataFromDB, this);

        agendaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = agendaTable.getSelectedRow();
                    if (row != -1 && onJumpRequest != null)
                        onJumpRequest.accept(appointmentList.get(row).getAppointmentID());
                }
            }
        });
    }

    private void attachClickMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem viewItem = new JMenuItem("View Appointments");
        JMenuItem createItem = new JMenuItem("Create New Appointment");

        viewItem.setFont(new Font("SansSerif", Font.BOLD, 14));
        createItem.setFont(new Font("SansSerif", Font.BOLD, 14));

        menu.add(viewItem);
        menu.add(new JSeparator());
        menu.add(createItem);

        viewItem.addActionListener(_ -> {
            if (onSearchDateRequest != null) onSearchDateRequest.accept(calendar.getDate());
        });

        createItem.addActionListener(_ -> {
            if (onCreateRequest != null) onCreateRequest.accept(calendar.getDate());
        });

        MouseAdapter clickHandler = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handle(e); }
            @Override public void mouseReleased(MouseEvent e) { handle(e); }

            private void handle(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) || e.isPopupTrigger()) {
                    Component c = e.getComponent();
                    if (c instanceof JButton dayBtn) {
                        String text = dayBtn.getText();
                        if (text == null || !text.matches("\\d+")) return;

                        // Select Date logic
                        java.util.Calendar cal = calendar.getCalendar();
                        cal.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(text));
                        calendar.setDate(cal.getTime());

                        // Update Menu Text
                        int count = countAppointmentsOnDate(calendar.getDate());
                        if (count > 0) {
                            viewItem.setText("View " + count + " Appointment(s)");
                            viewItem.setForeground(new Color(0, 150, 0));
                        } else {
                            viewItem.setText("View Day (Empty)");
                            viewItem.setForeground(Color.GRAY);
                        }
                        menu.show(c, e.getX(), e.getY());
                    }
                }
            }
        };

        JPanel dayPanel = calendar.getDayChooser().getDayPanel();
        for (Component comp : dayPanel.getComponents()) {
            if (comp instanceof JButton) comp.addMouseListener(clickHandler);
        }
    }

    // --- DATA HANDLING ---

    private void loadDataFromDB() {
        try {
            DatabaseHelper.autoUpdateStatuses();

            appointmentList.clear();
            appointmentList.addAll(DatabaseHelper.getDashboardAppointments(new java.util.Date()));
            appointmentList.sort(Comparator.comparing(Appointment::getDate));

            refreshTable();

            // Stats
            List<Appointment> activeList = DatabaseHelper.getActiveAppointments();
            long todayCount = appointmentList.stream().filter(a -> Utils.isToday(a.getDate())).count();

            todayLabel.setText(LanguageHelper.getString("dsb.today") + todayCount);
            activeLabel.setText(LanguageHelper.getString("dsb.active") + activeList.size());

            // Calendar Cache
            this.calendarCache = DatabaseHelper.getAllAppointments();

        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error loading dashboard", e);
            ThemedDialog.showMessage(this, LanguageHelper.getString("title.error"), "Database Error: " + e.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Appointment a : appointmentList) {
            tableModel.addRow(new Object[]{
                    a.getClientName(), a.getClientPhone(), a.getCarLicensePlate(),
                    a.getCarBrand(), dateFormat.format(a.getDate()), a.getStatus()
            });
        }
    }

    private int countAppointmentsOnDate(Date date) {
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

    private void updateText() {
        tableHeader.setText(LanguageHelper.getString("hdr.dashboard"));
        loadDataFromDB();

        if (tableModel != null) {
            String[] cols = {
                    LanguageHelper.getString("col.client"), LanguageHelper.getString("col.phone"),
                    LanguageHelper.getString("col.plate"), LanguageHelper.getString("col.brand"),
                    LanguageHelper.getString("col.date"), LanguageHelper.getString("col.status")
            };
            tableModel.setColumnIdentifiers(cols);

            // Re-apply renderer after column update
            agendaTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
            agendaTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));

            calendar.setLocale(LanguageHelper.getCurrentLocale());
            SwingUtilities.invokeLater(() -> CalendarCustomizer.styleCalendar(calendar));
            StatusMenuHelper.attach(agendaTable, appointmentList, this::loadDataFromDB, this);
        }
    }

    // --- SETTERS ---
    public void setOnCreateRequest(Consumer<Date> callback) { this.onCreateRequest = callback; }
    public void setOnJumpRequest(Consumer<Integer> callback) { this.onJumpRequest = callback; }
    public void setOnSearchDateRequest(Consumer<Date> callback) { this.onSearchDateRequest = callback; }
}
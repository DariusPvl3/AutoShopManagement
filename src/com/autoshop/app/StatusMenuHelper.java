package com.autoshop.app;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StatusMenuHelper {

    public static void attach(JTable table, List<Appointment> appointmentList, Runnable onRefresh, Component parent) {
        JPopupMenu popupMenu = new JPopupMenu();

        // Iterate through ALL statuses in the Enum to create menu items dynamically
        // This is better than hardcoding "Mark as DONE", etc.
        for (AppointmentStatus status : AppointmentStatus.values()) {
            JMenuItem item = new JMenuItem("Mark as " + status.toString()); // Uses our nice toString()

            // --- THE COLOR MAGIC ---
            item.setForeground(status.getColor());
            item.setFont(new Font("SansSerif", Font.BOLD, 12));
            // -----------------------

            item.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) return;

                try {
                    Appointment appt = appointmentList.get(selectedRow);
                    appt.setStatus(status); // Update Object
                    DatabaseHelper.updateAppointmentTransaction(appt); // Update DB
                    onRefresh.run(); // Refresh UI

                    // Optional: Show a tiny popup confirmation or just refresh silently
                    // JOptionPane.showMessageDialog(parent, "Status updated!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage());
                }
            });

            popupMenu.add(item);
        }

        table.setComponentPopupMenu(popupMenu);
    }
}
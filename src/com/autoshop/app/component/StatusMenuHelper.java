package com.autoshop.app.component;

import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.util.DatabaseHelper;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StatusMenuHelper {

    public static void attach(JTable table, List<Appointment> appointmentList, Runnable onRefresh, Component parent) {
        JPopupMenu popupMenu = new JPopupMenu();

        // Iterate through ALL statuses in the Enum to create menu items dynamically
        for (AppointmentStatus status : AppointmentStatus.values()) {
            JMenuItem item = new JMenuItem("Mark as " + status.toString());

            // --- COLOR ---
            item.setForeground(status.getColor());
            item.setFont(new Font("SansSerif", Font.BOLD, 12));
            item.addActionListener(_ -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) return;

                try {
                    Appointment appointment = appointmentList.get(selectedRow);
                    appointment.setStatus(status);
                    DatabaseHelper.updateAppointmentTransaction(appointment);
                    onRefresh.run();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage());
                }
            });
            popupMenu.add(item);
        }
        table.setComponentPopupMenu(popupMenu);
    }
}
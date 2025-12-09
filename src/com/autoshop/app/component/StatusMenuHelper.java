package com.autoshop.app.component;

import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicMenuItemUI; // Import this!
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

public class StatusMenuHelper {

    public static void attach(JTable table, List<Appointment> appointmentList, Runnable onRefresh, Component parent) {
        JPopupMenu popupMenu = new JPopupMenu();

        // 1. Style the Container
        popupMenu.setBackground(Color.WHITE);
        popupMenu.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        for (AppointmentStatus status : AppointmentStatus.values()) {

            String translatedName = LanguageHelper.getString(status.getLangKey());
            JMenuItem item = new JMenuItem(translatedName);

            // 2. FORCE THE UI TO RESPECT OUR COLORS
            // We replace the Windows UI with a Basic UI that lets us paint the background manually
            item.setUI(new BasicMenuItemUI() {
                @Override
                protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                    if (menuItem.isArmed() || menuItem.isSelected()) {
                        // Hovered/Selected -> Paint RED
                        g.setColor(Theme.RED);
                    } else {
                        // Normal -> Paint WHITE
                        g.setColor(Color.WHITE);
                    }
                    g.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
                }
            });

            // 3. Basic Styling
            item.setOpaque(true);
            item.setBackground(Color.WHITE);
            item.setForeground(status.getColor());
            item.setFont(new Font("SansSerif", Font.BOLD, 12));
            item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

            // 4. Handle TEXT Color on Hover
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    item.setForeground(Color.WHITE); // Text turns White on Red background
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    item.setForeground(status.getColor()); // Text turns back to Color on White background
                }
            });

            // 5. Action Logic
            item.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) return;

                try {
                    Appointment appointment = appointmentList.get(selectedRow);
                    appointment.setStatus(status);
                    DatabaseHelper.updateAppointmentTransaction(appointment);
                    onRefresh.run();
                } catch (SQLException ex) {
                    ThemedDialog.showMessage(parent, "Error", ex.getMessage());
                }
            });

            popupMenu.add(item);
        }
        table.setComponentPopupMenu(popupMenu);
    }
}
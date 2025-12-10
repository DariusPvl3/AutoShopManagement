package com.autoshop.app.component;

import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicMenuItemUI;
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

            // STORE THE CORRECT COLOR INSIDE THE ITEM FOR LATER RETRIEVAL
            item.putClientProperty("defaultColor", status.getColor());

            // 2. FORCE THE UI TO RESPECT OUR COLORS
            item.setUI(new BasicMenuItemUI() {
                @Override
                protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                    // Only paint RED if the mouse is ACTUALLY hovering (armed)
                    if (menuItem.isArmed()) {
                        g.setColor(Theme.RED);
                    } else {
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

            // 4. Hover Listener (Text Color Swap)
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    item.setForeground(Color.WHITE);
                    item.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    // Retrieve the stored color to ensure we restore the correct one
                    Color defColor = (Color) item.getClientProperty("defaultColor");
                    item.setForeground(defColor);
                    item.setBackground(Color.WHITE);
                    item.repaint();
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

        // --- THE FIX: RESET COLORS ON OPEN ---
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // Iterate through all items and force them back to "Normal" state
                for (Component comp : popupMenu.getComponents()) {
                    if (comp instanceof JMenuItem) {
                        JMenuItem menu = (JMenuItem) comp;
                        Color defColor = (Color) menu.getClientProperty("defaultColor");
                        if (defColor != null) {
                            menu.setForeground(defColor);
                            menu.setBackground(Color.WHITE);
                            // Force "un-arm" logic visually
                            menu.setArmed(false);
                            menu.setSelected(false);
                        }
                    }
                }
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        // 6. Manual Trigger
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handlePopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handlePopup(e); }

            private void handlePopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int r = table.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < table.getRowCount()) {
                        table.setRowSelectionInterval(r, r);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }
}
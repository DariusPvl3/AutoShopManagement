package com.autoshop.app;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof AppointmentStatus) {
            AppointmentStatus status = (AppointmentStatus) value;

            // 1. Set the Text Color
            if (isSelected) {
                c.setForeground(Color.WHITE); // Keep selection white
            } else {
                c.setForeground(status.getColor()); // Use the Enum color
            }

            // 2. Set the Font (Optional: Make it bold)
            c.setFont(c.getFont().deriveFont(Font.BOLD));
        }

        return c;
    }
}
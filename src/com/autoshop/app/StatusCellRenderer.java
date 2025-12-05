package com.autoshop.app;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Handle Enum Objects (Ideally your table model returns Enums now)
        if (value instanceof AppointmentStatus) {
            AppointmentStatus status = (AppointmentStatus) value;

            // Translate
            setText(LanguageHelper.getString(status.getLangKey()));

            // Color
            if (isSelected) {
                c.setForeground(Color.WHITE);
            } else {
                c.setForeground(status.getColor());
            }

            c.setFont(c.getFont().deriveFont(Font.BOLD));
        }

        return c;
    }
}
package com.autoshop.app.component;

import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.util.LanguageHelper;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Handle Enum Objects
        if (value instanceof AppointmentStatus status) {
            // Translate
            setText(LanguageHelper.getString(status.getLangKey()));
            // Color
            if (isSelected)
                c.setForeground(Color.WHITE);
            else
                c.setForeground(status.getColor());
        }
        return c;
    }
}
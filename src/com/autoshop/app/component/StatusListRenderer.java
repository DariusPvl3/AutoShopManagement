package com.autoshop.app.component;

import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.util.LanguageHelper;
import javax.swing.*;
import java.awt.*;

public class StatusListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof AppointmentStatus) {
            AppointmentStatus status = (AppointmentStatus) value;

            // 1. Translation
            // Instead of using the default toString(), we lookup the translation
            String translatedText = LanguageHelper.getString(status.getLangKey());
            setText(translatedText);

            // 2. Set Color
            if (isSelected) c.setForeground(Color.WHITE);
            else c.setForeground(status.getColor());

            c.setFont(c.getFont().deriveFont(Font.BOLD));
        } else {
            setText(value.toString());
            if (!isSelected)
                c.setForeground(Color.BLACK);

            c.setFont(c.getFont().deriveFont(Font.PLAIN));
        }
        return c;
    }
}
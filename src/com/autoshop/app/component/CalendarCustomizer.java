package com.autoshop.app.component;

import com.autoshop.app.util.Theme;
import com.toedter.calendar.JCalendar;
import javax.swing.*;
import java.awt.*;

public class CalendarCustomizer {

    public static void styleCalendar(JCalendar calendar) {
        JPanel dayPanel = calendar.getDayChooser().getDayPanel();

        // 1. Apply Theme Backgrounds
        calendar.setBackground(Theme.OFF_WHITE);
        dayPanel.setBackground(Theme.OFF_WHITE);
        calendar.getDayChooser().setBackground(Theme.OFF_WHITE);
        calendar.getMonthChooser().setBackground(Theme.OFF_WHITE);
        calendar.getYearChooser().setBackground(Theme.OFF_WHITE);

        // 2. Header Colors (Black text for days, Red for Sunday)
        calendar.getDayChooser().setDecorationBackgroundVisible(true);
        calendar.getDayChooser().setDecorationBackgroundColor(Theme.OFF_WHITE);
        calendar.getDayChooser().setWeekdayForeground(Theme.BLACK);
        calendar.getDayChooser().setSundayForeground(Theme.RED);

        // 3. Fonts & Sizes
        Font headerFont = new Font("SansSerif", Font.BOLD, 16);
        Dimension comboSize = new Dimension(150, 35);
        Dimension spinnerSize = new Dimension(100, 35);

        // --- STYLE MONTH CHOOSER ---
        JPanel monthPanel = calendar.getMonthChooser();
        monthPanel.setBackground(Theme.OFF_WHITE);
        monthPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 25));

        JComboBox<?> monthCombo = (JComboBox<?>) calendar.getMonthChooser().getComboBox();
        monthCombo.setFont(headerFont);
        monthCombo.setPreferredSize(comboSize);
        monthCombo.setBackground(Theme.WHITE);

        // Custom Renderer for Capitalization + Center Alignment
        monthCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setFont(headerFont);
                l.setHorizontalAlignment(SwingConstants.CENTER);

                // Capitalize Text (e.g., "december" -> "December")
                String text = l.getText();
                if (text != null && !text.isEmpty())
                    l.setText(text.substring(0, 1).toUpperCase() + text.substring(1));
                return l;
            }
        });

        // --- STYLE YEAR CHOOSER ---
        JPanel yearPanel = calendar.getYearChooser();
        yearPanel.setPreferredSize(spinnerSize);
        yearPanel.setBackground(Theme.OFF_WHITE);

        JSpinner yearSpinner = (JSpinner) calendar.getYearChooser().getSpinner();
        yearSpinner.setFont(headerFont);
        yearSpinner.setPreferredSize(spinnerSize);
        styleSpinnerTextField(yearSpinner, headerFont);

        // --- STYLE DAY NAMES (Sun, Mon...) ---
        for (Component comp : dayPanel.getComponents()) {
            // Day Names (Labels)
            if (comp instanceof JLabel dayLabel) {
                dayLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                String text = dayLabel.getText();
                if (text != null && !text.isEmpty())
                    dayLabel.setText(text.substring(0, 1).toUpperCase() + text.substring(1));
            }
            // Day Buttons
            else if (comp instanceof JButton) {
                comp.setFont(new Font("SansSerif", Font.BOLD, 14));
                comp.setBackground(Theme.OFF_WHITE);
            }
        }
    }

    private static void styleSpinnerTextField(Container container, Font font) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField tf) {
                tf.setFont(font);
                tf.setHorizontalAlignment(SwingConstants.CENTER);
                tf.setBackground(Theme.WHITE);
                return;
            } else if (comp instanceof Container)
                styleSpinnerTextField((Container) comp, font);
        }
    }

    public static void styleDateChooser(com.toedter.calendar.JDateChooser dateChooser) {
        Font font = new Font("SansSerif", Font.PLAIN, 14);
        dateChooser.setFont(font);

        for (Component comp : dateChooser.getComponents()) {
            if (comp instanceof JButton btn) {
                btn.setPreferredSize(new Dimension(30, 25));
            }
        }
        JCalendar popupCalendar = dateChooser.getJCalendar();
        styleCalendar(popupCalendar);
    }
}
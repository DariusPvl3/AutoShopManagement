package com.autoshop.app;

import com.toedter.calendar.JCalendar;
import javax.swing.*;
import java.awt.*;
import java.util.Calendar;

public class CalendarCustomizer {

    public static void styleCalendar(JCalendar calendar) {
        JPanel dayPanel = calendar.getDayChooser().getDayPanel();

        // 1. Reset Body Logic
        calendar.getDayChooser().setDecorationBackgroundVisible(true);

        // 2. Fonts & Sizes
        Font headerFont = new Font("SansSerif", Font.BOLD, 16);
        Dimension comboSize = new Dimension(150, 35);
        Dimension spinnerSize = new Dimension(100, 35);

        // --- 3. STYLE MONTH CHOOSER ---
        JPanel monthPanel = (JPanel) calendar.getMonthChooser();
        monthPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 25));

        JComboBox<?> monthCombo = (JComboBox<?>) calendar.getMonthChooser().getComboBox();
        monthCombo.setFont(headerFont);
        monthCombo.setPreferredSize(comboSize);

        // THE FIX FOR MONTHS: Custom Renderer to Capitalize
        monthCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setFont(headerFont);
                l.setHorizontalAlignment(SwingConstants.CENTER);

                // CAPITALIZE TEXT
                String text = l.getText();
                if (text != null && !text.isEmpty()) {
                    l.setText(text.substring(0, 1).toUpperCase() + text.substring(1));
                }
                return l;
            }
        });

        // --- 4. STYLE YEAR CHOOSER ---
        JPanel yearPanel = (JPanel) calendar.getYearChooser();
        yearPanel.setPreferredSize(spinnerSize);
        JSpinner yearSpinner = (JSpinner) calendar.getYearChooser().getSpinner();
        yearSpinner.setFont(headerFont);
        yearSpinner.setPreferredSize(spinnerSize);
        styleSpinnerTextField(yearSpinner, headerFont);

        // --- 5. STYLE DAY NAMES (Sun, Mon, Tue...) ---
        // These are usually JLabels inside the dayPanel (at the top)
        // We iterate and force capitalization
        for (Component comp : dayPanel.getComponents()) {
            // Day Names are typically JLabels, Day Numbers are JButtons
            if (comp instanceof JLabel) {
                JLabel dayLabel = (JLabel) comp;
                dayLabel.setFont(new Font("SansSerif", Font.BOLD, 12)); // Make header smaller/different if needed

                String text = dayLabel.getText();
                if (text != null && !text.isEmpty()) {
                    dayLabel.setText(text.substring(0, 1).toUpperCase() + text.substring(1));
                }
            }
            // Day Buttons
            else if (comp instanceof JButton) {
                comp.setFont(new Font("SansSerif", Font.BOLD, 14));
            }
        }
    }

    public static void paintDates(JCalendar calendar) {
        JPanel dayPanel = calendar.getDayChooser().getDayPanel();

        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getDate());
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        // Borders
        javax.swing.border.Border selectedBorder = BorderFactory.createLineBorder(new Color(52, 152, 219), 2);
        javax.swing.border.Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

        for (Component comp : dayPanel.getComponents()) {
            if (comp instanceof JButton btn) {
                String text = btn.getText();

                if (text == null || text.trim().isEmpty() || !text.matches("\\d+")) {
                    btn.setOpaque(false);
                    btn.setContentAreaFilled(false);
                    btn.setBorder(null);
                    continue;
                }

                int day = Integer.parseInt(text);

                btn.setContentAreaFilled(true);
                btn.setOpaque(true);
                btn.setBackground(null);
                btn.setForeground(Color.BLACK);

                // Apply Selection Border
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(calendar.getDate());

                if (selectedCal.get(Calendar.DAY_OF_MONTH) == day &&
                        selectedCal.get(Calendar.MONTH) == currentMonth &&
                        selectedCal.get(Calendar.YEAR) == currentYear) {
                    btn.setBorder(selectedBorder);
                } else {
                    btn.setBorder(emptyBorder);
                }
            }
        }
    }

    private static void styleSpinnerTextField(Container container, Font font) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField tf) {
                tf.setFont(font);
                tf.setHorizontalAlignment(SwingConstants.CENTER);
                return;
            } else if (comp instanceof Container) {
                styleSpinnerTextField((Container) comp, font);
            }
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
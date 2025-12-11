package com.autoshop.app.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return "";
        String[] words = input.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                String cap = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                result.append(cap).append(" ");
            }
        }
        return result.toString().trim();
    }

    public static Date combineDateAndTime(Date datePart, Date timePart) {
        if (datePart == null || timePart == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datePart);
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(timePart);
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        String cleanPhone = phone.replaceAll("[\\s\\-.()]", "");
        return cleanPhone.matches("^(\\+40|0)[0-9]{9}$");
    }

    public static boolean isValidPlate(String plate) {
        if (plate == null || plate.isEmpty()) return false;

        // Regex Logic:
        // ^[A-Z]{1,2}   -> 1 or 2 letters for County (e.g., B, TM, CJ)
        // -             -> Dash separator
        // [0-9]{2,3}    -> 2 or 3 digits (e.g., 05, 100)
        // -             -> Dash separator
        // [A-Z]{3}$     -> Exactly 3 letters at the end (e.g., AAA)
        return plate.matches("^[A-Z]{1,2}-[0-9]{2,3}-[A-Z]{3}$");
    }

    public static String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9]", "");
    }

    public static String formatPlate(String rawPlate) {
        String clean = rawPlate.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (clean.matches("^([A-Z]{1,2})([0-9]{2,3})([A-Z]{3})$")) {
            return clean.replaceAll("^([A-Z]{1,2})([0-9]{2,3})([A-Z]{3})$", "$1-$2-$3");
        }
        return clean;
    }

    // --- SCROLL LOGIC UPDATES ---

    // 1. OLD METHOD (Kept for compatibility)
    // Calls the new method with 'null' so to keep the code for carYearField
    public static void addMouseScrollToSpinner(JSpinner spinner) {
        addMouseScrollToSpinner(spinner, null);
    }

    // 2. NEW METHOD (Supports Callback)
    public static void addMouseScrollToSpinner(JSpinner spinner, Runnable onChange) {
        spinner.addMouseWheelListener(e -> {
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor) editor).getTextField().requestFocusInWindow();
            }

            Object value = spinner.getValue();
            int rotation = e.getWheelRotation();

            // --- DATE MODEL LOGIC ---
            if (value instanceof Date) {
                Calendar cal = Calendar.getInstance();
                cal.setTime((Date) value);
                int minutesToAdd = (rotation < 0) ? 1 : -1;
                cal.add(Calendar.MINUTE, minutesToAdd);
                spinner.setValue(cal.getTime());
            }
            // --- NUMBER MODEL LOGIC ---
            else if (value instanceof Number) {
                Number stepSize = 1;
                if (spinner.getModel() instanceof SpinnerNumberModel) {
                    stepSize = ((SpinnerNumberModel) spinner.getModel()).getStepSize();
                }
                if (value instanceof Integer) {
                    int current = (Integer) value;
                    int step = stepSize.intValue();
                    int next = (rotation < 0) ? current + step : current - step;

                    if (spinner.getModel() instanceof SpinnerNumberModel model) {
                        Comparable<?> max = model.getMaximum();
                        Comparable<?> min = model.getMinimum();
                        if (max != null && next > (Integer) max) next = (Integer) max;
                        if (min != null && next < (Integer) min) next = (Integer) min;
                    }
                    spinner.setValue(next);
                }
            }

            // --- TRIGGER CALLBACK ---
            if (onChange != null) {
                onChange.run();
            }
        });
    }

    public static boolean isToday(java.util.Date date) {
        java.util.Calendar t = java.util.Calendar.getInstance();
        java.util.Calendar d = java.util.Calendar.getInstance();
        d.setTime(date);
        return t.get(java.util.Calendar.YEAR) == d.get(java.util.Calendar.YEAR) &&
                t.get(java.util.Calendar.DAY_OF_YEAR) == d.get(java.util.Calendar.DAY_OF_YEAR);
    }

    public static void copyFile(File source, File dest) throws java.io.IOException {
        java.nio.file.Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public static String getCurrentTimeStamp(){
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
    }

    public static ImageIcon loadIcon(String path, int width, int height) {
        try {
            java.net.URL imgURL = Utils.class.getResource(path);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } else {
                System.err.println("Could not find file: " + path);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
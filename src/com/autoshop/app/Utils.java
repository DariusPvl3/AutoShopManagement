package com.autoshop.app;

import javax.swing.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String[] words = input.trim().split("\\s+"); // Split by spaces
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                // Capitalize first letter, lowercase the rest
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
        if (phone == null || phone.isEmpty()) {
            return false;
        } else {
            String cleanPhone = phone.trim().replace(" ", "");
            return cleanPhone.matches("^(\\+40|0)[0-9]{9}$");
        }
    }

    public static boolean isValidPlate(String plate) {
        // Regex for TM-12-ABC
        return plate.matches("^[A-Z]{1,2}-[0-9]{2,3}-[A-Z]{3}$");
    }

    public static String formatPlate(String rawPlate) {
        String clean = rawPlate.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (clean.matches("^([A-Z]{1,2})([0-9]{2,3})([A-Z]{3})$")) {
            return clean.replaceAll("^([A-Z]{1,2})([0-9]{2,3})([A-Z]{3})$", "$1-$2-$3");
        }
        return clean;
    }

    public static void addMouseScrollToSpinner(JSpinner spinner) {
        spinner.addMouseWheelListener(e -> {
            if (spinner.isEnabled()) {
                // Determine direction
                int rotation = e.getWheelRotation();

                // Determine step (Hour or Minute?)
                // Default to 15 minutes per scroll for speed
                int step = (rotation < 0) ? 1 : -1;

                // Manually adjust the date
                Date current = (Date) spinner.getValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(current);
                cal.add(Calendar.MINUTE, step * 15); // Jump 15 mins
                spinner.setValue(cal.getTime());
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
}

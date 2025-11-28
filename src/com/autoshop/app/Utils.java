package com.autoshop.app;

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
}

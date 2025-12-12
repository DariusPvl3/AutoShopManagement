package com.autoshop.app.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlateValidator {
    private static final Map<String, Pattern> PATTERNS = new HashMap<>();

    static {
        // Romania (RO):
        // Option 1 (Standard):  TM 12 ABC  (County + 2-3 Digits + 3 Letters)
        // Option 2 (Temporary): TM 123456  (County + 3-6 Digits)
        // We use an OR (|) operator to allow both.
        // Groups: 2=County, 3=Digits, 4=Letters | 6=County, 7=Digits
        PATTERNS.put("RO", Pattern.compile("^(([A-Z]{1,2})\\s?-?\\s?([0-9]{2,3})\\s?-?\\s?([A-Z]{3}))|(([A-Z]{1,2})\\s?-?\\s?([0-9]{3,6}))$"));

        // Austria (AT): 1-2 Letters + 3-6 Alphanumeric
        PATTERNS.put("AT", Pattern.compile("^[A-Z]{1,2}\\s?-?\\s?[A-Z0-9]{3,6}$"));

        // Germany (DE): 1-3 Letters + 1-2 Letters + 1-4 Digits
        PATTERNS.put("DE", Pattern.compile("^[A-Z]{1,3}\\s?-?\\s?[A-Z]{1,2}\\s?-?\\s?[0-9]{1,4}$"));

        // Bulgaria (BG): 1-2 Letters + 4 Digits + 1-2 Letters
        PATTERNS.put("BG", Pattern.compile("^[A-Z]{1,2}\\s?-?\\s?[0-9]{4}\\s?-?\\s?[A-Z]{1,2}$"));

        // Hungary (HU): Old (ABC-123) or New (AA-AA-123)
        PATTERNS.put("HU", Pattern.compile("^([A-Z]{3}\\s?-?\\s?[0-9]{3})|([A-Z]{2}\\s?-?\\s?[A-Z]{2}\\s?-?\\s?[0-9]{3})$"));

        // Ukraine (UA): 2 Letters + 4 Digits + 2 Letters
        PATTERNS.put("UA", Pattern.compile("^[A-Z]{2}\\s?-?\\s?[0-9]{4}\\s?-?\\s?[A-Z]{2}$"));

        // Moldova (MD): 3 Letters + 3 Digits
        PATTERNS.put("MD", Pattern.compile("^[A-Z]{3}\\s?-?\\s?[0-9]{3}$"));

        // Serbia (RS): 2 Letters + 3-4 Digits + 2 Letters
        PATTERNS.put("RS", Pattern.compile("^[A-Z]{2}\\s?-?\\s?[0-9]{3,4}\\s?-?\\s?[A-Z]{2}$"));

        // Italy (IT) & France (FR): AA-123-BB
        Pattern standardEuro = Pattern.compile("^[A-Z]{2}\\s?-?\\s?[0-9]{3}\\s?-?\\s?[A-Z]{2}$");
        PATTERNS.put("IT", standardEuro);
        PATTERNS.put("FR", standardEuro);

        // Spain (ES): 4 Digits + 3 Consonants
        PATTERNS.put("ES", Pattern.compile("^[0-9]{4}\\s?-?\\s?[BCDFGHJKLMNPRSTVWXYZ]{3}$"));
    }

    public static boolean isValid(String plate) {
        if (plate == null || plate.isEmpty()) return false;
        String cleanPlate = plate.toUpperCase().trim();

        for (Pattern pattern : PATTERNS.values()) {
            if (pattern.matcher(cleanPlate).matches()) {
                return true;
            }
        }
        return false;
    }

    public static String format(String plate) {
        if (plate == null) return "";
        String upper = plate.toUpperCase().trim();

        Matcher roMatcher = PATTERNS.get("RO").matcher(upper);
        if (roMatcher.matches()) {
            // Check which part of the OR matched
            if (roMatcher.group(2) != null) {
                // STANDARD: Groups 2 (County), 3 (Digits), 4 (Letters)
                return roMatcher.group(2) + "-" + roMatcher.group(3) + "-" + roMatcher.group(4);
            } else if (roMatcher.group(6) != null) {
                // TEMPORARY: Groups 6 (County), 7 (Digits)
                // Format: TM-123456
                return roMatcher.group(6) + "-" + roMatcher.group(7);
            }
        }

        return upper.replaceAll("[\\s-]", "");
    }
}
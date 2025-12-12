package com.autoshop.app.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneValidator {
    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    /**
     * Validates a phone number.
     * If the number starts with '+', it is treated as international.
     * If not, it assumes the default region (Romania 'RO').
     */
    public static boolean isValid(String numberStr) {
        if (numberStr == null || numberStr.trim().isEmpty()) return false;

        try {
            // "RO" is the default region if the user types "0744..."
            // If they type "+49...", the library ignores "RO" and uses the code.
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(numberStr, "RO");
            return phoneUtil.isValidNumber(numberProto);
        } catch (NumberParseException e) {
            return false;
        }
    }

    /**
     * Formats a phone number to standard E.164 (e.g., +40744123456)
     * Useful for storing consistent data in the DB.
     */
    public static String format(String numberStr) {
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(numberStr, "RO");
            if (phoneUtil.isValidNumber(numberProto)) {
                return phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            }
        } catch (NumberParseException ignored) { }
        return numberStr; // Return raw if parsing fails
    }
}
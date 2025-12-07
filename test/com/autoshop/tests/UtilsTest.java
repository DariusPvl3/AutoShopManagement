package com.autoshop.tests;

import com.autoshop.app.util.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class UtilsTest {
    @Test
    void testTitleCase(){
        // Scenario 1: Simple word
        String input1 = "benz";
        String expected1 = "Benz";
        String actual1 = Utils.toTitleCase(input1);
        Assertions.assertEquals(expected1, actual1, "Single word should be capitalized");

        // Scenario 2: Multiple words
        String input2 = "benz c180";
        String expected2 = "Benz C180";
        Assertions.assertEquals(expected2, Utils.toTitleCase(input2), "Multiple words should be capitalized individually");

        // Scenario 3: Edge Case (Empty)
        Assertions.assertEquals("", Utils.toTitleCase(""), "Empty string should return empty string");

        // Scenario 4: Edge Case (Null)
        Assertions.assertEquals("", Utils.toTitleCase(null), "Null input should return empty string to avoid crash");
    }

    @Test
    void testPhoneNumber(){
        // Scenario 1: Correct number without country code
        String input1 = "0123456789";
        boolean expected1 = true;
        boolean actual1 = Utils.isValidPhone(input1);
        Assertions.assertEquals(expected1, actual1, "Phone number should be valid");

        // Scenario 2: Correct number with country code
        String input2 = "+40123456789";
        boolean expected2 = true;
        boolean actual2 = Utils.isValidPhone(input2);
        Assertions.assertEquals(expected2, actual2, "Phone number should be valid");

        // Scenario 3: Correct number with no country code and spaces
        String input3 = "0123 456 789";
        boolean expected3 = true;
        boolean actual3 = Utils.isValidPhone(input3);
        Assertions.assertEquals(expected3, actual3, "Phone number should be valid");

        // Scenario 4: Correct number with country code and spaces
        String input4 = "+40123 456 789";
        boolean expected4 = true;
        boolean actual4 = Utils.isValidPhone(input4);
        Assertions.assertEquals(expected4, actual4, "Phone number should be valid");

        // Scenario 5: Different country code, testing Spain (should fail in this stage of development)
        String input5 = "+34678123456";
        boolean expected5 = false;
        boolean actual5 = Utils.isValidPhone(input5);
        Assertions.assertEquals(expected5, actual5, "Different country codes don't work yet");

        // Scenario 6: Not a valid phone number
        String input6 = "123";
        boolean expected6 = false;
        boolean actual6 = Utils.isValidPhone(input6);
        Assertions.assertEquals(expected6, actual6, "Not a real phone number should not work");

        // Scenario 7: No number at all
        String input7 = "";
        boolean expected7 = false;
        boolean actual7 = Utils.isValidPhone(input7);
        Assertions.assertEquals(expected7, actual7, "No number provided should not work");

        // Scenario 8: Passing null
        boolean expected8 = false;
        Utils.isValidPhone(null);
        boolean actual8 = false;
        Assertions.assertEquals(expected8, actual8, "Null should not work");
    }

    @Test
    void testLicensePlateFormatting() {
        // 1. Standard format input
        String input1 = "TM12ABC";
        String expected1 = "TM-12-ABC";
        Assertions.assertEquals(expected1, Utils.formatPlate(input1), "Should insert hyphens correctly");

        // 2. Lowercase input
        String input2 = "tm12abc";
        Assertions.assertEquals(expected1, Utils.formatPlate(input2), "Should uppercase and format");

        // 3. Already formatted
        String input3 = "TM-12-ABC";
        Assertions.assertEquals(expected1, Utils.formatPlate(input3), "Should ignore existing hyphens");

        // 4. Invalid/Short plate (Should remain cleaned but unformatted)
        String input4 = "TM1";
        Assertions.assertEquals("TM1", Utils.formatPlate(input4), "Short input should just be uppercased");
    }

    @Test
    void testCombineDateAndTime() {
        // Setup: Date = 2025-12-25, Time = 14:30
        java.util.Calendar dateCal = java.util.Calendar.getInstance();
        dateCal.set(2025, java.util.Calendar.DECEMBER, 25);

        java.util.Calendar timeCal = java.util.Calendar.getInstance();
        timeCal.set(java.util.Calendar.HOUR_OF_DAY, 14);
        timeCal.set(java.util.Calendar.MINUTE, 30);
        timeCal.set(java.util.Calendar.SECOND, 0);

        // Execute
        Date result = Utils.combineDateAndTime(dateCal.getTime(), timeCal.getTime());

        // Verify
        java.util.Calendar resultCal = java.util.Calendar.getInstance();
        resultCal.setTime(result);

        Assertions.assertEquals(2025, resultCal.get(java.util.Calendar.YEAR));
        Assertions.assertEquals(java.util.Calendar.DECEMBER, resultCal.get(java.util.Calendar.MONTH));
        Assertions.assertEquals(25, resultCal.get(java.util.Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(14, resultCal.get(java.util.Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(30, resultCal.get(java.util.Calendar.MINUTE));
    }
}

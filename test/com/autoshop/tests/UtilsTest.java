package com.autoshop.tests;

import com.autoshop.app.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        String input8 = null;
        boolean expected8 = false;
        boolean actual8 = Utils.isValidPhone(input8);
        Assertions.assertEquals(expected8, actual8, "Null should not work");
    }
}

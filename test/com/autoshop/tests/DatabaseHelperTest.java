package com.autoshop.tests;

import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.model.Part;
import com.autoshop.app.model.Supplier;
import com.autoshop.app.util.DatabaseHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelperTest {
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseHelper.setDataBaseName("test.db");
        DatabaseHelper.createNewTable();

        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Parts");
            stmt.executeUpdate("DELETE FROM Appointments");
            stmt.executeUpdate("DELETE FROM Cars");
            stmt.executeUpdate("DELETE FROM Clients");
            stmt.executeUpdate("DELETE FROM sqlite_sequence");
        }
    }
    @Test
    public void testInsertAppointment() throws SQLException {
        Date testDate = new Date();

        // Create a list of parts for testing
        List<Part> parts = new ArrayList<>();
        parts.add(new Part("CODE1", "Alternator", Supplier.AUTONET));

        Appointment test = new Appointment("Test", "0777777777", "TM01TST", "TestCar", "TestModel", 2000, "testPath", testDate, "Test Problem Description", "Test Repairs", parts, "Test Observations");
        DatabaseHelper.addAppointmentTransaction(test);
        List<Appointment> testList = DatabaseHelper.getAllAppointments();

        // Test 1: Check if appointment is created
        Assertions.assertEquals(1, testList.size(), "Appointment should have been added.");

        // Test 2: Check if info about appointment was added
        Assertions.assertEquals("Test", testList.get(0).getClientName(), "Client name should have been added.");

        // Test 3: Check Parts
        Assertions.assertEquals(1, testList.get(0).getPartList().size(), "Should have 1 part.");
        Assertions.assertEquals("Alternator", testList.get(0).getPartList().get(0).getName());
    }

    @Test
    public void testDeleteAppointment() throws SQLException {
        Date testDate = new Date();
        // Use empty part list for simple delete test
        Appointment test2 = new Appointment("Test2", "0712 345 678", "BZ02TST", "TestCar", "TestModel", 2000, "testPath2", testDate, "Test Description", "Test Repairs", new ArrayList<>(), "Test Observations");
        DatabaseHelper.addAppointmentTransaction(test2);

        List<Appointment> all =  DatabaseHelper.getAllAppointments();
        Appointment savedAppointment = all.get(0);
        int realID = savedAppointment.getAppointmentID();

        DatabaseHelper.deleteAppointment(realID);
        List<Appointment> checkList = DatabaseHelper.getAllAppointments();
        Assertions.assertEquals(0, checkList.size(), "Appointment should have been deleted.");
    }

    @Test
    public void testUpdateAppointment() throws SQLException {
        // 1. Create original
        Date date = new Date();
        List<Part> originalParts = new ArrayList<>();
        originalParts.add(new Part("OLD1", "OldPart", Supplier.UNIX));

        Appointment original = new Appointment("John Doe", "0711111111", "TM11TEST", "Audi", "A4", 2010, "", date, "Oil Change", "Reparation", originalParts, "Observations");
        DatabaseHelper.addAppointmentTransaction(original);

        // 2. Fetch it to get the ID
        Appointment saved = DatabaseHelper.getAllAppointments().get(0);

        // 3. Modify it (Change Name, Phone, Status, and Parts)
        saved.setClientName("Johnathan Doe");
        saved.setClientPhone("0799999999");
        saved.setStatus(AppointmentStatus.DONE);

        // Change parts completely
        List<Part> newParts = new ArrayList<>();
        newParts.add(new Part("NEW1", "NewPart", Supplier.BARDI));
        saved.setPartList(newParts);

        // 4. Perform Update
        DatabaseHelper.updateAppointmentTransaction(saved);

        // 5. Fetch again and Verify
        Appointment updated = DatabaseHelper.getAllAppointments().get(0);

        Assertions.assertEquals("Johnathan Doe", updated.getClientName(), "Client Name should update");
        Assertions.assertEquals("+40799999999", updated.getClientPhone(), "Client Phone should update");
        Assertions.assertEquals(AppointmentStatus.DONE, updated.getStatus(), "Status should update");

        // Verify Part Update
        Assertions.assertEquals(1, updated.getPartList().size());
        Assertions.assertEquals("NewPart", updated.getPartList().get(0).getName(), "Part name should update");
    }

    @Test
    public void testSearchFunction() throws SQLException {
        Date now = new Date();
        // Add two different cars with empty parts lists
        DatabaseHelper.addAppointmentTransaction(new Appointment("Client A", "0711111111", "TM01A", "Audi", "A4", 2010, "", now, "Fix", "Fixed", new ArrayList<>(), "Obs"));
        DatabaseHelper.addAppointmentTransaction(new Appointment("Client B", "0722222222", "TM02B", "BMW", "X5", 2015, "", now, "Fix", "Fixed", new ArrayList<>(), "Obs"));

        // 1. Search by Brand "Audi"
        List<Appointment> results = DatabaseHelper.searchAppointments("Audi", null, null, null);
        Assertions.assertEquals(1, results.size(), "Search for 'Audi' should return 1 result");
        Assertions.assertEquals("Audi", results.get(0).getCarBrand());

        // 2. Search by License Plate partial "02"
        results = DatabaseHelper.searchAppointments("02", null, null, null);
        Assertions.assertEquals(1, results.size(), "Search for '02' should find the BMW");
        Assertions.assertEquals("TM02B", results.get(0).getCarLicensePlate());

        // 3. Search for non-existent
        results = DatabaseHelper.searchAppointments("Mercedes", null, null, null);
        Assertions.assertEquals(0, results.size(), "Search for missing item should return empty list");
    }

    @Test
    public void testAutoUpdateStatuses() throws SQLException {
        // 1. Create a date in the PAST (Yesterday)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        Date yesterday = cal.getTime();

        // 2. Add an appointment with SCHEDULED status
        Appointment pastAppointment = new Appointment("Lazy Client", "0700000000", "TM00OLD", "Ford", "Focus", 2005, "", yesterday, "Late", "No repair", new ArrayList<>(), "No observations");
        // (Default constructor sets status to SCHEDULED)
        DatabaseHelper.addAppointmentTransaction(pastAppointment);

        // 3. Run the Automation
        DatabaseHelper.autoUpdateStatuses();

        // 4. Verify it flipped to IN_PROGRESS
        Appointment result = DatabaseHelper.getAllAppointments().get(0);
        Assertions.assertEquals(AppointmentStatus.IN_PROGRESS, result.getStatus(), "Past SCHEDULED appointment should become IN_PROGRESS");
    }

    @AfterAll
    public static void cleanUp() throws SQLException {
        DatabaseHelper.setDataBaseName("test.db");
        // Ensure Parts table is cleaned too
        String sql = "DELETE FROM Parts; DELETE FROM Appointments; DELETE FROM Cars; DELETE FROM Clients;";
        try(Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement()){
            stmt.execute(sql);
        }
    }
}
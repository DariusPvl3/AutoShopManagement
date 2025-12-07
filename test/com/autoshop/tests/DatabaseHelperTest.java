package com.autoshop.tests;

import com.autoshop.app.model.Appointment;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.model.AppointmentStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

public class DatabaseHelperTest {
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseHelper.setDataBaseName("test.db");
        DatabaseHelper.createNewTable();
        String sql = "DELETE FROM Appointments; DELETE FROM Cars; DELETE FROM Clients;";
        try(Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement()){
            stmt.execute(sql);
        }
    }

    @Test
    public void testInsertAppointment() throws SQLException {
        Date testDate = new Date();
        Appointment test = new Appointment("Test", "0777777777", "TM01TST", "TestCar", "TestModel", 2000, "testPath", testDate, "Test Problem Description");
        DatabaseHelper.addAppointmentTransaction(test);
        List<Appointment> testList = DatabaseHelper.getAllAppointments();

        // Test 1: Check if appointment is created
        Assertions.assertEquals(1, testList.size(), "Appointment should have been added.");

        // Test 2: Check if info about appointment was added by checking client name
        Assertions.assertEquals("Test", testList.getFirst().getClientName(), "Client name should have been added.");
    }

    @Test
    public void testDeleteAppointment() throws SQLException {
        Date testDate = new Date();
        Appointment test2 = new Appointment("Test2", "0712 345 678", "BZ02TST", "TestCar", "TestModel", 2000, "testPath2", testDate, "Test Description");
        DatabaseHelper.addAppointmentTransaction(test2);
        List<Appointment> all =  DatabaseHelper.getAllAppointments();
        Appointment savedAppointment = all.getFirst();
        int realID = savedAppointment.getAppointmentID();
        DatabaseHelper.deleteAppointment(realID);
        List<Appointment> checkList = DatabaseHelper.getAllAppointments();
        Assertions.assertEquals(0, checkList.size(), "Appointment should have been deleted.");
    }

    @Test
    public void testUpdateAppointment() throws SQLException {
        // 1. Create original
        Date date = new Date();
        Appointment original = new Appointment("John Doe", "0711111111", "TM11TEST", "Audi", "A4", 2010, "", date, "Oil Change");
        DatabaseHelper.addAppointmentTransaction(original);

        // 2. Fetch it to get the ID
        Appointment saved = DatabaseHelper.getAllAppointments().getFirst();

        // 3. Modify it (Change Name, Phone, and Status)
        saved.setClientName("Johnathan Doe");
        saved.setClientPhone("0799999999");
        saved.setStatus(AppointmentStatus.DONE);

        // 4. Perform Update
        DatabaseHelper.updateAppointmentTransaction(saved);

        // 5. Fetch again and Verify
        Appointment updated = DatabaseHelper.getAllAppointments().getFirst();

        Assertions.assertEquals("Johnathan Doe", updated.getClientName(), "Client Name should update");
        Assertions.assertEquals("0799999999", updated.getClientPhone(), "Client Phone should update");
        Assertions.assertEquals(AppointmentStatus.DONE, updated.getStatus(), "Status should update");
    }

    @Test
    public void testSearchFunction() throws SQLException {
        Date now = new Date();
        // Add two different cars
        DatabaseHelper.addAppointmentTransaction(new Appointment("Client A", "0711111111", "TM01A", "Audi", "A4", 2010, "", now, "Fix"));
        DatabaseHelper.addAppointmentTransaction(new Appointment("Client B", "0722222222", "TM02B", "BMW", "X5", 2015, "", now, "Fix"));

        // 1. Search by Brand "Audi"
        List<Appointment> results = DatabaseHelper.searchAppointments("Audi", null, null, null);
        Assertions.assertEquals(1, results.size(), "Search for 'Audi' should return 1 result");
        Assertions.assertEquals("Audi", results.getFirst().getCarBrand());

        // 2. Search by License Plate partial "02"
        results = DatabaseHelper.searchAppointments("02", null, null, null);
        Assertions.assertEquals(1, results.size(), "Search for '02' should find the BMW");
        Assertions.assertEquals("TM02B", results.getFirst().getCarLicensePlate());

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
        Appointment pastAppointment = new Appointment("Lazy Client", "0700000000", "TM00OLD", "Ford", "Focus", 2005, "", yesterday, "Late");
        // (Default constructor sets status to SCHEDULED)
        DatabaseHelper.addAppointmentTransaction(pastAppointment);

        // 3. Run the Automation
        DatabaseHelper.autoUpdateStatuses();

        // 4. Verify it flipped to IN_PROGRESS
        Appointment result = DatabaseHelper.getAllAppointments().getFirst();
        Assertions.assertEquals(AppointmentStatus.IN_PROGRESS, result.getStatus(), "Past SCHEDULED appointment should become IN_PROGRESS");
    }

    @AfterAll
    public static void cleanUp() throws SQLException {
        DatabaseHelper.setDataBaseName("test.db");
        String sql = "DELETE FROM Appointments; DELETE FROM Cars; DELETE FROM Clients;";
        try(Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement()){
            stmt.execute(sql);
        }
    }
}

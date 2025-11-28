package com.autoshop.tests;

import com.autoshop.app.Appointment;
import com.autoshop.app.DatabaseHelper;
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
        Assertions.assertEquals("Test", testList.get(0).getClientName(), "Client name should have been added.");
    }

    @Test
    public void testDeleteAppointment() throws SQLException {
        Date testDate = new Date();
        Appointment test2 = new Appointment("Test2", "0712 345 678", "BZ02TST", "TestCar", "TestModel", 2000, "testPath2", testDate, "Test Description");
        DatabaseHelper.addAppointmentTransaction(test2);
        List<Appointment> all =  DatabaseHelper.getAllAppointments();
        Appointment savedAppointment = all.get(0);
        int realID = savedAppointment.getAppointmentID();
        DatabaseHelper.deleteAppointment(realID);
        List<Appointment> checkList = DatabaseHelper.getAllAppointments();
        Assertions.assertEquals(0, checkList.size(), "Appointment should have been deleted.");
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

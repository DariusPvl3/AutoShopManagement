package com.autoshop.app;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper {
    private static String URL = "jdbc:sqlite:appointments.db";

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite Driver not found!");
        }
        return DriverManager.getConnection(URL);
    }

    public static void setDataBaseName(String dbName){
        URL = "jdbc:sqlite:" + dbName;
    }

    // 1. CREATE TABLES
    public static void createNewTable() throws SQLException {
        String clientQuery = "CREATE TABLE IF NOT EXISTS Clients (" +
                "client_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "phone TEXT UNIQUE);";

        String carsQuery = "CREATE TABLE IF NOT EXISTS Cars (" +
                "car_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "client_id INTEGER NOT NULL, " +
                "license_plate TEXT NOT NULL UNIQUE, " +
                "brand_name TEXT, " +
                "model TEXT, " +
                "year INTEGER, " +
                "photo_path TEXT, " +
                "FOREIGN KEY(client_id) REFERENCES Clients(client_id));";

        String appointmentsQuery = "CREATE TABLE IF NOT EXISTS Appointments (" +
                "appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "car_id INTEGER NOT NULL, " +
                "date INTEGER, " +
                "problem TEXT, " +
                "status TEXT, " +
                "FOREIGN KEY(car_id) REFERENCES Cars(car_id));";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(clientQuery);
            stmt.execute(carsQuery);
            stmt.execute(appointmentsQuery);
            System.out.println("Database tables checked/created.");
        }
    }

    // 2. THE SMART INSERT (Transaction)
    // This ensures we don't duplicate Clients or Cars
    public static void addAppointmentTransaction(Appointment appt) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                int clientId = getOrCreateClient(conn, appt.getClientName(), appt.getClientPhone());

                int carId = getOrCreateCar(conn, clientId, appt.getCarLicensePlate(), appt.getCarBrand(), appt.getCarModel(), appt.getCarYear(), appt.getCarPhotoPath());

                String sql = "INSERT INTO Appointments(car_id, date, problem, status) VALUES(?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, carId);
                    pstmt.setLong(2, appt.getDate().getTime());
                    pstmt.setString(3, appt.getProblemDescription());
                    pstmt.setString(4, AppointmentStatus.SCHEDULED.name());
                    pstmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Helper: Find client by phone. If not found, create new.
    private static int getOrCreateClient(Connection conn, String name, String phone) throws SQLException {
        // 1. Check existence
        String checkSql = "SELECT client_id FROM Clients WHERE phone = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("client_id"); // Client exists!
            }
        }

        // 2. Create new
        String insertSql = "INSERT INTO Clients(name, phone) VALUES(?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.executeUpdate();
            return pstmt.getGeneratedKeys().getInt(1);
        }
    }

    // NEW: Get Today's appointments OR any appointment that is currently IN_PROGRESS
    public static List<Appointment> getDashboardAppointments(java.util.Date day) throws SQLException {
        List<Appointment> list = new ArrayList<>();

        // 1. Calculate Today's Range
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(day);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        String sql = getString(cal, start);

        return getAppointments(list, sql);
    }

    private static String getString(Calendar cal, long start) {
        long end = cal.getTimeInMillis();

        // 2. The Hybrid SQL
        String sql = "SELECT app.appointment_id, app.date, app.problem, app.status, " +
                "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                "client.name, client.phone " +
                "FROM Appointments app " +
                "JOIN Cars car ON app.car_id = car.car_id " +
                "JOIN Clients client ON car.client_id = client.client_id " +
                "WHERE (app.date >= " + start + " AND app.date <= " + end + ") " +
                "OR (app.status = 'IN_PROGRESS')"; // <--- The Magic Fix
        return sql;
    }

    // Helper: Find car by plate. If not found, create new.
    private static int getOrCreateCar(Connection conn, int clientId, String plate, String brand, String model, int year, String photoPath) throws SQLException {
        String checkSql = "SELECT car_id FROM Cars WHERE license_plate = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, plate);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("car_id");
        }

        // Insert to Database
        String insertSql = "INSERT INTO Cars(client_id, license_plate, brand_name, model, year, photo_path) VALUES(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, clientId);
            pstmt.setString(2, plate);
            pstmt.setString(3, brand);
            pstmt.setString(4, model);
            pstmt.setInt(5, year);
            pstmt.setString(6, photoPath);
            pstmt.executeUpdate();
            return pstmt.getGeneratedKeys().getInt(1);
        }
    }

    // 3. GET ALL
    public static List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT app.appointment_id, app.date, app.problem, app.status, " +
                "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                "client.name, client.phone " +
                "FROM Appointments app " +
                "JOIN Cars car ON app.car_id = car.car_id " +
                "JOIN Clients client ON car.client_id = client.client_id";

        return getAppointments(list, sql);
    }

    // 4. DELETE
    public static void deleteAppointment(int id) throws SQLException {
        String sql = "DELETE FROM Appointments WHERE appointment_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public static void updateAppointmentTransaction(Appointment appt) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                // 1. Appointment
                String sqlAppt = "UPDATE Appointments SET date=?, problem=?, status=? WHERE appointment_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlAppt)) {
                    pstmt.setLong(1, appt.getDate().getTime());
                    pstmt.setString(2, appt.getProblemDescription());
                    pstmt.setString(3, appt.getStatus().name());
                    pstmt.setInt(4, appt.getAppointmentID());
                    pstmt.executeUpdate();
                }

                // 2. Car
                String sqlCar = "UPDATE Cars SET license_plate=?, brand_name=?, model=?, year=?, photo_path=? WHERE car_id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlCar)) {
                    pstmt.setString(1, appt.getCarLicensePlate());
                    pstmt.setString(2, appt.getCarBrand());
                    pstmt.setString(3, appt.getCarModel());
                    pstmt.setInt(4, appt.getCarYear());       // NEW
                    pstmt.setString(5, appt.getCarPhotoPath()); // NEW
                    pstmt.setInt(6, appt.getCarID());
                    pstmt.executeUpdate();
                }

                // 3. Client
                String sqlClient = "UPDATE Clients SET name=?, phone=? WHERE client_id = (SELECT client_id FROM Cars WHERE car_id=?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlClient)) {
                    pstmt.setString(1, appt.getClientName());
                    pstmt.setString(2, appt.getClientPhone());
                    pstmt.setInt(3, appt.getCarID());
                    pstmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // 5. GET FILTERED
    public static List<Appointment> getAppointmentsForDay(java.util.Date day) throws SQLException {
        List<Appointment> list = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(day);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long end = cal.getTimeInMillis();

        String sql = "SELECT app.appointment_id, app.date, app.problem, app.status, " +
                "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                "client.name, client.phone " +
                "FROM Appointments app " +
                "JOIN Cars car ON app.car_id = car.car_id " +
                "JOIN Clients client ON car.client_id = client.client_id " +
                "WHERE app.date >= " + start + " AND app.date <= " + end;

        return getAppointments(list, sql);
    }

    public static List<Appointment> getActiveAppointments() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT app.appointment_id, app.date, app.problem, app.status, " +
                "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                "client.name, client.phone " +
                "FROM Appointments app " +
                "JOIN Cars car ON app.car_id = car.car_id " +
                "JOIN Clients client ON car.client_id = client.client_id " +
                "WHERE app.status = 'IN_PROGRESS'";

        return getAppointments(list, sql);
    }

    private static List<Appointment> getAppointments(List<Appointment> list, String sql) throws SQLException {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Appointment appt = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("car_id"),
                        new Date(rs.getLong("date")),
                        rs.getString("problem"),
                        AppointmentStatus.valueOf(rs.getString("status")),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("license_plate"),
                        rs.getString("brand_name"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("photo_path")
                );
                list.add(appt);
            }
        }
        return list;
    }

    // AUTOMATION: Auto-start appointments if time has passed
    public static void autoUpdateStatuses() throws SQLException {
        long now = System.currentTimeMillis();

        // SQL: Set status to IN_PROGRESS
        // WHERE the date is in the past (<= now)
        // AND the status is currently 'SCHEDULED' (Don't touch DONE or CANCELLED jobs)
        String sql = "UPDATE Appointments SET status = 'IN_PROGRESS' WHERE date <= ? AND status = 'SCHEDULED'";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, now);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Auto-updated " + rowsUpdated + " appointments to IN_PROGRESS.");
            }
        }
    }
}
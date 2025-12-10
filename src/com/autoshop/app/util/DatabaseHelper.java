package com.autoshop.app.util;

import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.model.Car;
import com.autoshop.app.model.Client;

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
        String clientQuery = "CREATE TABLE IF NOT EXISTS Clients ("
                +"client_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, " + "phone TEXT UNIQUE);";

        String carsQuery = "CREATE TABLE IF NOT EXISTS Cars (" + "car_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "client_id INTEGER NOT NULL, " + "license_plate TEXT NOT NULL UNIQUE, " + "brand_name TEXT, "
                + "model TEXT, " + "year INTEGER, " + "photo_path TEXT, " + "FOREIGN KEY(client_id) REFERENCES Clients(client_id));";

        String appointmentsQuery = "CREATE TABLE IF NOT EXISTS Appointments (" + "appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "car_id INTEGER NOT NULL, "
                + "client_id INTEGER NOT NULL, "
                + "date INTEGER, " + "problem TEXT, " + "repairs TEXT," + "parts_used TEXT, "
                + "observations TEXT, " + "status TEXT, "
                + "FOREIGN KEY(car_id) REFERENCES Cars(car_id), "
                + "FOREIGN KEY(client_id) REFERENCES Clients(client_id));";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(clientQuery);
            stmt.execute(carsQuery);
            stmt.execute(appointmentsQuery);
        }
    }

    // 2. THE SMART INSERT (Transaction)
    // This ensures we don't duplicate Clients or Cars
    public static void addAppointmentTransaction(Appointment appointment) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                // 1. Get/Create the Client
                int clientId = getOrCreateClient(conn, appointment.getClientName(), appointment.getClientPhone());

                // 2. Get/Create the Car
                int carId = getOrCreateCar(conn, clientId, appointment.getCarLicensePlate(), appointment.getCarBrand(), appointment.getCarModel(), appointment.getCarYear(), appointment.getCarPhotoPath());

                // 3. Create Appointment linking THIS Client and THAT Car
                String sql = "INSERT INTO Appointments(car_id, client_id, date, problem, repairs, parts_used, observations, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setInt(1, carId);
                    preparedStatement.setInt(2, clientId);
                    preparedStatement.setLong(3, appointment.getDate().getTime());
                    preparedStatement.setString(4, appointment.getProblemDescription());
                    preparedStatement.setString(5, appointment.getRepairs());
                    preparedStatement.setString(6, appointment.getPartsUsed());
                    preparedStatement.setString(7, appointment.getObservations());
                    preparedStatement.setString(8, AppointmentStatus.SCHEDULED.name());
                    preparedStatement.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Helper: Find client by phone. If not found, create new.
    private static int getOrCreateClient(Connection conn, String name, String rawPhone) throws SQLException {
        // 1. Normalize the phone (clean spaces/dashes)
        String phone = com.autoshop.app.util.Utils.normalizePhone(rawPhone);

        // 2. Check existence BY PHONE (Unique Identifier)
        String checkSql = "SELECT client_id, name FROM Clients WHERE phone = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Found a client with this phone.
                // Optional: You could update their name here if it changed.
                return rs.getInt("client_id");
            }
        }

        // 3. If phone not found, Create New Client (even if name matches someone else)
        String insertSql = "INSERT INTO Clients(name, phone) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.executeUpdate();
            return ps.getGeneratedKeys().getInt(1);
        }
    }

    // Get Today's appointments OR any appointment that is currently IN_PROGRESS
    public static List<Appointment> getDashboardAppointments(java.util.Date day) throws SQLException {
        List<Appointment> list = new ArrayList<>();

        // Calculate Today's Range
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

        // The Hybrid SQL
        return "SELECT app.appointment_id, app.date, app.problem, app.repairs, app.parts_used, app.observations, app.status, " +
                "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                "client.name, client.phone " +
                "FROM Appointments app " +
                "JOIN Cars car ON app.car_id = car.car_id " +
                "JOIN Clients client ON car.client_id = client.client_id " +
                "WHERE (app.date >= " + start + " AND app.date <= " + end + ") " +
                "OR (app.status = 'IN_PROGRESS')";
    }

    // Helper: Find car by plate. If not found, create new.
    private static int getOrCreateCar(Connection conn, int clientId, String plate, String brand, String model, int year, String photoPath) throws SQLException {
        String checkSql = "SELECT car_id FROM Cars WHERE license_plate = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(checkSql)) {
            preparedStatement.setString(1, plate);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) return rs.getInt("car_id");
        }

        // Insert to Database
        String insertSql = "INSERT INTO Cars(client_id, license_plate, brand_name, model, year, photo_path) VALUES(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, clientId);
            preparedStatement.setString(2, plate);
            preparedStatement.setString(3, brand);
            preparedStatement.setString(4, model);
            preparedStatement.setInt(5, year);
            preparedStatement.setString(6, photoPath);
            preparedStatement.executeUpdate();
            return preparedStatement.getGeneratedKeys().getInt(1);
        }
    }

    // 3. GET ALL
    public static List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT app.appointment_id, app.date, app.problem, app.repairs, app.parts_used, app.observations, app.status, " +
                "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                "client.name, client.phone " +
                "FROM Appointments app " +
                "JOIN Cars car ON app.car_id = car.car_id " +
                "JOIN Clients client ON app.client_id = client.client_id";

        return getAppointments(list, sql);
    }

    // 4. DELETE
    public static void deleteAppointment(int id) throws SQLException {
        String sql = "DELETE FROM Appointments WHERE appointment_id = ?";
        try (Connection conn = connect(); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }

    public static void updateAppointmentTransaction(Appointment appointment) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Start Transaction
            try {
                // 1. Resolve the Client ID
                // Instead of overwriting the old client's data, we find/create the correct client
                // for the currently entered Name/Phone.
                int clientId = getOrCreateClient(conn, appointment.getClientName(), appointment.getClientPhone());

                // 2. Update Appointment (Now including the link to the correct Client)
                // Note: We added client_id to the SET list
                String sqlAppointment = "UPDATE Appointments SET client_id=?, date=?, problem=?, repairs=?, parts_used=?, observations=?, status=? WHERE appointment_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlAppointment)) {
                    ps.setInt(1, clientId);
                    ps.setLong(2, appointment.getDate().getTime());
                    ps.setString(3, appointment.getProblemDescription());
                    ps.setString(4, appointment.getRepairs());
                    ps.setString(5, appointment.getPartsUsed());
                    ps.setString(6, appointment.getObservations());
                    ps.setString(7, appointment.getStatus().name());
                    ps.setInt(8, appointment.getAppointmentID());
                    ps.executeUpdate();
                }

                // 3. Update Car Details
                String sqlCar = "UPDATE Cars SET license_plate=?, brand_name=?, model=?, year=?, photo_path=? WHERE car_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlCar)) {
                    ps.setString(1, appointment.getCarLicensePlate());
                    ps.setString(2, appointment.getCarBrand());
                    ps.setString(3, appointment.getCarModel());
                    ps.setInt(4, appointment.getCarYear());
                    ps.setString(5, appointment.getCarPhotoPath());
                    ps.setInt(6, appointment.getCarID());

                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        // This helps us catch if the ID was wrong
                        System.err.println("CRITICAL ERROR: No car found with ID " + appointment.getCarID());
                        throw new SQLException("Car record missing! ID: " + appointment.getCarID());
                    }
                }

                conn.commit(); // Apply changes
            } catch (SQLException e) {
                conn.rollback(); // Undo everything if ANY step failed
                e.printStackTrace();
                throw e; // Re-throw so the UI shows the error dialog
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

        String sql = "SELECT app.appointment_id, app.date, app.problem, app.repairs, app.parts_used, app.observations, app.status, " +
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
        String sql = "SELECT app.appointment_id, app.date, app.problem, app.repairs, app.parts_used, app.observations, app.status, " +
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
                Appointment appointment = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("car_id"),
                        new Date(rs.getLong("date")),
                        rs.getString("problem"),
                        rs.getString("repairs"),
                        rs.getString("parts_used"),
                        rs.getString("observations"),
                        AppointmentStatus.valueOf(rs.getString("status")),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("license_plate"),
                        rs.getString("brand_name"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("photo_path")
                );
                list.add(appointment);
            }
        }
        return list;
    }

    // AUTOMATION: Auto-start appointments if time has passed
    public static void autoUpdateStatuses() throws SQLException {
        long now = System.currentTimeMillis();
        String sql = "UPDATE Appointments SET status = 'IN_PROGRESS' WHERE date <= ? AND status = 'SCHEDULED'";

        try (Connection conn = connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setLong(1, now);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Auto-updated " + rowsUpdated + " appointments to IN_PROGRESS.");
            }
        }
    }

    public static List<Appointment> searchAppointments(String rawKeywords, AppointmentStatus status, java.util.Date from, java.util.Date to) throws SQLException {
        List<Appointment> list = new ArrayList<>();

        // Base Query
        StringBuilder sql = new StringBuilder(
                "SELECT app.appointment_id, app.date, app.problem, app.repairs, app.parts_used, app.observations, app.status, " +
                        "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                        "client.name, client.phone " +
                        "FROM Appointments app " +
                        "JOIN Cars car ON app.car_id = car.car_id " +
                        "JOIN Clients client ON car.client_id = client.client_id " +
                        "WHERE 1=1 "
        );

        // 1. Handle Status Filter
        if (status != null) sql.append("AND app.status = ? ");

        // 2. Handle Date Filters
        if (from != null) sql.append("AND app.date >= ? ");
        if (to != null)   sql.append("AND app.date <= ? ");

        // 3. Handle Smart Keywords (Split by space)
        String[] tokens = null;
        if (rawKeywords != null && !rawKeywords.isEmpty()) {
            tokens = rawKeywords.trim().split("\\s+"); // Split by whitespace

            for (int i = 0; i < tokens.length; i++) {
                // For each word, we require a match in AT LEAST ONE of the fields
                // AND ( ... OR ... OR ...)
                sql.append("AND (")
                        .append("client.name LIKE ? OR ")
                        .append("client.phone LIKE ? OR ")
                        .append("car.brand_name LIKE ? OR ")
                        .append("car.model LIKE ? OR ")
                        .append("car.year LIKE ? OR ")
                        .append("app.problem LIKE ? OR ")
                        .append("app.repairs LIKE ? OR ")
                        .append("app.parts_used LIKE ? OR ")
                        .append("app.observations LIKE ? OR ")
                        // Check raw input against plate
                        .append("car.license_plate LIKE ? OR ")
                        // Check formatted input against plate (TM12ABC -> matches TM-12-ABC)
                        .append("car.license_plate LIKE ? ")
                        .append(") ");
            }
        }

        try (Connection conn = connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql.toString())) {
            int index = 1;
            // Fill Status
            if (status != null)
                preparedStatement.setString(index++, status.name());
            // Fill Dates
            if (from != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(from);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                preparedStatement.setLong(index++, cal.getTimeInMillis());
            }
            if (to != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(to);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                preparedStatement.setLong(index++, cal.getTimeInMillis());
            }
            // Fill Keywords
            if (tokens != null) {
                for (String token : tokens) {
                    String pattern = "%" + token + "%";
                    // Client Name
                    preparedStatement.setString(index++, pattern);
                    // Phone
                    preparedStatement.setString(index++, pattern);
                    // Brand
                    preparedStatement.setString(index++, pattern);
                    // Model
                    preparedStatement.setString(index++, pattern);
                    // Year
                    preparedStatement.setString(index++, pattern);
                    // Problem
                    preparedStatement.setString(index++, pattern);
                    // Repairs
                    preparedStatement.setString(index++, pattern);
                    // Parts used
                    preparedStatement.setString(index++, pattern);
                    // Observations
                    preparedStatement.setString(index++, pattern);
                    // Plate (Raw)
                    preparedStatement.setString(index++, pattern);
                    // Plate (Formatted)
                    // If user types "tm12abc", Utils formats to "TM-12-ABC"
                    // If user types "Audi", Utils returns "AUDI" (no hyphens), which is fine
                    String formattedPlate = Utils.formatPlate(token);
                    String patternFormatted = "%" + formattedPlate + "%";
                    preparedStatement.setString(index++, patternFormatted);
                }
            }
            // Execute
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = new Appointment(
                            rs.getInt("appointment_id"),
                            rs.getInt("car_id"),
                            new Date(rs.getLong("date")),
                            rs.getString("problem"),
                            rs.getString("repairs"),
                            rs.getString("parts_used"),
                            rs.getString("observations"),
                            AppointmentStatus.valueOf(rs.getString("status").toUpperCase()),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("license_plate"),
                            rs.getString("brand_name"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            rs.getString("photo_path")
                    );
                    list.add(appointment);
                }
            }
        }
        return list;
    }

    // PARTIAL SEARCHES
    public static List<Client> getClientsByName(String partialName) throws SQLException {
        List<Client> clientList = new ArrayList<>();
        String sql = "SELECT * FROM Clients WHERE name LIKE ?";

        try (Connection conn = connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, "%" + partialName + "%");
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Client client = new Client(
                        rs.getInt("client_id"),
                        rs.getString("name"),
                        rs.getString("phone"));
                clientList.add(client);
            }
        }
        return clientList;
    }

    public static List<Client> getClientsByPhone(String partialPhone) throws SQLException {
        List<Client> clientList = new ArrayList<>();
        String sql = "SELECT * FROM Clients WHERE phone LIKE ?";
        try (Connection conn = connect();
        PreparedStatement preparedStatement = conn.prepareStatement(sql);) {
            preparedStatement.setString(1, partialPhone + "%");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Client client = new Client(
                        rs.getInt("client_id"),
                        rs.getString("name"),
                        rs.getString("phone")
                );
                clientList.add(client);
            }
        };
        return clientList;
    }

    public static List<Car> getCarModelsByBrand(String brand, String partialModel) throws SQLException {
        List<Car> carList = new ArrayList<>();
        String sql = "SELECT * FROM Cars WHERE brand_name = ? AND model LIKE ?";
        try (Connection conn = connect();
        PreparedStatement preparedStatement = conn.prepareStatement(sql);) {
            preparedStatement.setString(1, brand);
            preparedStatement.setString(2, partialModel + "%");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Car car = new Car(
                        rs.getInt("car_id"),
                        rs.getInt("client_id"),
                        rs.getString("license_plate"),
                        rs.getString("brand_name"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("photo_path")
                );
                carList.add(car);
            }
        }
        return carList;
    }

    public static List<Car> getCarDetailsByPlate(String plate) throws SQLException {
        List<Car> carList = new ArrayList<>();
        String sql = "SELECT * FROM Cars WHERE license_plate LIKE ?";
        try (Connection conn = connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql);) {
            preparedStatement.setString(1, plate + "%");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Car car = new Car(
                        rs.getInt("car_id"),
                        rs.getInt("client_id"),
                        rs.getString("license_plate"),
                        rs.getString("brand_name"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("photo_path")
                );
                carList.add(car);
            }
        }
        return carList;
    }
}
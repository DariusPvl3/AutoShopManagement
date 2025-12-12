package com.autoshop.app.util;

import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.model.Car;
import com.autoshop.app.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static String URL = "jdbc:sqlite:appointments.db";

    // =================================================================================================================
    //  SECTION 1: CONNECTION & SETUP
    // =================================================================================================================

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

    // =================================================================================================================
    //  SECTION 2: TRANSACTIONAL OPERATIONS (Add / Update)
    // =================================================================================================================

    public static void addAppointmentTransaction(Appointment appointment) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Start Transaction
            try {
                // 1. Get/Create the Client ( Robust Check )
                int clientId = getOrCreateClient(conn, appointment.getClientName(), appointment.getClientPhone());

                // 2. Get/Create the Car ( Robust Check )
                int carId = getOrCreateCar(conn, clientId,
                        appointment.getCarLicensePlate(),
                        appointment.getCarBrand(),
                        appointment.getCarModel(),
                        appointment.getCarYear(),
                        appointment.getCarPhotoPath());

                // 3. Create Appointment linking THIS Client and THAT Car
                String sql = "INSERT INTO Appointments(car_id, client_id, date, problem, repairs, parts_used, observations, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, carId);
                    ps.setInt(2, clientId);
                    ps.setLong(3, appointment.getDate().getTime());
                    ps.setString(4, appointment.getProblemDescription());
                    ps.setString(5, appointment.getRepairs());
                    ps.setString(6, appointment.getPartsUsed());
                    ps.setString(7, appointment.getObservations());
                    ps.setString(8, AppointmentStatus.SCHEDULED.name());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static void updateAppointmentTransaction(Appointment appointment) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Start Transaction
            try {
                // 1. Resolve the Client ID (Relink logic: find existing or create new)
                int clientId = getOrCreateClient(conn, appointment.getClientName(), appointment.getClientPhone());

                // 2. Resolve the Car ID (Relink logic: find existing or create new)
                // Fixes unique constraint crash when switching cars
                int carId = getOrCreateCar(conn, clientId,
                        appointment.getCarLicensePlate(),
                        appointment.getCarBrand(),
                        appointment.getCarModel(),
                        appointment.getCarYear(),
                        appointment.getCarPhotoPath());

                // 3. Update Appointment to point to the correct Client and Car IDs
                String sqlAppointment = "UPDATE Appointments SET client_id=?, car_id=?, date=?, problem=?, repairs=?, parts_used=?, observations=?, status=? WHERE appointment_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlAppointment)) {
                    ps.setInt(1, clientId);
                    ps.setInt(2, carId);
                    ps.setLong(3, appointment.getDate().getTime());
                    ps.setString(4, appointment.getProblemDescription());
                    ps.setString(5, appointment.getRepairs());
                    ps.setString(6, appointment.getPartsUsed());
                    ps.setString(7, appointment.getObservations());
                    ps.setString(8, appointment.getStatus().name());
                    ps.setInt(9, appointment.getAppointmentID());
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                throw e;
            }
        }
    }

    public static void deleteAppointment(int id) throws SQLException {
        String sql = "DELETE FROM Appointments WHERE appointment_id = ?";
        try (Connection conn = connect(); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }

    // =================================================================================================================
    //  SECTION 3: DATA RETRIEVAL (Getters)
    // =================================================================================================================

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
        long end = cal.getTimeInMillis();

        String sql = "SELECT app.appointment_id, app.date, app.problem, app.repairs, app.parts_used, app.observations, app.status, " +
                "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                "client.name, client.phone " +
                "FROM Appointments app " +
                "JOIN Cars car ON app.car_id = car.car_id " +
                "JOIN Clients client ON car.client_id = client.client_id " +
                "WHERE (app.date >= " + start + " AND app.date <= " + end + ") " +
                "OR (app.status = 'IN_PROGRESS')";

        return getAppointments(list, sql);
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

        // 3. Handle Smart Keywords
        String[] tokens = null;
        if (rawKeywords != null && !rawKeywords.isEmpty()) {
            tokens = rawKeywords.trim().split("\\s+");
            for (int i = 0; i < tokens.length; i++) {
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
                        .append("car.license_plate LIKE ? OR ")
                        .append("car.license_plate LIKE ? ")
                        .append(") ");
            }
        }

        try (Connection conn = connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql.toString())) {
            int index = 1;
            // Fill Status
            if (status != null) preparedStatement.setString(index++, status.name());
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
                    // Loop to fill ? for each field
                    for(int k=0; k<10; k++) preparedStatement.setString(index++, pattern);

                    // Plate Formatted
                    String formattedPlate = com.autoshop.app.util.Utils.formatPlate(token);
                    String patternFormatted = "%" + formattedPlate + "%";
                    preparedStatement.setString(index++, patternFormatted);
                }
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAppointment(rs));
                }
            }
        }
        return list;
    }

    // =================================================================================================================
    //  SECTION 4: AUTOCOMPLETE HELPERS
    // =================================================================================================================

    public static List<Client> getClientsByName(String partialName) throws SQLException {
        List<Client> clientList = new ArrayList<>();
        String sql = "SELECT * FROM Clients WHERE name LIKE ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + partialName + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) clientList.add(new Client(rs.getInt("client_id"), rs.getString("name"), rs.getString("phone")));
        }
        return clientList;
    }

    public static List<Client> getClientsByPhone(String partialPhone) throws SQLException {
        List<Client> clientList = new ArrayList<>();
        String sql = "SELECT * FROM Clients WHERE phone LIKE ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, partialPhone + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) clientList.add(new Client(rs.getInt("client_id"), rs.getString("name"), rs.getString("phone")));
        }
        return clientList;
    }

    public static List<Car> getCarModelsByBrand(String brand, String partialModel) throws SQLException {
        List<Car> carList = new ArrayList<>();
        String sql = "SELECT * FROM Cars WHERE brand_name = ? AND model LIKE ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, brand);
            ps.setString(2, partialModel + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) carList.add(extractCar(rs));
        }
        return carList;
    }

    public static List<Car> getCarDetailsByPlate(String plate) throws SQLException {
        List<Car> carList = new ArrayList<>();
        String sql = "SELECT * FROM Cars WHERE license_plate LIKE ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) carList.add(extractCar(rs));
        }
        return carList;
    }

    // =================================================================================================================
    //  SECTION 5: INTERNAL HELPERS (Private)
    // =================================================================================================================

    // Helper: Find client by phone. If found, update name. If not found, create new.
    private static int getOrCreateClient(Connection conn, String name, String rawPhone) throws SQLException {
        // 1. Handle Missing Phone
        if (rawPhone == null) {
            // We cannot search by phone, so we ALWAYS insert a new client.
            // (We assume clients without phones are distinct or temporary entries)
            return insertClient(conn, name, null);
        }

        String phone = com.autoshop.app.util.Utils.normalizePhone(rawPhone);

        // 2. Check existence BY PHONE
        String checkSql = "SELECT client_id, name FROM Clients WHERE phone = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("client_id");
            }
        }

        // 3. Not found? Create
        return insertClient(conn, name, phone);
    }

    private static int insertClient(Connection conn, String name, String phone) throws SQLException {
        String insertSql = "INSERT INTO Clients(name, phone) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            // setString allows null, which maps to SQL NULL
            ps.setString(2, phone);
            ps.executeUpdate();
            return ps.getGeneratedKeys().getInt(1);
        }
    }

    // Helper: Find car by plate. If found, update details. If not found, create new.
    private static int getOrCreateCar(Connection conn, int clientId, String plate, String brand, String model, int year, String photoPath) throws SQLException {
        int carId = -1;

        // Check existence
        String checkSql = "SELECT car_id FROM Cars WHERE license_plate = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) carId = rs.getInt("car_id");
        }

        if (carId != -1) {
            // Update Details & Link to Client
            String updateSql = "UPDATE Cars SET brand_name=?, model=?, year=?, photo_path=?, client_id=? WHERE car_id=?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, brand);
                ps.setString(2, model);
                ps.setInt(3, year);
                ps.setString(4, photoPath);
                ps.setInt(5, clientId);
                ps.setInt(6, carId);
                ps.executeUpdate();
            }
            return carId;
        } else {
            // Create New
            String insertSql = "INSERT INTO Cars(client_id, license_plate, brand_name, model, year, photo_path) VALUES(?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, clientId);
                ps.setString(2, plate);
                ps.setString(3, brand);
                ps.setString(4, model);
                ps.setInt(5, year);
                ps.setString(6, photoPath);
                ps.executeUpdate();
                return ps.getGeneratedKeys().getInt(1);
            }
        }
    }

    private static List<Appointment> getAppointments(List<Appointment> list, String sql) throws SQLException {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractAppointment(rs));
            }
        }
        return list;
    }

    private static Appointment extractAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
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
    }

    private static Car extractCar(ResultSet rs) throws SQLException {
        return new Car(
                rs.getInt("car_id"),
                rs.getInt("client_id"),
                rs.getString("license_plate"),
                rs.getString("brand_name"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("photo_path")
        );
    }

    public static void autoUpdateStatuses() throws SQLException {
        long now = System.currentTimeMillis();
        String sql = "UPDATE Appointments SET status = 'IN_PROGRESS' WHERE date <= ? AND status = 'SCHEDULED'";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, now);
            int rows = ps.executeUpdate();
            if(rows > 0) System.out.println("Auto-updated " + rows + " appointments.");
        }
    }
}
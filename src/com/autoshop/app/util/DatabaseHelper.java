package com.autoshop.app.util;

import com.autoshop.app.model.*; // Imports Appointment, Client, Car, Part, Supplier

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
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

        // UPDATED: Removed 'parts_used' column
        String appointmentsQuery = "CREATE TABLE IF NOT EXISTS Appointments (" + "appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "car_id INTEGER NOT NULL, "
                + "client_id INTEGER NOT NULL, "
                + "date INTEGER, " + "problem TEXT, " + "repairs TEXT, " // <--- parts_used removed
                + "observations TEXT, " + "status TEXT, "
                + "FOREIGN KEY(car_id) REFERENCES Cars(car_id), "
                + "FOREIGN KEY(client_id) REFERENCES Clients(client_id));";

        // NEW: Parts Table
        String partsQuery = "CREATE TABLE IF NOT EXISTS Parts (" +
                "part_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER NOT NULL, " +
                "code TEXT, " +
                "name TEXT, " +
                "supplier TEXT, " +
                "FOREIGN KEY(appointment_id) REFERENCES Appointments(appointment_id) ON DELETE CASCADE);";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(clientQuery);
            stmt.execute(carsQuery);
            stmt.execute(appointmentsQuery);
            stmt.execute(partsQuery); // <--- Execute new table
        }
    }

    // =================================================================================================================
    //  SECTION 2: TRANSACTIONAL OPERATIONS (Add / Update)
    // =================================================================================================================

    public static void addAppointmentTransaction(Appointment appointment) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Start Transaction
            try {
                // 1. Get/Create Client
                int clientId = getOrCreateClient(conn, appointment.getClientName(), appointment.getClientPhone());

                // 2. Get/Create Car
                int carId = getOrCreateCar(conn, clientId,
                        appointment.getCarLicensePlate(),
                        appointment.getCarBrand(),
                        appointment.getCarModel(),
                        appointment.getCarYear(),
                        appointment.getCarPhotoPath());

                // 3. Insert Appointment (No parts string)
                String sql = "INSERT INTO Appointments(car_id, client_id, date, problem, repairs, observations, status) VALUES(?, ?, ?, ?, ?, ?, ?)";
                int appointmentId = -1;

                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, carId);
                    ps.setInt(2, clientId);
                    ps.setLong(3, appointment.getDate().getTime());
                    ps.setString(4, appointment.getProblemDescription());
                    ps.setString(5, appointment.getRepairs());
                    ps.setString(6, appointment.getObservations());
                    ps.setString(7, AppointmentStatus.SCHEDULED.name());
                    ps.executeUpdate();
                    appointmentId = ps.getGeneratedKeys().getInt(1);
                }

                // 4. Insert Parts List (The New Logic)
                if (appointment.getPartList() != null && !appointment.getPartList().isEmpty()) {
                    String partSql = "INSERT INTO Parts(appointment_id, code, name, supplier) VALUES(?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(partSql)) {
                        for (Part p : appointment.getPartList()) {
                            ps.setInt(1, appointmentId);
                            ps.setString(2, p.getCode());
                            ps.setString(3, p.getName());
                            ps.setString(4, p.getSupplier().name());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
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
                // 1. Resolve Client
                int clientId = getOrCreateClient(conn, appointment.getClientName(), appointment.getClientPhone());

                // 2. Resolve Car
                int carId = getOrCreateCar(conn, clientId,
                        appointment.getCarLicensePlate(),
                        appointment.getCarBrand(),
                        appointment.getCarModel(),
                        appointment.getCarYear(),
                        appointment.getCarPhotoPath());

                // 3. Update Appointment Table
                String sqlAppointment = "UPDATE Appointments SET client_id=?, car_id=?, date=?, problem=?, repairs=?, observations=?, status=? WHERE appointment_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlAppointment)) {
                    ps.setInt(1, clientId);
                    ps.setInt(2, carId);
                    ps.setLong(3, appointment.getDate().getTime());
                    ps.setString(4, appointment.getProblemDescription());
                    ps.setString(5, appointment.getRepairs());
                    ps.setString(6, appointment.getObservations());
                    ps.setString(7, appointment.getStatus().name());
                    ps.setInt(8, appointment.getAppointmentID());
                    ps.executeUpdate();
                }

                // 4. Update Parts (Wipe and Replace Strategy)
                // A. Delete old parts for this appointment
                String deleteParts = "DELETE FROM Parts WHERE appointment_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteParts)) {
                    ps.setInt(1, appointment.getAppointmentID());
                    ps.executeUpdate();
                }

                // B. Insert current parts
                if (appointment.getPartList() != null && !appointment.getPartList().isEmpty()) {
                    String partSql = "INSERT INTO Parts(appointment_id, code, name, supplier) VALUES(?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(partSql)) {
                        for (Part p : appointment.getPartList()) {
                            ps.setInt(1, appointment.getAppointmentID());
                            ps.setString(2, p.getCode());
                            ps.setString(3, p.getName());
                            ps.setString(4, p.getSupplier().name());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
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
        // Note: Because we used ON DELETE CASCADE in the Parts table creation,
        // deleting the appointment automatically deletes the parts.
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
        String sql = "SELECT app.appointment_id, app.date, app.problem, app.repairs, app.observations, app.status, " +
                "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                "client.name, client.phone " +
                "FROM Appointments app " +
                "JOIN Cars car ON app.car_id = car.car_id " +
                "JOIN Clients client ON app.client_id = client.client_id";

        return getAppointments(list, sql);
    }

    public static List<Appointment> getActiveAppointments() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT app.appointment_id, app.date, app.problem, app.repairs, app.observations, app.status, " +
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

        String sql = "SELECT app.appointment_id, app.date, app.problem, app.repairs, app.observations, app.status, " +
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

        // UPDATED QUERY: Use DISTINCT because LEFT JOIN on Parts will duplicate rows if multiple parts exist
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT app.appointment_id, app.date, app.problem, app.repairs, app.observations, app.status, " +
                        "car.car_id, car.license_plate, car.brand_name, car.model, car.year, car.photo_path, " +
                        "client.name, client.phone " +
                        "FROM Appointments app " +
                        "JOIN Cars car ON app.car_id = car.car_id " +
                        "JOIN Clients client ON car.client_id = client.client_id " +
                        "LEFT JOIN Parts p ON app.appointment_id = p.appointment_id " + // <--- Join Parts
                        "WHERE 1=1 "
        );

        if (status != null) sql.append("AND app.status = ? ");
        if (from != null) sql.append("AND app.date >= ? ");
        if (to != null)   sql.append("AND app.date <= ? ");

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
                        .append("app.observations LIKE ? OR ")
                        // Removed app.parts_used, added Parts table search:
                        .append("p.code LIKE ? OR ")
                        .append("p.name LIKE ? OR ")
                        .append("p.supplier LIKE ? OR ")
                        // Car Plates
                        .append("car.license_plate LIKE ? OR ")
                        .append("car.license_plate LIKE ? ")
                        .append(") ");
            }
        }

        try (Connection conn = connect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (status != null) preparedStatement.setString(index++, status.name());
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
            if (tokens != null) {
                for (String token : tokens) {
                    String pattern = "%" + token + "%";
                    // 8 basic text fields (name, phone, brand, model, year, problem, repairs, obs)
                    for(int k=0; k<8; k++) preparedStatement.setString(index++, pattern);

                    // 3 New Part fields (code, name, supplier)
                    preparedStatement.setString(index++, pattern);
                    preparedStatement.setString(index++, pattern);
                    preparedStatement.setString(index++, pattern);

                    // 2 Plate fields (raw, formatted)
                    preparedStatement.setString(index++, pattern);
                    String formattedPlate = com.autoshop.app.util.Utils.formatPlate(token);
                    preparedStatement.setString(index++, "%" + formattedPlate + "%");
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
    // (These remain exactly the same as before)

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
        if (rawPhone == null) {
            return insertClient(conn, name, null);
        }
        String phone = com.autoshop.app.util.Utils.normalizePhone(rawPhone);
        String checkSql = "SELECT client_id, name FROM Clients WHERE phone = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Found: Update name
                String updateSql = "UPDATE Clients SET name=? WHERE client_id=?";
                try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                    up.setString(1, com.autoshop.app.util.Utils.toTitleCase(name));
                    up.setInt(2, rs.getInt("client_id"));
                    up.executeUpdate();
                }
                return rs.getInt("client_id");
            }
        }
        return insertClient(conn, name, phone);
    }

    private static int insertClient(Connection conn, String name, String phone) throws SQLException {
        String insertSql = "INSERT INTO Clients(name, phone) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, com.autoshop.app.util.Utils.toTitleCase(name));
            ps.setString(2, phone);
            ps.executeUpdate();
            return ps.getGeneratedKeys().getInt(1);
        }
    }

    // Helper: Find car by plate. If found, update details. If not found, create new.
    private static int getOrCreateCar(Connection conn, int clientId, String plate, String brand, String model, int year, String photoPath) throws SQLException {
        int carId = -1;
        String checkSql = "SELECT car_id FROM Cars WHERE license_plate = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) carId = rs.getInt("car_id");
        }

        if (carId != -1) {
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

    // Helper to fetch the parts for a specific appointment
    private static List<Part> getPartsForAppointment(int appointmentId) {
        List<Part> parts = new ArrayList<>();
        String sql = "SELECT * FROM Parts WHERE appointment_id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                parts.add(new Part(
                        rs.getInt("part_id"),
                        rs.getInt("appointment_id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        Supplier.valueOf(rs.getString("supplier"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log error but return empty list to avoid breaking UI
        }
        return parts;
    }

    private static Appointment extractAppointment(ResultSet rs) throws SQLException {
        int appId = rs.getInt("appointment_id");
        // FETCH PARTS HERE
        List<Part> parts = getPartsForAppointment(appId);

        return new Appointment(
                appId,
                rs.getInt("car_id"),
                new Date(rs.getLong("date")),
                rs.getString("problem"),
                rs.getString("repairs"),
                parts, // <--- Now passing List<Part>
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
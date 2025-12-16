package com.autoshop.app.model;

public class Part {
    private int id;
    private int appointmentId; // Links this part to a specific appointment
    private String code;
    private String name; // e.g., "Alternator"
    private Supplier supplier;

    public Part(int id, int appointmentId, String code, String name, Supplier supplier) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.code = code;
        this.name = name;
        this.supplier = supplier;
    }

    // Constructor for new parts (before saving to DB)
    public Part(String code, String name, Supplier supplier) {
        this(-1, -1, code, name, supplier);
    }

    // Getters
    public int getId() { return id; }
    public int getAppointmentId() { return appointmentId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Supplier getSupplier() { return supplier; }

    @Override
    public String toString() {
        return name + " (" + code + ") [" + supplier + "]";
    }
}
package com.autoshop.app.model;

import java.util.Date;

public class Appointment {
    // Database Fields
    private int appointmentID;
    private int carID;
    private Date date;
    private String problemDescription;
    private String repairs;
    private String partsUsed;
    private String observations;
    private AppointmentStatus status;

    // UI Display Fields (Transient)
    private String clientName;
    private String clientPhone;
    private String carLicensePlate;
    private String carBrand;
    private String carModel;
    private int carYear;
    private String carPhotoPath;

    // Constructor for CREATING (UI Input)
    public Appointment(String clientName, String clientPhone, String carLicensePlate, String carBrand, String carModel,
                       int carYear, String carPhotoPath, Date date, String problemDescription,  String repairs, String partsUsed, String observations) {
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.carLicensePlate = carLicensePlate;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.carYear = carYear;
        this.carPhotoPath = carPhotoPath;
        this.date = date;
        this.problemDescription = problemDescription;
        this.repairs = repairs;
        this.partsUsed = partsUsed;
        this.observations = observations;
        this.status = AppointmentStatus.SCHEDULED;
    }

    // Constructor with status, for getting appointments from database
    public Appointment(int appointmentId, int carId, java.sql.Date date, String problem, String repairs, String partsUsed, String observations, AppointmentStatus status, String name, String phone, String licensePlate, String brandName, String model, int year, String photoPath) {
        this.appointmentID = appointmentId;
        this.carID = carId;
        this.date = date;
        this.problemDescription = problem;
        this.repairs = repairs;
        this.partsUsed = partsUsed;
        this.observations = observations;
        this.status = status;
        this.clientName = name;
        this.clientPhone = phone;
        this.carLicensePlate = licensePlate;
        this.carBrand = brandName;
        this.carModel = model;
        this.carYear = year;
        this.carPhotoPath = photoPath;
    }

    // Getters & Setters
    public int getAppointmentID() { return appointmentID; }
    public int getCarID() { return carID; }
    public Date getDate() { return date; }
    public String getProblemDescription() { return problemDescription; }
    public String getRepairs() { return repairs; }
    public String getPartsUsed() { return partsUsed; }
    public String getObservations() { return observations; }
    public AppointmentStatus getStatus() { return status; }
    public String getClientName() { return clientName; }
    public String getClientPhone() { return clientPhone; }
    public String getCarLicensePlate() { return carLicensePlate; }
    public String getCarBrand() { return carBrand; }
    public String getCarModel() { return carModel; }
    public int getCarYear() { return carYear; }
    public String getCarPhotoPath() { return carPhotoPath; }

    // Setters
    public void setDate(Date date) { this.date = date; }
    public void setProblemDescription(String s) { this.problemDescription = s; }
    public void setRepairs(String s) { this.repairs = s; }
    public void setPartsUsed(String s) { this.partsUsed = s; }
    public void setObservations(String s) { this.observations = s; }
    public void setStatus(AppointmentStatus s) { this.status = s; }
    public void setClientName(String s) { this.clientName = s; }
    public void setClientPhone(String s) { this.clientPhone = s; }
    public void setCarLicensePlate(String s) { this.carLicensePlate = s; }
    public void setCarBrand(String s) { this.carBrand = s; }
    public void setCarModel(String s) { this.carModel = s; }
    public void setCarYear(int i) { this.carYear = i; }
    public void setCarPhotoPath(String s) { this.carPhotoPath = s; }
}
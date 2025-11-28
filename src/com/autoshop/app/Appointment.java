package com.autoshop.app;

import java.util.Date;

public class Appointment {
    // Database Fields
    private int appointmentID;
    private int carID;
    private Date date;
    private String problemDescription;
    private String status;

    // UI Display Fields (Transient)
    private String clientName;
    private String clientPhone;
    private String carLicensePlate;
    private String carBrand;
    private String carModel;
    private int carYear;          // NEW
    private String carPhotoPath;  // NEW

    // Constructor for LOADING (Full data)
    public Appointment(int appointmentID, int carID, Date date, String problemDescription, String status,
                       String clientName, String clientPhone, String carLicensePlate, String carBrand, String carModel,
                       int carYear, String carPhotoPath) { // Added args
        this.appointmentID = appointmentID;
        this.carID = carID;
        this.date = date;
        this.problemDescription = problemDescription;
        this.status = status;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.carLicensePlate = carLicensePlate;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.carYear = carYear;          // NEW
        this.carPhotoPath = carPhotoPath;// NEW
    }

    // Constructor for CREATING (UI Input)
    public Appointment(String clientName, String clientPhone, String carLicensePlate, String carBrand, String carModel,
                       int carYear, String carPhotoPath, Date date, String problemDescription) {
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.carLicensePlate = carLicensePlate;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.carYear = carYear;          // NEW
        this.carPhotoPath = carPhotoPath;// NEW
        this.date = date;
        this.problemDescription = problemDescription;
        this.status = "Scheduled";
    }

    // Getters & Setters
    public int getAppointmentID() { return appointmentID; }
    public int getCarID() { return carID; }
    public Date getDate() { return date; }
    public String getProblemDescription() { return problemDescription; }
    public String getStatus() { return status; }
    public String getClientName() { return clientName; }
    public String getClientPhone() { return clientPhone; }
    public String getCarLicensePlate() { return carLicensePlate; }
    public String getCarBrand() { return carBrand; }
    public String getCarModel() { return carModel; }

    // NEW GETTERS
    public int getCarYear() { return carYear; }
    public String getCarPhotoPath() { return carPhotoPath; }

    // Setters
    public void setDate(Date date) { this.date = date; }
    public void setProblemDescription(String s) { this.problemDescription = s; }
    public void setStatus(String s) { this.status = s; }
    public void setClientName(String s) { this.clientName = s; }
    public void setClientPhone(String s) { this.clientPhone = s; }
    public void setCarLicensePlate(String s) { this.carLicensePlate = s; }
    public void setCarBrand(String s) { this.carBrand = s; }
    public void setCarModel(String s) { this.carModel = s; }
    public void setCarYear(int i) { this.carYear = i; }         // NEW
    public void setCarPhotoPath(String s) { this.carPhotoPath = s; } // NEW
}
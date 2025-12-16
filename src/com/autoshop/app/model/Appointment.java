package com.autoshop.app.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Appointment {
    private int appointmentID;
    private int carID;
    private Date date;
    private String problemDescription;
    private String repairs;

    // CHANGED: List<Part> instead of String partsUsed
    private List<Part> partList;

    private String observations;
    private AppointmentStatus status;

    // Derived fields (from Joins)
    private String clientName;
    private String clientPhone;
    private String carLicensePlate;
    private String carBrand;
    private String carModel;
    private int carYear;
    private String carPhotoPath;

    // Constructor for New Appointments (No ID yet)
    public Appointment(String clientName, String clientPhone, String carLicensePlate,
                       String carBrand, String carModel, int carYear, String carPhotoPath,
                       Date date, String problemDescription, String repairs,
                       List<Part> partList, String observations) {
        this(-1, -1, date, problemDescription, repairs, partList, observations,
                AppointmentStatus.SCHEDULED, clientName, clientPhone,
                carLicensePlate, carBrand, carModel, carYear, carPhotoPath);
    }

    // Full Constructor (Loading from DB)
    public Appointment(int appointmentID, int carID, Date date, String problemDescription,
                       String repairs, List<Part> partList, String observations,
                       AppointmentStatus status, String clientName, String clientPhone,
                       String carLicensePlate, String carBrand, String carModel,
                       int carYear, String carPhotoPath) {
        this.appointmentID = appointmentID;
        this.carID = carID;
        this.date = date;
        this.problemDescription = problemDescription;
        this.repairs = repairs;

        // Ensure list is never null
        this.partList = (partList == null) ? new ArrayList<>() : partList;

        this.observations = observations;
        this.status = status;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.carLicensePlate = carLicensePlate;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.carYear = carYear;
        this.carPhotoPath = carPhotoPath;
    }

    // --- GETTERS & SETTERS ---
    public int getAppointmentID() { return appointmentID; }
    public int getCarID() { return carID; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public String getRepairs() { return repairs; }
    public void setRepairs(String repairs) { this.repairs = repairs; }

    // NEW: Parts List Getter/Setter
    public List<Part> getPartList() { return partList; }
    public void setPartList(List<Part> partList) { this.partList = partList; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    // Client/Car Getters
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    public String getCarLicensePlate() { return carLicensePlate; }
    public void setCarLicensePlate(String carLicensePlate) { this.carLicensePlate = carLicensePlate; }
    public String getCarBrand() { return carBrand; }
    public void setCarBrand(String carBrand) { this.carBrand = carBrand; }
    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public int getCarYear() { return carYear; }
    public void setCarYear(int carYear) { this.carYear = carYear; }
    public String getCarPhotoPath() { return carPhotoPath; }
    public void setCarPhotoPath(String carPhotoPath) { this.carPhotoPath = carPhotoPath; }

    // Helper to display parts as string in tables (Comma separated)
    public String getPartsSummary() {
        if (partList == null || partList.isEmpty()) return "-";

        if (partList.size() == 1) {
            return partList.get(0).getName(); // Just the name of the single part
        } else {
            return partList.size() + " Parts"; // "3 Parts" (Clean!)
        }
    }

    // Helper for old methods relying on getPartsUsed (Compatibility)
    public String getPartsUsed() {
        return getPartsSummary();
    }
    public void setPartsUsed(String s) {
        // Do nothing, or parse if needed.
        // This setter is just to satisfy old code that might call it.
    }
}
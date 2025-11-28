package com.autoshop.app;

public class Car {
    private int carID;
    private int  clientID;
    private String licensePlate;
    private String carBrand;
    private String carModel;
    private int year;
    private String photoPath;

    public Car(int carID, int clientID, String licensePlate, String carBrand, String carModel, int year, String photoPath) {
        this.carID = carID;
        this.clientID = clientID;
        this.licensePlate = licensePlate;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.year = year;
        this.photoPath = photoPath;
    }

    public int getCarID() { return carID; }
    public int getClientID() { return clientID; }
    public String getLicensePlate() { return licensePlate; }
    public String getCarBrand() { return carBrand; }
    public String getCarModel() { return carModel; }
    public int getYear() { return year; }
    public String getPhotoPath() { return photoPath; }

    public void setCarID(int carID) {}
    public void setClientID(int clientID) {}
    public void setLicensePlate(String licensePlate) {}
    public void setCarBrand(String carBrand) {}
    public void setCarModel(String carModel) {}
    public void setYear(int year) {}
    public void setPhotoPath(String photoPath) {}
}

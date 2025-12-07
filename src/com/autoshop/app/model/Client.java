package com.autoshop.app.model;

public class Client {
    private int clientID;
    private String clientName;
    private String clientPhone;

    public Client(String clientName, String clientPhone) {
        this.clientName = clientName;
        this.clientPhone = clientPhone;
    }
    public Client(int clientID, String clientName, String clientPhone) {
        this(clientName, clientPhone);
        this.clientID = clientID;
    }

    public int getClientID() { return clientID; }
    public String getClientName() { return clientName; }
    public String getClientPhone() { return clientPhone; }
    public void setClientID(int clientID) {}
    public void setClientName(String clientName) {}
    public void setClientPhone(String clientPhone) {}
}

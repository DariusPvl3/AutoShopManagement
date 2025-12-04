package com.autoshop.app;

import java.awt.Color;

public enum AppointmentStatus {
    SCHEDULED(new Color(1, 40, 227)),   // Blue
    IN_PROGRESS(new Color(170, 156, 0)), // Orange
    DONE(new Color(0, 124, 54)),         // Green
    CANCELLED(new Color(216, 19, 0));    // Red

    private final Color color;

    AppointmentStatus(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    // Helper to make the text look nice ("IN_PROGRESS" -> "In Progress")
    @Override
    public String toString() {
        String s = super.toString();
        return s.charAt(0) + s.substring(1).toLowerCase().replace("_", " ");
    }
}
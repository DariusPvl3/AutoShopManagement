package com.autoshop.app;

import java.awt.Color;

public enum AppointmentStatus {
    SCHEDULED(new Color(52, 152, 219)),   // Blue
    IN_PROGRESS(new Color(243, 156, 18)), // Orange
    DONE(new Color(39, 174, 96)),         // Green
    CANCELLED(new Color(192, 57, 43));    // Red

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
        return s.substring(0, 1) + s.substring(1).toLowerCase().replace("_", " ");
    }
}
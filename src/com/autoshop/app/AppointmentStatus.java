package com.autoshop.app;

import java.awt.Color;

public enum AppointmentStatus {
    // Pass Color AND Translation Key
    SCHEDULED(new Color(52, 152, 219),   "sts.scheduled"),
    IN_PROGRESS(new Color(243, 156, 18), "sts.in_progress"),
    DONE(new Color(39, 174, 96),         "sts.done"),
    CANCELLED(new Color(192, 57, 43),    "sts.cancelled");

    private final Color color;
    private final String langKey; // NEW

    AppointmentStatus(Color color, String langKey) {
        this.color = color;
        this.langKey = langKey;
    }

    public Color getColor() {
        return color;
    }

    public String getLangKey() {
        return langKey;
    }
}
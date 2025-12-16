package com.autoshop.app.model;

public enum Supplier {
    AUTONET,
    INTERCARS,
    UNIX,
    BARDI,
    AUTOTOTAL,
    MATEROM,
    CONEX,
    CATI;

    // Helper to format enum nicely (AUTONET -> Autonet)
    @Override
    public String toString() {
        String s = super.toString();
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
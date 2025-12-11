package com.autoshop.app.controller;

import com.autoshop.app.component.ThemedDialog;
import com.autoshop.app.model.Appointment;
import com.autoshop.app.model.AppointmentStatus;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchController {
    private final Component parentView;

    public SearchController(Component parentView) {
        this.parentView = parentView;
    }

    public List<Appointment> search(String keyword, Object statusObj, Date from, Date to) {
        AppointmentStatus status = (statusObj instanceof AppointmentStatus) ? (AppointmentStatus) statusObj : null;

        try {
            List<Appointment> results = DatabaseHelper.searchAppointments(keyword, status, from, to);

            if (results.isEmpty()) {
                ThemedDialog.showMessage(parentView, "Info", LanguageHelper.getString("msg.err.search"));
            }
            return results;

        } catch (SQLException e) {
            e.printStackTrace();
            ThemedDialog.showMessage(parentView,
                    LanguageHelper.getString("title.error"),
                    LanguageHelper.getString("msg.err.search"));
            return new ArrayList<>();
        }
    }
}
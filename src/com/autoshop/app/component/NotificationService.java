package com.autoshop.app.component;

import com.autoshop.app.model.Appointment;
import com.autoshop.app.util.DatabaseHelper;
import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.PreferencesHelper;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class NotificationService {

    private static final Set<Integer> notifiedAppointments = new HashSet<>();
    private static Timer timer;
    private static TrayIcon trayIcon;

    public static void start() {
        if (timer != null) return; // Already running

        // Setup System Tray
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage(
                        NotificationService.class.getResource("/resources/logo.png"));

                trayIcon = new TrayIcon(image, "AutoShop Scheduler");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
            } catch (Exception e) {
                System.err.println("SystemTray not supported or failed: " + e.getMessage());
            }
        }

        // Run check every 1 minute
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAppointments();
            }
        }, 0, 60 * 1000); // 0 delay, 60s period
    }

    private static void checkAppointments() {
        if (!PreferencesHelper.isNotificationEnabled()) return;

        int leadTimeMinutes = PreferencesHelper.getNotificationLeadTime();
        long leadTimeMillis = TimeUnit.MINUTES.toMillis(leadTimeMinutes);
        Date now = new Date();

        try {
            // Only check today's appointments
            List<Appointment> todayList = DatabaseHelper.getDashboardAppointments(now);

            for (Appointment appt : todayList) {
                // Logic:
                // 1. Must be "SCHEDULED" (not Done/Cancelled)
                // 2. Not already notified
                // 3. Time difference is between 0 and LeadTime (e.g., 0 to 15 mins away)

                if (!"SCHEDULED".equalsIgnoreCase(appt.getStatus().name())) continue;
                if (notifiedAppointments.contains(appt.getAppointmentID())) continue;

                long diff = appt.getDate().getTime() - now.getTime();

                if (diff > 0 && diff <= leadTimeMillis) {
                    sendNotification(appt);
                    notifiedAppointments.add(appt.getAppointmentID());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendNotification(Appointment appt) {
        if (trayIcon != null) {
            String title = LanguageHelper.getString("notif.title");
            String arrivingTxt = LanguageHelper.getString("notif.arriving");
            String minutesTxt = LanguageHelper.getString("notif.minutes");

            String message = appt.getClientName() + " (" + appt.getCarBrand() + ")\n" +
                    arrivingTxt + " " + PreferencesHelper.getNotificationLeadTime() + " " + minutesTxt + ".";
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } else {
            // Fallback for systems without Tray support
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public static void stop() {
        // 1. Stop the background timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // 2. Remove the icon from the Windows Taskbar
        if (SystemTray.isSupported() && trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }
}
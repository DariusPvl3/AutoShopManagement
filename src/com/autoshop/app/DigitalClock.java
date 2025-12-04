package com.autoshop.app;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DigitalClock extends JLabel {
    public DigitalClock() {
        setFont(new Font("SansSerif", Font.BOLD, 24));
        setForeground(new Color(52, 152, 219)); // Blue color
        setHorizontalAlignment(SwingConstants.RIGHT);

        // Update every 1000ms (1 second)
        Timer t = new Timer(1000, _ -> updateTime());
        t.start();
        updateTime();
    }

    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  |  EEE, dd MMM yyyy");
        setText(sdf.format(new Date()));
    }
}
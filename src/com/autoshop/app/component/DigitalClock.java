package com.autoshop.app.component;

import com.autoshop.app.util.LanguageHelper;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DigitalClock extends JLabel {
    public DigitalClock() {
        setFont(new Font("SansSerif", Font.BOLD, 24));
        setHorizontalAlignment(SwingConstants.RIGHT);

        // Update every 1000ms (1 second)
        Timer t = new Timer(1000, _ -> updateTime());
        t.start();
        updateTime();
    }

    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  |  EEE, dd MMM yyyy", LanguageHelper.getCurrentLocale());
        setText(sdf.format(new Date()));
    }
}
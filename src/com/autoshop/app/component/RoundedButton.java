package com.autoshop.app.component;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {

    private final int radius;

    public RoundedButton(String text) {
        super(text);
        this.radius = 20;

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // 1. Antialiasing for smooth curves
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 2. Paint the Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();

        // 3. Paint the Text
        super.paintComponent(g);
    }
}
package com.autoshop.app.component;

import com.autoshop.app.util.Theme;

import javax.swing.*;
import java.awt.*;

public class RedCheckBox extends JCheckBox {

    private static final int SIZE = 20;
    private static final Color THEME_RED = new Color(211, 47, 47); // Matches your buttons
    private static final Color BORDER_GRAY = new Color(160, 160, 160);

    public RedCheckBox(String text) {
        super(text);
        init();
    }

    private void init() {
        setFocusPainted(false);
        setBackground(Theme.OFF_WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setIcon(new CheckBoxIcon(false));
        setSelectedIcon(new CheckBoxIcon(true));
    }
    private static class CheckBoxIcon implements Icon {
        private final boolean isSelected;

        public CheckBoxIcon(boolean isSelected) {
            this.isSelected = isSelected;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                // Draw Red Background
                g2.setColor(THEME_RED);
                g2.fillRect(x, y, SIZE, SIZE);

                // Draw White Checkmark
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f)); // Thicker tick line
                g2.drawLine(x + 4, y + 10, x + 8, y + 14); // Down stroke
                g2.drawLine(x + 8, y + 14, x + 16, y + 6); // Up stroke
            } else {
                // Draw White Background
                g2.setColor(Color.WHITE);
                g2.fillRect(x, y, SIZE, SIZE);

                // Draw Gray Border
                g2.setColor(BORDER_GRAY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRect(x, y, SIZE, SIZE);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return SIZE; }

        @Override
        public int getIconHeight() { return SIZE; }
    }
}
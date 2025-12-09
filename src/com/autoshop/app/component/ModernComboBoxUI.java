package com.autoshop.app.component;

import com.autoshop.app.util.Theme;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public class ModernComboBoxUI extends BasicComboBoxUI {

    @Override
    protected JButton createArrowButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isHover = getModel().isRollover();
                boolean isPressed = getModel().isPressed();

                // 1. Determine Colors based on State
                Color bgColor;
                Color arrowColor;

                if (isPressed) {
                    bgColor = Theme.RED.darker(); // Darker Red when clicking
                    arrowColor = Color.WHITE;
                } else if (isHover) {
                    bgColor = Theme.RED;          // Red Background on Hover
                    arrowColor = Color.WHITE;     // White Arrow on Hover
                } else {
                    bgColor = Color.WHITE;        // White Background Normally
                    arrowColor = Theme.RED;       // Red Arrow Normally
                }

                // 2. Paint Background
                g2.setColor(bgColor);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // 3. Paint Arrow
                int size = 8;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                g2.setColor(arrowColor);

                Polygon triangle = new Polygon();
                triangle.addPoint(x, y);
                triangle.addPoint(x + size, y);
                triangle.addPoint(x + size / 2, y + size);

                g2.fillPolygon(triangle);

                // 4. Paint Separator (Only show when NOT hovering to look clean)
                if (!isHover && !isPressed) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawLine(0, 5, 0, getHeight() - 5);
                }

                g2.dispose();
            }
        };

        // --- BUTTON SETTINGS ---
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true); // Important: Enables the "isRollover" check

        // Hand Cursor
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setPreferredSize(new Dimension(30, 10));

        return btn;
    }
}
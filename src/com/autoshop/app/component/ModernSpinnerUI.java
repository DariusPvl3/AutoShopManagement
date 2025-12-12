package com.autoshop.app.component;

import com.autoshop.app.util.Theme;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSpinnerUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernSpinnerUI extends BasicSpinnerUI {

    @Override
    protected Component createNextButton() {
        JButton btn = createArrowButton(SwingConstants.NORTH);
        btn.setName("Spinner.nextButton");
        // Install the "Hold to Repeat" logic (true = Next/Up)
        installButtonLogic(btn, true);
        return btn;
    }

    @Override
    protected Component createPreviousButton() {
        JButton btn = createArrowButton(SwingConstants.SOUTH);
        btn.setName("Spinner.previousButton");
        // Install the "Hold to Repeat" logic (false = Previous/Down)
        installButtonLogic(btn, false);
        return btn;
    }

    // --- NEW HELPER METHOD FOR AUTO-REPEAT ---
    private void installButtonLogic(JButton btn, boolean isNext) {
        // 1. Create a Timer that fires every 60ms (fast speed)
        Timer timer = new Timer(60, e -> {
            if (spinner != null && spinner.isEnabled()) {
                Object newValue = isNext ? spinner.getNextValue() : spinner.getPreviousValue();
                if (newValue != null) {
                    spinner.setValue(newValue);
                }
            }
        });
        // 2. Initial Delay: Wait 400ms before starting the fast speed
        // This prevents the value from jumping wildly if you just wanted 1 click.
        timer.setInitialDelay(400);

        // 3. Attach Mouse Listener to Start/Stop the timer
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && spinner.isEnabled()) {
                    // A. Update Immediately (for the first click)
                    Object newValue = isNext ? spinner.getNextValue() : spinner.getPreviousValue();
                    if (newValue != null) {
                        spinner.setValue(newValue);
                    }
                    // B. Start the timer for "Holding"
                    timer.start();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                timer.stop();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Stop if the mouse slips off the button while holding
                timer.stop();
            }
        });
    }

    private JButton createArrowButton(int direction) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isHover = getModel().isRollover();
                boolean isPressed = getModel().isPressed();

                // Colors
                Color bgColor;
                Color arrowColor;

                if (isPressed) {
                    bgColor = Theme.RED.darker();
                    arrowColor = Color.WHITE;
                } else if (isHover) {
                    bgColor = Theme.RED;
                    arrowColor = Color.WHITE;
                } else {
                    bgColor = Color.WHITE;
                    arrowColor = Theme.RED;
                }

                // Background
                g2.setColor(bgColor);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Arrow
                int w = getWidth();
                int h = getHeight();
                int arrowSize = 5;

                g2.setColor(arrowColor);
                Polygon triangle = new Polygon();

                if (direction == SwingConstants.NORTH) {
                    triangle.addPoint(w / 2, h / 2 - 2);
                    triangle.addPoint(w / 2 - arrowSize, h / 2 + arrowSize - 2);
                    triangle.addPoint(w / 2 + arrowSize, h / 2 + arrowSize - 2);
                } else {
                    triangle.addPoint(w / 2, h / 2 + 3);
                    triangle.addPoint(w / 2 - arrowSize, h / 2 - arrowSize + 3);
                    triangle.addPoint(w / 2 + arrowSize, h / 2 - arrowSize + 3);
                }

                g2.fillPolygon(triangle);

                // Border
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(0, 0, 0, h);
                if (direction == SwingConstants.NORTH) {
                    g2.drawLine(0, h-1, w, h-1);
                }

                g2.dispose();
            }
        };

        // Button Settings
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(20, 10));

        return btn;
    }
}
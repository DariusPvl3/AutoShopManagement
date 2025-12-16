package com.autoshop.app.component;

import com.autoshop.app.util.Theme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSpinnerUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Date;

public class ModernSpinnerUI extends BasicSpinnerUI {

    @Override
    protected Component createNextButton() {
        JButton btn = createArrowButton(SwingConstants.NORTH);
        btn.setName("Spinner.nextButton");
        installButtonLogic(btn, true);
        return btn;
    }

    @Override
    protected Component createPreviousButton() {
        JButton btn = createArrowButton(SwingConstants.SOUTH);
        btn.setName("Spinner.previousButton");
        installButtonLogic(btn, false);
        return btn;
    }

    // --- LOGIC FOR 30-MINUTE SNAP ---
    private void installButtonLogic(JButton btn, boolean isNext) {
        Timer timer = new Timer(60, e -> {
            if (spinner != null && spinner.isEnabled()) {
                calculateAndSetValue(isNext);
            }
        });
        timer.setInitialDelay(400);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && spinner.isEnabled()) {
                    calculateAndSetValue(isNext);
                    timer.start();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                timer.stop();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                timer.stop();
            }
        });
    }

    private void calculateAndSetValue(boolean isNext) {
        Object current = spinner.getValue();
        Object newValue;

        // 1. If it's a Date (Time Spinner), use our Smart 30-min Snap Logic
        if (current instanceof Date) {
            newValue = getSnappedDate((Date) current, isNext);
        }
        // 2. Fallback for normal number spinners (just +1 or -1)
        else {
            newValue = isNext ? spinner.getNextValue() : spinner.getPreviousValue();
        }

        if (newValue != null) {
            spinner.setValue(newValue);
        }
    }

    /**
     * Calculates the next/previous time snapped to 00 or 30.
     * Examples (isNext = true):
     * 12:00 -> 12:30
     * 12:15 -> 12:30
     * 12:30 -> 13:00
     */
    private Date getSnappedDate(Date current, boolean isNext) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        int minute = cal.get(Calendar.MINUTE);

        if (isNext) {
            // GOING UP
            if (minute < 30) {
                minute = 30; // Snap to half hour
            } else {
                minute = 0;  // Snap to next hour
                cal.add(Calendar.HOUR_OF_DAY, 1);
            }
        } else {
            // GOING DOWN
            if (minute > 30) {
                minute = 30; // Snap down to half hour
            } else if (minute > 0 && minute <= 30) {
                minute = 0;  // Snap down to hour
            } else {
                minute = 30; // Snap back to previous half hour
                cal.add(Calendar.HOUR_OF_DAY, -1);
            }
        }

        // Apply clean minutes and zero out seconds
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    private JButton createArrowButton(int direction) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isHover = getModel().isRollover();
                boolean isPressed = getModel().isPressed();

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

                g2.setColor(bgColor);
                g2.fillRect(0, 0, getWidth(), getHeight());

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
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(0, 0, 0, h);
                if (direction == SwingConstants.NORTH) {
                    g2.drawLine(0, h-1, w, h-1);
                }

                g2.dispose();
            }
        };

        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(20, 10));

        return btn;
    }
}
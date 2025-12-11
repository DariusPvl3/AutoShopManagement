package com.autoshop.app.component;

import com.autoshop.app.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomTitleBar extends JPanel {

    private Point initialClick;
    private final JFrame parentFrame;

    public CustomTitleBar(JFrame frame, String title) {
        this.parentFrame = frame;
        this.setLayout(new BorderLayout());
        this.setBackground(Theme.BLACK);
        this.setPreferredSize(new Dimension(0, 35));

        // 1. Title Text
        JLabel titleLabel = new JLabel("  " + title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        this.add(titleLabel, BorderLayout.WEST);

        // 2. Window Controls (Minimize, Close)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);

        JButton minimizeBtn = createControlBtn("_", _ -> frame.setState(Frame.ICONIFIED));
        JButton closeBtn = createControlBtn("X", _ -> System.exit(0));

        // Make Close button turn red on hover
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { closeBtn.setBackground(Theme.RED); }
            public void mouseExited(java.awt.event.MouseEvent evt) { closeBtn.setBackground(Theme.BLACK); }
        });

        controls.add(minimizeBtn);
        controls.add(closeBtn);
        this.add(controls, BorderLayout.EAST);

        // 3. Enable Dragging the Window
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Get current window location
                int thisX = parentFrame.getLocation().x;
                int thisY = parentFrame.getLocation().y;

                // Determine how much the mouse moved
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                // Move window
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                parentFrame.setLocation(X, Y);
            }
        });
    }

    private JButton createControlBtn(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // Transparent initially
        btn.setOpaque(true);
        btn.setBackground(Theme.BLACK);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(45, 35));
        btn.addActionListener(action);
        return btn;
    }
}
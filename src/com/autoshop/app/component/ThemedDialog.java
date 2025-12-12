package com.autoshop.app.component;

import com.autoshop.app.util.LanguageHelper;
import com.autoshop.app.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ThemedDialog extends JDialog {

    private static final Font MAIN_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);

    private boolean confirmed = false;
    private Point initialClick;

    private ThemedDialog(Window owner, String title, String message, boolean isConfirm) {
        super(owner, title, ModalityType.APPLICATION_MODAL);

        // 1. Remove system border for custom styling
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.WHITE);

        // --- 2. Custom Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BLACK);
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Custom "X" Close Button
        JLabel closeBtn = new JLabel("X");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(Color.LIGHT_GRAY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(Color.WHITE);
            }
        });
        headerPanel.add(closeBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Dragging Logic
        headerPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });
        headerPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        // --- 3. Message Body ---
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Theme.WHITE);
        messagePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextArea text = new JTextArea(message);
        text.setFont(MAIN_FONT);
        text.setWrapStyleWord(true);
        text.setLineWrap(true);
        text.setEditable(false);
        text.setFocusable(false);
        text.setBackground(Theme.WHITE);
        text.setSize(new Dimension(300, 1));
        messagePanel.add(text, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.CENTER);

        // --- 4. Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Theme.WHITE);

        if (isConfirm) {
            String yesText = LanguageHelper.getString("btn.yes");
            String noText = LanguageHelper.getString("btn.no");

            if (yesText.startsWith("Key not found")) yesText = "Yes";
            if (noText.startsWith("Key not found")) noText = "No";

            JButton yesBtn = new RoundedButton(yesText);
            JButton noBtn = new RoundedButton(noText);
            ButtonStyler.apply(yesBtn, Theme.RED);
            ButtonStyler.apply(noBtn, Theme.BLACK);

            yesBtn.addActionListener(e -> { confirmed = true; dispose(); });
            noBtn.addActionListener(e -> { confirmed = false; dispose(); });

            buttonPanel.add(noBtn);
            buttonPanel.add(yesBtn);
        } else {
            String okText = LanguageHelper.getString("btn.ok");
            if (okText.startsWith("Key not found")) okText = "OK";

            JButton okBtn = new RoundedButton(okText);
            ButtonStyler.apply(okBtn, Theme.RED);
            okBtn.addActionListener(e -> dispose());
            buttonPanel.add(okBtn);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 200);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    public static void showMessage(Component parentComponent, String title, String message) {
        Window window = SwingUtilities.getWindowAncestor(parentComponent);
        ThemedDialog dialog = new ThemedDialog(window, title, message, false);
        dialog.setVisible(true);
    }

    public static boolean showConfirm(Component parentComponent, String title, String message) {
        Window window = SwingUtilities.getWindowAncestor(parentComponent);
        ThemedDialog dialog = new ThemedDialog(window, title, message, true);
        dialog.setVisible(true);
        return dialog.confirmed;
    }
}
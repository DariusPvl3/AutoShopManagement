package com.autoshop.app.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WindowResizeHelper extends MouseAdapter {
    private final JFrame frame;
    private int cursorDirection = Cursor.DEFAULT_CURSOR;
    private Point startPos = null;
    private Rectangle startBounds = null;

    public static void install(JFrame frame) {
        WindowResizeHelper resizer = new WindowResizeHelper(frame);
        frame.getRootPane().addMouseListener(resizer);
        frame.getRootPane().addMouseMotionListener(resizer);
    }

    private WindowResizeHelper(JFrame frame) {
        this.frame = frame;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int w = frame.getWidth();
        int h = frame.getHeight();

        // Determine cursor based on position
        int newCursor = Cursor.DEFAULT_CURSOR;

        // Corners
        // Sensitivity zone
        int BORDER_SIZE = 8;
        if (x < BORDER_SIZE && y < BORDER_SIZE) newCursor = Cursor.NW_RESIZE_CURSOR;
        else if (x > w - BORDER_SIZE && y < BORDER_SIZE) newCursor = Cursor.NE_RESIZE_CURSOR;
        else if (x < BORDER_SIZE && y > h - BORDER_SIZE) newCursor = Cursor.SW_RESIZE_CURSOR;
        else if (x > w - BORDER_SIZE && y > h - BORDER_SIZE) newCursor = Cursor.SE_RESIZE_CURSOR;

            // Edges
        else if (x < BORDER_SIZE) newCursor = Cursor.W_RESIZE_CURSOR;
        else if (x > w - BORDER_SIZE) newCursor = Cursor.E_RESIZE_CURSOR;
        else if (y < BORDER_SIZE) newCursor = Cursor.N_RESIZE_CURSOR;
        else if (y > h - BORDER_SIZE) newCursor = Cursor.S_RESIZE_CURSOR;

        frame.getRootPane().setCursor(Cursor.getPredefinedCursor(newCursor));
        cursorDirection = newCursor;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPos = e.getLocationOnScreen();
        startBounds = frame.getBounds();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (cursorDirection == Cursor.DEFAULT_CURSOR || startBounds == null) return;

        Point currPos = e.getLocationOnScreen();
        int dx = currPos.x - startPos.x;
        int dy = currPos.y - startPos.y;

        int x = startBounds.x;
        int y = startBounds.y;
        int w = startBounds.width;
        int h = startBounds.height;

        // Apply changes based on direction
        switch (cursorDirection) {
            case Cursor.E_RESIZE_CURSOR -> w += dx;
            case Cursor.S_RESIZE_CURSOR -> h += dy;
            case Cursor.SE_RESIZE_CURSOR -> { w += dx; h += dy; }
            // Complex resizing (moving origin)
            case Cursor.W_RESIZE_CURSOR -> { x += dx; w -= dx; }
            case Cursor.N_RESIZE_CURSOR -> { y += dy; h -= dy; }
            case Cursor.SW_RESIZE_CURSOR -> { x += dx; w -= dx; h += dy; }
            case Cursor.NE_RESIZE_CURSOR -> { y += dy; h -= dy; w += dx; }
            case Cursor.NW_RESIZE_CURSOR -> { x += dx; w -= dx; y += dy; h -= dy; }
        }

        // Min Size Safety
        if (w >= frame.getMinimumSize().width && h >= frame.getMinimumSize().height) {
            frame.setBounds(x, y, w, h);
        }
    }
}
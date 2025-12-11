package com.autoshop.app.component;

import com.autoshop.app.util.Theme;
import com.autoshop.app.util.LanguageHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class SwingTableStyler {
    private static final Font TABLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font EMPTY_FONT = new Font("SansSerif", Font.BOLD, 18);

    public static JTable create(TableModel model, int statusColumnIndex) {
        JTable table = new JTable(model) {

            // 1. Standard Row Renderer
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(TABLE_FONT);

                if (isRowSelected(row)) {
                    c.setBackground(Theme.RED);
                    c.setForeground(Theme.WHITE);
                } else {
                    if (row % 2 == 0) c.setBackground(Color.WHITE);
                    else c.setBackground(new Color(220, 220, 220));

                    if (column != statusColumnIndex)
                        c.setForeground(Color.BLACK);
                }
                return c;
            }

            // 2. Empty State Painting
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // Paints rows and background first

                // Only draw text if EMPTY
                if (getRowCount() == 0) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    // A. Check if a custom message was set via putClientProperty
                    Object customMsg = getClientProperty("empty_msg");
                    String text;

                    if (customMsg != null) {
                        text = customMsg.toString(); // Use custom text (e.g. "No results" or "")
                    } else {
                        // Default for AppointmentView
                        text = LanguageHelper.getString("msg.no_appointments");
                        // Fallback if language fails
                        if (text == null) text = "No Appointments Found";
                    }

                    // If text is empty string, draw nothing
                    if (text.isEmpty()) return;

                    g2.setFont(EMPTY_FONT);
                    g2.setColor(Color.GRAY);

                    FontMetrics metrics = g2.getFontMetrics(EMPTY_FONT);
                    int x = (getWidth() - metrics.stringWidth(text)) / 2;
                    int y = getHeight() / 2;

                    g2.drawString(text, x, y);
                }
            }
        };

        // 3. Settings
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.RED);
        table.setSelectionForeground(Theme.WHITE);
        table.setFocusable(false);
        table.setFillsViewportHeight(true); // Critical for painting background correctly

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(Theme.BLACK);
                c.setForeground(Theme.WHITE);
                c.setFont(HEADER_FONT);
                if (c instanceof JComponent) ((JComponent) c).setOpaque(true);
                return c;
            }
        });

        return table;
    }
}
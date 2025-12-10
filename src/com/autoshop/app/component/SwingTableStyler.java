package com.autoshop.app.component;

import com.autoshop.app.util.Theme;
import com.autoshop.app.util.LanguageHelper; // Make sure to import this if you use it for translation

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class SwingTableStyler {
    // Define standard font once
    private static final Font TABLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font EMPTY_FONT = new Font("SansSerif", Font.BOLD, 18); // Font for the empty message

    /**
     * Factory method to create a standardized JTable.
     * @param model The data model
     * @param statusColumnIndex The index of the "Status" column (to skip text coloring). Pass -1 if none.
     */
    public static JTable create(TableModel model, int statusColumnIndex) {
        // 1. Create the Table with BOTH overrides
        JTable table = new JTable(model) {

            // A. Row Coloring Logic (Existing)
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(TABLE_FONT);

                // Handle Row Colors
                if (isRowSelected(row)) {
                    c.setBackground(Theme.RED);
                    c.setForeground(Theme.WHITE);
                } else {
                    // Zebra Striping
                    if (row % 2 == 0) c.setBackground(Color.WHITE);
                    else c.setBackground(new Color(220, 220, 220)); // Light Gray

                    // Text Color Logic
                    // Reset to Black ONLY if it's not the Status column
                    if (column != statusColumnIndex)
                        c.setForeground(Color.BLACK);
                    // If it IS the status column, we leave the color alone (it was set by StatusCellRenderer)
                }
                return c;
            }

            // B. Empty Table Placeholder Logic (New)
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // If table is empty, draw the message
                if (getRowCount() == 0) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    String text = LanguageHelper.getString("msg.no_appointments");

                    g2.setFont(EMPTY_FONT);
                    g2.setColor(Color.GRAY);

                    // Calculate center position
                    FontMetrics metrics = g2.getFontMetrics(EMPTY_FONT);
                    int x = (getWidth() - metrics.stringWidth(text)) / 2;
                    int y = getHeight() / 2;

                    g2.drawString(text, x, y);
                }
            }
        };

        // 2. General Settings
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.RED);
        table.setSelectionForeground(Theme.WHITE);
        table.setFocusable(false);
        table.setFillsViewportHeight(true);

        // 3. Style the Header
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
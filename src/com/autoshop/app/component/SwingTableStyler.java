package com.autoshop.app.component;

import com.autoshop.app.util.Theme; // Assuming you have this
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class SwingTableStyler {
    // Define standard font once
    private static final Font TABLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);
    /**
     * Factory method to create a standardized JTable.
     * @param model The data model
     * @param statusColumnIndex The index of the "Status" column (to skip text coloring). Pass -1 if none.
     */
    public static JTable create(TableModel model, int statusColumnIndex) {
        // 1. Create the Table with the specific "prepareRenderer" logic
        JTable table = new JTable(model) {
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
        };

        // 2. General Settings
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.RED);
        table.setSelectionForeground(Theme.WHITE);

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
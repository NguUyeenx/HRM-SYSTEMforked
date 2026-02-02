package com.hrm.gui.components;

import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * PurpleTable - Custom styled JTable with purple theme
 * Features purple header, alternating row colors, and styled selection
 */
public class PurpleTable extends JTable {

    public PurpleTable() {
        super();
        initStyle();
    }

    public PurpleTable(TableModel model) {
        super(model);
        initStyle();
    }

    public PurpleTable(DefaultTableModel model) {
        super(model);
        initStyle();
    }

    public PurpleTable(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        initStyle();
    }

    private void initStyle() {
        // Header style
        JTableHeader header = getTableHeader();
        header.setBackground(UIColors.TABLE_HEADER_BG);
        header.setForeground(UIColors.TABLE_HEADER_FG);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);

        // Make header renderer opaque
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // Table body style
        setRowHeight(35);
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setSelectionBackground(UIColors.TABLE_SELECTION_BG);
        setSelectionForeground(UIColors.TABLE_SELECTION_FG);
        setGridColor(UIColors.BORDER_GRAY);
        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        setIntercellSpacing(new Dimension(0, 1));

        // Alternating row renderer
        setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    /**
     * Custom cell renderer for alternating row colors
     */
    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (isSelected) {
                c.setBackground(UIColors.TABLE_SELECTION_BG);
                c.setForeground(UIColors.TABLE_SELECTION_FG);
            } else {
                c.setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW);
                c.setForeground(UIColors.TEXT_DARK);
            }

            // Center align by default
            setHorizontalAlignment(SwingConstants.CENTER);

            // Add padding
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            return c;
        }
    }

    /**
     * Set column alignment
     */
    public void setColumnAlignment(int columnIndex, int alignment) {
        TableColumn column = getColumnModel().getColumn(columnIndex);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {

                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, col);

                if (isSelected) {
                    c.setBackground(UIColors.TABLE_SELECTION_BG);
                    c.setForeground(UIColors.TABLE_SELECTION_FG);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW);
                    c.setForeground(UIColors.TEXT_DARK);
                }

                return c;
            }
        };
        renderer.setHorizontalAlignment(alignment);
        column.setCellRenderer(renderer);
    }

    /**
     * Set column width
     */
    public void setColumnWidth(int columnIndex, int width) {
        TableColumn column = getColumnModel().getColumn(columnIndex);
        column.setPreferredWidth(width);
        column.setMinWidth(width);
        column.setMaxWidth(width);
    }

    /**
     * Set minimum column width
     */
    public void setColumnMinWidth(int columnIndex, int minWidth) {
        TableColumn column = getColumnModel().getColumn(columnIndex);
        column.setMinWidth(minWidth);
        column.setPreferredWidth(minWidth);
    }

    /**
     * Add status badge renderer to a column
     */
    public void setStatusBadgeColumn(int columnIndex) {
        TableColumn column = getColumnModel().getColumn(columnIndex);
        column.setCellRenderer(new StatusBadgeRenderer());
    }

    /**
     * Custom renderer for status badge columns
     */
    private class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (value == null) {
                return super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
            }

            StatusBadge badge = new StatusBadge(value.toString());

            // Handle selection
            if (isSelected) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
                panel.setBackground(UIColors.TABLE_SELECTION_BG);
                panel.add(badge);
                return panel;
            } else {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
                panel.setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW);
                panel.add(badge);
                return panel;
            }
        }
    }

    /**
     * Create a non-editable table model
     */
    public static DefaultTableModel createNonEditableModel(Object[] columnNames) {
        return new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /**
     * Create a non-editable table model with data
     */
    public static DefaultTableModel createNonEditableModel(Object[][] data, Object[] columnNames) {
        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}

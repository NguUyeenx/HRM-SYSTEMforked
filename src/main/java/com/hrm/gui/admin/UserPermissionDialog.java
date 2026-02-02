package com.hrm.gui.admin;

import com.hrm.model.Permission;
import com.hrm.model.User;
import com.hrm.service.MockDataService;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * User Permission Dialog - Manage per-user permission exceptions
 * Implements dynamic RBAC: Effective = (Role Permissions) ∪ (Granted) - (Denied)
 */
public class UserPermissionDialog extends JDialog {
    private final MockDataService dataService;
    private final User user;

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboModule;

    public UserPermissionDialog(Frame parent, User user) {
        super(parent, "Phan quyen cho: " + user.getFullName(), true);
        this.dataService = MockDataService.getInstance();
        this.user = user;

        initComponents();
        setupLayout();
        loadData();

        setSize(700, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Module filter
        cboModule = new JComboBox<>();
        cboModule.addItem("Tat ca");
        for (String module : dataService.getPermissionModules()) {
            cboModule.addItem(module);
        }
        cboModule.addActionListener(e -> loadData());

        // Table
        String[] columns = {"Quyen", "Mo ta", "Tu vai tro", "Ngoai le", "Ket qua"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only exception column is editable
            }
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        // Custom renderer for result column
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String result = (String) value;
                    if ("CO".equals(result)) {
                        c.setBackground(new Color(200, 255, 200));
                    } else {
                        c.setBackground(new Color(255, 200, 200));
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        // Combo box editor for exception column
        String[] exceptionOptions = {"(Theo vai tro)", "Cap quyen", "Tu choi"};
        JComboBox<String> cboException = new JComboBox<>(exceptionOptions);
        table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(cboException));
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top panel - info and filter
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Tai khoan: " + user.getUsername()));
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(new JLabel("Vai tro: " + user.getRoleNames()));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Nhom quyen:"));
        filterPanel.add(cboModule);

        topPanel.add(infoPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // Center - table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("Danh sach quyen"));

        // Info panel
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legendPanel.add(new JLabel("Chu thich:"));
        legendPanel.add(createLegendLabel("CO", new Color(200, 255, 200)));
        legendPanel.add(new JLabel("= Co quyen,"));
        legendPanel.add(createLegendLabel("KHONG", new Color(255, 200, 200)));
        legendPanel.add(new JLabel("= Khong co quyen"));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApply = UIHelper.createSuccessButton("Ap dung");
        btnApply.addActionListener(e -> applyChanges());
        JButton btnClose = UIHelper.createDefaultButton("Dong");
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnApply);
        buttonPanel.add(btnClose);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(legendPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JLabel createLegendLabel(String text, Color bgColor) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setOpaque(true);
        lbl.setBackground(bgColor);
        return lbl;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String selectedModule = (String) cboModule.getSelectedItem();
        List<Permission> permissions = dataService.getAllPermissions();
        Map<String, Boolean> userPerms = user.getUserPermissions();

        for (Permission perm : permissions) {
            if (!"Tat ca".equals(selectedModule) && !perm.getModule().equals(selectedModule)) {
                continue;
            }

            // Check if user has this permission from roles
            boolean fromRole = false;
            for (var role : user.getRoles()) {
                if (role.hasPermission(perm.getCode())) {
                    fromRole = true;
                    break;
                }
            }

            // Check user-specific exception
            Boolean exception = userPerms.get(perm.getCode());
            String exceptionText;
            if (exception == null) {
                exceptionText = "(Theo vai tro)";
            } else if (exception) {
                exceptionText = "Cap quyen";
            } else {
                exceptionText = "Tu choi";
            }

            // Calculate effective permission
            boolean effective = user.hasPermission(perm.getCode());

            Object[] row = {
                perm.getCode(),
                perm.getName(),
                fromRole ? "CO" : "-",
                exceptionText,
                effective ? "CO" : "KHONG"
            };
            tableModel.addRow(row);
        }
    }

    private void applyChanges() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String permCode = (String) tableModel.getValueAt(i, 0);
            String exception = (String) tableModel.getValueAt(i, 3);

            if ("(Theo vai tro)".equals(exception)) {
                user.removePermissionException(permCode);
            } else if ("Cap quyen".equals(exception)) {
                user.grantPermission(permCode);
            } else if ("Tu choi".equals(exception)) {
                user.denyPermission(permCode);
            }
        }

        dataService.saveUser(user);
        loadData(); // Refresh to show updated effective permissions

        JOptionPane.showMessageDialog(this,
                "Da cap nhat quyen thanh cong!",
                "Thong bao",
                JOptionPane.INFORMATION_MESSAGE);
    }
}

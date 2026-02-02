package com.hrm.gui.admin;

import com.hrm.model.Role;
import com.hrm.service.MockDataService;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Role Management Panel - CRUD operations for roles with permission assignment
 * Module 9: Phân quyền và bảo mật
 */
public class RoleManagementPanel extends JPanel {
    private final MockDataService dataService;
    private final SessionContext sessionContext;

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnCreate;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRefresh;

    public RoleManagementPanel() {
        this.dataService = MockDataService.getInstance();
        this.sessionContext = SessionContext.getInstance();

        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(UIColors.LIGHT_GRAY_BG);

        // Buttons
        btnCreate = UIHelper.createSuccessButton("Tao moi");
        btnCreate.addActionListener(e -> createRole());

        btnEdit = UIHelper.createPrimaryButton("Sua");
        btnEdit.addActionListener(e -> editRole());

        btnDelete = UIHelper.createDangerButton("Xoa");
        btnDelete.addActionListener(e -> deleteRole());

        btnRefresh = UIHelper.createDefaultButton("Lam moi");
        btnRefresh.addActionListener(e -> loadData());

        // Table
        String[] columns = {"Ma vai tro", "Ten vai tro", "Mo ta", "So quyen", "He thong"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        // System role cell renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String isSystem = (String) value;
                    if ("Co".equals(isSystem)) {
                        c.setBackground(new Color(255, 255, 200));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
    }

    private void setupLayout() {
        // Top panel - buttons
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(btnRefresh);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        if (sessionContext.hasPermission("ROLE_CREATE")) {
            buttonPanel.add(btnCreate);
        }
        if (sessionContext.hasPermission("ROLE_UPDATE")) {
            buttonPanel.add(btnEdit);
        }
        if (sessionContext.hasPermission("ROLE_DELETE")) {
            buttonPanel.add(btnDelete);
        }

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Center - table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("Danh sach vai tro"));

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Luu y: Vai tro he thong (ADMIN) khong the xoa"));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Role> roles = dataService.getAllRoles();

        for (Role role : roles) {
            Object[] row = {
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getPermissions().size(),
                role.isSystemRole() ? "Co" : "-"
            };
            tableModel.addRow(row);
        }
    }

    private void createRole() {
        RoleFormDialog dialog = new RoleFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSuccessful()) {
            loadData();
        }
    }

    private void editRole() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon vai tro can sua",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roleCode = (String) tableModel.getValueAt(selectedRow, 0);
        Role role = dataService.getRole(roleCode);
        if (role != null) {
            RoleFormDialog dialog = new RoleFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), role);
            dialog.setVisible(true);
            if (dialog.isSuccessful()) {
                loadData();
            }
        }
    }

    private void deleteRole() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon vai tro can xoa",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roleCode = (String) tableModel.getValueAt(selectedRow, 0);
        Role role = dataService.getRole(roleCode);

        if (role != null && role.isSystemRole()) {
            JOptionPane.showMessageDialog(this,
                    "Khong the xoa vai tro he thong!",
                    "Loi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac muon xoa vai tro '" + roleCode + "'?",
                "Xac nhan xoa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dataService.deleteRole(roleCode)) {
                JOptionPane.showMessageDialog(this,
                        "Da xoa vai tro thanh cong!",
                        "Thong bao",
                        JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Khong the xoa vai tro! Co the vai tro dang duoc gan cho tai khoan.",
                        "Loi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

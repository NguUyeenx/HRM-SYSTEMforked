package com.hrm.gui.admin;

import com.hrm.model.Role;
import com.hrm.model.User;
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
 * User Management Panel - CRUD operations for users
 * Module 9: Phân quyền và bảo mật
 */
public class UserManagementPanel extends JPanel {
    private final MockDataService dataService;
    private final SessionContext sessionContext;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnCreate;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnPermissions;
    private JButton btnRefresh;

    public UserManagementPanel() {
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

        // Search field
        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tim kiem theo ten dang nhap...");
        txtSearch.addActionListener(e -> searchUsers());

        // Buttons
        btnCreate = UIHelper.createSuccessButton("Tao moi");
        btnCreate.addActionListener(e -> createUser());

        btnEdit = UIHelper.createPrimaryButton("Sua");
        btnEdit.addActionListener(e -> editUser());

        btnDelete = UIHelper.createDangerButton("Xoa");
        btnDelete.addActionListener(e -> deleteUser());

        btnPermissions = UIHelper.createDefaultButton("Phan quyen");
        btnPermissions.addActionListener(e -> managePermissions());

        btnRefresh = UIHelper.createDefaultButton("Lam moi");
        btnRefresh.addActionListener(e -> loadData());

        // Table
        String[] columns = {"ID", "Ten dang nhap", "Ho ten", "Email", "Vai tro", "Trang thai"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);

        // Status cell renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = (String) value;
                    if ("Hoat dong".equals(status)) {
                        c.setBackground(new Color(200, 255, 200));
                    } else if ("Khoa".equals(status)) {
                        c.setBackground(new Color(255, 200, 200));
                    } else {
                        c.setBackground(new Color(255, 255, 200));
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
    }

    private void setupLayout() {
        // Top panel - search and buttons
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tim kiem:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnRefresh);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        if (sessionContext.hasPermission("USER_CREATE")) {
            buttonPanel.add(btnCreate);
        }
        if (sessionContext.hasPermission("USER_UPDATE")) {
            buttonPanel.add(btnEdit);
            buttonPanel.add(btnPermissions);
        }
        if (sessionContext.hasPermission("USER_DELETE")) {
            buttonPanel.add(btnDelete);
        }

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Center - table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("Danh sach tai khoan"));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<User> users = dataService.getAllUsers();

        for (User user : users) {
            String status = user.isLocked() ? "Khoa" : (user.isActive() ? "Hoat dong" : "Ngung");
            Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRoleNames(),
                status
            };
            tableModel.addRow(row);
        }
    }

    private void searchUsers() {
        String keyword = txtSearch.getText().toLowerCase().trim();
        tableModel.setRowCount(0);
        List<User> users = dataService.getAllUsers();

        for (User user : users) {
            if (user.getUsername().toLowerCase().contains(keyword) ||
                user.getFullName().toLowerCase().contains(keyword)) {
                String status = user.isLocked() ? "Khoa" : (user.isActive() ? "Hoat dong" : "Ngung");
                Object[] row = {
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRoleNames(),
                    status
                };
                tableModel.addRow(row);
            }
        }
    }

    private void createUser() {
        UserFormDialog dialog = new UserFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSuccessful()) {
            loadData();
        }
    }

    private void editUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon tai khoan can sua",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 1);
        User user = dataService.getUser(username);
        if (user != null) {
            UserFormDialog dialog = new UserFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), user);
            dialog.setVisible(true);
            if (dialog.isSuccessful()) {
                loadData();
            }
        }
    }

    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon tai khoan can xoa",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 1);
        if ("admin".equals(username)) {
            JOptionPane.showMessageDialog(this,
                    "Khong the xoa tai khoan admin!",
                    "Loi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac muon xoa tai khoan '" + username + "'?",
                "Xac nhan xoa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dataService.deleteUser(username)) {
                JOptionPane.showMessageDialog(this,
                        "Da xoa tai khoan thanh cong!",
                        "Thong bao",
                        JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Khong the xoa tai khoan!",
                        "Loi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void managePermissions() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon tai khoan can phan quyen",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 1);
        User user = dataService.getUser(username);
        if (user != null) {
            UserPermissionDialog dialog = new UserPermissionDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), user);
            dialog.setVisible(true);
            loadData();
        }
    }
}

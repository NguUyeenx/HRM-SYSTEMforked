package com.hrm.gui.admin;

import com.hrm.model.Role;
import com.hrm.model.User;
import com.hrm.service.MockDataService;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * User Form Dialog - Create/Edit user accounts
 */
public class UserFormDialog extends JDialog {
    private final MockDataService dataService;
    private final User editingUser;
    private boolean successful = false;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JPanel rolesPanel;
    private JCheckBox chkActive;
    private JCheckBox chkLocked;

    public UserFormDialog(Frame parent, User user) {
        super(parent, user == null ? "Tao tai khoan moi" : "Sua tai khoan", true);
        this.dataService = MockDataService.getInstance();
        this.editingUser = user;

        initComponents();
        setupLayout();
        if (user != null) {
            loadUserData();
        }

        setSize(450, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtFullName = new JTextField(20);
        txtEmail = new JTextField(20);
        chkActive = new JCheckBox("Hoat dong");
        chkActive.setSelected(true);
        chkLocked = new JCheckBox("Khoa tai khoan");

        // Roles panel with checkboxes
        rolesPanel = new JPanel();
        rolesPanel.setLayout(new BoxLayout(rolesPanel, BoxLayout.Y_AXIS));
        List<Role> roles = dataService.getAllRoles();
        for (Role role : roles) {
            JCheckBox chk = new JCheckBox(role.getName() + " (" + role.getCode() + ")");
            chk.setActionCommand(role.getCode());
            rolesPanel.add(chk);
        }

        if (editingUser != null) {
            txtUsername.setEditable(false);
        }
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Ten dang nhap:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel(editingUser == null ? "Mat khau:" : "Mat khau moi:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPassword, gbc);

        // Full name
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Ho ten:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtFullName, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtEmail, gbc);

        // Roles
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Vai tro:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane rolesScroll = new JScrollPane(rolesPanel);
        rolesScroll.setPreferredSize(new Dimension(250, 120));
        formPanel.add(rolesScroll, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Trang thai:"), gbc);
        gbc.gridx = 1;
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusPanel.add(chkActive);
        statusPanel.add(chkLocked);
        formPanel.add(statusPanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = UIHelper.createSuccessButton("Luu");
        btnSave.addActionListener(e -> save());
        JButton btnCancel = UIHelper.createDefaultButton("Huy");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadUserData() {
        txtUsername.setText(editingUser.getUsername());
        txtFullName.setText(editingUser.getFullName());
        txtEmail.setText(editingUser.getEmail());
        chkActive.setSelected(editingUser.isActive());
        chkLocked.setSelected(editingUser.isLocked());

        // Check roles
        for (Component comp : rolesPanel.getComponents()) {
            if (comp instanceof JCheckBox) {
                JCheckBox chk = (JCheckBox) comp;
                chk.setSelected(editingUser.hasRole(chk.getActionCommand()));
            }
        }
    }

    private void save() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();

        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ten dang nhap", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ho ten", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (editingUser == null && password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap mat khau", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (editingUser == null && dataService.isUsernameExists(username)) {
            JOptionPane.showMessageDialog(this, "Ten dang nhap da ton tai", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user;
        if (editingUser == null) {
            // Create new user
            user = dataService.createUser(username, password, fullName, email);
        } else {
            // Update existing user
            user = editingUser;
            if (!password.isEmpty()) {
                user.setPassword(password);
            }
            user.setFullName(fullName);
            user.setEmail(email);
        }

        if (user != null) {
            user.setActive(chkActive.isSelected());
            user.setLocked(chkLocked.isSelected());

            // Update roles
            user.getRoles().clear();
            for (Component comp : rolesPanel.getComponents()) {
                if (comp instanceof JCheckBox) {
                    JCheckBox chk = (JCheckBox) comp;
                    if (chk.isSelected()) {
                        Role role = dataService.getRole(chk.getActionCommand());
                        if (role != null) {
                            user.addRole(role);
                        }
                    }
                }
            }

            dataService.saveUser(user);
            successful = true;

            JOptionPane.showMessageDialog(this,
                    editingUser == null ? "Da tao tai khoan thanh cong!" : "Da cap nhat tai khoan thanh cong!",
                    "Thong bao",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    public boolean isSuccessful() {
        return successful;
    }
}

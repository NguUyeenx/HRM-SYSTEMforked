package com.hrm.gui.admin;

import com.hrm.model.Permission;
import com.hrm.model.Role;
import com.hrm.service.MockDataService;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Role Form Dialog - Create/Edit roles with permission assignment
 */
public class RoleFormDialog extends JDialog {
    private final MockDataService dataService;
    private final Role editingRole;
    private boolean successful = false;

    private JTextField txtCode;
    private JTextField txtName;
    private JTextArea txtDescription;
    private JPanel permissionsPanel;
    private Map<String, JCheckBox> permissionCheckboxes;

    public RoleFormDialog(Frame parent, Role role) {
        super(parent, role == null ? "Tao vai tro moi" : "Sua vai tro", true);
        this.dataService = MockDataService.getInstance();
        this.editingRole = role;
        this.permissionCheckboxes = new HashMap<>();

        initComponents();
        setupLayout();
        if (role != null) {
            loadRoleData();
        }

        setSize(600, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        txtCode = new JTextField(20);
        txtName = new JTextField(20);
        txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        if (editingRole != null) {
            txtCode.setEditable(false);
        }

        // Permissions panel organized by module
        permissionsPanel = new JPanel();
        permissionsPanel.setLayout(new BoxLayout(permissionsPanel, BoxLayout.Y_AXIS));

        List<String> modules = dataService.getPermissionModules();
        for (String module : modules) {
            JPanel modulePanel = new JPanel();
            modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.Y_AXIS));
            modulePanel.setBorder(new TitledBorder(module));
            modulePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            List<Permission> permissions = dataService.getPermissionsByModule(module);
            for (Permission perm : permissions) {
                JCheckBox chk = new JCheckBox(perm.getName() + " (" + perm.getCode() + ")");
                chk.setActionCommand(perm.getCode());
                permissionCheckboxes.put(perm.getCode(), chk);
                modulePanel.add(chk);
            }

            // Add select all / deselect all buttons for this module
            JPanel moduleButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnSelectAll = new JButton("Chon tat ca");
            btnSelectAll.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btnSelectAll.addActionListener(e -> {
                for (Permission perm : permissions) {
                    permissionCheckboxes.get(perm.getCode()).setSelected(true);
                }
            });
            JButton btnDeselectAll = new JButton("Bo chon");
            btnDeselectAll.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btnDeselectAll.addActionListener(e -> {
                for (Permission perm : permissions) {
                    permissionCheckboxes.get(perm.getCode()).setSelected(false);
                }
            });
            moduleButtonPanel.add(btnSelectAll);
            moduleButtonPanel.add(btnDeselectAll);
            modulePanel.add(moduleButtonPanel);

            permissionsPanel.add(modulePanel);
            permissionsPanel.add(Box.createVerticalStrut(5));
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

        // Code
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Ma vai tro:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtCode, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Ten vai tro:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtName, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mo ta:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane descScroll = new JScrollPane(txtDescription);
        formPanel.add(descScroll, gbc);

        // Permissions
        JScrollPane permScroll = new JScrollPane(permissionsPanel);
        permScroll.setBorder(new TitledBorder("Quyen han"));
        permScroll.setPreferredSize(new Dimension(550, 300));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = UIHelper.createSuccessButton("Luu");
        btnSave.addActionListener(e -> save());
        JButton btnCancel = UIHelper.createDefaultButton("Huy");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(permScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadRoleData() {
        txtCode.setText(editingRole.getCode());
        txtName.setText(editingRole.getName());
        txtDescription.setText(editingRole.getDescription());

        // Check permissions
        for (Permission perm : editingRole.getPermissions()) {
            JCheckBox chk = permissionCheckboxes.get(perm.getCode());
            if (chk != null) {
                chk.setSelected(true);
            }
        }
    }

    private void save() {
        String code = txtCode.getText().trim().toUpperCase();
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();

        // Validation
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ma vai tro", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ten vai tro", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (editingRole == null && dataService.getRole(code) != null) {
            JOptionPane.showMessageDialog(this, "Ma vai tro da ton tai", "Loi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Role role;
        if (editingRole == null) {
            role = new Role(code, name, description);
        } else {
            role = editingRole;
            role.setName(name);
            role.setDescription(description);
            role.clearPermissions();
        }

        // Add selected permissions
        for (Map.Entry<String, JCheckBox> entry : permissionCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                Permission perm = dataService.getPermission(entry.getKey());
                if (perm != null) {
                    role.addPermission(perm);
                }
            }
        }

        dataService.saveRole(role);
        successful = true;

        JOptionPane.showMessageDialog(this,
                editingRole == null ? "Da tao vai tro thanh cong!" : "Da cap nhat vai tro thanh cong!",
                "Thong bao",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean isSuccessful() {
        return successful;
    }
}

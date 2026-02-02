package com.hrm.gui.evaluation;

import com.hrm.model.EvalCriteria;
import com.hrm.service.EvaluationService;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Evaluation Configuration Dialog - manage criteria
 */
public class EvalConfigDialog extends JDialog {
    private final EvaluationService evalService;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName;
    private JTextArea txtDescription;
    private JSpinner spnWeight;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JLabel lblTotalWeight;

    private int selectedId = -1;

    public EvalConfigDialog(Frame parent) {
        super(parent, "Cau Hinh Tieu Chi Danh Gia", true);
        this.evalService = EvaluationService.getInstance();

        initComponents();
        setupLayout();
        setupEvents();
        loadData();

        setSize(700, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Table
        String[] columns = {"ID", "Ten tieu chi", "Mo ta", "Trong so (%)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);

        // Form fields
        txtName = new JTextField(20);
        txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        spnWeight = new JSpinner(new SpinnerNumberModel(10, 1, 100, 5));

        // Buttons
        btnAdd = UIHelper.createSuccessButton("Them moi");

        btnUpdate = UIHelper.createPrimaryButton("Cap nhat");
        btnUpdate.setEnabled(false);

        btnDelete = UIHelper.createDangerButton("Xoa");
        btnDelete.setEnabled(false);

        // Total weight label
        lblTotalWeight = new JLabel("Tong trong so: 0%");
        lblTotalWeight.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Table panel
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(new TitledBorder("Danh sach tieu chi"));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Thong tin tieu chi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Ten tieu chi:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mo ta:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(txtDescription), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Trong so (%):"), gbc);
        gbc.gridx = 1;
        formPanel.add(spnWeight, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(Box.createHorizontalStrut(30));
        buttonPanel.add(lblTotalWeight);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Right panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.add(formPanel, BorderLayout.CENTER);

        // Note panel
        JPanel notePanel = new JPanel();
        notePanel.setBackground(new Color(255, 255, 200));
        notePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblNote = new JLabel("<html><b>Luu y:</b> Tong trong so cua tat ca tieu chi phai bang 100%</html>");
        notePanel.add(lblNote);

        // Main layout
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(notePanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void setupEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                selectedId = (int) tableModel.getValueAt(row, 0);
                txtName.setText((String) tableModel.getValueAt(row, 1));
                txtDescription.setText((String) tableModel.getValueAt(row, 2));
                spnWeight.setValue(tableModel.getValueAt(row, 3));
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
            }
        });

        btnAdd.addActionListener(e -> addCriteria());
        btnUpdate.addActionListener(e -> updateCriteria());
        btnDelete.addActionListener(e -> deleteCriteria());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<EvalCriteria> criteriaList = evalService.getAllCriteria();

        for (EvalCriteria c : criteriaList) {
            Object[] row = {c.getId(), c.getName(), c.getDescription(), c.getWeight()};
            tableModel.addRow(row);
        }

        updateTotalWeight();
        clearForm();
    }

    private void updateTotalWeight() {
        int total = evalService.getTotalWeight();
        lblTotalWeight.setText("Tong trong so: " + total + "%");
        if (total == 100) {
            lblTotalWeight.setForeground(new Color(46, 204, 113));
        } else {
            lblTotalWeight.setForeground(Color.RED);
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtDescription.setText("");
        spnWeight.setValue(10);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        table.clearSelection();
    }

    private void addCriteria() {
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        int weight = (int) spnWeight.getValue();

        EvaluationService.ServiceResult<?> result = evalService.saveCriteria(name, description, weight);

        if (result.isSuccess()) {
            loadData();
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCriteria() {
        if (selectedId < 0) return;

        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        int weight = (int) spnWeight.getValue();

        EvaluationService.ServiceResult<?> result = evalService.updateCriteria(
                selectedId, name, description, weight);

        if (result.isSuccess()) {
            loadData();
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCriteria() {
        if (selectedId < 0) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac muon xoa tieu chi nay?",
                "Xac nhan",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            EvaluationService.ServiceResult<?> result = evalService.deleteCriteria(selectedId);
            if (result.isSuccess()) {
                loadData();
            }
        }
    }
}

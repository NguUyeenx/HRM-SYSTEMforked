package com.hrm.gui.leave;

import com.hrm.model.LeaveType;
import com.hrm.model.User;
import com.hrm.service.LeaveService;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Leave Create Dialog
 */
public class LeaveCreateDialog extends JDialog {
    private final LeaveService leaveService;
    private final User currentUser;

    private JComboBox<LeaveType> cboLeaveType;
    private JSpinner spnStartDate;
    private JSpinner spnEndDate;
    private JLabel lblTotalDays;
    private JTextArea txtReason;
    private JButton btnSubmit;
    private JButton btnCancel;

    private boolean successful = false;

    public LeaveCreateDialog(Frame parent) {
        super(parent, "Tao Don Nghi Phep", true);
        this.leaveService = LeaveService.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();

        initComponents();
        setupLayout();
        setupEvents();

        setSize(450, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        // Leave type combo
        cboLeaveType = new JComboBox<>();
        for (LeaveType type : leaveService.getAllLeaveTypes()) {
            cboLeaveType.addItem(type);
        }

        // Date spinners
        SpinnerDateModel startModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnStartDate = new JSpinner(startModel);
        spnStartDate.setEditor(new JSpinner.DateEditor(spnStartDate, "dd/MM/yyyy"));

        SpinnerDateModel endModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spnEndDate = new JSpinner(endModel);
        spnEndDate.setEditor(new JSpinner.DateEditor(spnEndDate, "dd/MM/yyyy"));

        // Total days label
        lblTotalDays = new JLabel("0 ngay lam viec");
        lblTotalDays.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalDays.setForeground(new Color(0, 102, 153));

        // Reason text area
        txtReason = new JTextArea(4, 30);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);

        // Buttons
        btnSubmit = UIHelper.createSuccessButton("Gui don");
        btnCancel = UIHelper.createDefaultButton("Huy");
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Employee info
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Nhan vien:"), gbc);
        gbc.gridx = 1;
        JLabel lblEmployee = new JLabel(currentUser.getFullName());
        lblEmployee.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(lblEmployee, gbc);

        // Leave type
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Loai phep:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cboLeaveType, gbc);

        // Start date
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Tu ngay:"), gbc);
        gbc.gridx = 1;
        formPanel.add(spnStartDate, gbc);

        // End date
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Den ngay:"), gbc);
        gbc.gridx = 1;
        formPanel.add(spnEndDate, gbc);

        // Total days
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Tong so ngay:"), gbc);
        gbc.gridx = 1;
        formPanel.add(lblTotalDays, gbc);

        // Reason
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Ly do:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(new JScrollPane(txtReason), gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnCancel);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void setupEvents() {
        // Calculate days when dates change
        spnStartDate.addChangeListener(e -> calculateDays());
        spnEndDate.addChangeListener(e -> calculateDays());

        btnSubmit.addActionListener(e -> submitRequest());
        btnCancel.addActionListener(e -> dispose());

        calculateDays();
    }

    private void calculateDays() {
        Date startDate = (Date) spnStartDate.getValue();
        Date endDate = (Date) spnEndDate.getValue();

        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        int days = leaveService.calculateBusinessDays(start, end);
        lblTotalDays.setText(days + " ngay lam viec");
    }

    private void submitRequest() {
        LeaveType leaveType = (LeaveType) cboLeaveType.getSelectedItem();
        Date startDate = (Date) spnStartDate.getValue();
        Date endDate = (Date) spnEndDate.getValue();
        String reason = txtReason.getText().trim();

        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui long nhap ly do nghi phep",
                    "Loi",
                    JOptionPane.ERROR_MESSAGE);
            txtReason.requestFocus();
            return;
        }

        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LeaveService.ServiceResult<?> result = leaveService.createRequest(
                currentUser.getId(),
                currentUser.getFullName(),
                leaveType.getCode(),
                start,
                end,
                reason);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "Thanh cong",
                    JOptionPane.INFORMATION_MESSAGE);
            successful = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "Loi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccessful() {
        return successful;
    }
}

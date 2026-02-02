package com.hrm.gui.leave;

import com.hrm.model.LeaveRequest;
import com.hrm.model.User;
import com.hrm.service.LeaveService;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Leave Approve Dialog
 */
public class LeaveApproveDialog extends JDialog {
    private final LeaveService leaveService;
    private final User currentUser;
    private final int requestId;
    private LeaveRequest request;

    private JTextArea txtNote;
    private JButton btnApprove;
    private JButton btnReject;
    private JButton btnCancel;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LeaveApproveDialog(Frame parent, int requestId) {
        super(parent, "Duyet Don Nghi Phep", true);
        this.leaveService = LeaveService.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.requestId = requestId;
        this.request = leaveService.getRequest(requestId);

        if (request == null) {
            JOptionPane.showMessageDialog(parent,
                    "Khong tim thay don nghi phep",
                    "Loi",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initComponents();
        setupLayout();
        setupEvents();

        setSize(500, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        txtNote = new JTextArea(3, 30);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);

        btnApprove = UIHelper.createSuccessButton("Duyet");
        btnApprove.setPreferredSize(new Dimension(100, 35));

        btnReject = UIHelper.createDangerButton("Tu choi");
        btnReject.setPreferredSize(new Dimension(100, 35));

        btnCancel = UIHelper.createDefaultButton("Dong");
        btnCancel.setPreferredSize(new Dimension(100, 35));
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Request info panel
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        infoPanel.setBorder(new TitledBorder("Thong tin don nghi phep"));

        infoPanel.add(new JLabel("Ma don:"));
        infoPanel.add(createValueLabel("#" + request.getId()));

        infoPanel.add(new JLabel("Nhan vien:"));
        infoPanel.add(createValueLabel(request.getEmployeeName()));

        infoPanel.add(new JLabel("Loai phep:"));
        infoPanel.add(createValueLabel(request.getLeaveTypeName()));

        infoPanel.add(new JLabel("Tu ngay:"));
        infoPanel.add(createValueLabel(request.getStartDate().format(DATE_FORMAT)));

        infoPanel.add(new JLabel("Den ngay:"));
        infoPanel.add(createValueLabel(request.getEndDate().format(DATE_FORMAT)));

        infoPanel.add(new JLabel("So ngay:"));
        infoPanel.add(createValueLabel(request.getTotalDays() + " ngay"));

        infoPanel.add(new JLabel("Trang thai:"));
        JLabel lblStatus = createValueLabel(request.getStatus().getDisplayName());
        if (request.getStatus() == LeaveRequest.Status.PENDING) {
            lblStatus.setForeground(new Color(230, 126, 34));
        }
        infoPanel.add(lblStatus);

        // Reason panel
        JPanel reasonPanel = new JPanel(new BorderLayout(5, 5));
        reasonPanel.setBorder(new TitledBorder("Ly do nghi phep"));
        JTextArea txtReason = new JTextArea(request.getReason());
        txtReason.setEditable(false);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setBackground(new Color(245, 245, 245));
        reasonPanel.add(new JScrollPane(txtReason), BorderLayout.CENTER);

        // Note panel
        JPanel notePanel = new JPanel(new BorderLayout(5, 5));
        notePanel.setBorder(new TitledBorder("Ghi chu cua nguoi duyet"));
        notePanel.add(new JScrollPane(txtNote), BorderLayout.CENTER);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(infoPanel, BorderLayout.NORTH);

        JPanel middlePanel = new JPanel(new GridLayout(2, 1, 10, 10));
        middlePanel.add(reasonPanel);
        middlePanel.add(notePanel);
        centerPanel.add(middlePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.add(btnApprove);
        buttonPanel.add(btnReject);
        buttonPanel.add(btnCancel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private void setupEvents() {
        btnApprove.addActionListener(e -> processRequest(true));
        btnReject.addActionListener(e -> processRequest(false));
        btnCancel.addActionListener(e -> dispose());
    }

    private void processRequest(boolean approve) {
        String note = txtNote.getText().trim();

        if (!approve && note.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui long nhap ly do tu choi",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            txtNote.requestFocus();
            return;
        }

        String action = approve ? "duyet" : "tu choi";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac muon " + action + " don nghi phep nay?",
                "Xac nhan",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        LeaveService.ServiceResult<?> result = leaveService.processRequest(
                requestId,
                approve,
                currentUser.getId(),
                currentUser.getFullName(),
                note);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "Thanh cong",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "Loi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

package com.hrm.gui.leave;

import com.hrm.model.LeaveBalance;
import com.hrm.model.LeaveRequest;
import com.hrm.model.User;
import com.hrm.service.LeaveService;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Leave List Panel - displays leave requests
 */
public class LeaveListPanel extends JPanel {
    private final LeaveService leaveService;
    private final User currentUser;
    private final boolean isManager;

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboFilter;
    private JButton btnCreate;
    private JButton btnApprove;
    private JButton btnRefresh;
    private JPanel balancePanel;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LeaveListPanel() {
        this.leaveService = LeaveService.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.isManager = currentUser.hasRole("MANAGER") || currentUser.hasRole("HR")
                || currentUser.hasRole("ADMIN") || currentUser.hasRole("DIRECTOR");

        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter combo
        String[] filters = isManager
                ? new String[]{"Don cua toi", "Cho duyet", "Tat ca"}
                : new String[]{"Don cua toi"};
        cboFilter = new JComboBox<>(filters);
        cboFilter.addActionListener(e -> loadData());

        // Buttons
        btnCreate = UIHelper.createSuccessButton("Tao don moi");
        btnCreate.addActionListener(e -> createRequest());

        btnApprove = UIHelper.createPrimaryButton("Duyet don");
        btnApprove.setEnabled(isManager);
        btnApprove.addActionListener(e -> approveRequest());

        btnRefresh = UIHelper.createDefaultButton("Lam moi");
        btnRefresh.addActionListener(e -> loadData());

        // Table
        String[] columns = {"ID", "Nhan vien", "Loai phep", "Tu ngay", "Den ngay",
                "So ngay", "Ly do", "Trang thai", "Nguoi duyet"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);

        // Status cell renderer
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = (String) value;
                    if ("Da duyet".equals(status)) {
                        c.setBackground(new Color(200, 255, 200));
                    } else if ("Tu choi".equals(status)) {
                        c.setBackground(new Color(255, 200, 200));
                    } else if ("Cho duyet".equals(status)) {
                        c.setBackground(new Color(255, 255, 200));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        // Balance Panel
        balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        balancePanel.setBorder(new TitledBorder("So ngay phep con lai"));
    }

    private void setupLayout() {
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.add(new JLabel("Hien thi:"));
        filterPanel.add(cboFilter);
        filterPanel.add(btnRefresh);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(btnCreate);
        if (isManager) {
            buttonPanel.add(btnApprove);
        }

        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        topPanel.add(balancePanel, BorderLayout.SOUTH);

        // Center panel - table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("Danh sach don nghi phep"));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String filter = (String) cboFilter.getSelectedItem();

        List<LeaveRequest> requests;
        if ("Cho duyet".equals(filter)) {
            requests = leaveService.getPendingRequests();
        } else if ("Tat ca".equals(filter)) {
            requests = leaveService.getAllRequests();
        } else {
            requests = leaveService.getMyRequests(currentUser.getId());
        }

        for (LeaveRequest req : requests) {
            Object[] row = {
                req.getId(),
                req.getEmployeeName(),
                req.getLeaveTypeName(),
                req.getStartDate().format(DATE_FORMAT),
                req.getEndDate().format(DATE_FORMAT),
                req.getTotalDays(),
                req.getReason(),
                req.getStatus().getDisplayName(),
                req.getApproverName() != null ? req.getApproverName() : "-"
            };
            tableModel.addRow(row);
        }

        // Update balance display
        updateBalanceDisplay();
    }

    private void updateBalanceDisplay() {
        balancePanel.removeAll();
        List<LeaveBalance> balances = leaveService.getBalances(currentUser.getId());
        for (LeaveBalance balance : balances) {
            JLabel lbl = new JLabel(getLeaveTypeName(balance.getLeaveTypeCode()) +
                    ": " + balance.getRemainingDays() + "/" + balance.getTotalDays() + " ngay");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (balance.getRemainingDays() <= 3) {
                lbl.setForeground(Color.RED);
            }
            balancePanel.add(lbl);
        }
        balancePanel.revalidate();
        balancePanel.repaint();
    }

    private String getLeaveTypeName(String code) {
        return leaveService.getAllLeaveTypes().stream()
                .filter(t -> t.getCode().equals(code))
                .map(t -> t.getName())
                .findFirst()
                .orElse(code);
    }

    private void createRequest() {
        LeaveCreateDialog dialog = new LeaveCreateDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSuccessful()) {
            loadData();
        }
    }

    private void approveRequest() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon don can duyet",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int requestId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 7);

        if (!"Cho duyet".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Chi co the duyet don dang cho duyet",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LeaveApproveDialog dialog = new LeaveApproveDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), requestId);
        dialog.setVisible(true);
        loadData();
    }
}

package com.hrm.gui.evaluation;

import com.hrm.model.EvalCycle;
import com.hrm.model.EvalSubmission;
import com.hrm.model.User;
import com.hrm.service.EvaluationService;
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
 * Evaluation Cycle List Panel
 */
public class EvalCycleListPanel extends JPanel {
    private final EvaluationService evalService;
    private final User currentUser;
    private final boolean isAdmin;
    private final boolean isManager;

    private JTable cycleTable;
    private DefaultTableModel cycleTableModel;
    private JTable resultTable;
    private DefaultTableModel resultTableModel;
    private JButton btnOpenCycle;
    private JButton btnCloseCycle;
    private JButton btnConfigCriteria;
    private JButton btnEvaluate;
    private JButton btnViewResults;
    private JButton btnRefresh;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EvalCycleListPanel() {
        this.evalService = EvaluationService.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.isAdmin = currentUser.hasRole("ADMIN") || currentUser.hasRole("HR");
        this.isManager = currentUser.hasRole("MANAGER") || currentUser.hasRole("DIRECTOR") || isAdmin;

        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Cycle table
        String[] cycleColumns = {"ID", "Ten ky danh gia", "Quy", "Nam", "Bat dau", "Ket thuc", "Trang thai"};
        cycleTableModel = new DefaultTableModel(cycleColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cycleTable = new JTable(cycleTableModel);
        cycleTable.setRowHeight(28);
        cycleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cycleTable.getSelectionModel().addListSelectionListener(e -> onCycleSelected());

        // Status cell renderer
        cycleTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = (String) value;
                    if ("Dang mo".equals(status)) {
                        c.setBackground(new Color(200, 255, 200));
                    } else if ("Da dong".equals(status)) {
                        c.setBackground(new Color(220, 220, 220));
                    } else {
                        c.setBackground(new Color(255, 255, 200));
                    }
                }
                return c;
            }
        });

        // Result table
        String[] resultColumns = {"Nhan vien", "Nguoi danh gia", "Diem", "Xep loai", "Ngay nop"};
        resultTableModel = new DefaultTableModel(resultColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(resultTableModel);
        resultTable.setRowHeight(25);

        // Buttons
        btnOpenCycle = UIHelper.createSuccessButton("Mo ky danh gia");
        btnOpenCycle.setEnabled(false);

        btnCloseCycle = UIHelper.createWarningButton("Dong ky danh gia");
        btnCloseCycle.setEnabled(false);

        btnConfigCriteria = UIHelper.createDefaultButton("Cau hinh tieu chi");
        btnConfigCriteria.setEnabled(isAdmin);

        btnEvaluate = UIHelper.createPrimaryButton("Danh gia nhan vien");
        btnEvaluate.setEnabled(isManager);

        btnViewResults = UIHelper.createDefaultButton("Xem ket qua");

        btnRefresh = UIHelper.createDefaultButton("Lam moi");
        btnRefresh.addActionListener(e -> loadData());
    }

    private void setupLayout() {
        // Top panel - Admin buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(btnRefresh);
        if (isAdmin) {
            topPanel.add(btnOpenCycle);
            topPanel.add(btnCloseCycle);
            topPanel.add(btnConfigCriteria);
        }
        if (isManager) {
            topPanel.add(btnEvaluate);
        }
        topPanel.add(btnViewResults);

        // Split pane for cycles and results
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(200);

        // Cycle panel
        JScrollPane cycleScroll = new JScrollPane(cycleTable);
        cycleScroll.setBorder(new TitledBorder("Cac ky danh gia"));
        splitPane.setTopComponent(cycleScroll);

        // Result panel
        JScrollPane resultScroll = new JScrollPane(resultTable);
        resultScroll.setBorder(new TitledBorder("Ket qua danh gia"));
        splitPane.setBottomComponent(resultScroll);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Setup button actions
        btnOpenCycle.addActionListener(e -> openCycle());
        btnCloseCycle.addActionListener(e -> closeCycle());
        btnConfigCriteria.addActionListener(e -> configCriteria());
        btnEvaluate.addActionListener(e -> evaluateEmployee());
        btnViewResults.addActionListener(e -> viewResults());
    }

    private void loadData() {
        cycleTableModel.setRowCount(0);
        resultTableModel.setRowCount(0);

        List<EvalCycle> cycles = evalService.getAllCycles();
        for (EvalCycle cycle : cycles) {
            Object[] row = {
                cycle.getId(),
                cycle.getName(),
                "Q" + cycle.getQuarter(),
                cycle.getYear(),
                cycle.getStartDate().format(DATE_FORMAT),
                cycle.getEndDate().format(DATE_FORMAT),
                cycle.getStatus().getDisplayName()
            };
            cycleTableModel.addRow(row);
        }

        // Load my evaluations
        loadMyResults();
    }

    private void loadMyResults() {
        resultTableModel.setRowCount(0);
        List<EvalSubmission> submissions = evalService.getSubmissionsByEmployee(currentUser.getId());
        for (EvalSubmission sub : submissions) {
            Object[] row = {
                sub.getEmployeeName(),
                sub.getEvaluatorName(),
                String.format("%.2f", sub.getOverallScore()),
                sub.getRating().getDisplayName(),
                sub.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            };
            resultTableModel.addRow(row);
        }
    }

    private void onCycleSelected() {
        int row = cycleTable.getSelectedRow();
        if (row >= 0) {
            String status = (String) cycleTableModel.getValueAt(row, 6);
            btnOpenCycle.setEnabled(isAdmin && "Nhap".equals(status));
            btnCloseCycle.setEnabled(isAdmin && "Dang mo".equals(status));
            btnEvaluate.setEnabled(isManager && "Dang mo".equals(status));

            // Load submissions for this cycle
            int cycleId = (int) cycleTableModel.getValueAt(row, 0);
            loadCycleResults(cycleId);
        }
    }

    private void loadCycleResults(int cycleId) {
        resultTableModel.setRowCount(0);
        List<EvalSubmission> submissions = evalService.getSubmissionsByCycle(cycleId);
        for (EvalSubmission sub : submissions) {
            Object[] row = {
                sub.getEmployeeName(),
                sub.getEvaluatorName(),
                String.format("%.2f", sub.getOverallScore()),
                sub.getRating().getDisplayName(),
                sub.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            };
            resultTableModel.addRow(row);
        }
    }

    private void openCycle() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) return;

        int cycleId = (int) cycleTableModel.getValueAt(row, 0);
        EvaluationService.ServiceResult<?> result = evalService.openCycle(cycleId);

        JOptionPane.showMessageDialog(this,
                result.getMessage(),
                result.isSuccess() ? "Thanh cong" : "Loi",
                result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        if (result.isSuccess()) {
            loadData();
        }
    }

    private void closeCycle() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) return;

        int cycleId = (int) cycleTableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac muon dong ky danh gia nay?\nSau khi dong se khong the sua doi.",
                "Xac nhan",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            EvaluationService.ServiceResult<?> result = evalService.closeCycle(cycleId);
            JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    result.isSuccess() ? "Thanh cong" : "Loi",
                    result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

            if (result.isSuccess()) {
                loadData();
            }
        }
    }

    private void configCriteria() {
        EvalConfigDialog dialog = new EvalConfigDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private void evaluateEmployee() {
        int row = cycleTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon ky danh gia",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cycleId = (int) cycleTableModel.getValueAt(row, 0);
        String cycleName = (String) cycleTableModel.getValueAt(row, 1);

        EvalDoDialog dialog = new EvalDoDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), cycleId, cycleName);
        dialog.setVisible(true);

        if (dialog.isSuccessful()) {
            loadData();
            onCycleSelected();
        }
    }

    private void viewResults() {
        EvalResultPanel resultPanel = new EvalResultPanel();
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Ket qua danh gia", true);
        dialog.setContentPane(resultPanel);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}

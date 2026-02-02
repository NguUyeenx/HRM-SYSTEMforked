package com.hrm.gui.evaluation;

import com.hrm.model.EvalScore;
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
 * Evaluation Result Panel - view evaluation results
 */
public class EvalResultPanel extends JPanel {
    private final EvaluationService evalService;
    private final User currentUser;
    private final boolean isAdmin;

    private JTable submissionTable;
    private DefaultTableModel submissionModel;
    private JTable detailTable;
    private DefaultTableModel detailModel;
    private JTextArea txtComment;
    private JComboBox<String> cboFilter;

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EvalResultPanel() {
        this.evalService = EvaluationService.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.isAdmin = currentUser.hasRole("ADMIN") || currentUser.hasRole("HR")
                || currentUser.hasRole("MANAGER") || currentUser.hasRole("DIRECTOR");

        initComponents();
        setupLayout();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter
        String[] filters = isAdmin
                ? new String[]{"Cua toi", "Tat ca"}
                : new String[]{"Cua toi"};
        cboFilter = new JComboBox<>(filters);
        cboFilter.addActionListener(e -> loadData());

        // Submission table
        String[] subColumns = {"ID", "Ky danh gia", "Nhan vien", "Nguoi danh gia", "Diem", "Xep loai", "Ngay"};
        submissionModel = new DefaultTableModel(subColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        submissionTable = new JTable(submissionModel);
        submissionTable.setRowHeight(28);
        submissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        submissionTable.getSelectionModel().addListSelectionListener(e -> showDetail());

        // Rating color renderer
        submissionTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String rating = (String) value;
                    if ("Xuat sac".equals(rating)) {
                        c.setBackground(new Color(200, 255, 200));
                        c.setForeground(new Color(0, 100, 0));
                    } else if ("Tot".equals(rating)) {
                        c.setBackground(new Color(220, 255, 220));
                        c.setForeground(new Color(0, 128, 0));
                    } else if ("Kha".equals(rating)) {
                        c.setBackground(new Color(255, 255, 200));
                        c.setForeground(new Color(128, 128, 0));
                    } else if ("Trung binh".equals(rating)) {
                        c.setBackground(new Color(255, 220, 200));
                        c.setForeground(new Color(200, 100, 0));
                    } else {
                        c.setBackground(new Color(255, 200, 200));
                        c.setForeground(Color.RED);
                    }
                }
                return c;
            }
        });

        // Detail table
        String[] detailColumns = {"Tieu chi", "Trong so", "Diem", "Diem quy doi", "Nhan xet"};
        detailModel = new DefaultTableModel(detailColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        detailTable = new JTable(detailModel);
        detailTable.setRowHeight(25);

        // Comment area
        txtComment = new JTextArea(3, 40);
        txtComment.setEditable(false);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.setBackground(new Color(245, 245, 245));
    }

    private void setupLayout() {
        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(new JLabel("Hien thi:"));
        topPanel.add(cboFilter);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(250);

        // Top - Submission list
        JScrollPane subScroll = new JScrollPane(submissionTable);
        subScroll.setBorder(new TitledBorder("Danh sach danh gia"));
        splitPane.setTopComponent(subScroll);

        // Bottom - Detail panel
        JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
        detailPanel.setBorder(new TitledBorder("Chi tiet danh gia"));

        JScrollPane detailScroll = new JScrollPane(detailTable);

        JPanel commentPanel = new JPanel(new BorderLayout(5, 5));
        commentPanel.add(new JLabel("Nhan xet chung:"), BorderLayout.NORTH);
        commentPanel.add(new JScrollPane(txtComment), BorderLayout.CENTER);

        detailPanel.add(detailScroll, BorderLayout.CENTER);
        detailPanel.add(commentPanel, BorderLayout.SOUTH);

        splitPane.setBottomComponent(detailPanel);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadData() {
        submissionModel.setRowCount(0);
        detailModel.setRowCount(0);
        txtComment.setText("");

        String filter = (String) cboFilter.getSelectedItem();
        List<EvalSubmission> submissions;

        if ("Tat ca".equals(filter)) {
            submissions = evalService.getAllSubmissions();
        } else {
            submissions = evalService.getSubmissionsByEmployee(currentUser.getId());
        }

        for (EvalSubmission sub : submissions) {
            Object[] row = {
                sub.getId(),
                sub.getCycleName(),
                sub.getEmployeeName(),
                sub.getEvaluatorName(),
                String.format("%.2f", sub.getOverallScore()),
                sub.getRating().getDisplayName(),
                sub.getSubmittedAt().format(DT_FORMAT)
            };
            submissionModel.addRow(row);
        }
    }

    private void showDetail() {
        int row = submissionTable.getSelectedRow();
        if (row < 0) {
            detailModel.setRowCount(0);
            txtComment.setText("");
            return;
        }

        detailModel.setRowCount(0);

        // Find the submission
        String filter = (String) cboFilter.getSelectedItem();
        List<EvalSubmission> submissions = "Tat ca".equals(filter)
                ? evalService.getAllSubmissions()
                : evalService.getSubmissionsByEmployee(currentUser.getId());

        if (row < submissions.size()) {
            EvalSubmission sub = submissions.get(row);

            for (EvalScore score : sub.getScores()) {
                Object[] detailRow = {
                    score.getCriteriaName(),
                    score.getWeight() + "%",
                    String.format("%.1f", score.getScore()),
                    String.format("%.2f", score.getWeightedScore()),
                    score.getComment() != null ? score.getComment() : ""
                };
                detailModel.addRow(detailRow);
            }

            // Add total row
            detailModel.addRow(new Object[]{
                "TONG CONG", "100%", "",
                String.format("%.2f", sub.getOverallScore()),
                sub.getRating().getDisplayName()
            });

            txtComment.setText(sub.getGeneralComment() != null ? sub.getGeneralComment() : "");
        }
    }
}

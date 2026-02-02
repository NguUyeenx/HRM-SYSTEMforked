package com.hrm.gui.evaluation;

import com.hrm.model.EvalCriteria;
import com.hrm.model.EvalScore;
import com.hrm.model.User;
import com.hrm.service.EvaluationService;
import com.hrm.service.MockDataService;
import com.hrm.util.SessionContext;
import com.hrm.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluation Do Dialog - manager evaluates employee
 */
public class EvalDoDialog extends JDialog {
    private final EvaluationService evalService;
    private final User currentUser;
    private final int cycleId;
    private final String cycleName;

    private JComboBox<UserItem> cboEmployee;
    private JPanel criteriaPanel;
    private Map<Integer, JSpinner> scoreSpinners;
    private Map<Integer, JTextArea> commentFields;
    private JTextArea txtGeneralComment;
    private JLabel lblPreviewScore;
    private JButton btnSubmit;
    private JButton btnCancel;

    private boolean successful = false;

    public EvalDoDialog(Frame parent, int cycleId, String cycleName) {
        super(parent, "Danh Gia Nhan Vien", true);
        this.evalService = EvaluationService.getInstance();
        this.currentUser = SessionContext.getInstance().getCurrentUser();
        this.cycleId = cycleId;
        this.cycleName = cycleName;
        this.scoreSpinners = new HashMap<>();
        this.commentFields = new HashMap<>();

        initComponents();
        setupLayout();
        setupEvents();

        setSize(700, 650);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Employee combo
        cboEmployee = new JComboBox<>();
        // Add mock employees (in real app, this would come from employee service)
        List<User> users = MockDataService.getInstance().getAllUsers();
        for (User user : users) {
            if (!user.hasRole("ADMIN") && user.getId() != currentUser.getId()) {
                cboEmployee.addItem(new UserItem(user.getId(), user.getFullName()));
            }
        }

        // Criteria panel
        criteriaPanel = new JPanel();
        criteriaPanel.setLayout(new BoxLayout(criteriaPanel, BoxLayout.Y_AXIS));

        // Build criteria inputs
        List<EvalCriteria> criteria = evalService.getAllCriteria();
        for (EvalCriteria c : criteria) {
            JPanel cPanel = createCriteriaPanel(c);
            criteriaPanel.add(cPanel);
            criteriaPanel.add(Box.createVerticalStrut(10));
        }

        // General comment
        txtGeneralComment = new JTextArea(3, 40);
        txtGeneralComment.setLineWrap(true);
        txtGeneralComment.setWrapStyleWord(true);

        // Preview score
        lblPreviewScore = new JLabel("Diem du kien: 0.00");
        lblPreviewScore.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPreviewScore.setForeground(new Color(0, 102, 153));

        // Buttons
        btnSubmit = UIHelper.createSuccessButton("Luu danh gia");
        btnSubmit.setPreferredSize(new Dimension(150, 40));

        btnCancel = UIHelper.createDefaultButton("Huy");
        btnCancel.setPreferredSize(new Dimension(100, 40));
    }

    private JPanel createCriteriaPanel(EvalCriteria criteria) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 10, 10, 10)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblName = new JLabel(criteria.getName() + " (" + criteria.getWeight() + "%)");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerPanel.add(lblName, BorderLayout.WEST);

        // Score spinner
        JSpinner spnScore = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 10.0, 0.5));
        spnScore.setPreferredSize(new Dimension(80, 30));
        ((JSpinner.NumberEditor) spnScore.getEditor()).getFormat().setMaximumFractionDigits(1);
        scoreSpinners.put(criteria.getId(), spnScore);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        scorePanel.add(new JLabel("Diem (1-10):"));
        scorePanel.add(spnScore);
        headerPanel.add(scorePanel, BorderLayout.EAST);

        // Description
        JLabel lblDesc = new JLabel("<html><i>" + (criteria.getDescription() != null ?
                criteria.getDescription() : "") + "</i></html>");
        lblDesc.setForeground(Color.GRAY);

        // Comment
        JTextArea txtComment = new JTextArea(2, 30);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        commentFields.put(criteria.getId(), txtComment);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(lblDesc, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(txtComment), BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top panel - cycle info and employee selection
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        topPanel.setBorder(new TitledBorder("Thong tin danh gia"));

        topPanel.add(new JLabel("Ky danh gia:"));
        JLabel lblCycle = new JLabel(cycleName);
        lblCycle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topPanel.add(lblCycle);

        topPanel.add(new JLabel("Nhan vien:"));
        topPanel.add(cboEmployee);

        // Criteria scroll
        JScrollPane criteriaScroll = new JScrollPane(criteriaPanel);
        criteriaScroll.setBorder(new TitledBorder("Cac tieu chi danh gia"));
        criteriaScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        // General comment
        JPanel commentPanel = new JPanel(new BorderLayout(5, 5));
        commentPanel.setBorder(new TitledBorder("Nhan xet chung"));
        commentPanel.add(new JScrollPane(txtGeneralComment), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(lblPreviewScore);
        buttonPanel.add(Box.createHorizontalStrut(30));
        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnCancel);

        bottomPanel.add(commentPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(criteriaScroll, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void setupEvents() {
        // Update preview when scores change
        for (JSpinner spn : scoreSpinners.values()) {
            spn.addChangeListener(e -> updatePreview());
        }

        btnSubmit.addActionListener(e -> submitEvaluation());
        btnCancel.addActionListener(e -> dispose());

        updatePreview();
    }

    private void updatePreview() {
        List<EvalScore> scores = buildScores();
        double overall = evalService.calculateOverallScore(scores);
        String rating = evalService.getRatingFromScore(overall).getDisplayName();
        lblPreviewScore.setText(String.format("Diem du kien: %.2f - %s", overall, rating));
    }

    private List<EvalScore> buildScores() {
        List<EvalScore> scores = new ArrayList<>();
        List<EvalCriteria> criteria = evalService.getAllCriteria();

        for (EvalCriteria c : criteria) {
            JSpinner spn = scoreSpinners.get(c.getId());
            JTextArea txt = commentFields.get(c.getId());

            if (spn != null) {
                EvalScore score = new EvalScore(
                        c.getId(),
                        c.getName(),
                        c.getWeight(),
                        ((Number) spn.getValue()).doubleValue()
                );
                if (txt != null) {
                    score.setComment(txt.getText().trim());
                }
                scores.add(score);
            }
        }
        return scores;
    }

    private void submitEvaluation() {
        UserItem selectedUser = (UserItem) cboEmployee.getSelectedItem();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui long chon nhan vien can danh gia",
                    "Thong bao",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<EvalScore> scores = buildScores();
        String generalComment = txtGeneralComment.getText().trim();

        EvaluationService.ServiceResult<?> result = evalService.submitEvaluation(
                cycleId,
                selectedUser.id,
                selectedUser.name,
                currentUser.getId(),
                currentUser.getFullName(),
                scores,
                generalComment
        );

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

    // Helper class for combo box
    private static class UserItem {
        int id;
        String name;

        UserItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

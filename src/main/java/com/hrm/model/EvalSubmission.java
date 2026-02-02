package com.hrm.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluation Submission model - represents a completed evaluation
 */
public class EvalSubmission {
    public enum Rating {
        EXCELLENT("Xuat sac", 9.0, 10.0),
        GOOD("Tot", 8.0, 8.9),
        FAIR("Kha", 6.5, 7.9),
        AVERAGE("Trung binh", 5.0, 6.4),
        POOR("Yeu", 0.0, 4.9);

        private final String displayName;
        private final double minScore;
        private final double maxScore;

        Rating(String displayName, double minScore, double maxScore) {
            this.displayName = displayName;
            this.minScore = minScore;
            this.maxScore = maxScore;
        }

        public String getDisplayName() { return displayName; }

        public static Rating fromScore(double score) {
            for (Rating r : values()) {
                if (score >= r.minScore && score <= r.maxScore) {
                    return r;
                }
            }
            return POOR;
        }
    }

    private int id;
    private int cycleId;
    private String cycleName;
    private int employeeId;
    private String employeeName;
    private int evaluatorId;
    private String evaluatorName;
    private List<EvalScore> scores;
    private double overallScore;
    private Rating rating;
    private String generalComment;
    private LocalDateTime submittedAt;

    public EvalSubmission() {
        this.scores = new ArrayList<>();
        this.submittedAt = LocalDateTime.now();
    }

    public void calculateOverall() {
        if (scores.isEmpty()) {
            this.overallScore = 0;
            this.rating = Rating.POOR;
            return;
        }

        double total = 0;
        for (EvalScore score : scores) {
            total += score.getWeightedScore();
        }
        this.overallScore = Math.round(total * 100.0) / 100.0;
        this.rating = Rating.fromScore(overallScore);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCycleId() { return cycleId; }
    public void setCycleId(int cycleId) { this.cycleId = cycleId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public int getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(int evaluatorId) { this.evaluatorId = evaluatorId; }

    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }

    public List<EvalScore> getScores() { return scores; }
    public void setScores(List<EvalScore> scores) { this.scores = scores; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public Rating getRating() { return rating; }
    public void setRating(Rating rating) { this.rating = rating; }

    public String getGeneralComment() { return generalComment; }
    public void setGeneralComment(String generalComment) { this.generalComment = generalComment; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}

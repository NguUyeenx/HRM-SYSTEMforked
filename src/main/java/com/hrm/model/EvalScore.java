package com.hrm.model;

/**
 * Individual score for a criteria in an evaluation
 */
public class EvalScore {
    private int criteriaId;
    private String criteriaName;
    private int weight;
    private double score;
    private String comment;

    public EvalScore() {}

    public EvalScore(int criteriaId, String criteriaName, int weight, double score) {
        this.criteriaId = criteriaId;
        this.criteriaName = criteriaName;
        this.weight = weight;
        this.score = score;
    }

    public double getWeightedScore() {
        return score * weight / 100.0;
    }

    // Getters and Setters
    public int getCriteriaId() { return criteriaId; }
    public void setCriteriaId(int criteriaId) { this.criteriaId = criteriaId; }

    public String getCriteriaName() { return criteriaName; }
    public void setCriteriaName(String criteriaName) { this.criteriaName = criteriaName; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

package com.hrm.model;

/**
 * Evaluation Criteria model
 */
public class EvalCriteria {
    private int id;
    private String name;
    private String description;
    private int weight; // percentage (total must = 100)
    private int maxScore;
    private boolean active;

    public EvalCriteria() {
        this.maxScore = 10;
        this.active = true;
    }

    public EvalCriteria(int id, String name, String description, int weight) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.maxScore = 10;
        this.active = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name + " (" + weight + "%)";
    }
}

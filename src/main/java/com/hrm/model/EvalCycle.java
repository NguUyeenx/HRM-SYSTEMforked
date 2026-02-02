package com.hrm.model;

import java.time.LocalDate;

/**
 * Evaluation Cycle model
 */
public class EvalCycle {
    public enum Status {
        DRAFT("Nhap"),
        OPEN("Dang mo"),
        CLOSED("Da dong");

        private final String displayName;
        Status(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private int id;
    private String name;
    private int year;
    private int quarter;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate evaluationDeadline;
    private Status status;

    public EvalCycle() {
        this.status = Status.DRAFT;
    }

    public EvalCycle(int id, String name, int year, int quarter,
                     LocalDate startDate, LocalDate endDate, LocalDate evaluationDeadline) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.quarter = quarter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.evaluationDeadline = evaluationDeadline;
        this.status = Status.DRAFT;
    }

    public boolean isOpen() {
        return status == Status.OPEN;
    }

    public boolean isClosed() {
        return status == Status.CLOSED;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getQuarter() { return quarter; }
    public void setQuarter(int quarter) { this.quarter = quarter; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getEvaluationDeadline() { return evaluationDeadline; }
    public void setEvaluationDeadline(LocalDate evaluationDeadline) { this.evaluationDeadline = evaluationDeadline; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return name + " (Q" + quarter + "/" + year + ")";
    }
}

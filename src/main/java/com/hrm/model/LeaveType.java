package com.hrm.model;

/**
 * Leave Type model
 */
public class LeaveType {
    private String code;
    private String name;
    private int defaultDays;
    private boolean paid;

    public LeaveType(String code, String name, int defaultDays, boolean paid) {
        this.code = code;
        this.name = name;
        this.defaultDays = defaultDays;
        this.paid = paid;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDefaultDays() { return defaultDays; }
    public void setDefaultDays(int defaultDays) { this.defaultDays = defaultDays; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}

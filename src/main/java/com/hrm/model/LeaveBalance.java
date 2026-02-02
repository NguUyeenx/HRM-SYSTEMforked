package com.hrm.model;

/**
 * Leave Balance model - tracks remaining leave days per employee per type
 */
public class LeaveBalance {
    private int employeeId;
    private String leaveTypeCode;
    private int year;
    private int totalDays;
    private int usedDays;

    public LeaveBalance(int employeeId, String leaveTypeCode, int year, int totalDays) {
        this.employeeId = employeeId;
        this.leaveTypeCode = leaveTypeCode;
        this.year = year;
        this.totalDays = totalDays;
        this.usedDays = 0;
    }

    public int getRemainingDays() {
        return totalDays - usedDays;
    }

    public void deductDays(int days) {
        this.usedDays += days;
    }

    public void restoreDays(int days) {
        this.usedDays = Math.max(0, this.usedDays - days);
    }

    // Getters and Setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getLeaveTypeCode() { return leaveTypeCode; }
    public void setLeaveTypeCode(String leaveTypeCode) { this.leaveTypeCode = leaveTypeCode; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public int getUsedDays() { return usedDays; }
    public void setUsedDays(int usedDays) { this.usedDays = usedDays; }
}

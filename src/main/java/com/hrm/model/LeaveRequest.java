package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Leave Request model
 */
public class LeaveRequest {
    public enum Status {
        PENDING("Cho duyet"),
        APPROVED("Da duyet"),
        REJECTED("Tu choi"),
        CANCELLED("Da huy");

        private final String displayName;
        Status(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private int id;
    private int employeeId;
    private String employeeName;
    private String leaveTypeCode;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;
    private String reason;
    private Status status;
    private Integer approverId;
    private String approverName;
    private String approverNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LeaveRequest() {
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getLeaveTypeCode() { return leaveTypeCode; }
    public void setLeaveTypeCode(String leaveTypeCode) { this.leaveTypeCode = leaveTypeCode; }

    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getApproverId() { return approverId; }
    public void setApproverId(Integer approverId) { this.approverId = approverId; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public String getApproverNote() { return approverNote; }
    public void setApproverNote(String approverNote) { this.approverNote = approverNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

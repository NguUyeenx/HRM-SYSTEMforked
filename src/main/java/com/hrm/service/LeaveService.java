package com.hrm.service;

import com.hrm.model.*;
import com.hrm.repo.LeaveRepository;
import com.hrm.util.SessionContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Leave Management Service
 */
public class LeaveService {
    private static LeaveService instance;
    private final LeaveRepository repository;

    private LeaveService() {
        this.repository = LeaveRepository.getInstance();
    }

    public static synchronized LeaveService getInstance() {
        if (instance == null) {
            instance = new LeaveService();
        }
        return instance;
    }

    /**
     * Calculate business days between two dates (excluding weekends)
     */
    public int calculateBusinessDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return 0;
        }

        int businessDays = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                businessDays++;
            }
            date = date.plusDays(1);
        }
        return businessDays;
    }

    /**
     * Create a new leave request
     */
    public ServiceResult<LeaveRequest> createRequest(int employeeId, String employeeName,
                                                     String leaveTypeCode, LocalDate startDate,
                                                     LocalDate endDate, String reason) {
        // Validation
        if (startDate == null || endDate == null) {
            return ServiceResult.error("Vui long chon ngay bat dau va ket thuc");
        }

        if (startDate.isBefore(LocalDate.now())) {
            return ServiceResult.error("Ngay bat dau phai tu hom nay tro di");
        }

        if (endDate.isBefore(startDate)) {
            return ServiceResult.error("Ngay ket thuc phai sau ngay bat dau");
        }

        LeaveType leaveType = repository.getLeaveType(leaveTypeCode);
        if (leaveType == null) {
            return ServiceResult.error("Loai phep khong hop le");
        }

        int totalDays = calculateBusinessDays(startDate, endDate);
        if (totalDays == 0) {
            return ServiceResult.error("Khong co ngay lam viec trong khoang thoi gian nay");
        }

        // Check balance for annual leave
        if ("AL".equals(leaveTypeCode)) {
            LeaveBalance balance = repository.getBalance(employeeId, leaveTypeCode, LocalDate.now().getYear());
            if (balance == null || balance.getRemainingDays() < totalDays) {
                int remaining = balance != null ? balance.getRemainingDays() : 0;
                return ServiceResult.error("So ngay phep con lai khong du. Con lai: " + remaining + " ngay");
            }
        }

        // Check for overlapping approved requests
        List<LeaveRequest> overlapping = repository.findOverlapping(employeeId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            return ServiceResult.error("Da co don nghi phep duoc duyet trong khoang thoi gian nay");
        }

        // Create request
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(employeeId);
        request.setEmployeeName(employeeName);
        request.setLeaveTypeCode(leaveTypeCode);
        request.setLeaveTypeName(leaveType.getName());
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setTotalDays(totalDays);
        request.setReason(reason);
        request.setStatus(LeaveRequest.Status.PENDING);

        repository.save(request);
        return ServiceResult.success(request, "Tao don nghi phep thanh cong");
    }

    /**
     * Approve or reject a leave request
     */
    public ServiceResult<LeaveRequest> processRequest(int requestId, boolean approve,
                                                       int approverId, String approverName, String note) {
        LeaveRequest request = repository.findById(requestId);
        if (request == null) {
            return ServiceResult.error("Khong tim thay don nghi phep");
        }

        if (request.getStatus() != LeaveRequest.Status.PENDING) {
            return ServiceResult.error("Don nghi phep da duoc xu ly truoc do");
        }

        request.setApproverId(approverId);
        request.setApproverName(approverName);
        request.setApproverNote(note);

        if (approve) {
            // Deduct balance for annual leave
            if ("AL".equals(request.getLeaveTypeCode())) {
                LeaveBalance balance = repository.getBalance(request.getEmployeeId(),
                        request.getLeaveTypeCode(), LocalDate.now().getYear());
                if (balance != null) {
                    if (balance.getRemainingDays() < request.getTotalDays()) {
                        return ServiceResult.error("Nhan vien khong du so ngay phep");
                    }
                    balance.deductDays(request.getTotalDays());
                }
            }
            request.setStatus(LeaveRequest.Status.APPROVED);
            return ServiceResult.success(request, "Da duyet don nghi phep");
        } else {
            request.setStatus(LeaveRequest.Status.REJECTED);
            return ServiceResult.success(request, "Da tu choi don nghi phep");
        }
    }

    /**
     * Cancel a leave request
     */
    public ServiceResult<LeaveRequest> cancelRequest(int requestId, int userId) {
        LeaveRequest request = repository.findById(requestId);
        if (request == null) {
            return ServiceResult.error("Khong tim thay don nghi phep");
        }

        if (request.getEmployeeId() != userId) {
            return ServiceResult.error("Ban khong co quyen huy don nay");
        }

        if (request.getStatus() == LeaveRequest.Status.APPROVED) {
            // Restore balance
            if ("AL".equals(request.getLeaveTypeCode())) {
                LeaveBalance balance = repository.getBalance(request.getEmployeeId(),
                        request.getLeaveTypeCode(), LocalDate.now().getYear());
                if (balance != null) {
                    balance.restoreDays(request.getTotalDays());
                }
            }
        }

        request.setStatus(LeaveRequest.Status.CANCELLED);
        return ServiceResult.success(request, "Da huy don nghi phep");
    }

    // Query methods
    public List<LeaveRequest> getMyRequests(int employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getPendingRequests() {
        return repository.findPendingRequests();
    }

    public List<LeaveRequest> getAllRequests() {
        return repository.findAll();
    }

    public List<LeaveType> getAllLeaveTypes() {
        return repository.getAllLeaveTypes();
    }

    public List<LeaveBalance> getBalances(int employeeId) {
        return repository.getBalances(employeeId, LocalDate.now().getYear());
    }

    public LeaveRequest getRequest(int id) {
        return repository.findById(id);
    }

    /**
     * Generic service result wrapper
     */
    public static class ServiceResult<T> {
        private boolean success;
        private String message;
        private T data;

        private ServiceResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> ServiceResult<T> success(T data, String message) {
            return new ServiceResult<>(true, message, data);
        }

        public static <T> ServiceResult<T> error(String message) {
            return new ServiceResult<>(false, message, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
}

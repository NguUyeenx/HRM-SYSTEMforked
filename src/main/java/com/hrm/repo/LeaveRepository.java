package com.hrm.repo;

import com.hrm.model.LeaveBalance;
import com.hrm.model.LeaveRequest;
import com.hrm.model.LeaveType;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for Leave Management
 */
public class LeaveRepository {
    private static LeaveRepository instance;

    private Map<String, LeaveType> leaveTypes;
    private Map<Integer, LeaveRequest> leaveRequests;
    private List<LeaveBalance> leaveBalances;
    private int nextRequestId = 1;

    private LeaveRepository() {
        leaveTypes = new LinkedHashMap<>();
        leaveRequests = new LinkedHashMap<>();
        leaveBalances = new ArrayList<>();
        initializeMockData();
    }

    public static synchronized LeaveRepository getInstance() {
        if (instance == null) {
            instance = new LeaveRepository();
        }
        return instance;
    }

    private void initializeMockData() {
        // Leave Types
        leaveTypes.put("AL", new LeaveType("AL", "Phep nam", 12, true));
        leaveTypes.put("SL", new LeaveType("SL", "Nghi om", 30, true));
        leaveTypes.put("UL", new LeaveType("UL", "Nghi khong luong", 0, false));

        // Initialize balances for mock employees (IDs 1-4)
        int currentYear = LocalDate.now().getYear();
        for (int empId = 1; empId <= 4; empId++) {
            leaveBalances.add(new LeaveBalance(empId, "AL", currentYear, 12));
            leaveBalances.add(new LeaveBalance(empId, "SL", currentYear, 30));
        }

        // Sample leave requests
        LeaveRequest req1 = new LeaveRequest();
        req1.setId(nextRequestId++);
        req1.setEmployeeId(4);
        req1.setEmployeeName("Nguyen Van An");
        req1.setLeaveTypeCode("AL");
        req1.setLeaveTypeName("Phep nam");
        req1.setStartDate(LocalDate.now().plusDays(5));
        req1.setEndDate(LocalDate.now().plusDays(7));
        req1.setTotalDays(3);
        req1.setReason("Du lich gia dinh");
        req1.setStatus(LeaveRequest.Status.PENDING);
        leaveRequests.put(req1.getId(), req1);
    }

    // Leave Types
    public List<LeaveType> getAllLeaveTypes() {
        return new ArrayList<>(leaveTypes.values());
    }

    public LeaveType getLeaveType(String code) {
        return leaveTypes.get(code);
    }

    // Leave Requests
    public LeaveRequest save(LeaveRequest request) {
        if (request.getId() == 0) {
            request.setId(nextRequestId++);
        }
        leaveRequests.put(request.getId(), request);
        return request;
    }

    public LeaveRequest findById(int id) {
        return leaveRequests.get(id);
    }

    public List<LeaveRequest> findByEmployeeId(int employeeId) {
        return leaveRequests.values().stream()
                .filter(r -> r.getEmployeeId() == employeeId)
                .sorted(Comparator.comparing(LeaveRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<LeaveRequest> findByStatus(LeaveRequest.Status status) {
        return leaveRequests.values().stream()
                .filter(r -> r.getStatus() == status)
                .sorted(Comparator.comparing(LeaveRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<LeaveRequest> findPendingRequests() {
        return findByStatus(LeaveRequest.Status.PENDING);
    }

    public List<LeaveRequest> findAll() {
        return leaveRequests.values().stream()
                .sorted(Comparator.comparing(LeaveRequest::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<LeaveRequest> findOverlapping(int employeeId, LocalDate start, LocalDate end) {
        return leaveRequests.values().stream()
                .filter(r -> r.getEmployeeId() == employeeId)
                .filter(r -> r.getStatus() == LeaveRequest.Status.APPROVED)
                .filter(r -> !r.getEndDate().isBefore(start) && !r.getStartDate().isAfter(end))
                .collect(Collectors.toList());
    }

    // Leave Balances
    public LeaveBalance getBalance(int employeeId, String leaveTypeCode, int year) {
        return leaveBalances.stream()
                .filter(b -> b.getEmployeeId() == employeeId
                        && b.getLeaveTypeCode().equals(leaveTypeCode)
                        && b.getYear() == year)
                .findFirst()
                .orElse(null);
    }

    public List<LeaveBalance> getBalances(int employeeId, int year) {
        return leaveBalances.stream()
                .filter(b -> b.getEmployeeId() == employeeId && b.getYear() == year)
                .collect(Collectors.toList());
    }

    public void updateBalance(LeaveBalance balance) {
        // Balance is already in the list, just updated by reference
    }

    public void addBalance(LeaveBalance balance) {
        leaveBalances.add(balance);
    }
}

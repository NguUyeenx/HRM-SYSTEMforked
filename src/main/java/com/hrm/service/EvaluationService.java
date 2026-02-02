package com.hrm.service;

import com.hrm.model.*;
import com.hrm.repo.EvaluationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance Evaluation Service
 */
public class EvaluationService {
    private static EvaluationService instance;
    private final EvaluationRepository repository;

    private EvaluationService() {
        this.repository = EvaluationRepository.getInstance();
    }

    public static synchronized EvaluationService getInstance() {
        if (instance == null) {
            instance = new EvaluationService();
        }
        return instance;
    }

    // Cycle Management
    public List<EvalCycle> getAllCycles() {
        return repository.getAllCycles();
    }

    public List<EvalCycle> getOpenCycles() {
        return repository.getOpenCycles();
    }

    public EvalCycle getCycle(int id) {
        return repository.getCycle(id);
    }

    public ServiceResult<EvalCycle> openCycle(int cycleId) {
        EvalCycle cycle = repository.getCycle(cycleId);
        if (cycle == null) {
            return ServiceResult.error("Khong tim thay ky danh gia");
        }

        if (cycle.getStatus() == EvalCycle.Status.OPEN) {
            return ServiceResult.error("Ky danh gia da dang mo");
        }

        if (cycle.getStatus() == EvalCycle.Status.CLOSED) {
            return ServiceResult.error("Ky danh gia da dong, khong the mo lai");
        }

        // Validate criteria
        int totalWeight = repository.getTotalWeight();
        if (totalWeight != 100) {
            return ServiceResult.error("Tong trong so tieu chi phai bang 100% (hien tai: " + totalWeight + "%)");
        }

        cycle.setStatus(EvalCycle.Status.OPEN);
        return ServiceResult.success(cycle, "Da mo ky danh gia");
    }

    public ServiceResult<EvalCycle> closeCycle(int cycleId) {
        EvalCycle cycle = repository.getCycle(cycleId);
        if (cycle == null) {
            return ServiceResult.error("Khong tim thay ky danh gia");
        }

        if (cycle.getStatus() != EvalCycle.Status.OPEN) {
            return ServiceResult.error("Chi co the dong ky danh gia dang mo");
        }

        cycle.setStatus(EvalCycle.Status.CLOSED);
        return ServiceResult.success(cycle, "Da dong ky danh gia");
    }

    // Criteria Management
    public List<EvalCriteria> getAllCriteria() {
        return repository.getAllCriteria();
    }

    public ServiceResult<EvalCriteria> saveCriteria(String name, String description, int weight) {
        if (name == null || name.trim().isEmpty()) {
            return ServiceResult.error("Ten tieu chi khong duoc de trong");
        }

        if (weight <= 0 || weight > 100) {
            return ServiceResult.error("Trong so phai tu 1 den 100");
        }

        EvalCriteria criteria = new EvalCriteria();
        criteria.setName(name.trim());
        criteria.setDescription(description);
        criteria.setWeight(weight);

        repository.saveCriteria(criteria);
        return ServiceResult.success(criteria, "Da luu tieu chi");
    }

    public ServiceResult<EvalCriteria> updateCriteria(int id, String name, String description, int weight) {
        EvalCriteria criteria = repository.getCriteria(id);
        if (criteria == null) {
            return ServiceResult.error("Khong tim thay tieu chi");
        }

        if (name == null || name.trim().isEmpty()) {
            return ServiceResult.error("Ten tieu chi khong duoc de trong");
        }

        if (weight <= 0 || weight > 100) {
            return ServiceResult.error("Trong so phai tu 1 den 100");
        }

        criteria.setName(name.trim());
        criteria.setDescription(description);
        criteria.setWeight(weight);

        return ServiceResult.success(criteria, "Da cap nhat tieu chi");
    }

    public ServiceResult<Void> deleteCriteria(int id) {
        EvalCriteria criteria = repository.getCriteria(id);
        if (criteria == null) {
            return ServiceResult.error("Khong tim thay tieu chi");
        }

        repository.deleteCriteria(id);
        return ServiceResult.success(null, "Da xoa tieu chi");
    }

    public int getTotalWeight() {
        return repository.getTotalWeight();
    }

    // Evaluation Submission
    public ServiceResult<EvalSubmission> submitEvaluation(int cycleId, int employeeId, String employeeName,
                                                          int evaluatorId, String evaluatorName,
                                                          List<EvalScore> scores, String generalComment) {
        EvalCycle cycle = repository.getCycle(cycleId);
        if (cycle == null) {
            return ServiceResult.error("Khong tim thay ky danh gia");
        }

        if (!cycle.isOpen()) {
            return ServiceResult.error("Ky danh gia chua mo hoac da dong");
        }

        // Check if already submitted
        EvalSubmission existing = repository.findSubmission(cycleId, employeeId);
        if (existing != null) {
            return ServiceResult.error("Nhan vien nay da duoc danh gia trong ky nay");
        }

        // Validate scores
        if (scores == null || scores.isEmpty()) {
            return ServiceResult.error("Vui long nhap diem cho cac tieu chi");
        }

        for (EvalScore score : scores) {
            if (score.getScore() < 1 || score.getScore() > 10) {
                return ServiceResult.error("Diem phai tu 1 den 10");
            }
        }

        // Create submission
        EvalSubmission submission = new EvalSubmission();
        submission.setCycleId(cycleId);
        submission.setCycleName(cycle.getName());
        submission.setEmployeeId(employeeId);
        submission.setEmployeeName(employeeName);
        submission.setEvaluatorId(evaluatorId);
        submission.setEvaluatorName(evaluatorName);
        submission.setScores(new ArrayList<>(scores));
        submission.setGeneralComment(generalComment);
        submission.calculateOverall();

        repository.saveSubmission(submission);
        return ServiceResult.success(submission, "Da luu danh gia. Diem tong: " +
                String.format("%.2f", submission.getOverallScore()) + " - " +
                submission.getRating().getDisplayName());
    }

    // Query methods
    public List<EvalSubmission> getSubmissionsByCycle(int cycleId) {
        return repository.getSubmissionsByCycle(cycleId);
    }

    public List<EvalSubmission> getSubmissionsByEmployee(int employeeId) {
        return repository.getSubmissionsByEmployee(employeeId);
    }

    public EvalSubmission getSubmission(int cycleId, int employeeId) {
        return repository.findSubmission(cycleId, employeeId);
    }

    public List<EvalSubmission> getAllSubmissions() {
        return repository.getAllSubmissions();
    }

    /**
     * Calculate overall score from weighted scores
     */
    public double calculateOverallScore(List<EvalScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (EvalScore score : scores) {
            total += score.getWeightedScore();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Get rating from score
     */
    public EvalSubmission.Rating getRatingFromScore(double score) {
        return EvalSubmission.Rating.fromScore(score);
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

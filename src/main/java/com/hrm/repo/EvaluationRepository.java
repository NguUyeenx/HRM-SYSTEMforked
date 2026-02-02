package com.hrm.repo;

import com.hrm.model.EvalCriteria;
import com.hrm.model.EvalCycle;
import com.hrm.model.EvalSubmission;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for Performance Evaluation
 */
public class EvaluationRepository {
    private static EvaluationRepository instance;

    private Map<Integer, EvalCycle> cycles;
    private Map<Integer, EvalCriteria> criteria;
    private Map<Integer, EvalSubmission> submissions;
    private int nextCycleId = 1;
    private int nextCriteriaId = 1;
    private int nextSubmissionId = 1;

    private EvaluationRepository() {
        cycles = new LinkedHashMap<>();
        criteria = new LinkedHashMap<>();
        submissions = new LinkedHashMap<>();
        initializeMockData();
    }

    public static synchronized EvaluationRepository getInstance() {
        if (instance == null) {
            instance = new EvaluationRepository();
        }
        return instance;
    }

    private void initializeMockData() {
        int currentYear = LocalDate.now().getYear();

        // Create evaluation cycles
        EvalCycle q1 = new EvalCycle(nextCycleId++, "Danh gia Q1/" + currentYear,
                currentYear, 1,
                LocalDate.of(currentYear, 1, 1),
                LocalDate.of(currentYear, 3, 31),
                LocalDate.of(currentYear, 4, 15));
        q1.setStatus(EvalCycle.Status.OPEN);
        cycles.put(q1.getId(), q1);

        EvalCycle q2 = new EvalCycle(nextCycleId++, "Danh gia Q2/" + currentYear,
                currentYear, 2,
                LocalDate.of(currentYear, 4, 1),
                LocalDate.of(currentYear, 6, 30),
                LocalDate.of(currentYear, 7, 15));
        q2.setStatus(EvalCycle.Status.DRAFT);
        cycles.put(q2.getId(), q2);

        // Create default criteria
        criteria.put(nextCriteriaId, new EvalCriteria(nextCriteriaId++,
                "Chat luong cong viec", "Muc do hoan thanh va chat luong dau ra", 40));
        criteria.put(nextCriteriaId, new EvalCriteria(nextCriteriaId++,
                "Nang suat", "Kha nang hoan thanh cong viec dung han", 30));
        criteria.put(nextCriteriaId, new EvalCriteria(nextCriteriaId++,
                "Thai do lam viec", "Tinh than trach nhiem, hop tac, chu dong", 30));
    }

    // Cycles
    public List<EvalCycle> getAllCycles() {
        return new ArrayList<>(cycles.values());
    }

    public EvalCycle getCycle(int id) {
        return cycles.get(id);
    }

    public EvalCycle saveCycle(EvalCycle cycle) {
        if (cycle.getId() == 0) {
            cycle.setId(nextCycleId++);
        }
        cycles.put(cycle.getId(), cycle);
        return cycle;
    }

    public List<EvalCycle> getOpenCycles() {
        return cycles.values().stream()
                .filter(EvalCycle::isOpen)
                .collect(Collectors.toList());
    }

    // Criteria
    public List<EvalCriteria> getAllCriteria() {
        return criteria.values().stream()
                .filter(EvalCriteria::isActive)
                .collect(Collectors.toList());
    }

    public EvalCriteria getCriteria(int id) {
        return criteria.get(id);
    }

    public EvalCriteria saveCriteria(EvalCriteria c) {
        if (c.getId() == 0) {
            c.setId(nextCriteriaId++);
        }
        criteria.put(c.getId(), c);
        return c;
    }

    public void deleteCriteria(int id) {
        EvalCriteria c = criteria.get(id);
        if (c != null) {
            c.setActive(false);
        }
    }

    public int getTotalWeight() {
        return criteria.values().stream()
                .filter(EvalCriteria::isActive)
                .mapToInt(EvalCriteria::getWeight)
                .sum();
    }

    // Submissions
    public EvalSubmission saveSubmission(EvalSubmission submission) {
        if (submission.getId() == 0) {
            submission.setId(nextSubmissionId++);
        }
        submissions.put(submission.getId(), submission);
        return submission;
    }

    public EvalSubmission getSubmission(int id) {
        return submissions.get(id);
    }

    public List<EvalSubmission> getSubmissionsByCycle(int cycleId) {
        return submissions.values().stream()
                .filter(s -> s.getCycleId() == cycleId)
                .collect(Collectors.toList());
    }

    public List<EvalSubmission> getSubmissionsByEmployee(int employeeId) {
        return submissions.values().stream()
                .filter(s -> s.getEmployeeId() == employeeId)
                .sorted(Comparator.comparing(EvalSubmission::getSubmittedAt).reversed())
                .collect(Collectors.toList());
    }

    public EvalSubmission findSubmission(int cycleId, int employeeId) {
        return submissions.values().stream()
                .filter(s -> s.getCycleId() == cycleId && s.getEmployeeId() == employeeId)
                .findFirst()
                .orElse(null);
    }

    public List<EvalSubmission> getAllSubmissions() {
        return new ArrayList<>(submissions.values());
    }
}

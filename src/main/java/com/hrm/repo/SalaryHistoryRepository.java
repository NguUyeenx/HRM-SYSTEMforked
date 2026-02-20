package com.hrm.repo;

import com.hrm.model.SalaryHistory;
import java.util.ArrayList;
import java.util.List;

public class SalaryHistoryRepository {

    private List<SalaryHistory> historyList = new ArrayList<>();
    private int currentId = 1;

    public SalaryHistoryRepository() {
        // Dữ liệu mẫu
        historyList.add(new SalaryHistory(currentId++, "CV001", 3.2, 3.5, 4500000, 5000000, "10/02/2024", "Admin"));
        historyList.add(new SalaryHistory(currentId++, "CV002", 2.5, 2.8, 2500000, 3000000, "05/01/2024", "Admin"));
        historyList.add(new SalaryHistory(currentId++, "CV003", 2.0, 2.2, 1200000, 1500000, "20/11/2023", "Admin"));
    }

    public List<SalaryHistory> findAll() {
        return historyList;
    }

    public List<SalaryHistory> findByMaChucVu(String maChucVu) {
        List<SalaryHistory> result = new ArrayList<>();
        for (SalaryHistory h : historyList) {
            if (h.getMaChucVu().equals(maChucVu)) {
                result.add(h);
            }
        }
        return result;
    }

    public void save(SalaryHistory history) {
        historyList.add(history);
        currentId++;
    }

    public int generateId() {
        return currentId++;
    }
}
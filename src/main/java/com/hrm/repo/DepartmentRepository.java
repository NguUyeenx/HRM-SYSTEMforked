package com.hrm.repo;

import com.hrm.model.Department;
import java.util.ArrayList;
import java.util.List;

public class DepartmentRepository {

    private List<Department> departments = new ArrayList<>();

    public DepartmentRepository() {
        // Dữ liệu mẫu
        departments.add(new Department("PB001", "Tong cong ty", null, "hoat dong"));
        departments.add(new Department("PB002", "Phong Ky thuat", "PB001", "hoat dong"));
        departments.add(new Department("PB003", "Phong Nhan su", "PB001", "hoat dong"));
        departments.add(new Department("PB004", "Bo phan Frontend", "PB002", "hoat dong"));
        departments.add(new Department("PB005", "Bo phan Backend", "PB002", "hoat dong"));
    }

    public List<Department> findAll() {
        return departments;
    }

    public Department findById(String maPhongBan) {
        for (Department d : departments) {
            if (d.getMaPhongBan().equals(maPhongBan)) {
                return d;
            }
        }
        return null;
    }

    public boolean existsById(String maPhongBan) {
        return findById(maPhongBan) != null;
    }

    public List<Department> findChildren(String maPhongBan) {
        List<Department> children = new ArrayList<>();
        for (Department d : departments) {
            if (maPhongBan.equals(d.getPhongBanCha())) {
                children.add(d);
            }
        }
        return children;
    }

    public void save(Department department) {
        departments.add(department);
    }

    public void update(Department updated) {
        for (int i = 0; i < departments.size(); i++) {
            if (departments.get(i).getMaPhongBan().equals(updated.getMaPhongBan())) {
                departments.set(i, updated);
                return;
            }
        }
    }
}
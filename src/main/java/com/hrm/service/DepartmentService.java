package com.hrm.service;

import com.hrm.model.Department;
import com.hrm.repo.DepartmentRepository;
import java.util.ArrayList;
import java.util.List;

public class DepartmentService {

    private DepartmentRepository repository = new DepartmentRepository();

    public List<Department> getAllDepartments() {
        return repository.findAll();
    }

    public List<Department> getActiveDepartments() {
        List<Department> result = new ArrayList<>();
        for (Department d : repository.findAll()) {
            if ("hoat dong".equals(d.getTrangThai())) {
                result.add(d);
            }
        }
        return result;
    }

    public Department getById(String maPhongBan) {
        return repository.findById(maPhongBan);
    }

    public void addDepartment(String maPhongBan, String tenPhongBan, String phongBanCha) {
        if (maPhongBan == null || maPhongBan.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma phong ban khong duoc de trong.");
        }

        if (tenPhongBan == null || tenPhongBan.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten phong ban khong duoc de trong.");
        }

        if (repository.existsById(maPhongBan.trim())) {
            throw new IllegalArgumentException("Ma phong ban '" + maPhongBan + "' da ton tai trong he thong.");
        }

        if (phongBanCha != null) {
            Department cha = repository.findById(phongBanCha);
            if (cha == null) {
                throw new IllegalArgumentException("Phong ban cha khong ton tai.");
            }
            if (!"hoat dong".equals(cha.getTrangThai())) {
                throw new IllegalArgumentException("Phong ban cha '" + cha.getTenPhongBan() + "' da ngung hoat dong.");
            }
        }

        Department dept = new Department(maPhongBan.trim(), tenPhongBan.trim(), phongBanCha, "hoat dong");
        repository.save(dept);
    }

    public void updateDepartment(String maPhongBan, String tenMoi, String phongBanChaMoi) {
        Department dept = repository.findById(maPhongBan);
        if (dept == null) {
            throw new IllegalArgumentException("Khong tim thay phong ban.");
        }

        if (tenMoi == null || tenMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten phong ban khong duoc de trong.");
        }

        if (phongBanChaMoi != null) {
            Department cha = repository.findById(phongBanChaMoi);
            if (cha == null) {
                throw new IllegalArgumentException("Phong ban cha khong ton tai.");
            }
            if (!"hoat dong".equals(cha.getTrangThai())) {
                throw new IllegalArgumentException("Phong ban cha '" + cha.getTenPhongBan() + "' da ngung hoat dong.");
            }

            if (isDescendant(maPhongBan, phongBanChaMoi)) {
                throw new IllegalArgumentException(
                        "Khong the chon phong ban con/chau lam phong ban cha. Se tao vong lap trong cay to chuc.");
            }
        }

        dept.setTenPhongBan(tenMoi.trim());
        dept.setPhongBanCha(phongBanChaMoi);
        repository.update(dept);
    }

    public void deactivateDepartment(String maPhongBan) {
        Department dept = repository.findById(maPhongBan);
        if (dept == null) {
            throw new IllegalArgumentException("Khong tim thay phong ban.");
        }

        List<Department> danhSachCon = repository.findChildren(maPhongBan);
        for (Department con : danhSachCon) {
            if ("hoat dong".equals(con.getTrangThai())) {
                throw new IllegalArgumentException("Khong the ngung hoat dong. Phong ban '" + con.getTenPhongBan()
                        + "' van dang hoat dong. Vui long ngung cac phong ban con truoc.");
            }
        }

        dept.setTrangThai("ngung hoat dong");
        repository.update(dept);
    }

    private boolean isDescendant(String maCha, String maCon) {
        if (maCon == null) {
            return false;
        }
        if (maCon.equals(maCha)) {
            return true;
        }

        Department con = repository.findById(maCon);
        if (con == null) {
            return false;
        }

        return isDescendant(maCha, con.getPhongBanCha());
    }
}
package com.hrm.service;

import com.hrm.model.Position;
import com.hrm.model.SalaryHistory;
import com.hrm.repo.PositionRepository;
import com.hrm.repo.SalaryHistoryRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PositionService {

    private PositionRepository positionRepo = new PositionRepository();
    private SalaryHistoryRepository historyRepo = new SalaryHistoryRepository();

    public List<Position> getAllPositions() {
        return positionRepo.findAll();
    }

    public List<Position> getActivePositions() {
        List<Position> result = new ArrayList<>();
        for (Position p : positionRepo.findAll()) {
            if ("hoat dong".equals(p.getTrangThai())) {
                result.add(p);
            }
        }
        return result;
    }

    public Position getById(String maChucVu) {
        return positionRepo.findById(maChucVu);
    }

    public List<SalaryHistory> getHistoryByMaChucVu(String maChucVu) {
        return historyRepo.findByMaChucVu(maChucVu);
    }

    public void addPosition(String maChucVu, String tenChucVu, int capBac, double heSoLuong, double phuCapChucVu,
            String moTa) {
        if (maChucVu == null || maChucVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma chuc vu khong duoc de trong.");
        }

        if (tenChucVu == null || tenChucVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten chuc vu khong duoc de trong.");
        }

        if (positionRepo.existsById(maChucVu.trim())) {
            throw new IllegalArgumentException("Ma chuc vu '" + maChucVu + "' da ton tai.");
        }

        if (heSoLuong <= 0) {
            throw new IllegalArgumentException("He so luong phai lon hon 0.");
        }

        if (phuCapChucVu < 0) {
            throw new IllegalArgumentException("Phu cap khong duoc am.");
        }

        Position pos = new Position(maChucVu.trim(), tenChucVu.trim(), capBac, heSoLuong, phuCapChucVu, moTa,
                "hoat dong");
        positionRepo.save(pos);
    }

    public void updatePosition(String maChucVu, String tenMoi, int capBacMoi, double heSoMoi, double phuCapMoi,
            String moTaMoi) {
        Position pos = positionRepo.findById(maChucVu);
        if (pos == null) {
            throw new IllegalArgumentException("Khong tim thay chuc vu.");
        }

        if (tenMoi == null || tenMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten chuc vu khong duoc de trong.");
        }

        if (heSoMoi <= 0) {
            throw new IllegalArgumentException("He so luong phai lon hon 0.");
        }

        if (phuCapMoi < 0) {
            throw new IllegalArgumentException("Phu cap khong duoc am.");
        }

        boolean heSoThayDoi = pos.getHeSoLuong() != heSoMoi;
        boolean phuCapThayDoi = pos.getPhuCapChucVu() != phuCapMoi;

        if (heSoThayDoi || phuCapThayDoi) {
            String ngayHom = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            SalaryHistory history = new SalaryHistory(historyRepo.generateId(), maChucVu, pos.getHeSoLuong(), heSoMoi,
                    pos.getPhuCapChucVu(), phuCapMoi, ngayHom, "Admin");
            historyRepo.save(history);
        }

        pos.setTenChucVu(tenMoi.trim());
        pos.setCapBac(capBacMoi);
        pos.setHeSoLuong(heSoMoi);
        pos.setPhuCapChucVu(phuCapMoi);
        pos.setMoTa(moTaMoi);
        positionRepo.update(pos);
    }

    public void deactivatePosition(String maChucVu) {
        Position pos = positionRepo.findById(maChucVu);
        if (pos == null) {
            throw new IllegalArgumentException("Khong tim thay chuc vu.");
        }

        pos.setTrangThai("ngung");
        positionRepo.update(pos);
    }
}
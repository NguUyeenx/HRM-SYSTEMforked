package com.hrm.service;

import com.hrm.model.*;
import com.hrm.repo.AttendanceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceService {

    private static AttendanceService instance;
    private final AttendanceRepository repository;
    private static final int SO_NGAY_CONG_CHUAN = 22;

    private AttendanceService() { this.repository = AttendanceRepository.getInstance(); }

    public static synchronized AttendanceService getInstance() {
        if (instance == null) instance = new AttendanceService();
        return instance;
    }

    // ── CA LÀM ──
    public List<CaLam> getDanhSachCaLam() { return repository.findCaLamHoatDong(); }
    public List<CaLam> getTatCaCaLam() { return repository.findAllCaLam(); }

    public ServiceResult<CaLam> themCaLam(String ma, String ten, String gioBD, String gioKT, double sg, boolean ot) {
        if (ma == null || ma.trim().isEmpty()) return ServiceResult.error("Ma ca khong duoc de trong.");
        if (ten == null || ten.trim().isEmpty()) return ServiceResult.error("Ten ca khong duoc de trong.");
        if (repository.findCaLamByMa(ma.trim()) != null) return ServiceResult.error("Ma ca da ton tai.");
        try {
            CaLam ca = new CaLam(ma.trim(), ten.trim(),
                java.time.LocalTime.parse(gioBD.trim()), java.time.LocalTime.parse(gioKT.trim()));
            ca.setSoGioChuan(sg); ca.setChoPhepLamThem(ot); repository.saveCaLam(ca);
            return ServiceResult.success(ca, "Da them ca lam '" + ten + "'.");
        } catch (Exception e) { return ServiceResult.error("Dinh dang gio khong hop le (HH:mm)."); }
    }

    public ServiceResult<CaLam> suaCaLam(String ma, String ten, String gioBD, String gioKT, double sg, boolean ot) {
        CaLam ca = repository.findCaLamByMa(ma);
        if (ca == null) return ServiceResult.error("Khong tim thay ca lam.");
        try {
            ca.setTenCaLam(ten.trim()); ca.setGioBatDau(java.time.LocalTime.parse(gioBD.trim()));
            ca.setGioKetThuc(java.time.LocalTime.parse(gioKT.trim()));
            ca.setSoGioChuan(sg); ca.setChoPhepLamThem(ot); repository.saveCaLam(ca);
            return ServiceResult.success(ca, "Da cap nhat ca lam.");
        } catch (Exception e) { return ServiceResult.error("Dinh dang gio khong hop le."); }
    }

    public ServiceResult<Void> xoaCaLam(String ma) {
        CaLam ca = repository.findCaLamByMa(ma);
        if (ca == null) return ServiceResult.error("Khong tim thay ca lam.");
        repository.deleteCaLam(ma);
        return ServiceResult.success(null, "Da ngung ca lam '" + ca.getTenCaLam() + "'.");
    }

    // ── CHECK-IN / CHECK-OUT ──
    public ServiceResult<ChamCong> checkIn(int maNV, String maCaLam) {
        if (repository.findByMaNVAndNgay(maNV, LocalDate.now()) != null)
            return ServiceResult.error("Da check-in hom nay.");
        CaLam caLam = repository.findCaLamByMa(maCaLam);
        if (caLam == null) return ServiceResult.error("Ca lam khong hop le.");
        ChamCong cc = new ChamCong(maNV, LocalDate.now(), maCaLam);
        cc.setTenCaLam(caLam.getTenCaLam()); cc.setGioVao(LocalDateTime.now());
        cc.setPhuongThucChamCong(ChamCong.PhuongThuc.THU_CONG);
        cc.setTrangThai(LocalDateTime.now().toLocalTime().isAfter(caLam.getGioBatDau().plusMinutes(15))
            ? ChamCong.TrangThai.DI_MUON : ChamCong.TrangThai.DUNG_GIO);
        repository.saveChamCong(cc);
        return ServiceResult.success(cc, "Check-in thanh cong.");
    }

    public ServiceResult<ChamCong> checkOut(int maNV) {
        ChamCong cc = repository.findByMaNVAndNgay(maNV, LocalDate.now());
        if (cc == null) return ServiceResult.error("Chua check-in.");
        if (cc.daCheckOut()) return ServiceResult.error("Da check-out.");
        cc.setGioRa(LocalDateTime.now()); cc.setSoGioLam(cc.tinhSoGioLam());
        CaLam ca = repository.findCaLamByMa(cc.getMaCaLam());
        if (ca != null && cc.getSoGioLam() > ca.getSoGioChuan())
            cc.setGioLamThem(cc.getSoGioLam() - ca.getSoGioChuan());
        repository.saveChamCong(cc);
        return ServiceResult.success(cc, "Check-out thanh cong.");
    }

    // ── TRUY VẤN ──
    public ChamCong getChamCongHomNay(int maNV) { return repository.findByMaNVAndNgay(maNV, LocalDate.now()); }
    public List<ChamCong> getLichSuChamCong(int maNV, LocalDate tu, LocalDate den) {
        return repository.findByMaNVAndNgayBetween(maNV, tu, den);
    }
    public List<ChamCong> getChamCongTheoThang(int thang, int nam) { return repository.findByThangNam(thang, nam); }

    // ── ĐƠN OT ──
    public ServiceResult<DangKyLamThem> taoDonLamThem(int maNV, LocalDate ngay, double soGio, String lyDo) {
        if (soGio <= 0 || soGio > 8) return ServiceResult.error("So gio phai tu 0.5 den 8.");
        if (lyDo == null || lyDo.trim().isEmpty()) return ServiceResult.error("Nhap ly do.");
        DangKyLamThem don = new DangKyLamThem(maNV, ngay, soGio, lyDo.trim());
        repository.saveDonOT(don);
        return ServiceResult.success(don, "Da tao don OT.");
    }

    public ServiceResult<DangKyLamThem> duyetDonLamThem(int maDK, int nguoiDuyetId, double heSoOT) {
        DangKyLamThem don = repository.findDonOTById(maDK);
        if (don == null) return ServiceResult.error("Khong tim thay don.");
        if (!don.dangChoDuyet()) return ServiceResult.error("Don da xu ly.");
        don.setHeSoOT(heSoOT); don.duyet(nguoiDuyetId);
        User approver = MockDataService.getInstance().getUserById(nguoiDuyetId);
        if (approver != null) don.setApproverName(approver.getFullName());
        repository.saveDonOT(don);
        return ServiceResult.success(don, "Da duyet (he so x" + heSoOT + ").");
    }

    public ServiceResult<DangKyLamThem> tuChoiDonLamThem(int maDK, int nguoiDuyetId) {
        DangKyLamThem don = repository.findDonOTById(maDK);
        if (don == null) return ServiceResult.error("Khong tim thay don.");
        if (!don.dangChoDuyet()) return ServiceResult.error("Don da xu ly.");
        don.tuChoi(nguoiDuyetId);
        User approver = MockDataService.getInstance().getUserById(nguoiDuyetId);
        if (approver != null) don.setApproverName(approver.getFullName());
        repository.saveDonOT(don);
        return ServiceResult.success(don, "Da tu choi don OT.");
    }

    public ServiceResult<DangKyLamThem> capNhatHeSoOT(int maDK, double heSoMoi) {
        DangKyLamThem don = repository.findDonOTById(maDK);
        if (don == null) return ServiceResult.error("Khong tim thay don.");
        if (heSoMoi <= 0 || heSoMoi > 5.0) return ServiceResult.error("He so phai tu 0.1 den 5.0.");
        don.setHeSoOT(heSoMoi); repository.saveDonOT(don);
        return ServiceResult.success(don, "Da cap nhat he so = x" + heSoMoi);
    }

    public List<DangKyLamThem> getDonLamThemCuaNV(int maNV) { return repository.findDonOTByMaNV(maNV); }
    public List<DangKyLamThem> getDonChoQuanLyDuyet() { return repository.findDonOTChoDuyet(); }

    public ServiceResult<Void> xoaDonLamThem(int maDK, int maNV) {
        DangKyLamThem don = repository.findDonOTById(maDK);
        if (don == null) return ServiceResult.error("Khong tim thay don.");
        if (!don.dangChoDuyet()) return ServiceResult.error("Chi xoa don cho duyet.");
        repository.deleteDonOT(maDK); return ServiceResult.success(null, "Da xoa.");
    }

    // ── CẤU HÌNH PHỤ CẤP ──
    public List<CauHinhPhuCap> getAllCauHinhPC() { return repository.findAllCauHinhPC(); }
    public List<CauHinhPhuCap> getCauHinhPCHoatDong() { return repository.findCauHinhPCHoatDong(); }

    public ServiceResult<CauHinhPhuCap> themCauHinhPC(ThanhPhanLuong.Loai loai, String tenKhoan,
            CauHinhPhuCap.KieuTinh kieuTinh, double giaTri, String nguon) {
        if (tenKhoan == null || tenKhoan.trim().isEmpty())
            return ServiceResult.error("Ten khoan khong duoc de trong.");
        if (giaTri <= 0) return ServiceResult.error("Gia tri phai lon hon 0.");
        CauHinhPhuCap pc = new CauHinhPhuCap(loai, tenKhoan.trim(), kieuTinh, giaTri, nguon.trim());
        repository.saveCauHinhPC(pc);
        return ServiceResult.success(pc, "Da them khoan '" + tenKhoan + "'.");
    }

    public ServiceResult<CauHinhPhuCap> suaCauHinhPC(int maPC, ThanhPhanLuong.Loai loai, String tenKhoan,
            CauHinhPhuCap.KieuTinh kieuTinh, double giaTri, String nguon) {
        CauHinhPhuCap pc = repository.findCauHinhPCById(maPC);
        if (pc == null) return ServiceResult.error("Khong tim thay.");
        if (tenKhoan == null || tenKhoan.trim().isEmpty())
            return ServiceResult.error("Ten khoan khong duoc de trong.");
        pc.setLoai(loai); pc.setTenKhoan(tenKhoan.trim());
        pc.setKieuTinh(kieuTinh); pc.setGiaTri(giaTri); pc.setNguon(nguon.trim());
        repository.saveCauHinhPC(pc);
        return ServiceResult.success(pc, "Da cap nhat.");
    }

    public ServiceResult<Void> xoaCauHinhPC(int maPC) {
        CauHinhPhuCap pc = repository.findCauHinhPCById(maPC);
        if (pc == null) return ServiceResult.error("Khong tim thay.");
        repository.deleteCauHinhPC(maPC);
        return ServiceResult.success(null, "Da ngung khoan '" + pc.getTenKhoan() + "'.");
    }

    // ── TÍNH BẢNG LƯƠNG (dùng cấu hình phụ cấp) ──
    public BangLuong tinhBangLuong(int thang, int nam) {
        LocalDate ngayBD = LocalDate.of(nam, thang, 1);
        LocalDate ngayKT = ngayBD.withDayOfMonth(ngayBD.lengthOfMonth());
        BangLuong bl = new BangLuong(0, ngayBD, ngayKT);
        bl.setTrangThai(BangLuong.TrangThai.DA_TINH);
        repository.saveBangLuong(bl);

        List<CauHinhPhuCap> dsPC = repository.findCauHinhPCHoatDong();
        List<User> allUsers = MockDataService.getInstance().getAllUsers();

        for (User user : allUsers) {
            if (user.hasRole("ADMIN")) continue;
            int maNV = user.getId();
            double luongCB = repository.getLuongCoBan(maNV);
            double luong1Gio = luongCB / SO_NGAY_CONG_CHUAN / 8.0;

            // Chấm công
            List<ChamCong> dsChamCong = repository.findByThangNam(thang, nam).stream()
                .filter(cc -> cc.getMaNV() == maNV && cc.hoanTat()).collect(Collectors.toList());
            int soNgayCong = dsChamCong.size();
            double tongGioLam = dsChamCong.stream().mapToDouble(ChamCong::getSoGioLam).sum();

            // OT đã duyệt
            List<DangKyLamThem> dsOT = repository.findDonOTDaDuyetTheoThang(thang, nam)
                .stream().filter(d -> d.getMaNV() == maNV).collect(Collectors.toList());
            double tongGioOT = dsOT.stream().mapToDouble(DangKyLamThem::getSoGio).sum();
            double tienOT = dsOT.stream().mapToDouble(d -> d.getSoGio() * d.getHeSoOT() * luong1Gio).sum();

            double luongChinh = soNgayCong * 8.0 * luong1Gio;

            ChiTietLuong ct = new ChiTietLuong();
            ct.setMaBL(bl.getMaBL()); ct.setMaNV(maNV); ct.setTenNV(user.getFullName());
            ct.setLuongCoBan(luongChinh); ct.setTienOT(tienOT);
            ct.setSoNgayCong(soNgayCong); ct.setTongGioLam(tongGioLam); ct.setTongGioOT(tongGioOT);

            // Áp dụng cấu hình phụ cấp/khấu trừ
            for (CauHinhPhuCap pc : dsPC) {
                double soTien = pc.tinhSoTien(luongCB);
                ct.themThanhPhan(new ThanhPhanLuong(pc.getLoai(), pc.getTenKhoan(), soTien, pc.getNguon()));
            }

            ct.tinhTong();
            ct.setTrangThai(ChiTietLuong.TrangThai.DA_TINH);
            repository.saveChiTietLuong(ct);
        }
        return bl;
    }

    public List<ChiTietLuong> getChiTietLuong(int maBL) { return repository.findChiTietLuongByMaBL(maBL); }

    // ── SERVICE RESULT ──
    public static class ServiceResult<T> {
        private boolean success; private String message; private T data;
        private ServiceResult(boolean s, String m, T d) { success=s; message=m; data=d; }
        public static <T> ServiceResult<T> success(T data, String msg) { return new ServiceResult<>(true, msg, data); }
        public static <T> ServiceResult<T> error(String msg) { return new ServiceResult<>(false, msg, null); }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
}
package com.hrm.repo;

import com.hrm.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceRepository {

    private static AttendanceRepository instance;

    private Map<String, CaLam> danhSachCaLam;
    private Map<Integer, ChamCong> danhSachChamCong;
    private Map<Integer, DangKyLamThem> danhSachDonOT;
    private Map<Integer, BangLuong> danhSachBangLuong;
    private Map<Integer, ChiTietLuong> danhSachChiTietLuong;
    private Map<Integer, Double> luongCoBan;
    private Map<Integer, CauHinhPhuCap> danhSachCauHinhPC;

    private int nextChamCongId = 1;
    private int nextDonOTId = 1;
    private int nextBangLuongId = 1;
    private int nextChiTietLuongId = 1;
    private int nextCauHinhPCId = 1;

    private AttendanceRepository() {
        danhSachCaLam = new LinkedHashMap<>();
        danhSachChamCong = new LinkedHashMap<>();
        danhSachDonOT = new LinkedHashMap<>();
        danhSachBangLuong = new LinkedHashMap<>();
        danhSachChiTietLuong = new LinkedHashMap<>();
        luongCoBan = new LinkedHashMap<>();
        danhSachCauHinhPC = new LinkedHashMap<>();
        initializeMockData();
    }

    public static synchronized AttendanceRepository getInstance() {
        if (instance == null) instance = new AttendanceRepository();
        return instance;
    }

    private void initializeMockData() {
        // Ca làm
        CaLam caHC = new CaLam("HANH_CHINH", "Ca hanh chinh", LocalTime.of(8, 0), LocalTime.of(17, 0));
        caHC.setSoGioChuan(8.0); danhSachCaLam.put(caHC.getMaCaLam(), caHC);
        CaLam caSang = new CaLam("CA_SANG", "Ca sang", LocalTime.of(6, 0), LocalTime.of(14, 0));
        caSang.setSoGioChuan(8.0); danhSachCaLam.put(caSang.getMaCaLam(), caSang);
        CaLam caChieu = new CaLam("CA_CHIEU", "Ca chieu", LocalTime.of(14, 0), LocalTime.of(22, 0));
        caChieu.setSoGioChuan(8.0); danhSachCaLam.put(caChieu.getMaCaLam(), caChieu);

        // Lương cơ bản
        luongCoBan.put(2, 15000000.0);
        luongCoBan.put(3, 20000000.0);
        luongCoBan.put(4, 10000000.0);

        // ── Cấu hình phụ cấp / khấu trừ mặc định ──
        saveCauHinhPC(new CauHinhPhuCap(ThanhPhanLuong.Loai.PHU_CAP,
                "Phu cap an trua", CauHinhPhuCap.KieuTinh.CO_DINH, 500000, "CongTy"));
        saveCauHinhPC(new CauHinhPhuCap(ThanhPhanLuong.Loai.PHU_CAP,
                "Phu cap di lai", CauHinhPhuCap.KieuTinh.CO_DINH, 300000, "CongTy"));
        saveCauHinhPC(new CauHinhPhuCap(ThanhPhanLuong.Loai.PHU_CAP,
                "Phu cap dien thoai", CauHinhPhuCap.KieuTinh.CO_DINH, 200000, "CongTy"));
        saveCauHinhPC(new CauHinhPhuCap(ThanhPhanLuong.Loai.KHAU_TRU,
                "BHXH (8%)", CauHinhPhuCap.KieuTinh.PHAN_TRAM, 8.0, "LuatDinh"));
        saveCauHinhPC(new CauHinhPhuCap(ThanhPhanLuong.Loai.KHAU_TRU,
                "BHYT (1.5%)", CauHinhPhuCap.KieuTinh.PHAN_TRAM, 1.5, "LuatDinh"));
        saveCauHinhPC(new CauHinhPhuCap(ThanhPhanLuong.Loai.KHAU_TRU,
                "BHTN (1%)", CauHinhPhuCap.KieuTinh.PHAN_TRAM, 1.0, "LuatDinh"));

        // Chấm công mẫu
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 1; i--) {
            LocalDate ngay = today.minusDays(i);
            if (ngay.getDayOfWeek().getValue() >= 6) continue;
            ChamCong cc = new ChamCong();
            cc.setMaChamCong(nextChamCongId++); cc.setMaNV(4); cc.setNgay(ngay);
            cc.setMaCaLam("HANH_CHINH"); cc.setTenCaLam("Ca hanh chinh");
            cc.setGioVao(ngay.atTime(8, 0)); cc.setGioRa(ngay.atTime(17, 15));
            cc.setSoGioLam(cc.tinhSoGioLam()); cc.setGioLamThem(0);
            cc.setTrangThai(ChamCong.TrangThai.DUNG_GIO);
            cc.setPhuongThucChamCong(ChamCong.PhuongThuc.THU_CONG);
            cc.setEmployeeName("Nguyen Van An");
            danhSachChamCong.put(cc.getMaChamCong(), cc);
        }

        // Đơn OT mẫu
        DangKyLamThem don1 = new DangKyLamThem(4, today.minusDays(3), 2.0, "Hoan thanh bao cao");
        don1.setMaDK(nextDonOTId++); don1.setHeSoOT(1.5);
        don1.duyet(1); don1.setApproverName("Administrator");
        danhSachDonOT.put(don1.getMaDK(), don1);
        DangKyLamThem don2 = new DangKyLamThem(4, today.plusDays(1), 3.0, "Fix bug gap");
        don2.setMaDK(nextDonOTId++); don2.setHeSoOT(1.5);
        danhSachDonOT.put(don2.getMaDK(), don2);
    }

    // ── CA LÀM ──
    public List<CaLam> findCaLamHoatDong() {
        return danhSachCaLam.values().stream().filter(CaLam::conHoatDong).collect(Collectors.toList());
    }
    public CaLam findCaLamByMa(String ma) { return danhSachCaLam.get(ma); }
    public CaLam saveCaLam(CaLam ca) { danhSachCaLam.put(ca.getMaCaLam(), ca); return ca; }
    public boolean deleteCaLam(String ma) {
        CaLam ca = danhSachCaLam.get(ma);
        if (ca != null) { ca.setTrangThai(CaLam.TrangThai.NGUNG_HOAT_DONG); return true; }
        return false;
    }
    public List<CaLam> findAllCaLam() { return new ArrayList<>(danhSachCaLam.values()); }

    // ── CHẤM CÔNG ──
    public ChamCong saveChamCong(ChamCong cc) {
        if (cc.getMaChamCong() == 0) cc.setMaChamCong(nextChamCongId++);
        danhSachChamCong.put(cc.getMaChamCong(), cc); return cc;
    }
    public ChamCong findByMaNVAndNgay(int maNV, LocalDate ngay) {
        return danhSachChamCong.values().stream()
            .filter(cc -> cc.getMaNV() == maNV && cc.getNgay().equals(ngay)).findFirst().orElse(null);
    }
    public List<ChamCong> findByMaNVAndNgayBetween(int maNV, LocalDate tu, LocalDate den) {
        return danhSachChamCong.values().stream()
            .filter(cc -> cc.getMaNV() == maNV && !cc.getNgay().isBefore(tu) && !cc.getNgay().isAfter(den))
            .sorted(Comparator.comparing(ChamCong::getNgay).reversed()).collect(Collectors.toList());
    }
    public List<ChamCong> findByThangNam(int thang, int nam) {
        return danhSachChamCong.values().stream()
            .filter(cc -> cc.getNgay().getMonthValue() == thang && cc.getNgay().getYear() == nam)
            .sorted(Comparator.comparing(ChamCong::getNgay).thenComparing(ChamCong::getMaNV)).collect(Collectors.toList());
    }

    // ── ĐƠN OT ──
    public DangKyLamThem saveDonOT(DangKyLamThem don) {
        if (don.getMaDK() == 0) don.setMaDK(nextDonOTId++);
        danhSachDonOT.put(don.getMaDK(), don); return don;
    }
    public DangKyLamThem findDonOTById(int maDK) { return danhSachDonOT.get(maDK); }
    public List<DangKyLamThem> findDonOTByMaNV(int maNV) {
        return danhSachDonOT.values().stream().filter(d -> d.getMaNV() == maNV)
            .sorted(Comparator.comparing(DangKyLamThem::getNgayTao).reversed()).collect(Collectors.toList());
    }
    public List<DangKyLamThem> findDonOTChoDuyet() {
        return danhSachDonOT.values().stream().filter(DangKyLamThem::dangChoDuyet)
            .sorted(Comparator.comparing(DangKyLamThem::getNgayTao).reversed()).collect(Collectors.toList());
    }
    public List<DangKyLamThem> findAllDonOT() { return new ArrayList<>(danhSachDonOT.values()); }
    public List<DangKyLamThem> findDonOTDaDuyetTheoThang(int thang, int nam) {
        return danhSachDonOT.values().stream()
            .filter(d -> d.daDuocDuyet() && d.getNgay() != null
                && d.getNgay().getMonthValue() == thang && d.getNgay().getYear() == nam)
            .collect(Collectors.toList());
    }
    public boolean deleteDonOT(int maDK) { return danhSachDonOT.remove(maDK) != null; }

    // ── BẢNG LƯƠNG ──
    public BangLuong saveBangLuong(BangLuong bl) {
        if (bl.getMaBL() == 0) bl.setMaBL(nextBangLuongId++);
        danhSachBangLuong.put(bl.getMaBL(), bl); return bl;
    }
    public List<BangLuong> findAllBangLuong() { return new ArrayList<>(danhSachBangLuong.values()); }

    // ── CHI TIẾT LƯƠNG ──
    public ChiTietLuong saveChiTietLuong(ChiTietLuong ct) {
        if (ct.getMaChiTietLuong() == 0) ct.setMaChiTietLuong(nextChiTietLuongId++);
        danhSachChiTietLuong.put(ct.getMaChiTietLuong(), ct); return ct;
    }
    public List<ChiTietLuong> findChiTietLuongByMaBL(int maBL) {
        return danhSachChiTietLuong.values().stream()
            .filter(ct -> ct.getMaBL() == maBL).collect(Collectors.toList());
    }

    // ── LƯƠNG CƠ BẢN ──
    public double getLuongCoBan(int maNV) { return luongCoBan.getOrDefault(maNV, 10000000.0); }
    public void setLuongCoBan(int maNV, double luong) { luongCoBan.put(maNV, luong); }

    // ── CẤU HÌNH PHỤ CẤP / KHẤU TRỪ ──
    public CauHinhPhuCap saveCauHinhPC(CauHinhPhuCap pc) {
        if (pc.getMaPC() == 0) pc.setMaPC(nextCauHinhPCId++);
        danhSachCauHinhPC.put(pc.getMaPC(), pc); return pc;
    }
    public CauHinhPhuCap findCauHinhPCById(int maPC) { return danhSachCauHinhPC.get(maPC); }
    public List<CauHinhPhuCap> findAllCauHinhPC() { return new ArrayList<>(danhSachCauHinhPC.values()); }
    public List<CauHinhPhuCap> findCauHinhPCHoatDong() {
        return danhSachCauHinhPC.values().stream()
            .filter(CauHinhPhuCap::isHoatDong).collect(Collectors.toList());
    }
    public boolean deleteCauHinhPC(int maPC) {
        CauHinhPhuCap pc = danhSachCauHinhPC.get(maPC);
        if (pc != null) { pc.setHoatDong(false); return true; }
        return false;
    }
}
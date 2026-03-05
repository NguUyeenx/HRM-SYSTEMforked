package com.hrm.service;

import com.hrm.model.*;
import com.hrm.repo.AttendanceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    /**
     * Check-in thủ công với ca chỉ định (dùng cho Admin/HR nhập tay).
     */
    public ServiceResult<ChamCong> checkIn(int maNV, String maCaLam) {
        if (repository.findByMaNVAndNgay(maNV, LocalDate.now()) != null)
            return ServiceResult.error("Da check-in hom nay.");
        CaLam caLam = repository.findCaLamByMa(maCaLam);
        if (caLam == null) return ServiceResult.error("Ca lam khong hop le.");
        return doCheckIn(maNV, caLam, false);
    }

    /**
     * Check-in TỰ ĐỘNG — hệ thống tự nhận diện ca làm theo giờ hiện tại.
     *
     * Logic nhận diện ca:
     * 1. Ưu tiên ca đang diễn ra: nowTime trong [gioBD - 30ph, gioKT]
     * 2. Fallback: ca có giờ bắt đầu gần nhất trong vòng ±2h
     *
     * Logic đi muộn: check-in sau (gioBD + 5 phút) → DI_MUON.
     *
     * @param maNV  Mã nhân viên (int PK)
     * @param laOT  true nếu nhân viên đánh dấu đây là ca OT
     * @return ServiceResult chứa ChamCong vừa tạo, hoặc thông báo lỗi
     */
    public ServiceResult<ChamCong> checkInAuto(int maNV, boolean laOT) {
        if (repository.findByMaNVAndNgay(maNV, LocalDate.now()) != null)
            return ServiceResult.error("Da check-in hom nay.");

        if (laOT && !coOTDaDuyetHomNay(maNV))
            return ServiceResult.error(
                "Ban chua co don OT duoc duyet cho ngay hom nay. Khong the danh dau ca OT.");

        java.time.LocalTime nowTime = LocalDateTime.now().toLocalTime();
        List<CaLam> dsCaLam = repository.findCaLamHoatDong();

        if (dsCaLam.isEmpty())
            return ServiceResult.error("Khong co ca lam nao dang hoat dong.");

        // ── Bước 1: Ca đang trong cửa sổ check-in [gioBD - 30ph → gioKT] ──
        // Nếu có nhiều ca khớp (rất hiếm), chọn ca có giờ bắt đầu GẦN NHẤT với now
        CaLam caLamPhuHop = dsCaLam.stream()
            .filter(ca -> trongCuaSoCheckIn(ca, nowTime))
            .min(Comparator.comparingLong(ca -> khoangCachDenGioBatDau(ca, nowTime)))
            .orElse(null);

        // ── Bước 2: Fallback — ca gần nhất trong vòng ±2h ──
        if (caLamPhuHop == null) {
            caLamPhuHop = dsCaLam.stream()
                .filter(ca -> khoangCachDenGioBatDau(ca, nowTime) <= 120)
                .min(Comparator.comparingLong(ca -> khoangCachDenGioBatDau(ca, nowTime)))
                .orElse(null);
        }

        if (caLamPhuHop == null)
            return ServiceResult.error(
                "Khong tim thay ca lam phu hop voi gio hien tai (" +
                nowTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + ").");

        return doCheckIn(maNV, caLamPhuHop, laOT);
    }

    // ================================================================
    // HELPER: Kiểm tra xem giờ hiện tại có nằm trong ca không
    // Hỗ trợ đúng ca qua nửa đêm (vd: 22:00 → 06:00)
    // Cửa sổ check-in mở sớm 30 phút trước giờ bắt đầu.
    // ================================================================
    private boolean trongCuaSoCheckIn(CaLam ca, java.time.LocalTime now) {
        java.time.LocalTime batDau = ca.getGioBatDau().minusMinutes(30); // mở sớm 30 phút
        java.time.LocalTime ketThuc = ca.getGioKetThuc();
        
        if (!batDau.isAfter(ketThuc)) {
            // ── Ca thường (không qua đêm) ──
            // ví dụ: batDau=07:30, ketThuc=17:00
            return !now.isBefore(batDau) && !now.isAfter(ketThuc);
        } else {
            // ── Ca qua đêm ──
            // ví dụ: batDau=21:30 (22:00-30ph), ketThuc=06:00
            // Hợp lệ khi: now >= 21:30 HOẶC now <= 06:00
            return !now.isBefore(batDau) || !now.isAfter(ketThuc);
        }
    }

    /**
     * Tính khoảng cách (phút) từ giờ hiện tại đến giờ bắt đầu ca.
     * Luôn trả về giá trị dương, hỗ trợ vòng quanh nửa đêm.
     * Ví dụ: now=23:50, batDau=00:10 → 20 phút (không phải -1430 phút)
     */
    private long khoangCachDenGioBatDau(CaLam ca, java.time.LocalTime now) {
        long diff = java.time.Duration.between(now, ca.getGioBatDau()).toMinutes();
        // Chuẩn hóa về [-720, +720] — lấy khoảng gần nhất qua nửa đêm
        if (diff > 720)  diff -= 1440;
        if (diff < -720) diff += 1440;
        return Math.abs(diff);
    }


    /**
     * Logic check-in dùng chung.
     * Ngưỡng đi muộn: sau gioBatDau + 5 phút.
     *
     * @param laOT đánh dấu đây là ca OT (lưu vào ghiChu = "OT")
     */
    private ServiceResult<ChamCong> doCheckIn(int maNV, CaLam caLam, boolean laOT) {
        ChamCong cc = new ChamCong(maNV, LocalDate.now(), caLam.getMaCaLam());
        cc.setTenCaLam(caLam.getTenCaLam());
        cc.setGioVao(LocalDateTime.now());
        cc.setPhuongThucChamCong(ChamCong.PhuongThuc.THU_CONG);

        // Đi muộn nếu check-in sau gioBatDau + 5 phút (thay vì 15 phút cũ)
        cc.setTrangThai(
            LocalDateTime.now().toLocalTime().isAfter(caLam.getGioBatDau().plusMinutes(5))
                ? ChamCong.TrangThai.DI_MUON
                : ChamCong.TrangThai.DUNG_GIO);

        // Đánh dấu OT vào ghiChu — không cần thêm cột DB
        cc.setLaOT(laOT);

        repository.saveChamCong(cc);

        String otInfo = laOT ? " [CA OT]" : "";
        return ServiceResult.success(cc,
            "Check-in thanh cong" + otInfo + " — Ca: " + caLam.getTenCaLam() +
            " (" + caLam.getGioBatDau() + " - " + caLam.getGioKetThuc() + ")");
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
    public ServiceResult<DangKyLamThem> taoDonLamThem(
            int maNV, LocalDate ngay,
            java.time.LocalTime gioVao, java.time.LocalTime gioRa,
            String lyDo) {

        if (gioVao == null || gioRa == null)
            return ServiceResult.error("Vui long nhap gio bat dau va gio ket thuc OT.");
        if (lyDo == null || lyDo.trim().isEmpty())
            return ServiceResult.error("Nhap ly do.");

        double soGio = DangKyLamThem.tinhSoGioOT(gioVao, gioRa);
        if (soGio < 0.5 || soGio > 8)
            return ServiceResult.error("Khoang thoi gian OT phai tu 0.5 den 8 gio.");

        DangKyLamThem don = new DangKyLamThem(maNV, ngay, gioVao, gioRa, lyDo.trim());
        repository.saveDonOT(don);
        return ServiceResult.success(don,
            "Da tao don OT: " + gioVao + " → " + gioRa +
            " (" + String.format("%.1f", soGio) + " gio).");
    }

    /**
     * Tạo đơn OT theo số giờ (cũ) — giữ để backward-compatible với phần khác.
     * @deprecated Dùng {@link #taoDonLamThem(int, LocalDate, LocalTime, LocalTime, String)} thay thế.
     */
    @Deprecated
    public ServiceResult<DangKyLamThem> taoDonLamThem(int maNV, LocalDate ngay, double soGio, String lyDo) {
        if (soGio <= 0 || soGio > 8) return ServiceResult.error("So gio phai tu 0.5 den 8.");
        if (lyDo == null || lyDo.trim().isEmpty()) return ServiceResult.error("Nhap ly do.");
        DangKyLamThem don = new DangKyLamThem(maNV, ngay, soGio, lyDo.trim());
        repository.saveDonOT(don);
        return ServiceResult.success(don, "Da tao don OT.");
    }

    public boolean coOTDaDuyetHomNay(int maNV) {
        return repository.coOTDaDuyetTheoNgay(maNV, LocalDate.now());
    }


public ServiceResult<DangKyLamThem> duyetDonLamThem(int maDK, int nguoiDuyetId, double heSoOT) {
    DangKyLamThem don = repository.findDonOTById(maDK);
    if (don == null) return ServiceResult.error("Khong tim thay don.");
    if (!don.dangChoDuyet()) return ServiceResult.error("Don da xu ly.");
    don.setHeSoOT(heSoOT);
    don.duyet(nguoiDuyetId);
    // FIX: Lấy tên người duyệt từ DB thay vì MockDataService
    String approverName = repository.getHoTenByMaNV(nguoiDuyetId);
    if (approverName != null) don.setApproverName(approverName);
    repository.saveDonOT(don);
    return ServiceResult.success(don, "Da duyet (he so x" + heSoOT + ").");
}

public ServiceResult<DangKyLamThem> tuChoiDonLamThem(int maDK, int nguoiDuyetId) {
    DangKyLamThem don = repository.findDonOTById(maDK);
    if (don == null) return ServiceResult.error("Khong tim thay don.");
    if (!don.dangChoDuyet()) return ServiceResult.error("Don da xu ly.");
    don.tuChoi(nguoiDuyetId);
    // FIX: Lấy tên người duyệt từ DB thay vì MockDataService
    String approverName = repository.getHoTenByMaNV(nguoiDuyetId);
    if (approverName != null) don.setApproverName(approverName);
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

    // ── TÍNH BẢNG LƯƠNG ──
    public BangLuong tinhBangLuong(int thang, int nam) {
        LocalDate ngayBD = LocalDate.of(nam, thang, 1);
        LocalDate ngayKT = ngayBD.withDayOfMonth(ngayBD.lengthOfMonth());

        BangLuong bl = new BangLuong(0, ngayBD, ngayKT);
        bl.setTrangThai(BangLuong.TrangThai.DA_TINH);
        repository.saveBangLuong(bl);

        // Nếu INSERT bị duplicate → tháng đó đã có bảng lương → tìm lại
        if (bl.getMaBL() == 0) {
            for (BangLuong ex : repository.findAllBangLuong()) {
                if (ex.getThang() == thang && ex.getNam() == nam) {
                    bl.setMaBL(ex.getMaBL());
                    break;
                }
            }
        }
        if (bl.getMaBL() == 0) return bl; // Vẫn không có → lỗi DB

        List<CauHinhPhuCap> dsPC = repository.findCauHinhPCHoatDong();

        // Lấy toàn bộ dữ liệu 1 lần (tránh N+1 query)
        List<ChamCong>      tatCaCC = repository.findByThangNam(thang, nam);
        List<DangKyLamThem> tatCaOT = repository.findDonOTDaDuyetTheoThang(thang, nam);

        // Nhóm chấm công theo maNV → chỉ xử lý những NV có ít nhất 1 bản ghi hoàn tất
        Map<Integer, List<ChamCong>> ccTheoNV = tatCaCC.stream()
            .filter(ChamCong::hoanTat)
            .collect(Collectors.groupingBy(ChamCong::getMaNV));

        // ✅ Chỉ tính lương cho NV có giờ công > 0 trong tháng
        for (Map.Entry<Integer, List<ChamCong>> entry : ccTheoNV.entrySet()) {
            int maNV = entry.getKey();
            List<ChamCong> dsChamCong = entry.getValue();

            int    soNgayCong = dsChamCong.size();
            double tongGioLam = dsChamCong.stream().mapToDouble(ChamCong::getSoGioLam).sum();

            // Bỏ qua NV không có giờ làm thực tế
            if (soNgayCong == 0 || tongGioLam <= 0) continue;

            double luongCB   = repository.getLuongCoBan(maNV);
            double luong1Gio = (luongCB > 0) ? luongCB / SO_NGAY_CONG_CHUAN / 8.0 : 0;

            List<DangKyLamThem> dsOT = tatCaOT.stream()
                .filter(d -> d.getMaNV() == maNV)
                .collect(Collectors.toList());
            double tongGioOT = dsOT.stream().mapToDouble(DangKyLamThem::getSoGio).sum();
            double tienOT    = dsOT.stream()
                .mapToDouble(d -> d.getSoGio() * d.getHeSoOT() * luong1Gio).sum();

            double luongChinh = tongGioLam * luong1Gio;

            // Lấy tên NV từ chấm công (đã JOIN THONGTINCANHAN khi load)
            String tenNV = dsChamCong.stream()
                .map(ChamCong::getEmployeeName)
                .filter(n -> n != null && !n.isEmpty())
                .findFirst()
                .orElse("NV-" + maNV);

            ChiTietLuong ct = new ChiTietLuong();
            ct.setMaBL(bl.getMaBL());
            ct.setMaNV(maNV);
            ct.setTenNV(tenNV);
            ct.setLuongCoBan(luongChinh);
            ct.setTienOT(tienOT);
            ct.setSoNgayCong(soNgayCong);
            ct.setTongGioLam(tongGioLam);
            ct.setTongGioOT(tongGioOT);

            for (CauHinhPhuCap pc : dsPC) {
                ct.themThanhPhan(new ThanhPhanLuong(
                    pc.getLoai(), pc.getTenKhoan(), pc.tinhSoTien(luongCB), pc.getNguon()));
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
package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BangLuong {

    public enum TrangThai {
        NHAP("nhap", "Nháp"),
        DA_TINH("da_tinh", "Đã tính"),
        DA_DUYET("da_duyet", "Đã duyệt"),
        DA_CHI("da_chi", "Đã chi"),
        // Alias cho DB schema mới (dang_xu_ly / da_khoa)
        DANG_XU_LY("dang_xu_ly", "Đang xử lý"),
        DA_KHOA("da_khoa", "Đã khóa");

        private final String dbValue;
        private final String displayName;

        TrangThai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        public static TrangThai fromDbValue(String value) {
            for (TrangThai t : values()) {
                if (t.dbValue.equals(value)) return t;
            }
            return NHAP;
        }
    }

    private int maBL;           // PK — alias cho maBangLuong
    private int maNV;           // giữ lại để không break Service
    private int thang;
    private int nam;
    private LocalDate ngayBD;   // giữ lại để không break Service
    private LocalDate ngayKT;   // giữ lại để không break Service
    private String tenBangLuong;
    private TrangThai trangThai;
    private LocalDateTime ngayTao;

    public BangLuong() {
        this.trangThai = TrangThai.NHAP;
        this.ngayTao = LocalDateTime.now();
    }

    /**
     * Constructor CŨ — giữ để AttendanceService không lỗi.
     * tự động tính thang/nam từ ngayBD.
     */
    public BangLuong(int maNV, LocalDate ngayBD, LocalDate ngayKT) {
        this();
        this.maNV = maNV;
        this.ngayBD = ngayBD;
        this.ngayKT = ngayKT;
        // Tự tính thang/nam để saveBangLuong() dùng được
        if (ngayBD != null) {
            this.thang = ngayBD.getMonthValue();
            this.nam   = ngayBD.getYear();
            this.tenBangLuong = "Bang luong thang " + thang + "/" + nam;
        }
    }

    /** Constructor MỚI — khớp DB: chỉ cần thang + nam */
    public BangLuong(int thang, int nam) {
        this();
        this.thang = thang;
        this.nam   = nam;
        this.ngayBD = LocalDate.of(nam, thang, 1);
        this.ngayKT = ngayBD.withDayOfMonth(ngayBD.lengthOfMonth());
        this.tenBangLuong = "Bang luong thang " + thang + "/" + nam;
    }

    // Getters & Setters
    public int getMaBL() { return maBL; }
    public void setMaBL(int maBL) { this.maBL = maBL; }

    // Alias cho DB column tên maBangLuong
    public int getMaBangLuong() { return maBL; }
    public void setMaBangLuong(int v) { this.maBL = v; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public int getThang() { return thang; }
    public void setThang(int thang) { this.thang = thang; }

    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }

    public LocalDate getNgayBD() { return ngayBD; }
    public void setNgayBD(LocalDate ngayBD) {
        this.ngayBD = ngayBD;
        if (ngayBD != null) { this.thang = ngayBD.getMonthValue(); this.nam = ngayBD.getYear(); }
    }

    public LocalDate getNgayKT() { return ngayKT; }
    public void setNgayKT(LocalDate ngayKT) { this.ngayKT = ngayKT; }

    public String getTenBangLuong() { return tenBangLuong; }
    public void setTenBangLuong(String tenBangLuong) { this.tenBangLuong = tenBangLuong; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    @Override
    public String toString() {
        return tenBangLuong != null ? tenBangLuong
               : "BangLuong{" + ngayBD + " - " + ngayKT + "}";
    }
}
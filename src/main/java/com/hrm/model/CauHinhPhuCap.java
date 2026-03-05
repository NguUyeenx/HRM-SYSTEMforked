package com.hrm.model;

/**
 * Model cấu hình phụ cấp / khấu trừ mặc định.
 * Admin quản lý danh sách này. Khi tính lương,
 * hệ thống tự động áp dụng các khoản này cho mỗi NV.
 */
public class CauHinhPhuCap {

    /**
     * Kiểu tính giá trị phụ cấp/khấu trừ.
     * DB có thể lưu "theo_phan_tram" hoặc "phan_tram" — cả 2 đều map về PHAN_TRAM.
     */
    public enum KieuTinh {
        CO_DINH("co_dinh", "Cố định"),
        PHAN_TRAM("phan_tram", "% Lương CB");

        private final String dbValue;
        private final String displayName;

        KieuTinh(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        /** Chuyển chuỗi từ DB → enum */
        public static KieuTinh fromDbValue(String value) {
            if (value == null) return CO_DINH;
            // Hỗ trợ cả 2 tên cũ/mới trong DB
            if ("theo_phan_tram".equals(value) || "phan_tram".equals(value)) return PHAN_TRAM;
            return CO_DINH;
        }
    }

    private int maPC;
    private ThanhPhanLuong.Loai loai;
    private String tenKhoan;
    private KieuTinh kieuTinh;
    private double giaTri;   // Số tiền cố định HOẶC % tùy kieuTinh
    private String nguon;
    private boolean hoatDong;

    public CauHinhPhuCap() {
        this.hoatDong = true;
        this.kieuTinh = KieuTinh.CO_DINH;
    }

    public CauHinhPhuCap(ThanhPhanLuong.Loai loai, String tenKhoan,
                          KieuTinh kieuTinh, double giaTri, String nguon) {
        this();
        this.loai = loai;
        this.tenKhoan = tenKhoan;
        this.kieuTinh = kieuTinh;
        this.giaTri = giaTri;
        this.nguon = nguon;
    }

    /**
     * Tính số tiền thực tế dựa trên lương cơ bản.
     * - CO_DINH: trả về giaTri (VD: 500_000)
     * - PHAN_TRAM: trả về luongCoBan × giaTri / 100 (VD: 8%)
     */
    public double tinhSoTien(double luongCoBan) {
        if (kieuTinh == KieuTinh.PHAN_TRAM) {
            return Math.round(luongCoBan * giaTri / 100.0);
        }
        return giaTri;
    }

    /** Hiển thị giá trị: "500000" hoặc "8%" */
    public String hienThiGiaTri() {
        if (kieuTinh == KieuTinh.PHAN_TRAM) {
            return giaTri % 1 == 0 ? (int) giaTri + "%" : giaTri + "%";
        }
        return String.valueOf((long) giaTri);
    }

    // Getters & Setters
    public int getMaPC() { return maPC; }
    public void setMaPC(int maPC) { this.maPC = maPC; }

    public ThanhPhanLuong.Loai getLoai() { return loai; }
    public void setLoai(ThanhPhanLuong.Loai loai) { this.loai = loai; }

    public String getTenKhoan() { return tenKhoan; }
    public void setTenKhoan(String tenKhoan) { this.tenKhoan = tenKhoan; }

    public KieuTinh getKieuTinh() { return kieuTinh; }
    public void setKieuTinh(KieuTinh kieuTinh) { this.kieuTinh = kieuTinh; }

    public double getGiaTri() { return giaTri; }
    public void setGiaTri(double giaTri) { this.giaTri = giaTri; }

    /**
     * Alias cho getGiaTri() — dùng trong AttendanceRepository khi đọc từ DB.
     * DB lưu cột tên "giaTriMacDinh", nên getter này để nhất quán.
     */
    public double getGiaTriMacDinh() { return giaTri; }
    public void setGiaTriMacDinh(double giaTri) { this.giaTri = giaTri; }

    public String getNguon() { return nguon; }
    public void setNguon(String nguon) { this.nguon = nguon; }

    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }
}
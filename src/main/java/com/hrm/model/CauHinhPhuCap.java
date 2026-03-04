package com.hrm.model;

/**
 * Model cấu hình phụ cấp / khấu trừ mặc định.
 *
 * Admin quản lý danh sách này. Khi tính lương,
 * hệ thống tự động áp dụng các khoản này cho mỗi NV.
 *
 * Có 2 kiểu tính:
 *   CO_DINH  → số tiền cố định (VD: phụ cấp ăn trưa = 500,000đ)
 *   PHAN_TRAM → % trên lương cơ bản (VD: BHXH = 8% lương CB)
 */
public class CauHinhPhuCap {

    public enum KieuTinh {
        CO_DINH("co_dinh", "Co dinh"),
        PHAN_TRAM("phan_tram", "% Luong CB");

        private final String dbValue;
        private final String displayName;

        KieuTinh(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }
    }

    private int maPC;
    private ThanhPhanLuong.Loai loai;   // PHU_CAP hoặc KHAU_TRU
    private String tenKhoan;
    private KieuTinh kieuTinh;
    private double giaTri;              // Số tiền (nếu cố định) hoặc % (nếu phần trăm)
    private String nguon;               // CongTy, LuatDinh, ChucVu...
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
     * - Cố định: trả về giaTri
     * - Phần trăm: trả về luongCoBan × giaTri / 100
     */
    public double tinhSoTien(double luongCoBan) {
        if (kieuTinh == KieuTinh.PHAN_TRAM) {
            return Math.round(luongCoBan * giaTri / 100.0);
        }
        return giaTri;
    }

    /** Hiển thị giá trị (500,000đ hoặc 8%) */
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

    public String getNguon() { return nguon; }
    public void setNguon(String nguon) { this.nguon = nguon; }

    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }

    @Override
    public String toString() {
        return loai.getDisplayName() + ": " + tenKhoan + " = " + hienThiGiaTri();
    }
}
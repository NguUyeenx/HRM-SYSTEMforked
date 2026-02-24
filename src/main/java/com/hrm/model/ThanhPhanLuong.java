package com.hrm.model;

/**
 * Model đại diện cho bảng THANHPHANLUONG.
 *
 * Mỗi bản ghi = 1 khoản thu/chi trong chi tiết lương.
 * Ví dụ:
 *   PHU_CAP  → "Phụ cấp chức vụ"     +2,000,000đ  (nguon: ChucVu)
 *   PHU_CAP  → "Phụ cấp ăn trưa"     +500,000đ    (nguon: CongTy)
 *   KHAU_TRU → "BHXH (8%)"           -800,000đ    (nguon: LuatDinhBHXH)
 *   KHAU_TRU → "Thuế TNCN"           -500,000đ    (nguon: LuatThue)
 */
public class ThanhPhanLuong {

    public enum Loai {
        PHU_CAP("phu_cap", "Phụ cấp"),
        KHAU_TRU("khau_tru", "Khấu trừ");

        private final String dbValue;
        private final String displayName;

        Loai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }
    }

    private int maTp;
    private int maCTLuong;       // FK → CHITIETLUONG
    private Loai loai;
    private String tenKhoan;
    private double soTien;
    private String nguon;        // Nguồn gốc: ChucVu, CongTy, LuatDinh...

    public ThanhPhanLuong() {}

    public ThanhPhanLuong(Loai loai, String tenKhoan, double soTien, String nguon) {
        this.loai = loai;
        this.tenKhoan = tenKhoan;
        this.soTien = soTien;
        this.nguon = nguon;
    }

    // Getters & Setters
    public int getMaTp() { return maTp; }
    public void setMaTp(int maTp) { this.maTp = maTp; }

    public int getMaCTLuong() { return maCTLuong; }
    public void setMaCTLuong(int maCTLuong) { this.maCTLuong = maCTLuong; }

    public Loai getLoai() { return loai; }
    public void setLoai(Loai loai) { this.loai = loai; }

    public String getTenKhoan() { return tenKhoan; }
    public void setTenKhoan(String tenKhoan) { this.tenKhoan = tenKhoan; }

    public double getSoTien() { return soTien; }
    public void setSoTien(double soTien) { this.soTien = soTien; }

    public String getNguon() { return nguon; }
    public void setNguon(String nguon) { this.nguon = nguon; }

    @Override
    public String toString() {
        return loai.getDisplayName() + ": " + tenKhoan + " = " + soTien;
    }
}
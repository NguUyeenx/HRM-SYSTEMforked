package com.hrm.model;

/**
 * Khớp với bảng THANHPHANLUONG trong DB:
 *   maThanhPhan, maChiTiet, tenThanhPhan,
 *   loai ENUM('thu_nhap','khau_tru'), soTien
 *
 * Lưu ý: DB dùng ENUM 'thu_nhap'/'khau_tru'
 * Code Java dùng PHU_CAP/KHAU_TRU → fromDbValue() xử lý mapping
 */
public class ThanhPhanLuong {

    public enum Loai {
        PHU_CAP("thu_nhap", "Phụ cấp"),   // DB value = 'thu_nhap'
        KHAU_TRU("khau_tru", "Khấu trừ"); // DB value = 'khau_tru'

        private final String dbValue;
        private final String displayName;

        Loai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        public static Loai fromDbValue(String value) {
            for (Loai l : values()) if (l.dbValue.equals(value)) return l;
            // Tương thích với code cũ dùng 'phu_cap'
            if ("phu_cap".equals(value)) return PHU_CAP;
            return PHU_CAP;
        }
    }

    private int maTp;
    private int maChiTiet;       // FK → CHITIETLUONG.maChiTiet (đúng tên DB)
    private String tenThanhPhan; // DB: tenThanhPhan (không phải tenKhoan)
    private Loai loai;
    private double soTien;
    private String nguon;        // Transient — không có trong DB, dùng nội bộ

    public ThanhPhanLuong() {}

    public ThanhPhanLuong(Loai loai, String tenThanhPhan, double soTien, String nguon) {
        this.loai = loai;
        this.tenThanhPhan = tenThanhPhan;
        this.soTien = soTien;
        this.nguon = nguon;
    }

    public int getMaTp() { return maTp; }
    public void setMaTp(int maTp) { this.maTp = maTp; }

    public int getMaCTLuong() { return maChiTiet; }
    public void setMaCTLuong(int v) { this.maChiTiet = v; }

    public int getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(int maChiTiet) { this.maChiTiet = maChiTiet; }

    public Loai getLoai() { return loai; }
    public void setLoai(Loai loai) { this.loai = loai; }

    /** DB dùng tenThanhPhan, alias getTenKhoan() cho code cũ */
    public String getTenKhoan() { return tenThanhPhan; }
    public void setTenKhoan(String v) { this.tenThanhPhan = v; }

    public String getTenThanhPhan() { return tenThanhPhan; }
    public void setTenThanhPhan(String tenThanhPhan) { this.tenThanhPhan = tenThanhPhan; }

    public double getSoTien() { return soTien; }
    public void setSoTien(double soTien) { this.soTien = soTien; }

    public String getNguon() { return nguon; }
    public void setNguon(String nguon) { this.nguon = nguon; }

    @Override
    public String toString() {
        return loai.getDisplayName() + ": " + tenThanhPhan + " = " + soTien;
    }
}
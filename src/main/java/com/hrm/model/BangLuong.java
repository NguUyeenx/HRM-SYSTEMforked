package com.hrm.model;

import java.time.LocalDateTime;

/**
 * Khớp với bảng BANGLUONG trong DB:
 *   maBangLuong, thang, nam, tenBangLuong, trangThai ENUM('dang_xu_ly','da_khoa')
 */
public class BangLuong {

    public enum TrangThai {
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
            for (TrangThai t : values()) if (t.dbValue.equals(value)) return t;
            return DANG_XU_LY;
        }
    }

    private int maBangLuong;   // PK — đúng tên DB
    private int thang;
    private int nam;
    private String tenBangLuong;
    private TrangThai trangThai;
    private LocalDateTime ngayTao;

    public BangLuong() {
        this.trangThai = TrangThai.DANG_XU_LY;
        this.ngayTao = LocalDateTime.now();
    }

    public BangLuong(int thang, int nam) {
        this();
        this.thang = thang;
        this.nam = nam;
        this.tenBangLuong = "Bang luong thang " + thang + "/" + nam;
    }

    public int getMaBangLuong() { return maBangLuong; }
    public void setMaBangLuong(int maBangLuong) { this.maBangLuong = maBangLuong; }

    /** Alias getMaBL() để không phải sửa Service/UI đang dùng tên cũ */
    public int getMaBL() { return maBangLuong; }
    public void setMaBL(int maBL) { this.maBangLuong = maBL; }

    public int getThang() { return thang; }
    public void setThang(int thang) { this.thang = thang; }

    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }

    public String getTenBangLuong() { return tenBangLuong; }
    public void setTenBangLuong(String tenBangLuong) { this.tenBangLuong = tenBangLuong; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    @Override
    public String toString() {
        return tenBangLuong != null ? tenBangLuong : "Tháng " + thang + "/" + nam;
    }
}
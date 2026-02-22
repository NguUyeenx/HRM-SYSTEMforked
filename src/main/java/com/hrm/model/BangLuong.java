package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model đại diện cho bảng BANGLUONG.
 *
 * Mỗi bản ghi đại diện cho 1 kỳ lương (thường là 1 tháng).
 * Liên kết: BANGLUONG → N ChiTietLuong (mỗi NV 1 chi tiết)
 */
public class BangLuong {

    public enum TrangThai {
        NHAP("nhap", "Nháp"),
        DA_TINH("da_tinh", "Đã tính"),
        DA_DUYET("da_duyet", "Đã duyệt"),
        DA_CHI("da_chi", "Đã chi");

        private final String dbValue;
        private final String displayName;

        TrangThai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }
    }

    private int maBL;
    private int maNV;
    private LocalDate ngayBD;
    private LocalDate ngayKT;
    private TrangThai trangThai;
    private LocalDateTime ngayTao;

    public BangLuong() {
        this.trangThai = TrangThai.NHAP;
        this.ngayTao = LocalDateTime.now();
    }

    public BangLuong(int maNV, LocalDate ngayBD, LocalDate ngayKT) {
        this();
        this.maNV = maNV;
        this.ngayBD = ngayBD;
        this.ngayKT = ngayKT;
    }

    // Getters & Setters
    public int getMaBL() { return maBL; }
    public void setMaBL(int maBL) { this.maBL = maBL; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public LocalDate getNgayBD() { return ngayBD; }
    public void setNgayBD(LocalDate ngayBD) { this.ngayBD = ngayBD; }

    public LocalDate getNgayKT() { return ngayKT; }
    public void setNgayKT(LocalDate ngayKT) { this.ngayKT = ngayKT; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    @Override
    public String toString() {
        return "BangLuong{maBL=" + maBL + ", " + ngayBD + " - " + ngayKT + ", " + trangThai.getDisplayName() + "}";
    }
}
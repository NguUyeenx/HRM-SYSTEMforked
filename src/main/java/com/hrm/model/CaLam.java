package com.hrm.model;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Model đại diện cho bảng CALAM (Ca làm việc).
 *
 * Dữ liệu mẫu trong DB:
 *   HANH_CHINH  → 08:00 - 17:00
 *   CA_SANG     → 06:00 - 14:00
 *   CA_CHIEU    → 14:00 - 22:00
 *   CA_DEM      → 22:00 - 06:00
 */
public class CaLam {

    // ── Trạng thái ca làm — ánh xạ ENUM DB ('hoat_dong' / 'ngung_hoat_dong') ──
    public enum TrangThai {
        HOAT_DONG("hoat_dong", "Hoạt động"),
        NGUNG_HOAT_DONG("ngung_hoat_dong", "Ngừng hoạt động");

        private final String dbValue;
        private final String displayName;

        TrangThai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        /** Chuyển chuỗi DB → enum; ném IllegalArgumentException nếu không hợp lệ. */
        public static TrangThai fromDbValue(String value) {
            for (TrangThai tt : values()) {
                if (tt.dbValue.equals(value)) {
                    return tt;
                }
            }
            throw new IllegalArgumentException("Trạng thái ca làm không hợp lệ: " + value);
        }
    }

    // ── Fields — mapping 1:1 với bảng CALAM ──
    private String maCaLam;
    private String tenCaLam;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private double soGioChuan;
    private boolean choPhepLamThem;
    private String moTa;
    private TrangThai trangThai;
    private LocalDateTime ngayTao;

    /** Constructor mặc định — thiết lập giá trị default giống DB. */
    public CaLam() {
        this.soGioChuan = 8.00;
        this.choPhepLamThem = true;
        this.trangThai = TrangThai.HOAT_DONG;
        this.ngayTao = LocalDateTime.now();
    }

    /** Constructor với các field bắt buộc (NOT NULL trong DB). */
    public CaLam(String maCaLam, String tenCaLam, LocalTime gioBatDau, LocalTime gioKetThuc) {
        this();
        this.maCaLam = maCaLam;
        this.tenCaLam = tenCaLam;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    // ── Getters & Setters ──

    public String getMaCaLam() { return maCaLam; }
    public void setMaCaLam(String maCaLam) { this.maCaLam = maCaLam; }

    public String getTenCaLam() { return tenCaLam; }
    public void setTenCaLam(String tenCaLam) { this.tenCaLam = tenCaLam; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }

    public LocalTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public double getSoGioChuan() { return soGioChuan; }
    public void setSoGioChuan(double soGioChuan) { this.soGioChuan = soGioChuan; }

    public boolean isChoPhepLamThem() { return choPhepLamThem; }
    public void setChoPhepLamThem(boolean choPhepLamThem) { this.choPhepLamThem = choPhepLamThem; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    // ── Helper methods ──

    /** Ca đêm khi giờ kết thúc nhỏ hơn giờ bắt đầu (vd: 22:00 → 06:00). */
    public boolean laCaDem() {
        return gioKetThuc.isBefore(gioBatDau);
    }

    /** Kiểm tra ca còn hoạt động. */
    public boolean conHoatDong() {
        return trangThai == TrangThai.HOAT_DONG;
    }

    @Override
    public String toString() {
        return tenCaLam + " (" + gioBatDau + " - " + gioKetThuc + ")";
    }
}
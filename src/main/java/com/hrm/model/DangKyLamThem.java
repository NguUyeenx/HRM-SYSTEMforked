package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model đại diện cho bảng DANGKYLAMTHEM (Đăng ký làm thêm giờ / OT).
 *
 * LIÊN KẾT VỚI BẢNG LƯƠNG:
 * - Khi đơn OT được duyệt → tính lương OT = soGio × heSoOT × lương/giờ
 * - Hệ số OT mặc định: 1.5 (ngày thường)
 */
public class DangKyLamThem {

    public enum TrangThai {
        CHO_DUYET("cho_duyet", "Chờ duyệt"),
        DA_DUYET("da_duyet", "Đã duyệt"),
        TU_CHOI("tu_choi", "Từ chối");

        private final String dbValue;
        private final String displayName;

        TrangThai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        public static TrangThai fromDbValue(String value) {
            for (TrangThai tt : values()) {
                if (tt.dbValue.equals(value)) return tt;
            }
            throw new IllegalArgumentException("Trạng thái OT không hợp lệ: " + value);
        }
    }

    private int maDK;
    private int maNV;
    private String employeeName;
    private LocalDate ngay;
    private double soGio;
    private String lyDo;
    private double heSoOT;
    private String nhanXet;           // Nhận xét của người duyệt

    private Integer nguoiDuyet;
    private String approverName;
    private LocalDateTime ngayDuyet;
    private TrangThai trangThai;
    private LocalDateTime ngayTao;

    public DangKyLamThem() {
        this.trangThai = TrangThai.CHO_DUYET;
        this.heSoOT = 1.5;
        this.ngayTao = LocalDateTime.now();
    }

    public DangKyLamThem(int maNV, LocalDate ngay, double soGio, String lyDo) {
        this();
        this.maNV = maNV;
        this.ngay = ngay;
        this.soGio = soGio;
        this.lyDo = lyDo;
    }

    // Getters & Setters
    public int getMaDK() { return maDK; }
    public void setMaDK(int maDK) { this.maDK = maDK; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }

    public double getSoGio() { return soGio; }
    public void setSoGio(double soGio) { this.soGio = soGio; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public double getHeSoOT() { return heSoOT; }
    public void setHeSoOT(double heSoOT) { this.heSoOT = heSoOT; }

    public String getNhanXet() { return nhanXet; }
    public void setNhanXet(String nhanXet) { this.nhanXet = nhanXet; }

    public Integer getNguoiDuyet() { return nguoiDuyet; }
    public void setNguoiDuyet(Integer nguoiDuyet) { this.nguoiDuyet = nguoiDuyet; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public LocalDateTime getNgayDuyet() { return ngayDuyet; }
    public void setNgayDuyet(LocalDateTime ngayDuyet) { this.ngayDuyet = ngayDuyet; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    // Helper
    public boolean dangChoDuyet() { return trangThai == TrangThai.CHO_DUYET; }
    public boolean daDuocDuyet() { return trangThai == TrangThai.DA_DUYET; }

    public double tinhTienOT(double luongMotGio) {
        return soGio * heSoOT * luongMotGio;
    }

    public void duyet(int maNguoiDuyet) {
        this.trangThai = TrangThai.DA_DUYET;
        this.nguoiDuyet = maNguoiDuyet;
        this.ngayDuyet = LocalDateTime.now();
    }

    public void tuChoi(int maNguoiDuyet) {
        this.trangThai = TrangThai.TU_CHOI;
        this.nguoiDuyet = maNguoiDuyet;
        this.ngayDuyet = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "DangKyLamThem{maNV=" + maNV + ", soGio=" + soGio + ", heSo=" + heSoOT + "}";
    }
}
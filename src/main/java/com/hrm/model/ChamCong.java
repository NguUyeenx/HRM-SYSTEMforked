package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Model đại diện cho bảng CHAMCONG (Chấm công hàng ngày).
 * ...
 * THAY ĐỔI: Thêm helper isLaOT() / setLaOT() dùng field ghiChu có sẵn.
 * Không thêm cột DB mới — ghiChu = "OT" khi ca này là OT.
 */
public class ChamCong {

    public enum TrangThai {
        DUNG_GIO("dung_gio", "Đúng giờ"),
        DI_MUON("di_muon", "Đi muộn"),
        VE_SOM("ve_som", "Về sớm"),
        VANG_MAT("vang_mat", "Vắng mặt"),
        NGHI_PHEP("nghi_phep", "Nghỉ phép"),
        CONG_TAC("cong_tac", "Công tác");

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
            throw new IllegalArgumentException("Trang thai cham cong khong hop le: " + value);
        }
    }

    public enum PhuongThuc {
        WIFI("wifi", "WiFi"),
        VAN_TAY("van_tay", "Vân tay"),
        THE_TU("the_tu", "Thẻ từ"),
        GPS("gps", "GPS"),
        THU_CONG("thu_cong", "Thủ công");

        private final String dbValue;
        private final String displayName;

        PhuongThuc(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        public static PhuongThuc fromDbValue(String value) {
            for (PhuongThuc pt : values()) {
                if (pt.dbValue.equals(value)) return pt;
            }
            throw new IllegalArgumentException("Phuong thuc cham cong khong hop le: " + value);
        }
    }

    // ── Hằng số đánh dấu ca OT trong cột ghiChu ──
    private static final String OT_FLAG = "OT";

    private int maChamCong;
    private int maNV;
    private String employeeName;    // TRANSIENT
    private LocalDate ngay;
    private String maCaLam;
    private String tenCaLam;        // TRANSIENT
    private LocalDateTime gioVao;
    private LocalDateTime gioRa;
    private double soGioLam;
    private double gioLamThem;
    private TrangThai trangThai;
    private PhuongThuc phuongThucChamCong;
    private String ghiChu;
    private LocalDateTime ngayTao;

    public ChamCong() {
        this.soGioLam = 0;
        this.gioLamThem = 0;
        this.trangThai = TrangThai.DUNG_GIO;
        this.phuongThucChamCong = PhuongThuc.THU_CONG;
        this.ngayTao = LocalDateTime.now();
    }

    public ChamCong(int maNV, LocalDate ngay, String maCaLam) {
        this();
        this.maNV = maNV;
        this.ngay = ngay;
        this.maCaLam = maCaLam;
        this.gioVao = LocalDateTime.now();
    }

    // ====================================================
    // GETTERS & SETTERS
    // ====================================================

    public int getMaChamCong() { return maChamCong; }
    public void setMaChamCong(int maChamCong) { this.maChamCong = maChamCong; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }

    public String getMaCaLam() { return maCaLam; }
    public void setMaCaLam(String maCaLam) { this.maCaLam = maCaLam; }

    public String getTenCaLam() { return tenCaLam; }
    public void setTenCaLam(String tenCaLam) { this.tenCaLam = tenCaLam; }

    public LocalDateTime getGioVao() { return gioVao; }
    public void setGioVao(LocalDateTime gioVao) { this.gioVao = gioVao; }

    public LocalDateTime getGioRa() { return gioRa; }
    public void setGioRa(LocalDateTime gioRa) { this.gioRa = gioRa; }

    public double getSoGioLam() { return soGioLam; }
    public void setSoGioLam(double soGioLam) { this.soGioLam = soGioLam; }

    public double getGioLamThem() { return gioLamThem; }
    public void setGioLamThem(double gioLamThem) { this.gioLamThem = gioLamThem; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public PhuongThuc getPhuongThucChamCong() { return phuongThucChamCong; }
    public void setPhuongThucChamCong(PhuongThuc phuongThucChamCong) {
        this.phuongThucChamCong = phuongThucChamCong;
    }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    // ====================================================
    // HELPERS — OT FLAG (dùng ghiChu để lưu, không thêm cột DB)
    // ====================================================

    /**
     * Kiểm tra ca chấm công này có phải ca OT không.
     * Dựa vào ghiChu == "OT" (được lưu vào DB qua cột ghiChu hiện có).
     */
    public boolean isLaOT() {
        return OT_FLAG.equals(ghiChu);
    }

    /**
     * Đánh dấu/bỏ đánh dấu ca OT.
     * @param laOT true = ca OT, false = ca thường
     */
    public void setLaOT(boolean laOT) {
        this.ghiChu = laOT ? OT_FLAG : null;
    }

    // ====================================================
    // BUSINESS METHODS
    // ====================================================

    public double tinhSoGioLam() {
        if (gioVao == null || gioRa == null) return 0;
        return Duration.between(gioVao, gioRa).toMinutes() / 60.0;
    }

    public boolean daCheckOut() { return gioRa != null; }

    public boolean hoanTat() { return gioVao != null && gioRa != null && soGioLam > 0; }

    @Override
    public String toString() {
        return "ChamCong{maNV=" + maNV + ", ngay=" + ngay
            + ", trangThai=" + (trangThai != null ? trangThai.getDisplayName() : "N/A")
            + ", soGioLam=" + soGioLam
            + (isLaOT() ? ", [OT]" : "")
            + '}';
    }
}
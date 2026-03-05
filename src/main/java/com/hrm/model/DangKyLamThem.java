package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Model đại diện cho bảng DANGKYLAMTHEM (đơn đăng ký làm thêm giờ / OT).
 * <p>
 * Khi đơn OT được duyệt, lương OT = soGio × heSoOT × lương/giờ.
 * Hệ số OT mặc định là 1.5 (ngày thường).
 * <p>
 * Nhân viên đăng ký theo khoảng giờ (gioVaoOT / gioRaOT);
 * soGio được tính tự động từ hai field này.
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
    private String nhanXet;

    // Khoảng giờ OT do nhân viên đăng ký; soGio được tính tự động từ 2 field này.
    private LocalTime gioVaoOT;   // Giờ bắt đầu OT (vd: 17:00)
    private LocalTime gioRaOT;    // Giờ kết thúc OT  (vd: 20:00)

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

    /** Constructor cũ — backward compatible */
    public DangKyLamThem(int maNV, LocalDate ngay, double soGio, String lyDo) {
        this();
        this.maNV = maNV;
        this.ngay = ngay;
        this.soGio = soGio;
        this.lyDo = lyDo;
    }

    /**
     * Constructor mới — nhận khoảng giờ, tự tính soGio.
     * @param maNV     Mã nhân viên
     * @param ngay     Ngày OT
     * @param gioVao   Giờ bắt đầu OT (vd: LocalTime.of(17, 0))
     * @param gioRa    Giờ kết thúc OT (vd: LocalTime.of(20, 0))
     * @param lyDo     Lý do OT
     */
    public DangKyLamThem(int maNV, LocalDate ngay, LocalTime gioVao, LocalTime gioRa, String lyDo) {
        this();
        this.maNV = maNV;
        this.ngay = ngay;
        this.gioVaoOT = gioVao;
        this.gioRaOT  = gioRa;
        this.lyDo = lyDo;
        // Tự động tính số giờ OT
        this.soGio = tinhSoGioOT(gioVao, gioRa);
    }

    /**
     * Tính số giờ OT từ khoảng thời gian.
     * Hỗ trợ ca qua đêm (vd: 22:00 → 01:00 hôm sau).
     */
    public static double tinhSoGioOT(LocalTime vao, LocalTime ra) {
        if (vao == null || ra == null) return 0;
        long minutes = java.time.Duration.between(vao, ra).toMinutes();
        // Nếu ra < vào → qua nửa đêm
        if (minutes < 0) minutes += 24 * 60;
        return minutes / 60.0;
    }

    // ── Getters & Setters ──
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

    /** Giờ bắt đầu OT (hiển thị trên form). */
    public LocalTime getGioVaoOT() { return gioVaoOT; }
    public void setGioVaoOT(LocalTime gioVaoOT) {
        this.gioVaoOT = gioVaoOT;
        if (this.gioRaOT != null) this.soGio = tinhSoGioOT(gioVaoOT, this.gioRaOT);
    }

    /** Giờ kết thúc OT (hiển thị trên form). */
    public LocalTime getGioRaOT() { return gioRaOT; }
    public void setGioRaOT(LocalTime gioRaOT) {
        this.gioRaOT = gioRaOT;
        if (this.gioVaoOT != null) this.soGio = tinhSoGioOT(this.gioVaoOT, gioRaOT);
    }

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

    // ── Helpers ──
    public boolean dangChoDuyet() { return trangThai == TrangThai.CHO_DUYET; }
    public boolean daDuocDuyet()  { return trangThai == TrangThai.DA_DUYET; }

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
        String range = (gioVaoOT != null && gioRaOT != null)
            ? gioVaoOT + "→" + gioRaOT
            : soGio + "h";
        return "DangKyLamThem{maNV=" + maNV + ", gio=" + range + ", heSo=" + heSoOT + "}";
    }
}
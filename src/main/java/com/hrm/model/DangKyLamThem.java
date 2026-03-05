package com.hrm.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Model đại diện cho bảng DANGKYLAMTHEM (Đăng ký làm thêm giờ / OT).
 *
 * LIÊN KẾT VỚI BẢNG LƯƠNG:
 * - Khi đơn OT được duyệt → tính lương OT = soGio × heSoOT × lương/giờ
 * - Hệ số OT mặc định: 1.5 (ngày thường)
 *
 * THAY ĐỔI (YC2):
 * - Thêm gioBatDauOT, gioKetThucOT để nhân viên điền khoảng giờ thay vì số giờ.
 * - soGio được tính tự động từ khoảng giờ qua constructor mới.
 * - Hỗ trợ ca qua đêm (VD: 22:00 → 02:00).
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

    // ── MỚI (YC2): Lưu khoảng giờ OT — chỉ ở tầng Java, không cần thêm cột DB ──
    private LocalTime gioBatDauOT;    // VD: 07:00
    private LocalTime gioKetThucOT;   // VD: 10:00

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

    /** Constructor cũ — giữ nguyên để backward compatible. */
    public DangKyLamThem(int maNV, LocalDate ngay, double soGio, String lyDo) {
        this();
        this.maNV = maNV;
        this.ngay = ngay;
        this.soGio = soGio;
        this.lyDo = lyDo;
    }

    /**
     * Constructor mới (YC2) — nhân viên điền từ giờ nào đến giờ nào.
     * soGio được tính tự động, hỗ trợ ca qua đêm.
     *
     * @param maNV         Mã nhân viên
     * @param ngay         Ngày OT
     * @param gioBatDauOT  Giờ bắt đầu (VD: LocalTime.of(7, 0))
     * @param gioKetThucOT Giờ kết thúc (VD: LocalTime.of(10, 0))
     * @param lyDo         Lý do OT
     */
    public DangKyLamThem(int maNV, LocalDate ngay, LocalTime gioBatDauOT, LocalTime gioKetThucOT, String lyDo) {
        this();
        this.maNV = maNV;
        this.ngay = ngay;
        this.gioBatDauOT = gioBatDauOT;
        this.gioKetThucOT = gioKetThucOT;
        this.lyDo = lyDo;
        this.soGio = tinhSoGioOT(gioBatDauOT, gioKetThucOT);
    }

    // ====================================================
    // GETTERS & SETTERS
    // ====================================================

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

    /** Getter giờ bắt đầu OT (null nếu tạo bằng constructor cũ). */
    public LocalTime getGioBatDauOT() { return gioBatDauOT; }
    public void setGioBatDauOT(LocalTime gioBatDauOT) {
        this.gioBatDauOT = gioBatDauOT;
        if (this.gioKetThucOT != null) this.soGio = tinhSoGioOT(gioBatDauOT, this.gioKetThucOT);
    }

    /** Getter giờ kết thúc OT (null nếu tạo bằng constructor cũ). */
    public LocalTime getGioKetThucOT() { return gioKetThucOT; }
    public void setGioKetThucOT(LocalTime gioKetThucOT) {
        this.gioKetThucOT = gioKetThucOT;
        if (this.gioBatDauOT != null) this.soGio = tinhSoGioOT(this.gioBatDauOT, gioKetThucOT);
    }

    // ====================================================
    // HELPERS
    // ====================================================

    public boolean dangChoDuyet() { return trangThai == TrangThai.CHO_DUYET; }
    public boolean daDuocDuyet()  { return trangThai == TrangThai.DA_DUYET; }

    public double tinhTienOT(double luongMotGio) {
        return soGio * heSoOT * luongMotGio;
    }

    /**
     * Tính số giờ OT từ khoảng thời gian. Hỗ trợ ca qua đêm.
     * VD: 22:00 → 02:00 = 4.0 giờ
     */
    private double tinhSoGioOT(LocalTime batDau, LocalTime ketThuc) {
        long phut;
        if (!ketThuc.isAfter(batDau)) {
            // Ca qua đêm
            phut = Duration.between(batDau, LocalTime.MIDNIGHT).toMinutes()
                 + Duration.between(LocalTime.MIDNIGHT, ketThuc).toMinutes();
        } else {
            phut = Duration.between(batDau, ketThuc).toMinutes();
        }
        // Làm tròn 1 chữ số thập phân
        return Math.round(phut / 60.0 * 10.0) / 10.0;
    }

    /**
     * Trả về chuỗi hiển thị khoảng giờ OT cho table.
     * VD: "07:00 - 10:00 (3.0h)" hoặc "3.0h" nếu không có giờ cụ thể.
     */
    public String getKhoangGioDisplay() {
        if (gioBatDauOT != null && gioKetThucOT != null) {
            return gioBatDauOT + " - " + gioKetThucOT
                + " (" + String.format("%.1f", soGio) + "h)";
        }
        return String.format("%.1f", soGio) + "h";
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
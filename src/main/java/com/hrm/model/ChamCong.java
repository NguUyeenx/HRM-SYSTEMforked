package com.hrm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Model đại diện cho bảng CHAMCONG (Chấm công hàng ngày).
 *
 * Mỗi bản ghi lưu lại:
 * - Nhân viên nào chấm công
 * - Ngày nào
 * - Giờ vào / giờ ra
 * - Số giờ làm thực tế / giờ làm thêm
 * - Trạng thái (đúng giờ, đi muộn, về sớm...)
 * - Phương thức chấm công (wifi, vân tay, thủ công...)
 *
 * RÀNG BUỘC QUAN TRỌNG:
 * Mỗi nhân viên chỉ có DUY NHẤT 1 bản ghi chấm công mỗi ngày.
 * (UNIQUE KEY uk_nv_ngay (maNV, ngay) trong DB)
 */
public class ChamCong {

    // ====================================================
    // ENUM: Trạng thái chấm công
    // ====================================================
    //
    // ENUM NÀY RẤT QUAN TRỌNG vì nó quyết định:
    // - Bảng lương tính thế nào (đi muộn bị trừ? vắng mặt không tính công?)
    // - Màu sắc hiển thị trên giao diện (xanh = đúng giờ, đỏ = vắng...)
    // - Báo cáo thống kê chấm công
    //
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
                if (tt.dbValue.equals(value)) {
                    return tt;
                }
            }
            throw new IllegalArgumentException("Trạng thái chấm công không hợp lệ: " + value);
        }
    }

    // ====================================================
    // ENUM: Phương thức chấm công
    // ====================================================
    //
    // Hệ thống hỗ trợ nhiều cách chấm công:
    // - wifi: Kiểm tra NV đang kết nối WiFi công ty
    // - van_tay: Máy quét vân tay
    // - the_tu: Quẹt thẻ nhân viên
    // - gps: Định vị (cho NV làm việc ngoài văn phòng)
    // - thu_cong: Admin/HR nhập tay
    //
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
                if (pt.dbValue.equals(value)) {
                    return pt;
                }
            }
            throw new IllegalArgumentException("Phương thức chấm công không hợp lệ: " + value);
        }
    }

    // ====================================================
    // FIELDS — Mapping với bảng CHAMCONG
    // ====================================================
    //
    // CHÚ Ý VỀ KIỂU DỮ LIỆU:
    //
    // 1. maNV dùng 'int' (primitive) vì nó là NOT NULL, luôn có giá trị
    // 2. maCaLam dùng 'String' vì nó có thể NULL (NV chưa được gán ca)
    // 3. gioVao/gioRa dùng 'LocalDateTime' thay vì 'LocalTime' vì:
    //    - DB lưu DATETIME (bao gồm cả ngày)
    //    - Ca đêm có thể qua ngày (vào 22:00 ngày 1, ra 06:00 ngày 2)
    //    - LocalDateTime giúp tính toán chênh lệch chính xác
    //
    // 4. employeeName KHÔNG CÓ trong DB (không phải cột trong bảng CHAMCONG)
    //    → Đây là field "phụ trợ" để hiển thị trên giao diện
    //    → Được lấy từ bảng THONGTINCANHAN khi JOIN query
    //    → Pattern này gọi là "Presentation field" hoặc "Transient field"
    //

    private int maChamCong;             // PRIMARY KEY — AUTO_INCREMENT
    private int maNV;                   // FK → NHANVIEN — NOT NULL
    private String employeeName;        // TRANSIENT — Không lưu DB, chỉ để hiển thị
    private LocalDate ngay;             // NOT NULL — Ngày chấm công
    private String maCaLam;             // FK → CALAM — có thể NULL
    private String tenCaLam;            // TRANSIENT — Tên ca để hiển thị
    private LocalDateTime gioVao;       // Thời điểm check-in
    private LocalDateTime gioRa;        // Thời điểm check-out
    private double soGioLam;            // Số giờ làm thực tế — DECIMAL(4,2)
    private double gioLamThem;          // Giờ OT — DECIMAL(4,2)
    private TrangThai trangThai;        // Trạng thái chấm công
    private PhuongThuc phuongThucChamCong; // Cách chấm công
    private String ghiChu;              // Ghi chú — có thể NULL
    private LocalDateTime ngayTao;      // Thời điểm tạo bản ghi

    // ====================================================
    // CONSTRUCTORS
    // ====================================================

    /** Constructor mặc định */
    public ChamCong() {
        this.soGioLam = 0;
        this.gioLamThem = 0;
        this.trangThai = TrangThai.DUNG_GIO;
        this.phuongThucChamCong = PhuongThuc.THU_CONG;
        this.ngayTao = LocalDateTime.now();
    }

    /**
     * Constructor cho check-in mới.
     * Khi nhân viên bấm "Check-in", chỉ cần biết:
     * - Ai check-in (maNV)
     * - Ngày nào (ngay)
     * - Ca nào (maCaLam)
     */
    public ChamCong(int maNV, LocalDate ngay, String maCaLam) {
        this();
        this.maNV = maNV;
        this.ngay = ngay;
        this.maCaLam = maCaLam;
        this.gioVao = LocalDateTime.now(); // Tự động ghi thời điểm check-in
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
    // HELPER METHODS
    // ====================================================

    /**
     * Tính số giờ làm việc từ giờ vào và giờ ra.
     *
     * TẠI SAO CẦN METHOD NÀY?
     * - Khi NV check-out, hệ thống tự động tính: gioRa - gioVao
     * - Kết quả làm tròn 2 chữ số thập phân (vd: 7.50 giờ, 8.25 giờ)
     *
     * LƯU Ý:
     * - Duration.between() trả về Duration (khoảng thời gian)
     * - toMinutes() chuyển sang phút → chia 60.0 để ra giờ
     * - Trả về 0 nếu chưa check-in hoặc chưa check-out
     *
     * @return Số giờ làm việc, làm tròn 2 chữ số thập phân
     */
    public double tinhSoGioLam() {
        if (gioVao == null || gioRa == null) {
            return 0;
        }
        long phut = Duration.between(gioVao, gioRa).toMinutes();
        // Làm tròn 2 chữ số: nhân 100, round, chia 100
        return Math.round(phut / 60.0 * 100.0) / 100.0;
    }

    /**
     * Kiểm tra nhân viên đã check-in chưa.
     * Dùng để hiển thị nút "Check-in" hay "Check-out" trên giao diện.
     */
    public boolean daCheckIn() {
        return gioVao != null;
    }

    /**
     * Kiểm tra nhân viên đã check-out chưa.
     * Nếu đã check-out → bản ghi hoàn chỉnh, không cho sửa.
     */
    public boolean daCheckOut() {
        return gioRa != null;
    }

    /**
     * Kiểm tra bản ghi đã hoàn tất chưa (cả check-in và check-out).
     * Bản ghi hoàn tất mới được tính vào bảng lương.
     */
    public boolean hoanTat() {
        return daCheckIn() && daCheckOut();
    }

    @Override
    public String toString() {
        return "ChamCong{" +
                "maNV=" + maNV +
                ", ngay=" + ngay +
                ", trangThai=" + (trangThai != null ? trangThai.getDisplayName() : "N/A") +
                ", soGioLam=" + soGioLam +
                '}';
    }
}
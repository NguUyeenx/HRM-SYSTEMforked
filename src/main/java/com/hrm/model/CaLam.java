package com.hrm.model;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Model đại diện cho bảng CALAM (Ca làm việc).
 *
 * Mỗi ca làm việc định nghĩa:
 * - Thời gian bắt đầu / kết thúc
 * - Số giờ chuẩn (thường là 8 giờ)
 * - Có cho phép làm thêm giờ hay không
 *
 * Dữ liệu mẫu trong DB:
 *   HANH_CHINH  → 08:00 - 17:00
 *   CA_SANG     → 06:00 - 14:00
 *   CA_CHIEU    → 14:00 - 22:00
 *   CA_DEM      → 22:00 - 06:00
 */
public class CaLam {

    // ====================================================
    // ENUM: Trạng thái ca làm
    // ====================================================
    //
    // TẠI SAO DÙNG ENUM?
    // - Trong DB, cột trangThai là ENUM('hoat_dong', 'ngung_hoat_dong')
    // - Nếu dùng String thuần, bạn có thể gõ nhầm "hoat_dnog" mà compiler không phát hiện
    // - Enum giúp giới hạn giá trị hợp lệ → an toàn hơn, IDE hỗ trợ auto-complete
    //
    public enum TrangThai {
        HOAT_DONG("hoat_dong", "Hoạt động"),
        NGUNG_HOAT_DONG("ngung_hoat_dong", "Ngừng hoạt động");

        private final String dbValue;       // Giá trị lưu trong DB
        private final String displayName;   // Giá trị hiển thị trên giao diện

        TrangThai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }

        /**
         * Chuyển đổi từ giá trị DB sang enum.
         * Ví dụ: fromDbValue("hoat_dong") → TrangThai.HOAT_DONG
         *
         * Tại sao cần method này?
         * Khi đọc dữ liệu từ ResultSet (JDBC), bạn nhận được String "hoat_dong".
         * Method này giúp convert ngược lại thành enum an toàn.
         */
        public static TrangThai fromDbValue(String value) {
            for (TrangThai tt : values()) {
                if (tt.dbValue.equals(value)) {
                    return tt;
                }
            }
            // Nếu không tìm thấy, ném lỗi rõ ràng thay vì trả về null
            throw new IllegalArgumentException("Trạng thái ca làm không hợp lệ: " + value);
        }
    }

    // ====================================================
    // CÁC FIELD — Mapping 1:1 với các cột trong bảng CALAM
    // ====================================================
    //
    // NGUYÊN TẮC ĐẶT TÊN:
    // - DB dùng camelCase tiếng Việt (maCaLam, tenCaLam...)
    // - Java model cũng giữ nguyên tên để mapping dễ dàng
    // - Kiểu dữ liệu Java phải tương thích với kiểu trong DB:
    //     VARCHAR  → String
    //     TIME     → LocalTime  (Java 8+, thay thế java.sql.Time cũ)
    //     DECIMAL  → double     (hoặc BigDecimal nếu cần chính xác tiền tệ)
    //     BOOLEAN  → boolean
    //     DATETIME → LocalDateTime
    //

    private String maCaLam;          // PRIMARY KEY — VARCHAR(20)
    private String tenCaLam;         // NOT NULL    — NVARCHAR(100)
    private LocalTime gioBatDau;     // NOT NULL    — TIME
    private LocalTime gioKetThuc;    // NOT NULL    — TIME
    private double soGioChuan;       // DEFAULT 8.00 — DECIMAL(4,2)
    private boolean choPhepLamThem;  // DEFAULT TRUE — BOOLEAN
    private String moTa;             // NULL OK     — NVARCHAR(255)
    private TrangThai trangThai;     // DEFAULT 'hoat_dong' — ENUM
    private LocalDateTime ngayTao;   // AUTO        — DATETIME

    // ====================================================
    // CONSTRUCTORS
    // ====================================================
    //
    // TẠI SAO CẦN NHIỀU CONSTRUCTOR?
    //
    // 1. Constructor không tham số (no-arg):
    //    - Cần thiết cho framework/library (JDBC mapping, serialization)
    //    - Thiết lập giá trị mặc định giống DB
    //
    // 2. Constructor đầy đủ:
    //    - Dùng khi tạo object mới từ form nhập liệu
    //    - Đảm bảo các field bắt buộc (NOT NULL) được cung cấp
    //

    /** Constructor mặc định — thiết lập giá trị default giống DB */
    public CaLam() {
        this.soGioChuan = 8.00;
        this.choPhepLamThem = true;
        this.trangThai = TrangThai.HOAT_DONG;
        this.ngayTao = LocalDateTime.now();
    }

    /**
     * Constructor với các field bắt buộc.
     * Chỉ yêu cầu những field NOT NULL trong DB.
     */
    public CaLam(String maCaLam, String tenCaLam, LocalTime gioBatDau, LocalTime gioKetThuc) {
        this(); // Gọi constructor mặc định trước để set default values
        this.maCaLam = maCaLam;
        this.tenCaLam = tenCaLam;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    // ====================================================
    // GETTERS & SETTERS
    // ====================================================
    //
    // ĐÂY LÀ QUY TẮC ENCAPSULATION (đóng gói) trong OOP:
    // - Field luôn là private → bên ngoài không truy cập trực tiếp
    // - Getter/Setter là public → kiểm soát cách đọc/ghi dữ liệu
    //
    // Lợi ích:
    // - Có thể thêm validation trong setter (ví dụ: không cho soGioChuan < 0)
    // - Có thể thêm logic khi đọc (ví dụ: format lại tên trước khi trả về)
    // - Thay đổi nội bộ mà không ảnh hưởng code bên ngoài
    //

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

    // ====================================================
    // HELPER METHODS — Logic liên quan đến ca làm
    // ====================================================
    //
    // NGUYÊN TẮC: Model không chỉ chứa data, mà còn có thể chứa
    // logic tính toán ĐƠN GIẢN liên quan đến chính nó.
    // (Logic phức tạp thì để ở Service layer)
    //

    /**
     * Kiểm tra xem ca làm có phải ca đêm không.
     * Ca đêm: giờ kết thúc nhỏ hơn giờ bắt đầu (qua ngày).
     * Ví dụ: 22:00 → 06:00
     */
    public boolean laCaDem() {
        return gioKetThuc.isBefore(gioBatDau);
    }

    /**
     * Kiểm tra ca làm còn hoạt động không.
     * Dùng khi cần filter danh sách ca chỉ hiển thị ca đang dùng.
     */
    public boolean conHoatDong() {
        return trangThai == TrangThai.HOAT_DONG;
    }

    // ====================================================
    // toString — Dùng cho debug và hiển thị trong ComboBox
    // ====================================================
    @Override
    public String toString() {
        return tenCaLam + " (" + gioBatDau + " - " + gioKetThuc + ")";
    }
}
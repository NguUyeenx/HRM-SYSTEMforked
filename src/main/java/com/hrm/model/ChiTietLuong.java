package com.hrm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model đại diện cho bảng CHITIETLUONG.
 *
 * Mỗi bản ghi = lương của 1 NV trong 1 kỳ lương.
 * Liên kết:
 *   BANGLUONG (1) → (N) CHITIETLUONG
 *   CHITIETLUONG (1) → (N) THANHPHANLUONG
 *
 * CÔNG THỨC:
 *   tongLuong     = luongCoBan + tongLuongChucVu + tienOT
 *   luongThucNhan = tongLuong - tongKhauTru
 */
public class ChiTietLuong {

    public enum TrangThai {
        CHUA_TINH("chua_tinh", "Chưa tính"),
        DA_TINH("da_tinh", "Đã tính"),
        DA_DUYET("da_duyet", "Đã duyệt");

        private final String dbValue;
        private final String displayName;

        TrangThai(String dbValue, String displayName) {
            this.dbValue = dbValue;
            this.displayName = displayName;
        }

        public String getDbValue() { return dbValue; }
        public String getDisplayName() { return displayName; }
    }

    private int maChiTietLuong;
    private int maBL;            // FK → BANGLUONG
    private int maNV;            // FK → NHANVIEN
    private String tenNV;        // Transient — để hiển thị

    // Các khoản lương
    private double luongCoBan;
    private double tongLuongChucVu;   // Phụ cấp chức vụ
    private double tienOT;            // Tiền OT (tính từ DangKyLamThem đã duyệt)
    private double tongKhauTru;       // BHXH, BHYT, thuế...
    private double tongLuong;         // = luongCoBan + tongLuongChucVu + tienOT
    private double luongThucNhan;     // = tongLuong - tongKhauTru

    // Thông tin chấm công
    private int soNgayCong;
    private double tongGioLam;
    private double tongGioOT;

    private TrangThai trangThai;
    private List<ThanhPhanLuong> danhSachThanhPhan;

    public ChiTietLuong() {
        this.trangThai = TrangThai.CHUA_TINH;
        this.danhSachThanhPhan = new ArrayList<>();
    }

    /** Tính tổng lương và lương thực nhận */
    public void tinhTong() {
        // Tổng phụ cấp
        double tongPhuCap = danhSachThanhPhan.stream()
                .filter(tp -> tp.getLoai() == ThanhPhanLuong.Loai.PHU_CAP)
                .mapToDouble(ThanhPhanLuong::getSoTien).sum();

        // Tổng khấu trừ
        double tongTru = danhSachThanhPhan.stream()
                .filter(tp -> tp.getLoai() == ThanhPhanLuong.Loai.KHAU_TRU)
                .mapToDouble(ThanhPhanLuong::getSoTien).sum();

        this.tongLuongChucVu = tongPhuCap;
        this.tongKhauTru = tongTru;
        this.tongLuong = luongCoBan + tongLuongChucVu + tienOT;
        this.luongThucNhan = tongLuong - tongKhauTru;
    }

    // Getters & Setters
    public int getMaChiTietLuong() { return maChiTietLuong; }
    public void setMaChiTietLuong(int maChiTietLuong) { this.maChiTietLuong = maChiTietLuong; }

    public int getMaBL() { return maBL; }
    public void setMaBL(int maBL) { this.maBL = maBL; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }

    public double getLuongCoBan() { return luongCoBan; }
    public void setLuongCoBan(double luongCoBan) { this.luongCoBan = luongCoBan; }

    public double getTongLuongChucVu() { return tongLuongChucVu; }
    public void setTongLuongChucVu(double tongLuongChucVu) { this.tongLuongChucVu = tongLuongChucVu; }

    public double getTienOT() { return tienOT; }
    public void setTienOT(double tienOT) { this.tienOT = tienOT; }

    public double getTongKhauTru() { return tongKhauTru; }
    public void setTongKhauTru(double tongKhauTru) { this.tongKhauTru = tongKhauTru; }

    public double getTongLuong() { return tongLuong; }
    public void setTongLuong(double tongLuong) { this.tongLuong = tongLuong; }

    public double getLuongThucNhan() { return luongThucNhan; }
    public void setLuongThucNhan(double luongThucNhan) { this.luongThucNhan = luongThucNhan; }

    public int getSoNgayCong() { return soNgayCong; }
    public void setSoNgayCong(int soNgayCong) { this.soNgayCong = soNgayCong; }

    public double getTongGioLam() { return tongGioLam; }
    public void setTongGioLam(double tongGioLam) { this.tongGioLam = tongGioLam; }

    public double getTongGioOT() { return tongGioOT; }
    public void setTongGioOT(double tongGioOT) { this.tongGioOT = tongGioOT; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public List<ThanhPhanLuong> getDanhSachThanhPhan() { return danhSachThanhPhan; }
    public void setDanhSachThanhPhan(List<ThanhPhanLuong> ds) { this.danhSachThanhPhan = ds; }

    public void themThanhPhan(ThanhPhanLuong tp) { this.danhSachThanhPhan.add(tp); }

    @Override
    public String toString() {
        return "ChiTietLuong{maNV=" + maNV + ", thucNhan=" + luongThucNhan + "}";
    }
}
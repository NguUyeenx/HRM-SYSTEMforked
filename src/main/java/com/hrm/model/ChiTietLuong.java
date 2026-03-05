package com.hrm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Khớp với bảng CHITIETLUONG trong DB:
 *   maChiTiet, maBangLuong, maNV,
 *   luongCoSo, tongLuongChucVu, luongLamThem,
 *   tongThuNhap, tongKhauTru, luongThucLanh,
 *   soNgayCong, soGioLamThem
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

        public static TrangThai fromDbValue(String value) {
            for (TrangThai t : values()) if (t.dbValue.equals(value)) return t;
            return CHUA_TINH;
        }
    }

    private int maChiTiet;       // PK — đúng tên DB
    private int maBangLuong;     // FK → BANGLUONG — đúng tên DB
    private int maNV;
    private String tenNV;        // Transient — lấy từ JOIN

    // Tên field khớp DB
    private double luongCoSo;        // DB: luongCoSo (không phải luongCoBan)
    private double tongLuongChucVu;
    private double luongLamThem;     // DB: luongLamThem (không phải tienOT)
    private double tongThuNhap;      // DB: tongThuNhap (không phải tongLuong)
    private double tongKhauTru;
    private double luongThucLanh;    // DB: luongThucLanh (không phải luongThucNhan)
    private double soNgayCong;
    private double soGioLamThem;     // DB: soGioLamThem (không phải tongGioOT)

    private TrangThai trangThai;
    private List<ThanhPhanLuong> danhSachThanhPhan;

    public ChiTietLuong() {
        this.trangThai = TrangThai.CHUA_TINH;
        this.danhSachThanhPhan = new ArrayList<>();
    }

    public void tinhTong() {
        double tongPhuCap = danhSachThanhPhan.stream()
                .filter(tp -> tp.getLoai() == ThanhPhanLuong.Loai.PHU_CAP)
                .mapToDouble(ThanhPhanLuong::getSoTien).sum();
        double tongTru = danhSachThanhPhan.stream()
                .filter(tp -> tp.getLoai() == ThanhPhanLuong.Loai.KHAU_TRU)
                .mapToDouble(ThanhPhanLuong::getSoTien).sum();

        this.tongLuongChucVu = tongPhuCap;
        this.tongKhauTru = tongTru;
        this.tongThuNhap = luongCoSo + tongLuongChucVu + luongLamThem;
        this.luongThucLanh = tongThuNhap - tongKhauTru;
    }

    /** Thêm 1 thành phần lương */
    public void themThanhPhan(ThanhPhanLuong tp) {
        this.danhSachThanhPhan.add(tp);
    }

    // ── Getters & Setters ──

    public int getMaChiTietLuong() { return maChiTiet; }
    public void setMaChiTietLuong(int id) { this.maChiTiet = id; }

    public int getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(int maChiTiet) { this.maChiTiet = maChiTiet; }

    public int getMaBL() { return maBangLuong; }
    public void setMaBL(int maBL) { this.maBangLuong = maBL; }

    public int getMaBangLuong() { return maBangLuong; }
    public void setMaBangLuong(int maBangLuong) { this.maBangLuong = maBangLuong; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }

    public double getLuongCoBan() { return luongCoSo; }
    public void setLuongCoBan(double v) { this.luongCoSo = v; }

    public double getLuongCoSo() { return luongCoSo; }
    public void setLuongCoSo(double luongCoSo) { this.luongCoSo = luongCoSo; }

    public double getTongLuongChucVu() { return tongLuongChucVu; }
    public void setTongLuongChucVu(double v) { this.tongLuongChucVu = v; }

    public double getTienOT() { return luongLamThem; }
    public void setTienOT(double v) { this.luongLamThem = v; }

    public double getLuongLamThem() { return luongLamThem; }
    public void setLuongLamThem(double v) { this.luongLamThem = v; }

    public double getTongLuong() { return tongThuNhap; }
    public void setTongLuong(double v) { this.tongThuNhap = v; }

    public double getTongThuNhap() { return tongThuNhap; }
    public void setTongThuNhap(double v) { this.tongThuNhap = v; }

    public double getTongKhauTru() { return tongKhauTru; }
    public void setTongKhauTru(double v) { this.tongKhauTru = v; }

    public double getLuongThucNhan() { return luongThucLanh; }
    public void setLuongThucNhan(double v) { this.luongThucLanh = v; }

    public double getLuongThucLanh() { return luongThucLanh; }
    public void setLuongThucLanh(double v) { this.luongThucLanh = v; }

    public double getSoNgayCong() { return soNgayCong; }
    public void setSoNgayCong(int v) { this.soNgayCong = v; }
    public void setSoNgayCong(double v) { this.soNgayCong = v; }

    public double getTongGioLam() { return soNgayCong * 8; }
    public void setTongGioLam(double v) { /* ignored, tính từ soNgayCong */ }

    public double getTongGioOT() { return soGioLamThem; }
    public void setTongGioOT(double v) { this.soGioLamThem = v; }

    public double getSoGioLamThem() { return soGioLamThem; }
    public void setSoGioLamThem(double v) { this.soGioLamThem = v; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }

    public List<ThanhPhanLuong> getDanhSachThanhPhan() { return danhSachThanhPhan; }
    public void setDanhSachThanhPhan(List<ThanhPhanLuong> ds) { this.danhSachThanhPhan = ds; }

    @Override
    public String toString() {
        return "ChiTietLuong{maNV=" + maNV + ", thucLanh=" + luongThucLanh + "}";
    }
}
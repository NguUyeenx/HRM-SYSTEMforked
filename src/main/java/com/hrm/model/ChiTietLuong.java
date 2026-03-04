package com.hrm.model;

import java.util.ArrayList;
import java.util.List;

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
            for (TrangThai t : values()) {
                if (t.dbValue.equals(value)) return t;
            }
            return CHUA_TINH;
        }
    }

    private int maChiTietLuong;
    private int maBL;
    private int maNV;
    private String tenNV;

    private double luongCoBan;
    private double tongLuongChucVu;
    private double tienOT;
    private double tongKhauTru;
    private double tongLuong;
    private double luongThucNhan;

    private int soNgayCong;
    private double tongGioLam;
    private double tongGioOT;

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
        this.tongLuong = luongCoBan + tongLuongChucVu + tienOT;
        this.luongThucNhan = tongLuong - tongKhauTru;
    }

    /** Thêm 1 thành phần lương (phụ cấp hoặc khấu trừ) vào danh sách */
    public void themThanhPhan(ThanhPhanLuong tp) {
        this.danhSachThanhPhan.add(tp);
    }

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
    public void setDanhSachThanhPhan(List<ThanhPhanLuong> danhSachThanhPhan) {
        this.danhSachThanhPhan = danhSachThanhPhan;
    }

    @Override
    public String toString() {
        return "ChiTietLuong{maNV=" + maNV + ", thucNhan=" + luongThucNhan + "}";
    }
}
package com.hrm.model;

public class SalaryHistory {

    private int id;
    private String maChucVu;
    private double heSoLuongCu;
    private double heSoLuongMoi;
    private double phuCapCu;
    private double phuCapMoi;
    private String ngayThayDoi;
    private String nguoiThayDoi;

    public SalaryHistory(int id, String maChucVu, double heSoLuongCu, double heSoLuongMoi,
            double phuCapCu, double phuCapMoi, String ngayThayDoi, String nguoiThayDoi) {
        this.id = id;
        this.maChucVu = maChucVu;
        this.heSoLuongCu = heSoLuongCu;
        this.heSoLuongMoi = heSoLuongMoi;
        this.phuCapCu = phuCapCu;
        this.phuCapMoi = phuCapMoi;
        this.ngayThayDoi = ngayThayDoi;
        this.nguoiThayDoi = nguoiThayDoi;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getMaChucVu() {
        return maChucVu;
    }

    public double getHeSoLuongCu() {
        return heSoLuongCu;
    }

    public double getHeSoLuongMoi() {
        return heSoLuongMoi;
    }

    public double getPhuCapCu() {
        return phuCapCu;
    }

    public double getPhuCapMoi() {
        return phuCapMoi;
    }

    public String getNgayThayDoi() {
        return ngayThayDoi;
    }

    public String getNguoiThayDoi() {
        return nguoiThayDoi;
    }
}
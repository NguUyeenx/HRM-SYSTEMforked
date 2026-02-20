package com.hrm.model;

public class Position {

    private String maChucVu;
    private String tenChucVu;
    private int capBac;
    private double heSoLuong;
    private double phuCapChucVu;
    private String moTa;
    private String trangThai;

    public Position(String maChucVu, String tenChucVu, int capBac,
            double heSoLuong, double phuCapChucVu, String moTa, String trangThai) {
        this.maChucVu = maChucVu;
        this.tenChucVu = tenChucVu;
        this.capBac = capBac;
        this.heSoLuong = heSoLuong;
        this.phuCapChucVu = phuCapChucVu;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    // Getters
    public String getMaChucVu() {
        return maChucVu;
    }

    public String getTenChucVu() {
        return tenChucVu;
    }

    public int getCapBac() {
        return capBac;
    }

    public double getHeSoLuong() {
        return heSoLuong;
    }

    public double getPhuCapChucVu() {
        return phuCapChucVu;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    // Setters
    public void setTenChucVu(String tenChucVu) {
        this.tenChucVu = tenChucVu;
    }

    public void setCapBac(int capBac) {
        this.capBac = capBac;
    }

    public void setHeSoLuong(double heSoLuong) {
        this.heSoLuong = heSoLuong;
    }

    public void setPhuCapChucVu(double phuCapChucVu) {
        this.phuCapChucVu = phuCapChucVu;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return tenChucVu + " (" + maChucVu + ")";
    }
}
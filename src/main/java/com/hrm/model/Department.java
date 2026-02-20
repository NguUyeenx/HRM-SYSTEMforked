package com.hrm.model;

public class Department {

    private String maPhongBan;
    private String tenPhongBan;
    private String phongBanCha;
    private String trangThai;

    public Department(String maPhongBan, String tenPhongBan, String phongBanCha, String trangThai) {
        this.maPhongBan = maPhongBan;
        this.tenPhongBan = tenPhongBan;
        this.phongBanCha = phongBanCha;
        this.trangThai = trangThai;
    }

    // Getters
    public String getMaPhongBan() {
        return maPhongBan;
    }

    public String getTenPhongBan() {
        return tenPhongBan;
    }

    public String getPhongBanCha() {
        return phongBanCha;
    }

    public String getTrangThai() {
        return trangThai;
    }

    // Setters
    public void setTenPhongBan(String tenPhongBan) {
        this.tenPhongBan = tenPhongBan;
    }

    public void setPhongBanCha(String phongBanCha) {
        this.phongBanCha = phongBanCha;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return tenPhongBan + " (" + maPhongBan + ")";
    }
}
package com.project.QLDSV_HTC.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DANGKY")
public class DangKy {

    @EmbeddedId
    private DangKyId id;

    @ManyToOne
    @MapsId("maLTC")
    @JoinColumn(name = "MALTC", nullable = false,
                foreignKey = @ForeignKey(name = "FK_DANGKY_LOPTINCHI"))
    private LopTinChi lopTinChi;

    @ManyToOne
    @MapsId("maSV")
    @JoinColumn(name = "MASV", nullable = false,
                foreignKey = @ForeignKey(name = "FK_DANGKY_SINHVIEN"))
    private Sinhvien sinhVien;

    @Column(name = "DIEM_CC")
    private Integer diemCC;

    @Column(name = "DIEM_GK")
    private Double diemGK;

    @Column(name = "DIEM_CK")
    private Double diemCK;

    @Column(name = "HUYDANGKY", nullable = false)
    private boolean huyDangKy = false;

    public DangKy() {
    }

    public DangKyId getId() {
        return id;
    }

    public void setId(DangKyId id) {
        this.id = id;
    }

    public LopTinChi getLopTinChi() {
        return lopTinChi;
    }

    public void setLopTinChi(LopTinChi lopTinChi) {
        this.lopTinChi = lopTinChi;
    }

    public Sinhvien getSinhVien() {
        return sinhVien;
    }

    public void setSinhVien(Sinhvien sinhVien) {
        this.sinhVien = sinhVien;
    }

    public Integer getDiemCC() {
        return diemCC;
    }

    public void setDiemCC(Integer diemCC) {
        this.diemCC = diemCC;
    }

    public Double getDiemGK() {
        return diemGK;
    }

    public void setDiemGK(Double diemGK) {
        this.diemGK = diemGK;
    }

    public Double getDiemCK() {
        return diemCK;
    }

    public void setDiemCK(Double diemCK) {
        this.diemCK = diemCK;
    }

    public boolean isHuyDangKy() {
        return huyDangKy;
    }

    public void setHuyDangKy(boolean huyDangKy) {
        this.huyDangKy = huyDangKy;
    }
}

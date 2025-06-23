package com.project.QLDSV_HTC.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    name = "MONHOC",
    uniqueConstraints = @UniqueConstraint(columnNames = "TENMH")
)
public class MonHoc {

    @Id
    @Column(name = "MAMH", length = 10, nullable = false)
    private String maMH;

    @Column(name = "TENMH", length = 50, nullable = false)
    private String tenMH;

    @Column(name = "SOTIET_LT", nullable = false)
    private int soTietLT;

    @Column(name = "SOTIET_TH", nullable = false)
    private int soTietTH;

    @OneToMany(mappedBy = "monHoc", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LopTinChi> dsLopTinChi;

    public MonHoc() {
    }
    
    public MonHoc(MonHoc other) {
        this.maMH      = other.getMaMH();
        this.tenMH     = other.getTenMH();
        this.soTietLT  = other.getSoTietLT();
        this.soTietTH  = other.getSoTietTH();
        // bỏ qua dsLopTinChi
    }

    public String getMaMH() {
        return maMH;
    }

    public void setMaMH(String maMH) {
        this.maMH = maMH;
    }

    public String getTenMH() {
        return tenMH;
    }

    public void setTenMH(String tenMH) {
        this.tenMH = tenMH;
    }

    public int getSoTietLT() {
        return soTietLT;
    }

    public void setSoTietLT(int soTietLT) {
        this.soTietLT = soTietLT;
    }

    public int getSoTietTH() {
        return soTietTH;
    }

    public void setSoTietTH(int soTietTH) {
        this.soTietTH = soTietTH;
    }

    public List<LopTinChi> getDsLopTinChi() {
        return dsLopTinChi;
    }

    public void setDsLopTinChi(List<LopTinChi> dsLopTinChi) {
        this.dsLopTinChi = dsLopTinChi;
    }

    @Override
    public String toString() {
        return tenMH;
    }
}

package com.project.QLDSV_HTC.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    name = "KHOA",
    uniqueConstraints = @UniqueConstraint(columnNames = "TENKHOA")
)
public class Khoa {

    @Id
    @Column(name = "MAKHOA", length = 10, nullable = false)
    private String maKhoa;

    @Column(name = "TENKHOA", length = 50, nullable = false)
    private String tenKhoa;

    // Quan hệ 1-N tới LOP
    @OneToMany(mappedBy = "khoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lop> dsLop;

    // Quan hệ 1-N tới GIANGVIEN
    @OneToMany(mappedBy = "khoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GiangVien> dsGiangVien;

    // Quan hệ 1-N tới LOP_TIN_CHI (nếu muốn, nhưng có thể để là trường String maKhoa ở LopTinChi)
    @OneToMany(mappedBy = "khoaQuanLy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LopTinChi> dsLopTinChi;

    public Khoa() {
    }

    public String getMaKhoa() {
        return maKhoa;
    }

    public void setMaKhoa(String maKhoa) {
        this.maKhoa = maKhoa;
    }

    public String getTenKhoa() {
        return tenKhoa;
    }

    public void setTenKhoa(String tenKhoa) {
        this.tenKhoa = tenKhoa;
    }

    public List<Lop> getDsLop() {
        return dsLop;
    }

    public void setDsLop(List<Lop> dsLop) {
        this.dsLop = dsLop;
    }

    public List<GiangVien> getDsGiangVien() {
        return dsGiangVien;
    }

    public void setDsGiangVien(List<GiangVien> dsGiangVien) {
        this.dsGiangVien = dsGiangVien;
    }

    public List<LopTinChi> getDsLopTinChi() {
        return dsLopTinChi;
    }

    public void setDsLopTinChi(List<LopTinChi> dsLopTinChi) {
        this.dsLopTinChi = dsLopTinChi;
    }
    
    @Override
    public String toString() {
        return tenKhoa;
    }

}

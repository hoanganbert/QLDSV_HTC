package com.project.QLDSV_HTC.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "SINHVIEN")
public class Sinhvien {

    @Id
    @Column(name = "MASV", length = 10, nullable = false)
    private String maSV;

    @Column(name = "HO", length = 50, nullable = false)
    private String ho;

    @Column(name = "TEN", length = 10, nullable = false)
    private String ten;

    @ManyToOne
    @JoinColumn(name = "MALOP", nullable = false,
                foreignKey = @ForeignKey(name = "FK_SINHVIEN_LOP"))
    private Lop lop;

    @Column(name = "PHAI", nullable = false)
    private boolean phai = false;

    @Column(name = "NGAYSINH")
    private LocalDate ngaySinh;

    @Column(name = "DIACHI", length = 100)
    private String diaChi;

    @Column(name = "DANGHIHOC", nullable = false)
    private boolean dangNghiHoc = false;

    @Column(name = "PASSWORD", length = 40)
    private String password = "123456";

    @OneToMany(mappedBy = "sinhVien", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DangKy> dsDangKy;

    public Sinhvien() {
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public Lop getLop() {
        return lop;
    }

    public void setLop(Lop lop) {
        this.lop = lop;
    }

    public boolean isPhai() {
        return phai;
    }

    public void setPhai(boolean phai) {
        this.phai = phai;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public boolean isDangNghiHoc() {
        return dangNghiHoc;
    }

    public void setDangNghiHoc(boolean dangNghiHoc) {
        this.dangNghiHoc = dangNghiHoc;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<DangKy> getDsDangKy() {
        return dsDangKy;
    }

    public void setDsDangKy(List<DangKy> dsDangKy) {
        this.dsDangKy = dsDangKy;
    }

    @Override
    public String toString() {
        return maSV + " – " + ho + " " + ten;
    }
}

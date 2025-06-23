package com.project.QLDSV_HTC.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    name = "LOP",
    uniqueConstraints = @UniqueConstraint(columnNames = "TENLOP")
)
public class Lop {

    @Id
    @Column(name = "MALOP", length = 10, nullable = false)
    private String maLop;

    @Column(name = "TENLOP", length = 50, nullable = false)
    private String tenLop;

    @Column(name = "KHOAHOC", length = 9, nullable = false)
    private String khoaHoc;

    @ManyToOne
    @JoinColumn(name = "MAKHOA", nullable = false,
                foreignKey = @ForeignKey(name = "FK_LOP_KHOA"))
    private Khoa khoa;

    @OneToMany(mappedBy = "lop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sinhvien> dsSinhVien;

    public Lop() {
    }
    
    public Lop(Lop other) {
        this.maLop   = other.getMaLop();
        this.tenLop  = other.getTenLop();
        this.khoaHoc = other.getKhoaHoc();
        this.khoa    = other.getKhoa();
        // bỏ qua dsSinhVien
    }

    public String getMaLop() {
        return maLop;
    }

    public void setMaLop(String maLop) {
        this.maLop = maLop;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public String getKhoaHoc() {
        return khoaHoc;
    }

    public void setKhoaHoc(String khoaHoc) {
        this.khoaHoc = khoaHoc;
    }

    public Khoa getKhoa() {
        return khoa;
    }

    public void setKhoa(Khoa khoa) {
        this.khoa = khoa;
    }

    public List<Sinhvien> getDsSinhVien() {
        return dsSinhVien;
    }

    public void setDsSinhVien(List<Sinhvien> dsSinhVien) {
        this.dsSinhVien = dsSinhVien;
    }
    
    @Override
    public String toString() {
        return maLop + " - " + tenLop;
    }

}

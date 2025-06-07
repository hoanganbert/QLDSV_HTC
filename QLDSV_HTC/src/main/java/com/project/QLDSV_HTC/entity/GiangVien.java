package com.project.QLDSV_HTC.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "GIANGVIEN")
public class GiangVien {

    @Id
    @Column(name = "MAGV", length = 10, nullable = false)
    private String maGV;

    @Column(name = "HO", length = 50, nullable = false)
    private String ho;

    @Column(name = "TEN", length = 10, nullable = false)
    private String ten;

    @Column(name = "HOCVI", length = 20)
    private String hocVi;

    @Column(name = "HOCHAM", length = 20)
    private String hocHam;

    @Column(name = "CHUYENMON", length = 50)
    private String chuyenMon;

    @ManyToOne
    @JoinColumn(name = "MAKHOA", nullable = false,
                foreignKey = @ForeignKey(name = "FK_GIANGVIEN_KHOA"))
    private Khoa khoa;

    @OneToMany(mappedBy = "giangVien", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LopTinChi> dsLopTinChi;

    public GiangVien() {
    }

    public String getMaGV() {
        return maGV;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
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

    public String getHocVi() {
        return hocVi;
    }

    public void setHocVi(String hocVi) {
        this.hocVi = hocVi;
    }

    public String getHocHam() {
        return hocHam;
    }

    public void setHocHam(String hocHam) {
        this.hocHam = hocHam;
    }

    public String getChuyenMon() {
        return chuyenMon;
    }

    public void setChuyenMon(String chuyenMon) {
        this.chuyenMon = chuyenMon;
    }

    public Khoa getKhoa() {
        return khoa;
    }

    public void setKhoa(Khoa khoa) {
        this.khoa = khoa;
    }

    public List<LopTinChi> getDsLopTinChi() {
        return dsLopTinChi;
    }

    public void setDsLopTinChi(List<LopTinChi> dsLopTinChi) {
        this.dsLopTinChi = dsLopTinChi;
    }
    
    @Override
    public String toString() {
        return ho + " " + ten;
    }
}

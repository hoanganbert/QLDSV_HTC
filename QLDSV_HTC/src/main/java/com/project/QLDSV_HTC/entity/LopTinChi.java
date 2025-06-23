package com.project.QLDSV_HTC.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@Entity
@Table(
    name = "LOPTINCHI",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"NIENKHOA", "HOCKY", "MAMH", "NHOM"}
    )
)
public class LopTinChi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MALTC")
    private Integer maLTC;

    @Column(name = "NIENKHOA", length = 9, nullable = false)
    private String nienKhoa;

    @Min(1) @Max(3)
    @Column(name = "HOCKY", nullable = false)
    private Integer hocKy;

    @ManyToOne
    @JoinColumn(name = "MAMH", nullable = false,
                foreignKey = @ForeignKey(name = "FK_LOPTINCHI_MONHOC"))
    private MonHoc monHoc;

    @Min(1)
    @Column(name = "NHOM", nullable = false)
    private Integer nhom;

    @ManyToOne
    @JoinColumn(name = "MAGV", nullable = false,
                foreignKey = @ForeignKey(name = "FK_LOPTINCHI_GIANGVIEN"))
    private GiangVien giangVien;

    @Min(1)
    @Column(name = "SOSVTOITHIEU", nullable = false)
    private Integer soSVToiThieu;

    @ManyToOne
    @JoinColumn(name = "MAKHOA", nullable = false,
                foreignKey = @ForeignKey(name = "FK_LOPTINCHI_KHOA"))
    private Khoa khoaQuanLy;

    @Column(name = "HUYLOP", nullable = false)
    private boolean huyLop = false;

    @OneToMany(mappedBy = "lopTinChi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DangKy> dsDangKy;

    public LopTinChi() {
    }
    
    // Copy constructor
    public LopTinChi(LopTinChi other) {
        this.maLTC        = other.getMaLTC();
        this.nienKhoa     = other.getNienKhoa();
        this.hocKy        = other.getHocKy();
        this.monHoc       = other.getMonHoc();
        this.nhom         = other.getNhom();
        this.giangVien    = other.getGiangVien();
        this.soSVToiThieu = other.getSoSVToiThieu();
        this.khoaQuanLy   = other.getKhoaQuanLy();
        this.huyLop       = other.isHuyLop();
        // lưu ý: dsDangKy không copy, vì chúng ta chỉ cần restore các trường chính
    }

    public Integer getMaLTC() {
        return maLTC;
    }

    public void setMaLTC(Integer maLTC) {
        this.maLTC = maLTC;
    }

    public String getNienKhoa() {
        return nienKhoa;
    }

    public void setNienKhoa(String nienKhoa) {
        this.nienKhoa = nienKhoa;
    }

    public Integer getHocKy() {
        return hocKy;
    }

    public void setHocKy(Integer hocKy) {
        this.hocKy = hocKy;
    }

    public MonHoc getMonHoc() {
        return monHoc;
    }

    public void setMonHoc(MonHoc monHoc) {
        this.monHoc = monHoc;
    }

    public Integer getNhom() {
        return nhom;
    }

    public void setNhom(Integer nhom) {
        this.nhom = nhom;
    }

    public GiangVien getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(GiangVien giangVien) {
        this.giangVien = giangVien;
    }

    public Integer getSoSVToiThieu() {
        return soSVToiThieu;
    }

    public void setSoSVToiThieu(Integer soSVToiThieu) {
        this.soSVToiThieu = soSVToiThieu;
    }

    public Khoa getKhoaQuanLy() {
        return khoaQuanLy;
    }

    public void setKhoaQuanLy(Khoa khoaQuanLy) {
        this.khoaQuanLy = khoaQuanLy;
    }

    public boolean isHuyLop() {
        return huyLop;
    }

    public void setHuyLop(boolean huyLop) {
        this.huyLop = huyLop;
    }

    public List<DangKy> getDsDangKy() {
        return dsDangKy;
    }

    public void setDsDangKy(List<DangKy> dsDangKy) {
        this.dsDangKy = dsDangKy;
    }
}

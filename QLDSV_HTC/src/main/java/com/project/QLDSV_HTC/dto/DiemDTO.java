package com.project.QLDSV_HTC.dto;

import java.util.List;

/**
 * 1. DiemDTO: dùng cho màn Nhập Điểm (trong NhapDiemController).
 *    - Lưu trữ mã SV, họ tên SV, điểm CC, GK, CK và điểm hết môn (tự tính).
 */
public class DiemDTO {
    private String maSV;
    private String hoTenSV;
    private Integer diemCC;
    private Double diemGK;
    private Double diemCK;
    private Double diemHM;

    public DiemDTO() {
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public String getHoTenSV() {
        return hoTenSV;
    }

    public void setHoTenSV(String hoTenSV) {
        this.hoTenSV = hoTenSV;
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

    public Double getDiemHM() {
        return diemHM;
    }

    public void setDiemHM(Double diemHM) {
        this.diemHM = diemHM;
    }

    /**
     * Tự động tính điểm hết môn = CC*0.1 + GK*0.3 + CK*0.6, làm tròn 1 chữ số thập phân.
     */
    public void computeDiemHM() {
        double cc = (diemCC == null) ? 0.0 : diemCC;
        double gk = (diemGK == null) ? 0.0 : diemGK;
        double ck = (diemCK == null) ? 0.0 : diemCK;
        double hm = cc * 0.1 + gk * 0.3 + ck * 0.6;
        // Làm tròn 1 chữ số thập phân
        hm = Math.round(hm * 10) / 10.0;
        this.diemHM = hm;
    }
}

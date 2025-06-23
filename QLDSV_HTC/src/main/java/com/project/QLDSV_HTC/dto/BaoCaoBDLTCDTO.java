package com.project.QLDSV_HTC.dto;

/**
 * 4. BaoCaoBDLTCDTO: dùng cho báo cáo “Bảng điểm môn học của 1 lớp tín chỉ” (BaoCaoBDLTCController).
 *    - STT
 *    - Mã SV
 *    - Họ
 *    - Tên
 *    - Điểm CC
 *    - Điểm GK
 *    - Điểm CK
 *    - Điểm hết môn (tự tính)
 */
public class BaoCaoBDLTCDTO {
    private Integer stt;
    private String maSV;
    private String ho;
    private String ten;
    private Integer diemCC;
    private Double diemGK;
    private Double diemCK;
    private Double diemHM;

    public BaoCaoBDLTCDTO() {
    }

    public Integer getStt() {
        return stt;
    }

    public void setStt(Integer stt) {
        this.stt = stt;
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

    public void computeDiemHM() {
        double cc = (diemCC == null) ? 0.0 : diemCC;
        double gk = (diemGK == null) ? 0.0 : diemGK;
        double ck = (diemCK == null) ? 0.0 : diemCK;
        double hm = cc * 0.1 + gk * 0.3 + ck * 0.6;
        hm = Math.round(hm * 10) / 10.0;
        this.diemHM = hm;
    }
}

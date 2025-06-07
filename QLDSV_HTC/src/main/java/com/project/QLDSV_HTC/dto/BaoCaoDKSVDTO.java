package com.project.QLDSV_HTC.dto;

/**
 * 3. BaoCaoDKSVDTO: dùng cho báo cáo “DS Sinh viên đăng ký lớp tín chỉ” (BaoCaoDKSVController).
 *    - STT
 *    - Mã SV
 *    - Họ
 *    - Tên
 *    - Phái
 *    - Mã lớp
 */
public class BaoCaoDKSVDTO {
    private Integer stt;
    private String maSV;
    private String ho;
    private String ten;
    private String phai;
    private String maLop;

    public BaoCaoDKSVDTO() {
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

    public String getPhai() {
        return phai;
    }

    public void setPhai(String phai) {
        this.phai = phai;
    }

    public String getMaLop() {
        return maLop;
    }

    public void setMaLop(String maLop) {
        this.maLop = maLop;
    }
}

package com.project.QLDSV_HTC.dto;

/**
 * 2. BaoCaoLTCDTO: dùng cho báo cáo “DS Lớp Tín chỉ” (BaoCaoLTCController).
 *    - STT
 *    - Tên môn học
 *    - Nhóm
 *    - Họ tên GV
 *    - Số SV tối thiểu
 *    - Số SV đã đăng ký
 */
public class BaoCaoLTCDTO {
    private Integer stt;
    private String tenMH;
    private Integer nhom;
    private String hoTenGV;
    private Integer soSVMin;
    private Integer soSVDaDK;

    public BaoCaoLTCDTO() {
    }

    public Integer getStt() {
        return stt;
    }

    public void setStt(Integer stt) {
        this.stt = stt;
    }

    public String getTenMH() {
        return tenMH;
    }

    public void setTenMH(String tenMH) {
        this.tenMH = tenMH;
    }

    public Integer getNhom() {
        return nhom;
    }

    public void setNhom(Integer nhom) {
        this.nhom = nhom;
    }

    public String getHoTenGV() {
        return hoTenGV;
    }

    public void setHoTenGV(String hoTenGV) {
        this.hoTenGV = hoTenGV;
    }

    public Integer getSoSVMin() {
        return soSVMin;
    }

    public void setSoSVMin(Integer soSVMin) {
        this.soSVMin = soSVMin;
    }

    public Integer getSoSVDaDK() {
        return soSVDaDK;
    }

    public void setSoSVDaDK(Integer soSVDaDK) {
        this.soSVDaDK = soSVDaDK;
    }
}

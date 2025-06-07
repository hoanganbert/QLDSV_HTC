package com.project.QLDSV_HTC.dto;

/**
 * 5. BaoCaoPhieuDiemDTO: dùng cho báo cáo “Phiếu điểm” (BaoCaoPhieuDiemSVController).
 *    - STT
 *    - Tên môn học
 *    - Điểm Max (nhiều lần thi thì lấy max)
 */
public class BaoCaoPhieuDiemDTO {
    private Integer stt;
    private String tenMH;
    private Double diemMax;

    public BaoCaoPhieuDiemDTO() {
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

    public Double getDiemMax() {
        return diemMax;
    }

    public void setDiemMax(Double diemMax) {
        this.diemMax = diemMax;
    }
}

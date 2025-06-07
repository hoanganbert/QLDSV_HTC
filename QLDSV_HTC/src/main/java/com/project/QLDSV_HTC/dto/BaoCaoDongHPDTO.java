package com.project.QLDSV_HTC.dto;

/**
 * 6. BaoCaoDongHPDTO: dùng cho báo cáo “Danh sách SV đóng học phí” (BaoCaoDongHPController).
 *    - STT
 *    - Họ
 *    - Tên
 *    - Học phí (số tiền phải đóng)
 *    - Số tiền đã đóng (có thể là 0 nếu chưa đóng)
 */
public class BaoCaoDongHPDTO {
    private Integer stt;
    private String ho;
    private String ten;
    private Double hocPhi;
    private Double soDaDong;

    public BaoCaoDongHPDTO() {
    }

    public Integer getStt() {
        return stt;
    }

    public void setStt(Integer stt) {
        this.stt = stt;
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

    public Double getHocPhi() {
        return hocPhi;
    }

    public void setHocPhi(Double hocPhi) {
        this.hocPhi = hocPhi;
    }

    public Double getSoDaDong() {
        return soDaDong;
    }

    public void setSoDaDong(Double soDaDong) {
        this.soDaDong = soDaDong;
    }
    
    public String getHoTen() {
        return ho + " " + ten;
    }

}

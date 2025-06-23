package com.project.QLDSV_HTC.dto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * DangKyDTO: Dùng cho màn Đăng ký lớp tín chỉ (DangKyController).
 * Mỗi dòng đại diện cho một lớp tín chỉ có thể đăng ký, gồm:
 *  - selected: (Boolean) Checkbox để chọn/lựa đăng ký
 *  - maLTC: (Integer) Mã Lớp tín chỉ
 *  - maMH: (String) Mã Môn học
 *  - tenMH: (String) Tên Môn học
 *  - nhom: (Integer) Nhóm
 *  - hoTenGV: (String) Họ tên Giảng viên dạy lớp đó
 *  - soSVDaDK: (Integer) Số sinh viên đã đăng ký (chưa hủy)
 */
public class DangKyDTO {
//    private Boolean selected;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private Integer maLTC;
    private String maMH;
    private String tenMH;
    private Integer nhom;
    private String hoTenGV;
    private Integer soSVDaDK;

//    public DangKyDTO() {
//        this.selected = false;
//    }
//
//    public Boolean getSelected() {
//        return selected;
//    }
//
//    public void setSelected(Boolean selected) {
//        this.selected = selected;
//    }
    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }
    public void setSelected(boolean sel) {
        this.selected.set(sel);
    }

    public Integer getMaLTC() {
        return maLTC;
    }

    public void setMaLTC(Integer maLTC) {
        this.maLTC = maLTC;
    }

    public String getMaMH() {
        return maMH;
    }

    public void setMaMH(String maMH) {
        this.maMH = maMH;
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

    public Integer getSoSVDaDK() {
        return soSVDaDK;
    }

    public void setSoSVDaDK(Integer soSVDaDK) {
        this.soSVDaDK = soSVDaDK;
    }
}

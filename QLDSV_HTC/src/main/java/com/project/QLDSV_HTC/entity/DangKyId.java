package com.project.QLDSV_HTC.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DangKyId implements Serializable {

    @Column(name = "MALTC", nullable = false)
    private Integer maLTC;

    @Column(name = "MASV", length = 10, nullable = false)
    private String maSV;

    public DangKyId() {
    }

    public DangKyId(Integer maLTC, String maSV) {
        this.maLTC = maLTC;
        this.maSV = maSV;
    }

    public Integer getMaLTC() {
        return maLTC;
    }

    public void setMaLTC(Integer maLTC) {
        this.maLTC = maLTC;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DangKyId)) return false;
        DangKyId that = (DangKyId) o;
        return Objects.equals(maLTC, that.maLTC) &&
               Objects.equals(maSV, that.maSV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maLTC, maSV);
    }
}

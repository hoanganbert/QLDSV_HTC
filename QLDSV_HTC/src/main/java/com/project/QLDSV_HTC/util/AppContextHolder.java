package com.project.QLDSV_HTC.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Bean singleton dùng để lưu trữ thông tin người dùng sau khi đăng nhập, 
 * phục vụ cho việc phân quyền trong toàn ứng dụng.
 */
@Component
@Scope("singleton")
public class AppContextHolder {

    private String role;
    private String maKhoa;
    private String maGV;
    private String maSV;
    private String tenKhoa;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMaKhoa() {
        return maKhoa;
    }

    public void setMaKhoa(String maKhoa) {
        this.maKhoa = maKhoa;
    }

    public String getMaGV() {
        return maGV;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }
    
    public String getTenKhoa() {
        return tenKhoa;
    }
    
    public void setTenKhoa(String tenKhoa) {
        this.tenKhoa = tenKhoa;
    }
}

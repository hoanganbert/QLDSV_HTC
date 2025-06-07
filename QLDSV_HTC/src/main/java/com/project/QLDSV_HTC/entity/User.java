package com.project.QLDSV_HTC.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @Column(name = "USERNAME", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "PASSWORD", length = 50, nullable = false)
    private String password;

    /**
     * Vai trò của người dùng: "PGV", "KHOA", "GIANGVIEN", hoặc "SINHVIEN"
     */
    @Column(name = "ROLE", length = 20, nullable = false)
    private String role;

    /**
     * Nếu role = "KHOA", trường này lưu mã khoa (nChar(10))
     */
    @Column(name = "MAKHOA", length = 10)
    private String maKhoa;

    /**
     * Nếu role = "GIANGVIEN", trường này lưu mã giảng viên (nChar(10))
     */
    @Column(name = "MAGV", length = 10)
    private String maGV;

    /**
     * Nếu role = "SINHVIEN", trường này lưu mã sinh viên (nChar(10))
     */
    @Column(name = "MASV", length = 10)
    private String maSV;

    public User() {
    }

    public User(String username, String password, String role,
                String maKhoa, String maGV, String maSV) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.maKhoa = maKhoa;
        this.maGV = maGV;
        this.maSV = maSV;
    }

    // Getter / Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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
}

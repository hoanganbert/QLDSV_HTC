package com.project.QLDSV_HTC.repository;

import com.project.QLDSV_HTC.entity.GiangVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiangVienRepository extends JpaRepository<GiangVien, String> {
    List<GiangVien> findByKhoa_MaKhoa(String maKhoa);
}

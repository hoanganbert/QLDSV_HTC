package com.project.QLDSV_HTC.repository;

import com.project.QLDSV_HTC.entity.Lop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LopRepository extends JpaRepository<Lop, String> {
    /**
     * Trả về danh sách Lớp thuộc 1 Khoa (theo maKhoa).
     */
    @Query("SELECT l FROM Lop l WHERE l.khoa.maKhoa = :maKhoa")
    List<Lop> findByMaKhoa(@Param("maKhoa") String maKhoa);
}

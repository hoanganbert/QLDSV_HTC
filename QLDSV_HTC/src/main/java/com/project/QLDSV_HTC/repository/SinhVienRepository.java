package com.project.QLDSV_HTC.repository;

import com.project.QLDSV_HTC.entity.Sinhvien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SinhVienRepository extends JpaRepository<Sinhvien, String> {
    /**
     * Lấy danh sách sinh viên theo mã Lớp.
     */
    @Query("SELECT s FROM Sinhvien s WHERE s.lop.maLop = :maLop")
    List<Sinhvien> findByMaLop(@Param("maLop") String maLop);
}



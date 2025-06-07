package com.project.QLDSV_HTC.repository;

import com.project.QLDSV_HTC.entity.MonHoc;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonHocRepository extends JpaRepository<MonHoc, String> {
    List<MonHoc> findByTenMHContaining(String keyword);
}

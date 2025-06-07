package com.project.QLDSV_HTC.repository;

import com.project.QLDSV_HTC.entity.Khoa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhoaRepository extends JpaRepository<Khoa, String> {

     List<Khoa> findByTenKhoaContaining(String keyword);
}
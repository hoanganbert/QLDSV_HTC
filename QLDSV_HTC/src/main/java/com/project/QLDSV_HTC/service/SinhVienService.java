// File: src/main/java/com/project/QLDSV_HTC/service/SinhVienService.java
package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.entity.Sinhvien;
import com.project.QLDSV_HTC.repository.SinhVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SinhVienService {

    @Autowired
    private SinhVienRepository svRepo;

    public List<Sinhvien> getAllSV() {
        return svRepo.findAll();
    }

    public Optional<Sinhvien> findById(String maSV) {
        return svRepo.findById(maSV);
    }

    public boolean existsById(String maSV) {
        return svRepo.existsById(maSV);
    }

    public Sinhvien save(Sinhvien sv) {
        return svRepo.save(sv);
    }

    public void delete(String maSV) {
        svRepo.deleteById(maSV);
    }

    public List<Sinhvien> getSVTheoLop(String maLop) {
        return svRepo.findByMaLop(maLop);
    }
}

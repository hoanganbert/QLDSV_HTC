// File: src/main/java/com/project/QLDSV_HTC/service/GiangVienService.java
package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.entity.GiangVien;
import com.project.QLDSV_HTC.repository.GiangVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GiangVienService {

    @Autowired
    private GiangVienRepository gvRepo;

    public List<GiangVien> getAllGiangVien() {
        return gvRepo.findAll();
    }

    public Optional<GiangVien> findById(String maGV) {
        return gvRepo.findById(maGV);
    }

    public boolean existsById(String maGV) {
        return gvRepo.existsById(maGV);
    }

    public GiangVien save(GiangVien gv) {
        return gvRepo.save(gv);
    }

    public void delete(String maGV) {
        gvRepo.deleteById(maGV);
    }
    
    public List<GiangVien> getByMaKhoa(String maKhoa) {
        return gvRepo.findByKhoa_MaKhoa(maKhoa);
    }
}

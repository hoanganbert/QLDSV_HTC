// File: src/main/java/com/project/QLDSV_HTC/service/LopService.java
package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.entity.Lop;
import com.project.QLDSV_HTC.repository.LopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LopService {

    @Autowired
    private LopRepository lopRepo;

    public List<Lop> getAllLop() {
        return lopRepo.findAll();
    }

    public Optional<Lop> findById(String maLop) {
        return lopRepo.findById(maLop);
    }

    public boolean existsById(String maLop) {
        return lopRepo.existsById(maLop);
    }

    public Lop save(Lop lop) {
        return lopRepo.save(lop);
    }

    public void delete(String maLop) {
        lopRepo.deleteById(maLop);
    }

    public List<Lop> getLopByKhoa(String maKhoa) {
        return lopRepo.findByMaKhoa(maKhoa);
    }
}

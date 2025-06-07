// File: src/main/java/com/project/QLDSV_HTC/service/KhoaService.java
package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.entity.Khoa;
import com.project.QLDSV_HTC.repository.KhoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KhoaService {

    @Autowired
    private KhoaRepository khoaRepo;

    public List<Khoa> getAllKhoa() {
        return khoaRepo.findAll();
    }

    public Optional<Khoa> findById(String maKhoa) {
        return khoaRepo.findById(maKhoa);
    }

    public boolean existsById(String maKhoa) {
        return khoaRepo.existsById(maKhoa);
    }

    public Khoa save(Khoa khoa) {
        return khoaRepo.save(khoa);
    }

    public void delete(String maKhoa) {
        khoaRepo.deleteById(maKhoa);
    }
}

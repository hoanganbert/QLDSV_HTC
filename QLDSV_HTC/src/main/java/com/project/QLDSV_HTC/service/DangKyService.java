// File: src/main/java/com/project/QLDSV_HTC/service/DangKyService.java
package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.entity.DangKy;
import com.project.QLDSV_HTC.entity.DangKyId;
import com.project.QLDSV_HTC.repository.DangKyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DangKyService {

    @Autowired
    private DangKyRepository dkRepo;

    public List<DangKy> getDKTheoLTC(Integer maLTC) {
        return dkRepo.findByMaLTCAndHuyDangKy(maLTC, false);
    }

    public boolean isDangKy(Integer maLTC, String maSV) {
        return dkRepo.existsByMaLTCAndMaSVAndHuyDangKy(maLTC, maSV, false);
    }

    public int countByLTC(Integer maLTC) {
        return dkRepo.countByMaLTCAndHuyDangKy(maLTC, false);
    }

    public DangKy save(DangKy dk) {
        return dkRepo.save(dk);
    }

    public void delete(Integer maLTC, String maSV) {
        dkRepo.deleteById(new DangKyId(maLTC, maSV));
    }
}

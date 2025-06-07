// File: src/main/java/com/project/QLDSV_HTC/service/MonHocService.java
package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.entity.MonHoc;
import com.project.QLDSV_HTC.repository.MonHocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MonHocService {

    @Autowired
    private MonHocRepository mhRepo;

    public List<MonHoc> getAllMonHoc() {
        return mhRepo.findAll();
    }

    public Optional<MonHoc> findById(String maMH) {
        return mhRepo.findById(maMH);
    }

    public boolean existsById(String maMH) {
        return mhRepo.existsById(maMH);
    }

    public MonHoc save(MonHoc mh) {
        return mhRepo.save(mh);
    }

    public void delete(String maMH) {
        mhRepo.deleteById(maMH);
    }
}

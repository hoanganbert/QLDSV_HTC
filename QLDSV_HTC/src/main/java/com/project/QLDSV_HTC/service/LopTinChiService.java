package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.entity.DangKy;
import com.project.QLDSV_HTC.entity.DangKyId;
import com.project.QLDSV_HTC.entity.LopTinChi;
import com.project.QLDSV_HTC.entity.Sinhvien;
import com.project.QLDSV_HTC.repository.DangKyRepository;
import com.project.QLDSV_HTC.repository.LopTinChiRepository;
import com.project.QLDSV_HTC.repository.SinhVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LopTinChiService {

    @Autowired private LopTinChiRepository ltcRepo;
    @Autowired private DangKyRepository dkRepo;
    @Autowired private SinhVienRepository svRepo;

    public List<LopTinChi> getAllLTC() {
        return ltcRepo.findAll();
    }

    public Optional<LopTinChi> findById(Integer maLTC) {
        return ltcRepo.findById(maLTC);
    }

    public LopTinChi save(LopTinChi ltc) {
        return ltcRepo.save(ltc);
    }

    public void delete(Integer maLTC) {
        ltcRepo.deleteById(maLTC);
    }

    public List<LopTinChi> findByNienKhoaHocKyAndKhoa(String nienKhoa, Integer hocKy, String maKhoa) {
        return ltcRepo.findByNienKhoaHocKyAndMaKhoa(nienKhoa, hocKy, maKhoa);
    }

    public int countSVDaDK(Integer maLTC) {
        return ltcRepo.countSVDaDK(maLTC);
    }

    public Optional<LopTinChi> getLTCTheoChiTiet(String nienKhoa, Integer hocKy, String maMH, Integer nhom, String maGV) {
        return ltcRepo.findByChiTiet(nienKhoa, hocKy, maMH, nhom, maGV);
    }

    public boolean existsByChiTiet(String nienKhoa, Integer hocKy, String maMH, Integer nhom, String maGV) {
        return ltcRepo.existsByChiTiet(nienKhoa, hocKy, maMH, nhom, maGV);
    }

    /**
     * Cập nhật đăng ký/hủy đăng ký cho 1 sinh viên vào lớp tín chỉ.
     * Nếu selected = true → tạo mới bản ghi DangKy (huyDangKy=false).
     * Nếu selected = false → cập nhật cờ huyDangKy=true.
     */
    public void capNhatDangKy(String maSV, Integer maLTC, boolean selected) {
        DangKyId id = new DangKyId(maLTC, maSV);
        Optional<DangKy> opt = dkRepo.findById(id);

        if (selected) {
            if (opt.isPresent()) {
                // nếu bản ghi đã tồn tại, chỉ cần set huyDangKy = false
                DangKy dk = opt.get();
                dk.setHuyDangKy(false);
                dkRepo.save(dk);
            } else {
                // tạo mới một DangKy hoàn chỉnh
                Optional<LopTinChi> optLtc = ltcRepo.findById(maLTC);
                Optional<Sinhvien> optSv = svRepo.findById(maSV);
                if (optLtc.isPresent() && optSv.isPresent()) {
                    DangKy dk = new DangKy();
                    dk.setId(id);
                    dk.setLopTinChi(optLtc.get());
                    dk.setSinhVien(optSv.get());
                    dk.setDiemCC(null);
                    dk.setDiemGK(null);
                    dk.setDiemCK(null);
                    dk.setHuyDangKy(false);
                    dkRepo.save(dk);
                }
                // Nếu ltc hoặc sv không tồn tại, có thể bỏ qua hoặc log lỗi tuỳ mục đích
            }
        } else {
            if (opt.isPresent()) {
                // nếu bản ghi tồn tại, set huyDangKy = true
                DangKy dk = opt.get();
                dk.setHuyDangKy(true);
                dkRepo.save(dk);
            }
            // nếu chưa tồn tại (selected=false) thì không cần làm gì
        }
    }

    public List<LopTinChi> findByNienKhoaHocKy(String nienKhoa, Integer hocKy) {
        return ltcRepo.findByNienKhoaAndHocKy(nienKhoa, hocKy);
    }

}

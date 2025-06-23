package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BaoCaoService {

    @Autowired
    private JdbcTemplate jdbc;

    /** 1. DS SV đăng ký lớp tín chỉ */
    public List<BaoCaoDKSVDTO> getBaoCaoDKSV(
            String maKhoa, String nienKhoa, int hocKy,
            String maMH, int nhom) {
        return jdbc.query(
            "EXEC dbo.sp_BaoCao_DKSV ?, ?, ?, ?, ?",
            new Object[]{ maKhoa, nienKhoa, hocKy, maMH, nhom },
            new BeanPropertyRowMapper<>(BaoCaoDKSVDTO.class)
        );
    }

    /** 2. Bảng điểm môn học của 1 lớp tín chỉ */
    public List<BaoCaoBDLTCDTO> getBaoCaoBDLTC(
            String maKhoa, String nienKhoa, int hocKy,
            String maMH, int nhom) {
        return jdbc.query(
            "EXEC dbo.sp_BaoCao_BDLTC ?, ?, ?, ?, ?",
            new Object[]{ maKhoa, nienKhoa, hocKy, maMH, nhom },
            new BeanPropertyRowMapper<>(BaoCaoBDLTCDTO.class)
        );
    }

    /** 3. DS Lớp tín chỉ */
    public List<BaoCaoLTCDTO> getBaoCaoLTCList(
            String maKhoa, String nienKhoa, int hocKy) {
        return jdbc.query(
            "EXEC dbo.sp_BaoCao_LTCList ?, ?, ?",
            new Object[]{ maKhoa, nienKhoa, hocKy },
            new BeanPropertyRowMapper<>(BaoCaoLTCDTO.class)
        );
    }

    /** 4. DS SV đóng học phí */
    public List<BaoCaoDongHPDTO> getBaoCaoDongHP(
            String maLop, String nienKhoa, int hocKy) {
        return jdbc.query(
            "EXEC dbo.sp_BaoCao_DongHP ?, ?, ?",
            new Object[]{ maLop, nienKhoa, hocKy },
            new BeanPropertyRowMapper<>(BaoCaoDongHPDTO.class)
        );
    }

    /** 5. Phiếu điểm SV */
    public List<BaoCaoPhieuDiemDTO> getBaoCaoPhieuDiemSV(
            String maSV, String nienKhoa, int hocKy) {
        return jdbc.query(
            "EXEC dbo.sp_BaoCao_PhieuDiemSV ?, ?, ?",
            new Object[]{ maSV, nienKhoa, hocKy },
            new BeanPropertyRowMapper<>(BaoCaoPhieuDiemDTO.class)
        );
    }

    /** 6. Bảng điểm tổng kết cuối khóa (cross-tab) */
    public BieuDoBDDTO getBaoCaoBDDTO(String maLop) {
        List<Map<String,Object>> rows = jdbc.queryForList(
            "EXEC dbo.sp_BieuDoBD ?", maLop
        );

        List<String> subjectNames = rows.stream()
            .map(r -> (String) r.get("MaMH"))
            .distinct()
            .collect(Collectors.toList());

        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String,Object> r : rows) {
            String sv = (String) r.get("MaSVHoTen");
            String mh = (String) r.get("MaMH");
            Object diem = r.get("DiemHM");
            grouped
              .computeIfAbsent(sv, k -> new HashMap<>())
              .put(mh, diem);
        }

        List<List<Object>> data = new ArrayList<>();
        for (Map.Entry<String, Map<String,Object>> e : grouped.entrySet()) {
            List<Object> row = new ArrayList<>();
            row.add(e.getKey());
            subjectNames.forEach(mh -> row.add(e.getValue().getOrDefault(mh, null)));
            data.add(row);
        }

        BieuDoBDDTO dto = new BieuDoBDDTO();
        dto.setSubjectNames(subjectNames);
        dto.setData(data);
        return dto;
    }
}

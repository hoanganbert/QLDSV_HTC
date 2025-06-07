package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.dto.*;
import com.project.QLDSV_HTC.entity.LopTinChi;
import com.project.QLDSV_HTC.entity.DangKy;
import com.project.QLDSV_HTC.entity.Sinhvien;
import com.project.QLDSV_HTC.repository.LopTinChiRepository;
import com.project.QLDSV_HTC.repository.DangKyRepository;
import com.project.QLDSV_HTC.repository.SinhVienRepository;
import com.project.QLDSV_HTC.repository.LopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BaoCaoService {

    @Autowired
    private LopTinChiRepository ltcRepo;

    @Autowired
    private DangKyRepository dkRepo;

    @Autowired
    private SinhVienRepository svRepo;

    @Autowired
    private LopRepository lopRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Báo cáo DS LTC (DS Lớp tín chỉ) cho Niên khóa, Học kỳ, và Khoa.
     */
    public List<BaoCaoLTCDTO> getBaoCaoLTC(String maKhoa, String nienKhoa, Integer hocKy) {
        List<LopTinChi> list = ltcRepo.findByNienKhoaHocKyAndMaKhoa(nienKhoa, hocKy, maKhoa);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        List<BaoCaoLTCDTO> result = new ArrayList<>();
        int stt = 1;
        for (LopTinChi ltc : list) {
            BaoCaoLTCDTO dto = new BaoCaoLTCDTO();
            dto.setStt(stt++);
            dto.setTenMH(ltc.getMonHoc().getTenMH());
            dto.setNhom(ltc.getNhom());
            dto.setHoTenGV(ltc.getGiangVien().getHo() + " " + ltc.getGiangVien().getTen());
            dto.setSoSVMin(ltc.getSoSVToiThieu());
            int count = dkRepo.countByMaLTCAndHuyDangKy(ltc.getMaLTC(), false);
            dto.setSoSVDaDK(count);
            result.add(dto);
        }
        return result;
    }

    /**
     * Báo cáo DS SV ĐK cho Niên khóa, Học kỳ, Môn học, Nhóm, và Khoa.
     */
    public List<BaoCaoDKSVDTO> getBaoCaoDKSV(
            String maKhoa, String nienKhoa, Integer hocKy, String maMH, Integer nhom) {
        Optional<LopTinChi> optLtc = ltcRepo.findOneByNK_HK_MH_Nhom_Khoa(nienKhoa, hocKy, maMH, nhom, maKhoa);
        if (!optLtc.isPresent()) {
            return Collections.emptyList();
        }
        LopTinChi ltc = optLtc.get();
        List<DangKy> ds = dkRepo.findByMaLTCAndHuyDangKy(ltc.getMaLTC(), false);
        if (ds.isEmpty()) {
            return Collections.emptyList();
        }
        List<BaoCaoDKSVDTO> result = new ArrayList<>();
        int stt = 1;
        for (DangKy dk : ds) {
            Sinhvien sv = svRepo.findById(dk.getId().getMaSV()).orElse(null);
            if (sv != null) {
                BaoCaoDKSVDTO dto = new BaoCaoDKSVDTO();
                dto.setStt(stt++);
                dto.setMaSV(sv.getMaSV());
                dto.setHo(sv.getHo());
                dto.setTen(sv.getTen());
                dto.setPhai(sv.isPhai() ? "Nữ" : "Nam");
                dto.setMaLop(sv.getLop().getMaLop());
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * Báo cáo Bảng điểm môn học của 1 lớp tín chỉ.
     */
    public List<BaoCaoBDLTCDTO> getBaoCaoBDLTC(
            String maKhoa, String nienKhoa, Integer hocKy, String maMH, Integer nhom, String maGV) {
        Optional<LopTinChi> optLtc = ltcRepo.findByChiTiet(nienKhoa, hocKy, maMH, nhom, maGV);
        if (!optLtc.isPresent()) {
            return Collections.emptyList();
        }
        LopTinChi ltc = optLtc.get();
        if (!ltc.getKhoaQuanLy().getMaKhoa().equals(maKhoa)) {
            return Collections.emptyList();
        }
        List<DangKy> ds = dkRepo.findByMaLTCAndHuyDangKy(ltc.getMaLTC(), false);
        if (ds.isEmpty()) {
            return Collections.emptyList();
        }
        List<BaoCaoBDLTCDTO> result = new ArrayList<>();
        int stt = 1;
        for (DangKy dk : ds) {
            Sinhvien sv = svRepo.findById(dk.getId().getMaSV()).orElse(null);
            if (sv != null) {
                BaoCaoBDLTCDTO dto = new BaoCaoBDLTCDTO();
                dto.setStt(stt++);
                dto.setMaSV(sv.getMaSV());
                dto.setHo(sv.getHo());
                dto.setTen(sv.getTen());
                dto.setDiemCC(dk.getDiemCC() == null ? 0 : dk.getDiemCC());
                dto.setDiemGK(dk.getDiemGK() == null ? 0.0 : dk.getDiemGK());
                dto.setDiemCK(dk.getDiemCK() == null ? 0.0 : dk.getDiemCK());
                dto.computeDiemHM();
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * Báo cáo Phiếu điểm SV: Lấy điểm Max của mỗi môn SV đã thi trong niên khóa/học kỳ.
     */
    public List<BaoCaoPhieuDiemDTO> getBaoCaoPhieuDiemSV(
            String maSV, String nienKhoa, Integer hocKy) {
        String sql = ""
            + "SELECT mh.TENMH, "
            + "       MAX(dk.DIEMCK*0.6 + dk.DIEMCC*0.1 + dk.DIEMGK*0.3) AS DIEMMAX "
            + "FROM DANGKY dk "
            + "JOIN LOPTINCHI ltc ON dk.MALTC = ltc.MALTC "
            + "JOIN MONHOC mh ON ltc.MAMH = mh.MAMH "
            + "WHERE dk.MASV = ? "
            + "  AND ltc.NIENKHOA = ? "
            + "  AND ltc.HOCKY = ? "
            + "  AND dk.HUYDANGKY = 0 "
            + "GROUP BY mh.TENMH "
            + "ORDER BY mh.TENMH";
        return jdbcTemplate.query(
            sql,
            new Object[]{maSV, nienKhoa, hocKy},
            (ResultSet rs, int rowNum) -> {
                BaoCaoPhieuDiemDTO dto = new BaoCaoPhieuDiemDTO();
                dto.setStt(rowNum + 1);
                dto.setTenMH(rs.getString("TENMH"));
                dto.setDiemMax(rs.getDouble("DIEMMAX"));
                return dto;
            });
    }

    /**
     * Báo cáo Danh sách sinh viên đóng học phí: dựa vào Lớp truyền thống, Niên khóa, Học kỳ.
     * Nếu SV chưa đóng, trả về 0.
     */
    public List<BaoCaoDongHPDTO> getBaoCaoDongHP(
            String maLop, String nienKhoa, Integer hocKy) {
        String sql = ""
            + "SELECT s.HO, s.TEN, "
            + "       COALESCE(hp.HOCPHI, 0) AS HOCPHI, "
            + "       COALESCE(hp.SOTIEN, 0) AS SODADONG "
            + "  FROM SINHVIEN s "
            + "  LEFT JOIN ( "
            + "     SELECT maSV, SUM(HP) AS HOCPHI, SUM(THANHTIEN) AS SOTIEN "
            + "       FROM HOCPHI "
            + "      WHERE NIENKHOA = ? AND HOCKY = ? "
            + "      GROUP BY maSV "
            + "  ) hp ON s.MASV = hp.maSV "
            + " WHERE s.MALOP = ? "
            + " ORDER BY s.HO, s.TEN";
        List<BaoCaoDongHPDTO> tmp = jdbcTemplate.query(
            sql,
            new Object[]{nienKhoa, hocKy, maLop},
            (ResultSet rs, int rowNum) -> {
                BaoCaoDongHPDTO dto = new BaoCaoDongHPDTO();
                dto.setStt(rowNum + 1);
                dto.setHo(rs.getString("HO"));
                dto.setTen(rs.getString("TEN"));
                dto.setHocPhi(rs.getDouble("HOCPHI"));
                dto.setSoDaDong(rs.getDouble("SODADONG"));
                return dto;
            });
        return tmp != null ? tmp : Collections.emptyList();
    }

    /**
     * Báo cáo Bảng điểm tổng kết cuối khóa (cross-tab) dựa vào Lớp truyền thống.
     * Trả về đối tượng chứa:
     *  - subjectNames: danh sách tên môn học làm header
     *  - data: mỗi phần tử là List<Object> [ "MãSV – Họ tên", điểm môn 1, điểm môn 2, … ]
     */
    public BieuDoBDDTO getBaoCaoBDTO(String maLop) {
        // 1) Lấy danh sách môn học của lớp truyền thống
        String sqlSubjects = ""
            + "SELECT DISTINCT mh.TENMH "
            + "FROM DANGKY dk "
            + "JOIN LOPTINCHI ltc ON dk.MALTC = ltc.MALTC "
            + "JOIN MONHOC mh ON ltc.MAMH = mh.MAMH "
            + "JOIN SINHVIEN s ON dk.MASV = s.MASV "
            + "WHERE s.MALOP = ? AND dk.HUYDANGKY = 0 "
            + "ORDER BY mh.TENMH";
        List<String> subjectNames = jdbcTemplate.queryForList(
            sqlSubjects, new Object[]{maLop}, String.class);
        if (subjectNames.isEmpty()) {
            return new BieuDoBDDTO();
        }

        // 2) Xây dựng và thực thi dynamic pivot
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT s.MASV + ' - ' + s.HO + ' ' + s.TEN AS SV ");
        for (String subj : subjectNames) {
            sb.append(", MAX(CASE WHEN mh.TENMH = '")
              .append(subj.replace("'", "''"))
              .append("' THEN dk.DIEMCC*0.1 + dk.DIEMGK*0.3 + dk.DIEMCK*0.6 ELSE NULL END) AS [")
              .append(subj)
              .append("] ");
        }
        sb.append("FROM SINHVIEN s ")
          .append("JOIN DANGKY dk ON s.MASV = dk.MASV AND dk.HUYDANGKY = 0 ")
          .append("JOIN LOPTINCHI ltc ON dk.MALTC = ltc.MALTC ")
          .append("JOIN MONHOC mh ON ltc.MAMH = mh.MAMH ")
          .append("WHERE s.MALOP = ? ")
          .append("GROUP BY s.MASV, s.HO, s.TEN ")
          .append("ORDER BY s.HO, s.TEN");

        List<List<Object>> data = new ArrayList<>();
        jdbcTemplate.query(sb.toString(), new Object[]{maLop}, (ResultSet rs) -> {
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                // Cột đầu: "MãSV – Họ tên"
                row.add(rs.getString("SV"));
                // Tiếp theo từng môn
                for (String subj : subjectNames) {
                    Double diem = rs.getDouble(subj);
                    if (rs.wasNull()) {
                        row.add(0.0);
                    } else {
                        Double rounded = Math.round(diem * 10) / 10.0;
                        row.add(rounded);
                    }
                }
                data.add(row);
            }
        });

        BieuDoBDDTO dto = new BieuDoBDDTO();
        dto.setSubjectNames(subjectNames);
        dto.setData(data);
        return dto;
    }
}

package com.project.QLDSV_HTC.repository;

import com.project.QLDSV_HTC.entity.LopTinChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LopTinChiRepository extends JpaRepository<LopTinChi, Integer> {
	@Query("SELECT l FROM LopTinChi l " +
		       " WHERE l.nienKhoa = :nk " +
		       "   AND l.hocKy = :hk " +
		       "   AND l.khoaQuanLy.maKhoa = :maKhoa " +
		       "   AND l.huyLop = false " +
		       " ORDER BY l.monHoc.tenMH, l.nhom")
	List<LopTinChi> findByNienKhoaHocKyAndMaKhoa(
	    @Param("nk") String nienKhoa,
	    @Param("hk") Integer hocKy,
	    @Param("maKhoa") String maKhoa);

    @Query("SELECT COUNT(d) FROM DangKy d " +
           "WHERE d.lopTinChi.maLTC = :maLTC " +
           "  AND d.huyDangKy = false")
    int countSVDaDK(@Param("maLTC") Integer maLTC);

    @Query("SELECT l FROM LopTinChi l " +
            " WHERE l.nienKhoa = :nk " +
            "   AND l.hocKy = :hk " +
            "   AND l.monHoc.maMH = :maMH " +
            "   AND l.nhom = :nhom " +
            "   AND l.giangVien.maGV = :maGV " +
            "   AND l.huyLop = false")
     Optional<LopTinChi> findByChiTiet(
             @Param("nk") String nienKhoa,
             @Param("hk") Integer hocKy,
             @Param("maMH") String maMH,
             @Param("nhom") Integer nhom,
             @Param("maGV") String maGV);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
    	       " FROM LopTinChi l " +
    	       " WHERE l.nienKhoa = :nk " +
    	       "   AND l.hocKy = :hk " +
    	       "   AND l.monHoc.maMH = :maMH " +
    	       "   AND l.nhom = :nhom " +
    	       "   AND l.giangVien.maGV = :maGV " +
    	       "   AND l.huyLop = false")
	boolean existsByChiTiet(
	    @Param("nk") String nienKhoa,
	    @Param("hk") Integer hocKy,
	    @Param("maMH") String maMH,
	    @Param("nhom") Integer nhom,
	    @Param("maGV") String maGV);
    
    @Query("SELECT l FROM LopTinChi l " +
            " WHERE l.nienKhoa = :nk " +
            "   AND l.hocKy = :hk " +
            "   AND l.monHoc.maMH = :maMH " +
            "   AND l.nhom = :nhom " +
            "   AND l.khoaQuanLy.maKhoa = :maKhoa " +
            "   AND l.huyLop = false")
     Optional<LopTinChi> findOneByNK_HK_MH_Nhom_Khoa(
             @Param("nk") String nienKhoa,
             @Param("hk") Integer hocKy,
             @Param("maMH") String maMH,
             @Param("nhom") Integer nhom,
             @Param("maKhoa") String maKhoa);
    
    @Query("SELECT l FROM LopTinChi l " +
            "WHERE l.nienKhoa = :nk " +
            "  AND l.hocKy = :hk " +
            "  AND l.huyLop = false " +
            "ORDER BY l.monHoc.tenMH, l.nhom")
     List<LopTinChi> findByNienKhoaAndHocKy(
             @Param("nk") String nienKhoa,
             @Param("hk") Integer hocKy);

}

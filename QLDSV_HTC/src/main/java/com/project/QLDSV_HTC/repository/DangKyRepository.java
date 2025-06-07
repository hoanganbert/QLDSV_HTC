package com.project.QLDSV_HTC.repository;

import com.project.QLDSV_HTC.entity.DangKy;
import com.project.QLDSV_HTC.entity.DangKyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DangKyRepository extends JpaRepository<DangKy, DangKyId> {
	@Query("SELECT d FROM DangKy d " +
		       " WHERE d.lopTinChi.maLTC = :maLTC " +
		       "   AND d.huyDangKy = :huyDangKy " +
		       " ORDER BY d.sinhVien.ho, d.sinhVien.ten")
		List<DangKy> findByMaLTCAndHuyDangKy(
		    @Param("maLTC") Integer maLTC,
		    @Param("huyDangKy") boolean huyDangKy);

	@Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
		       " FROM DangKy d " +
		       " WHERE d.lopTinChi.maLTC = :maLTC " +
		       "   AND d.sinhVien.maSV = :maSV " +
		       "   AND d.huyDangKy = :huyDangKy")
	boolean existsByMaLTCAndMaSVAndHuyDangKy(
	    @Param("maLTC") Integer maLTC,
	    @Param("maSV") String maSV,
	    @Param("huyDangKy") boolean huyDangKy);

	@Query("SELECT COUNT(d) FROM DangKy d " +
	       " WHERE d.lopTinChi.maLTC = :maLTC " +
	       "   AND d.huyDangKy = :huyDangKy")
	int countByMaLTCAndHuyDangKy(
	    @Param("maLTC") Integer maLTC,
	    @Param("huyDangKy") boolean huyDangKy);

}

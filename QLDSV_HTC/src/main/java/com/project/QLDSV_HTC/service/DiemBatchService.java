package com.project.QLDSV_HTC.service;

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.project.QLDSV_HTC.dto.DiemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.CallableStatementCallback;
import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class DiemBatchService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Gửi lên 1 batch điểm của nhiều sinh viên bằng TVP.
     *
     * @param maLTC  mã lớp tín chỉ
     * @param dsDiem danh sách DiemDTO (có getMaSV(), getDiemCC(), getDiemGK(), getDiemCK())
     */
    public void capNhatDiemBatch(Integer maLTC, List<DiemDTO> dsDiem) throws SQLException {
        // Tạo TVP 
        SQLServerDataTable tvp = new SQLServerDataTable();
        tvp.addColumnMetadata("MaLTC",   java.sql.Types.INTEGER);
        tvp.addColumnMetadata("MaSV",    java.sql.Types.NCHAR);
        tvp.addColumnMetadata("Diem_CC", java.sql.Types.INTEGER);
        tvp.addColumnMetadata("Diem_GK", java.sql.Types.FLOAT);
        tvp.addColumnMetadata("Diem_CK", java.sql.Types.FLOAT);

        for (DiemDTO dto : dsDiem) {
            tvp.addRow(
                maLTC,
                dto.getMaSV(),
                dto.getDiemCC(),
                dto.getDiemGK(),
                dto.getDiemCK()
            );
        }

        // Gọi sp_CapNhatDiemBatch bằng CallableStatement + unwrap
        final String sql = "{call dbo.sp_CapNhatDiemBatch(?, ?)}";

        jdbcTemplate.execute(
		  (CallableStatementCreator) conn -> {
		      SQLServerConnection sqlConn = conn.unwrap(SQLServerConnection.class);
		      SQLServerCallableStatement cs =
		        (SQLServerCallableStatement) sqlConn.prepareCall(sql);
		      cs.setInt(1, maLTC);
		      cs.setStructured(2, "dbo.DiemTableType", tvp);
		      return cs;
		  },
		  (CallableStatementCallback<Void>) cs -> {
		      cs.execute();
		      return null;
		  }
		);
    }
}

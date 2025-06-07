package com.project.QLDSV_HTC.service;

import com.project.QLDSV_HTC.dto.DiemDTO;
import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.List;

@Service
public class DiemBatchService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Gọi stored procedure sp_CapNhatDiemBangBatch với một TVP (DiemTableType).
     */
    public void capNhatDiemBatch(Integer maLTC, List<DiemDTO> dsDiem) throws Exception {
        // 1) Chuẩn bị SQLServerDataTable (TVP)
        SQLServerDataTable tvp = new SQLServerDataTable();
        tvp.addColumnMetadata("MALTC", Types.INTEGER);
        tvp.addColumnMetadata("MASV", Types.NCHAR);
        tvp.addColumnMetadata("DIEM_CC", Types.INTEGER);
        tvp.addColumnMetadata("DIEM_GK", Types.FLOAT);
        tvp.addColumnMetadata("DIEM_CK", Types.FLOAT);

        for (DiemDTO dto : dsDiem) {
            Integer diemCC = dto.getDiemCC() == null ? 0 : dto.getDiemCC();
            Double diemGK  = dto.getDiemGK()  == null ? 0.0 : dto.getDiemGK();
            Double diemCK  = dto.getDiemCK()  == null ? 0.0 : dto.getDiemCK();
            tvp.addRow(maLTC, dto.getMaSV(), diemCC, diemGK, diemCK);
        }

        // 2) Gọi stored procedure
        String sql = "{call sp_CapNhatDiemBangBatch(?)}";

        jdbcTemplate.execute((java.sql.Connection connection) -> {
            // a) Bóc unwrap ra SQLServerConnection của driver SQL Server
            SQLServerConnection sqlServerConn = connection.unwrap(SQLServerConnection.class);

            // b) Tạo CallableStatement từ SQLServerConnection
            SQLServerCallableStatement cs = (SQLServerCallableStatement) sqlServerConn.prepareCall(sql);

            // c) Gắn TVP vào vị trí tham số
            cs.setStructured(1, "DiemTableType", tvp);

            return cs; // phải trả về cs để JdbcTemplate biết “mình đã hoàn thành”
        });
    }
}

// File: src/main/java/com/project/QLDSV_HTC/service/BackupRestoreService.java
package com.project.QLDSV_HTC.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * BackupRestoreService có thể được dùng nếu muốn tách logic sao lưu / phục hồi
 * ra khỏi controller. Tuy nhiên trong ví dụ, controller trực tiếp gọi JdbcTemplate.
 */
@Service
public class BackupRestoreService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createDevice(String dbName, String deviceName, String physicalPath) {
        String sql = "EXEC sp_addumpdevice 'disk', ?, ?";
        jdbcTemplate.update(sql, deviceName, physicalPath);
    }

    public void backupDatabase(String dbName, String deviceName) {
        String tSql = "BACKUP DATABASE [" + dbName + "] TO " + deviceName
                + " WITH INIT, NAME = 'Full Backup " + dbName + " on " + java.time.LocalDateTime.now() + "'";
        jdbcTemplate.execute(tSql);
    }

    public void restoreDatabaseFull(String dbName, String deviceName, Integer backupSetId) {
        jdbcTemplate.execute("ALTER DATABASE [" + dbName + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
        String tSql = "RESTORE DATABASE [" + dbName + "] FROM " + deviceName
                + " WITH FILE = " + backupSetId + ", REPLACE";
        jdbcTemplate.execute(tSql);
        jdbcTemplate.execute("ALTER DATABASE [" + dbName + "] SET MULTI_USER");
    }

    public void restoreDatabasePointInTime(String dbName, String deviceName,
                                           Integer backupSetId, String stopAt) {
        jdbcTemplate.execute("ALTER DATABASE [" + dbName + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
        String t1 = "RESTORE DATABASE [" + dbName + "] FROM " + deviceName
                + " WITH FILE = " + backupSetId + ", NORECOVERY";
        jdbcTemplate.execute(t1);
        String t2 = "RESTORE LOG [" + dbName + "] FROM " + deviceName
                + " WITH STOPAT = '" + stopAt + "', RECOVERY";
        jdbcTemplate.execute(t2);
        jdbcTemplate.execute("ALTER DATABASE [" + dbName + "] SET MULTI_USER");
    }
}

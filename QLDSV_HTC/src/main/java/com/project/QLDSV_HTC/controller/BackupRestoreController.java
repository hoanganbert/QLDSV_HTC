package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class BackupRestoreController {

    @FXML private ComboBox<String> cboDatabases;
    @FXML private Button btnCreateDevice;
    @FXML private Button btnBackup;
    @FXML private Button btnRestore;
    @FXML private TableView<BackupInfo> tableBackups;
    @FXML private TableColumn<BackupInfo, Integer> colBackupSet;
    @FXML private TableColumn<BackupInfo, String> colMediaName;
    @FXML private TableColumn<BackupInfo, String> colBackupDateTime;
    @FXML private TableColumn<BackupInfo, String> colUserName;
    @FXML private CheckBox chkPointInTime;
    @FXML private DatePicker dpRestoreDate;
    @FXML private Spinner<Integer> spRestoreHour;
    @FXML private Spinner<Integer> spRestoreMinute;
    @FXML private Button btnClose;

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AppContextHolder appContext;

    private ObservableList<BackupInfo> dsBackups = FXCollections.observableArrayList();

    private String currentDevice = null;   // Tên device hiện tại (DEVICE_<DB>)

    @FXML
    public void initialize() {
        // Chỉ PGV mới được truy cập
        if (!"PGV".equals(appContext.getRole())) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Chỉ PGV mới có quyền sao lưu/phục hồi CSDL.", Alert.AlertType.WARNING);
            return;
        }

        // Load danh sách Databases (không bao gồm system DB)
        List<String> dbs = jdbcTemplate.queryForList(
            "SELECT name FROM sys.databases WHERE database_id > 4", String.class);
        cboDatabases.setItems(FXCollections.observableArrayList(dbs));
        cboDatabases.getSelectionModel().selectedItemProperty().addListener((obs, old, newDB) -> {
            if (newDB != null) {
                loadBackupSets(newDB);
                currentDevice = "DEVICE_" + newDB;
            }
        });

        // Cấu hình TableView
        colBackupSet.setCellValueFactory(new PropertyValueFactory<>("backupSetId"));
        colMediaName.setCellValueFactory(new PropertyValueFactory<>("mediaName"));
        colBackupDateTime.setCellValueFactory(new PropertyValueFactory<>("backupDateTime"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        tableBackups.setItems(dsBackups);

        // Tạo Device sao lưu
        btnCreateDevice.setOnAction(e -> createDevice());

        // Sao lưu
        btnBackup.setOnAction(e -> backupDatabase());

        // Phục hồi
        btnRestore.setOnAction(e -> restoreDatabase());

        // Point-in-time restore
        chkPointInTime.selectedProperty().addListener((o, old, newVal) -> {
            dpRestoreDate.setDisable(!newVal);
            spRestoreHour.setDisable(!newVal);
            spRestoreMinute.setDisable(!newVal);
        });
        // Cấu hình spinner
        SpinnerValueFactory<Integer> hours =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
        SpinnerValueFactory<Integer> minutes =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        spRestoreHour.setValueFactory(hours);
        spRestoreMinute.setValueFactory(minutes);

        // Thoát
        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void createDevice() {
        String dbName = cboDatabases.getValue();
        if (dbName == null) {
            showAlert("Thông báo", "Chọn cơ sở dữ liệu trước.", Alert.AlertType.WARNING);
            return;
        }
        String deviceName = "DEVICE_" + dbName;
        // Kiểm tra device đã tồn tại?
        String sqlCheck = "SELECT COUNT(*) FROM sys.backup_devices WHERE name = ?";
        Integer count = jdbcTemplate.queryForObject(sqlCheck, new Object[]{deviceName}, Integer.class);
        if (count != null && count > 0) {
            showAlert("Thông báo", "Device '" + deviceName + "' đã tồn tại.", Alert.AlertType.INFORMATION);
            return;
        }
        // Tạo device
        String tSql = "EXEC sp_addumpdevice 'disk', ?, ?";
        String physicalName = "C:\\Backup\\" + deviceName + ".bak";
        jdbcTemplate.update(tSql, deviceName, physicalName);
        showAlert("Thành công", "Đã tạo device: " + deviceName + "\nFile vật lý: " + physicalName,
                  Alert.AlertType.INFORMATION);
    }

    private void backupDatabase() {
        String dbName = cboDatabases.getValue();
        if (dbName == null) {
            showAlert("Thông báo", "Chọn cơ sở dữ liệu trước.", Alert.AlertType.WARNING);
            return;
        }
        String deviceName = "DEVICE_" + dbName;
        Integer cnt = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys.backup_devices WHERE name = ?", new Object[]{deviceName}, Integer.class);
        if (cnt == null || cnt == 0) {
            showAlert("Lỗi", "Chưa có device '" + deviceName + "'. Vui lòng Tạo Device trước.", Alert.AlertType.ERROR);
            return;
        }
        String tSql = "BACKUP DATABASE [" + dbName + "] TO " + deviceName +
                      " WITH INIT, NAME = 'Full Backup " + dbName + " on " +
                      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'";
        try {
            jdbcTemplate.execute(tSql);
            showAlert("Thành công", "Đã sao lưu CSDL '" + dbName + "' vào Device '" + deviceName + "'.", Alert.AlertType.INFORMATION);
            loadBackupSets(dbName);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Sao lưu thất bại: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void restoreDatabase() {
        String dbName = cboDatabases.getValue();
        if (dbName == null) {
            showAlert("Thông báo", "Chọn cơ sở dữ liệu trước.", Alert.AlertType.WARNING);
            return;
        }
        BackupInfo sel = tableBackups.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Thông báo", "Chọn 1 bản sao lưu để phục hồi.", Alert.AlertType.WARNING);
            return;
        }

        boolean pointInTime = chkPointInTime.isSelected();
        String deviceName = "DEVICE_" + dbName;

        try {
            // Chuyển sang SINGLE_USER
            jdbcTemplate.execute("ALTER DATABASE [" + dbName + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE");

            if (!pointInTime) {
                // Restore full backup (overwrite)
                String tSql = "RESTORE DATABASE [" + dbName + "] FROM " + deviceName +
                              " WITH FILE = " + sel.getBackupSetId() + ", REPLACE";
                jdbcTemplate.execute(tSql);
            } else {
                LocalDate date = dpRestoreDate.getValue();
                Integer hour = spRestoreHour.getValue();
                Integer minute = spRestoreMinute.getValue();
                if (date == null || hour == null || minute == null) {
                    showAlert("Lỗi", "Chọn ngày giờ phục hồi.", Alert.AlertType.ERROR);
                    return;
                }
                String restoreDateTime = date + " " + String.format("%02d:%02d:00", hour, minute);
                // Restore full backup (NORECOVERY)
                String t1 = "RESTORE DATABASE [" + dbName + "] FROM " + deviceName +
                            " WITH FILE = " + sel.getBackupSetId() + ", NORECOVERY";
                jdbcTemplate.execute(t1);
                // Restore log đến thời điểm
                String t2 = "RESTORE LOG [" + dbName + "] FROM " + deviceName +
                            " WITH STOPAT = '" + restoreDateTime + "', RECOVERY";
                jdbcTemplate.execute(t2);
            }
            // Đặt lại MULTI_USER
            jdbcTemplate.execute("ALTER DATABASE [" + dbName + "] SET MULTI_USER");
            showAlert("Thành công", "Đã phục hồi CSDL '" + dbName + "'.", Alert.AlertType.INFORMATION);
            loadBackupSets(dbName);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Phục hồi thất bại: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadBackupSets(String dbName) {
        dsBackups.clear();
        String sql = "EXEC msdb.dbo.sp_help_backupset @database_name = ?";
        List<BackupInfo> list = jdbcTemplate.query(
            sql,
            new Object[]{dbName},
            (ResultSet rs, int rowNum) -> {
                BackupInfo bi = new BackupInfo();
                bi.setBackupSetId(rs.getInt("backup_set_id"));
                bi.setDatabaseName(rs.getString("database_name"));
                bi.setBackupDateTime(rs.getString("backup_finish_date"));
                bi.setUserName(rs.getString("user_name"));
                bi.setMediaName(rs.getString("media_description"));
                return bi;
            });
        dsBackups.addAll(list);
    }

    private void disableForm() {
        cboDatabases.setDisable(true);
        btnCreateDevice.setDisable(true);
        btnBackup.setDisable(true);
        btnRestore.setDisable(true);
        chkPointInTime.setDisable(true);
        dpRestoreDate.setDisable(true);
        spRestoreHour.setDisable(true);
        spRestoreMinute.setDisable(true);
        btnClose.setText("Đóng");
        tableBackups.setDisable(true);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // Inner class để hiển thị thông tin backup
    public static class BackupInfo {
        private Integer backupSetId;
        private String databaseName;
        private String mediaName;
        private String backupDateTime;
        private String userName;

        public Integer getBackupSetId() { return backupSetId; }
        public void setBackupSetId(Integer backupSetId) { this.backupSetId = backupSetId; }
        public String getDatabaseName() { return databaseName; }
        public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
        public String getMediaName() { return mediaName; }
        public void setMediaName(String mediaName) { this.mediaName = mediaName; }
        public String getBackupDateTime() { return backupDateTime; }
        public void setBackupDateTime(String backupDateTime) { this.backupDateTime = backupDateTime; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }
}

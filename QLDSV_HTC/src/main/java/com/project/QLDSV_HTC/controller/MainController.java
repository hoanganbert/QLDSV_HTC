package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.SpringContext;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainController {

    @FXML private MenuItem menuKhoa;
    @FXML private MenuItem menuLop;
    @FXML private MenuItem menuSinhVien;
    @FXML private MenuItem menuMonHoc;
    @FXML private MenuItem menuGiangVien;
    @FXML private MenuItem menuLTC;
    @FXML private MenuItem menuNhapDiem;
    @FXML private MenuItem menuDangKy;

    @FXML private MenuItem menuBaoCao1;
    @FXML private MenuItem menuBaoCao2;
    @FXML private MenuItem menuBaoCao3;
    @FXML private MenuItem menuBaoCao4;
    @FXML private MenuItem menuBaoCao5;
    @FXML private MenuItem menuBaoCao6;

    @FXML private MenuItem menuUser;
    @FXML private MenuItem menuBackup;
    @FXML private MenuItem menuLogout;

    private final AppContextHolder appContext;

    public MainController(AppContextHolder appContext) {
        this.appContext = appContext;
    }

    @FXML
    public void initialize() {
        String role = appContext.getRole();

        // ======= Phân quyền cho mục QUẢN LÝ =======
        if ("PGV".equals(role)) {
            // PGV: được phép xem tất cả
        }
        else if ("KHOA".equals(role)) {
            // KHOA: không cho nhập Khoa, Lớp, Giảng viên, Sinh viên, mở LTC, nhập Môn
            menuKhoa.setDisable(true);
            menuLop.setDisable(true);
            menuSinhVien.setDisable(true);
            menuGiangVien.setDisable(true);
            menuLTC.setDisable(true);
            menuMonHoc.setDisable(true);
        }
        else if ("GIANGVIEN".equals(role)) {
            // Giảng viên: chỉ được nhập điểm (nếu có) và xem báo cáo liên quan
            menuKhoa.setDisable(true);
            menuLop.setDisable(true);
            menuSinhVien.setDisable(true);
            menuMonHoc.setDisable(true);
            menuGiangVien.setDisable(true);
            menuLTC.setDisable(true);
            menuDangKy.setDisable(true);
            menuBaoCao1.setDisable(true);
            menuBaoCao2.setDisable(true);
            menuBaoCao3.setDisable(true);
            menuBaoCao4.setDisable(true);
            menuBaoCao5.setDisable(true);
            menuBaoCao6.setDisable(true);
            // Giảng viên có thể nhập điểm
            menuNhapDiem.setDisable(false);
        }
        else if ("SINHVIEN".equals(role)) {
            // SV: chỉ được Đăng ký LTC và xem Phiếu điểm
            menuKhoa.setDisable(true);
            menuLop.setDisable(true);
            menuSinhVien.setDisable(true);
            menuMonHoc.setDisable(true);
            menuGiangVien.setDisable(true);
            menuLTC.setDisable(true);
            menuNhapDiem.setDisable(true);
            menuBaoCao1.setDisable(true);
            menuBaoCao2.setDisable(false); // Xem DS SV ĐK
            menuBaoCao3.setDisable(true);
            menuBaoCao4.setDisable(false); // Xem Phiếu điểm
            menuBaoCao5.setDisable(true);
            menuBaoCao6.setDisable(true);
            menuUser.setDisable(true);
            menuBackup.setDisable(true);
        }

        // ======= Sự kiện cho từng menu =======
        menuKhoa.setOnAction(e -> openForm("/fxml/KhoaForm.fxml", "Quản lý Khoa"));
        menuLop.setOnAction(e -> openForm("/fxml/LopForm.fxml", "Quản lý Lớp"));
        menuSinhVien.setOnAction(e -> openForm("/fxml/SinhVienForm.fxml", "Quản lý Sinh viên"));
        menuMonHoc.setOnAction(e -> openForm("/fxml/MonHocForm.fxml", "Quản lý Môn học"));
        menuGiangVien.setOnAction(e -> openForm("/fxml/GiangVienForm.fxml", "Quản lý Giảng viên"));
        menuLTC.setOnAction(e -> openForm("/fxml/LopTinChiForm.fxml", "Mở Lớp Tín chỉ"));
        menuNhapDiem.setOnAction(e -> openForm("/fxml/NhapDiemForm.fxml", "Nhập Điểm"));
        menuDangKy.setOnAction(e -> openForm("/fxml/DangKyForm.fxml", "Đăng ký Lớp Tín chỉ"));

        menuBaoCao1.setOnAction(e -> openForm("/fxml/BaoCaoLTCForm.fxml", "Báo cáo DS LTC"));
        menuBaoCao2.setOnAction(e -> openForm("/fxml/BaoCaoDKSVForm.fxml", "Báo cáo DS SV ĐK"));
        menuBaoCao3.setOnAction(e -> openForm("/fxml/BaoCaoBDLTCForm.fxml", "Báo cáo Bảng Điểm LTC"));
        menuBaoCao4.setOnAction(e -> openForm("/fxml/BaoCaoPhieuDiemSVForm.fxml", "Báo cáo Phiếu Điểm SV"));
        menuBaoCao5.setOnAction(e -> openForm("/fxml/BaoCaoDongHPForm.fxml", "Báo cáo Đóng Học Phí"));
        menuBaoCao6.setOnAction(e -> openForm("/fxml/BaoCaoBDTOForm.fxml", "Báo cáo Bảng Điểm Tổng kết"));

        menuUser.setOnAction(e -> openForm("/fxml/UserForm.fxml", "Quản lý Tài khoản"));
        menuBackup.setOnAction(e -> openForm("/fxml/BackupRestoreForm.fxml", "Sao lưu / Phục hồi CSDL"));

        menuLogout.setOnAction(e -> {
            // Đóng MainForm và trở về Login
            ((Stage) menuLogout.getParentPopup().getOwnerWindow()).close();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginForm.fxml"));
                loader.setControllerFactory(param -> SpringContext.getBean(param));
                Stage stage = new Stage();
                stage.setTitle("Đăng nhập Hệ thống");
                stage.setScene(new Scene(loader.load()));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void openForm(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(param -> SpringContext.getBean(param));
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

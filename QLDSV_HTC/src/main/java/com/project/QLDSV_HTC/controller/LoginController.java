package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.SpringContext;
import com.project.QLDSV_HTC.entity.User;
import com.project.QLDSV_HTC.service.GiangVienService;
import com.project.QLDSV_HTC.service.SinhVienService;
import com.project.QLDSV_HTC.service.UserService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
public class LoginController {

    @FXML private ComboBox<String> cboRole;
    @FXML private Label lblLogin;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtMaSV;
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;

    @Autowired private UserService userService;
    @Autowired private SinhVienService sinhVienService;
    @Autowired private GiangVienService giangVienService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // 1. Khởi tạo ComboBox Role
        cboRole.getItems().addAll(Arrays.asList("Giảng viên", "Sinh viên"));
        cboRole.getSelectionModel().selectFirst();

        // 2. Thiết lập listener để thay đổi giao diện tùy role
        cboRole.getSelectionModel().selectedItemProperty().addListener((obs, old, newRole) -> {
            if ("Sinh viên".equals(newRole)) {
                // Ẩn txtLogin, đổi lblLogin thành cố định "Login (sv):"
                lblLogin.setText("Ma SV:");
                txtLogin.setVisible(false);
                txtLogin.setManaged(false);

                // Hiển thị ô Mã SV
                txtMaSV.setVisible(true);
                txtMaSV.setManaged(true);
            } else {
                // Giảng viên: hiện txtLogin, ẩn txtMaSV
                lblLogin.setText("Login:");
                txtLogin.setVisible(true);
                txtLogin.setManaged(true);

                txtMaSV.clear();
                txtMaSV.setVisible(false);
                txtMaSV.setManaged(false);
            }
            lblMessage.setText("");
        });

        // 3. Nút Đăng nhập
        btnLogin.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String role = cboRole.getValue();
        String password = txtPassword.getText().trim();

        if (role == null) {
            lblMessage.setText("Phải chọn vai trò (Giảng viên hoặc Sinh viên).");
            return;
        }
        if (password.isEmpty()) {
            lblMessage.setText("Mật khẩu không được để trống.");
            return;
        }

        if ("Giảng viên".equals(role)) {
            String username = txtLogin.getText().trim();
            if (username.isEmpty()) {
                lblMessage.setText("Hãy nhập Login của Giảng viên.");
                return;
            }
            // Lấy user từ DB
            User user = userService.findByUsername(username);
            if (user == null
                || !user.getPassword().equals(password)
                || !"GIANGVIEN".equals(user.getRole())) {
                lblMessage.setText("Login hoặc mật khẩu không đúng, hoặc không phải Giảng viên.");
                return;
            }

            // --- ĐẶT QUYỀN ĐẶT BIỆT CHO MỘT SỐ GV ---
            String maGV = user.getMaGV().trim();
            if ("GV03".equals(maGV)) {
                // GV03 có quyền như PGV
                appContext.setRole("PGV");
                appContext.setMaKhoa(null);
                appContext.setMaGV(null);
            }
            else if ("GV04".equals(maGV)) {
                // GV04 có quyền như KHOA
                appContext.setRole("KHOA");
                // nạp mã khoa từ bảng User (user.getMaKhoa()) hoặc hard-code nếu muốn
                appContext.setMaKhoa(user.getMaKhoa());
                appContext.setMaGV(null);
            }
            else {
                // Các GV khác vẫn là GIANGVIEN bình thường
                appContext.setRole("GIANGVIEN");
                appContext.setMaGV(maGV);
            }

            // Tiếp tục mở MainForm
            openMainWindow();
        }

        else { // Sinh viên
            // Sinh viên login chung "sv"
            User userSV = userService.findByUsername("sv");
            if (userSV == null || !userSV.getPassword().equals(password) || !"SINHVIEN".equals(userSV.getRole())) {
                lblMessage.setText("Sai mật khẩu dành cho Sinh viên.");
                return;
            }
            // Bây giờ SV phải nhập Mã SV thực sự
            String maSV = txtMaSV.getText().trim();
            if (maSV.isEmpty()) {
                lblMessage.setText("Hãy nhập mã SV của bạn.");
                return;
            }
            // Kiểm tra trong bảng Sinhvien
            if (!sinhVienService.existsById(maSV)) {
                lblMessage.setText("Mã SV không tồn tại hoặc SV chưa đăng ký.");
                return;
            }
            appContext.setRole("SINHVIEN");
            appContext.setMaSV(maSV);

            // Chuyển thẳng vào form Đăng ký lớp tín chỉ
            openDangKyWindow();
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainForm.fxml"));
            loader.setControllerFactory(param -> SpringContext.getBean(param));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Quản lý Đồ án QLDSV_HTC");
            stage.setScene(new Scene(root));
            // Đóng login
            ((Stage) btnLogin.getScene().getWindow()).close();
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void openDangKyWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DangKyForm.fxml"));
            loader.setControllerFactory(param -> SpringContext.getBean(param));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Đăng ký Lớp Tín chỉ");
            stage.setScene(new Scene(root));
            ((Stage) btnLogin.getScene().getWindow()).close();
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

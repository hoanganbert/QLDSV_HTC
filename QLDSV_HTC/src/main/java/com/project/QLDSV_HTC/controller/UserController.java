package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.entity.GiangVien;
import com.project.QLDSV_HTC.entity.Khoa;
import com.project.QLDSV_HTC.entity.Sinhvien;
import com.project.QLDSV_HTC.entity.User;
import com.project.QLDSV_HTC.service.GiangVienService;
import com.project.QLDSV_HTC.service.KhoaService;
import com.project.QLDSV_HTC.service.SinhVienService;
import com.project.QLDSV_HTC.service.UserService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserController {

    @FXML private ComboBox<String> cboRoleSelect;
    @FXML private ComboBox<Khoa> cboKhoa;
    @FXML private ComboBox<GiangVien> cboGV;
    @FXML private ComboBox<Sinhvien> cboSV;
    @FXML private TextField txtMaUser;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnAddUser;
    @FXML private Button btnUpdateUser;   
    @FXML private Button btnDeleteUser;
    @FXML private Button btnClose;
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colMaKhoa;
    @FXML private TableColumn<User, String> colMaGV;
    @FXML private TableColumn<User, String> colMaSV;

    private ObservableList<User> dsUsers = FXCollections.observableArrayList();
    private ObservableList<String> dsRoles = FXCollections.observableArrayList("PGV", "KHOA", "GIANGVIEN", "SINHVIEN");

    @Autowired private UserService userService;
    @Autowired private KhoaService khoaService;
    @Autowired private GiangVienService giangVienService;
    @Autowired private SinhVienService sinhVienService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // 1. Phân quyền: chỉ PGV mới được vào form
        if (!"PGV".equals(appContext.getRole())) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Chỉ PGV mới có quyền quản lý tài khoản.", Alert.AlertType.WARNING);
            return;
        }

        // 2. Load role select
        cboRoleSelect.setItems(dsRoles);
        cboRoleSelect.getSelectionModel().selectFirst();

        // 3. Khi chọn Role, show/hide 3 ComboBox và clear dữ liệu cũ
        cboRoleSelect.getSelectionModel().selectedItemProperty().addListener((obs, old, newRole) -> {
            cboKhoa.getSelectionModel().clearSelection();
            cboGV.getSelectionModel().clearSelection();
            cboSV.getSelectionModel().clearSelection();
            txtMaUser.clear();

            switch (newRole) {
                case "PGV":
                    cboKhoa.setVisible(false);
                    cboKhoa.setManaged(false);
                    cboGV.setVisible(false);
                    cboGV.setManaged(false);
                    cboSV.setVisible(false);
                    cboSV.setManaged(false);
                    break;
                case "KHOA":
                    cboKhoa.setVisible(true);
                    cboKhoa.setManaged(true);
                    cboGV.setVisible(false);
                    cboGV.setManaged(false);
                    cboSV.setVisible(false);
                    cboSV.setManaged(false);
                    break;
                case "GIANGVIEN":
                    cboKhoa.setVisible(false);
                    cboKhoa.setManaged(false);
                    cboGV.setVisible(true);
                    cboGV.setManaged(true);
                    cboSV.setVisible(false);
                    cboSV.setManaged(false);
                    break;
                case "SINHVIEN":
                    cboKhoa.setVisible(false);
                    cboKhoa.setManaged(false);
                    cboGV.setVisible(false);
                    cboGV.setManaged(false);
                    cboSV.setVisible(true);
                    cboSV.setManaged(true);
                    break;
            }
        });

        // 4. Load dữ liệu cho cboKhoa, cboGV, cboSV
        cboKhoa.setItems(FXCollections.observableArrayList(khoaService.getAllKhoa()));
        cboGV.setItems(FXCollections.observableArrayList(giangVienService.getAllGiangVien()));
        cboSV.setItems(FXCollections.observableArrayList(sinhVienService.getAllSV()));

        // 5. Khi chọn 1 Khoa/GV/SV, tự động điền txtMaUser
        cboKhoa.getSelectionModel().selectedItemProperty().addListener((o, old, newKhoa) -> {
            if (newKhoa != null) {
                txtMaUser.setText(newKhoa.getMaKhoa());
            }
        });
        cboGV.getSelectionModel().selectedItemProperty().addListener((o, old, newGV) -> {
            if (newGV != null) {
                txtMaUser.setText(newGV.getMaGV());
            }
        });
        cboSV.getSelectionModel().selectedItemProperty().addListener((o, old, newSV) -> {
            if (newSV != null) {
                txtMaUser.setText(newSV.getMaSV());
            }
        });

        // 6. Cấu hình TableView User
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colMaKhoa.setCellValueFactory(new PropertyValueFactory<>("maKhoa"));
        colMaGV.setCellValueFactory(new PropertyValueFactory<>("maGV"));
        colMaSV.setCellValueFactory(new PropertyValueFactory<>("maSV"));
        tableUsers.setItems(dsUsers);

        // 7. Load danh sách user từ DB
        loadUsers();

        // 8. Khi chọn 1 dòng trong table, đổ lên form (có set txtMaUser)
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, newUser) -> {
            if (newUser != null) {
                // Set username và password
                txtUsername.setText(newUser.getUsername());
                txtPassword.setText(newUser.getPassword());

                // Set role
                cboRoleSelect.getSelectionModel().select(newUser.getRole());

                // Clear các selection trước
                cboKhoa.getSelectionModel().clearSelection();
                cboGV.getSelectionModel().clearSelection();
                cboSV.getSelectionModel().clearSelection();
                txtMaUser.clear();

                switch (newUser.getRole()) {
                    case "KHOA":
                        String mk = newUser.getMaKhoa();
                        txtMaUser.setText(mk);
                        khoaService.getAllKhoa().stream()
                            .filter(k -> k.getMaKhoa().equals(mk))
                            .findFirst()
                            .ifPresent(k -> cboKhoa.getSelectionModel().select(k));
                        break;

                    case "GIANGVIEN":
                        String mgv = newUser.getMaGV();
                        txtMaUser.setText(mgv);
                        giangVienService.getAllGiangVien().stream()
                            .filter(g -> g.getMaGV().equals(mgv))
                            .findFirst()
                            .ifPresent(g -> cboGV.getSelectionModel().select(g));
                        break;

                    case "SINHVIEN":
                        String msv = newUser.getMaSV();
                        txtMaUser.setText(msv);
                        sinhVienService.getAllSV().stream()
                            .filter(sv -> sv.getMaSV().equals(msv))
                            .findFirst()
                            .ifPresent(sv -> cboSV.getSelectionModel().select(sv));
                        break;

                    default: // PGV
                        // txtMaUser đã clear
                        break;
                }
            }
        });

        // 9. Tạo tài khoản mới
        btnAddUser.setOnAction(e -> {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();
            String role = cboRoleSelect.getValue();
            String maK = null, maG = null, maS = null;

            if (username.isEmpty() || password.isEmpty() || role == null) {
                showAlert("Lỗi", "Phải nhập đầy đủ Username, Password, Role.", Alert.AlertType.ERROR);
                return;
            }
            switch (role) {
                case "KHOA":
                    Khoa k = cboKhoa.getSelectionModel().getSelectedItem();
                    if (k == null) {
                        showAlert("Lỗi", "Chọn Khoa cho tài khoản KHOA.", Alert.AlertType.ERROR);
                        return;
                    }
                    maK = k.getMaKhoa();
                    break;
                case "GIANGVIEN":
                    GiangVien gv = cboGV.getSelectionModel().getSelectedItem();
                    if (gv == null) {
                        showAlert("Lỗi", "Chọn Giảng viên cho tài khoản GV.", Alert.AlertType.ERROR);
                        return;
                    }
                    maG = gv.getMaGV();
                    break;
                case "SINHVIEN":
                    Sinhvien sv = cboSV.getSelectionModel().getSelectedItem();
                    if (sv == null) {
                        showAlert("Lỗi", "Chọn Sinh viên cho tài khoản SV.", Alert.AlertType.ERROR);
                        return;
                    }
                    maS = sv.getMaSV();
                    break;
                case "PGV":
                    // Không cần mã gì thêm
                    break;
            }

            if (userService.existsByUsername(username)) {
                showAlert("Lỗi", "Username đã tồn tại.", Alert.AlertType.WARNING);
                return;
            }
            User u = new User();
            u.setUsername(username);
            u.setPassword(password);
            u.setRole(role);
            u.setMaKhoa(maK);
            u.setMaGV(maG);
            u.setMaSV(maS);
            userService.save(u);

            loadUsers();
            clearForm();
        });

        // 10. Cập nhật tài khoản (nút “Update” mới thêm)
        btnUpdateUser.setOnAction(e -> {
            User sel = tableUsers.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn tài khoản cần sửa.", Alert.AlertType.WARNING);
                return;
            }
            String password = txtPassword.getText().trim();
            String role = cboRoleSelect.getValue();
            String maK = null, maG = null, maS = null;
            if (role == null) {
                showAlert("Lỗi", "Chọn Role trước khi cập nhật.", Alert.AlertType.ERROR);
                return;
            }
            switch (role) {
                case "KHOA":
                    Khoa k = cboKhoa.getSelectionModel().getSelectedItem();
                    if (k == null) {
                        showAlert("Lỗi", "Chọn Khoa cho tài khoản KHOA.", Alert.AlertType.ERROR);
                        return;
                    }
                    maK = k.getMaKhoa();
                    break;
                case "GIANGVIEN":
                    GiangVien gv = cboGV.getSelectionModel().getSelectedItem();
                    if (gv == null) {
                        showAlert("Lỗi", "Chọn Giảng viên cho tài khoản GV.", Alert.AlertType.ERROR);
                        return;
                    }
                    maG = gv.getMaGV();
                    break;
                case "SINHVIEN":
                    Sinhvien sv = cboSV.getSelectionModel().getSelectedItem();
                    if (sv == null) {
                        showAlert("Lỗi", "Chọn Sinh viên cho tài khoản SV.", Alert.AlertType.ERROR);
                        return;
                    }
                    maS = sv.getMaSV();
                    break;
                case "PGV":
                    break;
            }
            sel.setPassword(password);
            sel.setRole(role);
            sel.setMaKhoa(maK);
            sel.setMaGV(maG);
            sel.setMaSV(maS);
            userService.save(sel);

            loadUsers();
            clearForm();
        });

        // 11. Xóa tài khoản
        btnDeleteUser.setOnAction(e -> {
            User sel = tableUsers.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn tài khoản cần xóa.", Alert.AlertType.WARNING);
                return;
            }
            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.setTitle("Xác nhận");
            c.setContentText("Bạn có chắc muốn xóa tài khoản '" + sel.getUsername() + "'?");
            if (c.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
                userService.delete(sel.getUsername());
                loadUsers();
                clearForm();
            }
        });

        // 12. Thoát
        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void loadUsers() {
        dsUsers.setAll(userService.findAll());
    }

    private void clearForm() {
        txtUsername.clear();
        txtPassword.clear();
        cboRoleSelect.getSelectionModel().selectFirst();
        cboKhoa.getSelectionModel().clearSelection();
        cboGV.getSelectionModel().clearSelection();
        cboSV.getSelectionModel().clearSelection();
        txtMaUser.clear();
    }

    private void disableForm() {
        txtUsername.setDisable(true);
        txtPassword.setDisable(true);
        cboRoleSelect.setDisable(true);
        cboKhoa.setDisable(true);
        cboGV.setDisable(true);
        cboSV.setDisable(true);
        btnAddUser.setDisable(true);
        btnUpdateUser.setDisable(true);
        btnDeleteUser.setDisable(true);
        btnClose.setText("Đóng");
        tableUsers.setDisable(true);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

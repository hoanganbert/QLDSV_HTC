package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.entity.Khoa;
import com.project.QLDSV_HTC.service.KhoaService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KhoaController {

    @FXML private TextField txtMaKhoa;
    @FXML private TextField txtTenKhoa;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnClose;

    @FXML private TableView<Khoa> tableKhoa;
    @FXML private TableColumn<Khoa, String> colMaKhoa;
    @FXML private TableColumn<Khoa, String> colTenKhoa;

    private ObservableList<Khoa> dsKhoa = FXCollections.observableArrayList();

    @Autowired private KhoaService khoaService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // Chỉ PGV mới có quyền quản lý Khoa
        if (!"PGV".equals(appContext.getRole())) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Chỉ PGV mới có quyền quản lý Khoa.", Alert.AlertType.WARNING);
            return;
        }

        // Cấu hình TableView
        colMaKhoa.setCellValueFactory(new PropertyValueFactory<>("maKhoa"));
        colTenKhoa.setCellValueFactory(new PropertyValueFactory<>("tenKhoa"));

        // Load dữ liệu
        loadTableKhoa();

        // Khi chọn 1 dòng trong table, đổ dữ liệu lên form
        tableKhoa.getSelectionModel().selectedItemProperty().addListener((obs, old, newKhoa) -> {
            if (newKhoa != null) {
                txtMaKhoa.setText(newKhoa.getMaKhoa().trim());
                txtTenKhoa.setText(newKhoa.getTenKhoa());
                txtMaKhoa.setDisable(true);
            }
        });

        // Nút Thêm
        btnAdd.setOnAction(e -> {
            String ma = txtMaKhoa.getText().trim();
            String ten = txtTenKhoa.getText().trim();
            if (ma.isEmpty() || ten.isEmpty()) {
                showAlert("Lỗi", "Phải nhập đầy đủ Mã Khoa và Tên Khoa.", Alert.AlertType.ERROR);
                return;
            }
            if (khoaService.existsById(ma)) {
                showAlert("Lỗi", "Mã Khoa đã tồn tại.", Alert.AlertType.WARNING);
                return;
            }
            Khoa k = new Khoa();
            k.setMaKhoa(ma);
            k.setTenKhoa(ten);
            khoaService.save(k);
            loadTableKhoa();
            clearForm();
        });

        // Nút Ghi (Update)
        btnUpdate.setOnAction(e -> {
            Khoa sel = tableKhoa.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Khoa cần sửa.", Alert.AlertType.WARNING);
                return;
            }
            String ten = txtTenKhoa.getText().trim();
            if (ten.isEmpty()) {
                showAlert("Lỗi", "Phải nhập Tên Khoa.", Alert.AlertType.ERROR);
                return;
            }
            sel.setTenKhoa(ten);
            khoaService.save(sel);
            loadTableKhoa();
            clearForm();
        });

        // Nút Xóa
        btnDelete.setOnAction(e -> {
            Khoa sel = tableKhoa.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Khoa cần xóa.", Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setContentText("Bạn có chắc muốn xóa Khoa " + sel.getMaKhoa() + "?");
            if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
                khoaService.delete(sel.getMaKhoa());
                loadTableKhoa();
                clearForm();
            }
        });

        // Nút Phục hồi (Refresh)
        btnRefresh.setOnAction(e -> {
            clearForm();
            loadTableKhoa();
        });

        // Nút Thoát
        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void loadTableKhoa() {
        List<Khoa> list = khoaService.getAllKhoa();
        dsKhoa.setAll(list);
        tableKhoa.setItems(dsKhoa);
    }

    private void clearForm() {
        txtMaKhoa.clear();
        txtTenKhoa.clear();
        txtMaKhoa.setDisable(false);
        tableKhoa.getSelectionModel().clearSelection();
    }

    private void disableForm() {
        txtMaKhoa.setDisable(true);
        txtTenKhoa.setDisable(true);
        btnAdd.setDisable(true);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnRefresh.setDisable(true);
        btnClose.setText("Đóng");
        tableKhoa.setDisable(true);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

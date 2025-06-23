package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.entity.Khoa;
import com.project.QLDSV_HTC.entity.Lop;
import com.project.QLDSV_HTC.service.KhoaService;
import com.project.QLDSV_HTC.service.LopService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Deque;
import java.util.ArrayDeque;

@Component
public class LopController {

    // NOTE: Requires a copy constructor Lop(Lop other) in the entity

    @FXML private TextField txtMaLop;
    @FXML private TextField txtTenLop;
    @FXML private TextField txtKhoaHoc;
    @FXML private ComboBox<Khoa> cboKhoa;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnClose;

    @FXML private TableView<Lop> tableLop;
    @FXML private TableColumn<Lop, String> colMaLop;
    @FXML private TableColumn<Lop, String> colTenLop;
    @FXML private TableColumn<Lop, String> colKhoaHoc;
    @FXML private TableColumn<Lop, String> colMaKhoa;

    private ObservableList<Lop> dsLop = FXCollections.observableArrayList();
    private ObservableList<Khoa> dsKhoa = FXCollections.observableArrayList();

    // Undo stack lưu trạng thái trước mỗi thao tác Update/Delete
    private Deque<Lop> undoStack = new ArrayDeque<>();

    @Autowired private LopService lopService;
    @Autowired private KhoaService khoaService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // Phân quyền: chỉ PGV và KHOA
        String role = appContext.getRole();
        if (!"PGV".equals(role) && !"KHOA".equals(role)) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Bạn không có quyền truy cập chức năng Quản lý Lớp.", Alert.AlertType.WARNING);
            return;
        }

        // Load ComboBox Khoa
        dsKhoa.clear();
        dsKhoa.addAll(khoaService.getAllKhoa());
        cboKhoa.setItems(dsKhoa);

        // Nếu role = KHOA, tự động chọn khoa login và disable
        if ("KHOA".equals(role)) {
            String maKhoaLogin = appContext.getMaKhoa();
            Khoa khoaLogin = dsKhoa.stream()
                    .filter(k -> k.getMaKhoa().equals(maKhoaLogin))
                    .findFirst().orElse(null);
            if (khoaLogin != null) {
                cboKhoa.getSelectionModel().select(khoaLogin);
                cboKhoa.setDisable(true);
            }
        }

        // Cấu hình TableView
        colMaLop.setCellValueFactory(new PropertyValueFactory<>("maLop"));
        colTenLop.setCellValueFactory(new PropertyValueFactory<>("tenLop"));
        colKhoaHoc.setCellValueFactory(new PropertyValueFactory<>("khoaHoc"));
        colMaKhoa.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getKhoa().getMaKhoa()));

        // Load dữ liệu
        loadTableLop();

        // Khi chọn 1 dòng trong table, đổ lên form
        tableLop.getSelectionModel().selectedItemProperty().addListener((obs, old, newLop) -> {
            if (newLop != null) bindForm(newLop);
        });

        // Nút Thêm
        btnAdd.setOnAction(e -> {
            String ma = txtMaLop.getText().trim();
            String ten = txtTenLop.getText().trim();
            String kh = txtKhoaHoc.getText().trim();
            Khoa k = cboKhoa.getSelectionModel().getSelectedItem();

            if (ma.isEmpty() || ten.isEmpty() || kh.isEmpty() || k == null) {
                showAlert("Lỗi", "Phải nhập đầy đủ: Mã Lớp, Tên Lớp, Khóa Học, Khoa.", Alert.AlertType.ERROR);
                return;
            }
            if (lopService.existsById(ma)) {
                showAlert("Lỗi", "Mã Lớp đã tồn tại.", Alert.AlertType.WARNING);
                return;
            }
            Lop lop = new Lop();
            lop.setMaLop(ma);
            lop.setTenLop(ten);
            lop.setKhoaHoc(kh);
            lop.setKhoa(k);
            lopService.save(lop);
            loadTableLop();
            clearForm();
        });

        // Nút Ghi (Update): lưu snapshot trước khi thay đổi
        btnUpdate.setOnAction(e -> {
            Lop sel = tableLop.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Lớp cần sửa.", Alert.AlertType.WARNING);
                return;
            }
            undoStack.push(new Lop(sel));
            // Áp dụng thay đổi từ form
            sel.setTenLop(txtTenLop.getText().trim());
            sel.setKhoaHoc(txtKhoaHoc.getText().trim());
            sel.setKhoa(cboKhoa.getSelectionModel().getSelectedItem());
            lopService.save(sel);
            loadTableLop();
            clearForm();
        });

        // Nút Xóa: lưu snapshot trước khi xóa
        btnDelete.setOnAction(e -> {
            Lop sel = tableLop.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Lớp cần xóa.", Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setContentText("Bạn có chắc muốn xóa Lớp " + sel.getMaLop() + "?");
            if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
                undoStack.push(new Lop(sel));
                lopService.delete(sel.getMaLop());
                loadTableLop();
                clearForm();
            }
        });

        // Nút Phục hồi (Undo)
        btnRefresh.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                Lop prev = undoStack.pop();
                bindForm(prev);
                lopService.save(prev);
                loadTableLop();
            } else {
                clearForm();
                loadTableLop();
            }
        });

        // Nút Thoát
        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void loadTableLop() {
        List<Lop> list = lopService.getAllLop();
        if ("KHOA".equals(appContext.getRole())) {
            String maKhoaLogin = appContext.getMaKhoa();
            list = list.stream()
                       .filter(l -> l.getKhoa().getMaKhoa().equals(maKhoaLogin))
                       .collect(Collectors.toList());
        }
        dsLop.setAll(list);
        tableLop.setItems(dsLop);
    }

    private void bindForm(Lop lop) {
        txtMaLop.setText(lop.getMaLop());
        txtTenLop.setText(lop.getTenLop());
        txtKhoaHoc.setText(lop.getKhoaHoc());
        cboKhoa.getSelectionModel().select(lop.getKhoa());
        txtMaLop.setDisable(true);
    }

    private void clearForm() {
        txtMaLop.clear();
        txtTenLop.clear();
        txtKhoaHoc.clear();
        if (!"KHOA".equals(appContext.getRole())) {
            cboKhoa.getSelectionModel().clearSelection();
            txtMaLop.setDisable(false);
        } else {
            txtMaLop.setDisable(false);
        }
        tableLop.getSelectionModel().clearSelection();
    }

    private void disableForm() {
        txtMaLop.setDisable(true);
        txtTenLop.setDisable(true);
        txtKhoaHoc.setDisable(true);
        cboKhoa.setDisable(true);
        btnAdd.setDisable(true);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnRefresh.setDisable(true);
        btnClose.setText("Đóng");
        tableLop.setDisable(true);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

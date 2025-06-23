package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.entity.MonHoc;
import com.project.QLDSV_HTC.service.MonHocService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;

@Component
public class MonHocController {

    @FXML private TextField txtMaMH;
    @FXML private TextField txtTenMH;
    @FXML private TextField txtSoTietLT;
    @FXML private TextField txtSoTietTH;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnThoat;

    @FXML private TableView<MonHoc> tableMH;
    @FXML private TableColumn<MonHoc, String> colMaMH;
    @FXML private TableColumn<MonHoc, String> colTenMH;
    @FXML private TableColumn<MonHoc, Integer> colSoTietLT;
    @FXML private TableColumn<MonHoc, Integer> colSoTietTH;

    private ObservableList<MonHoc> dsMH = FXCollections.observableArrayList();

    // Undo stack lưu trạng thái trước mỗi thao tác Update/Delete
    private Deque<MonHoc> undoStack = new ArrayDeque<>();

    @Autowired private MonHocService monHocService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // Chỉ PGV mới có quyền
        if (!"PGV".equals(appContext.getRole())) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Chỉ PGV mới có quyền quản lý Môn học.", Alert.AlertType.WARNING);
            return;
        }

        // Cấu hình TableView
        colMaMH.setCellValueFactory(new PropertyValueFactory<>("maMH"));
        colTenMH.setCellValueFactory(new PropertyValueFactory<>("tenMH"));
        colSoTietLT.setCellValueFactory(new PropertyValueFactory<>("soTietLT"));
        colSoTietTH.setCellValueFactory(new PropertyValueFactory<>("soTietTH"));

        loadTableMH();

        tableMH.getSelectionModel().selectedItemProperty().addListener((obs, old, newMH) -> {
            if (newMH != null) {
                bindForm(newMH);
            }
        });

        btnAdd.setOnAction(e -> addMonHoc());

        btnUpdate.setOnAction(e -> {
            MonHoc sel = tableMH.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Môn học cần sửa.", Alert.AlertType.WARNING);
                return;
            }
            // Lưu snapshot trước khi thay đổi
            undoStack.push(new MonHoc(sel));
            applyFormTo(sel);
            monHocService.save(sel);
            loadTableMH(); clearForm();
        });

        btnDelete.setOnAction(e -> {
            MonHoc sel = tableMH.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Môn học cần xóa.", Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setContentText("Bạn có chắc muốn xóa Môn học " + sel.getMaMH() + "?");
            if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
                undoStack.push(new MonHoc(sel));
                monHocService.delete(sel.getMaMH());
                loadTableMH(); clearForm();
            }
        });

        // Nút Phục hồi (Undo)
        btnRefresh.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                MonHoc prev = undoStack.pop();
                bindForm(prev);
                monHocService.save(prev);
                loadTableMH();
            } else {
                clearForm(); loadTableMH();
            }
        });

        btnThoat.setOnAction(e -> btnThoat.getScene().getWindow().hide());
    }

    private void loadTableMH() {
        List<MonHoc> list = monHocService.getAllMonHoc();
        dsMH.setAll(list);
        tableMH.setItems(dsMH);
    }

    private void bindForm(MonHoc mh) {
        txtMaMH.setText(mh.getMaMH());
        txtTenMH.setText(mh.getTenMH());
        txtSoTietLT.setText(String.valueOf(mh.getSoTietLT()));
        txtSoTietTH.setText(String.valueOf(mh.getSoTietTH()));
        txtMaMH.setDisable(true);
    }

    private void applyFormTo(MonHoc sel) {
        sel.setTenMH(txtTenMH.getText().trim());
        sel.setSoTietLT(Integer.parseInt(txtSoTietLT.getText().trim()));
        sel.setSoTietTH(Integer.parseInt(txtSoTietTH.getText().trim()));
    }

    private void addMonHoc() {
        String ma = txtMaMH.getText().trim();
        String ten = txtTenMH.getText().trim();
        String soLTStr = txtSoTietLT.getText().trim();
        String soTHStr = txtSoTietTH.getText().trim();

        if (ma.isEmpty() || ten.isEmpty() || soLTStr.isEmpty() || soTHStr.isEmpty()) {
            showAlert("Lỗi", "Phải nhập đầy đủ Mã MH, Tên MH, Số tiết LT và Số tiết TH.", Alert.AlertType.ERROR);
            return;
        }
        int soLT, soTH;
        try {
            soLT = Integer.parseInt(soLTStr);
            soTH = Integer.parseInt(soTHStr);
            if (soLT < 0 || soTH < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showAlert("Lỗi", "Số tiết LT / TH phải là số nguyên >= 0.", Alert.AlertType.ERROR);
            return;
        }
        if (monHocService.existsById(ma)) {
            showAlert("Lỗi", "Mã MH đã tồn tại.", Alert.AlertType.WARNING);
            return;
        }
        MonHoc mh = new MonHoc();
        mh.setMaMH(ma);
        mh.setTenMH(ten);
        mh.setSoTietLT(soLT);
        mh.setSoTietTH(soTH);
        monHocService.save(mh);
        loadTableMH(); clearForm();
    }

    private void clearForm() {
        txtMaMH.clear(); txtTenMH.clear(); txtSoTietLT.clear(); txtSoTietTH.clear();
        txtMaMH.setDisable(false);
        tableMH.getSelectionModel().clearSelection();
    }

    private void disableForm() {
        txtMaMH.setDisable(true); txtTenMH.setDisable(true);
        txtSoTietLT.setDisable(true); txtSoTietTH.setDisable(true);
        btnAdd.setDisable(true); btnUpdate.setDisable(true);
        btnDelete.setDisable(true); btnRefresh.setDisable(true);
        btnThoat.setText("Đóng"); tableMH.setDisable(true);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
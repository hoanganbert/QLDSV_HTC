package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.entity.GiangVien;
import com.project.QLDSV_HTC.entity.Khoa;
import com.project.QLDSV_HTC.service.GiangVienService;
import com.project.QLDSV_HTC.service.KhoaService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GiangVienController {

    @FXML private TextField txtMaGV;
    @FXML private TextField txtHo;
    @FXML private TextField txtTen;
    @FXML private ComboBox<Khoa> cboKhoa;

    @FXML private Button btnAddGV;
    @FXML private Button btnUpdateGV;
    @FXML private Button btnDeleteGV;
    @FXML private Button btnRefreshGV;
    @FXML private Button btnCloseGV;

    @FXML private TableView<GiangVien> tableGV;
    @FXML private TableColumn<GiangVien, String> colMaGV;
    @FXML private TableColumn<GiangVien, String> colHo;
    @FXML private TableColumn<GiangVien, String> colTen;
    @FXML private TableColumn<GiangVien, String> colMaKhoa;

    private final ObservableList<GiangVien> dsGV = FXCollections.observableArrayList();
    private final ObservableList<Khoa> dsKhoa = FXCollections.observableArrayList();

    @Autowired private GiangVienService giangVienService;
    @Autowired private KhoaService khoaService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // 1. Chỉ PGV mới được phép
        if (!"PGV".equals(appContext.getRole())) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Chỉ PGV mới có quyền quản lý Giảng viên.", Alert.AlertType.WARNING);
            return;
        }

        // 2. Load ComboBox Khoa
        dsKhoa.clear();
        dsKhoa.addAll(khoaService.getAllKhoa());
        cboKhoa.setItems(dsKhoa);

        // 3. Cấu hình TableView
        colMaGV.setCellValueFactory(new PropertyValueFactory<>("maGV"));
        colHo.setCellValueFactory(new PropertyValueFactory<>("ho"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("ten"));

        // Hiển thị mã khoa qua quan hệ GiangVien → Khoa
        colMaKhoa.setCellValueFactory(cellData -> {
            Khoa k = cellData.getValue().getKhoa();
            String ma = (k == null ? "" : k.getMaKhoa());
            return new ReadOnlyStringWrapper(ma);
        });

        loadTableGV();

        // 4. Khi chọn 1 dòng, đổ lên form
        tableGV.getSelectionModel().selectedItemProperty().addListener((obs, old, newGV) -> {
            if (newGV != null) {
                txtMaGV.setText(newGV.getMaGV().trim());
                txtHo.setText(newGV.getHo());
                txtTen.setText(newGV.getTen());
                // Chọn Khoa trong ComboBox dựa vào newGV.getKhoa().getMaKhoa()
                if (newGV.getKhoa() != null) {
                    String maKhoa = newGV.getKhoa().getMaKhoa();
                    for (Khoa k : dsKhoa) {
                        if (k.getMaKhoa().equals(maKhoa)) {
                            cboKhoa.getSelectionModel().select(k);
                            break;
                        }
                    }
                }
                txtMaGV.setDisable(true);
            }
        });

        // 5. Nút Thêm GV
        btnAddGV.setOnAction(e -> {
            String ma = txtMaGV.getText().trim();
            String ho = txtHo.getText().trim();
            String ten = txtTen.getText().trim();
            Khoa k = cboKhoa.getSelectionModel().getSelectedItem();

            if (ma.isEmpty() || ho.isEmpty() || ten.isEmpty() || k == null) {
                showAlert("Lỗi", "Phải nhập đầy đủ Mã GV, Họ, Tên và Khoa.", Alert.AlertType.ERROR);
                return;
            }
            if (giangVienService.existsById(ma)) {
                showAlert("Lỗi", "Mã GV đã tồn tại.", Alert.AlertType.WARNING);
                return;
            }
            GiangVien gv = new GiangVien();
            gv.setMaGV(ma);
            gv.setHo(ho);
            gv.setTen(ten);
            gv.setKhoa(k);              // sửa thành setKhoa(k)
            giangVienService.save(gv);

            loadTableGV();
            clearForm();
        });

        // 6. Nút Cập nhật GV
        btnUpdateGV.setOnAction(e -> {
            GiangVien sel = tableGV.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Giảng viên cần sửa.", Alert.AlertType.WARNING);
                return;
            }
            String ho = txtHo.getText().trim();
            String ten = txtTen.getText().trim();
            Khoa k = cboKhoa.getSelectionModel().getSelectedItem();
            if (ho.isEmpty() || ten.isEmpty() || k == null) {
                showAlert("Lỗi", "Phải nhập đầy đủ Họ, Tên và Khoa.", Alert.AlertType.ERROR);
                return;
            }
            sel.setHo(ho);
            sel.setTen(ten);
            sel.setKhoa(k);             // sửa thành setKhoa(k)
            giangVienService.save(sel);

            loadTableGV();
            clearForm();
        });

        // 7. Nút Xóa GV
        btnDeleteGV.setOnAction(e -> {
            GiangVien sel = tableGV.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Giảng viên cần xóa.", Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setContentText("Bạn có chắc muốn xóa Giảng viên " + sel.getMaGV() + "?");
            if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
                giangVienService.delete(sel.getMaGV());
                loadTableGV();
                clearForm();
            }
        });

        // 8. Nút Refresh (Phục hồi)
        btnRefreshGV.setOnAction(e -> {
            clearForm();
            loadTableGV();
        });

        // 9. Nút Thoát
        btnCloseGV.setOnAction(e -> btnCloseGV.getScene().getWindow().hide());
    }

    private void loadTableGV() {
        List<GiangVien> list = giangVienService.getAllGiangVien();
        dsGV.setAll(list);
        tableGV.setItems(dsGV);
    }

    private void clearForm() {
        txtMaGV.clear();
        txtHo.clear();
        txtTen.clear();
        cboKhoa.getSelectionModel().clearSelection();
        txtMaGV.setDisable(false);
        tableGV.getSelectionModel().clearSelection();
    }

    private void disableForm() {
        txtMaGV.setDisable(true);
        txtHo.setDisable(true);
        txtTen.setDisable(true);
        cboKhoa.setDisable(true);
        btnAddGV.setDisable(true);
        btnUpdateGV.setDisable(true);
        btnDeleteGV.setDisable(true);
        btnRefreshGV.setDisable(true);
        btnCloseGV.setText("Đóng");
        tableGV.setDisable(true);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

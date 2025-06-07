// File: src/main/java/com/project/QLDSV_HTC/controller/LopTinChiController.java
package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.entity.Khoa;
import com.project.QLDSV_HTC.entity.LopTinChi;
import com.project.QLDSV_HTC.entity.MonHoc;
import com.project.QLDSV_HTC.entity.GiangVien;
import com.project.QLDSV_HTC.service.LopTinChiService;
import com.project.QLDSV_HTC.service.MonHocService;
import com.project.QLDSV_HTC.service.GiangVienService;
import com.project.QLDSV_HTC.service.KhoaService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class LopTinChiController {

    @FXML private ComboBox<String> cboNienKhoa;
    @FXML private ComboBox<Integer> cboHocKy;
    @FXML private ComboBox<MonHoc> cboMonHoc;
    @FXML private ComboBox<Integer> cboNhom;
    @FXML private ComboBox<GiangVien> cboGiangVien;
    @FXML private ComboBox<Khoa> cboKhoa;

    @FXML private TextField txtSVToiThieu;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnThoat;

    @FXML private TableView<LopTinChi> tableLTC;
    @FXML private TableColumn<LopTinChi, Integer> colMaLTC;
    @FXML private TableColumn<LopTinChi, String> colNienKhoa;
    @FXML private TableColumn<LopTinChi, Integer> colHocKy;
    @FXML private TableColumn<LopTinChi, String> colMaMH;
    @FXML private TableColumn<LopTinChi, Integer> colNhom;
    @FXML private TableColumn<LopTinChi, String> colMaGV;
    @FXML private TableColumn<LopTinChi, Integer> colSVToiThieu;
    @FXML private TableColumn<LopTinChi, String> colMaKhoa;

    private ObservableList<LopTinChi> dsLTC = FXCollections.observableArrayList();
    private ObservableList<String> dsNienKhoa = FXCollections.observableArrayList("2023-2024", "2024-2025", "2025-2026");
    private ObservableList<Integer> dsHocKy = FXCollections.observableArrayList(Arrays.asList(1, 2, 3));
    private ObservableList<Integer> dsNhom = FXCollections.observableArrayList(Arrays.asList(1, 2, 3, 4, 5));
    private ObservableList<Khoa> dsKhoa = FXCollections.observableArrayList();

    @Autowired private LopTinChiService ltcService;
    @Autowired private MonHocService monHocService;
    @Autowired private GiangVienService gvService;
    @Autowired private KhoaService khoaService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // Chỉ PGV mới có quyền thao tác
        if (!"PGV".equals(appContext.getRole())) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Chỉ PGV mới có quyền mở Lớp Tín chỉ.", Alert.AlertType.WARNING);
            return;
        }

        // Khởi tạo ComboBox
        cboNienKhoa.setItems(dsNienKhoa);
        cboHocKy.setItems(dsHocKy);
        cboMonHoc.setItems(FXCollections.observableArrayList(monHocService.getAllMonHoc()));
        cboNhom.setItems(dsNhom);
        cboGiangVien.setItems(FXCollections.observableArrayList(gvService.getAllGiangVien()));

        dsKhoa.clear();
        dsKhoa.addAll(khoaService.getAllKhoa());
        cboKhoa.setItems(dsKhoa);

        // Cấu hình TableView
        colMaLTC.setCellValueFactory(new PropertyValueFactory<>("maLTC"));
        colNienKhoa.setCellValueFactory(new PropertyValueFactory<>("nienKhoa"));
        colHocKy.setCellValueFactory(new PropertyValueFactory<>("hocKy"));
        // Cột Mã Môn học
        colMaMH.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getMonHoc().getMaMH())
        );
        colNhom.setCellValueFactory(new PropertyValueFactory<>("nhom"));
        // Cột Mã Giảng viên
        colMaGV.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getGiangVien().getMaGV())
        );
        colSVToiThieu.setCellValueFactory(new PropertyValueFactory<>("soSVToiThieu"));
        // Cột Mã Khoa: lấy từ đối tượng khoaQuanLy
        colMaKhoa.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getKhoaQuanLy().getMaKhoa())
        );

        loadTableLTC();

        // Khi chọn 1 dòng, đổ dữ liệu lên form
        tableLTC.getSelectionModel().selectedItemProperty().addListener((obs, old, newLTC) -> {
            if (newLTC != null) {
                cboNienKhoa.getSelectionModel().select(newLTC.getNienKhoa());
                cboHocKy.getSelectionModel().select(newLTC.getHocKy());
                cboMonHoc.getSelectionModel().select(newLTC.getMonHoc());
                cboNhom.getSelectionModel().select(newLTC.getNhom());
                cboGiangVien.getSelectionModel().select(newLTC.getGiangVien());
                txtSVToiThieu.setText(String.valueOf(newLTC.getSoSVToiThieu()));
                // Lựa chọn Khoa trong dsKhoa sao cho maKhoa khớp
                for (Khoa k : dsKhoa) {
                    if (k.getMaKhoa().equals(newLTC.getKhoaQuanLy().getMaKhoa())) {
                        cboKhoa.getSelectionModel().select(k);
                        break;
                    }
                }
            }
        });

        // Nút Thêm LTC
        btnAdd.setOnAction(e -> {
            String nk = cboNienKhoa.getValue();
            Integer hk = cboHocKy.getValue();
            MonHoc mh = cboMonHoc.getValue();
            Integer nh = cboNhom.getValue();
            GiangVien gv = cboGiangVien.getValue();
            Khoa kh = cboKhoa.getValue();
            String svMinStr = txtSVToiThieu.getText().trim();

            if (nk == null || hk == null || mh == null || nh == null || gv == null || kh == null || svMinStr.isEmpty()) {
                showAlert("Lỗi", "Phải nhập đầy đủ thông tin.", Alert.AlertType.ERROR);
                return;
            }
            int svMin;
            try {
                svMin = Integer.parseInt(svMinStr);
                if (svMin <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert("Lỗi", "Số SV tối thiểu phải là số nguyên dương.", Alert.AlertType.ERROR);
                return;
            }
            // Kiểm tra trùng
            boolean exists = ltcService.existsByChiTiet(nk, hk, mh.getMaMH(), nh, gv.getMaGV());
            if (exists) {
                showAlert("Lỗi", "Lớp Tín chỉ này đã tồn tại.", Alert.AlertType.WARNING);
                return;
            }
            LopTinChi ltc = new LopTinChi();
            ltc.setNienKhoa(nk);
            ltc.setHocKy(hk);
            ltc.setMonHoc(mh);
            ltc.setNhom(nh);
            ltc.setGiangVien(gv);
            ltc.setSoSVToiThieu(svMin);
            ltc.setKhoaQuanLy(kh);     // Đổi tên thành setKhoaQuanLy
            ltc.setHuyLop(false);
            ltcService.save(ltc);

            loadTableLTC();
        });

        // Nút Ghi (Update) LTC
        btnUpdate.setOnAction(e -> {
            LopTinChi sel = tableLTC.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Lớp Tín chỉ cần sửa.", Alert.AlertType.WARNING);
                return;
            }
            String nk = cboNienKhoa.getValue();
            Integer hk = cboHocKy.getValue();
            MonHoc mh = cboMonHoc.getValue();
            Integer nh = cboNhom.getValue();
            GiangVien gv = cboGiangVien.getValue();
            Khoa kh = cboKhoa.getValue();
            String svMinStr = txtSVToiThieu.getText().trim();

            if (nk == null || hk == null || mh == null || nh == null || gv == null || kh == null || svMinStr.isEmpty()) {
                showAlert("Lỗi", "Phải nhập đầy đủ thông tin.", Alert.AlertType.ERROR);
                return;
            }
            int svMin;
            try {
                svMin = Integer.parseInt(svMinStr);
                if (svMin <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert("Lỗi", "Số SV tối thiểu phải là số nguyên dương.", Alert.AlertType.ERROR);
                return;
            }
            sel.setNienKhoa(nk);
            sel.setHocKy(hk);
            sel.setMonHoc(mh);
            sel.setNhom(nh);
            sel.setGiangVien(gv);
            sel.setSoSVToiThieu(svMin);
            sel.setKhoaQuanLy(kh);   // Đổi sang setKhoaQuanLy
            ltcService.save(sel);

            loadTableLTC();
        });

        // Nút Xóa LTC
        btnDelete.setOnAction(e -> {
            LopTinChi sel = tableLTC.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Lớp Tín chỉ cần xóa.", Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setContentText("Bạn có chắc muốn xóa Lớp Tín chỉ mã " + sel.getMaLTC() + "?");
            if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
            	try {
            	    ltcService.delete(sel.getMaLTC());
            	} catch (DataIntegrityViolationException ex) {
            	    showAlert("Lỗi", "Không thể xóa vì đang có dữ liệu tham chiếu.", Alert.AlertType.ERROR);
            	}
                loadTableLTC();
            }
        });

        // Nút Phục hồi (Refresh)
        btnRefresh.setOnAction(e -> loadTableLTC());

        // Nút Thoát
        btnThoat.setOnAction(e -> btnThoat.getScene().getWindow().hide());
    }

    private void loadTableLTC() {
        List<LopTinChi> list = ltcService.getAllLTC();
        dsLTC.setAll(list);
        tableLTC.setItems(dsLTC);
    }

    private void disableForm() {
        cboNienKhoa.setDisable(true);
        cboHocKy.setDisable(true);
        cboMonHoc.setDisable(true);
        cboNhom.setDisable(true);
        cboGiangVien.setDisable(true);
        cboKhoa.setDisable(true);
        txtSVToiThieu.setDisable(true);

        btnAdd.setDisable(true);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnRefresh.setDisable(true);

        btnThoat.setText("Đóng");
        tableLTC.setDisable(true);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

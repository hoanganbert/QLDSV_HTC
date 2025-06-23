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

import java.util.ArrayDeque;
import java.util.Deque;
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

    // Undo stack lưu trạng thái trước mỗi thao tác Update/Delete
    private Deque<LopTinChi> undoStack = new ArrayDeque<>();

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
        colMaMH.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getMonHoc().getMaMH()));
        colNhom.setCellValueFactory(new PropertyValueFactory<>("nhom"));
        colMaGV.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getGiangVien().getMaGV()));
        colSVToiThieu.setCellValueFactory(new PropertyValueFactory<>("soSVToiThieu"));
        colMaKhoa.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getKhoaQuanLy().getMaKhoa()));

        loadTableLTC();

        // Khi chọn 1 dòng, đổ dữ liệu lên form
        tableLTC.getSelectionModel().selectedItemProperty().addListener((obs, old, newLTC) -> {
            if (newLTC != null) bindForm(newLTC);
        });

        // Thêm LTC
        btnAdd.setOnAction(e -> addLTC());

        // Update LTC: lưu trạng thái cũ rồi cập nhật
        btnUpdate.setOnAction(e -> {
            LopTinChi sel = tableLTC.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn Lớp Tín chỉ cần sửa.", Alert.AlertType.WARNING);
                return;
            }
            // Lưu snapshot
            undoStack.push(new LopTinChi(sel));
            applyFormTo(sel);
            ltcService.save(sel);
            loadTableLTC();
        });

        // Delete LTC: lưu trạng thái cũ rồi xóa
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
                // Lưu snapshot
                undoStack.push(new LopTinChi(sel));
                try {
                    ltcService.delete(sel.getMaLTC());
                } catch (DataIntegrityViolationException ex) {
                    showAlert("Lỗi", "Không thể xóa vì đang có dữ liệu tham chiếu.", Alert.AlertType.ERROR);
                }
                loadTableLTC();
            }
        });

        // Phục hồi (Undo)
        btnRefresh.setOnAction(e -> {
            if (undoStack.isEmpty()) {
                showAlert("Thông báo", "Không có hành động nào để phục hồi.", Alert.AlertType.INFORMATION);
            } else {
                LopTinChi prev = undoStack.pop();
                bindForm(prev);
                // Lưu lại vào DB (tuỳ chọn)
                ltcService.save(prev);
                loadTableLTC();
            }
        });

        // Thoát
        btnThoat.setOnAction(e -> btnThoat.getScene().getWindow().hide());
    }

    private void loadTableLTC() {
        dsLTC.setAll(ltcService.getAllLTC());
        tableLTC.setItems(dsLTC);
    }

    private void bindForm(LopTinChi ltc) {
        cboNienKhoa.getSelectionModel().select(ltc.getNienKhoa());
        cboHocKy.getSelectionModel().select(ltc.getHocKy());
        cboMonHoc.getSelectionModel().select(ltc.getMonHoc());
        cboNhom.getSelectionModel().select(ltc.getNhom());
        cboGiangVien.getSelectionModel().select(ltc.getGiangVien());
        txtSVToiThieu.setText(String.valueOf(ltc.getSoSVToiThieu()));
        for (Khoa k : dsKhoa) {
            if (k.getMaKhoa().equals(ltc.getKhoaQuanLy().getMaKhoa())) {
                cboKhoa.getSelectionModel().select(k);
                break;
            }
        }
    }

    private void applyFormTo(LopTinChi sel) {
        sel.setNienKhoa(cboNienKhoa.getValue());
        sel.setHocKy(cboHocKy.getValue());
        sel.setMonHoc(cboMonHoc.getValue());
        sel.setNhom(cboNhom.getValue());
        sel.setGiangVien(cboGiangVien.getValue());
        sel.setSoSVToiThieu(Integer.parseInt(txtSVToiThieu.getText().trim()));
        sel.setKhoaQuanLy(cboKhoa.getValue());
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

    private void addLTC() {
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
        ltc.setKhoaQuanLy(kh);
        ltc.setHuyLop(false);
        ltcService.save(ltc);
        loadTableLTC();
    }
}

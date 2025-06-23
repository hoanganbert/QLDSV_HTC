package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.dto.DiemDTO;
import com.project.QLDSV_HTC.entity.DangKy;
import com.project.QLDSV_HTC.entity.LopTinChi;
import com.project.QLDSV_HTC.service.DangKyService;
import com.project.QLDSV_HTC.service.DiemBatchService;
import com.project.QLDSV_HTC.service.LopTinChiService;
import com.project.QLDSV_HTC.service.MonHocService;
import com.project.QLDSV_HTC.service.GiangVienService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

@Component
public class NhapDiemController {

    @FXML private ComboBox<String> cboNienKhoa;
    @FXML private ComboBox<Integer> cboHocKy;
    @FXML private ComboBox<com.project.QLDSV_HTC.entity.MonHoc> cboMonHoc;
    @FXML private ComboBox<Integer> cboNhom;
    @FXML private ComboBox<com.project.QLDSV_HTC.entity.GiangVien> cboGiangVien;

    @FXML private Button btnLoc;
    @FXML private Button btnGhi;
    @FXML private Button btnPhucHoi;
    @FXML private Button btnClose;

    @FXML private TableView<DiemDTO> tableDiem;
    @FXML private TableColumn<DiemDTO, String> colMaSV, colHoTenSV;
    @FXML private TableColumn<DiemDTO, Integer> colDiemCC;
    @FXML private TableColumn<DiemDTO, Double> colDiemGK, colDiemCK, colDiemHM;

    private ObservableList<DiemDTO> dsDiem = FXCollections.observableArrayList();
    private ObservableList<String>  dsNienKhoa = FXCollections.observableArrayList("2023-2024", "2024-2025", "2025-2026");
    private ObservableList<Integer> dsHocKy    = FXCollections.observableArrayList(1, 2, 3);
    private ObservableList<Integer> dsNhom     = FXCollections.observableArrayList(Arrays.asList(1,2,3,4,5));

    @Autowired private LopTinChiService ltcService;
    @Autowired private MonHocService monHocService;
    @Autowired private GiangVienService gvService;
    @Autowired private DangKyService dkService;
    @Autowired private DiemBatchService diemBatchService;
    @Autowired private AppContextHolder appContext;

    // Undo stack lưu thay đổi từng ô
    private static class Change {
        final DiemDTO dto;
        final String field;
        final Object oldValue;
        Change(DiemDTO dto, String field, Object oldValue) {
            this.dto = dto; this.field = field; this.oldValue = oldValue;
        }
    }
    private Stack<Change> undoStack = new Stack<>();

    @FXML
    public void initialize() {
        // chỉ PGV hoặc KHOA được phép nhập
        String role = appContext.getRole();
        if (!"PGV".equals(role) && !"KHOA".equals(role)) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Bạn không có quyền nhập điểm.", Alert.AlertType.WARNING);
            return;
        }

        // load comboBox
        cboNienKhoa.setItems(dsNienKhoa);
        cboHocKy.setItems(dsHocKy);
        cboMonHoc.setItems(FXCollections.observableArrayList(monHocService.getAllMonHoc()));
        cboNhom.setItems(dsNhom);
        cboGiangVien.setItems(FXCollections.observableArrayList(gvService.getAllGiangVien()));

        // bật editable cho table
        tableDiem.setEditable(true);

        // Mã SV, Họ tên SV read-only
        colMaSV.setCellValueFactory(cell -> cell.getValue().maSVProperty());
        colHoTenSV.setCellValueFactory(cell -> cell.getValue().hoTenSVProperty());

        // Điểm CC
        colDiemCC.setCellValueFactory(cell -> cell.getValue().diemCCProperty().asObject());
        colDiemCC.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colDiemCC.setOnEditCommit(e -> {
            DiemDTO dto = e.getRowValue();
            int oldVal = dto.getDiemCC();
            int newVal = e.getNewValue();
            if (newVal < 0 || newVal > 10) {
                showAlert("Lỗi", "Điểm chuyên cần phải từ 0 đến 10.", Alert.AlertType.ERROR);
            } else {
                undoStack.push(new Change(dto, "CC", oldVal));
                dto.setDiemCC(newVal);
                dto.computeDiemHM();
            }
            tableDiem.refresh();
        });

        // Điểm GK
        colDiemGK.setCellValueFactory(cell -> cell.getValue().diemGKProperty().asObject());
        colDiemGK.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colDiemGK.setOnEditCommit(e -> {
            DiemDTO dto = e.getRowValue();
            double oldVal = dto.getDiemGK();
            double newVal = e.getNewValue();
            if (newVal < 0 || newVal > 10) {
                showAlert("Lỗi", "Điểm giữa kỳ phải từ 0 đến 10.", Alert.AlertType.ERROR);
            } else {
                undoStack.push(new Change(dto, "GK", oldVal));
                dto.setDiemGK(newVal);
                dto.computeDiemHM();
            }
            tableDiem.refresh();
        });

        // Điểm CK
        colDiemCK.setCellValueFactory(cell -> cell.getValue().diemCKProperty().asObject());
        colDiemCK.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colDiemCK.setOnEditCommit(e -> {
            DiemDTO dto = e.getRowValue();
            double oldVal = dto.getDiemCK();
            double newVal = e.getNewValue();
            if (newVal < 0 || newVal > 10) {
                showAlert("Lỗi", "Điểm cuối kỳ phải từ 0 đến 10.", Alert.AlertType.ERROR);
            } else {
                undoStack.push(new Change(dto, "CK", oldVal));
                dto.setDiemCK(newVal);
                dto.computeDiemHM();
            }
            tableDiem.refresh();
        });

        // Điểm hết môn read-only
        colDiemHM.setCellValueFactory(cell -> cell.getValue().diemHMProperty().asObject());

        // gán data vào table
        tableDiem.setItems(dsDiem);

        // button events
        btnLoc.setOnAction(e -> locSVTheoLTC());
        btnGhi.setOnAction(e -> ghiDiem());
        btnPhucHoi.setOnAction(e -> {
            if (undoStack.isEmpty()) {
                showAlert("Thông báo", "Không có thao tác nào để phục hồi.", Alert.AlertType.INFORMATION);
                return;
            }
            Change last = undoStack.pop();
            DiemDTO dto = last.dto;
            switch (last.field) {
                case "CC": dto.setDiemCC((Integer) last.oldValue); break;
                case "GK": dto.setDiemGK((Double) last.oldValue);  break;
                case "CK": dto.setDiemCK((Double) last.oldValue);  break;
            }
            dto.computeDiemHM();
            tableDiem.refresh();
        });
        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void disableForm() {
        cboNienKhoa.setDisable(true);
        cboHocKy.setDisable(true);
        cboMonHoc.setDisable(true);
        cboNhom.setDisable(true);
        cboGiangVien.setDisable(true);
        btnLoc.setDisable(true);
        btnGhi.setDisable(true);
        btnPhucHoi.setDisable(true);
        btnClose.setText("Đóng");
        tableDiem.setDisable(true);
    }

    private void locSVTheoLTC() {
        String nk = cboNienKhoa.getValue();
        Integer hk = cboHocKy.getValue();
        com.project.QLDSV_HTC.entity.MonHoc mh = cboMonHoc.getValue();
        Integer nh = cboNhom.getValue();
        com.project.QLDSV_HTC.entity.GiangVien gv = cboGiangVien.getValue();

        if (nk==null||hk==null||mh==null||nh==null||gv==null) {
            showAlert("Thông báo", "Chọn đủ Niên khóa, Học kỳ, Môn, Nhóm, GV.", Alert.AlertType.WARNING);
            return;
        }
        Optional<LopTinChi> opt = ltcService.getLTCTheoChiTiet(nk, hk, mh.getMaMH(), nh, gv.getMaGV());
        if (opt.isEmpty()) {
            showAlert("Thông báo", "Không tìm thấy Lớp tín chỉ phù hợp.", Alert.AlertType.INFORMATION);
            return;
        }
        LopTinChi ltc = opt.get();
        // check quyền KHOA
        if ("KHOA".equals(appContext.getRole()) &&
            !appContext.getMaKhoa().equals(ltc.getKhoaQuanLy().getMaKhoa())) {
            showAlert("Truy cập bị từ chối", "Bạn chỉ nhập điểm cho khoa mình.", Alert.AlertType.WARNING);
            return;
        }

        List<DangKy> ds = dkService.getDKTheoLTC(ltc.getMaLTC());
        dsDiem.clear();
        undoStack.clear();

        for (DangKy dk : ds) {
            DiemDTO dto = new DiemDTO();
            dto.setMaSV(dk.getSinhVien().getMaSV());
            dto.setHoTenSV(dk.getSinhVien().getHo() + " " + dk.getSinhVien().getTen());
            dto.setDiemCC(dk.getDiemCC() == null? 0 : dk.getDiemCC());
            dto.setDiemGK(dk.getDiemGK() == null? 0.0 : dk.getDiemGK());
            dto.setDiemCK(dk.getDiemCK() == null? 0.0 : dk.getDiemCK());
            dto.computeDiemHM();
            dsDiem.add(dto);
        }
    }

    private void ghiDiem() {
        String nk = cboNienKhoa.getValue();
        Integer hk = cboHocKy.getValue();
        com.project.QLDSV_HTC.entity.MonHoc mh = cboMonHoc.getValue();
        Integer nh = cboNhom.getValue();
        com.project.QLDSV_HTC.entity.GiangVien gv = cboGiangVien.getValue();

        Optional<LopTinChi> opt = ltcService.getLTCTheoChiTiet(nk, hk, mh.getMaMH(), nh, gv.getMaGV());
        if (opt.isEmpty()) {
            showAlert("Thông báo", "Không tìm thấy Lớp tín chỉ phù hợp.", Alert.AlertType.INFORMATION);
            return;
        }
        int maLTC = opt.get().getMaLTC();
        try {
            diemBatchService.capNhatDiemBatch(maLTC, dsDiem);
            showAlert("Thành công", "Cập nhật điểm thành công.", Alert.AlertType.INFORMATION);
            tableDiem.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Cập nhật điểm thất bại: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

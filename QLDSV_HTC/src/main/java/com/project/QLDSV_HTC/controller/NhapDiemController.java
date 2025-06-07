package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.dto.DiemDTO;
import com.project.QLDSV_HTC.entity.DangKy;
import com.project.QLDSV_HTC.entity.LopTinChi;
import com.project.QLDSV_HTC.service.DangKyService;
import com.project.QLDSV_HTC.service.LopTinChiService;
import com.project.QLDSV_HTC.service.MonHocService;
import com.project.QLDSV_HTC.service.GiangVienService;
import com.project.QLDSV_HTC.service.DiemBatchService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private ObservableList<String>    dsNienKhoa = FXCollections.observableArrayList("2023-2024", "2024-2025", "2025-2026");
    private ObservableList<Integer>   dsHocKy    = FXCollections.observableArrayList(Arrays.asList(1, 2, 3));
    private ObservableList<Integer>   dsNhom     = FXCollections.observableArrayList(Arrays.asList(1, 2, 3, 4, 5));

    @Autowired private LopTinChiService ltcService;
    @Autowired private MonHocService monHocService;
    @Autowired private GiangVienService gvService;
    @Autowired private DangKyService dkService;
    @Autowired private DiemBatchService diemBatchService;
    @Autowired private AppContextHolder appContext;

    /**
     * Lớp lưu lại một lần thay đổi (Change) cho mỗi ô điểm.
     */
    private static class Change {
        private DiemDTO dto;
        private String field;
        private Object oldValue;

        public Change(DiemDTO dto, String field, Object oldValue) {
            this.dto = dto;
            this.field = field;
            this.oldValue = oldValue;
        }
        public DiemDTO getDto()     { return dto; }
        public String getField()    { return field; }
        public Object getOldValue() { return oldValue; }
    }

    // Stack để lưu lịch sử thay đổi; mỗi lần edit đẩy vào stack, undo pop ra.
    private Stack<Change> undoStack = new Stack<>();

    @FXML
    public void initialize() {
        // Phân quyền: chỉ PGV hoặc KHOA
        String role = appContext.getRole();
        if (!"PGV".equals(role) && !"KHOA".equals(role)) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Bạn không có quyền nhập điểm.", Alert.AlertType.WARNING);
            return;
        }

        // Thiết lập dữ liệu cho ComboBox
        cboNienKhoa.setItems(dsNienKhoa);
        cboHocKy.setItems(dsHocKy);
        cboMonHoc.setItems(FXCollections.observableArrayList(monHocService.getAllMonHoc()));
        cboNhom.setItems(dsNhom);
        cboGiangVien.setItems(FXCollections.observableArrayList(gvService.getAllGiangVien()));

        // Cấu hình TableView và các cột
        colMaSV.setCellValueFactory(new PropertyValueFactory<>("maSV"));
        colHoTenSV.setCellValueFactory(new PropertyValueFactory<>("hoTenSV"));

        // Cột CC (Integer, editable)
        colDiemCC.setCellValueFactory(new PropertyValueFactory<>("diemCC"));
        colDiemCC.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colDiemCC.setOnEditCommit(e -> {
            DiemDTO dto = e.getRowValue();
            int oldVal = dto.getDiemCC();
            int newVal = e.getNewValue();
            if (newVal < 0 || newVal > 10) {
                showAlert("Lỗi", "Điểm chuyên cần phải từ 0 đến 10.", Alert.AlertType.ERROR);
                tableDiem.refresh();
            } else {
                undoStack.push(new Change(dto, "CC", oldVal));
                dto.setDiemCC(newVal);
                dto.computeDiemHM();
                tableDiem.refresh();
            }
        });

        // Cột GK (Double, editable)
        colDiemGK.setCellValueFactory(new PropertyValueFactory<>("diemGK"));
        colDiemGK.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colDiemGK.setOnEditCommit(e -> {
            DiemDTO dto = e.getRowValue();
            double oldVal = dto.getDiemGK();
            double newVal = e.getNewValue();
            if (newVal < 0 || newVal > 10) {
                showAlert("Lỗi", "Điểm giữa kỳ phải từ 0 đến 10.", Alert.AlertType.ERROR);
                tableDiem.refresh();
            } else {
                undoStack.push(new Change(dto, "GK", oldVal));
                dto.setDiemGK(newVal);
                dto.computeDiemHM();
                tableDiem.refresh();
            }
        });

        // Cột CK (Double, editable)
        colDiemCK.setCellValueFactory(new PropertyValueFactory<>("diemCK"));
        colDiemCK.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colDiemCK.setOnEditCommit(e -> {
            DiemDTO dto = e.getRowValue();
            double oldVal = dto.getDiemCK();
            double newVal = e.getNewValue();
            if (newVal < 0 || newVal > 10) {
                showAlert("Lỗi", "Điểm cuối kỳ phải từ 0 đến 10.", Alert.AlertType.ERROR);
                tableDiem.refresh();
            } else {
                undoStack.push(new Change(dto, "CK", oldVal));
                dto.setDiemCK(newVal);
                dto.computeDiemHM();
                tableDiem.refresh();
            }
        });

        // Cột HM (Double, chỉ đọc)
        colDiemHM.setCellValueFactory(new PropertyValueFactory<>("diemHM"));

        tableDiem.setEditable(true);

        // Nút “Lọc”
        btnLoc.setOnAction(e -> locSVTheoLTC());

        // Nút “Ghi Điểm”
        btnGhi.setOnAction(e -> ghiDiem());

        // Nút “Phục hồi” (multi-level undo)
        btnPhucHoi.setOnAction(e -> {
            if (undoStack.isEmpty()) {
                showAlert("Thông báo", "Không còn thao tác nào để phục hồi.", Alert.AlertType.INFORMATION);
                return;
            }
            Change last = undoStack.pop();
            DiemDTO dto = last.getDto();
            String field = last.getField();
            Object oldValue = last.getOldValue();
            switch (field) {
                case "CC":
                    dto.setDiemCC((Integer) oldValue);
                    break;
                case "GK":
                    dto.setDiemGK((Double) oldValue);
                    break;
                case "CK":
                    dto.setDiemCK((Double) oldValue);
                    break;
            }
            dto.computeDiemHM();
            tableDiem.refresh();
        });

        // Nút “Thoát”
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
        String nk = cboNienKhoa.getSelectionModel().getSelectedItem();
        Integer hk = cboHocKy.getSelectionModel().getSelectedItem();
        com.project.QLDSV_HTC.entity.MonHoc mh = cboMonHoc.getSelectionModel().getSelectedItem();
        Integer nh = cboNhom.getSelectionModel().getSelectedItem();
        com.project.QLDSV_HTC.entity.GiangVien gv = cboGiangVien.getSelectionModel().getSelectedItem();

        if (nk == null || hk == null || mh == null || nh == null || gv == null) {
            showAlert("Thông báo", "Chọn đủ thông tin lọc (Niên khóa, Học kỳ, Môn học, Nhóm, Giảng viên).", Alert.AlertType.WARNING);
            return;
        }

        // 1) Tìm LopTinChi theo chi tiết
        Optional<LopTinChi> optLTC = ltcService.getLTCTheoChiTiet(nk, hk, mh.getMaMH(), nh, gv.getMaGV());
        if (optLTC.isEmpty()) {
            showAlert("Thông báo", "Không tìm thấy Lớp tín chỉ phù hợp.", Alert.AlertType.INFORMATION);
            return;
        }
        LopTinChi ltc = optLTC.get();
        // 2) Nếu role = KHOA, kiểm tra lớp đó có cùng khoa hay không
        if ("KHOA".equals(appContext.getRole())) {
            String maKhoaLogin = appContext.getMaKhoa();
            if (!maKhoaLogin.equals(ltc.getKhoaQuanLy().getMaKhoa())) {
                showAlert("Truy cập bị từ chối", "Bạn chỉ có quyền nhập điểm cho Lớp tín chỉ thuộc Khoa của mình.", Alert.AlertType.WARNING);
                return;
            }
        }

        // 3) Lấy danh sách DangKy (chưa hủy) của lớp tín chỉ ltc
        List<DangKy> ds = dkService.getDKTheoLTC(ltc.getMaLTC());
        dsDiem.clear();
        undoStack.clear();   // mỗi lần Lọc mới, xóa toàn bộ lịch sử undo

        // 4) Chuyển về DiemDTO để hiển thị
        for (DangKy dk : ds) {
            DiemDTO dto = new DiemDTO();
            dto.setMaSV(dk.getSinhVien().getMaSV());
            dto.setHoTenSV(dk.getSinhVien().getHo() + " " + dk.getSinhVien().getTen());
            dto.setDiemCC(dk.getDiemCC() == null ? 0 : dk.getDiemCC());
            dto.setDiemGK(dk.getDiemGK() == null ? 0.0 : dk.getDiemGK());
            dto.setDiemCK(dk.getDiemCK() == null ? 0.0 : dk.getDiemCK());
            dto.computeDiemHM();
            dsDiem.add(dto);
        }
        tableDiem.setItems(dsDiem);
    }

    private void ghiDiem() {
        String nk = cboNienKhoa.getSelectionModel().getSelectedItem();
        Integer hk = cboHocKy.getSelectionModel().getSelectedItem();
        com.project.QLDSV_HTC.entity.MonHoc mh = cboMonHoc.getSelectionModel().getSelectedItem();
        Integer nh = cboNhom.getSelectionModel().getSelectedItem();
        com.project.QLDSV_HTC.entity.GiangVien gv = cboGiangVien.getSelectionModel().getSelectedItem();

        Optional<LopTinChi> optLTC = ltcService.getLTCTheoChiTiet(nk, hk, mh.getMaMH(), nh, gv.getMaGV());
        if (optLTC.isEmpty()) {
            showAlert("Thông báo", "Không tìm thấy Lớp tín chỉ phù hợp.", Alert.AlertType.INFORMATION);
            return;
        }
        LopTinChi ltc = optLTC.get();
        Integer maLTC = ltc.getMaLTC();

        try {
            // Gọi batch update
            diemBatchService.capNhatDiemBatch(maLTC, dsDiem);
            showAlert("Thành công", "Cập nhật điểm thành công (batch).", Alert.AlertType.INFORMATION);
            tableDiem.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Có lỗi khi cập nhật điểm: " + ex.getMessage(), Alert.AlertType.ERROR);
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

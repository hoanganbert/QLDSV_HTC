package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.dto.DangKyDTO;
import com.project.QLDSV_HTC.entity.LopTinChi;
import com.project.QLDSV_HTC.entity.Sinhvien;
import com.project.QLDSV_HTC.service.DangKyService;
import com.project.QLDSV_HTC.service.LopTinChiService;
import com.project.QLDSV_HTC.service.SinhVienService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DangKyController {

    @FXML private Label lblMaSV;
    @FXML private Label lblHoTen;
    @FXML private Label lblMaLop;

    @FXML private ComboBox<String> cboNienKhoa;
    @FXML private ComboBox<Integer> cboHocKy;
    @FXML private TableView<DangKyDTO> tableDK;
    @FXML private TableColumn<DangKyDTO, Boolean> colSelect;
    @FXML private TableColumn<DangKyDTO, String> colMaMH;
    @FXML private TableColumn<DangKyDTO, String> colTenMH;
    @FXML private TableColumn<DangKyDTO, Integer> colNhom;
    @FXML private TableColumn<DangKyDTO, String> colHoTenGV;
    @FXML private TableColumn<DangKyDTO, Integer> colSoSVDaDK;

    @FXML private Button btnChay;
    @FXML private Button btnGhi;
    @FXML private Button btnClose;

    private ObservableList<DangKyDTO> dsDK = FXCollections.observableArrayList();

    @Autowired private SinhVienService sinhVienService;
    @Autowired private LopTinChiService ltcService;
    @Autowired private DangKyService dkService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // --------------- 1. Kiểm tra role -----------------
        if (!"SINHVIEN".equals(appContext.getRole())) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Chỉ Sinh viên mới được đăng ký lớp tín chỉ.", Alert.AlertType.WARNING);
            return;
        }

        // --------------- 2. Hiển thị thông tin SV -----------------
        String maSV = appContext.getMaSV();
        Optional<Sinhvien> optSv = sinhVienService.findById(maSV);
        if (optSv.isEmpty()) {
            showAlert("Lỗi", "Không tìm thấy sinh viên có mã " + maSV, Alert.AlertType.ERROR);
            disableForm();
            return;
        }
        Sinhvien sv = optSv.get();
        lblMaSV.setText(maSV);
        lblHoTen.setText(sv.getHo() + " " + sv.getTen());
        lblMaLop.setText(sv.getLop().getMaLop());

        // --------------- 3. Load ComboBox Niên Khóa, Học Kỳ -----------------
        cboNienKhoa.setItems(FXCollections.observableArrayList("2023-2024", "2024-2025", "2025-2026"));
        cboHocKy.setItems(FXCollections.observableArrayList(1, 2, 3));

        // --------------- 4. Cấu hình TableView -----------------
        // a) CellValueFactory bind đến BooleanProperty
        colSelect.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        // b) CellFactory cho checkbox
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        // c) Cho phép chỉnh sửa checkbox
        tableDK.setEditable(true);
        colSelect.setEditable(true);

        // d) Các cột khác
        colMaMH.setCellValueFactory(new PropertyValueFactory<>("maMH"));
        colTenMH.setCellValueFactory(new PropertyValueFactory<>("tenMH"));
        colNhom.setCellValueFactory(new PropertyValueFactory<>("nhom"));
        colHoTenGV.setCellValueFactory(new PropertyValueFactory<>("hoTenGV"));
        colSoSVDaDK.setCellValueFactory(new PropertyValueFactory<>("soSVDaDK"));

        // e) Gán data
        tableDK.setItems(dsDK);

        // --------------- 5. Nút “Chạy” (lọc danh sách) -----------------
        btnChay.setOnAction(e -> chayDangKy());

        // --------------- 6. Nút “Ghi” (cập nhật đăng ký/hủy đăng ký) -----------------
        btnGhi.setOnAction(e -> {
            for (DangKyDTO dto : dsDK) {
                ltcService.capNhatDangKy(appContext.getMaSV(), dto.getMaLTC(), dto.isSelected());
            }
            showAlert("Thành công", "Cập nhật đăng ký thành công.", Alert.AlertType.INFORMATION);
            chayDangKy(); // reload lại sau khi lưu
        });

        // --------------- 7. Nút “Đóng” -----------------
        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void chayDangKy() {
        String nk = cboNienKhoa.getValue();
        Integer hk = cboHocKy.getValue();
        if (nk == null || hk == null) {
            showAlert("Thông báo", "Chọn Niên khóa và Học kỳ.", Alert.AlertType.WARNING);
            return;
        }
        String maSV = appContext.getMaSV();
        Optional<Sinhvien> optSv = sinhVienService.findById(maSV);
        if (optSv.isEmpty()) {
            showAlert("Lỗi", "Không tìm thấy sinh viên có mã " + maSV, Alert.AlertType.ERROR);
            return;
        }
        Sinhvien sv = optSv.get();
        // Lấy mã KHOA của sinh viên: từ Lop → Khoa
        String maKhoa = sv.getLop().getKhoa().getMaKhoa();

        // ------------- Lấy danh sách LTC (chưa hủy) theo nk, hk, và Khoa ------------
        List<LopTinChi> listLTC = ltcService.findByNienKhoaHocKyAndKhoa(nk, hk, maKhoa);

        dsDK.clear();
        for (LopTinChi ltc : listLTC) {
            DangKyDTO dto = new DangKyDTO();
            dto.setMaLTC(ltc.getMaLTC());
            dto.setMaMH(ltc.getMonHoc().getMaMH());
            dto.setTenMH(ltc.getMonHoc().getTenMH());
            dto.setNhom(ltc.getNhom());
            dto.setHoTenGV(ltc.getGiangVien().getHo() + " " + ltc.getGiangVien().getTen());
            // Đếm số SV đã đăng ký (chưa hủy) cho lớp này
            int soSVDaDK = dkService.countByLTC(ltc.getMaLTC());
            dto.setSoSVDaDK(soSVDaDK);
            // Kiểm tra SV hiện tại đã đăng ký chưa
            boolean isRegistered = dkService.isDangKy(ltc.getMaLTC(), maSV);
            dto.setSelected(isRegistered);
            dsDK.add(dto);
        }
    }

    private void disableForm() {
        lblMaSV.setText("");
        lblHoTen.setText("");
        lblMaLop.setText("");
        cboNienKhoa.setDisable(true);
        cboHocKy.setDisable(true);
        tableDK.setDisable(true);
        btnChay.setDisable(true);
        btnGhi.setDisable(true);
        btnClose.setText("Đóng");
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

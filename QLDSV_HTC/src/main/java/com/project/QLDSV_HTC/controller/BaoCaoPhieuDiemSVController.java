package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.dto.BaoCaoPhieuDiemDTO;
import com.project.QLDSV_HTC.entity.Sinhvien;
import com.project.QLDSV_HTC.service.BaoCaoService;
import com.project.QLDSV_HTC.service.SinhVienService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class BaoCaoPhieuDiemSVController {

    @FXML private ComboBox<Sinhvien> cboSV;
    @FXML private TextField txtMaSV;    // dùng nếu muốn cho nhập thủ công (không bắt buộc)
    @FXML private ComboBox<String> cboNienKhoa;
    @FXML private ComboBox<Integer> cboHocKy;

    @FXML private Button btnChay;
    @FXML private Button btnXuatExcel;
    @FXML private Button btnClose;

    @FXML private TableView<BaoCaoPhieuDiemDTO> tableBaoCao;
    @FXML private TableColumn<BaoCaoPhieuDiemDTO, Integer> colSTT;
    @FXML private TableColumn<BaoCaoPhieuDiemDTO, String>  colTenMH;
    @FXML private TableColumn<BaoCaoPhieuDiemDTO, Double>  colDiemMax;

    private ObservableList<BaoCaoPhieuDiemDTO> dsBaoCao = FXCollections.observableArrayList();
    private ObservableList<String>            dsNienKhoa = FXCollections.observableArrayList("2023-2024", "2024-2025", "2025-2026");
    private ObservableList<Integer>           dsHocKy    = FXCollections.observableArrayList(1, 2, 3);

    @Autowired private BaoCaoService baoCaoService;
    @Autowired private SinhVienService sinhVienService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // 1. Load ComboBox Sinh viên (PGV/KHOA có thể chọn, SV chỉ xem của mình)
        cboSV.setItems(FXCollections.observableArrayList(sinhVienService.getAllSV()));
        cboNienKhoa.setItems(dsNienKhoa);
        cboHocKy.setItems(dsHocKy);

        // 2. Nếu user là SINHVIEN, tự động chọn sinh viên đó và disable ComboBox/TextField
        if ("SINHVIEN".equals(appContext.getRole())) {
            String maSV = appContext.getMaSV();
            Optional<Sinhvien> optSv = sinhVienService.findById(maSV);
            if (optSv.isPresent()) {
                Sinhvien sv = optSv.get();
                cboSV.getSelectionModel().select(sv);
                cboSV.setDisable(true);
                txtMaSV.setText(maSV);
                txtMaSV.setDisable(true);
            }
        }

        // 3. Cấu hình TableView
        colSTT    .setCellValueFactory(new PropertyValueFactory<>("stt"));
        colTenMH  .setCellValueFactory(new PropertyValueFactory<>("tenMH"));
        colDiemMax.setCellValueFactory(new PropertyValueFactory<>("diemMax"));

        tableBaoCao.setItems(dsBaoCao);

        // 4. Nút “Chạy”
        btnChay.setOnAction(e -> chayBaoCao());

        // 5. Nút “Xuất Excel”
        btnXuatExcel.setOnAction(e -> xuatExcel());

        // 6. Nút “Đóng”
        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void chayBaoCao() {
        String maSV = null;
        if (!txtMaSV.getText().trim().isEmpty()) {
            maSV = txtMaSV.getText().trim();
        } else if (cboSV.getValue() != null) {
            maSV = cboSV.getValue().getMaSV();
        }
        String nk = cboNienKhoa.getValue();
        Integer hk = cboHocKy.getValue();

        if (maSV == null || nk == null || hk == null) {
            showAlert("Thông báo", "Phải chọn/nhập Mã SV, Niên khóa, Học kỳ.", Alert.AlertType.WARNING);
            return;
        }
        // Kiểm tra SV tồn tại
        if (!sinhVienService.existsById(maSV)) {
            showAlert("Lỗi", "Mã SV không tồn tại.", Alert.AlertType.ERROR);
            return;
        }

        List<BaoCaoPhieuDiemDTO> list = baoCaoService.getBaoCaoPhieuDiemSV(maSV, nk, hk);
        dsBaoCao.setAll(list);
    }

    private void xuatExcel() {
        if (dsBaoCao.isEmpty()) {
            showAlert("Thông báo", "Chưa có dữ liệu để xuất.", Alert.AlertType.WARNING);
            return;
        }

        String maSV = cboSV.getValue() != null
                      ? cboSV.getValue().getMaSV()
                      : txtMaSV.getText().trim();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo Phiếu Điểm SV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("PhieuDiem_" + maSV + "_" + timestamp + ".xlsx");

        File file = fileChooser.showSaveDialog(btnXuatExcel.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("PhieuDiemSV");

            // Tiêu đề
            Row row0 = sheet.createRow(0);
            Cell cell0 = row0.createCell(0);
            cell0.setCellValue("SINH VIÊN: " + maSV);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

            Row row1 = sheet.createRow(1);
            Cell cell1 = row1.createCell(0);
            cell1.setCellValue("Niên khóa: " + cboNienKhoa.getValue() + "   Học kỳ: " + cboHocKy.getValue());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 2));

            // Header cột (bắt đầu row 3, index=3)
            Row header = sheet.createRow(3);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Tên môn học");
            header.createCell(2).setCellValue("Điểm Max");

            // Dữ liệu từ row 4, index=4
            int rowIdx = 4;
            for (BaoCaoPhieuDiemDTO dto : dsBaoCao) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(dto.getStt());
                r.createCell(1).setCellValue(dto.getTenMH());
                r.createCell(2).setCellValue(dto.getDiemMax());
            }

            // Auto-size cột 0..2
            for (int i = 0; i <= 2; i++) {
                sheet.autoSizeColumn(i);
            }

            // Ghi file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            showAlert("Thành công", "Đã xuất báo cáo ra Excel:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Xuất Excel thất bại:\n" + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String t, String m, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}

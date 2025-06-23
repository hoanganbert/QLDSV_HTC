package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.dto.BaoCaoDongHPDTO;
import com.project.QLDSV_HTC.service.BaoCaoService;
import com.project.QLDSV_HTC.service.LopService;
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
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class BaoCaoDongHPController {

    @FXML private ComboBox<com.project.QLDSV_HTC.entity.Lop> cboLop;
    @FXML private ComboBox<String> cboNienKhoa;
    @FXML private ComboBox<Integer> cboHocKy;

    @FXML private Button btnChay;
    @FXML private Button btnXuatExcel;
    @FXML private Button btnClose;

    @FXML private TableView<BaoCaoDongHPDTO> tableBaoCao;
    @FXML private TableColumn<BaoCaoDongHPDTO, Integer> colSTT;
    @FXML private TableColumn<BaoCaoDongHPDTO, String>  colHoTen;
    @FXML private TableColumn<BaoCaoDongHPDTO, Double>  colHocPhi;
    @FXML private TableColumn<BaoCaoDongHPDTO, Double>  colSoDaDong;

    @FXML private Label lblTongSV;
    @FXML private Label lblTongTien;

    private ObservableList<BaoCaoDongHPDTO> dsBaoCao = FXCollections.observableArrayList();
    private ObservableList<String>            dsNienKhoa = FXCollections.observableArrayList("2023-2024", "2024-2025", "2025-2026");
    private ObservableList<Integer>           dsHocKy    = FXCollections.observableArrayList(1, 2, 3);

    @Autowired private BaoCaoService baoCaoService;
    @Autowired private LopService lopService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        if ("PGV".equals(appContext.getRole())) {
            cboLop.setItems(FXCollections.observableArrayList(lopService.getAllLop()));
        } else if ("KHOA".equals(appContext.getRole())) {
            String maKhoa = appContext.getMaKhoa();
            cboLop.setItems(FXCollections.observableArrayList(lopService.getLopByKhoa(maKhoa)));
        } else {
            cboLop.setItems(FXCollections.observableArrayList());
            cboLop.setDisable(true);
        }

        // 2. Load Niên khoá, Học kỳ
        cboNienKhoa.setItems(dsNienKhoa);
        cboHocKy   .setItems(dsHocKy);

        // 3. Cấu hình TableView
        colSTT    .setCellValueFactory(new PropertyValueFactory<>("stt"));
        colHoTen  .setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colHocPhi .setCellValueFactory(new PropertyValueFactory<>("hocPhi"));
        colSoDaDong.setCellValueFactory(new PropertyValueFactory<>("soDaDong"));

        tableBaoCao.setItems(dsBaoCao);

        // 4. Nút “Chạy”
        btnChay.setOnAction(e -> chayBaoCao());

        // 5. Nút “Xuất Excel”
        btnXuatExcel.setOnAction(e -> xuatExcel());

        // 6. Nút “Đóng”
        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void chayBaoCao() {
        com.project.QLDSV_HTC.entity.Lop lop = cboLop.getValue();
        String nk = cboNienKhoa.getValue();
        Integer hk = cboHocKy.getValue();

        if (lop == null || nk == null || hk == null) {
            showAlert("Thông báo", "Chọn đầy đủ: Lớp, Niên khóa, Học kỳ.", Alert.AlertType.WARNING);
            return;
        }
        String maLop = lop.getMaLop();

        List<BaoCaoDongHPDTO> list = baoCaoService.getBaoCaoDongHP(maLop, nk, hk);
        dsBaoCao.setAll(list);

        // Tính tổng số SV và tổng tiền
        int tongSV = list.size();
        double tongTien = list.stream().mapToDouble(BaoCaoDongHPDTO::getSoDaDong).sum();

        lblTongSV.setText("Tổng số sinh viên: " + tongSV);
        // Hiển thị tiền theo định dạng VN
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        lblTongTien.setText("Tổng số tiền đã đóng: " + nf.format(tongTien) + " đ");
    }

    private void xuatExcel() {
        if (dsBaoCao.isEmpty()) {
            showAlert("Thông báo", "Chưa có dữ liệu để xuất.", Alert.AlertType.WARNING);
            return;
        }

        com.project.QLDSV_HTC.entity.Lop lop = cboLop.getValue();
        String maLop = lop.getMaLop();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo Đóng Học Phí");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("BaoCao_DongHP_" + maLop + "_" + timestamp + ".xlsx");

        File file = fileChooser.showSaveDialog(btnXuatExcel.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DongHocPhi");

            // Tiêu đề
            Row row0 = sheet.createRow(0);
            Cell cell0 = row0.createCell(0);
            cell0.setCellValue("LỚP: " + maLop + "   Niên khóa: " + cboNienKhoa.getValue() + "   Học kỳ: " + cboHocKy.getValue());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

            // Header cột (tại row 2)
            Row header = sheet.createRow(2);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Họ và tên");
            header.createCell(2).setCellValue("Học phí");
            header.createCell(3).setCellValue("Số tiền đã đóng");

            // Data từ row 3
            int rowIdx = 3;
            for (BaoCaoDongHPDTO dto : dsBaoCao) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(dto.getStt());
                r.createCell(1).setCellValue(dto.getHo() + " " + dto.getTen());
                r.createCell(2).setCellValue(dto.getHocPhi());
                r.createCell(3).setCellValue(dto.getSoDaDong());
            }

            // Dòng tổng
            rowIdx += 1;
            Row sumRow = sheet.createRow(rowIdx);
            Cell c0 = sumRow.createCell(0);
            c0.setCellValue(
                "Tổng số sinh viên: " + dsBaoCao.size() +
                "    Tổng tiền đã đóng: " + NumberFormat.getInstance(new Locale("vi","VN"))
                                                .format(dsBaoCao.stream()
                                                               .mapToDouble(BaoCaoDongHPDTO::getSoDaDong)
                                                               .sum()) + " đ"
            );
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, 3));

            // Auto-size
            for (int i = 0; i <= 3; i++) {
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

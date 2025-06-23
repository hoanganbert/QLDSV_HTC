package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.dto.BaoCaoLTCDTO;
import com.project.QLDSV_HTC.entity.LopTinChi;
import com.project.QLDSV_HTC.service.LopTinChiService;
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

@Component
public class BaoCaoLTCController {

    @FXML private ComboBox<String> cboNienKhoa;
    @FXML private ComboBox<Integer> cboHocKy;
    @FXML private Button btnChay;
    @FXML private TableView<BaoCaoLTCDTO> tableBaoCao;
    @FXML private TableColumn<BaoCaoLTCDTO, Integer> colSTT;
    @FXML private TableColumn<BaoCaoLTCDTO, String> colTenMH;
    @FXML private TableColumn<BaoCaoLTCDTO, Integer> colNhom;
    @FXML private TableColumn<BaoCaoLTCDTO, String> colHoTenGV;
    @FXML private TableColumn<BaoCaoLTCDTO, Integer> colSoSVMin;
    @FXML private TableColumn<BaoCaoLTCDTO, Integer> colSoSVDaDK;
    @FXML private Button btnXuatExcel;
    @FXML private Button btnClose;

    private ObservableList<BaoCaoLTCDTO> dsBaoCao = FXCollections.observableArrayList();

    @Autowired private LopTinChiService ltcService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        cboNienKhoa.setItems(FXCollections.observableArrayList("2023-2024", "2024-2025", "2025-2026"));
        cboHocKy.setItems(FXCollections.observableArrayList(1, 2, 3));

        colSTT.setCellValueFactory(new PropertyValueFactory<>("stt"));
        colTenMH.setCellValueFactory(new PropertyValueFactory<>("tenMH"));
        colNhom.setCellValueFactory(new PropertyValueFactory<>("nhom"));
        colHoTenGV.setCellValueFactory(new PropertyValueFactory<>("hoTenGV"));
        colSoSVMin.setCellValueFactory(new PropertyValueFactory<>("soSVMin"));
        colSoSVDaDK.setCellValueFactory(new PropertyValueFactory<>("soSVDaDK"));

        tableBaoCao.setItems(dsBaoCao);

        btnChay.setOnAction(e -> chayBaoCao());

        btnXuatExcel.setOnAction(e -> xuatExcel());

        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void chayBaoCao() {
        String nk = cboNienKhoa.getSelectionModel().getSelectedItem();
        Integer hk = cboHocKy.getSelectionModel().getSelectedItem();
        if (nk == null || hk == null) {
            showAlert("Thông báo", "Chọn đủ Niên Khóa, Học Kỳ.", Alert.AlertType.WARNING);
            return;
        }

        // Nếu PGV, hiển thị tất cả; nếu KHOA, chỉ hiển thị theo khoa của họ
        List<LopTinChi> list;
        if ("PGV".equals(appContext.getRole())) {
            list = ltcService.findByNienKhoaHocKy(nk, hk);
        } else {
            String maKhoa = appContext.getMaKhoa();
            list = ltcService.findByNienKhoaHocKyAndKhoa(nk, hk, maKhoa);
        }

        dsBaoCao.clear();
        int stt = 1;
        for (LopTinChi ltc : list) {
            BaoCaoLTCDTO dto = new BaoCaoLTCDTO();
            dto.setStt(stt++);
            dto.setTenMH(ltc.getMonHoc().getTenMH());
            dto.setNhom(ltc.getNhom());
            dto.setHoTenGV(ltc.getGiangVien().getHo() + " " + ltc.getGiangVien().getTen());
            dto.setSoSVMin(ltc.getSoSVToiThieu());
            int soSVDaDK = ltcService.countSVDaDK(ltc.getMaLTC());
            dto.setSoSVDaDK(soSVDaDK);
            dsBaoCao.add(dto);
        }
    }

    private void xuatExcel() {
        if (dsBaoCao.isEmpty()) {
            showAlert("Thông báo", "Chưa có dữ liệu để xuất.", Alert.AlertType.WARNING);
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu báo cáo DS LTC");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("BaoCao_LTC_" + timestamp + ".xlsx");
        File file = fileChooser.showSaveDialog(btnXuatExcel.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DS_LTC");
            Row row0 = sheet.createRow(0);
            Cell cell0 = row0.createCell(0);
            cell0.setCellValue("KHOA: " + appContext.getTenKhoa());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            Row row1 = sheet.createRow(1);
            Cell cell1 = row1.createCell(0);
            cell1.setCellValue("Niên khóa: " + cboNienKhoa.getValue() + "   Học kỳ: " + cboHocKy.getValue());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 5));

            Row header = sheet.createRow(3);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Tên môn học");
            header.createCell(2).setCellValue("Nhóm");
            header.createCell(3).setCellValue("Họ tên GV");
            header.createCell(4).setCellValue("SV tối thiểu");
            header.createCell(5).setCellValue("SV đã đăng ký");

            int rowIdx = 4;
            for (BaoCaoLTCDTO dto : dsBaoCao) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(dto.getStt());
                r.createCell(1).setCellValue(dto.getTenMH());
                r.createCell(2).setCellValue(dto.getNhom());
                r.createCell(3).setCellValue(dto.getHoTenGV());
                r.createCell(4).setCellValue(dto.getSoSVMin());
                r.createCell(5).setCellValue(dto.getSoSVDaDK());
            }

            Row lastRow = sheet.createRow(rowIdx + 1);
            Cell c = lastRow.createCell(0);
            c.setCellValue("Số lượng lớp đã mở: " + dsBaoCao.size());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx + 1, rowIdx + 1, 0, 5));

            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            showAlert("Thành công", "Đã xuất báo cáo ra Excel:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Xuất Excel thất bại: " + ex.getMessage(), Alert.AlertType.ERROR);
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

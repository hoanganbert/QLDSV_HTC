package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.dto.BaoCaoDKSVDTO;
import com.project.QLDSV_HTC.entity.Khoa;
import com.project.QLDSV_HTC.entity.MonHoc;
import com.project.QLDSV_HTC.service.BaoCaoService;
import com.project.QLDSV_HTC.service.KhoaService;
import com.project.QLDSV_HTC.service.MonHocService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class BaoCaoDKSVController {

    @FXML private ComboBox<String>       cboNienKhoa;
    @FXML private ComboBox<Integer>      cboHocKy;
    @FXML private ComboBox<MonHoc>       cboMonHoc;
    @FXML private ComboBox<Integer>      cboNhom;

    @FXML private Button btnChay;
    @FXML private Button btnXuatExcel;
    @FXML private Button btnClose;

    @FXML private TableView<BaoCaoDKSVDTO> tableBaoCao;
    @FXML private TableColumn<BaoCaoDKSVDTO, Integer> colSTT;
    @FXML private TableColumn<BaoCaoDKSVDTO, String>  colMaSV;
    @FXML private TableColumn<BaoCaoDKSVDTO, String>  colHo;
    @FXML private TableColumn<BaoCaoDKSVDTO, String>  colTen;
    @FXML private TableColumn<BaoCaoDKSVDTO, String>  colPhai;
    @FXML private TableColumn<BaoCaoDKSVDTO, String>  colMaLop;

    private final ObservableList<String>      dsNienKhoa = FXCollections.observableArrayList("2023-2024", "2024-2025", "2025-2026");
    private final ObservableList<Integer>     dsHocKy    = FXCollections.observableArrayList(1, 2, 3);
    private final ObservableList<Integer>     dsNhom     = FXCollections.observableArrayList(1, 2, 3, 4, 5);
    private final ObservableList<BaoCaoDKSVDTO> dsBaoCao  = FXCollections.observableArrayList();

    @Autowired private BaoCaoService    baoCaoService;
    @Autowired private MonHocService    monHocService;
    @Autowired private KhoaService    khoaService;

    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        cboNienKhoa.setItems(dsNienKhoa);
        cboHocKy.setItems(dsHocKy);
        cboMonHoc.setItems(FXCollections.observableArrayList(monHocService.getAllMonHoc()));
        cboNhom.setItems(dsNhom);

        colSTT  .setCellValueFactory(new PropertyValueFactory<>("stt"));
        colMaSV .setCellValueFactory(new PropertyValueFactory<>("maSV"));
        colHo   .setCellValueFactory(new PropertyValueFactory<>("ho"));
        colTen  .setCellValueFactory(new PropertyValueFactory<>("ten"));
        colPhai .setCellValueFactory(new PropertyValueFactory<>("phai"));
        colMaLop.setCellValueFactory(new PropertyValueFactory<>("maLop"));
        tableBaoCao.setItems(dsBaoCao);

        btnChay     .setOnAction(e -> runReport());
        btnXuatExcel.setOnAction(e -> exportExcel());
        btnClose    .setOnAction(e -> ((Window)btnClose.getScene().getWindow()).hide());
    }

    private void runReport() {
        String nk   = cboNienKhoa.getValue();
        Integer hk  = cboHocKy.getValue();
        MonHoc mh   = cboMonHoc.getValue();
        Integer nhom= cboNhom.getValue();

        if (nk == null || hk == null || mh == null || nhom == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn đầy đủ Niên khóa, Học kỳ, Môn học và Nhóm.");
            return;
        }

        String maKhoa = "PGV".equals(appContext.getRole()) ? null : appContext.getMaKhoa();
        List<BaoCaoDKSVDTO> list = baoCaoService.getBaoCaoDKSV(
            maKhoa, nk, hk, mh.getMaMH(), nhom
        );
        dsBaoCao.setAll(list);
        if (list.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Không có dữ liệu báo cáo.");
        }
    }

    @FXML
    private void exportExcel() {
        if (dsBaoCao.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa có dữ liệu để xuất.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Xuất Excel");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx")
        );
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        chooser.setInitialFileName("BaoCao_DKSV_" + ts + ".xlsx");
        File file = chooser.showSaveDialog(btnXuatExcel.getScene().getWindow());
        if (file == null) return;

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("DSDKLTC");

            Row r0 = sheet.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("DANH SÁCH SINH VIÊN ĐĂNG KÝ LỚP TÍN CHỈ");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            String tenKhoa = "PGV".equals(appContext.getRole())
                ? "Tất cả"
                : khoaService.findById(appContext.getMaKhoa())
                           .map(Khoa::getTenKhoa)
                           .orElse("");
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("KHOA: " + tenKhoa);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(
                "Niên khóa: " + cboNienKhoa.getValue()
              + "   Học kỳ: "   + cboHocKy.getValue()
            );
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));

            Row r3 = sheet.createRow(3);
            String tenMH = cboMonHoc.getSelectionModel().getSelectedItem().getTenMH();
            Integer nhom = cboNhom.getValue();
            r3.createCell(0).setCellValue(
                "Môn học: " + tenMH
              + " – Nhóm: " + nhom
            );
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 5));

            int headerRow = 5;
            Row hdr = sheet.createRow(headerRow);
            hdr.createCell(0).setCellValue("STT");
            hdr.createCell(1).setCellValue("Mã SV");
            hdr.createCell(2).setCellValue("Họ");
            hdr.createCell(3).setCellValue("Tên");
            hdr.createCell(4).setCellValue("Phái");
            hdr.createCell(5).setCellValue("Mã lớp");

            // Dữ liệu: sort theo Tên rồi Họ
            List<BaoCaoDKSVDTO> sorted = new ArrayList<>(dsBaoCao);
            sorted.sort(Comparator
                .comparing(BaoCaoDKSVDTO::getTen, String::compareToIgnoreCase)
                .thenComparing(BaoCaoDKSVDTO::getHo, String::compareToIgnoreCase)
            );

            int rowIdx = headerRow + 1;
            for (BaoCaoDKSVDTO dto : sorted) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dto.getStt());
                row.createCell(1).setCellValue(dto.getMaSV());
                row.createCell(2).setCellValue(dto.getHo());
                row.createCell(3).setCellValue(dto.getTen());
                row.createCell(4).setCellValue(dto.getPhai());
                row.createCell(5).setCellValue(dto.getMaLop());
            }
            
            Row footer = sheet.createRow(rowIdx++);
            Cell footCell = footer.createCell(0);
            footCell.setCellValue("Số sinh viên đã đăng ký: " + dsBaoCao.size());
            sheet.addMergedRegion(new CellRangeAddress(
                rowIdx - 1,  
                rowIdx - 1,  
                0,           
                5            
            ));

            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            showAlert(Alert.AlertType.INFORMATION, "Đã xuất Excel thành công: " + file.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi khi xuất Excel:\n" + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

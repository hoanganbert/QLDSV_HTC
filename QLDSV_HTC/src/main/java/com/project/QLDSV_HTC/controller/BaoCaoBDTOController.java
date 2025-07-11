package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.service.BaoCaoService;
import com.project.QLDSV_HTC.service.LopService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
public class BaoCaoBDTOController {

    @FXML private ComboBox<com.project.QLDSV_HTC.entity.Lop> cboLop;
    @FXML private Button btnChay;
    @FXML private Button btnXuatExcel;
    @FXML private Button btnClose;

    @FXML private TableView<ObservableList<Object>> tableBaoCao; 

    private ObservableList<ObservableList<Object>> dsBaoCao = FXCollections.observableArrayList();

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


        tableBaoCao.setItems(dsBaoCao);

        btnChay.setOnAction(e -> chayBaoCao());

        btnXuatExcel.setOnAction(e -> xuatExcel());

        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void chayBaoCao() {
        com.project.QLDSV_HTC.entity.Lop lop = cboLop.getValue();
        if (lop == null) {
            showAlert("Thông báo", "Chọn Mã Lớp trước khi chạy.", Alert.AlertType.WARNING);
            return;
        }
        String maLop = lop.getMaLop();

        com.project.QLDSV_HTC.dto.BieuDoBDDTO result = baoCaoService.getBaoCaoBDDTO(maLop);

        tableBaoCao.getColumns().clear();
        dsBaoCao.clear();

        TableColumn<ObservableList<Object>, Object> colMaSVHoTen = new TableColumn<>("MãSV - Họ tên");
        colMaSVHoTen.setCellValueFactory(param -> {
            return new ReadOnlyObjectWrapper<>(param.getValue().get(0));
        });
        tableBaoCao.getColumns().add(colMaSVHoTen);

        List<String> subjectNames = result.getSubjectNames(); 
        for (int i = 0; i < subjectNames.size(); i++) {
            final int colIndex = i + 1;
            String subj = subjectNames.get(i);
            TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(subj);
            col.setCellValueFactory(param -> {
                return new ReadOnlyObjectWrapper<>(param.getValue().get(colIndex));
            });
            tableBaoCao.getColumns().add(col);
        }

        //Đẩy dữ liệu vào dsBaoCao (mỗi dòng là 1 ObservableList<Object>)
        for (List<Object> row : result.getData()) {
            ObservableList<Object> obsRow = FXCollections.observableArrayList(row);
            dsBaoCao.add(obsRow);
        }
    }

    private void xuatExcel() {
        if (dsBaoCao.isEmpty() || tableBaoCao.getColumns().isEmpty()) {
            showAlert("Thông báo", "Chưa có dữ liệu để xuất.", Alert.AlertType.WARNING);
            return;
        }

        com.project.QLDSV_HTC.entity.Lop lop = cboLop.getValue();
        String maLop = lop.getMaLop();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo Bảng Điểm Tổng kết");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("BaoCao_BDTO_" + maLop + "_" + timestamp + ".xlsx");

        File file = fileChooser.showSaveDialog(btnXuatExcel.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("BangDiemTongKet");

            Row row0 = sheet.createRow(0);
            Cell cell0 = row0.createCell(0);
            cell0.setCellValue("BẢNG ĐIỂM TỔNG KẾT CUỐI KHÓA - LỚP: " + maLop);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, tableBaoCao.getColumns().size() - 1));

            Row header = sheet.createRow(2);
            for (int colIndex = 0; colIndex < tableBaoCao.getColumns().size(); colIndex++) {
                String colName = tableBaoCao.getColumns().get(colIndex).getText();
                header.createCell(colIndex).setCellValue(colName);
            }

            int rowIdx = 3;
            for (ObservableList<Object> obsRow : dsBaoCao) {
                Row excelRow = sheet.createRow(rowIdx++);
                for (int j = 0; j < obsRow.size(); j++) {
                    Object cellVal = obsRow.get(j);
                    Cell c = excelRow.createCell(j);
                    if (cellVal instanceof Number) {
                        c.setCellValue(((Number) cellVal).doubleValue());
                    } else {
                        c.setCellValue(cellVal == null ? "" : cellVal.toString());
                    }
                }
            }

            for (int i = 0; i < tableBaoCao.getColumns().size(); i++) {
                sheet.autoSizeColumn(i);
            }

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

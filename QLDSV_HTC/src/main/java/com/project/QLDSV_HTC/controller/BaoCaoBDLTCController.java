package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.dto.BaoCaoBDLTCDTO;
import com.project.QLDSV_HTC.entity.MonHoc;
import com.project.QLDSV_HTC.service.BaoCaoService;
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
public class BaoCaoBDLTCController {

    @FXML private ComboBox<String> cboNienKhoa;
    @FXML private ComboBox<Integer> cboHocKy;
    @FXML private ComboBox<com.project.QLDSV_HTC.entity.MonHoc> cboMonHoc;
    @FXML private ComboBox<Integer> cboNhom;

    @FXML private Button btnChay;
    @FXML private Button btnXuatExcel;
    @FXML private Button btnClose;

    @FXML private TableView<BaoCaoBDLTCDTO> tableBaoCao;
    @FXML private TableColumn<BaoCaoBDLTCDTO, Integer> colSTT;
    @FXML private TableColumn<BaoCaoBDLTCDTO, String>  colMaSV;
    @FXML private TableColumn<BaoCaoBDLTCDTO, String>  colHo;
    @FXML private TableColumn<BaoCaoBDLTCDTO, String>  colTen;
    @FXML private TableColumn<BaoCaoBDLTCDTO, Integer> colDiemCC;
    @FXML private TableColumn<BaoCaoBDLTCDTO, Double>  colDiemGK;
    @FXML private TableColumn<BaoCaoBDLTCDTO, Double>  colDiemCK;
    @FXML private TableColumn<BaoCaoBDLTCDTO, Double>  colDiemHM;

    private ObservableList<BaoCaoBDLTCDTO> dsBaoCao = FXCollections.observableArrayList();
    private ObservableList<String>    dsNienKhoa = FXCollections.observableArrayList("2023-2024", "2024-2025", "2025-2026");
    private ObservableList<Integer>   dsHocKy    = FXCollections.observableArrayList(1, 2, 3);
    private ObservableList<Integer>   dsNhom     = FXCollections.observableArrayList(1, 2, 3, 4, 5);

    @Autowired private BaoCaoService baoCaoService;
    @Autowired private AppContextHolder appContext;
    @Autowired
    private com.project.QLDSV_HTC.service.MonHocService monHocService;

    @FXML
    public void initialize() {
        cboNienKhoa.setItems(dsNienKhoa);
        cboHocKy  .setItems(dsHocKy);
        cboMonHoc.setItems(FXCollections.observableArrayList(
        	      monHocService.getAllMonHoc()
        	));
        cboNhom   .setItems(dsNhom);

        colSTT  .setCellValueFactory(new PropertyValueFactory<>("stt"));
        colMaSV .setCellValueFactory(new PropertyValueFactory<>("maSV"));
        colHo   .setCellValueFactory(new PropertyValueFactory<>("ho"));
        colTen  .setCellValueFactory(new PropertyValueFactory<>("ten"));
        colDiemCC.setCellValueFactory(new PropertyValueFactory<>("diemCC"));
        colDiemGK.setCellValueFactory(new PropertyValueFactory<>("diemGK"));
        colDiemCK.setCellValueFactory(new PropertyValueFactory<>("diemCK"));
        colDiemHM.setCellValueFactory(new PropertyValueFactory<>("diemHM"));

        tableBaoCao.setItems(dsBaoCao);

        btnChay.setOnAction(e -> chayBaoCao());

        btnXuatExcel.setOnAction(e -> xuatExcel());

        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    private void chayBaoCao() {
        String nk   = cboNienKhoa.getValue();
        Integer hk  = cboHocKy.getValue();
        MonHoc   mh = cboMonHoc.getValue();
        Integer nh  = cboNhom.getValue();

        if (nk==null||hk==null||mh==null||nh==null) {
            showAlert("Thông báo",
                "Chọn đầy đủ Niên khóa, Học kỳ, Môn học, Nhóm.",
                Alert.AlertType.WARNING);
            return;
        }

        String maKhoa = "PGV".equals(appContext.getRole())
            ? null
            : appContext.getMaKhoa();

        List<BaoCaoBDLTCDTO> list = baoCaoService.getBaoCaoBDLTC(
            maKhoa, nk, hk, mh.getMaMH(), nh);

        for (BaoCaoBDLTCDTO dto : list) {
            dto.computeDiemHM();
        }

        dsBaoCao.setAll(list);

        if (list.isEmpty()) {
            showAlert("Thông báo",
                "Không có dữ liệu báo cáo.",
                Alert.AlertType.INFORMATION);
        }
    }

    private void xuatExcel() {
        if (dsBaoCao.isEmpty()) {
            showAlert("Thông báo", "Chưa có dữ liệu để xuất.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo Bảng Điểm LTC");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("BaoCao_BDLTC_" + timestamp + ".xlsx");

        File file = fileChooser.showSaveDialog(btnXuatExcel.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("BangDiemLTC");

            Row row0 = sheet.createRow(0);
            Cell cell0 = row0.createCell(0);
            cell0.setCellValue("KHOA: " + appContext.getTenKhoa());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7));

            Row row1 = sheet.createRow(1);
            Cell cell1 = row1.createCell(0);
            cell1.setCellValue(
                "Niên khóa: " + cboNienKhoa.getValue() +
                "   Học kỳ: " + cboHocKy.getValue() +
                "   Môn học: " + cboMonHoc.getValue().getTenMH() +
                "   Nhóm: " + cboNhom.getValue()
            );
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 7));

            Row header = sheet.createRow(3);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Mã SV");
            header.createCell(2).setCellValue("Họ");
            header.createCell(3).setCellValue("Tên");
            header.createCell(4).setCellValue("Điểm CC");
            header.createCell(5).setCellValue("Điểm GK");
            header.createCell(6).setCellValue("Điểm CK");
            header.createCell(7).setCellValue("Điểm hết môn");

            int rowIdx = 4;
            for (BaoCaoBDLTCDTO dto : dsBaoCao) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(dto.getStt());
                r.createCell(1).setCellValue(dto.getMaSV());
                r.createCell(2).setCellValue(dto.getHo());
                r.createCell(3).setCellValue(dto.getTen());
                r.createCell(4).setCellValue(dto.getDiemCC());
                r.createCell(5).setCellValue(dto.getDiemGK());
                r.createCell(6).setCellValue(dto.getDiemCK());
                r.createCell(7).setCellValue(dto.getDiemHM());
            }
            
            Row footer = sheet.createRow(rowIdx++);
            Cell cellFooter = footer.createCell(0);
            cellFooter.setCellValue("Số sinh viên: " + dsBaoCao.size());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                rowIdx-1,   
                rowIdx-1,   
                0,          
                7           
            ));

            for (int i = 0; i <= 7; i++) {
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

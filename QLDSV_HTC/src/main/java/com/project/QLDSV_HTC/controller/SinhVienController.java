package com.project.QLDSV_HTC.controller;

import com.project.QLDSV_HTC.entity.Lop;
import com.project.QLDSV_HTC.entity.Sinhvien;
import com.project.QLDSV_HTC.service.LopService;
import com.project.QLDSV_HTC.service.SinhVienService;
import com.project.QLDSV_HTC.util.AppContextHolder;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SinhVienController {

    @FXML private ComboBox<Lop> cboLop;
    @FXML private TextField txtMaSV;
    @FXML private TextField txtHo;
    @FXML private TextField txtTen;
    @FXML private DatePicker dpNgaySinh;
    @FXML private TextField txtDiaChi;
    @FXML private ComboBox<String> cboPhai;
    @FXML private CheckBox chkNghiHoc;
    @FXML private PasswordField txtPassword;

    @FXML private Button btnAddSV;
    @FXML private Button btnUpdateSV;
    @FXML private Button btnDeleteSV;
    @FXML private Button btnRefreshSV;
    @FXML private Button btnCloseSV;

    @FXML private TableView<Sinhvien> tableSV;
    @FXML private TableColumn<Sinhvien, String> colMaSV;
    @FXML private TableColumn<Sinhvien, String> colHo;
    @FXML private TableColumn<Sinhvien, String> colTen;
    @FXML private TableColumn<Sinhvien, String> colPhai;
    @FXML private TableColumn<Sinhvien, String> colNgaySinh;
    @FXML private TableColumn<Sinhvien, String> colDiaChi;
    @FXML private TableColumn<Sinhvien, Boolean> colNghiHoc;
    @FXML private TableColumn<Sinhvien, String> colMaLop;

    private ObservableList<Sinhvien> dsSV = FXCollections.observableArrayList();
    private ObservableList<Lop> dsLop = FXCollections.observableArrayList();
    private ObservableList<String> dsPhai = FXCollections.observableArrayList("Nam", "Nữ");

    // Undo stack lưu trạng thái trước mỗi thao tác Update/Delete
    private Deque<Sinhvien> undoStack = new ArrayDeque<>();

    @Autowired private SinhVienService sinhVienService;
    @Autowired private LopService lopService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        String role = appContext.getRole();
        if (!"PGV".equals(role) && !"KHOA".equals(role)) {
            disableForm(); showAlert("Truy cập bị từ chối", "Bạn không có quyền quản lý Sinh viên.", Alert.AlertType.WARNING);
            return;
        }

        // Load dsLop và dsPhai
        dsLop.clear(); dsLop.addAll(lopService.getAllLop());
        if ("KHOA".equals(role)) {
            String maKhoaLogin = appContext.getMaKhoa();
            dsLop = FXCollections.observableArrayList(
                dsLop.stream().filter(l -> l.getKhoa().getMaKhoa().equals(maKhoaLogin)).collect(Collectors.toList())
            );
        }
        cboLop.setItems(dsLop);
        cboPhai.setItems(dsPhai);
        
        // Cấu hình table
        colMaSV.setCellValueFactory(new PropertyValueFactory<>("maSV"));
        colHo.setCellValueFactory(new PropertyValueFactory<>("ho"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("ten"));
        colPhai.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().isPhai()?"Nữ":"Nam"));
        colNgaySinh.setCellValueFactory(cell -> {
            LocalDate d = cell.getValue().getNgaySinh();
            return new ReadOnlyStringWrapper(d==null?"":d.toString());
        });
        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colNghiHoc.setCellValueFactory(new PropertyValueFactory<>("dangNghiHoc"));
        colMaLop.setCellValueFactory(cell -> {
            Lop l = cell.getValue().getLop();
            return new ReadOnlyStringWrapper(l==null?"":l.getMaLop());
        });

        // Selection listener
        tableSV.getSelectionModel().selectedItemProperty().addListener((obs, old, newSV) -> {
            if (newSV != null) bindForm(newSV);
        });

        // Add SV
        btnAddSV.setOnAction(e -> addSinhVien());

        // Update SV
        btnUpdateSV.setOnAction(e -> {
            Sinhvien sel = tableSV.getSelectionModel().getSelectedItem();
            if (sel==null) { showAlert("Thông báo","Chọn SV cần sửa.",Alert.AlertType.WARNING); return; }
            undoStack.push(new Sinhvien(sel));
            applyFormTo(sel);
            sinhVienService.save(sel);
            reloadSV(); clearForm();
        });

        // Delete SV
        btnDeleteSV.setOnAction(e -> {
            Sinhvien sel = tableSV.getSelectionModel().getSelectedItem();
            if (sel==null) { showAlert("Thông báo","Chọn SV cần xóa.",Alert.AlertType.WARNING); return; }
            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.setTitle("Xác nhận"); c.setContentText("Bạn có chắc muốn xóa SV " + sel.getMaSV() + "?");
            if (c.showAndWait().filter(b->b==ButtonType.OK).isPresent()) {
                undoStack.push(new Sinhvien(sel));
                sinhVienService.delete(sel.getMaSV());
                reloadSV(); clearForm();
            }
        });

        // Undo SV
        btnRefreshSV.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                Sinhvien prev = undoStack.pop();
                bindForm(prev);
                sinhVienService.save(prev);
                reloadSV();
            } else {
                clearForm(); reloadSV();
            }
        });

        btnCloseSV.setOnAction(e -> btnCloseSV.getScene().getWindow().hide());
    }

    private void reloadSV() {
        Lop lop = cboLop.getSelectionModel().getSelectedItem();
        if (lop!=null) loadSVTheoLop(lop.getMaLop());
    }

    private void loadSVTheoLop(String maLop) {
        List<Sinhvien> list = sinhVienService.getSVTheoLop(maLop);
        tableSV.setItems(FXCollections.observableArrayList(list));
    }

    private void bindForm(Sinhvien sv) {
        txtMaSV.setText(sv.getMaSV()); txtHo.setText(sv.getHo()); txtTen.setText(sv.getTen());
        dpNgaySinh.setValue(sv.getNgaySinh()); txtDiaChi.setText(sv.getDiaChi());
        cboPhai.getSelectionModel().select(sv.isPhai()?"Nữ":"Nam");
        chkNghiHoc.setSelected(sv.isDangNghiHoc()); txtPassword.setText(sv.getPassword());
        cboLop.getSelectionModel().select(sv.getLop()); txtMaSV.setDisable(true);
    }

    private void applyFormTo(Sinhvien sel) {
        sel.setHo(txtHo.getText().trim()); sel.setTen(txtTen.getText().trim());
        sel.setNgaySinh(dpNgaySinh.getValue()); sel.setDiaChi(txtDiaChi.getText().trim());
        sel.setPhai("Nữ".equals(cboPhai.getValue())); sel.setDangNghiHoc(chkNghiHoc.isSelected());
        sel.setPassword(txtPassword.getText().trim()); sel.setLop(cboLop.getValue());
    }

    private void addSinhVien() {
        Lop lop = cboLop.getSelectionModel().getSelectedItem();
        String ma = txtMaSV.getText().trim();
        String ho = txtHo.getText().trim();
        String ten = txtTen.getText().trim();
        LocalDate ns = dpNgaySinh.getValue();
        String dc = txtDiaChi.getText().trim();
        String phaiStr = cboPhai.getValue();
        boolean phaiBool = "Nữ".equals(phaiStr);
        boolean nghiHoc = chkNghiHoc.isSelected();
        String pwd = txtPassword.getText().trim();

        if (lop == null || ma.isEmpty() || ho.isEmpty() || ten.isEmpty()
                || ns == null || dc.isEmpty() || phaiStr == null) {
            showAlert("Lỗi", "Phải nhập đầy đủ thông tin SV.", Alert.AlertType.ERROR);
            return;
        }
        if (sinhVienService.existsById(ma)) {
            showAlert("Lỗi", "Mã SV đã tồn tại.", Alert.AlertType.WARNING);
            return;
        }

        Sinhvien sv = new Sinhvien();
        sv.setMaSV(ma);
        sv.setHo(ho);
        sv.setTen(ten);
        sv.setNgaySinh(ns);
        sv.setDiaChi(dc);
        sv.setPhai(phaiBool);
        sv.setDangNghiHoc(nghiHoc);
        sv.setPassword(pwd);
        sv.setLop(lop);

        sinhVienService.save(sv);
        // Reload lại danh sách SV trong lớp đang chọn
        loadSVTheoLop(lop.getMaLop());
        clearForm();
    }


    private void clearForm() {
        txtMaSV.clear(); txtHo.clear(); txtTen.clear(); dpNgaySinh.setValue(null);
        txtDiaChi.clear(); cboPhai.getSelectionModel().clearSelection(); chkNghiHoc.setSelected(false);
        txtPassword.clear(); txtMaSV.setDisable(false);
        tableSV.getSelectionModel().clearSelection();
    }

    private void disableForm() {
        cboLop.setDisable(true); txtMaSV.setDisable(true); txtHo.setDisable(true);
        txtTen.setDisable(true); dpNgaySinh.setDisable(true); txtDiaChi.setDisable(true);
        cboPhai.setDisable(true); chkNghiHoc.setDisable(true); txtPassword.setDisable(true);
        btnAddSV.setDisable(true); btnUpdateSV.setDisable(true); btnDeleteSV.setDisable(true);
        btnRefreshSV.setDisable(true); btnCloseSV.setText("Đóng"); tableSV.setDisable(true);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}

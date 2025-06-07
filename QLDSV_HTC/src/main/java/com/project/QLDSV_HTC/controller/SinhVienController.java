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

    @Autowired private SinhVienService sinhVienService;
    @Autowired private LopService lopService;
    @Autowired private AppContextHolder appContext;

    @FXML
    public void initialize() {
        // Chỉ PGV và KHOA mới được quản lý SV
        String role = appContext.getRole();
        if (!"PGV".equals(role) && !"KHOA".equals(role)) {
            disableForm();
            showAlert("Truy cập bị từ chối", "Bạn không có quyền quản lý Sinh viên.", Alert.AlertType.WARNING);
            return;
        }

        // 1. Load ComboBox Lớp
        dsLop.clear();
        dsLop.addAll(lopService.getAllLop());
        if ("KHOA".equals(role)) {
            // Nếu là KHOA, chỉ hiển thị các lớp thuộc khoa đăng nhập
            String maKhoaLogin = appContext.getMaKhoa();
            dsLop = FXCollections.observableArrayList(
                dsLop.stream()
                     .filter(l -> l.getKhoa().getMaKhoa().equals(maKhoaLogin))
                     .collect(Collectors.toList())
            );
        }
        cboLop.setItems(dsLop);

        // 2. Load ComboBox Phái
        cboPhai.setItems(dsPhai);

        // 3. Cấu hình TableView Sinh viên
        colMaSV.setCellValueFactory(new PropertyValueFactory<>("maSV"));
        colHo.setCellValueFactory(new PropertyValueFactory<>("ho"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("ten"));

        // Col Phái: chuyển boolean → "Nam"/"Nữ"
        colPhai.setCellValueFactory(cell -> {
            boolean phaiBool = cell.getValue().isPhai();     // isPhai() trả về boolean
            String phaiStr = phaiBool ? "Nữ" : "Nam";
            return new ReadOnlyStringWrapper(phaiStr);
        });

        // Col Ngày sinh: nếu trong entity dùng LocalDate
        colNgaySinh.setCellValueFactory(cell -> {
            LocalDate ngay = cell.getValue().getNgaySinh();  // getNgaySinh() trả về LocalDate
            String text = (ngay == null) ? "" : ngay.toString();
            return new ReadOnlyStringWrapper(text);
        });

        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colNghiHoc.setCellValueFactory(new PropertyValueFactory<>("dangNghiHoc")); 
        colMaLop.setCellValueFactory(cell -> {
            Lop lop = cell.getValue().getLop();
            String ma = (lop == null ? "" : lop.getMaLop());
            return new ReadOnlyStringWrapper(ma);
        });

        tableSV.setItems(dsSV);

        // 4. Khi chọn 1 SV trong table, đổ lên form
        tableSV.getSelectionModel().selectedItemProperty().addListener((obs, old, newSV) -> {
            if (newSV != null) {
                // Mã SV, Họ, Tên
                txtMaSV.setText(newSV.getMaSV());
                txtHo.setText(newSV.getHo());
                txtTen.setText(newSV.getTen());

                // Ngày sinh (LocalDate)
                dpNgaySinh.setValue(newSV.getNgaySinh());

                // Địa chỉ
                txtDiaChi.setText(newSV.getDiaChi());

                // Phái: boolean → "Nam"/"Nữ"
                cboPhai.getSelectionModel().select(newSV.isPhai() ? "Nữ" : "Nam");

                // Đang nghỉ học
                chkNghiHoc.setSelected(newSV.isDangNghiHoc());

                // Password
                txtPassword.setText(newSV.getPassword() == null ? "" : newSV.getPassword());

                // Chọn ComboBox Lớp dựa vào đối tượng Lop trong Sinhvien
                Lop lopOfSV = newSV.getLop();
                if (lopOfSV != null) {
                    for (Lop l : dsLop) {
                        if (l.getMaLop().equals(lopOfSV.getMaLop())) {
                            cboLop.getSelectionModel().select(l);
                            break;
                        }
                    }
                }

                // Khi sửa SV, không cho đổi mã
                txtMaSV.setDisable(true);
            }
        });

        // 5. Khi chọn 1 Lớp, load DS SV theo Lớp đó
        cboLop.getSelectionModel().selectedItemProperty().addListener((obs, old, newLop) -> {
            if (newLop != null) {
                loadSVTheoLop(newLop.getMaLop());
                clearForm();
            } else {
                dsSV.clear();
            }
        });

        // 6. Nút Thêm SV
        btnAddSV.setOnAction(e -> {
            Lop lop = cboLop.getSelectionModel().getSelectedItem();
            String ma = txtMaSV.getText().trim();
            String ho = txtHo.getText().trim();
            String ten = txtTen.getText().trim();
            LocalDate ns = dpNgaySinh.getValue();
            String dc = txtDiaChi.getText().trim();
            String phaiStr = cboPhai.getValue();
            boolean phaiBool = "Nữ".equals(phaiStr);    // chuyển "Nam"/"Nữ" → boolean
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
            sv.setNgaySinh(ns);            // dùng LocalDate trực tiếp
            sv.setDiaChi(dc);
            sv.setPhai(phaiBool);
            sv.setDangNghiHoc(nghiHoc);
            sv.setPassword(pwd);
            sv.setLop(lop);

            sinhVienService.save(sv);

            loadSVTheoLop(lop.getMaLop());
            clearForm();
        });

        // 7. Nút Ghi (Update) SV
        btnUpdateSV.setOnAction(e -> {
            Sinhvien sel = tableSV.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn SV cần sửa.", Alert.AlertType.WARNING);
                return;
            }
            String ho = txtHo.getText().trim();
            String ten = txtTen.getText().trim();
            LocalDate ns = dpNgaySinh.getValue();
            String dc = txtDiaChi.getText().trim();
            String phaiStr = cboPhai.getValue();
            boolean phaiBool = "Nữ".equals(phaiStr);
            boolean nghiHoc = chkNghiHoc.isSelected();
            String pwd = txtPassword.getText().trim();
            Lop lop = cboLop.getSelectionModel().getSelectedItem();

            if (ho.isEmpty() || ten.isEmpty() || ns == null
                    || dc.isEmpty() || phaiStr == null || lop == null) {
                showAlert("Lỗi", "Phải nhập đầy đủ thông tin SV.", Alert.AlertType.ERROR);
                return;
            }

            sel.setHo(ho);
            sel.setTen(ten);
            sel.setNgaySinh(ns);
            sel.setDiaChi(dc);
            sel.setPhai(phaiBool);
            sel.setDangNghiHoc(nghiHoc);
            sel.setPassword(pwd);
            sel.setLop(lop);

            sinhVienService.save(sel);

            loadSVTheoLop(lop.getMaLop());
            clearForm();
        });

        // 8. Nút Xóa SV
        btnDeleteSV.setOnAction(e -> {
            Sinhvien sel = tableSV.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Thông báo", "Chọn SV cần xóa.", Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setContentText("Bạn có chắc muốn xóa SV " + sel.getMaSV() + "?");
            if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
                sinhVienService.delete(sel.getMaSV());
                loadSVTheoLop(sel.getLop().getMaLop());
                clearForm();
            }
        });

        // 9. Nút Phục hồi (Refresh)
        btnRefreshSV.setOnAction(e -> {
            clearForm();
            Lop lop = cboLop.getSelectionModel().getSelectedItem();
            if (lop != null) {
                loadSVTheoLop(lop.getMaLop());
            }
        });

        // 10. Nút Thoát
        btnCloseSV.setOnAction(e -> btnCloseSV.getScene().getWindow().hide());
    }

    private void loadSVTheoLop(String maLop) {
        List<Sinhvien> list = sinhVienService.getSVTheoLop(maLop);
        dsSV.setAll(list);
        tableSV.setItems(dsSV);
    }

    private void clearForm() {
        txtMaSV.clear();
        txtHo.clear();
        txtTen.clear();
        dpNgaySinh.setValue(null);
        txtDiaChi.clear();
        cboPhai.getSelectionModel().clearSelection();
        chkNghiHoc.setSelected(false);
        txtPassword.clear();
        txtMaSV.setDisable(false);
        tableSV.getSelectionModel().clearSelection();
    }

    private void disableForm() {
        cboLop.setDisable(true);
        txtMaSV.setDisable(true);
        txtHo.setDisable(true);
        txtTen.setDisable(true);
        dpNgaySinh.setDisable(true);
        txtDiaChi.setDisable(true);
        cboPhai.setDisable(true);
        chkNghiHoc.setDisable(true);
        txtPassword.setDisable(true);
        btnAddSV.setDisable(true);
        btnUpdateSV.setDisable(true);
        btnDeleteSV.setDisable(true);
        btnRefreshSV.setDisable(true);
        btnCloseSV.setText("Đóng");
        tableSV.setDisable(true);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

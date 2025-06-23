package com.project.QLDSV_HTC.dto;

import javafx.beans.property.*;

/**
 * DiemDTO: dùng cho màn Nhập Điểm.
 * Lưu trữ mã SV, họ tên SV, điểm CC, GK, CK và điểm hết môn (tự tính).
 */
public class DiemDTO {
    // Các property để TableView có thể 2-chiều bind/edit
    private final StringProperty  maSV     = new SimpleStringProperty();
    private final StringProperty  hoTenSV  = new SimpleStringProperty();
    private final IntegerProperty diemCC   = new SimpleIntegerProperty(0);
    private final DoubleProperty  diemGK   = new SimpleDoubleProperty(0.0);
    private final DoubleProperty  diemCK   = new SimpleDoubleProperty(0.0);
    private final DoubleProperty  diemHM   = new SimpleDoubleProperty(0.0);

    public DiemDTO() {
        // khởi tính HM mặc định
        computeDiemHM();
    }

    // --- Property getters ---
    public StringProperty maSVProperty()     { return maSV; }
    public StringProperty hoTenSVProperty()  { return hoTenSV; }
    public IntegerProperty diemCCProperty()  { return diemCC; }
    public DoubleProperty diemGKProperty()   { return diemGK; }
    public DoubleProperty diemCKProperty()   { return diemCK; }
    public DoubleProperty diemHMProperty()   { return diemHM; }

    // --- JavaBean getters/setters ---
    public String getMaSV()        { return maSV.get(); }
    public void setMaSV(String v)  { maSV.set(v); }

    public String getHoTenSV()         { return hoTenSV.get(); }
    public void setHoTenSV(String v)   { hoTenSV.set(v); }

    public int getDiemCC()         { return diemCC.get(); }
    public void setDiemCC(int v)   { diemCC.set(v); }

    public double getDiemGK()      { return diemGK.get(); }
    public void setDiemGK(double v){ diemGK.set(v); }

    public double getDiemCK()      { return diemCK.get(); }
    public void setDiemCK(double v){ diemCK.set(v); }

    public double getDiemHM()      { return diemHM.get(); }
    private void setDiemHM(double v){ diemHM.set(v); }

    /**
     * Tính điểm hết môn = CC*0.1 + GK*0.3 + CK*0.6,
     * làm tròn đến .0 hoặc .5 (tối ưu cho tín chỉ).
     */
    public void computeDiemHM() {
        double cc = getDiemCC();
        double gk = getDiemGK();
        double ck = getDiemCK();

        double hm = cc * 0.1 + gk * 0.3 + ck * 0.6;
        // Làm tròn tới 0.5 gần nhất
        hm = Math.round(hm * 2) / 2.0;
        setDiemHM(hm);
    }
}

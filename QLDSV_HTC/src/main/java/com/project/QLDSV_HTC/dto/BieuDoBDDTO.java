package com.project.QLDSV_HTC.dto;

import java.util.List;

/**
 * 7. BieuDoBDDTO (tên tắt của “Bảng Điểm Tổng kết DTO”):
 *    - dùng cho báo cáo cross-tab “Bảng điểm tổng kết cuối khóa” (BaoCaoBDTOController).
 *    - subjectNames: danh sách tên môn học làm header
 *    - data: mỗi phần tử là một List<Object> tương ứng một dòng:
 *        + data.get(i).get(0): "MãSV – Họ tên"
 *        + data.get(i).get(j) (với j≥1): điểm cuối khóa cho môn thứ (j−1)
 */
public class BieuDoBDDTO {
    private List<String> subjectNames;
    private List<List<Object>> data;

    public BieuDoBDDTO() {
    }

    public List<String> getSubjectNames() {
        return subjectNames;
    }

    public void setSubjectNames(List<String> subjectNames) {
        this.subjectNames = subjectNames;
    }

    public List<List<Object>> getData() {
        return data;
    }

    public void setData(List<List<Object>> data) {
        this.data = data;
    }
}

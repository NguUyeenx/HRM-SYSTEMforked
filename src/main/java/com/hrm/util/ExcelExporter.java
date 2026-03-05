package com.hrm.util;

import com.hrm.model.ChiTietLuong;
import com.hrm.model.ThanhPhanLuong;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Tiện ích xuất bảng lương ra file Excel (.xlsx).
 * - exportHienTai(): chỉ xuất các cột đang hiển thị (theo colHidden[])
 * - exportDayDu():   xuất toàn bộ 12 cột + sheet "Chi tiet phu cap"
 */
public class ExcelExporter {

    private static final String[] ALL_COLS = {
        "Ma NV", "Ho ten", "Luong chinh", "Ngay cong",
        "Gio lam", "Gio OT", "Phu cap", "Khau tru",
        "Tien OT", "Tong thu nhap", "Thuc nhan", "Trang thai"
    };

    // Index các cột cần format tiền
    private static final boolean[] IS_MONEY = {
        false, false, true, false,
        false, false, true, true,
        true,  true,  true, false
    };

    // ─────────────────────────────────────────────────────────
    //  XUẤT HIỆN TẠI — chỉ các cột đang hiển thị
    // ─────────────────────────────────────────────────────────
    public static void exportHienTai(String filePath, List<ChiTietLuong> ds,
                                     boolean[] colHidden, int thang, int nam,
                                     Function<Integer, String> getMaNhanVien)
            throws IOException {

        // Xây danh sách index gốc của các cột ĐANG HIỆN
        int[] visibleIdx = buildVisibleIdx(colHidden);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Styles s = new Styles(wb);
            XSSFSheet sheet = wb.createSheet(String.format("BangLuong_T%02d_%d", thang, nam));

            int rowNum = writeTitleRow(sheet, s,
                String.format("BANG LUONG THANG %02d/%d", thang, nam),
                visibleIdx.length);

            writeHeaderRow(sheet, s, rowNum++, visibleIdx);
            writeDataRows(sheet, s, rowNum, ds, visibleIdx, getMaNhanVien);
            rowNum += ds.size();
            writeFooterRow(sheet, s, rowNum, ds, visibleIdx);

            autoSize(sheet, visibleIdx.length);
            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  XUẤT ĐẦY ĐỦ — tất cả cột + sheet phụ cấp
    // ─────────────────────────────────────────────────────────
    public static void exportDayDu(String filePath, List<ChiTietLuong> ds,
                                   int thang, int nam,
                                   Function<Integer, String> getMaNhanVien)
            throws IOException {

        int[] allIdx = new int[ALL_COLS.length];
        for (int i = 0; i < allIdx.length; i++) allIdx[i] = i;

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Styles s = new Styles(wb);

            // ── Sheet 1: Tổng hợp ──
            XSSFSheet sheet1 = wb.createSheet(String.format("BangLuong_T%02d_%d", thang, nam));
            int rowNum = writeTitleRow(sheet1, s,
                String.format("BANG LUONG DAY DU THANG %02d/%d", thang, nam),
                ALL_COLS.length);
            writeHeaderRow(sheet1, s, rowNum++, allIdx);
            writeDataRows(sheet1, s, rowNum, ds, allIdx, getMaNhanVien);
            rowNum += ds.size();
            writeFooterRow(sheet1, s, rowNum, ds, allIdx);
            autoSize(sheet1, ALL_COLS.length);

            // ── Sheet 2: Chi tiết phụ cấp ──
            XSSFSheet sheet2 = wb.createSheet("Chi tiet phu cap");
            writeSheet2(sheet2, s, ds, getMaNhanVien);

            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  CÁC METHOD HELPER VIẾT TỪNG PHẦN
    // ─────────────────────────────────────────────────────────

    private static int writeTitleRow(XSSFSheet sheet, Styles s, String title, int colSpan) {
        Row row = sheet.createRow(0);
        row.setHeightInPoints(28);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(s.title);
        if (colSpan > 1)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colSpan - 1));
        // Dòng trắng ở giữa
        sheet.createRow(1);
        return 2; // header bắt đầu từ dòng 2
    }

    private static void writeHeaderRow(XSSFSheet sheet, Styles s, int rowNum, int[] visibleIdx) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(22);
        for (int col = 0; col < visibleIdx.length; col++) {
            Cell cell = row.createCell(col);
            cell.setCellValue(ALL_COLS[visibleIdx[col]]);
            cell.setCellStyle(s.header);
        }
    }

    private static void writeDataRows(XSSFSheet sheet, Styles s, int startRow,
                                      List<ChiTietLuong> ds, int[] visibleIdx,
                                      Function<Integer, String> getMaNhanVien) {
        int rowNum = startRow;
        for (ChiTietLuong ct : ds) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(20);
            Object[] data = buildRowData(ct, getMaNhanVien.apply(ct.getMaNV()));
            for (int col = 0; col < visibleIdx.length; col++) {
                int origIdx = visibleIdx[col];
                Cell cell = row.createCell(col);
                Object val = data[origIdx];
                if (val instanceof Double d) {
                    cell.setCellValue(d);
                    cell.setCellStyle(IS_MONEY[origIdx] ? s.money : s.number);
                } else {
                    cell.setCellValue(val != null ? val.toString() : "");
                    cell.setCellStyle(s.normal);
                }
            }
        }
    }

    /**
     * Viết hàng TỔNG CỘNG ở cuối.
     *
     * Logic đúng:
     *   - Duyệt qua từng cột ĐANG HIỂN THỊ (visibleIdx)
     *   - Nếu cột đó là cột tổng (index gốc 7, 8, 10) → ghi giá trị tổng
     *   - Nếu cột đó là cột "Ho ten" (index gốc 1) → ghi label "TONG CONG (N NV)"
     *   - Còn lại → ô trống
     *
     * Bằng cách này, dù xuất ít hay nhiều cột đều KHÔNG bị mất ô tổng.
     */
    private static void writeFooterRow(XSSFSheet sheet, Styles s, int rowNum,
                                       List<ChiTietLuong> ds, int[] visibleIdx) {
        if (ds.isEmpty()) return;

        // Tính tổng trước
        double tongThucNhan   = ds.stream().mapToDouble(ChiTietLuong::getLuongThucNhan).sum();
        double tongTienOT     = ds.stream().mapToDouble(ChiTietLuong::getTienOT).sum();
        double tongKhauTru    = ds.stream().mapToDouble(ChiTietLuong::getTongKhauTru).sum();
        double tongThuNhap    = ds.stream().mapToDouble(ChiTietLuong::getTongLuong).sum();
        double tongLuongChinh = ds.stream().mapToDouble(ChiTietLuong::getLuongCoBan).sum();
        double tongPhuCap     = ds.stream().mapToDouble(ChiTietLuong::getTongLuongChucVu).sum();

        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(22);

        for (int col = 0; col < visibleIdx.length; col++) {
            int origIdx = visibleIdx[col];
            Cell cell = row.createCell(col);

            switch (origIdx) {
                case 0 -> { // Ma NV
                    cell.setCellValue("TONG");
                    cell.setCellStyle(s.footerLabel);
                }
                case 1 -> { // Ho ten — đặt label chính
                    cell.setCellValue(ds.size() + " (NV)");
                    cell.setCellStyle(s.footerLabel);
                }
                case 2 -> { // Luong chinh
                    cell.setCellValue(tongLuongChinh);
                    cell.setCellStyle(s.footerMoney);
                }
                case 6 -> { // Phu cap
                    cell.setCellValue(tongPhuCap);
                    cell.setCellStyle(s.footerMoney);
                }
                case 7 -> { // Khau tru
                    cell.setCellValue(tongKhauTru);
                    cell.setCellStyle(s.footerMoney);
                }
                case 8 -> { // Tien OT
                    cell.setCellValue(tongTienOT);
                    cell.setCellStyle(s.footerMoney);
                }
                case 9 -> { // Tong thu nhap
                    cell.setCellValue(tongThuNhap);
                    cell.setCellStyle(s.footerMoney);
                }
                case 10 -> { // Thuc nhan — quan trọng nhất
                    cell.setCellValue(tongThucNhan);
                    cell.setCellStyle(s.footerMoneyHighlight);
                }
                default -> { // Ngay cong, Gio lam, Gio OT, Trang thai → trống
                    cell.setCellValue("");
                    cell.setCellStyle(s.footerLabel);
                }
            }
        }
    }

    private static void writeSheet2(XSSFSheet sheet, Styles s,
                                    List<ChiTietLuong> ds,
                                    Function<Integer, String> getMaNhanVien) {
        String[] cols2 = {"Ma NV", "Ho ten", "Loai", "Ten khoan", "So tien"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < cols2.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols2[i]);
            c.setCellStyle(s.header);
        }
        int r = 1;
        for (ChiTietLuong ct : ds) {
            String maNV = getMaNhanVien.apply(ct.getMaNV());
            for (ThanhPhanLuong tp : ct.getDanhSachThanhPhan()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(maNV);
                row.createCell(1).setCellValue(ct.getTenNV());
                row.createCell(2).setCellValue(tp.getLoai().getDisplayName());
                row.createCell(3).setCellValue(tp.getTenKhoan());
                Cell mc = row.createCell(4);
                mc.setCellValue(tp.getSoTien());
                mc.setCellStyle(s.money);
            }
        }
        for (int i = 0; i < cols2.length; i++) sheet.autoSizeColumn(i);
    }

    // ─────────────────────────────────────────────────────────
    //  UTILITIES
    // ─────────────────────────────────────────────────────────

    /** Tạo mảng dữ liệu 1 hàng theo đúng thứ tự 12 cột gốc */
    private static Object[] buildRowData(ChiTietLuong ct, String maNV) {
        return new Object[]{
            maNV,                                    // 0  Ma NV
            ct.getTenNV(),                           // 1  Ho ten
            ct.getLuongCoBan(),                      // 2  Luong chinh
            (double) ct.getSoNgayCong(),             // 3  Ngay cong
            ct.getTongGioLam(),                      // 4  Gio lam
            ct.getTongGioOT(),                       // 5  Gio OT
            ct.getTongLuongChucVu(),                 // 6  Phu cap
            ct.getTongKhauTru(),                     // 7  Khau tru
            ct.getTienOT(),                          // 8  Tien OT
            ct.getTongLuong(),                       // 9  Tong thu nhap
            ct.getLuongThucNhan(),                   // 10 Thuc nhan
            ct.getTrangThai().getDisplayName()       // 11 Trang thai
        };
    }

    /** Lấy danh sách index gốc của các cột đang hiện (colHidden[i] == false) */
    private static int[] buildVisibleIdx(boolean[] colHidden) {
        int count = 0;
        for (boolean h : colHidden) if (!h) count++;
        int[] idx = new int[count];
        int vi = 0;
        for (int i = 0; i < colHidden.length; i++) {
            if (!colHidden[i]) idx[vi++] = i;
        }
        return idx;
    }

    private static void autoSize(XSSFSheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
            // Thêm padding nhỏ tránh text bị cắt
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 512, 15000));
        }
    }

    // ─────────────────────────────────────────────────────────
    //  STYLES — gom tất cả CellStyle vào 1 class nội bộ
    // ─────────────────────────────────────────────────────────
    private static class Styles {
        final CellStyle title, header, normal, number, money;
        final CellStyle footerLabel, footerMoney, footerMoneyHighlight;

        Styles(XSSFWorkbook wb) {
            DataFormat fmt = wb.createDataFormat();
            short moneyFmt = fmt.getFormat("#,##0");
            short numFmt   = fmt.getFormat("0.0");

            // Title
            title = wb.createCellStyle();
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            title.setFont(titleFont);
            title.setAlignment(HorizontalAlignment.CENTER);
            title.setVerticalAlignment(VerticalAlignment.CENTER);

            // Header
            header = wb.createCellStyle();
            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);
            header.setFont(headerFont);
            header.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(header, BorderStyle.THIN, IndexedColors.GREY_50_PERCENT);

            // Normal (text)
            normal = wb.createCellStyle();
            setBorder(normal, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT);

            // Number (giờ, ngày công)
            number = wb.createCellStyle();
            number.setDataFormat(numFmt);
            number.setAlignment(HorizontalAlignment.CENTER);
            setBorder(number, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT);

            // Money
            money = wb.createCellStyle();
            money.setDataFormat(moneyFmt);
            money.setAlignment(HorizontalAlignment.RIGHT);
            setBorder(money, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT);

            // Footer label
            footerLabel = wb.createCellStyle();
            XSSFFont footerFont = wb.createFont();
            footerFont.setBold(true);
            footerLabel.setFont(footerFont);
            footerLabel.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            footerLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            footerLabel.setBorderTop(BorderStyle.MEDIUM);
            footerLabel.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());

            // Footer money
            footerMoney = wb.createCellStyle();
            footerMoney.cloneStyleFrom(footerLabel);
            footerMoney.setDataFormat(moneyFmt);
            footerMoney.setAlignment(HorizontalAlignment.RIGHT);

            // Footer money highlight (Thực nhận)
            footerMoneyHighlight = wb.createCellStyle();
            footerMoneyHighlight.cloneStyleFrom(footerMoney);
            XSSFFont hlFont = wb.createFont();
            hlFont.setBold(true);
            hlFont.setFontHeightInPoints((short) 13);
            hlFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            footerMoneyHighlight.setFont(hlFont);
            footerMoneyHighlight.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            footerMoneyHighlight.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        private static void setBorder(CellStyle style, BorderStyle bs, IndexedColors color) {
            style.setBorderTop(bs);    style.setTopBorderColor(color.getIndex());
            style.setBorderBottom(bs); style.setBottomBorderColor(color.getIndex());
            style.setBorderLeft(bs);   style.setLeftBorderColor(color.getIndex());
            style.setBorderRight(bs);  style.setRightBorderColor(color.getIndex());
        }
    }
}
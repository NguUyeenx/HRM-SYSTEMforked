package com.hrm.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho ChamCong model.
 */
class ChamCongTest {

    @Test
    @DisplayName("Constructor mac dinh: soGioLam = 0, trangThai = DUNG_GIO")
    void constructor_macDinh() {
        ChamCong cc = new ChamCong();

        assertEquals(0, cc.getSoGioLam());
        assertEquals(0, cc.getGioLamThem());
        assertEquals(ChamCong.TrangThai.DUNG_GIO, cc.getTrangThai());
        assertEquals(ChamCong.PhuongThuc.THU_CONG, cc.getPhuongThucChamCong());
    }

    @Test
    @DisplayName("Constructor check-in: set maNV, ngay, gioVao tu dong")
    void constructor_checkIn() {
        ChamCong cc = new ChamCong(5, LocalDate.of(2026, 2, 21), "HANH_CHINH");

        assertEquals(5, cc.getMaNV());
        assertEquals(LocalDate.of(2026, 2, 21), cc.getNgay());
        assertEquals("HANH_CHINH", cc.getMaCaLam());
        assertNotNull(cc.getGioVao(), "Gio vao phai duoc set tu dong");
    }

    // ====================================================
    // TEST TÍNH GIỜ LÀM
    // ====================================================

    @Test
    @DisplayName("tinhSoGioLam: 08:00 → 17:00 = 9.0 gio")
    void tinhSoGioLam_caNgay_traVe9Gio() {
        ChamCong cc = new ChamCong();
        cc.setGioVao(LocalDateTime.of(2026, 2, 21, 8, 0));
        cc.setGioRa(LocalDateTime.of(2026, 2, 21, 17, 0));

        assertEquals(9.0, cc.tinhSoGioLam(), 0.01);
    }

    @Test
    @DisplayName("tinhSoGioLam: 08:30 → 17:45 = 9.25 gio")
    void tinhSoGioLam_coPhutLe_traVeDung() {
        ChamCong cc = new ChamCong();
        cc.setGioVao(LocalDateTime.of(2026, 2, 21, 8, 30));
        cc.setGioRa(LocalDateTime.of(2026, 2, 21, 17, 45));

        assertEquals(9.25, cc.tinhSoGioLam(), 0.01);
    }

    @Test
    @DisplayName("tinhSoGioLam: chua check-out → tra ve 0")
    void tinhSoGioLam_chuaCheckOut_traVe0() {
        ChamCong cc = new ChamCong();
        cc.setGioVao(LocalDateTime.of(2026, 2, 21, 8, 0));
        // gioRa = null

        assertEquals(0, cc.tinhSoGioLam());
    }

    @Test
    @DisplayName("tinhSoGioLam: chua check-in → tra ve 0")
    void tinhSoGioLam_chuaCheckIn_traVe0() {
        ChamCong cc = new ChamCong();
        // cả gioVao và gioRa đều null

        assertEquals(0, cc.tinhSoGioLam());
    }

    // ====================================================
    // TEST TRẠNG THÁI CHECK-IN/OUT
    // ====================================================

    @Test
    @DisplayName("daCheckIn / daCheckOut: kiem tra logic")
    void trangThaiCheckInOut() {
        ChamCong cc = new ChamCong();

        // Ban đầu: chưa check gì
        assertFalse(cc.daCheckIn());
        assertFalse(cc.daCheckOut());
        assertFalse(cc.hoanTat());

        // Sau check-in
        cc.setGioVao(LocalDateTime.now());
        assertTrue(cc.daCheckIn());
        assertFalse(cc.daCheckOut());
        assertFalse(cc.hoanTat());

        // Sau check-out
        cc.setGioRa(LocalDateTime.now());
        assertTrue(cc.daCheckIn());
        assertTrue(cc.daCheckOut());
        assertTrue(cc.hoanTat(), "Ca 2 gio vao va gio ra co → hoan tat");
    }

    // ====================================================
    // TEST ENUM
    // ====================================================

    @Test
    @DisplayName("TrangThai enum: mapping dung DB values")
    void trangThai_fromDbValue() {
        assertEquals(ChamCong.TrangThai.DUNG_GIO,
                ChamCong.TrangThai.fromDbValue("dung_gio"));
        assertEquals(ChamCong.TrangThai.DI_MUON,
                ChamCong.TrangThai.fromDbValue("di_muon"));
        assertEquals(ChamCong.TrangThai.VE_SOM,
                ChamCong.TrangThai.fromDbValue("ve_som"));
        assertEquals(ChamCong.TrangThai.VANG_MAT,
                ChamCong.TrangThai.fromDbValue("vang_mat"));
    }

    @Test
    @DisplayName("PhuongThuc enum: mapping dung DB values")
    void phuongThuc_fromDbValue() {
        assertEquals(ChamCong.PhuongThuc.WIFI,
                ChamCong.PhuongThuc.fromDbValue("wifi"));
        assertEquals(ChamCong.PhuongThuc.THU_CONG,
                ChamCong.PhuongThuc.fromDbValue("thu_cong"));
    }
}
package com.hrm.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho DangKyLamThem model.
 */
class DangKyLamThemTest {

    @Test
    @DisplayName("Constructor mac dinh: trang thai = CHO_DUYET")
    void constructor_macDinh() {
        DangKyLamThem dk = new DangKyLamThem();

        assertEquals(DangKyLamThem.TrangThai.CHO_DUYET, dk.getTrangThai());
        assertNull(dk.getNguoiDuyet(), "Chua ai duyet → null");
        assertNull(dk.getNgayDuyet(), "Chua duyet → ngayDuyet null");
        assertNotNull(dk.getNgayTao());
    }

    @Test
    @DisplayName("Constructor day du: set dung cac field")
    void constructor_dayDu() {
        DangKyLamThem dk = new DangKyLamThem(
                5, LocalDate.of(2026, 2, 21), 2.5, "Hoan thanh bao cao");

        assertEquals(5, dk.getMaNV());
        assertEquals(LocalDate.of(2026, 2, 21), dk.getNgay());
        assertEquals(2.5, dk.getSoGio(), 0.01);
        assertEquals("Hoan thanh bao cao", dk.getLyDo());
        assertEquals(DangKyLamThem.TrangThai.CHO_DUYET, dk.getTrangThai());
    }

    // ====================================================
    // TEST WORKFLOW: DUYỆT / TỪ CHỐI
    // ====================================================

    @Test
    @DisplayName("duyet(): set trang thai, nguoi duyet, ngay duyet cung luc")
    void duyet_setDung3Fields() {
        // Arrange
        DangKyLamThem dk = new DangKyLamThem(5, LocalDate.now(), 2.0, "OT");

        // Act
        dk.duyet(10); // Quản lý ID = 10 duyệt

        // Assert
        assertEquals(DangKyLamThem.TrangThai.DA_DUYET, dk.getTrangThai());
        assertEquals(10, dk.getNguoiDuyet());
        assertNotNull(dk.getNgayDuyet(), "Ngay duyet phai duoc set tu dong");
    }

    @Test
    @DisplayName("tuChoi(): set trang thai TU_CHOI")
    void tuChoi_setDungTrangThai() {
        DangKyLamThem dk = new DangKyLamThem(5, LocalDate.now(), 2.0, "OT");

        dk.tuChoi(10);

        assertEquals(DangKyLamThem.TrangThai.TU_CHOI, dk.getTrangThai());
        assertEquals(10, dk.getNguoiDuyet());
        assertNotNull(dk.getNgayDuyet());
    }

    // ====================================================
    // TEST HELPER METHODS
    // ====================================================

    @Test
    @DisplayName("dangChoDuyet: don moi → true, don da duyet → false")
    void dangChoDuyet_kiemTraDung() {
        DangKyLamThem dk = new DangKyLamThem(5, LocalDate.now(), 2.0, "OT");

        assertTrue(dk.dangChoDuyet(), "Don moi phai dang cho duyet");

        dk.duyet(10);
        assertFalse(dk.dangChoDuyet(), "Don da duyet → khong con cho");
    }

    @Test
    @DisplayName("daDuocDuyet: chi true khi trang thai = DA_DUYET")
    void daDuocDuyet_kiemTraDung() {
        DangKyLamThem dk = new DangKyLamThem(5, LocalDate.now(), 2.0, "OT");

        assertFalse(dk.daDuocDuyet(), "Don moi chua duoc duyet");

        dk.duyet(10);
        assertTrue(dk.daDuocDuyet(), "Sau khi duyet phai tra ve true");
    }

    @Test
    @DisplayName("daDuocDuyet: don bi tu choi → false")
    void daDuocDuyet_biTuChoi_traVeFalse() {
        DangKyLamThem dk = new DangKyLamThem(5, LocalDate.now(), 2.0, "OT");

        dk.tuChoi(10);
        assertFalse(dk.daDuocDuyet(),
                "Don bi tu choi khong phai 'da duoc duyet'");
    }

    // ====================================================
    // TEST ENUM
    // ====================================================

    @Test
    @DisplayName("TrangThai.fromDbValue: tat ca gia tri hop le")
    void trangThai_fromDbValue_hopLe() {
        assertEquals(DangKyLamThem.TrangThai.CHO_DUYET,
                DangKyLamThem.TrangThai.fromDbValue("cho_duyet"));
        assertEquals(DangKyLamThem.TrangThai.DA_DUYET,
                DangKyLamThem.TrangThai.fromDbValue("da_duyet"));
        assertEquals(DangKyLamThem.TrangThai.TU_CHOI,
                DangKyLamThem.TrangThai.fromDbValue("tu_choi"));
    }

    @Test
    @DisplayName("TrangThai.fromDbValue: gia tri sai → exception")
    void trangThai_fromDbValue_sai_throwException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DangKyLamThem.TrangThai.fromDbValue("abc");
        });
    }
}
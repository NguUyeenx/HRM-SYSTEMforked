package com.hrm.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho CaLam model.
 *
 * ĐÂY LÀ FILE TEST ĐẦU TIÊN — Đọc kỹ comment để hiểu cách viết test!
 *
 * CẤU TRÚC MỘT TEST METHOD:
 *   @Test                    ← Đánh dấu đây là method test
 *   @DisplayName("...")      ← Tên hiển thị khi chạy (đọc dễ hơn tên method)
 *   void tenMethod() {
 *       // 1. ARRANGE — Chuẩn bị dữ liệu
 *       // 2. ACT     — Thực hiện hành động cần test
 *       // 3. ASSERT  — Kiểm tra kết quả có đúng không
 *   }
 *
 * PATTERN NÀY GỌI LÀ: AAA (Arrange - Act - Assert)
 * Là cách viết test chuẩn trong ngành.
 */
class CaLamTest {

    // ====================================================
    // BIẾN DÙNG CHUNG CHO CÁC TEST
    // ====================================================
    //
    // @BeforeEach: method này chạy TRƯỚC MỖI @Test method
    // → Đảm bảo mỗi test bắt đầu với dữ liệu "sạch"
    // → Không test nào ảnh hưởng đến test khác
    //
    private CaLam caHanhChinh;
    private CaLam caDem;

    @BeforeEach
    void setUp() {
        // Ca hành chính: 08:00 - 17:00
        caHanhChinh = new CaLam("HANH_CHINH", "Ca hanh chinh",
                LocalTime.of(8, 0), LocalTime.of(17, 0));

        // Ca đêm: 22:00 - 06:00 (qua ngày)
        caDem = new CaLam("CA_DEM", "Ca dem",
                LocalTime.of(22, 0), LocalTime.of(6, 0));
    }

    // ====================================================
    // TEST CONSTRUCTOR
    // ====================================================

    @Test
    @DisplayName("Constructor mac dinh: soGioChuan = 8, trangThai = HOAT_DONG")
    void constructor_macDinh_setGiaTriDefault() {
        // Arrange & Act
        CaLam caLam = new CaLam();

        // Assert
        // assertEquals(expected, actual): kỳ vọng == thực tế?
        assertEquals(8.00, caLam.getSoGioChuan(), "So gio chuan phai la 8");
        assertTrue(caLam.isChoPhepLamThem(), "Mac dinh phai cho phep lam them");
        assertEquals(CaLam.TrangThai.HOAT_DONG, caLam.getTrangThai(),
                "Trang thai mac dinh phai la HOAT_DONG");
        assertNotNull(caLam.getNgayTao(), "Ngay tao khong duoc null");
    }

    @Test
    @DisplayName("Constructor co tham so: set dung ma, ten, gio")
    void constructor_coThamSo_setDungGiaTri() {
        // Assert — kiểm tra constructor có set đúng giá trị không
        assertEquals("HANH_CHINH", caHanhChinh.getMaCaLam());
        assertEquals("Ca hanh chinh", caHanhChinh.getTenCaLam());
        assertEquals(LocalTime.of(8, 0), caHanhChinh.getGioBatDau());
        assertEquals(LocalTime.of(17, 0), caHanhChinh.getGioKetThuc());

        // Kiểm tra default values vẫn được set (từ this())
        assertEquals(8.00, caHanhChinh.getSoGioChuan());
        assertTrue(caHanhChinh.isChoPhepLamThem());
    }

    // ====================================================
    // TEST HELPER METHODS
    // ====================================================

    @Test
    @DisplayName("laCaDem: ca hanh chinh KHONG phai ca dem")
    void laCaDem_caHanhChinh_traVeFalse() {
        // Ca hành chính: 08:00 → 17:00 (kết thúc SAU bắt đầu → KHÔNG phải ca đêm)
        assertFalse(caHanhChinh.laCaDem());
    }

    @Test
    @DisplayName("laCaDem: ca dem (22:00-06:00) LA ca dem")
    void laCaDem_caDem_traVeTrue() {
        // Ca đêm: 22:00 → 06:00 (kết thúc TRƯỚC bắt đầu → là ca đêm)
        assertTrue(caDem.laCaDem());
    }

    @Test
    @DisplayName("conHoatDong: trang thai HOAT_DONG → true")
    void conHoatDong_hoatDong_traVeTrue() {
        caHanhChinh.setTrangThai(CaLam.TrangThai.HOAT_DONG);
        assertTrue(caHanhChinh.conHoatDong());
    }

    @Test
    @DisplayName("conHoatDong: trang thai NGUNG → false")
    void conHoatDong_ngungHoatDong_traVeFalse() {
        caHanhChinh.setTrangThai(CaLam.TrangThai.NGUNG_HOAT_DONG);
        assertFalse(caHanhChinh.conHoatDong());
    }

    // ====================================================
    // TEST ENUM
    // ====================================================

    @Test
    @DisplayName("TrangThai.fromDbValue: 'hoat_dong' → HOAT_DONG")
    void fromDbValue_hopLe_traVeEnum() {
        assertEquals(CaLam.TrangThai.HOAT_DONG,
                CaLam.TrangThai.fromDbValue("hoat_dong"));
        assertEquals(CaLam.TrangThai.NGUNG_HOAT_DONG,
                CaLam.TrangThai.fromDbValue("ngung_hoat_dong"));
    }

    @Test
    @DisplayName("TrangThai.fromDbValue: gia tri khong hop le → throw exception")
    void fromDbValue_khongHopLe_throwException() {
        // assertThrows: kỳ vọng code bên trong SẼ ném exception
        // Nếu KHÔNG ném → test FAIL
        assertThrows(IllegalArgumentException.class, () -> {
            CaLam.TrangThai.fromDbValue("khong_ton_tai");
        });
    }

    @Test
    @DisplayName("toString: hien thi ten + gio")
    void toString_hienThiDungFormat() {
        String result = caHanhChinh.toString();
        // Kiểm tra chứa tên ca và thời gian
        assertTrue(result.contains("Ca hanh chinh"));
        assertTrue(result.contains("08:00"));
        assertTrue(result.contains("17:00"));
    }
}
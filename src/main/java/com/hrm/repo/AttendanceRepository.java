package com.hrm.repo;

import com.hrm.model.*;
import com.hrm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;          // ← chỉ dùng java.sql.Date (KHÔNG import java.util.Date)
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * AttendanceRepository — Phiên bản MySQL.
 * Mọi method giữ nguyên tên và kiểu trả về như phiên bản in-memory.
 * Service layer và UI không cần sửa gì cả.
 */
public class AttendanceRepository {

    private static AttendanceRepository instance;

    private AttendanceRepository() {}

    public static synchronized AttendanceRepository getInstance() {
        if (instance == null) instance = new AttendanceRepository();
        return instance;
    }

    // ================================================================
    // HELPER: map ResultSet → Object
    // ================================================================

    private CaLam mapCaLam(ResultSet rs) throws SQLException {
        CaLam ca = new CaLam();
        ca.setMaCaLam(rs.getString("maCaLam"));
        ca.setTenCaLam(rs.getString("tenCaLam"));
        ca.setGioBatDau(rs.getTime("gioBatDau").toLocalTime());
        ca.setGioKetThuc(rs.getTime("gioKetThuc").toLocalTime());
        ca.setSoGioChuan(rs.getDouble("soGioChuan"));
        ca.setChoPhepLamThem(rs.getBoolean("choPhepLamThem"));
        ca.setTrangThai(rs.getBoolean("hoatDong")
                ? CaLam.TrangThai.HOAT_DONG
                : CaLam.TrangThai.NGUNG_HOAT_DONG);
        return ca;
    }

    private ChamCong mapChamCong(ResultSet rs) throws SQLException {
        ChamCong cc = new ChamCong();
        cc.setMaChamCong(rs.getInt("maChamCong"));
        cc.setMaNV(rs.getInt("maNV"));
        cc.setNgay(rs.getDate("ngay").toLocalDate());

        String maCaLam = rs.getString("maCaLam");
        if (maCaLam != null) cc.setMaCaLam(maCaLam);

        try { cc.setTenCaLam(rs.getString("tenCaLam")); } catch (SQLException ignored) {}
        try { cc.setEmployeeName(rs.getString("hoTen")); } catch (SQLException ignored) {}

        Timestamp gioVao = rs.getTimestamp("gioVao");
        if (gioVao != null) cc.setGioVao(gioVao.toLocalDateTime());

        Timestamp gioRa = rs.getTimestamp("gioRa");
        if (gioRa != null) cc.setGioRa(gioRa.toLocalDateTime());

        cc.setSoGioLam(rs.getDouble("soGioLam"));
        cc.setGioLamThem(rs.getDouble("gioLamThem"));
        cc.setTrangThai(ChamCong.TrangThai.fromDbValue(rs.getString("trangThai")));
        cc.setPhuongThucChamCong(ChamCong.PhuongThuc.fromDbValue(rs.getString("phuongThucChamCong")));

        String ghiChu = rs.getString("ghiChu");
        if (ghiChu != null) cc.setGhiChu(ghiChu);

        Timestamp ngayTao = rs.getTimestamp("ngayTao");
        if (ngayTao != null) cc.setNgayTao(ngayTao.toLocalDateTime());

        return cc;
    }

    private DangKyLamThem mapDonOT(ResultSet rs) throws SQLException {
        DangKyLamThem don = new DangKyLamThem();
        don.setMaDK(rs.getInt("maDK"));
        don.setMaNV(rs.getInt("maNV"));
        don.setNgay(rs.getDate("ngay").toLocalDate());
        don.setSoGio(rs.getDouble("soGio"));
        don.setLyDo(rs.getString("lyDo"));
        don.setHeSoOT(rs.getDouble("heSoOT"));
        don.setNhanXet(rs.getString("nhanXet"));
        don.setTrangThai(DangKyLamThem.TrangThai.fromDbValue(rs.getString("trangThai")));

        try { don.setEmployeeName(rs.getString("hoTen")); } catch (SQLException ignored) {}

        int nguoiDuyet = rs.getInt("nguoiDuyet");
        if (!rs.wasNull()) don.setNguoiDuyet(nguoiDuyet);

        Timestamp ngayDuyet = rs.getTimestamp("ngayDuyet");
        if (ngayDuyet != null) don.setNgayDuyet(ngayDuyet.toLocalDateTime());

        Timestamp ngayTao = rs.getTimestamp("ngayTao");
        if (ngayTao != null) don.setNgayTao(ngayTao.toLocalDateTime());

        return don;
    }

    private CauHinhPhuCap mapCauHinh(ResultSet rs) throws SQLException {
        CauHinhPhuCap pc = new CauHinhPhuCap();
        pc.setMaPC(rs.getInt("maCauHinh"));
        pc.setLoai(ThanhPhanLuong.Loai.fromDbValue(rs.getString("loai")));
        pc.setTenKhoan(rs.getString("tenKhoan"));
        pc.setKieuTinh(CauHinhPhuCap.KieuTinh.fromDbValue(rs.getString("kieuTinh")));
        pc.setGiaTriMacDinh(rs.getDouble("giaTriMacDinh"));
        pc.setNguon(rs.getString("nguon"));
        pc.setHoatDong(rs.getBoolean("hoatDong"));
        return pc;
    }

    // ================================================================
    // CA LÀM
    // ================================================================

    public List<CaLam> findCaLamHoatDong() {
        List<CaLam> list = new ArrayList<>();
        String sql = "SELECT * FROM CALAM WHERE hoatDong = TRUE ORDER BY maCaLam";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCaLam(rs));
        } catch (SQLException e) {
            System.err.println("[DB] findCaLamHoatDong lỗi: " + e.getMessage());
        }
        return list;
    }

    public List<CaLam> findAllCaLam() {
        List<CaLam> list = new ArrayList<>();
        String sql = "SELECT * FROM CALAM ORDER BY maCaLam";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCaLam(rs));
        } catch (SQLException e) {
            System.err.println("[DB] findAllCaLam lỗi: " + e.getMessage());
        }
        return list;
    }

    public CaLam findCaLamByMa(String ma) {
        String sql = "SELECT * FROM CALAM WHERE maCaLam = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCaLam(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DB] findCaLamByMa lỗi: " + e.getMessage());
        }
        return null;
    }

    public CaLam saveCaLam(CaLam ca) {
        String sql = """
            INSERT INTO CALAM (maCaLam, tenCaLam, gioBatDau, gioKetThuc, soGioChuan, choPhepLamThem, hoatDong)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                tenCaLam = VALUES(tenCaLam),
                gioBatDau = VALUES(gioBatDau),
                gioKetThuc = VALUES(gioKetThuc),
                soGioChuan = VALUES(soGioChuan),
                choPhepLamThem = VALUES(choPhepLamThem),
                hoatDong = VALUES(hoatDong)
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ca.getMaCaLam());
            ps.setString(2, ca.getTenCaLam());
            ps.setTime(3, Time.valueOf(ca.getGioBatDau()));
            ps.setTime(4, Time.valueOf(ca.getGioKetThuc()));
            ps.setDouble(5, ca.getSoGioChuan());
            ps.setBoolean(6, ca.isChoPhepLamThem());
            ps.setBoolean(7, ca.conHoatDong());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveCaLam lỗi: " + e.getMessage());
        }
        return ca;
    }

    public boolean deleteCaLam(String ma) {
        String sql = "UPDATE CALAM SET hoatDong = FALSE WHERE maCaLam = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ma);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] deleteCaLam lỗi: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // CHẤM CÔNG
    // ================================================================

    public ChamCong saveChamCong(ChamCong cc) {
        if (cc.getMaChamCong() == 0) {
            String sql = """
                INSERT INTO CHAMCONG (maNV, ngay, maCaLam, gioVao, gioRa,
                    soGioLam, gioLamThem, trangThai, phuongThucChamCong, ghiChu)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, cc.getMaNV());
                ps.setDate(2, Date.valueOf(cc.getNgay()));
                ps.setString(3, cc.getMaCaLam());
                ps.setTimestamp(4, cc.getGioVao() != null ? Timestamp.valueOf(cc.getGioVao()) : null);
                ps.setTimestamp(5, cc.getGioRa() != null ? Timestamp.valueOf(cc.getGioRa()) : null);
                ps.setDouble(6, cc.getSoGioLam());
                ps.setDouble(7, cc.getGioLamThem());
                ps.setString(8, cc.getTrangThai().getDbValue());
                ps.setString(9, cc.getPhuongThucChamCong().getDbValue());
                ps.setString(10, cc.getGhiChu());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) cc.setMaChamCong(keys.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("[DB] saveChamCong INSERT lỗi: " + e.getMessage());
            }
        } else {
            String sql = """
                UPDATE CHAMCONG SET maCaLam=?, gioVao=?, gioRa=?,
                    soGioLam=?, gioLamThem=?, trangThai=?, phuongThucChamCong=?, ghiChu=?
                WHERE maChamCong=?
                """;
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cc.getMaCaLam());
                ps.setTimestamp(2, cc.getGioVao() != null ? Timestamp.valueOf(cc.getGioVao()) : null);
                ps.setTimestamp(3, cc.getGioRa() != null ? Timestamp.valueOf(cc.getGioRa()) : null);
                ps.setDouble(4, cc.getSoGioLam());
                ps.setDouble(5, cc.getGioLamThem());
                ps.setString(6, cc.getTrangThai().getDbValue());
                ps.setString(7, cc.getPhuongThucChamCong().getDbValue());
                ps.setString(8, cc.getGhiChu());
                ps.setInt(9, cc.getMaChamCong());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[DB] saveChamCong UPDATE lỗi: " + e.getMessage());
            }
        }
        return cc;
    }

    public ChamCong findByMaNVAndNgay(int maNV, LocalDate ngay) {
        String sql = """
            SELECT cc.*, nv.hoTen, ca.tenCaLam
            FROM CHAMCONG cc
            LEFT JOIN NHANVIEN nv ON cc.maNV = nv.maNV
            LEFT JOIN CALAM ca ON cc.maCaLam = ca.maCaLam
            WHERE cc.maNV = ? AND cc.ngay = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setDate(2, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapChamCong(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DB] findByMaNVAndNgay lỗi: " + e.getMessage());
        }
        return null;
    }

    public List<ChamCong> findByMaNVAndNgayBetween(int maNV, LocalDate tu, LocalDate den) {
        List<ChamCong> list = new ArrayList<>();
        String sql = """
            SELECT cc.*, nv.hoTen, ca.tenCaLam
            FROM CHAMCONG cc
            LEFT JOIN NHANVIEN nv ON cc.maNV = nv.maNV
            LEFT JOIN CALAM ca ON cc.maCaLam = ca.maCaLam
            WHERE cc.maNV = ? AND cc.ngay BETWEEN ? AND ?
            ORDER BY cc.ngay DESC
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setDate(2, Date.valueOf(tu));
            ps.setDate(3, Date.valueOf(den));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapChamCong(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB] findByMaNVAndNgayBetween lỗi: " + e.getMessage());
        }
        return list;
    }

    public List<ChamCong> findByThangNam(int thang, int nam) {
        List<ChamCong> list = new ArrayList<>();
        String sql = """
            SELECT cc.*, nv.hoTen, ca.tenCaLam
            FROM CHAMCONG cc
            LEFT JOIN NHANVIEN nv ON cc.maNV = nv.maNV
            LEFT JOIN CALAM ca ON cc.maCaLam = ca.maCaLam
            WHERE MONTH(cc.ngay) = ? AND YEAR(cc.ngay) = ?
            ORDER BY cc.ngay ASC, cc.maNV ASC
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapChamCong(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB] findByThangNam lỗi: " + e.getMessage());
        }
        return list;
    }

    // ================================================================
    // ĐƠN OT
    // ================================================================

    public DangKyLamThem saveDonOT(DangKyLamThem don) {
        if (don.getMaDK() == 0) {
            String sql = """
                INSERT INTO DANGKYLAMTHEM (maNV, ngay, soGio, lyDo, heSoOT, nhanXet,
                    nguoiDuyet, ngayDuyet, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, don.getMaNV());
                ps.setDate(2, Date.valueOf(don.getNgay()));
                ps.setDouble(3, don.getSoGio());
                ps.setString(4, don.getLyDo());
                ps.setDouble(5, don.getHeSoOT());
                ps.setString(6, don.getNhanXet());
                if (don.getNguoiDuyet() != null) ps.setInt(7, don.getNguoiDuyet());
                else ps.setNull(7, Types.INTEGER);
                ps.setTimestamp(8, don.getNgayDuyet() != null ? Timestamp.valueOf(don.getNgayDuyet()) : null);
                ps.setString(9, don.getTrangThai().getDbValue());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) don.setMaDK(keys.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("[DB] saveDonOT INSERT lỗi: " + e.getMessage());
            }
        } else {
            String sql = """
                UPDATE DANGKYLAMTHEM SET soGio=?, lyDo=?, heSoOT=?, nhanXet=?,
                    nguoiDuyet=?, ngayDuyet=?, trangThai=?
                WHERE maDK=?
                """;
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, don.getSoGio());
                ps.setString(2, don.getLyDo());
                ps.setDouble(3, don.getHeSoOT());
                ps.setString(4, don.getNhanXet());
                if (don.getNguoiDuyet() != null) ps.setInt(5, don.getNguoiDuyet());
                else ps.setNull(5, Types.INTEGER);
                ps.setTimestamp(6, don.getNgayDuyet() != null ? Timestamp.valueOf(don.getNgayDuyet()) : null);
                ps.setString(7, don.getTrangThai().getDbValue());
                ps.setInt(8, don.getMaDK());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[DB] saveDonOT UPDATE lỗi: " + e.getMessage());
            }
        }
        return don;
    }

    public DangKyLamThem findDonOTById(int maDK) {
        String sql = """
            SELECT dk.*, nv.hoTen FROM DANGKYLAMTHEM dk
            LEFT JOIN NHANVIEN nv ON dk.maNV = nv.maNV
            WHERE dk.maDK = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDonOT(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DB] findDonOTById lỗi: " + e.getMessage());
        }
        return null;
    }

    public List<DangKyLamThem> findDonOTByMaNV(int maNV) {
        List<DangKyLamThem> list = new ArrayList<>();
        String sql = """
            SELECT dk.*, nv.hoTen FROM DANGKYLAMTHEM dk
            LEFT JOIN NHANVIEN nv ON dk.maNV = nv.maNV
            WHERE dk.maNV = ? ORDER BY dk.ngayTao DESC
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapDonOT(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB] findDonOTByMaNV lỗi: " + e.getMessage());
        }
        return list;
    }

    public List<DangKyLamThem> findDonOTChoDuyet() {
        List<DangKyLamThem> list = new ArrayList<>();
        String sql = """
            SELECT dk.*, nv.hoTen FROM DANGKYLAMTHEM dk
            LEFT JOIN NHANVIEN nv ON dk.maNV = nv.maNV
            WHERE dk.trangThai = 'cho_duyet' ORDER BY dk.ngayTao DESC
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapDonOT(rs));
        } catch (SQLException e) {
            System.err.println("[DB] findDonOTChoDuyet lỗi: " + e.getMessage());
        }
        return list;
    }

    public List<DangKyLamThem> findAllDonOT() {
        List<DangKyLamThem> list = new ArrayList<>();
        String sql = """
            SELECT dk.*, nv.hoTen FROM DANGKYLAMTHEM dk
            LEFT JOIN NHANVIEN nv ON dk.maNV = nv.maNV
            ORDER BY dk.ngayTao DESC
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapDonOT(rs));
        } catch (SQLException e) {
            System.err.println("[DB] findAllDonOT lỗi: " + e.getMessage());
        }
        return list;
    }

    public List<DangKyLamThem> findDonOTDaDuyetTheoThang(int thang, int nam) {
        List<DangKyLamThem> list = new ArrayList<>();
        String sql = """
            SELECT dk.*, nv.hoTen FROM DANGKYLAMTHEM dk
            LEFT JOIN NHANVIEN nv ON dk.maNV = nv.maNV
            WHERE dk.trangThai = 'da_duyet'
              AND MONTH(dk.ngay) = ? AND YEAR(dk.ngay) = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapDonOT(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB] findDonOTDaDuyetTheoThang lỗi: " + e.getMessage());
        }
        return list;
    }

    public boolean deleteDonOT(int maDK) {
        String sql = "DELETE FROM DANGKYLAMTHEM WHERE maDK = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDK);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] deleteDonOT lỗi: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // BẢNG LƯƠNG
    // ================================================================

    public BangLuong saveBangLuong(BangLuong bl) {
        if (bl.getMaBL() == 0) {
            String sql = "INSERT INTO BANGLUONG (maNV, ngayBD, ngayKT, trangThai) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, bl.getMaNV());
                ps.setDate(2, Date.valueOf(bl.getNgayBD()));
                ps.setDate(3, Date.valueOf(bl.getNgayKT()));
                ps.setString(4, bl.getTrangThai().getDbValue());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) bl.setMaBL(keys.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("[DB] saveBangLuong INSERT lỗi: " + e.getMessage());
            }
        } else {
            String sql = "UPDATE BANGLUONG SET trangThai=? WHERE maBL=?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, bl.getTrangThai().getDbValue());
                ps.setInt(2, bl.getMaBL());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[DB] saveBangLuong UPDATE lỗi: " + e.getMessage());
            }
        }
        return bl;
    }

    public List<BangLuong> findAllBangLuong() {
        List<BangLuong> list = new ArrayList<>();
        String sql = "SELECT * FROM BANGLUONG ORDER BY ngayBD DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BangLuong bl = new BangLuong();
                bl.setMaBL(rs.getInt("maBL"));
                bl.setMaNV(rs.getInt("maNV"));
                bl.setNgayBD(rs.getDate("ngayBD").toLocalDate());
                bl.setNgayKT(rs.getDate("ngayKT").toLocalDate());
                bl.setTrangThai(BangLuong.TrangThai.fromDbValue(rs.getString("trangThai")));
                list.add(bl);
            }
        } catch (SQLException e) {
            System.err.println("[DB] findAllBangLuong lỗi: " + e.getMessage());
        }
        return list;
    }

    // ================================================================
    // CHI TIẾT LƯƠNG
    // ================================================================

    public ChiTietLuong saveChiTietLuong(ChiTietLuong ct) {
        if (ct.getMaChiTietLuong() == 0) {
            String sql = """
                INSERT INTO CHITIETLUONG (maBL, maNV, luongCoBan, tongLuongChucVu, tienOT,
                    tongKhauTru, tongLuong, luongThucNhan, soNgayCong, tongGioLam, tongGioOT, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, ct.getMaBL());
                ps.setInt(2, ct.getMaNV());
                ps.setDouble(3, ct.getLuongCoBan());
                ps.setDouble(4, ct.getTongLuongChucVu());
                ps.setDouble(5, ct.getTienOT());
                ps.setDouble(6, ct.getTongKhauTru());
                ps.setDouble(7, ct.getTongLuong());
                ps.setDouble(8, ct.getLuongThucNhan());
                ps.setInt(9, ct.getSoNgayCong());
                ps.setDouble(10, ct.getTongGioLam());
                ps.setDouble(11, ct.getTongGioOT());
                ps.setString(12, ct.getTrangThai().getDbValue());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) ct.setMaChiTietLuong(keys.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("[DB] saveChiTietLuong lỗi: " + e.getMessage());
            }
        }
        return ct;
    }

    public List<ChiTietLuong> findChiTietLuongByMaBL(int maBL) {
        List<ChiTietLuong> list = new ArrayList<>();
        String sql = """
            SELECT ct.*, nv.hoTen FROM CHITIETLUONG ct
            LEFT JOIN NHANVIEN nv ON ct.maNV = nv.maNV
            WHERE ct.maBL = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBL);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietLuong ct = new ChiTietLuong();
                    ct.setMaChiTietLuong(rs.getInt("maChiTietLuong"));
                    ct.setMaBL(rs.getInt("maBL"));
                    ct.setMaNV(rs.getInt("maNV"));
                    ct.setTenNV(rs.getString("hoTen"));
                    ct.setLuongCoBan(rs.getDouble("luongCoBan"));
                    ct.setTongLuongChucVu(rs.getDouble("tongLuongChucVu"));
                    ct.setTienOT(rs.getDouble("tienOT"));
                    ct.setTongKhauTru(rs.getDouble("tongKhauTru"));
                    ct.setTongLuong(rs.getDouble("tongLuong"));
                    ct.setLuongThucNhan(rs.getDouble("luongThucNhan"));
                    ct.setSoNgayCong(rs.getInt("soNgayCong"));
                    ct.setTongGioLam(rs.getDouble("tongGioLam"));
                    ct.setTongGioOT(rs.getDouble("tongGioOT"));
                    ct.setTrangThai(ChiTietLuong.TrangThai.fromDbValue(rs.getString("trangThai")));
                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] findChiTietLuongByMaBL lỗi: " + e.getMessage());
        }
        return list;
    }

    // ================================================================
    // LƯƠNG CƠ BẢN
    // ================================================================

    public double getLuongCoBan(int maNV) {
        String sql = "SELECT luongCoBan FROM NHANVIEN WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("luongCoBan");
            }
        } catch (SQLException e) {
            System.err.println("[DB] getLuongCoBan lỗi: " + e.getMessage());
        }
        return 10_000_000.0;
    }

    public void setLuongCoBan(int maNV, double luong) {
        String sql = "UPDATE NHANVIEN SET luongCoBan = ? WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, luong);
            ps.setInt(2, maNV);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] setLuongCoBan lỗi: " + e.getMessage());
        }
    }

    // ================================================================
    // CẤU HÌNH PHỤ CẤP / KHẤU TRỪ
    // ================================================================

    public CauHinhPhuCap saveCauHinhPC(CauHinhPhuCap pc) {
        if (pc.getMaPC() == 0) {
            String sql = """
                INSERT INTO CAUHINH_PHUCAP (loai, tenKhoan, kieuTinh, giaTriMacDinh, nguon, hoatDong)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, pc.getLoai().getDbValue());
                ps.setString(2, pc.getTenKhoan());
                ps.setString(3, pc.getKieuTinh().getDbValue());
                ps.setDouble(4, pc.getGiaTriMacDinh());
                ps.setString(5, pc.getNguon());
                ps.setBoolean(6, pc.isHoatDong());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) pc.setMaPC(keys.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("[DB] saveCauHinhPC INSERT lỗi: " + e.getMessage());
            }
        } else {
            String sql = """
                UPDATE CAUHINH_PHUCAP SET loai=?, tenKhoan=?, kieuTinh=?,
                    giaTriMacDinh=?, nguon=?, hoatDong=?
                WHERE maCauHinh=?
                """;
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pc.getLoai().getDbValue());
                ps.setString(2, pc.getTenKhoan());
                ps.setString(3, pc.getKieuTinh().getDbValue());
                ps.setDouble(4, pc.getGiaTriMacDinh());
                ps.setString(5, pc.getNguon());
                ps.setBoolean(6, pc.isHoatDong());
                ps.setInt(7, pc.getMaPC());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("[DB] saveCauHinhPC UPDATE lỗi: " + e.getMessage());
            }
        }
        return pc;
    }

    public CauHinhPhuCap findCauHinhPCById(int maPC) {
        String sql = "SELECT * FROM CAUHINH_PHUCAP WHERE maCauHinh = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPC);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCauHinh(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DB] findCauHinhPCById lỗi: " + e.getMessage());
        }
        return null;
    }

    public List<CauHinhPhuCap> findAllCauHinhPC() {
        List<CauHinhPhuCap> list = new ArrayList<>();
        String sql = "SELECT * FROM CAUHINH_PHUCAP ORDER BY loai, tenKhoan";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCauHinh(rs));
        } catch (SQLException e) {
            System.err.println("[DB] findAllCauHinhPC lỗi: " + e.getMessage());
        }
        return list;
    }

    public List<CauHinhPhuCap> findCauHinhPCHoatDong() {
        List<CauHinhPhuCap> list = new ArrayList<>();
        String sql = "SELECT * FROM CAUHINH_PHUCAP WHERE hoatDong = TRUE ORDER BY loai, tenKhoan";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCauHinh(rs));
        } catch (SQLException e) {
            System.err.println("[DB] findCauHinhPCHoatDong lỗi: " + e.getMessage());
        }
        return list;
    }

    public boolean deleteCauHinhPC(int maPC) {
        String sql = "UPDATE CAUHINH_PHUCAP SET hoatDong = FALSE WHERE maCauHinh = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPC);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] deleteCauHinhPC lỗi: " + e.getMessage());
            return false;
        }
    }
}
package com.hrm.repo;

import com.hrm.model.*;
import com.hrm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
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

public class AttendanceRepository {

    private static AttendanceRepository instance;
    private AttendanceRepository() {}
    public static synchronized AttendanceRepository getInstance() {
        if (instance == null) instance = new AttendanceRepository();
        return instance;
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private CaLam mapCaLam(ResultSet rs) throws SQLException {
        CaLam ca = new CaLam();
        ca.setMaCaLam(rs.getString("maCaLam"));
        ca.setTenCaLam(rs.getString("tenCaLam"));
        ca.setGioBatDau(rs.getTime("gioBatDau").toLocalTime());
        ca.setGioKetThuc(rs.getTime("gioKetThuc").toLocalTime());
        ca.setSoGioChuan(rs.getDouble("soGioChuan"));
        ca.setChoPhepLamThem(rs.getBoolean("choPhepLamThem"));
        // DB dùng ENUM trangThai ('hoat_dong'/'ngung_hoat_dong'), KHÔNG có cột hoatDong
        String tt = rs.getString("trangThai");
        ca.setTrangThai("hoat_dong".equals(tt)
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
        try { cc.setTenCaLam(rs.getString("tenCaLam")); }  catch (SQLException ignored) {}
        // hoTen lấy từ THONGTINCANHAN qua JOIN
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
    // CA LÀM — dùng cột trangThai ENUM (không có cột hoatDong)
    // ================================================================

    public List<CaLam> findCaLamHoatDong() {
        List<CaLam> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM CALAM WHERE trangThai = 'hoat_dong' ORDER BY maCaLam");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCaLam(rs));
        } catch (SQLException e) { System.err.println("[DB] findCaLamHoatDong: " + e.getMessage()); }
        return list;
    }

    public List<CaLam> findAllCaLam() {
        List<CaLam> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM CALAM ORDER BY maCaLam");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCaLam(rs));
        } catch (SQLException e) { System.err.println("[DB] findAllCaLam: " + e.getMessage()); }
        return list;
    }

    public CaLam findCaLamByMa(String ma) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM CALAM WHERE maCaLam = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapCaLam(rs); }
        } catch (SQLException e) { System.err.println("[DB] findCaLamByMa: " + e.getMessage()); }
        return null;
    }

    public CaLam saveCaLam(CaLam ca) {
        // Lưu cột trangThai ENUM, không có cột hoatDong
        String sql = """
            INSERT INTO CALAM (maCaLam, tenCaLam, gioBatDau, gioKetThuc, soGioChuan, choPhepLamThem, trangThai)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                tenCaLam=VALUES(tenCaLam), gioBatDau=VALUES(gioBatDau),
                gioKetThuc=VALUES(gioKetThuc), soGioChuan=VALUES(soGioChuan),
                choPhepLamThem=VALUES(choPhepLamThem), trangThai=VALUES(trangThai)
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ca.getMaCaLam());
            ps.setString(2, ca.getTenCaLam());
            ps.setTime(3, Time.valueOf(ca.getGioBatDau()));
            ps.setTime(4, Time.valueOf(ca.getGioKetThuc()));
            ps.setDouble(5, ca.getSoGioChuan());
            ps.setBoolean(6, ca.isChoPhepLamThem());
            ps.setString(7, ca.conHoatDong() ? "hoat_dong" : "ngung_hoat_dong");
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] saveCaLam: " + e.getMessage()); }
        return ca;
    }

    public boolean deleteCaLam(String ma) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE CALAM SET trangThai = 'ngung_hoat_dong' WHERE maCaLam = ?")) {
            ps.setString(1, ma);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] deleteCaLam: " + e.getMessage()); return false; }
    }

    // ================================================================
    // CHẤM CÔNG — JOIN THONGTINCANHAN để lấy hoTen
    // ================================================================

    public ChamCong saveChamCong(ChamCong cc) {
        if (cc.getMaChamCong() == 0) {
            String sql = """
                INSERT INTO CHAMCONG (maNV,ngay,maCaLam,gioVao,gioRa,soGioLam,gioLamThem,trangThai,phuongThucChamCong,ghiChu)
                VALUES (?,?,?,?,?,?,?,?,?,?)""";
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
                try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) cc.setMaChamCong(k.getInt(1)); }
            } catch (SQLException e) { System.err.println("[DB] saveChamCong INSERT: " + e.getMessage()); }
        } else {
            String sql = """
                UPDATE CHAMCONG SET maCaLam=?,gioVao=?,gioRa=?,soGioLam=?,gioLamThem=?,
                    trangThai=?,phuongThucChamCong=?,ghiChu=?
                WHERE maChamCong=?""";
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
            } catch (SQLException e) { System.err.println("[DB] saveChamCong UPDATE: " + e.getMessage()); }
        }
        return cc;
    }

    public ChamCong findByMaNVAndNgay(int maNV, LocalDate ngay) {
        String sql = """
            SELECT cc.*, ttcn.hoTen, ca.tenCaLam FROM CHAMCONG cc
            LEFT JOIN THONGTINCANHAN ttcn ON cc.maNV = ttcn.maNV
            LEFT JOIN CALAM ca ON cc.maCaLam = ca.maCaLam
            WHERE cc.maNV=? AND cc.ngay=?""";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV); ps.setDate(2, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapChamCong(rs); }
        } catch (SQLException e) { System.err.println("[DB] findByMaNVAndNgay: " + e.getMessage()); }
        return null;
    }

    public List<ChamCong> findByMaNVAndNgayBetween(int maNV, LocalDate tu, LocalDate den) {
        List<ChamCong> list = new ArrayList<>();
        String sql = """
            SELECT cc.*, ttcn.hoTen, ca.tenCaLam FROM CHAMCONG cc
            LEFT JOIN THONGTINCANHAN ttcn ON cc.maNV = ttcn.maNV
            LEFT JOIN CALAM ca ON cc.maCaLam = ca.maCaLam
            WHERE cc.maNV=? AND cc.ngay BETWEEN ? AND ? ORDER BY cc.ngay DESC""";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV); ps.setDate(2, Date.valueOf(tu)); ps.setDate(3, Date.valueOf(den));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapChamCong(rs)); }
        } catch (SQLException e) { System.err.println("[DB] findByMaNVAndNgayBetween: " + e.getMessage()); }
        return list;
    }

    public List<ChamCong> findByThangNam(int thang, int nam) {
        List<ChamCong> list = new ArrayList<>();
        String sql = """
            SELECT cc.*, ttcn.hoTen, ca.tenCaLam FROM CHAMCONG cc
            LEFT JOIN THONGTINCANHAN ttcn ON cc.maNV = ttcn.maNV
            LEFT JOIN CALAM ca ON cc.maCaLam = ca.maCaLam
            WHERE MONTH(cc.ngay)=? AND YEAR(cc.ngay)=? ORDER BY cc.ngay, cc.maNV""";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang); ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapChamCong(rs)); }
        } catch (SQLException e) { System.err.println("[DB] findByThangNam: " + e.getMessage()); }
        return list;
    }

    // ================================================================
    // ĐƠN OT — JOIN THONGTINCANHAN để lấy hoTen
    // ================================================================

    public DangKyLamThem saveDonOT(DangKyLamThem don) {
        if (don.getMaDK() == 0) {
            String sql = """
                INSERT INTO DANGKYLAMTHEM (maNV,ngay,soGio,lyDo,heSoOT,nhanXet,nguoiDuyet,ngayDuyet,trangThai)
                VALUES (?,?,?,?,?,?,?,?,?)""";
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
                try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) don.setMaDK(k.getInt(1)); }
            } catch (SQLException e) { System.err.println("[DB] saveDonOT INSERT: " + e.getMessage()); }
        } else {
            String sql = """
                UPDATE DANGKYLAMTHEM SET soGio=?,lyDo=?,heSoOT=?,nhanXet=?,
                    nguoiDuyet=?,ngayDuyet=?,trangThai=?
                WHERE maDK=?""";
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
            } catch (SQLException e) { System.err.println("[DB] saveDonOT UPDATE: " + e.getMessage()); }
        }
        return don;
    }

    public DangKyLamThem findDonOTById(int maDK) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT dk.*, ttcn.hoTen FROM DANGKYLAMTHEM dk LEFT JOIN THONGTINCANHAN ttcn ON dk.maNV=ttcn.maNV WHERE dk.maDK=?")) {
            ps.setInt(1, maDK);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapDonOT(rs); }
        } catch (SQLException e) { System.err.println("[DB] findDonOTById: " + e.getMessage()); }
        return null;
    }

    public List<DangKyLamThem> findDonOTByMaNV(int maNV) {
        List<DangKyLamThem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT dk.*, ttcn.hoTen FROM DANGKYLAMTHEM dk LEFT JOIN THONGTINCANHAN ttcn ON dk.maNV=ttcn.maNV WHERE dk.maNV=? ORDER BY dk.ngayTao DESC")) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapDonOT(rs)); }
        } catch (SQLException e) { System.err.println("[DB] findDonOTByMaNV: " + e.getMessage()); }
        return list;
    }

    public List<DangKyLamThem> findDonOTChoDuyet() {
        List<DangKyLamThem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT dk.*, ttcn.hoTen FROM DANGKYLAMTHEM dk LEFT JOIN THONGTINCANHAN ttcn ON dk.maNV=ttcn.maNV WHERE dk.trangThai='cho_duyet' ORDER BY dk.ngayTao DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapDonOT(rs));
        } catch (SQLException e) { System.err.println("[DB] findDonOTChoDuyet: " + e.getMessage()); }
        return list;
    }

    public List<DangKyLamThem> findAllDonOT() {
        List<DangKyLamThem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT dk.*, ttcn.hoTen FROM DANGKYLAMTHEM dk LEFT JOIN THONGTINCANHAN ttcn ON dk.maNV=ttcn.maNV ORDER BY dk.ngayTao DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapDonOT(rs));
        } catch (SQLException e) { System.err.println("[DB] findAllDonOT: " + e.getMessage()); }
        return list;
    }

    public List<DangKyLamThem> findDonOTDaDuyetTheoThang(int thang, int nam) {
        List<DangKyLamThem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT dk.*, ttcn.hoTen FROM DANGKYLAMTHEM dk LEFT JOIN THONGTINCANHAN ttcn ON dk.maNV=ttcn.maNV WHERE dk.trangThai='da_duyet' AND MONTH(dk.ngay)=? AND YEAR(dk.ngay)=?")) {
            ps.setInt(1, thang); ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapDonOT(rs)); }
        } catch (SQLException e) { System.err.println("[DB] findDonOTDaDuyetTheoThang: " + e.getMessage()); }
        return list;
    }

    public boolean deleteDonOT(int maDK) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM DANGKYLAMTHEM WHERE maDK=?")) {
            ps.setInt(1, maDK); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] deleteDonOT: " + e.getMessage()); return false; }
    }

    // ================================================================
    // BẢNG LƯƠNG — DB thực tế: maBangLuong, thang, nam (không có maNV, ngayBD, ngayKT)
    // ================================================================

    public BangLuong saveBangLuong(BangLuong bl) {
        if (bl.getMaBL() == 0) {
            String sql = "INSERT INTO BANGLUONG (thang, nam, tenBangLuong, trangThai) VALUES (?,?,?,?)";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, bl.getThang());
                ps.setInt(2, bl.getNam());
                ps.setString(3, bl.getTenBangLuong());
                // Map DA_TINH → 'dang_xu_ly' nếu DB chỉ có enum dang_xu_ly/da_khoa
                String dbTrangThai = bl.getTrangThai().getDbValue();
                // Nếu DB không nhận 'da_tinh', fallback về 'dang_xu_ly'
                if (!"dang_xu_ly".equals(dbTrangThai) && !"da_khoa".equals(dbTrangThai)) {
                    dbTrangThai = "dang_xu_ly";
                }
                ps.setString(4, dbTrangThai);
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) bl.setMaBL(k.getInt(1)); }
            } catch (SQLException e) { System.err.println("[DB] saveBangLuong INSERT: " + e.getMessage()); }
        } else {
            String sql = "UPDATE BANGLUONG SET trangThai=? WHERE maBangLuong=?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                String dbTrangThai = bl.getTrangThai().getDbValue();
                if (!"dang_xu_ly".equals(dbTrangThai) && !"da_khoa".equals(dbTrangThai)) {
                    dbTrangThai = "da_khoa";
                }
                ps.setString(1, dbTrangThai);
                ps.setInt(2, bl.getMaBL());
                ps.executeUpdate();
            } catch (SQLException e) { System.err.println("[DB] saveBangLuong UPDATE: " + e.getMessage()); }
        }
        return bl;
    }

    public List<BangLuong> findAllBangLuong() {
        List<BangLuong> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM BANGLUONG ORDER BY nam DESC, thang DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BangLuong bl = new BangLuong(rs.getInt("thang"), rs.getInt("nam"));
                bl.setMaBL(rs.getInt("maBangLuong"));
                bl.setTenBangLuong(rs.getString("tenBangLuong"));
                bl.setTrangThai(BangLuong.TrangThai.fromDbValue(rs.getString("trangThai")));
                list.add(bl);
            }
        } catch (SQLException e) { System.err.println("[DB] findAllBangLuong: " + e.getMessage()); }
        return list;
    }

    // ================================================================
    // CHI TIẾT LƯƠNG — DB: maChiTiet, maBangLuong, luongCoSo, luongLamThem, tongThuNhap, luongThucLanh
    // ================================================================

    public ChiTietLuong saveChiTietLuong(ChiTietLuong ct) {
        if (ct.getMaChiTietLuong() == 0) {
            String sql = """
                INSERT INTO CHITIETLUONG (maBangLuong,maNV,luongCoSo,tongLuongChucVu,luongLamThem,
                    tongKhauTru,tongThuNhap,luongThucLanh,soNgayCong,soGioLamThem)
                VALUES (?,?,?,?,?,?,?,?,?,?)""";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, ct.getMaBL());                      // maBangLuong
                ps.setInt(2, ct.getMaNV());
                ps.setDouble(3, ct.getLuongCoBan());             // → luongCoSo
                ps.setDouble(4, ct.getTongLuongChucVu());
                ps.setDouble(5, ct.getTienOT());                 // → luongLamThem
                ps.setDouble(6, ct.getTongKhauTru());
                ps.setDouble(7, ct.getTongLuong());              // → tongThuNhap
                ps.setDouble(8, ct.getLuongThucNhan());          // → luongThucLanh
                ps.setDouble(9, ct.getSoNgayCong());
                ps.setDouble(10, ct.getTongGioOT());             // → soGioLamThem
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) ct.setMaChiTietLuong(k.getInt(1)); }
            } catch (SQLException e) { System.err.println("[DB] saveChiTietLuong: " + e.getMessage()); }
        }
        return ct;
    }

    public List<ChiTietLuong> findChiTietLuongByMaBL(int maBL) {
        List<ChiTietLuong> list = new ArrayList<>();
        String sql = """
            SELECT ct.*, ttcn.hoTen FROM CHITIETLUONG ct
            LEFT JOIN THONGTINCANHAN ttcn ON ct.maNV = ttcn.maNV
            WHERE ct.maBangLuong=?""";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBL);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietLuong ct = new ChiTietLuong();
                    ct.setMaChiTietLuong(rs.getInt("maChiTiet"));
                    ct.setMaBL(rs.getInt("maBangLuong"));
                    ct.setMaNV(rs.getInt("maNV"));
                    ct.setTenNV(rs.getString("hoTen"));
                    ct.setLuongCoBan(rs.getDouble("luongCoSo"));
                    ct.setTongLuongChucVu(rs.getDouble("tongLuongChucVu"));
                    ct.setTienOT(rs.getDouble("luongLamThem"));
                    ct.setTongKhauTru(rs.getDouble("tongKhauTru"));
                    ct.setTongLuong(rs.getDouble("tongThuNhap"));
                    ct.setLuongThucNhan(rs.getDouble("luongThucLanh"));
                    ct.setSoNgayCong((int) rs.getDouble("soNgayCong"));
                    ct.setTongGioOT(rs.getDouble("soGioLamThem"));
                    list.add(ct);
                }
            }
        } catch (SQLException e) { System.err.println("[DB] findChiTietLuongByMaBL: " + e.getMessage()); }
        return list;
    }

    // ================================================================
    // LƯƠNG CƠ BẢN — lấy từ HOPDONGLAODONG (hợp đồng đang hiệu lực)
    // ================================================================

    public double getLuongCoBan(int maNV) {
        String sql = """
            SELECT luongCoSo FROM HOPDONGLAODONG
            WHERE maNV=? AND trangThai='hieu_luc'
            ORDER BY ngayHieuLuc DESC LIMIT 1""";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("luongCoSo");
            }
        } catch (SQLException e) { System.err.println("[DB] getLuongCoBan: " + e.getMessage()); }
        return 10_000_000.0;
    }

    public void setLuongCoBan(int maNV, double luong) {
        String sql = """
            UPDATE HOPDONGLAODONG SET luongCoSo=?
            WHERE maNV=? AND trangThai='hieu_luc'
            ORDER BY ngayHieuLuc DESC LIMIT 1""";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, luong); ps.setInt(2, maNV);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] setLuongCoBan: " + e.getMessage()); }
    }

    // ================================================================
    // CẤU HÌNH PHỤ CẤP
    // ================================================================

    public CauHinhPhuCap saveCauHinhPC(CauHinhPhuCap pc) {
        if (pc.getMaPC() == 0) {
            String sql = "INSERT INTO CAUHINH_PHUCAP (loai,tenKhoan,kieuTinh,giaTriMacDinh,nguon,hoatDong) VALUES (?,?,?,?,?,?)";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, pc.getLoai().getDbValue());
                ps.setString(2, pc.getTenKhoan());
                ps.setString(3, pc.getKieuTinh().getDbValue());
                ps.setDouble(4, pc.getGiaTriMacDinh());
                ps.setString(5, pc.getNguon());
                ps.setBoolean(6, pc.isHoatDong());
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) pc.setMaPC(k.getInt(1)); }
            } catch (SQLException e) { System.err.println("[DB] saveCauHinhPC INSERT: " + e.getMessage()); }
        } else {
            String sql = "UPDATE CAUHINH_PHUCAP SET loai=?,tenKhoan=?,kieuTinh=?,giaTriMacDinh=?,nguon=?,hoatDong=? WHERE maCauHinh=?";
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
            } catch (SQLException e) { System.err.println("[DB] saveCauHinhPC UPDATE: " + e.getMessage()); }
        }
        return pc;
    }

    public CauHinhPhuCap findCauHinhPCById(int maPC) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM CAUHINH_PHUCAP WHERE maCauHinh=?")) {
            ps.setInt(1, maPC);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapCauHinh(rs); }
        } catch (SQLException e) { System.err.println("[DB] findCauHinhPCById: " + e.getMessage()); }
        return null;
    }

    public List<CauHinhPhuCap> findAllCauHinhPC() {
        List<CauHinhPhuCap> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM CAUHINH_PHUCAP ORDER BY loai, tenKhoan");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCauHinh(rs));
        } catch (SQLException e) { System.err.println("[DB] findAllCauHinhPC: " + e.getMessage()); }
        return list;
    }

    public List<CauHinhPhuCap> findCauHinhPCHoatDong() {
        List<CauHinhPhuCap> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM CAUHINH_PHUCAP WHERE hoatDong=TRUE ORDER BY loai, tenKhoan");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCauHinh(rs));
        } catch (SQLException e) { System.err.println("[DB] findCauHinhPCHoatDong: " + e.getMessage()); }
        return list;
    }

    public boolean deleteCauHinhPC(int maPC) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE CAUHINH_PHUCAP SET hoatDong=FALSE WHERE maCauHinh=?")) {
            ps.setInt(1, maPC); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[DB] deleteCauHinhPC: " + e.getMessage()); return false; }
    }

    // ================================================================
    // LOOKUP NHÂN VIÊN
    // ================================================================

    public static class NhanVienInfo {
        public final int    maNV;
        public final String maNhanVien;
        public final String hoTen;
        public final String email;          // ← thêm mới
        public final String tenChucVu;
        public final String tenPhongBan;
        public final String trangThai;

        public NhanVienInfo(int maNV, String maNhanVien, String hoTen, String email,
                            String tenChucVu, String tenPhongBan, String trangThai) {
            this.maNV        = maNV;
            this.maNhanVien  = maNhanVien;
            this.hoTen       = hoTen       != null ? hoTen       : "";
            this.email       = email       != null ? email       : "";
            this.tenChucVu   = tenChucVu   != null ? tenChucVu   : "";
            this.tenPhongBan = tenPhongBan != null ? tenPhongBan : "";
            this.trangThai   = trangThai   != null ? trangThai   : "";
        }
    }

    /** Lấy maNhanVien (mã hiển thị VD: NV001) từ maNV (PK số nguyên) */
    public String getMaNhanVienById(int maNV) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT maNhanVien FROM NHANVIEN WHERE maNV = ?")) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("maNhanVien");
            }
        } catch (SQLException e) {
            System.err.println("[DB] getMaNhanVienById: " + e.getMessage());
        }
        return "NV-" + maNV;
    }

    /** Tra cứu nhân viên theo maNhanVien, trả về thông tin đầy đủ kể cả email */
    public NhanVienInfo findNhanVienByMa(String maNhanVien) {
        String sql = """
            SELECT nv.maNV, nv.maNhanVien, nv.trangThai,
                   ttcn.hoTen, ttcn.email,
                   cv.tenChucVu, pb.tenPhongBan
            FROM NHANVIEN nv
            LEFT JOIN THONGTINCANHAN ttcn ON nv.maNV = ttcn.maNV
            LEFT JOIN BONHIEM bn ON nv.maNV = bn.maNV
                AND bn.trangThai = 'hieu_luc' AND bn.loaiBoNhiem = 'chinh'
            LEFT JOIN CHUCVU cv ON bn.maChucVu = cv.maChucVu
            LEFT JOIN PHONGBAN pb ON bn.maPhongBan = pb.maPhongBan
            WHERE nv.maNhanVien = ?
            LIMIT 1
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new NhanVienInfo(
                    rs.getInt("maNV"), rs.getString("maNhanVien"),
                    rs.getString("hoTen"), rs.getString("email"),
                    rs.getString("tenChucVu"), rs.getString("tenPhongBan"),
                    rs.getString("trangThai"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] findNhanVienByMa: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách NV có ít nhất 1 bản ghi chấm công trong tháng/năm chỉ định.
     * Dùng cho tính lương — chỉ tính người thực sự đi làm.
     */
    public List<NhanVienInfo> findNhanVienCoChamCong(int thang, int nam) {
        List<NhanVienInfo> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT
                nv.maNV, nv.maNhanVien, nv.trangThai,
                ttcn.hoTen, ttcn.email,
                cv.tenChucVu, pb.tenPhongBan
            FROM CHAMCONG cc
            JOIN NHANVIEN nv ON cc.maNV = nv.maNV
            LEFT JOIN THONGTINCANHAN ttcn ON nv.maNV = ttcn.maNV
            LEFT JOIN BONHIEM bn ON nv.maNV = bn.maNV
                AND bn.trangThai = 'hieu_luc' AND bn.loaiBoNhiem = 'chinh'
            LEFT JOIN CHUCVU cv ON bn.maChucVu = cv.maChucVu
            LEFT JOIN PHONGBAN pb ON bn.maPhongBan = pb.maPhongBan
            WHERE MONTH(cc.ngay) = ? AND YEAR(cc.ngay) = ?
            ORDER BY nv.maNhanVien
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang); ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new NhanVienInfo(
                    rs.getInt("maNV"), rs.getString("maNhanVien"),
                    rs.getString("hoTen"), rs.getString("email"),
                    rs.getString("tenChucVu"), rs.getString("tenPhongBan"),
                    rs.getString("trangThai")));
            }
        } catch (SQLException e) {
            System.err.println("[DB] findNhanVienCoChamCong: " + e.getMessage());
        }
        return list;
    }
}
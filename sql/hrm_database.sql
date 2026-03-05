-- =====================================================
-- HRM DATABASE SCHEMA - MySQL (XAMPP Compatible)
-- Based on HRM_Business_Specification.md
-- =====================================================
-- Created for: XAMPP MySQL 5.7+ / MariaDB 10.x
-- Encoding: UTF-8 (Vietnamese support)
-- =====================================================

-- Drop database if exists and create new
DROP DATABASE IF EXISTS hrm_db;
CREATE DATABASE hrm_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hrm_db;

-- =====================================================
-- NGHIỆP VỤ 2: CƠ CẤU TỔ CHỨC (Create first - referenced by others)
-- =====================================================

-- Bảng PHONGBAN (Phòng ban)
CREATE TABLE PHONGBAN (
    maPhongBan VARCHAR(20) PRIMARY KEY,
    tenPhongBan NVARCHAR(100) NOT NULL,
    phongBanCha VARCHAR(20) NULL,
    moTa NVARCHAR(500),
    trangThai ENUM('hoat_dong', 'ngung_hoat_dong') DEFAULT 'hoat_dong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (phongBanCha) REFERENCES PHONGBAN(maPhongBan) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Bảng CHUCVU (Chức vụ)
CREATE TABLE CHUCVU (
    maChucVu VARCHAR(20) PRIMARY KEY,
    tenChucVu NVARCHAR(100) NOT NULL,
    capBac INT NOT NULL DEFAULT 10, -- 1 = cao nhất
    heSoLuong DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    phuCapChucVu DECIMAL(15,2) DEFAULT 0,
    moTa NVARCHAR(500),
    trangThai ENUM('hoat_dong', 'ngung_hoat_dong') DEFAULT 'hoat_dong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Bảng LICHSU_HESOLUONG (Lịch sử thay đổi hệ số lương chức vụ)
CREATE TABLE LICHSU_HESOLUONG (
    maLichSu INT AUTO_INCREMENT PRIMARY KEY,
    maChucVu VARCHAR(20) NOT NULL,
    heSoLuongCu DECIMAL(5,2),
    heSoLuongMoi DECIMAL(5,2),
    phuCapCu DECIMAL(15,2),
    phuCapMoi DECIMAL(15,2),
    ngayThayDoi DATETIME DEFAULT CURRENT_TIMESTAMP,
    nguoiThayDoi INT,
    lyDo NVARCHAR(500),
    FOREIGN KEY (maChucVu) REFERENCES CHUCVU(maChucVu) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 1: QUẢN LÝ HỒ SƠ NHÂN VIÊN
-- =====================================================

-- Bảng NHANVIEN (Nhân viên)
CREATE TABLE NHANVIEN (
    maNV INT AUTO_INCREMENT PRIMARY KEY,
    maNhanVien VARCHAR(20) UNIQUE NOT NULL, -- Mã định danh hiển thị
    loaiHopDong ENUM('thu_viec', 'xac_dinh_thoi_han', 'khong_xac_dinh') DEFAULT 'thu_viec',
    ngayVaoLam DATE NOT NULL,
    trangThai ENUM('dang_lam_viec', 'tam_nghi', 'nghi_viec') DEFAULT 'dang_lam_viec',
    ghiChu NVARCHAR(500),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Bảng THONGTINCANHAN (Thông tin cá nhân - Dữ liệu nhạy cảm)
CREATE TABLE THONGTINCANHAN (
    maNV INT PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    ngaySinh DATE,
    gioiTinh ENUM('nam', 'nu', 'khac'),
    CCCD VARCHAR(12) UNIQUE,
    dienThoai VARCHAR(15),
    email VARCHAR(100),
    diaChi NVARCHAR(255),
    diaChiThuongTru NVARCHAR(255),
    queQuan NVARCHAR(255),
    danToc NVARCHAR(50),
    tonGiao NVARCHAR(50),
    tinhTrangHonNhan ENUM('doc_than', 'da_ket_hon', 'ly_hon') DEFAULT 'doc_than',
    anhDaiDien VARCHAR(255),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 3: BỔ NHIỆM VÀ ĐIỀU CHUYỂN
-- =====================================================

-- Bảng BONHIEM (Bổ nhiệm)
CREATE TABLE BONHIEM (
    maBoNhiem INT AUTO_INCREMENT PRIMARY KEY,
    maNV INT NOT NULL,
    maPhongBan VARCHAR(20) NOT NULL,
    maChucVu VARCHAR(20) NOT NULL,
    loaiBoNhiem ENUM('chinh', 'kiem_nhiem') DEFAULT 'chinh',
    tyLeHuongLuong DECIMAL(5,2) DEFAULT 100.00, -- % lương chức vụ
    maQuanLy INT NULL, -- Nhân viên quản lý trực tiếp
    nguoiDuyet INT NULL,
    tuNgay DATE NOT NULL,
    denNgay DATE NULL, -- NULL = vô thời hạn
    ngayPheDuyet DATETIME NULL,
    lyDo NVARCHAR(500),
    trangThai ENUM('cho_duyet', 'hieu_luc', 'het_hieu_luc', 'tu_choi') DEFAULT 'cho_duyet',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (maPhongBan) REFERENCES PHONGBAN(maPhongBan),
    FOREIGN KEY (maChucVu) REFERENCES CHUCVU(maChucVu),
    FOREIGN KEY (maQuanLy) REFERENCES NHANVIEN(maNV) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 4: CHẤM CÔNG VÀ LÀM THÊM GIỜ
-- =====================================================

-- Bảng CALAM (Ca làm việc)
CREATE TABLE CALAM (
    maCaLam VARCHAR(20) PRIMARY KEY,
    tenCaLam NVARCHAR(100) NOT NULL,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL,
    soGioChuan DECIMAL(4,2) DEFAULT 8.00,
    choPhepLamThem BOOLEAN DEFAULT TRUE,
    moTa NVARCHAR(255),
    trangThai ENUM('hoat_dong', 'ngung_hoat_dong') DEFAULT 'hoat_dong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Bảng CHAMCONG (Chấm công)
CREATE TABLE CHAMCONG (
    maChamCong INT AUTO_INCREMENT PRIMARY KEY,
    maNV INT NOT NULL,
    ngay DATE NOT NULL,
    maCaLam VARCHAR(20),
    gioVao DATETIME,
    gioRa DATETIME,
    soGioLam DECIMAL(4,2) DEFAULT 0,
    gioLamThem DECIMAL(4,2) DEFAULT 0,
    trangThai ENUM('dung_gio', 'di_muon', 've_som', 'vang_mat', 'nghi_phep', 'cong_tac') DEFAULT 'dung_gio',
    phuongThucChamCong ENUM('wifi', 'van_tay', 'the_tu', 'gps', 'thu_cong') DEFAULT 'thu_cong',
    ghiChu NVARCHAR(255),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (maCaLam) REFERENCES CALAM(maCaLam),
    UNIQUE KEY uk_nv_ngay (maNV, ngay) -- Mỗi NV chỉ có 1 bản ghi/ngày
) ENGINE=InnoDB;

-- Bảng DANGKYLAMTHEM (Đăng ký làm thêm giờ)
CREATE TABLE DANGKYLAMTHEM (
    maDK INT AUTO_INCREMENT PRIMARY KEY,
    maNV INT NOT NULL,
    ngay DATE NOT NULL,
    soGio DECIMAL(4,2) NOT NULL,
    lyDo NVARCHAR(500),
    heSoOT DECIMAL(4,2) DEFAULT 1.5,       -- Hệ số nhân lương OT (1.5 ngày thường, 2.0 cuối tuần...)
    nhanXet NVARCHAR(500),                  -- Nhận xét của người duyệt
    nguoiDuyet INT,
    ngayDuyet DATETIME,
    trangThai ENUM('cho_duyet', 'da_duyet', 'tu_choi') DEFAULT 'cho_duyet',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (nguoiDuyet) REFERENCES NHANVIEN(maNV)
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 5: HỢP ĐỒNG LAO ĐỘNG
-- =====================================================

-- Bảng HOPDONGLAODONG
CREATE TABLE HOPDONGLAODONG (
    maHopDong INT AUTO_INCREMENT PRIMARY KEY,
    soHopDong VARCHAR(50) UNIQUE NOT NULL,
    maNV INT NOT NULL,
    loaiHopDong ENUM('thu_viec', 'xac_dinh_thoi_han', 'khong_xac_dinh') NOT NULL,
    luongCoSo DECIMAL(15,2) NOT NULL,
    ngayKy DATE NOT NULL,
    ngayHieuLuc DATE NOT NULL,
    ngayHetHieuLuc DATE NULL, -- NULL = không xác định thời hạn
    fileDinhKem VARCHAR(255),
    noiDung TEXT,
    trangThai ENUM('hieu_luc', 'het_han', 'thanh_ly', 'huy') DEFAULT 'hieu_luc',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 6: TÍNH LƯƠNG
-- =====================================================

-- Bảng BANGLUONG (Bảng lương theo kỳ)
CREATE TABLE BANGLUONG (
    maBangLuong INT AUTO_INCREMENT PRIMARY KEY,
    thang INT NOT NULL,
    nam INT NOT NULL,
    tenBangLuong NVARCHAR(100),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayKhoa DATETIME NULL,
    nguoiTao INT,
    nguoiKhoa INT,
    trangThai ENUM('dang_xu_ly', 'da_khoa') DEFAULT 'dang_xu_ly',
    UNIQUE KEY uk_thang_nam (thang, nam)
) ENGINE=InnoDB;

-- Bảng CHITIETLUONG (Chi tiết lương - SNAPSHOT)
CREATE TABLE CHITIETLUONG (
    maChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    maBangLuong INT NOT NULL,
    maNV INT NOT NULL,
    -- Snapshot data at calculation time
    luongCoSo DECIMAL(15,2) NOT NULL,
    tongLuongChucVu DECIMAL(15,2) DEFAULT 0,
    luongLamThem DECIMAL(15,2) DEFAULT 0,
    -- Tổng hợp
    tongThuNhap DECIMAL(15,2) DEFAULT 0,
    tongKhauTru DECIMAL(15,2) DEFAULT 0,
    luongThucLanh DECIMAL(15,2) DEFAULT 0,
    -- Thông tin thêm
    soNgayCong DECIMAL(4,1) DEFAULT 0,
    soGioLamThem DECIMAL(5,2) DEFAULT 0,
    ghiChu NVARCHAR(500),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maBangLuong) REFERENCES BANGLUONG(maBangLuong) ON DELETE CASCADE,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    UNIQUE KEY uk_bangluong_nv (maBangLuong, maNV)
) ENGINE=InnoDB;

-- Bảng THANHPHANLUONG (Chi tiết từng khoản lương)
CREATE TABLE THANHPHANLUONG (
    maThanhPhan INT AUTO_INCREMENT PRIMARY KEY,
    maChiTiet INT NOT NULL,
    tenThanhPhan NVARCHAR(100) NOT NULL,
    loai ENUM('thu_nhap', 'khau_tru') NOT NULL,
    soTien DECIMAL(15,2) NOT NULL,
    ghiChu NVARCHAR(255),
    FOREIGN KEY (maChiTiet) REFERENCES CHITIETLUONG(maChiTiet) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Bảng CAUHINH_PHUCAP (Cấu hình các khoản phụ cấp / khấu trừ dùng khi tính lương)
CREATE TABLE CAUHINH_PHUCAP (
    maCauHinh INT AUTO_INCREMENT PRIMARY KEY,
    loai ENUM('thu_nhap', 'khau_tru') NOT NULL,         -- Loại khoản: thu nhập thêm hoặc khấu trừ
    tenKhoan NVARCHAR(100) NOT NULL,                    -- Tên khoản (VD: "Phụ cấp ăn trưa", "BHXH")
    kieuTinh ENUM('co_dinh', 'phan_tram') NOT NULL,     -- Cố định (VND) hoặc phần trăm (%) lương cơ sở
    giaTriMacDinh DECIMAL(15,2) NOT NULL DEFAULT 0,     -- Giá trị: số tiền cố định hoặc % tùy kieuTinh
    nguon NVARCHAR(100),                                -- Nguồn gốc khoản (VD: "Công ty", "BHXH bắt buộc")
    hoatDong BOOLEAN DEFAULT TRUE,                      -- TRUE = áp dụng khi tính lương
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 7: QUẢN LÝ NGHỈ PHÉP
-- =====================================================

-- Bảng LOAIPHEP (Loại nghỉ phép)
CREATE TABLE LOAIPHEP (
    maLoaiPhep VARCHAR(20) PRIMARY KEY,
    tenLoaiPhep NVARCHAR(100) NOT NULL,
    coLuong BOOLEAN DEFAULT TRUE,
    canChungTu BOOLEAN DEFAULT FALSE,
    soNgayToiDa INT DEFAULT 0, -- 0 = không giới hạn
    moTa NVARCHAR(255),
    trangThai ENUM('hoat_dong', 'ngung_hoat_dong') DEFAULT 'hoat_dong'
) ENGINE=InnoDB;

-- Bảng SODUNGPHEP (Số dư phép năm)
CREATE TABLE SODUNGPHEP (
    maSoDung INT AUTO_INCREMENT PRIMARY KEY,
    maNV INT NOT NULL,
    nam INT NOT NULL,
    maLoaiPhep VARCHAR(20) NOT NULL,
    soNgayDuocCap DECIMAL(4,1) DEFAULT 12,
    soNgayDaDung DECIMAL(4,1) DEFAULT 0,
    soNgayConLai DECIMAL(4,1) GENERATED ALWAYS AS (soNgayDuocCap - soNgayDaDung) STORED,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (maLoaiPhep) REFERENCES LOAIPHEP(maLoaiPhep),
    UNIQUE KEY uk_nv_nam_loai (maNV, nam, maLoaiPhep)
) ENGINE=InnoDB;

-- Bảng DONXINNGHIPHEP (Đơn xin nghỉ phép)
CREATE TABLE DONXINNGHIPHEP (
    maDon INT AUTO_INCREMENT PRIMARY KEY,
    maNV INT NOT NULL,
    maLoaiPhep VARCHAR(20) NOT NULL,
    tuNgay DATE NOT NULL,
    denNgay DATE NOT NULL,
    soNgayNghi DECIMAL(4,1) NOT NULL,
    lyDo NVARCHAR(500),
    fileDinhKem VARCHAR(255), -- Chứng từ nếu cần
    nguoiDuyet INT,
    ngayDuyet DATETIME,
    lyDoTuChoi NVARCHAR(500),
    trangThai ENUM('cho_duyet', 'da_duyet', 'tu_choi', 'huy') DEFAULT 'cho_duyet',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (maLoaiPhep) REFERENCES LOAIPHEP(maLoaiPhep),
    FOREIGN KEY (nguoiDuyet) REFERENCES NHANVIEN(maNV)
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 8: ĐÁNH GIÁ HIỆU SUẤT
-- =====================================================

-- Bảng DOTDANHGIA (Đợt đánh giá)
CREATE TABLE DOTDANHGIA (
    maDot INT AUTO_INCREMENT PRIMARY KEY,
    tenDot NVARCHAR(100) NOT NULL,
    nam INT NOT NULL,
    kyDanhGia ENUM('quy_1', 'quy_2', 'quy_3', 'quy_4', 'nam') NOT NULL,
    tuNgay DATE NOT NULL,
    denNgay DATE NOT NULL,
    moTa NVARCHAR(500),
    trangThai ENUM('chua_bat_dau', 'dang_dien_ra', 'da_ket_thuc') DEFAULT 'chua_bat_dau',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Bảng TIEUCHIDANHGIA (Tiêu chí đánh giá)
CREATE TABLE TIEUCHIDANHGIA (
    maTieuChi INT AUTO_INCREMENT PRIMARY KEY,
    tenTieuChi NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(500),
    nhomTieuChi NVARCHAR(50), -- Năng lực, Thái độ, Kết quả...
    diemToiDa DECIMAL(5,2) DEFAULT 10,
    trangThai ENUM('hoat_dong', 'ngung_hoat_dong') DEFAULT 'hoat_dong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Bảng DOTDANHGIA_TIEUCHI (Cấu hình tiêu chí cho đợt)
CREATE TABLE DOTDANHGIA_TIEUCHI (
    maDot INT NOT NULL,
    maTieuChi INT NOT NULL,
    trongSo DECIMAL(5,2) DEFAULT 1.00, -- Trọng số của tiêu chí trong đợt
    batBuoc BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (maDot, maTieuChi),
    FOREIGN KEY (maDot) REFERENCES DOTDANHGIA(maDot) ON DELETE CASCADE,
    FOREIGN KEY (maTieuChi) REFERENCES TIEUCHIDANHGIA(maTieuChi) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Bảng DANHGIAHIEUSUAT (Kết quả đánh giá tổng thể)
CREATE TABLE DANHGIAHIEUSUAT (
    maDanhGia INT AUTO_INCREMENT PRIMARY KEY,
    maDot INT NOT NULL,
    maNV INT NOT NULL, -- Nhân viên được đánh giá
    nguoiDanhGia INT NOT NULL, -- Quản lý đánh giá
    tongDiem DECIMAL(5,2) DEFAULT 0,
    xepLoai ENUM('xuat_sac', 'tot', 'kha', 'trung_binh', 'yeu') DEFAULT 'trung_binh',
    nhanXetChung NVARCHAR(1000),
    ngayDanhGia DATETIME,
    trangThai ENUM('chua_danh_gia', 'da_danh_gia', 'da_xac_nhan') DEFAULT 'chua_danh_gia',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maDot) REFERENCES DOTDANHGIA(maDot) ON DELETE CASCADE,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE,
    FOREIGN KEY (nguoiDanhGia) REFERENCES NHANVIEN(maNV),
    UNIQUE KEY uk_dot_nv (maDot, maNV)
) ENGINE=InnoDB;

-- Bảng CHITIETDANHGIA (Chi tiết điểm từng tiêu chí)
CREATE TABLE CHITIETDANHGIA (
    maChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    maDanhGia INT NOT NULL,
    maTieuChi INT NOT NULL,
    diem DECIMAL(5,2) DEFAULT 0,
    nhanXet NVARCHAR(500),
    FOREIGN KEY (maDanhGia) REFERENCES DANHGIAHIEUSUAT(maDanhGia) ON DELETE CASCADE,
    FOREIGN KEY (maTieuChi) REFERENCES TIEUCHIDANHGIA(maTieuChi),
    UNIQUE KEY uk_danhgia_tieuchi (maDanhGia, maTieuChi)
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 9: PHÂN QUYỀN VÀ BẢO MẬT
-- =====================================================

-- Bảng VAITRO (Vai trò)
CREATE TABLE VAITRO (
    maVaiTro VARCHAR(20) PRIMARY KEY,
    tenVaiTro NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(255),
    laVaiTroHeThong BOOLEAN DEFAULT FALSE, -- Không thể xóa
    trangThai ENUM('hoat_dong', 'ngung_hoat_dong') DEFAULT 'hoat_dong',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Bảng QUYEN (Quyền)
CREATE TABLE QUYEN (
    maQuyen VARCHAR(50) PRIMARY KEY,
    tenQuyen NVARCHAR(100) NOT NULL,
    nhomQuyen VARCHAR(50), -- Module: Employee, Leave, Payroll...
    moTa NVARCHAR(255),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Bảng VAITRO_QUYEN (N-N: Vai trò - Quyền)
CREATE TABLE VAITRO_QUYEN (
    maVaiTro VARCHAR(20) NOT NULL,
    maQuyen VARCHAR(50) NOT NULL,
    PRIMARY KEY (maVaiTro, maQuyen),
    FOREIGN KEY (maVaiTro) REFERENCES VAITRO(maVaiTro) ON DELETE CASCADE,
    FOREIGN KEY (maQuyen) REFERENCES QUYEN(maQuyen) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Bảng TAIKHOAN (Tài khoản đăng nhập)
CREATE TABLE TAIKHOAN (
    maTaiKhoan INT AUTO_INCREMENT PRIMARY KEY,
    tenDangNhap VARCHAR(50) UNIQUE NOT NULL,
    matKhau VARCHAR(255) NOT NULL, -- Hashed password
    maNV INT UNIQUE, -- 0..1 với NHANVIEN
    maVaiTro VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    hoatDong BOOLEAN DEFAULT TRUE,
    biKhoa BOOLEAN DEFAULT FALSE,
    lanDangNhapCuoi DATETIME,
    soLanDangNhapLoi INT DEFAULT 0,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE SET NULL,
    FOREIGN KEY (maVaiTro) REFERENCES VAITRO(maVaiTro)
) ENGINE=InnoDB;

-- Bảng TAIKHOAN_QUYEN (Quyền ngoại lệ cho tài khoản)
CREATE TABLE TAIKHOAN_QUYEN (
    maTaiKhoan INT NOT NULL,
    maQuyen VARCHAR(50) NOT NULL,
    choPhep BOOLEAN NOT NULL, -- TRUE = cấp thêm, FALSE = thu hồi
    ghiChu NVARCHAR(255),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (maTaiKhoan, maQuyen),
    FOREIGN KEY (maTaiKhoan) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE CASCADE,
    FOREIGN KEY (maQuyen) REFERENCES QUYEN(maQuyen) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 10: THÔNG BÁO NỘI BỘ
-- =====================================================

-- Bảng THONGBAO
CREATE TABLE THONGBAO (
    maThongBao INT AUTO_INCREMENT PRIMARY KEY,
    tieuDe NVARCHAR(200) NOT NULL,
    noiDung TEXT,
    loaiThongBao ENUM('he_thong', 'don_tu', 'thong_bao_chung') DEFAULT 'he_thong',
    maTaiKhoanGui INT NULL, -- NULL = từ hệ thống
    maTaiKhoanNhan INT NOT NULL,
    daDoc BOOLEAN DEFAULT FALSE,
    ngayDoc DATETIME,
    linkLienQuan VARCHAR(255), -- Link đến đối tượng liên quan
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maTaiKhoanGui) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE SET NULL,
    FOREIGN KEY (maTaiKhoanNhan) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- NGHIỆP VỤ 11: TUYỂN DỤNG
-- =====================================================

-- Bảng YEUCAUTUYENDUNG (Yêu cầu tuyển dụng)
CREATE TABLE YEUCAUTUYENDUNG (
    maYeuCau INT AUTO_INCREMENT PRIMARY KEY,
    maPhongBan VARCHAR(20) NOT NULL,
    maChucVu VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL DEFAULT 1,
    lyDo NVARCHAR(500),
    mucLuongDuKien NVARCHAR(100),
    yeuCauKinhNghiem NVARCHAR(500),
    yeuCauHocVan NVARCHAR(255),
    yeuCauKhac NVARCHAR(500),
    hanTuyenDung DATE,
    nguoiDuyet INT,
    ngayDuyet DATETIME,
    trangThai ENUM('cho_duyet', 'da_duyet', 'tu_choi', 'da_tuyen_du') DEFAULT 'cho_duyet',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maPhongBan) REFERENCES PHONGBAN(maPhongBan),
    FOREIGN KEY (maChucVu) REFERENCES CHUCVU(maChucVu)
) ENGINE=InnoDB;

-- Bảng TINTUYENDUNG (Tin tuyển dụng công khai)
CREATE TABLE TINTUYENDUNG (
    maTin INT AUTO_INCREMENT PRIMARY KEY,
    maYeuCau INT NOT NULL,
    tieuDe NVARCHAR(200) NOT NULL,
    noiDung TEXT,
    mucLuong NVARCHAR(100),
    diaDiem NVARCHAR(255),
    hanNopHoSo DATE,
    trangThai ENUM('dang_tuyen', 'tam_dung', 'da_dong') DEFAULT 'dang_tuyen',
    soLuotXem INT DEFAULT 0,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maYeuCau) REFERENCES YEUCAUTUYENDUNG(maYeuCau) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Bảng UNGVIEN (Ứng viên)
CREATE TABLE UNGVIEN (
    maUngVien INT AUTO_INCREMENT PRIMARY KEY,
    maTin INT NOT NULL,
    hoTen NVARCHAR(100) NOT NULL,
    email VARCHAR(100),
    dienThoai VARCHAR(15),
    ngaySinh DATE,
    gioiTinh ENUM('nam', 'nu', 'khac'),
    diaChi NVARCHAR(255),
    trinhDoHocVan NVARCHAR(100),
    kinhNghiem NVARCHAR(500),
    fileCv VARCHAR(255),
    nguonUngTuyen NVARCHAR(100), -- Website, facebook, giới thiệu...
    trangThai ENUM('moi', 'dang_phong_van', 'trung_tuyen', 'tu_choi') DEFAULT 'moi',
    nhanXet NVARCHAR(1000),
    maNV INT NULL, -- Liên kết khi trúng tuyển
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maTin) REFERENCES TINTUYENDUNG(maTin) ON DELETE CASCADE,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- BẢNG PHỤ TRỢ: AUDIT LOG
-- =====================================================

-- Bảng LOG_AUDIT (Ghi nhận hoạt động hệ thống)
CREATE TABLE LOG_AUDIT (
    maLog INT AUTO_INCREMENT PRIMARY KEY,
    maTaiKhoan INT,
    hanhDong VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    bangDuLieu VARCHAR(50), -- Tên bảng bị ảnh hưởng
    maBanGhi VARCHAR(50), -- ID bản ghi bị ảnh hưởng
    duLieuCu TEXT, -- JSON data cũ
    duLieuMoi TEXT, -- JSON data mới
    diaChi_IP VARCHAR(45),
    userAgent VARCHAR(255),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maTaiKhoan) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Indexes cho NHANVIEN
CREATE INDEX idx_nv_trangthai ON NHANVIEN(trangThai);
CREATE INDEX idx_nv_ngayvaolam ON NHANVIEN(ngayVaoLam);

-- Indexes cho BONHIEM
CREATE INDEX idx_bonhiem_nv ON BONHIEM(maNV);
CREATE INDEX idx_bonhiem_trangthai ON BONHIEM(trangThai);
CREATE INDEX idx_bonhiem_tungay ON BONHIEM(tuNgay);

-- Indexes cho CHAMCONG
CREATE INDEX idx_chamcong_ngay ON CHAMCONG(ngay);
CREATE INDEX idx_chamcong_nv_thang ON CHAMCONG(maNV, ngay);

-- Indexes cho DONXINNGHIPHEP
CREATE INDEX idx_don_nv ON DONXINNGHIPHEP(maNV);
CREATE INDEX idx_don_trangthai ON DONXINNGHIPHEP(trangThai);

-- Indexes cho THONGBAO
CREATE INDEX idx_thongbao_nguoinhan ON THONGBAO(maTaiKhoanNhan);
CREATE INDEX idx_thongbao_dadoc ON THONGBAO(daDoc);

-- Indexes cho LOG_AUDIT
CREATE INDEX idx_audit_taikhoan ON LOG_AUDIT(maTaiKhoan);
CREATE INDEX idx_audit_ngay ON LOG_AUDIT(ngayTao);
CREATE INDEX idx_audit_bang ON LOG_AUDIT(bangDuLieu);

-- =====================================================
-- INSERT DỮ LIỆU MẪU
-- =====================================================

-- Vai trò mặc định
INSERT INTO VAITRO (maVaiTro, tenVaiTro, moTa, laVaiTroHeThong) VALUES
('ADMIN', 'Quản trị viên', 'Toàn quyền quản trị hệ thống', TRUE),
('HR', 'Nhân sự', 'Quản lý nhân viên, hợp đồng, nghỉ phép', FALSE),
('MANAGER', 'Quản lý', 'Quản lý team, duyệt phép, đánh giá nhân viên', FALSE),
('EMPLOYEE', 'Nhân viên', 'Xem thông tin cá nhân, đăng ký nghỉ phép', FALSE);

-- Quyền mặc định
INSERT INTO QUYEN (maQuyen, tenQuyen, nhomQuyen) VALUES
-- Employee
('VIEW_SELF', 'Xem thông tin cá nhân', 'Employee'),
('EMPLOYEE_VIEW', 'Xem danh sách nhân viên', 'Employee'),
('EMPLOYEE_CREATE', 'Tạo nhân viên', 'Employee'),
('EMPLOYEE_UPDATE', 'Cập nhật nhân viên', 'Employee'),
('EMPLOYEE_DELETE', 'Xóa nhân viên', 'Employee'),
-- Leave
('LEAVE_CREATE', 'Tạo đơn nghỉ phép', 'Leave'),
('LEAVE_VIEW_SELF', 'Xem nghỉ phép cá nhân', 'Leave'),
('LEAVE_VIEW_ALL', 'Xem tất cả nghỉ phép', 'Leave'),
('LEAVE_APPROVE', 'Duyệt nghỉ phép', 'Leave'),
('LEAVE_MANAGE', 'Quản lý nghỉ phép', 'Leave'),
-- Evaluation
('EVAL_VIEW_SELF', 'Xem đánh giá cá nhân', 'Evaluation'),
('EVAL_VIEW_ALL', 'Xem tất cả đánh giá', 'Evaluation'),
('EVAL_REVIEW', 'Đánh giá nhân viên', 'Evaluation'),
('EVAL_MANAGE', 'Quản lý đánh giá', 'Evaluation'),
-- User
('USER_VIEW', 'Xem danh sách tài khoản', 'User'),
('USER_CREATE', 'Tạo tài khoản', 'User'),
('USER_UPDATE', 'Cập nhật tài khoản', 'User'),
('USER_DELETE', 'Xóa tài khoản', 'User'),
-- Role
('ROLE_VIEW', 'Xem vai trò', 'Role'),
('ROLE_CREATE', 'Tạo vai trò', 'Role'),
('ROLE_UPDATE', 'Cập nhật vai trò', 'Role'),
('ROLE_DELETE', 'Xóa vai trò', 'Role'),
-- Report
('REPORT_VIEW', 'Xem báo cáo', 'Report'),
('REPORT_EXPORT', 'Xuất báo cáo', 'Report'),
-- Settings
('SETTINGS_VIEW', 'Xem cài đặt', 'Settings'),
('SETTINGS_UPDATE', 'Cập nhật cài đặt', 'Settings'),
    
-- Organization (NV2)
('DEPARTMENT_VIEW', 'Xem phòng ban', 'Organization'),
('DEPARTMENT_MANAGE', 'Quản lý phòng ban', 'Organization'),
('POSITION_VIEW', 'Xem chức vụ', 'Organization'),
('POSITION_MANAGE', 'Quản lý chức vụ', 'Organization'),

-- Appointments (NV3)
('APPOINTMENT_VIEW', 'Xem bổ nhiệm', 'Appointment'),
('APPOINTMENT_CREATE', 'Tạo bổ nhiệm', 'Appointment'),
('APPOINTMENT_APPROVE', 'Duyệt bổ nhiệm', 'Appointment'),

-- Attendance (NV4)
('ATTENDANCE_VIEW', 'Xem chấm công', 'Attendance'),
('ATTENDANCE_MANAGE', 'Quản lý chấm công', 'Attendance'),

-- Contracts (NV5)
('CONTRACT_VIEW', 'Xem hợp đồng', 'Contract'),
('CONTRACT_MANAGE', 'Quản lý hợp đồng', 'Contract'),

-- Payroll (NV6)
('PAYROLL_VIEW', 'Xem lương', 'Payroll'),
('PAYROLL_CALCULATE', 'Tính lương', 'Payroll'),

-- Recruitment (NV11)
('RECRUITMENT_VIEW', 'Xem tuyển dụng', 'Recruitment'),
('RECRUITMENT_MANAGE', 'Quản lý tuyển dụng', 'Recruitment');

-- Gán quyền cho vai trò ADMIN (tất cả quyền)
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen)
SELECT 'ADMIN', maQuyen FROM QUYEN;

-- Gán quyền cho vai trò HR
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('HR', 'VIEW_SELF'), ('HR', 'EMPLOYEE_VIEW'), ('HR', 'EMPLOYEE_CREATE'), ('HR', 'EMPLOYEE_UPDATE'),
('HR', 'LEAVE_CREATE'), ('HR', 'LEAVE_VIEW_SELF'), ('HR', 'LEAVE_VIEW_ALL'), ('HR', 'LEAVE_APPROVE'), ('HR', 'LEAVE_MANAGE'),
('HR', 'EVAL_VIEW_SELF'), ('HR', 'EVAL_VIEW_ALL'), ('HR', 'EVAL_MANAGE'),
('HR', 'REPORT_VIEW');

-- Gán quyền cho vai trò MANAGER
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('MANAGER', 'VIEW_SELF'), ('MANAGER', 'EMPLOYEE_VIEW'),
('MANAGER', 'LEAVE_CREATE'), ('MANAGER', 'LEAVE_VIEW_SELF'), ('MANAGER', 'LEAVE_VIEW_ALL'), ('MANAGER', 'LEAVE_APPROVE'),
('MANAGER', 'EVAL_VIEW_SELF'), ('MANAGER', 'EVAL_VIEW_ALL'), ('MANAGER', 'EVAL_REVIEW'),
('MANAGER', 'REPORT_VIEW');

-- Gán quyền cho vai trò EMPLOYEE
INSERT INTO VAITRO_QUYEN (maVaiTro, maQuyen) VALUES
('EMPLOYEE', 'VIEW_SELF'),
('EMPLOYEE', 'LEAVE_CREATE'), ('EMPLOYEE', 'LEAVE_VIEW_SELF'),
('EMPLOYEE', 'EVAL_VIEW_SELF');

-- Loại phép mặc định
INSERT INTO LOAIPHEP (maLoaiPhep, tenLoaiPhep, coLuong, canChungTu, soNgayToiDa) VALUES
('PHEP_NAM', 'Nghỉ phép năm', TRUE, FALSE, 12),
('PHEP_OM', 'Nghỉ ốm', TRUE, TRUE, 30),
('PHEP_CUOI', 'Nghỉ cưới', TRUE, TRUE, 3),
('PHEP_TANG', 'Nghỉ tang', TRUE, TRUE, 3),
('PHEP_THAI_SAN', 'Nghỉ thai sản', TRUE, TRUE, 180),
('PHEP_KHONG_LUONG', 'Nghỉ không lương', FALSE, FALSE, 0);

-- Ca làm việc mặc định
INSERT INTO CALAM (maCaLam, tenCaLam, gioBatDau, gioKetThuc, soGioChuan) VALUES
('HANH_CHINH', 'Ca hành chính', '08:00:00', '17:00:00', 8.00),
('CA_SANG', 'Ca sáng', '06:00:00', '14:00:00', 8.00),
('CA_CHIEU', 'Ca chiều', '14:00:00', '22:00:00', 8.00),
('CA_DEM', 'Ca đêm', '22:00:00', '06:00:00', 8.00);

-- Phòng ban mẫu
INSERT INTO PHONGBAN (maPhongBan, tenPhongBan, phongBanCha, moTa) VALUES
('CONGTY', 'Công ty ABC', NULL, 'Công ty mẹ'),
('PHONGNS', 'Phòng Nhân sự', 'CONGTY', 'Quản lý nhân sự'),
('PHONGKT', 'Phòng Kế toán', 'CONGTY', 'Quản lý tài chính'),
('PHONGKD', 'Phòng Kinh doanh', 'CONGTY', 'Kinh doanh và bán hàng'),
('PHONGIT', 'Phòng IT', 'CONGTY', 'Công nghệ thông tin');

-- Chức vụ mẫu
INSERT INTO CHUCVU (maChucVu, tenChucVu, capBac, heSoLuong, phuCapChucVu) VALUES
('GD', 'Giám đốc', 1, 5.00, 10000000),
('PGD', 'Phó Giám đốc', 2, 4.00, 7000000),
('TP', 'Trưởng phòng', 3, 3.00, 5000000),
('PP', 'Phó phòng', 4, 2.50, 3000000),
('TT', 'Trưởng team', 5, 2.00, 2000000),
('NV', 'Nhân viên', 10, 1.00, 0);

-- Tiêu chí đánh giá mẫu
INSERT INTO TIEUCHIDANHGIA (tenTieuChi, moTa, nhomTieuChi, diemToiDa) VALUES
('Chất lượng công việc', 'Đánh giá chất lượng output', 'Kết quả', 10),
('Tiến độ hoàn thành', 'Đánh giá việc hoàn thành đúng hạn', 'Kết quả', 10),
('Sáng tạo & Cải tiến', 'Khả năng đưa ra giải pháp mới', 'Năng lực', 10),
('Kỹ năng chuyên môn', 'Kiến thức và kỹ năng nghề', 'Năng lực', 10),
('Tinh thần hợp tác', 'Khả năng làm việc nhóm', 'Thái độ', 10),
('Tuân thủ nội quy', 'Chấp hành quy định công ty', 'Thái độ', 10);

-- Tài khoản admin mặc định (password: 123 - cần hash trong thực tế)
INSERT INTO TAIKHOAN (tenDangNhap, matKhau, maNV, maVaiTro, email) VALUES
('admin', '123', NULL, 'ADMIN', 'admin@hrm.local');

-- =====================================================
-- STORED PROCEDURES & TRIGGERS
-- =====================================================

DELIMITER //

-- Trigger: Ghi log khi thay đổi hệ số lương chức vụ
CREATE TRIGGER trg_lichsu_hesoluong
BEFORE UPDATE ON CHUCVU
FOR EACH ROW
BEGIN
    IF OLD.heSoLuong != NEW.heSoLuong OR OLD.phuCapChucVu != NEW.phuCapChucVu THEN
        INSERT INTO LICHSU_HESOLUONG (maChucVu, heSoLuongCu, heSoLuongMoi, phuCapCu, phuCapMoi)
        VALUES (OLD.maChucVu, OLD.heSoLuong, NEW.heSoLuong, OLD.phuCapChucVu, NEW.phuCapChucVu);
    END IF;
END //

-- Trigger: Cập nhật số ngày phép đã dùng khi đơn được duyệt
CREATE TRIGGER trg_cap_nhat_so_phep
AFTER UPDATE ON DONXINNGHIPHEP
FOR EACH ROW
BEGIN
    IF NEW.trangThai = 'da_duyet' AND OLD.trangThai != 'da_duyet' THEN
        UPDATE SODUNGPHEP 
        SET soNgayDaDung = soNgayDaDung + NEW.soNgayNghi
        WHERE maNV = NEW.maNV 
          AND nam = YEAR(NEW.tuNgay) 
          AND maLoaiPhep = NEW.maLoaiPhep;
    END IF;
END //

-- Procedure: Tạo số dư phép năm cho nhân viên mới
CREATE PROCEDURE sp_tao_so_du_phep(IN p_maNV INT, IN p_nam INT)
BEGIN
    INSERT INTO SODUNGPHEP (maNV, nam, maLoaiPhep, soNgayDuocCap)
    SELECT p_maNV, p_nam, maLoaiPhep, 
           CASE maLoaiPhep 
               WHEN 'PHEP_NAM' THEN 12 
               ELSE 0 
           END
    FROM LOAIPHEP
    WHERE trangThai = 'hoat_dong';
END //

-- Procedure: Tính xếp loại đánh giá
CREATE PROCEDURE sp_tinh_xep_loai(IN p_maDanhGia INT)
BEGIN
    DECLARE v_tongDiem DECIMAL(5,2);
    DECLARE v_diemToiDa DECIMAL(5,2);
    DECLARE v_tyLe DECIMAL(5,2);
    DECLARE v_xepLoai VARCHAR(20);
    
    -- Tính tổng điểm
    SELECT SUM(ct.diem * dt.trongSo), SUM(tc.diemToiDa * dt.trongSo)
    INTO v_tongDiem, v_diemToiDa
    FROM CHITIETDANHGIA ct
    JOIN TIEUCHIDANHGIA tc ON ct.maTieuChi = tc.maTieuChi
    JOIN DANHGIAHIEUSUAT dg ON ct.maDanhGia = dg.maDanhGia
    JOIN DOTDANHGIA_TIEUCHI dt ON dg.maDot = dt.maDot AND ct.maTieuChi = dt.maTieuChi
    WHERE ct.maDanhGia = p_maDanhGia;
    
    -- Tính tỷ lệ %
    SET v_tyLe = (v_tongDiem / v_diemToiDa) * 100;
    
    -- Xếp loại
    SET v_xepLoai = CASE
        WHEN v_tyLe >= 90 THEN 'xuat_sac'
        WHEN v_tyLe >= 80 THEN 'tot'
        WHEN v_tyLe >= 65 THEN 'kha'
        WHEN v_tyLe >= 50 THEN 'trung_binh'
        ELSE 'yeu'
    END;
    
    -- Cập nhật
    UPDATE DANHGIAHIEUSUAT 
    SET tongDiem = v_tongDiem, xepLoai = v_xepLoai
    WHERE maDanhGia = p_maDanhGia;
END //

DELIMITER ;

-- =====================================================
-- COMPLETION MESSAGE
-- =====================================================
SELECT 'Database HRM created successfully!' AS Message;

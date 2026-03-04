package com.hrm.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối đến MySQL database.
 *
 * ĐÂY LÀ PATTERN: Singleton + Utility class
 *
 * TẠI SAO CẦN CLASS NÀY?
 * - Tập trung quản lý thông tin kết nối DB ở MỘT NƠI duy nhất
 * - Nếu đổi host/port/password, chỉ cần sửa file database.properties
 * - Các Repository gọi getConnection() mà không cần biết chi tiết kết nối
 *
 * CÁCH HOẠT ĐỘNG:
 * 1. Đọc file database.properties (url, username, password)
 * 2. Dùng DriverManager.getConnection() để tạo kết nối đến MySQL
 * 3. Trả về Connection object cho Repository sử dụng
 */
public class DatabaseConnection {

    // ====================================================
    // STATIC FIELDS — Load config 1 lần duy nhất khi class được nạp
    // ====================================================
    //
    // STATIC BLOCK là gì?
    // - Chạy DUY NHẤT 1 LẦN khi JVM nạp class vào bộ nhớ
    // - Phù hợp cho việc đọc file config, khởi tạo driver...
    // - Nếu lỗi ở đây, app sẽ crash sớm → tốt hơn là crash muộn
    //
    private static String url;
    private static String username;
    private static String password;

    static {
        try {
            Properties props = new Properties();

            // getResourceAsStream đọc file từ thư mục resources/
            // ClassLoader tìm file trong classpath, không phụ thuộc vào vị trí chạy app
            InputStream input = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("database.properties");

            if (input == null) {
                throw new RuntimeException("Không tìm thấy file database.properties trong resources/");
            }

            props.load(input);
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");

            // Nạp MySQL JDBC Driver
            // Từ JDBC 4.0+ (Java 6+), dòng này KHÔNG BẮT BUỘC
            // vì driver tự đăng ký qua ServiceLoader
            // Nhưng để AN TOÀN với mọi môi trường (XAMPP cũ), nên giữ
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file database.properties: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy MySQL JDBC Driver. "
                    + "Hãy thêm mysql-connector-java vào thư viện dự án.", e);
        }
    }

    // ====================================================
    // Private constructor — Không cho phép tạo instance
    // ====================================================
    //
    // Đây là UTILITY CLASS (chỉ có static methods)
    // → Không cần tạo object: DatabaseConnection db = new DatabaseConnection() ← SAI
    // → Chỉ gọi: Connection conn = DatabaseConnection.getConnection() ← ĐÚNG
    //
    private DatabaseConnection() {}

    /**
     * Tạo và trả về một Connection mới đến database.
     *
     * QUAN TRỌNG: Người gọi PHẢI đóng Connection sau khi dùng xong!
     * Cách tốt nhất: dùng try-with-resources (sẽ thấy trong Repository)
     *
     * @return Connection đến MySQL database
     * @throws SQLException nếu không kết nối được
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
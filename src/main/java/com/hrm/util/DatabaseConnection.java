package com.hrm.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection - Singleton quản lý kết nối MySQL.
 *
 * TẠI SAO DÙNG SINGLETON?
 * - Toàn bộ app chỉ cần 1 điểm quản lý kết nối DB
 * - Tránh mở quá nhiều connection cùng lúc
 * - Dễ test: chỉ cần mock lớp này
 *
 * CÁCH DÙNG:
 *   Connection conn = DatabaseConnection.getInstance().getConnection();
 *   // ... dùng conn để query ...
 *   conn.close(); // Luôn đóng sau khi dùng!
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private String url;
    private String username;
    private String password;

    // ──────────────────────────────────────────
    // PRIVATE CONSTRUCTOR: đọc config từ file
    // ──────────────────────────────────────────
    private DatabaseConnection() {
        loadConfig();
    }

    /** Lấy instance duy nhất (thread-safe) */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Đọc thông tin kết nối từ database.properties.
     * File này nằm trong src/main/resources/ và được
     * đóng gói vào .jar khi build.
     */
    private void loadConfig() {
        Properties props = new Properties();

        // getResourceAsStream tìm file trong classpath (resources/)
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input == null) {
                throw new RuntimeException(
                    "Không tìm thấy file database.properties!\n" +
                    "Hãy kiểm tra: src/main/resources/database.properties"
                );
            }
            props.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc database.properties: " + e.getMessage(), e);
        }

        this.url      = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");
    }

    /**
     * Lấy một Connection mới tới MySQL.
     *
     * ⚠️ QUAN TRỌNG: Sau khi dùng xong, PHẢI đóng connection:
     *   try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
     *       // dùng conn ở đây
     *   } // tự động đóng khi ra khỏi try-block (try-with-resources)
     *
     * @return Connection mới tới hrm_db
     * @throws SQLException nếu không kết nối được
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Kiểm tra kết nối có hoạt động không.
     * Gọi lúc khởi động app để báo lỗi sớm.
     *
     * @return true nếu kết nối thành công
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(3); // timeout 3 giây
        } catch (SQLException e) {
            System.err.println("[DB] Lỗi kết nối: " + e.getMessage());
            return false;
        }
    }
}
package com.hrm;

import com.hrm.gui.LoginFrame;
import com.hrm.util.DatabaseConnection;

import javax.swing.*;
import java.sql.Connection;

/**
 * Main entry point for HRM Application
 */
public class MainApp {

    public static void main(String[] args) {

        // ── 1. Set Look and Feel ──────────────────────────
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ── 2. Kiểm tra kết nối Database ─────────────────
        System.out.println("==============================================");
        System.out.println("[HRM] Khởi động ứng dụng HRM System...");
        System.out.println("[HRM] Đang kiểm tra kết nối MySQL...");

        // DatabaseConnection trong repo là SINGLETON → phải gọi getInstance() trước
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (conn != null && conn.isValid(3)) {
                System.out.println("[HRM] ✓ Kết nối MySQL thành công!");
                System.out.println("[HRM] Database: " + conn.getCatalog());
            }
        } catch (Exception e) {
            System.err.println("[HRM] ✗ Kết nối MySQL THẤT BẠI!");
            System.err.println("[HRM] Lỗi: " + e.getMessage());

            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                    null,
                    "Không thể kết nối MySQL!\n\n"
                    + "Lỗi: " + e.getMessage() + "\n\n"
                    + "Kiểm tra:\n"
                    + "  • MySQL Service đang chạy chưa?\n"
                    + "  • database.properties có đúng mật khẩu không?\n"
                    + "  • Database 'hrm_db' đã được tạo chưa?",
                    "Lỗi Kết Nối Database",
                    JOptionPane.ERROR_MESSAGE
                )
            );
            return;
        }

        System.out.println("==============================================");

        // ── 3. Mở Login Frame ────────────────────────────
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
package com.hrm;

import com.hrm.gui.LoginFrame;
import com.hrm.util.DatabaseConnection;

import javax.swing.*;

/**
 * Main entry point cho HRM Application.
 * Bước 1: Kiểm tra kết nối DB trước khi mở UI.
 * Bước 2: Nếu kết nối thành công → mở LoginFrame.
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
        // Làm điều này TRƯỚC khi mở UI để báo lỗi sớm,
        // tránh trường hợp user đăng nhập xong mới thấy lỗi DB.
        System.out.println("[App] Đang kiểm tra kết nối database...");

        if (!DatabaseConnection.getInstance().testConnection()) {
            // Hiện thông báo lỗi thân thiện cho user
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                    null,
                    "Không thể kết nối đến MySQL Database!\n\n" +
                    "Vui lòng kiểm tra:\n" +
                    "  1. MySQL Server đang chạy chưa?\n" +
                    "  2. Mật khẩu trong database.properties có đúng không?\n" +
                    "  3. Database 'hrm_db' đã được tạo chưa?",
                    "Lỗi Kết Nối Database",
                    JOptionPane.ERROR_MESSAGE
                )
            );
            return; // Dừng app
        }

        System.out.println("[App] ✓ Kết nối database thành công!");

        // ── 3. Mở Login Frame ────────────────────────────
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
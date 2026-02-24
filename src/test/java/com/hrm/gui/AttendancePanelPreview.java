package com.hrm.gui;

import com.hrm.gui.attendance.AttendancePanel;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * FILE NÀY CHỈ ĐỂ XEM TRƯỚC GIAO DIỆN — KHÔNG PHẢI CODE CHÍNH.
 *
 * Cách chạy trong IntelliJ:
 *   Click chuột phải → Run 'AttendancePanelPreview.main()'
 *
 * Cách chạy bằng CMD:
 *   mvn compile exec:java -Dexec.mainClass="com.hrm.gui.AttendancePanelPreview" -Dexec.classpathScope=test
 *
 * TẠI SAO CẦN FILE NÀY?
 *   - App chính (MainApp) cần login → phức tạp
 *   - File này mở THẲNG panel Chấm công → xem ngay giao diện
 *   - Đặt trong src/test/ → không đóng gói vào app cuối cùng
 */
public class AttendancePanelPreview {

    public static void main(String[] args) {
        // Swing phải chạy trên EDT (Event Dispatch Thread)
        // Đây là quy tắc BẮT BUỘC của Swing
        SwingUtilities.invokeLater(() -> {

            // ====================================
            // Giả lập session (thay vì phải login)
            // ====================================
            setupMockSession();

            // ====================================
            // Tạo cửa sổ preview
            // ====================================
            JFrame frame = new JFrame("PREVIEW - Cham cong & Lam them gio");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setMinimumSize(new Dimension(900, 600));

            // Wrapper giống MainFrame
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(UIColors.LIGHT_GRAY_BG);
            wrapper.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Header
            JLabel lblHeader = new JLabel("Cham cong & Lam them gio");
            lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblHeader.setForeground(UIColors.TEXT_DARK);
            lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
            wrapper.add(lblHeader, BorderLayout.NORTH);

            // Nhúng AttendancePanel
            AttendancePanel panel = new AttendancePanel();
            wrapper.add(panel, BorderLayout.CENTER);

            frame.setContentPane(wrapper);
            frame.setLocationRelativeTo(null); // Giữa màn hình
            frame.setVisible(true);

            System.out.println("=== PREVIEW DANG CHAY ===");
            System.out.println("Dong cua so de thoat.");
        });
    }

    /**
     * Giả lập user đã đăng nhập.
     *
     * Vì AttendancePanel cần SessionContext.getCurrentUser(),
     * ta phải setup mock data trước khi tạo panel.
     */
    private static void setupMockSession() {
        try {
            // Dùng MockDataService có sẵn trong project
            com.hrm.service.MockDataService mockData =
                    com.hrm.service.MockDataService.getInstance();
            mockData.initializeData();

            // Đăng nhập giả bằng AuthService
            com.hrm.service.AuthService authService =
                    com.hrm.service.AuthService.getInstance();
            authService.login("admin", "123");

            System.out.println("Mock session: Dang nhap voi user 'admin'");
        } catch (Exception e) {
            System.err.println("Loi khi setup mock session: " + e.getMessage());
            System.err.println("Panel se hien thi voi du lieu trong.");
        }
    }
}
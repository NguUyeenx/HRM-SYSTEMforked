package com.hrm.gui;

import com.hrm.service.AuthService;
import com.hrm.gui.components.PurpleButton;
import com.hrm.gui.components.RoundedPanel;
import com.hrm.gui.leave.LeaveListPanel;
import com.hrm.gui.evaluation.EvalCycleListPanel;
import com.hrm.gui.admin.UserManagementPanel;
import com.hrm.gui.admin.RoleManagementPanel;
import com.hrm.model.User;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.hrm.gui.attendance.AttendancePanel;

/**
 * MainFrame - Main application frame with purple theme
 * Features header, sidebar navigation, and dynamic content area
 * Uses SessionContext for session management
 */
public class MainFrame extends JFrame {

    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JLabel lblUserName;
    private JLabel lblUserRole;

    // Menu buttons
    private JButton btnDashboard;
    private JButton btnUsers;
    private JButton btnRoles;
    private JButton btnEmployees;
    private JButton btnOrganization;   // NV2 - Phòng ban & Chức vụ
    private JButton btnAppointments;   // NV3 - Bổ nhiệm
    private JButton btnAttendance;     // NV4 - Chấm công
    private JButton btnContracts;      // NV5 - Hợp đồng
    private JButton btnPayroll;        // NV6 - Lương
    private JButton btnLeave;
    private JButton btnPerformance;
    private JButton btnNotifications;  // NV10 - Thông báo
    private JButton btnRecruitment;    // NV11 - Tuyển dụng
    private JButton btnReports;
    private JButton btnSettings;
    private JButton btnLogout;

    private JButton currentActiveButton;
    private final AuthService authService;

    public MainFrame() {
        this.authService = AuthService.getInstance();

        if (!authService.isLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Phien lam viec khong hop le");
            dispose();
            new LoginFrame().setVisible(true);
            return;
        }

        initComponents();
        setupLayout();
        setupEvents();
        setupPermissions();
        setLocationRelativeTo(null);

        // Show dashboard by default
        showDashboard();
    }

    private void initComponents() {
        setTitle("HRM System - He thong quan ly nhan su");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));

        // Header panel - Purple
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIColors.PRIMARY_PURPLE);
        headerPanel.setPreferredSize(new Dimension(0, 65));

        // Sidebar panel
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setBackground(UIColors.WHITE);

        // Content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIColors.LIGHT_GRAY_BG);

        // Create menu buttons
        btnDashboard = createMenuButton("Trang chu", "dashboard");
        
        // Nhân sự
        btnEmployees = createMenuButton("Ho so nhan vien", "employees");
        btnOrganization = createMenuButton("Co cau to chuc", "organization");
        btnAppointments = createMenuButton("Bo nhiem", "appointments");
        btnRecruitment = createMenuButton("Tuyen dung", "recruitment");
        
        // Chấm công & Lương
        btnAttendance = createMenuButton("Cham cong", "attendance");
        btnContracts = createMenuButton("Hop dong lao dong", "contracts");
        btnPayroll = createMenuButton("Tinh luong", "payroll");
        
        // Chính sách
        btnLeave = createMenuButton("Nghi phep", "leave");
        btnPerformance = createMenuButton("Danh gia hieu suat", "performance");
        
        // Hệ thống
        btnUsers = createMenuButton("Quan ly tai khoan", "users");
        btnRoles = createMenuButton("Quan ly vai tro", "roles");
        btnNotifications = createMenuButton("Thong bao", "notifications");
        btnReports = createMenuButton("Bao cao", "reports");
        btnSettings = createMenuButton("Cai dat", "settings");
        btnLogout = createMenuButton("Dang xuat", "logout");
        btnLogout.setForeground(UIColors.DANGER_RED);
    }

    private JButton createMenuButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(UIColors.TEXT_DARK);
        button.setBackground(UIColors.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(220, 45));
        button.setMaximumSize(new Dimension(220, 45));
        button.setBorder(new EmptyBorder(10, 20, 10, 10));

        // Hover effect - Light purple
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button != currentActiveButton) {
                    button.setBackground(UIColors.LIGHT_PURPLE);
                    button.setForeground(UIColors.PRIMARY_PURPLE);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button != currentActiveButton) {
                    button.setBackground(UIColors.WHITE);
                    button.setForeground(UIColors.TEXT_DARK);
                    if (button == btnLogout) {
                        button.setForeground(UIColors.DANGER_RED);
                    }
                }
            }
        });

        return button;
    }

    private void setActiveButton(JButton button) {
        // Reset previous active button
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(UIColors.WHITE);
            currentActiveButton.setForeground(UIColors.TEXT_DARK);
            currentActiveButton.setBorder(new EmptyBorder(10, 20, 10, 10));
        }

        currentActiveButton = button;

        if (button != null) {
            // Active state - Purple left border + light purple background
            button.setBackground(UIColors.LIGHT_PURPLE);
            button.setForeground(UIColors.PRIMARY_PURPLE);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, UIColors.PRIMARY_PURPLE),
                new EmptyBorder(10, 16, 10, 10)
            ));
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        User user = SessionContext.getInstance().getCurrentUser();
        String displayName = user != null ? user.getFullName() : "Guest";
        String roleName = user != null ? user.getRoleNames() : "";

        // ========================
        // HEADER PANEL
        // ========================
        headerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Left - Logo
        JPanel logoSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoSection.setOpaque(false);

        JLabel lblLogo = new JLabel("HRM System");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(Color.WHITE);
        logoSection.add(lblLogo);

        // Right - User info + Logout
        JPanel userSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userSection.setOpaque(false);

        // User name in header
        JLabel lblHeaderUser = new JLabel(displayName);
        lblHeaderUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblHeaderUser.setForeground(Color.WHITE);

        // Role badge
        JLabel lblHeaderRole = new JLabel(roleName);
        lblHeaderRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHeaderRole.setForeground(new Color(255, 255, 255, 180));

        // Logout button in header
        JButton btnHeaderLogout = new JButton("Dang xuat");
        btnHeaderLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHeaderLogout.setForeground(Color.WHITE);
        btnHeaderLogout.setBackground(UIColors.DARK_PURPLE);
        btnHeaderLogout.setBorderPainted(false);
        btnHeaderLogout.setFocusPainted(false);
        btnHeaderLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHeaderLogout.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnHeaderLogout.addActionListener(e -> logout());
        btnHeaderLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnHeaderLogout.setBackground(UIColors.PURPLE_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnHeaderLogout.setBackground(UIColors.DARK_PURPLE);
            }
        });

        JLabel separator = new JLabel(" | ");
        separator.setForeground(new Color(255,255,255,100));

        userSection.add(lblHeaderUser);
        userSection.add(separator);
        userSection.add(lblHeaderRole);
        userSection.add(Box.createHorizontalStrut(10));
        userSection.add(btnHeaderLogout);

        headerPanel.add(logoSection, BorderLayout.WEST);
        headerPanel.add(userSection, BorderLayout.EAST);

        // ========================
        // SIDEBAR PANEL with Scroll
        // ========================
        JPanel sidebarContent = new JPanel();
        sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));
        sidebarContent.setBackground(UIColors.WHITE);

        // Profile section with light purple background
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(UIColors.LIGHT_PURPLE);
        profilePanel.setMaximumSize(new Dimension(240, 120));
        profilePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar circle
        JLabel lblAvatar = new JLabel(getInitials(displayName));
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setBackground(UIColors.PRIMARY_PURPLE);
        lblAvatar.setOpaque(true);
        lblAvatar.setPreferredSize(new Dimension(60, 60));
        lblAvatar.setMinimumSize(new Dimension(60, 60));
        lblAvatar.setMaximumSize(new Dimension(60, 60));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblUserName = new JLabel(displayName);
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUserName.setForeground(UIColors.TEXT_DARK);
        lblUserName.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblUserRole = new JLabel(roleName);
        lblUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserRole.setForeground(UIColors.TEXT_GRAY);
        lblUserRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        profilePanel.add(lblAvatar);
        profilePanel.add(Box.createVerticalStrut(10));
        profilePanel.add(lblUserName);
        profilePanel.add(Box.createVerticalStrut(2));
        profilePanel.add(lblUserRole);

        sidebarContent.add(profilePanel);
        sidebarContent.add(Box.createVerticalStrut(15));

        // Menu section
        JLabel lblMenu = new JLabel("MENU");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenu.setForeground(UIColors.TEXT_GRAY);
        lblMenu.setBorder(new EmptyBorder(5, 20, 10, 0));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenu.setMaximumSize(new Dimension(240, 30));
        sidebarContent.add(lblMenu);

        // All menu items - flat list
        sidebarContent.add(btnDashboard);
        sidebarContent.add(btnEmployees);
        sidebarContent.add(btnOrganization);
        sidebarContent.add(btnAppointments);
        sidebarContent.add(btnRecruitment);
        sidebarContent.add(btnAttendance);
        sidebarContent.add(btnContracts);
        sidebarContent.add(btnPayroll);
        sidebarContent.add(btnLeave);
        sidebarContent.add(btnPerformance);
        sidebarContent.add(btnUsers);
        sidebarContent.add(btnRoles);
        sidebarContent.add(btnNotifications);
        sidebarContent.add(btnReports);
        sidebarContent.add(btnSettings);

        // Push logout to bottom
        sidebarContent.add(Box.createVerticalGlue());
        sidebarContent.add(btnLogout);
        sidebarContent.add(Box.createVerticalStrut(15));

        // Wrap in scroll pane
        JScrollPane sidebarScroll = new JScrollPane(sidebarContent);
        sidebarScroll.setPreferredSize(new Dimension(240, 0));
        sidebarScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIColors.BORDER_GRAY));
        sidebarScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);

        // ========================
        // ADD TO FRAME
        // ========================
        add(headerPanel, BorderLayout.NORTH);
        add(sidebarScroll, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return ("" + name.charAt(0)).toUpperCase();
    }

    private void setupEvents() {
        // Window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        // Menu button actions
        btnDashboard.addActionListener(e -> showDashboard());
        
        // Nhan su
        btnEmployees.addActionListener(e -> showPlaceholder("Ho so nhan vien"));
        btnOrganization.addActionListener(e -> showPlaceholder("Co cau to chuc"));
        btnAppointments.addActionListener(e -> showPlaceholder("Bo nhiem"));
        btnRecruitment.addActionListener(e -> showPlaceholder("Tuyen dung"));
        
        // Cham cong & Luong
        btnAttendance.addActionListener(e -> showAttendance());
        btnContracts.addActionListener(e -> showPlaceholder("Hop dong lao dong"));
        btnPayroll.addActionListener(e -> showPlaceholder("Tinh luong"));
        
        // Chinh sach
        btnLeave.addActionListener(e -> showLeaveManagement());
        btnPerformance.addActionListener(e -> showPerformanceEvaluation());
        
        // He thong
        btnUsers.addActionListener(e -> showUserManagement());
        btnRoles.addActionListener(e -> showRoleManagement());
        btnNotifications.addActionListener(e -> showPlaceholder("Thong bao"));
        btnReports.addActionListener(e -> showPlaceholder("Bao cao"));
        btnSettings.addActionListener(e -> showSettings());
        btnLogout.addActionListener(e -> logout());
    }

    private void setupPermissions() {
        // Show/hide menu items based on permissions from SessionContext
        SessionContext sc = SessionContext.getInstance();
        boolean isAdmin = sc.hasRole("ADMIN");
        
        // Nhan su - visible to HR and managers
        btnEmployees.setVisible(isAdmin || sc.hasPermission("EMPLOYEE_VIEW") || sc.hasPermission("VIEW_SELF"));
        btnOrganization.setVisible(isAdmin || sc.hasPermission("DEPARTMENT_VIEW") || sc.hasPermission("POSITION_VIEW"));
        btnAppointments.setVisible(isAdmin || sc.hasPermission("APPOINTMENT_VIEW"));
        btnRecruitment.setVisible(isAdmin || sc.hasPermission("RECRUITMENT_VIEW"));
        
        // Cham cong & Luong
        btnAttendance.setVisible(isAdmin || sc.hasPermission("ATTENDANCE_VIEW") || sc.hasPermission("VIEW_SELF"));
        btnContracts.setVisible(isAdmin || sc.hasPermission("CONTRACT_VIEW"));
        btnPayroll.setVisible(isAdmin || sc.hasPermission("PAYROLL_VIEW"));
        
        // Chinh sach
        btnLeave.setVisible(isAdmin || sc.hasPermission("LEAVE_VIEW_ALL") || sc.hasPermission("LEAVE_VIEW_SELF"));
        btnPerformance.setVisible(isAdmin || sc.hasPermission("EVAL_VIEW_ALL") || sc.hasPermission("EVAL_VIEW_SELF"));
        
        // He thong
        btnUsers.setVisible(isAdmin || sc.hasPermission("USER_VIEW") || sc.hasPermission("USER_CREATE"));
        btnRoles.setVisible(isAdmin || sc.hasPermission("ROLE_VIEW") || sc.hasPermission("ROLE_CREATE"));
        btnNotifications.setVisible(true); // Everyone can see notifications
        btnReports.setVisible(isAdmin || sc.hasPermission("REPORT_VIEW"));
    }

    private void showDashboard() {
        setActiveButton(btnDashboard);
        contentPanel.removeAll();

        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        dashboardPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header
        JLabel lblHeader = new JLabel("Tong quan");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(UIColors.TEXT_DARK);
        dashboardPanel.add(lblHeader, BorderLayout.NORTH);

        // Stats cards
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        cardsPanel.add(RoundedPanel.createStatCard("Tai khoan", "4", UIColors.PRIMARY_PURPLE));
        cardsPanel.add(RoundedPanel.createStatCard("Vai tro", "4", UIColors.SUCCESS_GREEN));
        cardsPanel.add(RoundedPanel.createStatCard("Nhan vien", "10", UIColors.WARNING_YELLOW));
        cardsPanel.add(RoundedPanel.createStatCard("Nghi phep cho duyet", "3", UIColors.DANGER_RED));
        cardsPanel.add(RoundedPanel.createStatCard("Danh gia thang nay", "5", UIColors.DARK_PURPLE));
        cardsPanel.add(RoundedPanel.createStatCard("Bao cao moi", "2", UIColors.INFO_BLUE));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);

        // Welcome message
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(new EmptyBorder(30, 0, 0, 0));

        User currentUser = SessionContext.getInstance().getCurrentUser();
        String displayName = currentUser != null ? currentUser.getFullName() : "Guest";
        JLabel lblWelcome = new JLabel("Chao mung " + displayName + " den voi HRM System!");
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblWelcome.setForeground(UIColors.TEXT_GRAY);
        welcomePanel.add(lblWelcome, BorderLayout.NORTH);

        centerPanel.add(welcomePanel, BorderLayout.CENTER);
        dashboardPanel.add(centerPanel, BorderLayout.CENTER);

        contentPanel.add(dashboardPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showSettings() {
        setActiveButton(btnSettings);
        contentPanel.removeAll();

        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        settingsPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblHeader = new JLabel("Cai dat tai khoan");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(UIColors.TEXT_DARK);
        settingsPanel.add(lblHeader, BorderLayout.NORTH);

        // Change password card
        RoundedPanel passwordCard = RoundedPanel.createFlatCard();
        passwordCard.setLayout(new GridBagLayout());
        passwordCard.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblPasswordTitle = new JLabel("Doi mat khau");
        lblPasswordTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPasswordTitle.setForeground(UIColors.PRIMARY_PURPLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        passwordCard.add(lblPasswordTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel lbl1 = new JLabel("Mat khau hien tai:");
        lbl1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordCard.add(lbl1, gbc);

        JPasswordField txtCurrentPass = new JPasswordField(20);
        txtCurrentPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        passwordCard.add(txtCurrentPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lbl2 = new JLabel("Mat khau moi:");
        lbl2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordCard.add(lbl2, gbc);

        JPasswordField txtNewPass = new JPasswordField(20);
        txtNewPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        passwordCard.add(txtNewPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lbl3 = new JLabel("Xac nhan mat khau:");
        lbl3.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordCard.add(lbl3, gbc);

        JPasswordField txtConfirmPass = new JPasswordField(20);
        txtConfirmPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        passwordCard.add(txtConfirmPass, gbc);

        PurpleButton btnChangePassword = new PurpleButton("Doi mat khau");
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 8, 8, 8);
        passwordCard.add(btnChangePassword, gbc);

        btnChangePassword.addActionListener(e -> {
            String currentPass = new String(txtCurrentPass.getPassword());
            String newPass = new String(txtNewPass.getPassword());
            String confirmPass = new String(txtConfirmPass.getPassword());

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui long nhap day du thong tin",
                    "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Mat khau xac nhan khong khop",
                    "Loi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Mock success - in real app this would update database
            JOptionPane.showMessageDialog(this, "Doi mat khau thanh cong (mock)",
                "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            txtCurrentPass.setText("");
            txtNewPass.setText("");
            txtConfirmPass.setText("");
        });

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
        centerPanel.add(passwordCard);

        settingsPanel.add(centerPanel, BorderLayout.CENTER);

        contentPanel.add(settingsPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPlaceholder(String title) {
        contentPanel.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIColors.LIGHT_GRAY_BG);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblHeader = new JLabel(title);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(UIColors.TEXT_DARK);
        panel.add(lblHeader, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JLabel lblPlaceholder = new JLabel("Chuc nang dang phat trien...");
        lblPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblPlaceholder.setForeground(UIColors.TEXT_GRAY);
        centerPanel.add(lblPlaceholder);

        panel.add(centerPanel, BorderLayout.CENTER);

        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showLeaveManagement() {
        setActiveButton(btnLeave);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Quan ly nghi phep");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Add LeaveListPanel
        LeaveListPanel leavePanel = new LeaveListPanel();
        wrapperPanel.add(leavePanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showAttendance() {
        setActiveButton(btnAttendance);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Cham cong & Lam them gio");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Nhúng AttendancePanel vào content area
        AttendancePanel attendancePanel = new AttendancePanel();
        wrapperPanel.add(attendancePanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPerformanceEvaluation() {
        setActiveButton(btnPerformance);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Danh gia hieu suat");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Add EvalCycleListPanel
        EvalCycleListPanel evalPanel = new EvalCycleListPanel();
        wrapperPanel.add(evalPanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showUserManagement() {
        setActiveButton(btnUsers);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Quan ly tai khoan");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Add UserManagementPanel
        UserManagementPanel userPanel = new UserManagementPanel();
        wrapperPanel.add(userPanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showRoleManagement() {
        setActiveButton(btnRoles);
        contentPanel.removeAll();

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(UIColors.LIGHT_GRAY_BG);
        wrapperPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblHeader = new JLabel("Quan ly vai tro");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(UIColors.TEXT_DARK);
        lblHeader.setBorder(new EmptyBorder(0, 10, 15, 0));
        wrapperPanel.add(lblHeader, BorderLayout.NORTH);

        // Add RoleManagementPanel
        RoleManagementPanel rolePanel = new RoleManagementPanel();
        wrapperPanel.add(rolePanel, BorderLayout.CENTER);

        contentPanel.add(wrapperPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac muon dang xuat?",
            "Xac nhan dang xuat",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ban co chac muon thoat ung dung?",
            "Xac nhan thoat",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            System.exit(0);
        }
    }
}

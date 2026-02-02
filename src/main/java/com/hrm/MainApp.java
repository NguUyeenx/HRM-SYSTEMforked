package com.hrm;

import com.hrm.gui.LoginFrame;
import javax.swing.*;

/**
 * Main entry point for HRM Application
 * Uses Module 9 Authentication with database
 */
public class MainApp {

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Open Login Frame on EDT
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

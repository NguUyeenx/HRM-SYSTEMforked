package com.hrm.util;

import javax.swing.*;
import java.awt.*;

/**
 * UI Helper - provides styled components that work across all Look and Feels
 */
public class UIHelper {

    // Color constants
    public static final Color PRIMARY_COLOR = new Color(0, 102, 153);
    public static final Color SUCCESS_COLOR = new Color(46, 164, 79);
    public static final Color DANGER_COLOR = new Color(192, 57, 43);
    public static final Color WARNING_COLOR = new Color(230, 126, 34);
    public static final Color INFO_COLOR = new Color(52, 152, 219);

    /**
     * Create a styled button with proper colors that work with all Look and Feels
     */
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        styleButton(btn, bgColor, fgColor);
        return btn;
    }

    /**
     * Create a primary button (blue)
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, INFO_COLOR, Color.WHITE);
    }

    /**
     * Create a success button (green)
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR, Color.WHITE);
    }

    /**
     * Create a danger button (red)
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER_COLOR, Color.WHITE);
    }

    /**
     * Create a warning button (orange)
     */
    public static JButton createWarningButton(String text) {
        return createStyledButton(text, WARNING_COLOR, Color.WHITE);
    }

    /**
     * Style an existing button
     */
    public static void styleButton(JButton btn, Color bgColor, Color fgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        // Add hover effect
        final Color originalBg = bgColor;
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bgColor.darker());
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(originalBg);
                }
            }
        });
    }

    /**
     * Create a navigation button (for header)
     */
    public static JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 82, 133));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(0, 60, 100));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(0, 82, 133));
            }
        });

        return btn;
    }

    /**
     * Create a default button (gray)
     */
    public static JButton createDefaultButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

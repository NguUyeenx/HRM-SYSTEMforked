package com.hrm.gui.components;

import com.hrm.util.UIColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * PurpleButton - Custom styled button with purple theme
 * Features hover effect and consistent styling
 */
public class PurpleButton extends JButton {

    private Color normalBackground;
    private Color hoverBackground;
    private Color pressedBackground;

    /**
     * Create a primary purple button
     */
    public PurpleButton(String text) {
        this(text, UIColors.PRIMARY_PURPLE, UIColors.PURPLE_HOVER, UIColors.DARK_PURPLE);
    }

    /**
     * Create a button with custom colors
     */
    public PurpleButton(String text, Color normalBg, Color hoverBg, Color pressedBg) {
        super(text);
        this.normalBackground = normalBg;
        this.hoverBackground = hoverBg;
        this.pressedBackground = pressedBg;

        initStyle();
        setupHoverEffect();
    }

    private void initStyle() {
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setBackground(normalBackground);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);
        setContentAreaFilled(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    private void setupHoverEffect() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(hoverBackground);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(normalBackground);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(pressedBackground);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(hoverBackground);
                }
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setBackground(normalBackground);
            setForeground(Color.WHITE);
        } else {
            setBackground(UIColors.BORDER_GRAY);
            setForeground(UIColors.TEXT_GRAY);
        }
    }

    /**
     * Create a success (green) button
     */
    public static PurpleButton success(String text) {
        return new PurpleButton(text,
            UIColors.SUCCESS_GREEN,
            UIColors.darker(UIColors.SUCCESS_GREEN),
            UIColors.darker(UIColors.darker(UIColors.SUCCESS_GREEN)));
    }

    /**
     * Create a danger (red) button
     */
    public static PurpleButton danger(String text) {
        return new PurpleButton(text,
            UIColors.DANGER_RED,
            UIColors.darker(UIColors.DANGER_RED),
            UIColors.darker(UIColors.darker(UIColors.DANGER_RED)));
    }

    /**
     * Create a warning (yellow) button
     */
    public static PurpleButton warning(String text) {
        PurpleButton btn = new PurpleButton(text,
            UIColors.WARNING_YELLOW,
            UIColors.darker(UIColors.WARNING_YELLOW),
            UIColors.darker(UIColors.darker(UIColors.WARNING_YELLOW)));
        btn.setForeground(UIColors.TEXT_DARK);
        return btn;
    }

    /**
     * Create an info (blue) button
     */
    public static PurpleButton info(String text) {
        return new PurpleButton(text,
            UIColors.INFO_BLUE,
            UIColors.darker(UIColors.INFO_BLUE),
            UIColors.darker(UIColors.darker(UIColors.INFO_BLUE)));
    }

    /**
     * Create a secondary (gray) button
     */
    public static PurpleButton secondary(String text) {
        PurpleButton btn = new PurpleButton(text,
            UIColors.BORDER_GRAY,
            UIColors.darker(UIColors.BORDER_GRAY),
            UIColors.darker(UIColors.darker(UIColors.BORDER_GRAY)));
        btn.setForeground(UIColors.TEXT_DARK);
        return btn;
    }
}

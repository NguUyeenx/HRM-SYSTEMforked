package com.hrm.gui.components;

import com.hrm.util.UIColors;

import javax.swing.*;
import java.awt.*;

/**
 * RoundedPanel - Panel with rounded corners
 * Useful for card-style layouts
 */
public class RoundedPanel extends JPanel {

    private int cornerRadius;
    private Color borderColor;
    private int borderWidth;
    private boolean drawShadow;

    /**
     * Create panel with default corner radius (10px)
     */
    public RoundedPanel() {
        this(10);
    }

    /**
     * Create panel with specified corner radius
     */
    public RoundedPanel(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        this.borderColor = null;
        this.borderWidth = 0;
        this.drawShadow = false;

        setOpaque(false);
        setBackground(Color.WHITE);
    }

    /**
     * Create panel with layout (default corner radius)
     */
    public RoundedPanel(LayoutManager layout) {
        this(layout, 10);
    }

    /**
     * Create panel with corner radius and layout
     */
    public RoundedPanel(LayoutManager layout, int cornerRadius) {
        super(layout);
        this.cornerRadius = cornerRadius;
        this.borderColor = null;
        this.borderWidth = 0;
        this.drawShadow = false;

        setOpaque(false);
        setBackground(Color.WHITE);
    }

    /**
     * Set corner radius
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    /**
     * Set border color and width
     */
    public void setBorderStyle(Color color, int width) {
        this.borderColor = color;
        this.borderWidth = width;
        repaint();
    }

    /**
     * Enable/disable shadow effect
     */
    public void setDrawShadow(boolean draw) {
        this.drawShadow = draw;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int shadowOffset = drawShadow ? 3 : 0;
        int width = getWidth() - shadowOffset;
        int height = getHeight() - shadowOffset;

        // Draw shadow
        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(shadowOffset, shadowOffset, width, height, cornerRadius, cornerRadius);
        }

        // Draw background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // Draw border
        if (borderColor != null && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRoundRect(borderWidth / 2, borderWidth / 2,
                    width - borderWidth, height - borderWidth, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }

    /**
     * Factory method: Create a card panel with shadow
     */
    public static RoundedPanel createCard() {
        RoundedPanel panel = new RoundedPanel(15);
        panel.setBackground(Color.WHITE);
        panel.setBorderStyle(UIColors.BORDER_GRAY, 1);
        panel.setDrawShadow(true);
        return panel;
    }

    /**
     * Factory method: Create a card panel without shadow
     */
    public static RoundedPanel createFlatCard() {
        RoundedPanel panel = new RoundedPanel(10);
        panel.setBackground(Color.WHITE);
        panel.setBorderStyle(UIColors.BORDER_GRAY, 1);
        return panel;
    }

    /**
     * Factory method: Create a purple card
     */
    public static RoundedPanel createPurpleCard() {
        RoundedPanel panel = new RoundedPanel(10);
        panel.setBackground(UIColors.LIGHT_PURPLE);
        panel.setBorderStyle(UIColors.PRIMARY_PURPLE, 1);
        return panel;
    }

    /**
     * Factory method: Create a stat card with title and value
     */
    public static RoundedPanel createStatCard(String title, String value, Color accentColor) {
        RoundedPanel card = new RoundedPanel(new BorderLayout());
        card.setCornerRadius(10);
        card.setBackground(Color.WHITE);
        card.setBorderStyle(UIColors.BORDER_GRAY, 1);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Color stripe at top
        JPanel stripe = new JPanel();
        stripe.setBackground(accentColor);
        stripe.setPreferredSize(new Dimension(0, 4));
        card.add(stripe, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout(5, 10));
        content.setOpaque(false);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValue.setForeground(accentColor);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(UIColors.TEXT_GRAY);

        content.add(lblValue, BorderLayout.CENTER);
        content.add(lblTitle, BorderLayout.SOUTH);

        card.add(content, BorderLayout.CENTER);

        return card;
    }
}

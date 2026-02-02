package com.hrm.gui.components;

import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * StatusBadge - Colored badge for displaying status
 * Automatically colors based on status text
 */
public class StatusBadge extends JLabel {

    public enum BadgeType {
        SUCCESS, WARNING, DANGER, INFO, DEFAULT, PURPLE
    }

    public StatusBadge(String status) {
        this(status, getBadgeTypeFromStatus(status));
    }

    public StatusBadge(String status, BadgeType type) {
        super(status);
        initStyle(type);
    }

    private void initStyle(BadgeType type) {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setBorder(new EmptyBorder(4, 10, 4, 10));

        switch (type) {
            case SUCCESS:
                setBackground(UIColors.SUCCESS_GREEN);
                setForeground(Color.WHITE);
                break;
            case WARNING:
                setBackground(UIColors.WARNING_YELLOW);
                setForeground(UIColors.TEXT_DARK);
                break;
            case DANGER:
                setBackground(UIColors.DANGER_RED);
                setForeground(Color.WHITE);
                break;
            case INFO:
                setBackground(UIColors.INFO_BLUE);
                setForeground(Color.WHITE);
                break;
            case PURPLE:
                setBackground(UIColors.PRIMARY_PURPLE);
                setForeground(Color.WHITE);
                break;
            default:
                setBackground(UIColors.BORDER_GRAY);
                setForeground(UIColors.TEXT_DARK);
                break;
        }
    }

    /**
     * Auto-detect badge type from status text
     */
    private static BadgeType getBadgeTypeFromStatus(String status) {
        if (status == null) return BadgeType.DEFAULT;

        String upper = status.toUpperCase();

        // Success statuses
        if (upper.contains("APPROVED") || upper.contains("ACTIVE") ||
            upper.contains("COMPLETED") || upper.contains("SUCCESS") ||
            upper.contains("OPEN") || upper.contains("DONE")) {
            return BadgeType.SUCCESS;
        }

        // Warning statuses
        if (upper.contains("PENDING") || upper.contains("WAITING") ||
            upper.contains("DRAFT") || upper.contains("IN_PROGRESS") ||
            upper.contains("REVIEW")) {
            return BadgeType.WARNING;
        }

        // Danger statuses
        if (upper.contains("REJECTED") || upper.contains("CANCELLED") ||
            upper.contains("LOCKED") || upper.contains("INACTIVE") ||
            upper.contains("FAILED") || upper.contains("ERROR") ||
            upper.contains("CLOSED") || upper.contains("DELETED")) {
            return BadgeType.DANGER;
        }

        // Info statuses
        if (upper.contains("INFO") || upper.contains("NEW") ||
            upper.contains("SUBMITTED")) {
            return BadgeType.INFO;
        }

        return BadgeType.DEFAULT;
    }

    /**
     * Update the status and re-apply styling
     */
    public void setStatus(String status) {
        setText(status);
        initStyle(getBadgeTypeFromStatus(status));
    }

    /**
     * Update the status with specific type
     */
    public void setStatus(String status, BadgeType type) {
        setText(status);
        initStyle(type);
    }

    /**
     * Factory methods for common statuses (Vietnamese)
     */
    public static StatusBadge approved() {
        return new StatusBadge("Da duyet", BadgeType.SUCCESS);
    }

    public static StatusBadge pending() {
        return new StatusBadge("Cho duyet", BadgeType.WARNING);
    }

    public static StatusBadge rejected() {
        return new StatusBadge("Tu choi", BadgeType.DANGER);
    }

    public static StatusBadge active() {
        return new StatusBadge("Hoat dong", BadgeType.SUCCESS);
    }

    public static StatusBadge inactive() {
        return new StatusBadge("Ngung hoat dong", BadgeType.DANGER);
    }

    public static StatusBadge locked() {
        return new StatusBadge("Bi khoa", BadgeType.DANGER);
    }

    public static StatusBadge draft() {
        return new StatusBadge("Nhap", BadgeType.WARNING);
    }

    public static StatusBadge open() {
        return new StatusBadge("Dang mo", BadgeType.SUCCESS);
    }

    public static StatusBadge closed() {
        return new StatusBadge("Da dong", BadgeType.DANGER);
    }
}

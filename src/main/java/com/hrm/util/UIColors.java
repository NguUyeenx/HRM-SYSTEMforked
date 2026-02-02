package com.hrm.util;

import java.awt.Color;

/**
 * UIColors - Purple Theme Color Palette for HRM System
 * Defines all colors used throughout the application
 */
public final class UIColors {

    private UIColors() {
        // Utility class - no instantiation
    }

    // ========================
    // PRIMARY PURPLE COLORS
    // ========================

    /** Main purple color - #8A2BE2 */
    public static final Color PRIMARY_PURPLE = new Color(138, 43, 226);

    /** Light purple for backgrounds - #E6D9FF */
    public static final Color LIGHT_PURPLE = new Color(230, 217, 255);

    /** Dark purple for emphasis - #6A1BB2 */
    public static final Color DARK_PURPLE = new Color(106, 27, 178);

    /** Purple hover state - #7621CE */
    public static final Color PURPLE_HOVER = new Color(118, 33, 206);

    // ========================
    // NEUTRAL COLORS
    // ========================

    /** Pure white - #FFFFFF */
    public static final Color WHITE = new Color(255, 255, 255);

    /** Light gray background - #F8F9FA */
    public static final Color LIGHT_GRAY_BG = new Color(248, 249, 250);

    /** Border gray - #E0E0E0 */
    public static final Color BORDER_GRAY = new Color(224, 224, 224);

    /** Dark text color - #333333 */
    public static final Color TEXT_DARK = new Color(51, 51, 51);

    /** Gray text color - #666666 */
    public static final Color TEXT_GRAY = new Color(102, 102, 102);

    /** Light gray text - #999999 */
    public static final Color TEXT_LIGHT_GRAY = new Color(153, 153, 153);

    // ========================
    // STATUS COLORS
    // ========================

    /** Success green - #28A745 */
    public static final Color SUCCESS_GREEN = new Color(40, 167, 69);

    /** Warning yellow - #FFC107 */
    public static final Color WARNING_YELLOW = new Color(255, 193, 7);

    /** Danger red - #DC3545 */
    public static final Color DANGER_RED = new Color(220, 53, 69);

    /** Info blue - #17A2B8 */
    public static final Color INFO_BLUE = new Color(23, 162, 184);

    // ========================
    // SIDEBAR COLORS
    // ========================

    /** Sidebar background - Dark gray */
    public static final Color SIDEBAR_BG = new Color(37, 42, 52);

    /** Sidebar header/footer background */
    public static final Color SIDEBAR_HEADER_BG = new Color(30, 35, 45);

    /** Sidebar hover state */
    public static final Color SIDEBAR_HOVER = new Color(50, 55, 65);

    /** Sidebar separator */
    public static final Color SIDEBAR_SEPARATOR = new Color(60, 65, 75);

    // ========================
    // TABLE COLORS
    // ========================

    /** Table header background */
    public static final Color TABLE_HEADER_BG = PRIMARY_PURPLE;

    /** Table header text */
    public static final Color TABLE_HEADER_FG = WHITE;

    /** Table alternate row */
    public static final Color TABLE_ALT_ROW = new Color(250, 248, 255);

    /** Table selection background */
    public static final Color TABLE_SELECTION_BG = LIGHT_PURPLE;

    /** Table selection text */
    public static final Color TABLE_SELECTION_FG = DARK_PURPLE;

    // ========================
    // HELPER METHODS
    // ========================

    /**
     * Get darker shade of a color
     */
    public static Color darker(Color color) {
        return new Color(
            Math.max(0, (int)(color.getRed() * 0.8)),
            Math.max(0, (int)(color.getGreen() * 0.8)),
            Math.max(0, (int)(color.getBlue() * 0.8))
        );
    }

    /**
     * Get lighter shade of a color
     */
    public static Color lighter(Color color) {
        return new Color(
            Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * 0.3)),
            Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * 0.3)),
            Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * 0.3))
        );
    }

    /**
     * Get color with alpha transparency
     */
    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}

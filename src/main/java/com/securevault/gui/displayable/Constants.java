package com.securevault.gui.displayable;

import javax.swing.border.SoftBevelBorder;
import java.awt.*;

public class Constants {
    public static final int ICON_WIDTH = 65;
    public static final int ICON_HEIGHT = 85;
    public static final int WIDTH = 900;
    public static final int HEIGHT = 700;
    public static final Color FILE_NAME_COLOR = Color.CYAN;
    public static final Font FILE_NAME_FONT = new Font(Font.DIALOG_INPUT, Font.BOLD, 11);
    public static final Color PATH_LABEL_COLOR = Color.MAGENTA;
    public static final Font PATH_LABEL_FONT = new Font(Font.DIALOG_INPUT, Font.BOLD, 13);
    public static final Color PATH_FIELD_BACKGROUND = Color.YELLOW;
    public static final Color PATH_FIELD_FOREGROUND = Color.BLACK;
    public static final Font PATH_FIELD_FONT = new Font(Font.DIALOG_INPUT, Font.BOLD, 15);
    public static final SoftBevelBorder SELECTED_FILE_BORDER = new SoftBevelBorder(SoftBevelBorder.LOWERED, Color.GREEN, Color.GREEN);
    public static final SoftBevelBorder UNSELECTED_FILE_BORDER = new SoftBevelBorder(SoftBevelBorder.LOWERED, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0));
    public static final Color POPUP_MENU_BACKGROUND = Color.PINK;
    public static final Color POPUP_MENU_FOREGROUND = Color.BLACK;
    public static final Font POPUP_MENU_FONT = new Font(Font.DIALOG, Font.BOLD, 13);
}

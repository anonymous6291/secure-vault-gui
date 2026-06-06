package com.securevault.gui.displayable;

import javax.swing.border.SoftBevelBorder;
import java.awt.*;

public class Constants {
    public static final String FILES_VIEW_BACKGROUND_IMAGE = "background_images/background.jpg";
    public static final String PASSWORD_VIEW_BACKGROUND_IMAGE = "background_images/background.jpg";
    public static final String API_KEY_VIEW_BACKGROUND_IMAGE = "background_images/background.jpg";
    public static final int BACK_BUTTON_WIDTH = 50;
    public static final int BACK_BUTTON_HEIGHT = 50;
    public static final String BACK_BUTTON_ICON = "other_icons/back_button.png";
    public static final int SETTING_BUTTON_WIDTH = 50;
    public static final int SETTING_BUTTON_HEIGHT = 50;
    public static final String SETTING_BUTTON_ICON = "other_icons/setting.png";
    public static final Color SETTING_POPUP_MENU_BACKGROUND = Color.PINK;
    public static final Color SETTING_POPUP_MENU_FOREGROUND = Color.BLACK;
    public static final Font SETTING_POPUP_MENU_FONT = new Font(Font.DIALOG, Font.BOLD, 13);
    public static final int SETTING_SUBMENU_DIALOG_WIDTH = 400;
    public static final int SETTING_SUBMENU_DIALOG_HEIGHT = 300;
    public static final Color SETTING_SUBMENU_DIALOG_BACKGROUND = Color.CYAN;
    public static final Color SETTING_SUBMENU_DIALOG_FOREGROUND = Color.BLACK;
    public static final Font SETTING_SUBMENU_DIALOG_FONT = new Font(Font.DIALOG, Font.BOLD, 13);
    public static final int CONFIRM_AND_CANCEL_BUTTON_WIDTH = 100;
    public static final int CONFIRM_AND_CANCEL_BUTTON_HEIGHT = 50;
    public static final Color CONFIRM_BUTTON_BACKGROUND = Color.RED.brighter();
    public static final Color CONFIRM_BUTTON_FOREGROUND = Color.BLACK;
    public static final Font CONFIRM_BUTTON_FONT = new Font(Font.DIALOG, Font.BOLD, 13);
    public static final Color CANCEL_BUTTON_BACKGROUND = Color.GREEN.brighter();
    public static final Color CANCEL_BUTTON_FOREGROUND = Color.BLACK;
    public static final Font CANCEL_BUTTON_FONT = new Font(Font.DIALOG, Font.BOLD, 13);
    public static final int ICON_WIDTH = 65;
    public static final int ICON_HEIGHT = 85;
    public static final int WIDTH = 900;
    public static final int HEIGHT = 700;
    public static final Color FILE_NAME_COLOR = Color.CYAN;
    public static final Font FILE_NAME_FONT = new Font(Font.DIALOG_INPUT, Font.BOLD, 11);
    public static final Color PATH_LABEL_COLOR = Color.MAGENTA;
    public static final Font PATH_LABEL_FONT = new Font(Font.DIALOG_INPUT, Font.BOLD, 13);
    public static final Color TEXT_FIELD_BACKGROUND = Color.YELLOW;
    public static final Color TEXT_FIELD_FOREGROUND = Color.BLACK;
    public static final Font TEXT_FIELD_FONT = new Font(Font.DIALOG_INPUT, Font.BOLD, 15);
    public static final SoftBevelBorder SELECTED_FILE_BORDER = new SoftBevelBorder(SoftBevelBorder.LOWERED, Color.GREEN, Color.GREEN);
    public static final SoftBevelBorder UNSELECTED_FILE_BORDER = new SoftBevelBorder(SoftBevelBorder.LOWERED, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0));
    public static final Color FILE_POPUP_MENU_BACKGROUND = Color.PINK;
    public static final Color FILE_POPUP_MENU_FOREGROUND = Color.BLACK;
    public static final Font FILE_POPUP_MENU_FONT = new Font(Font.DIALOG, Font.BOLD, 13);
    public static final int TOP_MENU_HEIGHT = 50;
    public static final int PATH_TEXT_AREA_WIDTH = 300;
    public static final int PATH_TEXT_AREA_HEIGHT = 30;
    public static final String LOCKDOWN_VAULT_MENU_MESSAGE = "<html> Vault will is not be accessible during lockdown mode but the data is safe. If you want to proceed then enter the duration of lockdown. (This action is not revertible!)</html>";
    public static final String DESTROY_VAULT_MENU_MESSAGE = "<html> Destroying the vault will cause you to lose all the contents. If you really want to destroy the vault then enter the password and proceed. (This action is not revertible!)</html>";
    public static final String LOCKDOWN_VAULT_DURATION_FIELD_LABEL_MESSAGE = "<html> Duration :</html>";
    public static final String LOCKDOWN_VAULT_DURATION_FIELD_LABEL_INVALID_MESSAGE = "<html> Duration (Invalid!): </html>";
    public static final String DESTROY_VAULT_PASSWORD_FIELD_LABEL_MESSAGE = "<html> Password :</html>";
    public static final String DESTROY_VAULT_PASSWORD_FIELD_LABEL_ERROR_MESSAGE = "<html> Password (Failed!): </html>";
}


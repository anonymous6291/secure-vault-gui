package com.securevault.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class IconManager {
    public static Image getDirectoryIcon() {
        File image = new File("/home/anonymous/Pictures/Background/Screenshot_20250531_153334_Instagram.jpg");
        return new ImageIcon(image.toString()).getImage();
    }

    public static Image getFileIcon(String fileName) {
        File image = new File("/home/anonymous/Pictures/Background/Screenshot_20250531_153327_Instagram.jpg");
        return new ImageIcon(image.toString()).getImage();
    }
}

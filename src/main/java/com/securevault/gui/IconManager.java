package com.securevault.gui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class IconManager {
    private static final ClassLoader classLoader = IconManager.class.getClassLoader();

    public static Image getDirectoryIcon() {
        URL url = classLoader.getResource("other_icons/folder.png");
        if (url == null) {
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    public static Image getUnknownFileIcon() {
        URL url = classLoader.getResource("other_icons/unknown_file.png");
        if (url == null) {
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    public static Image getFileIcon(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return getUnknownFileIcon();
        }
        String extension = fileName.substring(dotIndex + 1);
        URL url = classLoader.getResource("file_icons/" + extension + ".png");
        if (url == null) {
            return getUnknownFileIcon();
        }
        return new ImageIcon(url).getImage();
    }
}

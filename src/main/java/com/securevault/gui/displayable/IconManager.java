package com.securevault.gui.displayable;

import com.securevault.gui.resource.ResourceManager;

import java.net.URL;

public class IconManager {

    public static URL getDirectoryIconURL() {
        return ResourceManager.getResource("other_icons/folder.png");
    }

    public static URL getUnknownFileIconURL() {
        return ResourceManager.getResource("other_icons/unknown_file.png");
    }

    public static URL getFileIconURL(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return getUnknownFileIconURL();
        }
        String extension = fileName.substring(dotIndex + 1);
        URL url = ResourceManager.getResource("file_icons/" + extension + ".png");
        if (url == null) {
            return getUnknownFileIconURL();
        }
        return url;
    }
}

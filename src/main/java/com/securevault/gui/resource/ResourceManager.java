package com.securevault.gui.resource;

import java.net.URL;

public class ResourceManager {
    private static final ClassLoader classLoader = ResourceManager.class.getClassLoader();

    public static URL getResource(String path) {
        return classLoader.getResource(path);
    }
}

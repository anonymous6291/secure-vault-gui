package com.securevault.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

public class DirectoryManager implements FileIconViewEventListener {
    private final TreeMap<String, DirectoryManager> directories = new TreeMap<>();
    private final TreeSet<String> files = new TreeSet<>();
    private final Map<String, FileIconView> fileIconViewMap = new HashMap<>();
    private final Path path;
    private final String directoryName;
    private final DirectoryManager parentDirectoryManager;
    private final DirectoryManagerListener directoryManagerListener;
    private final Semaphore lock = new Semaphore(1, true);
    private volatile JPanel displayPanel;

    DirectoryManager(Path path, DirectoryManager parentDirectoryManager, DirectoryManagerListener directoryManagerListener) {
        this.path = path;
        this.parentDirectoryManager = parentDirectoryManager;
        this.directoryManagerListener = directoryManagerListener;
        this.directoryName = path.getFileName().toString();
        updateUI();
    }

    public TreeMap<String, DirectoryManager> getDirectories() {
        return directories;
    }


    public DirectoryManager getChildDirectoryManager(String directoryName) {
        return directories.get(directoryName);
    }

    public DirectoryManager getDirectorManager(Path path) {
        return directoryManagerListener.getDirectoryManager(path);
    }

    public DirectoryManager getParentDirectoryManager() {
        return parentDirectoryManager;
    }

    public TreeSet<String> getFiles() {
        return files;
    }

    public Path getPath() {
        return path;
    }

    public Map<String, FileIconView> getFileIconViewMap() {
        return fileIconViewMap;
    }

    public void addFile(String[] paths, int i, int n) {
        String fileName = paths[i];
        if (i == n - 1) {
            files.add(fileName);
            fileIconViewMap.put(fileName, new FileIconView(fileName, false, this));
            updateUI();
        } else {
            boolean updateUI = false;
            DirectoryManager directoryManager = directories.get(fileName);
            if (directoryManager == null) {
                directories.put(fileName, directoryManager = new DirectoryManager(Path.of(path.toString(), fileName), this, directoryManagerListener));
                fileIconViewMap.put(fileName, new FileIconView(fileName, true, this));
                updateUI = true;
            }
            directoryManager.addFile(paths, i + 1, n);
            if (updateUI) {
                updateUI();
            }
        }
    }

    public void updateUI() {
        try {
            lock.acquire();
        } catch (InterruptedException _) {
            return;
        }
        try {
            displayPanel = DirectoryDisplayPanelManager.getDirectoryDisplayPanel(this);
        } catch (Exception e) {
            IO.println(e);
        } finally {
            lock.release();
        }
    }

    public JPanel getDisplayPanel() {
        updateUI();
        return displayPanel;
    }

    public void display() {
        directoryManagerListener.displayDirectory(getDisplayPanel());
    }

    public Dimension getDisplaySize() {
        return directoryManagerListener.getDisplayDimension();
    }

    public void back() {
        directoryManagerListener.displayDirectory(parentDirectoryManager.getDisplayPanel());
    }

    @Override
    public void click(String message, MouseEvent mouseEvent) {
    }
}

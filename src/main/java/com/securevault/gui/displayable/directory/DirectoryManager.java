package com.securevault.gui.displayable.directory;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.directory.listeners.DirectoryManagerListener;
import com.securevault.gui.displayable.directory.listeners.FileIconViewEventListener;

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
    private final JPopupMenu popupMenu = new JPopupMenu("Action: ");
    private final TreeMap<String, DirectoryManager> directories = new TreeMap<>();
    private final TreeSet<String> files = new TreeSet<>();
    private final Map<String, FileIconView> fileIconViewMap = new HashMap<>();
    private final Path path;
    private final String directoryName;
    private final DirectoryManager parentDirectoryManager;
    private final DirectoryManagerListener directoryManagerListener;
    private final Semaphore lock = new Semaphore(1, true);
    private JPanel displayPanel;
    private volatile boolean uiChanged;
    private FileIconView lastSelectedFileIconView = new FileIconView("", false, this);

    public DirectoryManager(Path path, DirectoryManager parentDirectoryManager, DirectoryManagerListener directoryManagerListener) {
        this.path = path;
        this.parentDirectoryManager = parentDirectoryManager;
        this.directoryManagerListener = directoryManagerListener;
        this.directoryName = path.getFileName().toString();
        this.uiChanged = true;
        popupMenu.add(getMenuItem("Retrieve", Action.RETRIEVE));
        popupMenu.add(getMenuItem("Delete", Action.DELETE));
        popupMenu.add(getMenuItem("Rename", Action.RENAME));
        popupMenu.add(getMenuItem("Add File", Action.ADD));
        updateUI();
    }

    private JMenuItem getMenuItem(String label, Action action) {
        JMenuItem jMenuItem = new JMenuItem(label);
        jMenuItem.setBackground(Constants.POPUP_MENU_BACKGROUND);
        jMenuItem.setForeground(Constants.POPUP_MENU_FOREGROUND);
        jMenuItem.setFont(Constants.POPUP_MENU_FONT);
        jMenuItem.addActionListener(_ -> manageAction(action));
        return jMenuItem;
    }

    private void setLock() {
        try {
            lock.acquire();
        } catch (InterruptedException _) {
        }
    }

    private void unlock() {
        lock.release();
    }

    private void manageAction(Action action) {
    }

    public TreeMap<String, DirectoryManager> getDirectories() {
        return directories;
    }

    public TreeSet<String> getFiles() {
        return files;
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

    public Path getPath() {
        return path;
    }

    public Map<String, FileIconView> getFileIconViewMap() {
        return fileIconViewMap;
    }

    public void addFile(String[] paths, int i, int n) {
        setLock();
        boolean updateUI = true;
        try {
            String fileName = paths[i];
            if (i == n - 1) {
                files.add(fileName);
                fileIconViewMap.put(fileName, new FileIconView(fileName, false, this));
            } else {
                DirectoryManager directoryManager = directories.get(fileName);
                if (directoryManager == null) {
                    directories.put(fileName, directoryManager = new DirectoryManager(Path.of(path.toString(), fileName), this, directoryManagerListener));
                    fileIconViewMap.put(fileName, new FileIconView(fileName, true, this));
                } else {
                    updateUI = false;
                }
                directoryManager.addFile(paths, i + 1, n);
            }
        } finally {
            unlock();
        }
        if (updateUI) {
            uiChanged = true;
            updateUI();
        }
    }

    public void updateUI() {
        setLock();
        try {
            if (uiChanged) {
                displayPanel = DirectoryDisplayPanelManager.getDirectoryDisplayPanel(this);
                uiChanged = false;
            }
        } catch (Exception e) {
            IO.println(e);
        } finally {
            unlock();
        }
    }

    private JPanel getDisplayPanel0() {
        return displayPanel;
    }

    public JPanel getDisplayPanel() {
        updateUI();
        return getDisplayPanel0();
    }

    public void display() {
        updateUI();
        directoryManagerListener.displayDirectory(getDisplayPanel());
    }

    public Dimension getDisplaySize() {
        return directoryManagerListener.getDisplayDimension();
    }

    public void back() {
        setLock();
        try {
            if (parentDirectoryManager != null) {
                directoryManagerListener.displayDirectory(parentDirectoryManager.getDisplayPanel());
            }
        } finally {
            unlock();
        }
    }

    @Override
    public void click(FileIconView currentSelectedFileIconView, MouseEvent mouseEvent) {
        setLock();
        try {
            lastSelectedFileIconView.removeBorder();
            currentSelectedFileIconView.setBorder();
            if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                popupMenu.show(currentSelectedFileIconView.getDisplayableComponent(), mouseEvent.getX(), mouseEvent.getY());
            } else if (mouseEvent.getClickCount() > 1) {
                String fileName = currentSelectedFileIconView.getFileName();
                if (directories.containsKey(fileName)) {
                    directories.get(fileName).display();
                }
            }
            lastSelectedFileIconView = currentSelectedFileIconView;
        } finally {
            unlock();
        }
    }

    enum Action {
        RETRIEVE, DELETE, RENAME, ADD
    }
}

package com.securevault.gui.displayable.directory;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.directory.listeners.DirectoryViewListener;
import com.securevault.gui.displayable.directory.listeners.FileIconViewEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

public class DirectoryView implements FileIconViewEventListener {
    private final JPopupMenu filePopupMenu = new JPopupMenu("Action: ");
    private final JPopupMenu directoryViewPopupMenu = new JPopupMenu("Action: ");
    private final TreeMap<String, DirectoryView> directories = new TreeMap<>();
    private final TreeSet<String> files = new TreeSet<>();
    private final Map<String, FileIconView> fileIconViewMap = new HashMap<>();
    private String directoryName;
    private final DirectoryView parentDirectoryView;
    private final DirectoryViewListener directoryViewListener;
    private final Semaphore lock = new Semaphore(1, true);
    private final JPanel displayComponent;
    private volatile boolean uiChanged;
    private FileIconView lastSelectedFileIconView = new FileIconView("", false, this);

    public DirectoryView(String directoryName, DirectoryView parentDirectoryView, DirectoryViewListener directoryViewListener) {
        this.parentDirectoryView = parentDirectoryView;
        this.directoryViewListener = directoryViewListener;
        this.directoryName = directoryName;
        this.uiChanged = true;
        displayComponent = new JPanel(new BorderLayout());
        displayComponent.setOpaque(false);
        directoryViewPopupMenu.add(getMenuItem("Add Files", DirectoryViewAction.ADD));
        filePopupMenu.add(getMenuItem("Retrieve", DirectoryViewAction.RETRIEVE));
        filePopupMenu.add(getMenuItem("Delete", DirectoryViewAction.DELETE));
        filePopupMenu.add(getMenuItem("Rename", DirectoryViewAction.RENAME));
        filePopupMenu.add(getMenuItem("Add Files", DirectoryViewAction.ADD));
    }

    private JMenuItem getMenuItem(String label, DirectoryViewAction action) {
        JMenuItem jMenuItem = new JMenuItem(label);
        jMenuItem.setBackground(Constants.FILE_POPUP_MENU_BACKGROUND);
        jMenuItem.setForeground(Constants.FILE_POPUP_MENU_FOREGROUND);
        jMenuItem.setFont(Constants.FILE_POPUP_MENU_FONT);
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

    public void setDirectoryName(String newName) {
        directoryName = newName;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    private void manageAction(DirectoryViewAction action) {
        directoryViewListener.actionPerformed(Path.of(getPath().toString(), lastSelectedFileIconView.getFileName()), action);
    }

    public TreeMap<String, DirectoryView> getDirectories() {
        return directories;
    }

    public TreeSet<String> getFiles() {
        return files;
    }

    public boolean fileExists(String fileName) {
        setLock();
        try {
            return directories.containsKey(fileName) || files.contains(fileName);
        } finally {
            unlock();
        }
    }

    public void addFile(String fileName) {
        setLock();
        try {
            files.add(fileName);
            fileIconViewMap.put(fileName, new FileIconView(fileName, false, this));
        } finally {
            unlock();
        }
        uiChanged = true;
        updateUI();
    }

    public void deleteFile(String fileName) {
        setLock();
        try {
            FileIconView fileIconView = fileIconViewMap.remove(fileName);
            if (fileIconView == null) {
                return;
            }
            if (fileIconView.isDirectory()) {
                directories.remove(fileName);
            } else {
                files.remove(fileName);
            }
        } finally {
            unlock();
        }
        uiChanged = true;
        updateUI();
    }

    public void renameFile(String targetFile, String newFileName) {
        setLock();
        try {
            FileIconView fileIconView = fileIconViewMap.remove(targetFile);
            if (fileIconView == null) {
                return;
            }
            fileIconView.setFileName(newFileName);
            fileIconViewMap.put(newFileName, fileIconView);
            if (fileIconView.isDirectory()) {
                DirectoryView directoryView = directories.remove(targetFile);
                directoryView.setDirectoryName(newFileName);
                directories.put(newFileName, directoryView);
            } else {
                files.remove(targetFile);
                files.add(newFileName);
            }
        } finally {
            unlock();
        }
        uiChanged = true;
        updateUI();
    }

    public DirectoryView getOrMakeChildDirectoryView(String directoryName) {
        setLock();
        boolean uiChanged = false;
        try {
            DirectoryView childDirectoryView = directories.get(directoryName);
            if (childDirectoryView != null) {
                return childDirectoryView;
            }
            uiChanged = true;
            directories.put(directoryName, childDirectoryView = new DirectoryView(directoryName, this, directoryViewListener));
            fileIconViewMap.put(directoryName, new FileIconView(directoryName, true, this));
            return childDirectoryView;
        } finally {
            unlock();
            if (uiChanged) {
                this.uiChanged = true;
                updateUI();
            }
        }
    }

    public DirectoryView getChildDirectoryView(String directoryName) {
        return directories.get(directoryName);
    }

    public DirectoryView getParentDirectoryView() {
        return parentDirectoryView;
    }

    public Path getPath() {
        if (parentDirectoryView == null) {
            return Path.of(directoryName);
        }
        return Path.of(getParentDirectoryView().getPath().toString(), directoryName);
    }

    public Map<String, FileIconView> getFileIconViewMap() {
        return fileIconViewMap;
    }

    public void updateUI() {
        setLock();
        try {
            if (uiChanged) {
                JComponent jComponent = DirectoryDisplayPanelManager.getDirectoryDisplayPanel(this);
                jComponent.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            directoryViewPopupMenu.show(displayComponent, e.getX(), e.getY());
                        }
                    }
                });
                displayComponent.removeAll();
                displayComponent.add(jComponent);
                displayComponent.validate();
                displayComponent.repaint();
                uiChanged = false;
            }
        } catch (Exception e) {
            IO.println(e);
        } finally {
            unlock();
        }
    }

    private JComponent getDisplayComponent0() {
        return displayComponent;
    }

    public JComponent getDisplayComponent() {
        return getDisplayComponent0();
    }

    public void display() {
        directoryViewListener.displayThisDirectoryView(this);
    }

    public Dimension getDisplaySize() {
        return directoryViewListener.getDisplayDimension();
    }

    @Override
    public void click(FileIconView currentSelectedFileIconView, MouseEvent mouseEvent) {
        setLock();
        try {
            lastSelectedFileIconView.removeBorder();
            currentSelectedFileIconView.setBorder();
            if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                filePopupMenu.show(currentSelectedFileIconView.getDisplayableComponent(), mouseEvent.getX(), mouseEvent.getY());
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
}

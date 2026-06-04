package com.securevault.gui.displayable;

import com.securevault.gui.listeners.FileIconViewEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class FileIconView extends MouseAdapter {
    private static final int FILE_NAME_WIDTH = Constants.ICON_WIDTH;
    private static final int FILE_NAME_HEIGHT = 15;
    private static final int FILE_ICON_WIDTH = Constants.ICON_WIDTH;
    private static final int FILE_ICON_HEIGHT = Constants.ICON_HEIGHT - FILE_NAME_HEIGHT;
    private final JPanel jPanel;
    private final JPanel iconPanel;
    private final String fileName;
    private final boolean directory;
    private final FileIconViewEventListener fileIconViewEventListener;
    private boolean borderSet;

    public FileIconView(String fileName, boolean directory, FileIconViewEventListener fileIconViewEventListener) {
        this.fileName = fileName;
        this.directory = directory;
        this.fileIconViewEventListener = fileIconViewEventListener;
        URL iconURL;
        if (directory) {
            iconURL = IconManager.getDirectoryIconURL();
        } else {
            iconURL = IconManager.getFileIconURL(fileName);
        }
        jPanel = new JPanel(new BorderLayout());
        jPanel.setOpaque(false);
        iconPanel = new JPanel(new BorderLayout());
        JPanel icon = new ImagePanel(iconURL, FILE_ICON_WIDTH - 6, FILE_ICON_HEIGHT - 6);
        icon.setOpaque(false);
        iconPanel.add(icon, BorderLayout.CENTER);
        iconPanel.setOpaque(false);
        JLabel fileNameLabel = new JLabel(fileName);
        fileNameLabel.setFont(Constants.FILE_NAME_FONT);
        fileNameLabel.setForeground(Constants.FILE_NAME_COLOR);
        fileNameLabel.setPreferredSize(new Dimension(FILE_NAME_WIDTH, FILE_NAME_HEIGHT));
        fileNameLabel.setVerticalAlignment(JLabel.CENTER);
        fileNameLabel.setHorizontalAlignment(JLabel.CENTER);
        jPanel.setPreferredSize(new Dimension(Constants.ICON_WIDTH, Constants.ICON_HEIGHT));
        jPanel.add(iconPanel, BorderLayout.CENTER);
        jPanel.add(fileNameLabel, BorderLayout.SOUTH);
        jPanel.addMouseListener(this);
        removeBorder();
    }

    public JPanel getDisplayableComponent() {
        return jPanel;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setBorder() {
        iconPanel.setBorder(Constants.SELECTED_FILE_BORDER);
        borderSet = true;
    }

    public void removeBorder() {
        iconPanel.setBorder(Constants.UNSELECTED_FILE_BORDER);
        borderSet = false;
    }

    public boolean isBorderSet() {
        return borderSet;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        fileIconViewEventListener.click(this, mouseEvent);
    }
}
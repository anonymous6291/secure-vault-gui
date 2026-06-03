package com.securevault.gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
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
    private final String fileName;
    private final Border border = new BevelBorder(BevelBorder.LOWERED, Color.RED, Color.RED);
    private final boolean directory;
    private final FileIconViewEventListener fileIconViewEventListener;

    FileIconView(String fileName, boolean directory, FileIconViewEventListener fileIconViewEventListener) {
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
        JPanel iconPanel = new ImagePanel(iconURL, FILE_ICON_WIDTH, FILE_ICON_HEIGHT);
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
        jPanel.setBorder(border);
    }

    public void removeBorder() {
        jPanel.setBorder(null);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        fileIconViewEventListener.click(fileName, mouseEvent);
    }
}
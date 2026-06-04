package com.securevault.gui.displayable.directory;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.WrapLayout;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class DirectoryDisplayPanelManager {
    private static final String BACK_BUTTON_ICON = "other_icons/back_button.png";
    private static final int BACK_BUTTON_WIDTH = 50;
    private static final int BACK_BUTTON_HEIGHT = 50;
    private static final int TOP_MENU_HEIGHT = 50;
    private static final int PATH_TEXT_AREA_WIDTH = 300;
    private static final int PATH_TEXT_AREA_HEIGHT = 30;

    public static JPanel getDirectoryDisplayPanel(DirectoryManager directoryManager) {
        JPanel jPanel = new JPanel(new BorderLayout());
        Dimension dimension = directoryManager.getDisplaySize();
        jPanel.add(getTopView(directoryManager, dimension), BorderLayout.NORTH);
        jPanel.add(getFilesView(directoryManager, dimension), BorderLayout.CENTER);
        jPanel.setBackground(Color.GREEN);
        jPanel.setOpaque(false);
        return jPanel;
    }

    private static JComponent getTopView(DirectoryManager directoryManager, Dimension dimension) {
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setOpaque(false);
        jPanel.setPreferredSize(new Dimension(dimension.width, TOP_MENU_HEIGHT));
        Icon backIcon = new ImageIcon(new ImageIcon(ResourceManager.getResource(BACK_BUTTON_ICON)).getImage().getScaledInstance(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT, Image.SCALE_SMOOTH));
        JButton backButton = new JButton("", backIcon);
        backButton.setPreferredSize(new Dimension(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT));
        backButton.addActionListener(_ -> directoryManager.back());
        backButton.setFocusPainted(false);
        JPanel pathFieldHolder = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pathFieldHolder.setOpaque(false);
        JLabel pathLabel = new JLabel("Path: ");
        pathLabel.setForeground(Constants.PATH_LABEL_COLOR);
        pathLabel.setFont(Constants.PATH_LABEL_FONT);
        pathFieldHolder.add(pathLabel);
        JTextField pathField = new JTextField(directoryManager.getPath().toString());
        pathField.setPreferredSize(new Dimension(PATH_TEXT_AREA_WIDTH, PATH_TEXT_AREA_HEIGHT));
        pathField.setFont(Constants.PATH_FIELD_FONT);
        pathField.setBackground(Constants.PATH_FIELD_BACKGROUND);
        pathField.setForeground(Constants.PATH_FIELD_FOREGROUND);
        pathField.addActionListener(_ -> {
            try {
                DirectoryManager targetDirectoryManager = directoryManager.getDirectorManager(Path.of(pathField.getText()));
                if (targetDirectoryManager != null) {
                    targetDirectoryManager.display();
                } else {
                    pathField.setText(directoryManager.getPath().toString());
                }
            } catch (Exception e) {
                IO.println(e);
            }
        });
        pathFieldHolder.add(pathField);
        jPanel.add(backButton, BorderLayout.WEST);
        jPanel.add(pathFieldHolder, BorderLayout.CENTER);
        return jPanel;
    }

    private static JComponent getFilesView(DirectoryManager directoryManager, Dimension dimension) {
        Map<String, FileIconView> fileIconViewMap = directoryManager.getFileIconViewMap();
        JPanel jPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension dimension1 = super.getPreferredSize();
                return new Dimension(dimension.width, dimension1.height);
            }
        };
        TreeMap<String, DirectoryManager> directories = directoryManager.getDirectories();
        TreeSet<String> files = directoryManager.getFiles();
        jPanel.setOpaque(false);
        jPanel.setDoubleBuffered(true);
        for (String directoryName : directories.keySet()) {
            jPanel.add(fileIconViewMap.get(directoryName).getDisplayableComponent());
        }
        for (String fileName : files) {
            jPanel.add(fileIconViewMap.get(fileName).getDisplayableComponent());
        }
        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.getViewport().setOpaque(false);
        jScrollPane.setOpaque(false);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        return jScrollPane;
    }
}

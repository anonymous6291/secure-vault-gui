package com.securevault.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class DirectoryDisplayPanelManager {
    private static final int TOP_MENU_HEIGHT = 50;

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
        JPanel jPanel = new JPanel();
        jPanel.setOpaque(false);
        jPanel.setBackground(Color.CYAN);
        jPanel.setPreferredSize(new Dimension(dimension.width, TOP_MENU_HEIGHT));
        return jPanel;
    }

    private static JComponent getFilesView(DirectoryManager directoryManager, Dimension dimension) {
        Map<String, FileIconView> fileIconViewMap = directoryManager.getFileIconViewMap();
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(dimension.width, d.height);
            }
        };
        jPanel.setOpaque(false);
        jPanel.setBackground(Color.YELLOW);
        jPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setOpaque(false);
        jScrollPane.setBackground(Color.ORANGE);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        for (String directoryName : directoryManager.getDirectories().keySet()) {
            jPanel.add(fileIconViewMap.get(directoryName).getDisplayableComponent());
        }
        for (String fileName : directoryManager.getFiles()) {
            jPanel.add(fileIconViewMap.get(fileName).getDisplayableComponent());
        }
        return jScrollPane;
    }
}

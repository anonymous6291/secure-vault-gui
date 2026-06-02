package com.securevault.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

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
        JPanel jPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 20,20)) {
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

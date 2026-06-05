package com.securevault.gui.displayable.directory;

import com.securevault.gui.displayable.WrapLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;


public class DirectoryDisplayPanelManager {

    public static JComponent getDirectoryDisplayPanel(DirectoryView directoryView) {
        Map<String, FileIconView> fileIconViewMap = directoryView.getFileIconViewMap();
        JPanel jPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension dimension1 = super.getPreferredSize();
                return new Dimension(directoryView.getDisplaySize().width, dimension1.height);
            }
        };
        TreeMap<String, DirectoryView> directories = directoryView.getDirectories();
        TreeSet<String> files = directoryView.getFiles();
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

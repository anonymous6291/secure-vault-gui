package com.securevault.gui;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.displayable.directory.DirectoryView;
import com.securevault.gui.displayable.directory.DirectoryViewManager;
import com.securevault.gui.displayable.directory.listeners.DirectoryViewManagerListener;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static com.securevault.gui.displayable.Constants.FILES_VIEW_BACKGROUND_IMAGE;

public class SecureVaultGUI implements DirectoryViewManagerListener {
    private final JFrame jFrame;
    private final DirectoryViewManager directoryViewManager;
    private final JPanel passwordViewPanel;
    private final JPanel apiKeyViewPanel;
    private final Dimension dimension;

    SecureVaultGUI(List<Path> files)  {
        jFrame = new JFrame("SecureVault");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int windowWidth = toolkit.getScreenSize().width;
        int windowHeight = toolkit.getScreenSize().height;
        int width = Constants.WIDTH;
        int height = Constants.HEIGHT;
        dimension = new Dimension(width - 1, height);
        jFrame.setSize(width, height);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);
        jFrame.setLocation((windowWidth - width) >> 1, (windowHeight - height) >> 1);
        directoryViewManager = new DirectoryViewManager(this, dimension);
        passwordViewPanel = getViewPanel(FILES_VIEW_BACKGROUND_IMAGE, width, height);
        apiKeyViewPanel = getViewPanel(FILES_VIEW_BACKGROUND_IMAGE, width, height);
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add("Files", directoryViewManager.getDisplayPanel());
        tabbedPane.add("Passwords", passwordViewPanel);
        tabbedPane.add("APIKeys", apiKeyViewPanel);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        jFrame.setContentPane(contentPane);
        addFiles(files);
        jFrame.setVisible(true);
        //root.getChildDirectoryManager("a").display();
    }

    private JPanel getViewPanel(String background, int w, int h) {
        JPanel jPanel = new ImagePanel(ResourceManager.getResource(background), w, h);
        jPanel.setLayout(new BorderLayout());
        return jPanel;
    }

    public void addFile(Path filePath) {
        directoryViewManager.addFile(filePath);
    }

    public void addFiles(List<Path> files) {
        directoryViewManager.addFiles(files);
    }

    @Override
    public void lockdown(long duration) {

    }

    @Override
    public boolean isSelfDestructEnabled() {
        return false;
    }

    @Override
    public int getSelfDestructTries() {
        return 0;
    }

    @Override
    public void setSelfDestruct() {

    }
}

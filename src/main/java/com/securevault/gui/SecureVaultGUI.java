package com.securevault.gui;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.displayable.directory.DirectoryManager;
import com.securevault.gui.displayable.directory.listeners.DirectoryManagerListener;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class SecureVaultGUI implements DirectoryManagerListener {
    private static final String FILES_VIEW_BACKGROUND_IMAGE = "background_images/background.jpg";
    private static final String PASSWORD_VIEW_BACKGROUND_IMAGE = "background_images/background.jpg";
    private static final String API_KEY_VIEW_BACKGROUND_IMAGE = "background_images/background.jpg";
    private final JFrame jFrame;
    private final JPanel fileViewPanel;
    private final JPanel passwordViewPanel;
    private final JPanel apiKeyViewPanel;
    private final Dimension dimension;
    private final DirectoryManager root;

    SecureVaultGUI(List<String> files) {
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
        fileViewPanel = getViewPanel(FILES_VIEW_BACKGROUND_IMAGE, width, height);
        passwordViewPanel = getViewPanel(FILES_VIEW_BACKGROUND_IMAGE, width, height);
        apiKeyViewPanel = getViewPanel(FILES_VIEW_BACKGROUND_IMAGE, width, height);
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add("Files", fileViewPanel);
        tabbedPane.add("Passwords", passwordViewPanel);
        tabbedPane.add("APIKeys", apiKeyViewPanel);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        jFrame.setContentPane(contentPane);
        root = new DirectoryManager(Path.of(""), null, this);
        addFiles(files);
        jFrame.setVisible(true);
        root.display();
        //root.getChildDirectoryManager("a").display();
    }

    private JPanel getViewPanel(String background, int w, int h) {
        JPanel jPanel = new ImagePanel(ResourceManager.getResource(background), w, h);
        jPanel.setLayout(new BorderLayout());
        return jPanel;
    }

    private String[] splitPath(Path path) {
        String pathString = path.normalize().toString();
        if (pathString.startsWith(File.separator)) {
            pathString = pathString.substring(1);
        }
        return pathString.split(Pattern.quote(File.separator));
    }

    private void addFile0(Path path) {
        String[] paths = splitPath(path);
        root.addFile(paths, 0, paths.length);
    }

    public void addFile(String fileName) {
        addFile0(Path.of(fileName));
    }

    public void addFiles(List<String> files) {
        files.forEach(x -> addFile0(Path.of(x)));
    }

    @Override
    public void displayDirectory(JPanel jPanel) {
        fileViewPanel.removeAll();
        fileViewPanel.add(jPanel, BorderLayout.CENTER);
        fileViewPanel.revalidate();
        fileViewPanel.repaint();
        jFrame.revalidate();
        jFrame.repaint();
    }

    @Override
    public DirectoryManager getDirectoryManager(Path path) {
        DirectoryManager root = this.root;
        String[] paths = splitPath(path);
        for (String subPath : paths) {
            root = root.getChildDirectoryManager(subPath);
            if (root == null) {
                return null;
            }
        }
        if (root == this.root) {
            return null;
        }
        return root;
    }

    @Override
    public Dimension getDisplayDimension() {
        return dimension;
    }

    @Override
    public void refreshUI() {

    }

    @Override
    public void addFile(Path from, Path to) {
    }

    @Override
    public void getFile(Path from, Path to) {

    }

    @Override
    public void deleteFile(Path from) {

    }

    @Override
    public void renameFile(Path path) {
    }
}

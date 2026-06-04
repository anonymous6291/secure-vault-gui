package com.securevault.gui;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.DirectoryManager;
import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.listeners.DirectoryManagerListener;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class SecureVaultGUI implements DirectoryManagerListener {
    private static final String BACKGROUND_IMAGE = "background_images/background.jpg";
    private final JFrame jFrame;
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
        URL backgroundURL = ResourceManager.getResource(BACKGROUND_IMAGE);
        JPanel background = new ImagePanel(backgroundURL, width, height);
        background.setBackground(Color.CYAN);
        jFrame.setContentPane(background);
        root = new DirectoryManager(Path.of(""), null, this);
        addFiles(files);
        jFrame.setVisible(true);
        root.display();
        //root.getChildDirectoryManager("a").display();
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
        jFrame.setGlassPane(jPanel);
        jPanel.setVisible(true);
        jPanel.repaint();
        jPanel.validate();
        jFrame.repaint();
        jFrame.validate();
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
}

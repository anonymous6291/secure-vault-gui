package com.securevault.gui;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.directory.DirectoryViewManager;
import com.securevault.gui.displayable.directory.listeners.DirectoryViewManagerListener;
import com.securevault.gui.displayable.keys.KeyManager;
import com.securevault.gui.displayable.keys.KeyType;
import com.securevault.gui.displayable.keys.Pair;
import com.securevault.gui.displayable.keys.listeners.KeyManagerListener;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SecureVaultGUI implements DirectoryViewManagerListener, KeyManagerListener {
    private final JFrame jFrame;
    private final DirectoryViewManager directoryViewManager;
    private final KeyManager passwordManager;
    private final KeyManager apiKeyManager;
    private final Dimension dimension;

    SecureVaultGUI() {
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
        directoryViewManager = new DirectoryViewManager(jFrame, this, dimension);
        passwordManager = new KeyManager(jFrame, this, KeyType.PASSWORD, dimension);
        apiKeyManager = new KeyManager(jFrame, this, KeyType.API_KEY, dimension);
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add("Files", directoryViewManager.getDisplayPanel());
        tabbedPane.add("Passwords", passwordManager.getDisplayPanel());
        tabbedPane.add("APIKeys", apiKeyManager.getDisplayPanel());
        tabbedPane.setSelectedIndex(1);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        jFrame.setContentPane(contentPane);
        jFrame.setVisible(true);
        for (int i = 0; i <= 100; i++) {
            Pair pair = new Pair("googly.com", i + "@gmail.com");
            passwordManager.addKeyToView(pair);
            apiKeyManager.addKeyToView(pair);
        }
        Pair pair = new Pair("googly.com", 0 + "@gmail.com");
        passwordManager.addKeyToView(pair);
        apiKeyManager.addKeyToView(pair);
    }

    public void addFile(Path filePath) {
        directoryViewManager.addFile(filePath);
    }

    public void addFiles(List<Path> files) {
        directoryViewManager.addFiles(files);
    }

    private void addFilesRecursively(Path path, Path removePath) {
        try {
            if (Files.isDirectory(path)) {
                Files.list(path).forEach(subPath -> addFilesRecursively(subPath, removePath));
            } else if (Files.isRegularFile(path)) {
                addFile(removePath.relativize(path));
            }
        } catch (Exception _) {
        }
    }

    @Override
    public void addFileToVault(Path filePath) {
        IO.println("ADD: " + filePath);
    }

    @Override
    public void retrieveFileFromVault(Path path) {
        IO.println("Retrieve: " + path);
    }

    @Override
    public void deleteFileFromVault(Path path) {
        IO.println("Delete: " + path);
        directoryViewManager.deleteFile(path);
    }

    @Override
    public void renameFileFromVault(Path path, String newName) {
        IO.println("Rename :" + path + " => " + newName);
    }

    @Override
    public void close() {
        IO.println("Close");
    }

    @Override
    public void lockdown(long duration) {
        IO.println("Lockdown: " + duration);
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
    public void setSelfDestruct(int tries) {
        IO.println("Tries: " + tries);
    }

    @Override
    public void disableSelfDestruct() {
        IO.println("Disable");
    }

    @Override
    public void selfDestructVault(String password) {
        IO.println("Destroy: " + password);
        throw new RuntimeException();
    }

    @Override
    public void addKey(Pair pair, String value, KeyType keyType) {
        IO.println("ADD : " + pair + " : " + value + " : " + keyType);
    }

    @Override
    public String getKey(Pair pair, KeyType keyType) {
        IO.println("GET : " + pair + " : " + keyType);
        return "Hello";
    }

    @Override
    public void deleteKey(Pair pair, KeyType keyType) {
        IO.println("DELETE : " + pair + " : " + keyType);
    }
}

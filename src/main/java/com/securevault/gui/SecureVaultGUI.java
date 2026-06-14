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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class SecureVaultGUI implements DirectoryViewManagerListener, KeyManagerListener, WindowListener {
    private final JFrame jFrame;
    private final DirectoryViewManager directoryViewManager;
    private final KeyManager passwordManager;
    private final KeyManager apiKeyManager;
    private final Dimension dimension;
    private Consumer<String> failedFilesListConsumer;
    private int pendingFilesCount = 0;
    private double progress = 0;

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
        jFrame.addWindowListener(this);
        directoryViewManager = new DirectoryViewManager(jFrame, this, dimension);
        passwordManager = new KeyManager(jFrame, this, KeyType.PASSWORD, dimension);
        apiKeyManager = new KeyManager(jFrame, this, KeyType.API_KEY, dimension);
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add("Files", directoryViewManager.getDisplayPanel());
        tabbedPane.add("Passwords", passwordManager.getDisplayPanel());
        tabbedPane.add("APIKeys", apiKeyManager.getDisplayPanel());
        //tabbedPane.setSelectedIndex(1);
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
        Thread.startVirtualThread(() -> {
            while (true) {
                try {
                    pendingFilesCount = Integer.parseInt(IO.readln("Pending: "));
                    progress = Double.parseDouble(IO.readln("Progress: "));
                    int n = Integer.parseInt(IO.readln("Failed count: "));
                    for (int  i = 0; i < n; i++) {
                        failedFilesListConsumer.accept(i+" failed to transfer due to xxxxxxxxxxxxxxxxxxxxxyyyyyyyyyyyy");
                    }
                } catch (Exception _) {
                }
            }
        });
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
    public int getNumberOfPendingFileTransfer() {
        return pendingFilesCount;
    }

    @Override
    public double getFileTransferProgress() {
        return progress;
    }

    @Override
    public void registerFailedFileTransferConsumer(Consumer<String> consumer) {
        failedFilesListConsumer = consumer;
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

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        directoryViewManager.shutdown();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}

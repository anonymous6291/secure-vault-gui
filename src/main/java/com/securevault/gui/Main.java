package com.securevault.gui;

import com.securevault.gui.displayable.keys.KeyType;
import com.securevault.gui.displayable.keys.Pair;
import com.securevault.gui.manager.SecureVaultGUI;
import com.securevault.gui.manager.SecureVaultGUIListener;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class Main implements SecureVaultGUIListener {
    private static SecureVaultGUI secureVaultGUI;
    private static Consumer<String> failedFilesListConsumer;
    private static int pendingFilesCount = 0;
    private static double progress = 0;

    static void main() {
        try {
            List<Path> files = new LinkedList<>();
            files.add(Path.of("a/b/c"));
            files.add(Path.of("d/b/c"));
            for (int i = 0; i < 200; i++) {
                files.add(Path.of(i + ".java"));
            }
            SwingUtilities.invokeAndWait(() -> {
                secureVaultGUI = new SecureVaultGUI(new Main());
            });
            Thread.startVirtualThread(() -> {
                while (true) {
                    try {
                        pendingFilesCount = Integer.parseInt(IO.readln("Pending: "));
                        progress = Double.parseDouble(IO.readln("Progress: "));
                        int n = Integer.parseInt(IO.readln("Failed count: "));
                        for (int i = 0; i < n; i++) {
                            failedFilesListConsumer.accept(i + " failed to transfer due to xxxxxxxxxxxxxxxxxxxxxyyyyyyyyyyyy");
                        }
                    } catch (Exception _) {
                    }
                }
            });
           secureVaultGUI.showErrorDialog("Error occurred.");
            IO.println("Response: "+ secureVaultGUI.askForQuery("File hello already exists.", List.of("RENAME","RENAME_ALL","SKIP","SKIP_ALL","REPLACE","REPLACE_ALL")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void recursiveList(Path path, Path remove, List<Path> files) {
        try {
            if (Files.isDirectory(path)) {
                Files.list(path).forEach(x -> recursiveList(x, remove, files));
            } else {
                files.add(remove.relativize(path));
            }
        } catch (Exception e) {
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
    public void addFileToVault(Path from, Path to) {
        IO.println("ADD: " + from + " : " + to);
        List<Path> list = new ArrayList<>();
        recursiveList(from,from,list);
        secureVaultGUI.addFiles(list);
    }

    @Override
    public void retrieveFileFromVault(Path path) {
        IO.println("Retrieve: " + path);
    }

    @Override
    public void deleteFileFromVault(Path path) {
        IO.println("Delete: " + path);
        //directoryViewManager.deleteFile(path);
    }

    @Override
    public void renameFileFromVault(Path path, String newName) {
        IO.println("Rename :" + path + " => " + newName);
    }

    @Override
    public void close() {
        IO.println("Close");
        secureVaultGUI.showLoginPage();
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
    public boolean doLogin(Path path, String password, boolean create) {
        IO.println("Path: [" + path + "] : " + password + " n: " + create);
        return true;
    }

    @Override
    public List<Path> getFilesList() {
        List<Path> files = new ArrayList<>();
        Path target = Path.of("/home/anonymous/Desktop/copy");
        recursiveList(target, target, files);
        return files;
    }

    @Override
    public List<Pair> getKeysList(KeyType keyType) {
        List<Pair> list = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            Pair pair = new Pair("googly.com", i + "@gmail.com");
            list.add(pair);
        }
        return list;
    }
}

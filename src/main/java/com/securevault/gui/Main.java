package com.securevault.gui;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class Main {
    static void main() {
        try {
            List<Path> files = new LinkedList<>();
            files.add(Path.of("a/b/c"));
            files.add(Path.of("d/b/c"));
            for (int i = 0; i < 200; i++) {
                files.add(Path.of(i + ".java"));
            }
            SwingUtilities.invokeAndWait(() -> {
                SecureVaultGUI secureVaultGUI = new SecureVaultGUI();
                try {
                    LinkedList<Path> file = new LinkedList<>();
                    Path path = Path.of("/home/anonymous/Desktop/copy");
                    recursiveList(path, path, file);
                    secureVaultGUI.addFiles(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
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
}

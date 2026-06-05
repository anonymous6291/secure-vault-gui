package com.securevault.gui;

import javax.swing.*;
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
            SwingUtilities.invokeAndWait(() -> new SecureVaultGUI(files));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.securevault.gui;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class Main {
    static void main() {
        try {
            List<String> files = new LinkedList<>();
            files.add("a/b/c");
            files.add("d/b/c");
            for (int i = 0; i < 200; i++) {
                files.add(i + ".java");
            }
            SwingUtilities.invokeAndWait(() -> new SecureVaultGUI(files));
        } catch (Exception e) {
            IO.println(e);
        }
    }
}

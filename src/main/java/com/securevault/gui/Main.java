package com.securevault.gui;

import com.securevault.gui.manager.SecureVaultGUI;
import com.securevault.gui.manager.SecureVaultManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class Main {
    private static SecureVaultGUI secureVaultGUI;
    private static Consumer<String> failedFilesListConsumer;
    private static int pendingFilesCount = 0;
    private static double progress = 0;

    static void main() {
        try {
            /*List<Path> files = new LinkedList<>();
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
        */
            SecureVaultManager secureVaultManager = new SecureVaultManager();
            secureVaultManager.start();
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

package com.securevault.gui.displayable.directory.listeners;

import com.securevault.gui.displayable.directory.DirectoryManager;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public interface DirectoryManagerListener {
    void displayDirectory(JPanel jPanel);

    DirectoryManager getDirectoryManager(Path path);

    Dimension getDisplayDimension();

    void refreshUI();

    void addFile(Path from, Path to);

    void getFile(Path from, Path to);

    void deleteFile(Path from);

    void renameFile(Path path);
}

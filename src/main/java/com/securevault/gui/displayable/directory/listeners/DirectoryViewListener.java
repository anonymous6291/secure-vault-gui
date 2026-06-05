package com.securevault.gui.displayable.directory.listeners;

import com.securevault.gui.displayable.directory.DirectoryView;
import com.securevault.gui.displayable.directory.DirectoryViewAction;

import java.awt.*;
import java.nio.file.Path;

public interface DirectoryViewListener {
    void displayThisDirectoryView(DirectoryView directoryView);

    Dimension getDisplayDimension();

    void actionPerformed(Path filePath, boolean isDirectory, DirectoryViewAction directoryViewAction);
}

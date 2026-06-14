package com.securevault.gui.displayable.directory.listeners;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface DirectoryViewManagerListener {
    int getNumberOfPendingFileTransfer();

    double getFileTransferProgress();

    void registerFailedFileTransferConsumer(Consumer<String> consumer);

    void addFileToVault(Path path);

    void retrieveFileFromVault(Path path);

    void deleteFileFromVault(Path path);

    void renameFileFromVault(Path path, String newName);

    void close();

    void lockdown(long duration);

    boolean isSelfDestructEnabled();

    int getSelfDestructTries();

    void setSelfDestruct(int tries);

    void disableSelfDestruct();

    void selfDestructVault(String password);
}

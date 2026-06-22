package com.securevault.gui.displayable.directory.listeners;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface DirectoryViewManagerListener {
    boolean changeVaultPassword(String oldPassword, String newPassword);

    String getLogs();

    void clearLogs();

    int getNumberOfPendingFileTransfer();

    double getFileTransferProgress();

    void registerFailedFileTransferConsumer(Consumer<String> consumer);

    void abortAllFileTransfers();

    void addFileToVault(Path from, Path to);

    void retrieveFileFromVault(Path from, Path to);

    void deleteFileFromVault(Path path);

    String renameFileFromVault(Path path, String newName);

    void closeVault();

    void lockdown(long duration);

    boolean isSelfDestructEnabled();

    int getSelfDestructTries();

    void setSelfDestruct(int tries);

    void disableSelfDestruct();

    void selfDestructVault(String password);
}

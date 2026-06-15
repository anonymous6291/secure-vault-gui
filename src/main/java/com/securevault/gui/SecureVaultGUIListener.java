package com.securevault.gui;

import com.securevault.gui.displayable.directory.listeners.DirectoryViewManagerListener;
import com.securevault.gui.displayable.keys.KeyType;
import com.securevault.gui.displayable.keys.Pair;
import com.securevault.gui.displayable.keys.listeners.KeyManagerListener;

import java.nio.file.Path;
import java.util.List;

public interface SecureVaultGUIListener extends DirectoryViewManagerListener, KeyManagerListener {
    boolean doLogin(Path path, String password, boolean create);

    List<Path> getFilesList();

    List<Pair> getKeysList(KeyType keyType);
}

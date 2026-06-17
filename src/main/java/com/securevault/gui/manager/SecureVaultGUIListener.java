package com.securevault.gui.manager;

import com.securevault.gui.displayable.directory.listeners.DirectoryViewManagerListener;
import com.securevault.gui.displayable.keys.KeyType;
import com.securevault.gui.displayable.keys.WebsiteIdPair;
import com.securevault.gui.displayable.keys.listeners.KeyManagerListener;

import java.nio.file.Path;
import java.util.List;

public interface SecureVaultGUIListener extends DirectoryViewManagerListener, KeyManagerListener {
    boolean doLogin(Path path, String password, boolean create);

    void shutdown();

    List<Path> getFilesList();

    List<WebsiteIdPair> getKeysList(KeyType keyType);
}

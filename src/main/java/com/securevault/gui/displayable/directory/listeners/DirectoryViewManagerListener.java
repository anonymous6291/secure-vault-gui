package com.securevault.gui.displayable.directory.listeners;

public interface DirectoryViewManagerListener {
    void lockdown(long duration);

    boolean isSelfDestructEnabled();

    int getSelfDestructTries();

    void setSelfDestruct();
}

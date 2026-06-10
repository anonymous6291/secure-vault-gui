package com.securevault.gui.displayable.keys.listeners;

import com.securevault.gui.displayable.keys.KeyType;

public interface KeyManagerListener {
    void add(String name, String value, KeyType keyType);

    String get(String name, KeyType keyType);

    void delete(String name, KeyType keyType);
}

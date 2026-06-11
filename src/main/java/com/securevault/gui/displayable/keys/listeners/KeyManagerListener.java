package com.securevault.gui.displayable.keys.listeners;

import com.securevault.gui.displayable.keys.KeyType;
import com.securevault.gui.displayable.keys.Pair;

public interface KeyManagerListener {
    void addKey(Pair pair, String value, KeyType keyType);

    String getKey(Pair pair, KeyType keyType);

    void deleteKey(Pair pair, KeyType keyType);
}

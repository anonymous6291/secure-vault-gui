package com.securevault.gui.displayable.keys.listeners;

import com.securevault.gui.displayable.keys.KeyType;
import com.securevault.gui.displayable.keys.WebsiteIdPair;

public interface KeyManagerListener {
    void addKey(WebsiteIdPair websiteIdPair, String value, KeyType keyType);

    String getKey(WebsiteIdPair websiteIdPair, KeyType keyType);

    void deleteKey(WebsiteIdPair websiteIdPair, KeyType keyType);
}

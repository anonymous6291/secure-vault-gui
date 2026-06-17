package com.securevault.gui.displayable.keys.listeners;

import com.securevault.gui.displayable.keys.KeyView;
import com.securevault.gui.displayable.keys.WebsiteIdPair;

import java.awt.event.MouseEvent;

public interface KeyViewListener {
    String getValue(WebsiteIdPair websiteIdPair);

    void clicked(MouseEvent mouseEvent, KeyView keyView);
}

package com.securevault.gui.displayable.keys.listeners;

import com.securevault.gui.displayable.keys.KeyView;
import com.securevault.gui.displayable.keys.Pair;

import java.awt.event.MouseEvent;

public interface KeyViewListener {
    String getValue(Pair pair);

    void clicked(MouseEvent mouseEvent, KeyView keyView);
}

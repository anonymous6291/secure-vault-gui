package com.securevault.gui.displayable;

import javax.swing.*;

public class JDialogDisplayer {
    public static void display(JDialog jDialog) {
        Thread.startVirtualThread(() -> jDialog.setVisible(true));
    }
}

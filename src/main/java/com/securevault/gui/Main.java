package com.securevault.gui;

import com.securevault.gui.manager.SecureVaultManager;

public class Main {
    static void main() {
        try {
            new SecureVaultManager().start();
        } catch (Exception e) {
            IO.println(e);
        }
    }
}

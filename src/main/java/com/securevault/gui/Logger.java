package com.securevault.gui;

public class Logger {
    public Logger() {
    }

    public void logInfo(String message) {
        log(message, Type.INFO);
    }

    public void logError(String message) {
        log(message, Type.ERROR);
    }

    public void logSevere(String message) {
        log(message, Type.SEVERE);
    }

    public void log(String message, Type type) {
        IO.println("[" + type + "] : " + message);
    }

    public enum Type {
        INFO, ERROR, SEVERE
    }
}

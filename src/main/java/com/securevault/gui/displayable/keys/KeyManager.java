package com.securevault.gui.displayable.keys;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.displayable.keys.listeners.KeyManagerListener;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

public class KeyManager {
    private final JFrame windowFrame;
    private final KeyManagerListener keyManagerListener;
    private final KeyType keyType;
    private final Dimension dimension;
    private final JPanel displayPanel;
    private final JPanel keyViews;
    private final Semaphore lock = new Semaphore(1, true);
    private final TreeSet<String> keys = new TreeSet<>();
    private final Map<String, KeyView> keyViewMap = new HashMap<>();

    public KeyManager(JFrame windowFrame, KeyManagerListener keyManagerListener, KeyType keyType, Dimension dimension) {
        this.windowFrame = windowFrame;
        this.keyManagerListener = keyManagerListener;
        this.keyType = keyType;
        this.dimension = dimension;
        this.displayPanel = new ImagePanel(ResourceManager.getResource(Constants.KEYS_VIEW_BACKGROUND_IMAGE), dimension.width, dimension.height);
        displayPanel.setLayout(new BorderLayout());
        keyViews = new JPanel(new BorderLayout());
        keyViews.setOpaque(false);
        displayPanel.add(keyViews, BorderLayout.CENTER);
    }

    public JPanel getDisplayPanel() {
        return displayPanel;
    }

    private void setLock() {
        try {
            lock.acquire();
        } catch (Exception _) {
        }
    }

    private void unlock() {
        lock.release();
    }

    private void addKeyToView0(String name) {
        setLock();
        keys.add(name);
        keyViewMap.put(name, new KeyView(name, dimension.width >> 1));
        unlock();
    }

    public void addKeyToView(String name) {
        addKeyToView0(name);
        updateUI();
    }

    public void addKeysToView(List<String> names) {
        names.forEach(this::addKeyToView0);
        updateUI();
    }

    private void deleteKeyFromView0(String name) {
        setLock();
        unlock();
    }

    public void deleteKeyFromView(String name) {
        deleteKeyFromView0(name);
        updateUI();
    }

    private void updateUI() {
        setLock();
        try {
            JPanel jPanel = new JPanel(new GridLayout(0, 2));
            JScrollPane jScrollPane = new JScrollPane(jPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane.getVerticalScrollBar().setUnitIncrement(20);
            jScrollPane.setOpaque(false);
            jScrollPane.getViewport().setOpaque(false);
            jPanel.setOpaque(false);
            for (String name : keys) {
                jPanel.add(keyViewMap.get(name).getView());
            }
            keyViews.removeAll();
            keyViews.validate();
            keyViews.add(jScrollPane, BorderLayout.CENTER);
            keyViews.validate();
            keyViews.repaint();
        } finally {
            unlock();
        }
    }
}
package com.securevault.gui.displayable.keys;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.displayable.WrapLayout;
import com.securevault.gui.displayable.keys.listeners.KeyManagerListener;
import com.securevault.gui.displayable.keys.listeners.KeyViewListener;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

public class KeyManager implements KeyViewListener {
    private final JFrame windowFrame;
    private final KeyManagerListener keyManagerListener;
    private final KeyType keyType;
    private final Dimension dimension;
    private final JPanel displayPanel;
    private final JPanel keyViews;
    private final Semaphore lock = new Semaphore(1, true);
    private final Map<Pair, KeyView> keyViewMap = new TreeMap<>();

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

    private void addKeyToView0(Pair pair) {
        setLock();
        keyViewMap.put(pair, new KeyView(pair, this));
        unlock();
    }

    public void addKeyToView(Pair pair) {
        addKeyToView0(pair);
        updateUI();
    }

    public void addKeysToView(List<Pair> values) {
        values.forEach(this::addKeyToView0);
        updateUI();
    }

    private void deleteKeyFromView0(Pair pair) {
        setLock();
        unlock();
    }

    public void deleteKeyFromView(Pair pair) {
        deleteKeyFromView0(pair);
        updateUI();
    }

    private void updateUI() {
        setLock();
        try {
            JPanel jPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 20, 20));
            JScrollPane jScrollPane = new JScrollPane(jPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane.getVerticalScrollBar().setUnitIncrement(20);
            jScrollPane.setOpaque(false);
            jScrollPane.getViewport().setOpaque(false);
            jPanel.setOpaque(false);
            keyViewMap.values().forEach(x -> jPanel.add(x.getView()));
            keyViews.removeAll();
            keyViews.validate();
            keyViews.add(jScrollPane, BorderLayout.CENTER);
            keyViews.validate();
            keyViews.repaint();
        } finally {
            unlock();
        }
    }

    @Override
    public String getValue(Pair pair) {
        return "Hello World!!!!";
    }

    @Override
    public void clicked(MouseEvent mouseEvent, KeyView keyView) {
    }
}
package com.securevault.gui.displayable.directory;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.displayable.directory.listeners.DirectoryViewListener;
import com.securevault.gui.displayable.directory.listeners.DirectoryViewManagerListener;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static com.securevault.gui.displayable.Constants.*;

public class DirectoryViewManager implements DirectoryViewListener {
    private final JPopupMenu settingPopupMenu = new JPopupMenu("Setting");
    private final DirectoryViewManagerListener directoryViewManagerListener;
    private final JPanel displayPanel;
    private final DirectoryView rootDirectoryView;
    private volatile DirectoryView currentDirectoryView;
    private JButton settingButton;
    private final Dimension directoryViewSize;
    private JTextField pathField;

    public DirectoryViewManager(DirectoryViewManagerListener directoryViewManagerListener, Dimension displaySize) {
        this.directoryViewManagerListener = directoryViewManagerListener;
        directoryViewSize = new Dimension(displaySize.width, displaySize.height - Constants.TOP_MENU_HEIGHT);
        rootDirectoryView = currentDirectoryView = new DirectoryView(Path.of(""), null, this);
        displayPanel = new ImagePanel(ResourceManager.getResource(FILES_VIEW_BACKGROUND_IMAGE), displaySize.width, displaySize.height);
        displayPanel.setLayout(new BorderLayout());
        initSettingPopupMenu();
        displayPanel.add(getTopView(displaySize), BorderLayout.NORTH);
        displayPanel.add(currentDirectoryView.getDisplayComponent(), BorderLayout.CENTER);
    }

    private JComponent getTopView(Dimension dimension) {
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setOpaque(false);
        jPanel.setPreferredSize(new Dimension(dimension.width, TOP_MENU_HEIGHT));
        Icon backIcon = new ImageIcon(new ImageIcon(ResourceManager.getResource(BACK_BUTTON_ICON)).getImage().getScaledInstance(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT, Image.SCALE_SMOOTH));
        JButton backButton = new JButton("", backIcon);
        backButton.setPreferredSize(new Dimension(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT));
        backButton.addActionListener(_ -> back());
        backButton.setFocusPainted(false);
        JPanel pathFieldHolder = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pathFieldHolder.setOpaque(false);
        JLabel pathLabel = new JLabel("Path: ");
        pathLabel.setForeground(Constants.PATH_LABEL_COLOR);
        pathLabel.setFont(Constants.PATH_LABEL_FONT);
        pathFieldHolder.add(pathLabel);
        pathField = new JTextField("");
        pathField.setPreferredSize(new Dimension(PATH_TEXT_AREA_WIDTH, PATH_TEXT_AREA_HEIGHT));
        pathField.setFont(Constants.PATH_FIELD_FONT);
        pathField.setBackground(Constants.PATH_FIELD_BACKGROUND);
        pathField.setForeground(Constants.PATH_FIELD_FOREGROUND);
        pathField.addActionListener(_ -> gotoDirectory(pathField.getText()));
        pathFieldHolder.add(pathField);
        Icon setting = new ImageIcon(new ImageIcon(ResourceManager.getResource(SETTING_BUTTON_ICON)).getImage().getScaledInstance(SETTING_BUTTON_WIDTH, SETTING_BUTTON_HEIGHT, Image.SCALE_SMOOTH));
        settingButton = new JButton("", setting);
        settingButton.setPreferredSize(new Dimension(SETTING_BUTTON_WIDTH, SETTING_BUTTON_HEIGHT));
        settingButton.setFocusPainted(false);
        settingButton.addActionListener(_ -> displaySettingPopupMenu());
        jPanel.add(backButton, BorderLayout.WEST);
        jPanel.add(pathFieldHolder, BorderLayout.CENTER);
        jPanel.add(settingButton, BorderLayout.EAST);
        return jPanel;
    }

    private JMenuItem getSettingMenuItem(String message) {
        JMenuItem jMenuItem = new JMenuItem(message);
        jMenuItem.setBackground(SETTING_POPUP_MENU_BACKGROUND);
        jMenuItem.setForeground(SETTING_POPUP_MENU_FOREGROUND);
        jMenuItem.setFont(SETTING_POPUP_MENU_FONT);
        return jMenuItem;
    }

    private void initSettingPopupMenu() {
        JMenuItem closeVault = getSettingMenuItem("Close Vault");
        closeVault.addActionListener(_ -> manageCloseVaultMenu());
        JMenuItem lockdownVault = getSettingMenuItem("Lockdown Vault");
        lockdownVault.addActionListener(_ -> manageLockdownVaultMenu());
        JMenuItem selfDestructStatus = getSettingMenuItem("Self Destruct Status");
        selfDestructStatus.addActionListener(_ -> manageSelfDestructStatusMenu());
        settingPopupMenu.add(closeVault);
        settingPopupMenu.add(lockdownVault);
        settingPopupMenu.add(selfDestructStatus);
    }

    private void displaySettingPopupMenu() {
        settingPopupMenu.show(settingButton, -150, 20);
    }

    public JPanel getDisplayPanel() {
        return displayPanel;
    }

    private void changePathLabel() {
        pathField.setText(currentDirectoryView.getPath().toString());
    }

    public void displayDirectoryView(DirectoryView newDirectoryView) {
        if (currentDirectoryView == newDirectoryView) {
            return;
        }
        displayPanel.remove(currentDirectoryView.getDisplayComponent());
        displayPanel.add(newDirectoryView.getDisplayComponent(), BorderLayout.CENTER);
        currentDirectoryView = newDirectoryView;
        changePathLabel();
        refreshUI();
    }

    private String[] splitPath(Path path) {
        path = path.normalize();
        String pathString = path.normalize().toString();
        return pathString.split(Pattern.quote(File.separator));
    }

    private void addFile0(Path path) {
        DirectoryView current = currentDirectoryView;
        String[] split = splitPath(path);
        int n = split.length - 1;
        for (int i = 0; i < n; i++) {
            current = current.getOrMakeChildDirectoryView(split[i]);
        }
        current.addFile(split[n]);
    }

    public void addFile(Path path) {
        addFile0(path);
    }

    public void addFiles(List<Path> files) {
        files.forEach(this::addFile0);
    }

    public void back() {
        DirectoryView parentDirectoryView = currentDirectoryView.getParentDirectoryView();
        if (parentDirectoryView == null) {
            return;
        }
        displayDirectoryView(parentDirectoryView);
    }

    public void gotoDirectory(String path) {
        String[] paths = splitPath(Path.of(path));
        DirectoryView current = rootDirectoryView;
        for (String subPath : paths) {
            current = current.getChildDirectoryView(subPath);
            if (current == null) {
                return;
            }
        }
        current.display();
    }

    public void refreshUI() {
        displayPanel.validate();
        displayPanel.repaint();
    }

    private void manageCloseVaultMenu() {
    }

    private void manageLockdownVaultMenu() {
    }

    private void manageSelfDestructStatusMenu() {
    }

    @Override
    public void displayThisDirectoryView(DirectoryView directoryView) {
        displayDirectoryView(directoryView);
    }

    @Override
    public Dimension getDisplayDimension() {
        return directoryViewSize;
    }

    @Override
    public void actionPerformed(Path filePath, boolean isDirectory, DirectoryViewAction action) {
    }
}

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
    private final JFrame windowFrame;
    private final JPopupMenu settingPopupMenu = new JPopupMenu("Setting");
    private JDialog closeVaultDialog;
    private JDialog lockdownVaultDialog;
    private JDialog destroyVaultDialog;
    private JDialog selfDestructStatusDialog;
    private final DirectoryViewManagerListener directoryViewManagerListener;
    private final JPanel displayPanel;
    private final DirectoryView rootDirectoryView;
    private volatile DirectoryView currentDirectoryView;
    private JButton settingButton;
    private final Dimension displaySize;
    private final Dimension directoryViewSize;
    private JTextField pathField;

    public DirectoryViewManager(JFrame windowFrame, DirectoryViewManagerListener directoryViewManagerListener, Dimension displaySize) {
        this.windowFrame = windowFrame;
        this.directoryViewManagerListener = directoryViewManagerListener;
        this.displaySize = displaySize;
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
        closeVault.addActionListener(_ -> manageSettingMenu(closeVaultDialog));
        JMenuItem lockdownVault = getSettingMenuItem("Lockdown Vault");
        lockdownVault.addActionListener(_ -> manageSettingMenu(lockdownVaultDialog));
        JMenuItem destroyVault = getSettingMenuItem("Destroy Vault");
        destroyVault.addActionListener(_ -> manageSettingMenu(destroyVaultDialog));
        JMenuItem selfDestructStatus = getSettingMenuItem("Self Destruct Status");
        selfDestructStatus.addActionListener(_ -> manageSettingMenu(selfDestructStatusDialog));
        settingPopupMenu.add(closeVault);
        settingPopupMenu.add(lockdownVault);
        settingPopupMenu.add(destroyVault);
        settingPopupMenu.add(selfDestructStatus);
        initCloseVaultDialog();
        initLockdownVaultDialog();
        initDestroyVaultDialog();
        initSelfDestructStatusDialog();
    }

    private Point getSettingSubMenuPosition() {
        return new Point((displaySize.width - SETTING_SUBMENU_DIALOG_WIDTH) >> 1, (displaySize.height - SETTING_SUBMENU_DIALOG_HEIGHT) >> 1);
    }

    private JDialog getSettingDefaultDialog(String top) {
        JDialog jDialog = new JDialog(windowFrame, top, true);
        jDialog.setSize(new Dimension(SETTING_SUBMENU_DIALOG_WIDTH, SETTING_SUBMENU_DIALOG_HEIGHT));
        jDialog.setLocationRelativeTo(windowFrame);
        jDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        jDialog.setResizable(false);
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.GREEN);
        jDialog.add(jPanel);
        return jDialog;
    }

    private void initCloseVaultDialog() {
        closeVaultDialog = getSettingDefaultDialog("Close Vault");
    }

    private void initLockdownVaultDialog() {
        lockdownVaultDialog = getSettingDefaultDialog( "Lockdown Vault");
    }

    private void initDestroyVaultDialog() {
        destroyVaultDialog = getSettingDefaultDialog( "Destroy Vault");
    }

    private void initSelfDestructStatusDialog() {
        selfDestructStatusDialog = getSettingDefaultDialog( "Self Destruct Status");
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

    private void manageSettingMenu(JDialog jDialog) {
        jDialog.setVisible(true);
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

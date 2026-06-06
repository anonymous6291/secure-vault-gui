package com.securevault.gui.displayable.directory;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.displayable.directory.listeners.DirectoryViewListener;
import com.securevault.gui.displayable.directory.listeners.DirectoryViewManagerListener;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static com.securevault.gui.displayable.Constants.*;

public class DirectoryViewManager implements DirectoryViewListener {
    private final JFrame windowFrame;
    private final JPopupMenu settingPopupMenu = new JPopupMenu("Setting");
    private final DirectoryViewManagerListener directoryViewManagerListener;
    private final JPanel displayPanel;
    private final DirectoryView rootDirectoryView;
    private final Dimension displaySize;
    private final Dimension directoryViewSize;
    private JDialog closeVaultDialog;
    private JDialog lockdownVaultDialog;
    private JLabel lockdownVaultDurationLabel;
    private JTextField lockdownVaultDuration;
    private JComboBox<String> lockdownVaultDurationUnit;
    private JDialog destroyVaultDialog;
    private JLabel destroyVaultPasswordLabel;
    private JPasswordField destroyVaultPasswordField;
    private JDialog selfDestructStatusDialog;
    private volatile DirectoryView currentDirectoryView;
    private JButton settingButton;
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

    private static JLabel getMessageLabel(String message) {
        JLabel jLabel = new JLabel(message);
        jLabel.setFont(SETTING_SUBMENU_DIALOG_FONT);
        jLabel.setForeground(SETTING_SUBMENU_DIALOG_FOREGROUND);
        jLabel.setHorizontalTextPosition(JLabel.CENTER);
        jLabel.setVerticalAlignment(JLabel.CENTER);
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        jLabel.setVerticalTextPosition(JLabel.CENTER);
        return jLabel;
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
        pathField.setFont(Constants.TEXT_FIELD_FONT);
        pathField.setBackground(Constants.TEXT_FIELD_BACKGROUND);
        pathField.setForeground(Constants.TEXT_FIELD_FOREGROUND);
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
        manageSettingMenu(selfDestructStatusDialog);
    }

    private JDialog getSettingDefaultDialog(String top) {
        JDialog jDialog = new JDialog(windowFrame, top, false);
        jDialog.setSize(new Dimension(SETTING_SUBMENU_DIALOG_WIDTH, SETTING_SUBMENU_DIALOG_HEIGHT));
        jDialog.setLocationRelativeTo(windowFrame);
        jDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        jDialog.setResizable(false);
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.GREEN);
        jDialog.add(jPanel);
        return jDialog;
    }

    private JButton getButton(String text, Color background, Color foreground, Font font, ActionListener actionListener) {
        JButton jButton = new JButton(text);
        jButton.addActionListener(actionListener);
        jButton.setBackground(background);
        jButton.setForeground(foreground);
        jButton.setFont(font);
        jButton.setFocusPainted(false);
        jButton.setPreferredSize(new Dimension(CONFIRM_AND_CANCEL_BUTTON_WIDTH, CONFIRM_AND_CANCEL_BUTTON_HEIGHT));
        return jButton;
    }

    private void initCloseVaultDialog() {
        closeVaultDialog = getSettingDefaultDialog("Close Vault");
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        JLabel jLabel = new JLabel("You want to close the vault?");
        jLabel.setFont(SETTING_SUBMENU_DIALOG_FONT);
        jLabel.setForeground(SETTING_SUBMENU_DIALOG_FOREGROUND);
        jPanel.add(jLabel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        JButton no = getButton("No, don't", CANCEL_BUTTON_BACKGROUND, CANCEL_BUTTON_FOREGROUND, CANCEL_BUTTON_FONT, _ -> closeVaultDialog.setVisible(false));
        jPanel.add(no, gbc);
        gbc.gridx++;
        JButton yes = getButton("Yes, close", CONFIRM_BUTTON_BACKGROUND, CONFIRM_BUTTON_FOREGROUND, CONFIRM_BUTTON_FONT, _ -> closeVault());
        jPanel.add(yes, gbc);
        closeVaultDialog.setContentPane(jPanel);
        closeVaultDialog.validate();
        closeVaultDialog.repaint();
    }

    private void initLockdownVaultDialog() {
        lockdownVaultDialog = getSettingDefaultDialog("Lockdown Vault");
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        JLabel jLabel = getMessageLabel(LOCKDOWN_VAULT_MENU_MESSAGE);
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        labelPanel.add(jLabel, BorderLayout.CENTER);
        jPanel.add(labelPanel, gbc);
        gbc.gridy++;
        JPanel fieldPanel = new JPanel(new FlowLayout());
        fieldPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        lockdownVaultDurationLabel = getMessageLabel(LOCKDOWN_VAULT_DURATION_FIELD_LABEL_MESSAGE);
        lockdownVaultDuration = new JTextField("", 5);
        lockdownVaultDuration.setPreferredSize(new Dimension(80, 30));
        lockdownVaultDuration.setBackground(TEXT_FIELD_BACKGROUND);
        lockdownVaultDuration.setForeground(TEXT_FIELD_FOREGROUND);
        lockdownVaultDuration.setFont(TEXT_FIELD_FONT);
        lockdownVaultDurationUnit = new JComboBox<>(new String[]{"Minute", "Hour", "Day"});
        lockdownVaultDurationUnit.setBackground(TEXT_FIELD_BACKGROUND);
        lockdownVaultDurationUnit.setForeground(TEXT_FIELD_FOREGROUND);
        lockdownVaultDurationUnit.setFont(TEXT_FIELD_FONT);
        fieldPanel.add(lockdownVaultDurationLabel);
        fieldPanel.add(lockdownVaultDuration);
        fieldPanel.add(lockdownVaultDurationUnit);
        jPanel.add(fieldPanel, gbc);
        gbc.gridy++;
        JPanel buttonContainer = new JPanel(new GridBagLayout());
        buttonContainer.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        JButton no = getButton("Cancel", CANCEL_BUTTON_BACKGROUND, CANCEL_BUTTON_FOREGROUND, CANCEL_BUTTON_FONT, _ -> lockdownVaultDialog.setVisible(false));
        buttonContainer.add(no, gridBagConstraints);
        gridBagConstraints.gridx++;
        JButton yes = getButton("Lockdown", CONFIRM_BUTTON_BACKGROUND, CONFIRM_BUTTON_FOREGROUND, CONFIRM_BUTTON_FONT, _ -> lockdownVault());
        buttonContainer.add(yes, gridBagConstraints);
        jPanel.add(buttonContainer, gbc);
        lockdownVaultDialog.setContentPane(jPanel);
        lockdownVaultDialog.validate();
        lockdownVaultDialog.repaint();
    }

    private void initDestroyVaultDialog() {
        destroyVaultDialog = getSettingDefaultDialog("Destroy Vault");
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        JLabel jLabel = getMessageLabel(DESTROY_VAULT_MENU_MESSAGE);
        jPanel.add(jLabel, gbc);
        gbc.gridy++;
        JPanel passwordFieldPanel = new JPanel(new FlowLayout());
        passwordFieldPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        destroyVaultPasswordLabel = getMessageLabel(DESTROY_VAULT_PASSWORD_FIELD_LABEL_MESSAGE);
        destroyVaultPasswordField = new JPasswordField(30);
        destroyVaultPasswordField.setBackground(TEXT_FIELD_BACKGROUND);
        destroyVaultPasswordField.setForeground(TEXT_FIELD_FOREGROUND);
        destroyVaultPasswordField.setFont(TEXT_FIELD_FONT);
        destroyVaultPasswordField.setEchoChar('*');
        destroyVaultPasswordField.setPreferredSize(new Dimension(50, 30));
        passwordFieldPanel.add(destroyVaultPasswordLabel);
        passwordFieldPanel.add(destroyVaultPasswordField);
        jPanel.add(passwordFieldPanel, gbc);
        gbc.gridy++;
        JPanel buttonContainer = new JPanel(new GridBagLayout());
        buttonContainer.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        JButton no = getButton("Cancel", CANCEL_BUTTON_BACKGROUND, CANCEL_BUTTON_FOREGROUND, CANCEL_BUTTON_FONT, _ -> destroyVaultDialog.setVisible(false));
        buttonContainer.add(no, gridBagConstraints);
        gridBagConstraints.gridx++;
        JButton yes = getButton("Destroy", CONFIRM_BUTTON_BACKGROUND, CONFIRM_BUTTON_FOREGROUND, CONFIRM_BUTTON_FONT, _ -> destroyVault());
        buttonContainer.add(yes, gridBagConstraints);
        jPanel.add(buttonContainer, gbc);
        destroyVaultDialog.add(jPanel);
        destroyVaultDialog.validate();
        destroyVaultDialog.repaint();
    }

    private void initSelfDestructStatusDialog() {
        selfDestructStatusDialog = getSettingDefaultDialog("Self Destruct Status");
        selfDestructStatusDialog.validate();
        selfDestructStatusDialog.repaint();
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

    private void closeVault() {
        directoryViewManagerListener.close();
        closeVaultDialog.setVisible(false);
    }

    private void lockdownVault() {
        try {
            long value = Long.parseLong(lockdownVaultDuration.getText());
            String option = (String) lockdownVaultDurationUnit.getSelectedItem();
            if (value <= 0 || option == null) {
                throw new RuntimeException();
            }
            lockdownVaultDurationLabel.setText(LOCKDOWN_VAULT_DURATION_FIELD_LABEL_MESSAGE);
            if (option.startsWith("M")) {
                value *= 60;
            } else if (option.startsWith("H")) {
                value *= 60 * 60;
            } else {
                value *= 24 * 60 * 60;
            }
            directoryViewManagerListener.lockdown(value);
            lockdownVaultDialog.setVisible(false);
        } catch (Exception _) {
            lockdownVaultDurationLabel.setText(LOCKDOWN_VAULT_DURATION_FIELD_LABEL_INVALID_MESSAGE);
            lockdownVaultDurationLabel.repaint();
        }
    }

    private void destroyVault() {
        try {
            directoryViewManagerListener.selfDestructVault(new String(destroyVaultPasswordField.getPassword()));
            destroyVaultPasswordLabel.setText(DESTROY_VAULT_PASSWORD_FIELD_LABEL_MESSAGE);
            destroyVaultDialog.setVisible(false);
        } catch (Exception _) {
            destroyVaultPasswordLabel.setText(DESTROY_VAULT_PASSWORD_FIELD_LABEL_ERROR_MESSAGE);
        }
    }

    private int getSelfDestructTries() {
        return directoryViewManagerListener.getSelfDestructTries();
    }

    private void disableSelfDestruct() {
        directoryViewManagerListener.disableSelfDestruct();
        IO.println("Disable");
    }

    private void setSelfDestruct() {
        directoryViewManagerListener.setSelfDestruct(0);
        IO.println("Enable: " + 0);
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

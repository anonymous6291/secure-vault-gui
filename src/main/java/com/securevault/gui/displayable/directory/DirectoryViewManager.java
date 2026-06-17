package com.securevault.gui.displayable.directory;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.displayable.directory.listeners.DirectoryViewListener;
import com.securevault.gui.displayable.directory.listeners.DirectoryViewManagerListener;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import static com.securevault.gui.displayable.Constants.*;

public class DirectoryViewManager implements DirectoryViewListener {
    private final JFrame windowFrame;
    private final JPopupMenu settingPopupMenu = new JPopupMenu("Setting");
    private final DirectoryViewManagerListener directoryViewManagerListener;
    private final JPanel displayPanel;
    private final DirectoryView rootDirectoryView;
    private final Dimension directoryViewSize;
    private final JFileChooser fileChooser;
    private final FileProgressViewer fileProgressViewer;
    private JDialog renameFileDialog;
    private JLabel renameFileNewNameLabel;
    private JTextField targetRenameFile;
    private JTextField renameFileNewName;
    private JDialog closeVaultDialog;
    private JDialog lockdownVaultDialog;
    private JLabel lockdownVaultDurationLabel;
    private JTextField lockdownVaultDuration;
    private JComboBox<String> lockdownVaultDurationUnit;
    private JDialog destroyVaultDialog;
    private JLabel destroyVaultPasswordLabel;
    private JPasswordField destroyVaultPasswordField;
    private JDialog selfDestructStatusDialog;
    private JCheckBox selfDestructEnabled;
    private JLabel selfDestructTriesLabel;
    private JTextField selfDestructTriesTextField;
    private volatile DirectoryView currentDirectoryView;
    private JButton settingButton;
    private JTextField pathField;

    public DirectoryViewManager(JFrame windowFrame, DirectoryViewManagerListener directoryViewManagerListener, Dimension displaySize) {
        this.windowFrame = windowFrame;
        this.directoryViewManagerListener = directoryViewManagerListener;
        directoryViewSize = new Dimension(displaySize.width, displaySize.height - Constants.TOP_MENU_HEIGHT);
        rootDirectoryView = currentDirectoryView = new DirectoryView("", null, this);
        displayPanel = new ImagePanel(ResourceManager.getResource(FILES_VIEW_BACKGROUND_IMAGE), displaySize.width, displaySize.height);
        displayPanel.setLayout(new BorderLayout());
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose files to add");
        fileChooser.setApproveButtonText("Add");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        initRenameFileDialog();
        initSettingPopupMenu();
        displayPanel.add(getTopView(displaySize), BorderLayout.NORTH);
        displayPanel.add(currentDirectoryView.getDisplayComponent(), BorderLayout.CENTER);
        fileProgressViewer = new FileProgressViewer(directoryViewManagerListener, windowFrame);
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

    private JTextField getTextField(int columns, int width) {
        JTextField jTextField = new JTextField(columns);
        jTextField.setBackground(TEXT_FIELD_BACKGROUND);
        jTextField.setForeground(TEXT_FIELD_FOREGROUND);
        jTextField.setFont(TEXT_FIELD_FONT);
        jTextField.setPreferredSize(new Dimension(width, 30));
        return jTextField;
    }

    private void initRenameFileDialog() {
        renameFileDialog = getSettingDefaultDialog("Rename File");
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        jPanel.add(getMessageLabel(RENAME_FILE_LABEL_TARGET_FILE_MESSAGE), gbc);
        gbc.gridy++;
        targetRenameFile = getTextField(30, 50);
        targetRenameFile.setEditable(false);
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        container.add(targetRenameFile);
        jPanel.add(container, gbc);
        gbc.gridy++;
        renameFileNewNameLabel = getMessageLabel(RENAME_FILE_LABEL_NEW_FILE_MESSAGE);
        jPanel.add(renameFileNewNameLabel, gbc);
        gbc.gridy++;
        container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        renameFileNewName = getTextField(10, 50);
        container.add(renameFileNewName);
        jPanel.add(container, gbc);
        gbc.gridy++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
        buttons.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        JButton no = getButton("Cancel", CONFIRM_BUTTON_BACKGROUND, CONFIRM_BUTTON_FOREGROUND, CONFIRM_BUTTON_FONT, _ -> renameFileDialog.setVisible(false));
        buttons.add(no);
        JButton yes = getButton("Rename", CANCEL_BUTTON_BACKGROUND, CANCEL_BUTTON_FOREGROUND, CANCEL_BUTTON_FONT, _ -> renameButtonTriggered());
        buttons.add(yes);
        jPanel.add(buttons, gbc);
        renameFileDialog.setContentPane(jPanel);
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
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        fieldPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        lockdownVaultDurationLabel = getMessageLabel(LOCKDOWN_VAULT_DURATION_FIELD_LABEL_MESSAGE);
        lockdownVaultDuration = getTextField(5, 80);
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
        destroyVaultDialog.setContentPane(jPanel);
        destroyVaultDialog.validate();
        destroyVaultDialog.repaint();
    }

    private void initSelfDestructStatusDialog() {
        selfDestructStatusDialog = getSettingDefaultDialog("Self Destruct Status");
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        JLabel jLabel = getMessageLabel(SELF_DESTRUCT_MENU_MESSAGE);
        jPanel.add(jLabel, gridBagConstraints);
        gridBagConstraints.gridy++;
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 1));
        valuePanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        JLabel label = getMessageLabel(SELF_DESTRUCT_TOGGLE_BUTTON_LABEL_MESSAGE);
        valuePanel.add(label);
        selfDestructEnabled = new JCheckBox();
        selfDestructEnabled.setFocusPainted(false);
        selfDestructEnabled.addActionListener(_ -> updateValuesAccordingToSelfDestructToggleButton());
        selfDestructEnabled.setForeground(Color.BLACK);
        selfDestructEnabled.setFont(TEXT_FIELD_FONT);
        valuePanel.add(selfDestructEnabled);
        selfDestructTriesLabel = getMessageLabel(SELF_DESTRUCT_TRIES_FIELD_LABEL_MESSAGE);
        valuePanel.add(selfDestructTriesLabel);
        selfDestructTriesTextField = getTextField(10, 50);
        valuePanel.add(selfDestructTriesTextField);
        selfDestructEnabled.setSelected(directoryViewManagerListener.isSelfDestructEnabled());
        selfDestructTriesTextField.setText(Integer.toString(directoryViewManagerListener.getSelfDestructTries()));
        updateValuesAccordingToSelfDestructToggleButton();
        jPanel.add(valuePanel, gridBagConstraints);
        gridBagConstraints.gridy++;
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 1));
        buttonContainer.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
        JButton cancel = getButton("Cancel", CONFIRM_BUTTON_BACKGROUND, CONFIRM_BUTTON_FOREGROUND, CONFIRM_BUTTON_FONT, _ -> selfDestructStatusDialog.setVisible(false));
        buttonContainer.add(cancel);
        JButton save = getButton("Save", CANCEL_BUTTON_BACKGROUND, CANCEL_BUTTON_FOREGROUND, CANCEL_BUTTON_FONT, _ -> handleSelfDestructSelection());
        buttonContainer.add(save);
        jPanel.add(buttonContainer, gridBagConstraints);
        selfDestructStatusDialog.setContentPane(jPanel);
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
        if (path == null) {
            return new String[0];
        }
        path = path.normalize();
        String pathString = path.normalize().toString();
        return pathString.split(Pattern.quote(File.separator));
    }

    private void addFile0(Path path) {
        DirectoryView current = rootDirectoryView;
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

    private void deleteFile0(Path path) {
        String[] paths = splitPath(path);
        DirectoryView directoryView = rootDirectoryView;
        int n = paths.length - 1;
        for (int i = 0; i < n; i++) {
            directoryView = directoryView.getChildDirectoryView(paths[i]);
            if (directoryView == null) {
                return;
            }
        }
        directoryView.deleteFile(paths[n]);
    }

    public void deleteFile(Path path) {
        deleteFile0(path);
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

    public void shutdown() {
        fileProgressViewer.stop();
    }

    private void manageSettingMenu(JDialog jDialog) {
        jDialog.setVisible(true);
    }

    private void closeVault() {
        directoryViewManagerListener.closeVault();
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

    private void updateValuesAccordingToSelfDestructToggleButton() {
        if (selfDestructEnabled.isSelected()) {
            selfDestructEnabled.setBackground(Color.GREEN);
            selfDestructEnabled.setText("Yes");
            selfDestructTriesTextField.setEditable(true);
        } else {
            selfDestructEnabled.setBackground(Color.RED);
            selfDestructEnabled.setText("No");
            selfDestructTriesTextField.setEditable(false);
        }
    }

    private void handleSelfDestructSelection() {
        updateValuesAccordingToSelfDestructToggleButton();
        if (selfDestructEnabled.isSelected()) {
            try {
                int tries = Integer.parseInt(selfDestructTriesTextField.getText());
                if (tries <= 0) {
                    throw new RuntimeException();
                }
                directoryViewManagerListener.setSelfDestruct(tries);
                selfDestructTriesLabel.setText(SELF_DESTRUCT_TRIES_FIELD_LABEL_MESSAGE);
            } catch (Exception _) {
                selfDestructTriesLabel.setText(SELF_DESTRUCT_TRIES_FIELD_LABEL_INVALID_MESSAGE);
                return;
            }
        } else {
            directoryViewManagerListener.disableSelfDestruct();
        }
        selfDestructStatusDialog.setVisible(false);
    }

    private void renameButtonTriggered() {
        try {
            Path targetPath = Path.of(targetRenameFile.getText());
            Path parentPath = targetPath.getParent();
            String targetFileName = targetPath.getFileName().toString();
            String[] paths = splitPath(parentPath);
            DirectoryView directoryView = rootDirectoryView;
            int n = paths.length - 1;
            for (int i = 0; i < n; i++) {
                directoryView = directoryView.getChildDirectoryView(paths[i]);
                if (directoryView == null) {
                    return;
                }
            }
            String renamedName = renameFileNewName.getText();
            if (directoryView.fileExists(renamedName)) {
                renameFileNewNameLabel.setText(RENAME_FILE_LABEL_NEW_FILE_ALREADY_EXISTS_MESSAGE);
            } else {
                directoryViewManagerListener.renameFileFromVault(targetPath, renamedName);
                currentDirectoryView.renameFile(targetFileName, renamedName);
                renameFileDialog.setVisible(false);
                renameFileNewNameLabel.setText(RENAME_FILE_LABEL_NEW_FILE_MESSAGE);
            }
        } catch (Exception e) {
            renameFileNewNameLabel.setText(RENAME_FILE_LABEL_NEW_FILE_INVALID_NAME);
        }
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
    public void actionPerformed(Path filePath, DirectoryViewAction action) {
        switch (action) {
            case ADD -> {
                try {
                    fileChooser.setSelectedFile(null);
                    if (fileChooser.showDialog(displayPanel, null) == JFileChooser.APPROVE_OPTION) {
                        File[] selectedFiles = fileChooser.getSelectedFiles();
                        if (selectedFiles != null) {
                            Arrays.stream(selectedFiles).forEach(x -> directoryViewManagerListener.addFileToVault(x.toPath(), currentDirectoryView.getPath()));
                        }
                    }
                } catch (Exception e) {
                    IO.println(e);
                }
            }
            case RETRIEVE -> directoryViewManagerListener.retrieveFileFromVault(filePath);
            case DELETE -> {
                try {
                    directoryViewManagerListener.deleteFileFromVault(filePath);
                    deleteFile(filePath);
                    DirectoryView directoryView = rootDirectoryView;
                    String[] paths = splitPath(filePath);
                    int n = paths.length - 1;
                    for (int i = 0; i < n; i++) {
                        directoryView = directoryView.getChildDirectoryView(paths[i]);
                        if (directoryView == null) {
                            return;
                        }
                    }
                    currentDirectoryView.deleteFile(filePath.getFileName().toString());
                } catch (Exception e) {
                    IO.println(e);
                }
            }
            case RENAME -> {
                String filePathString = filePath.toString();
                targetRenameFile.setText(filePathString);
                targetRenameFile.setCaretPosition(filePathString.length());
                renameFileNewName.setText("");
                renameFileDialog.setVisible(true);
            }
        }
    }

    static class FileProgressViewer implements Runnable {
        private static final int REFRESH_DELAY_MS = 300;
        private final DirectoryViewManagerListener directoryViewManagerListener;
        private final ConcurrentLinkedQueue<String> failedTransferFileMessages = new ConcurrentLinkedQueue<>();
        private final JFrame jFrame;
        private final JDialog jDialog;
        private final JPanel jPanel;
        private JDialog failedFileDialog;
        private JPanel failedFilesPanel;
        private JLabel failedFilesListLabel;
        private JPanel progressPanel;
        private JLabel progressLabel;
        private JProgressBar jProgressBar;
        private JPanel failedFilePanel;
        private JLabel failedFileLabel;
        private volatile boolean stop;

        public FileProgressViewer(DirectoryViewManagerListener directoryViewManagerListener, JFrame jFrame) {
            this.directoryViewManagerListener = directoryViewManagerListener;
            this.jFrame = jFrame;
            jDialog = new JDialog(jFrame, "");
            jDialog.setSize(new Dimension(PROGRESS_WIDTH, PROGRESS_HEIGHT));
            jDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            jDialog.setResizable(false);
            setLocation();
            jFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    setLocation();
                }
            });
            directoryViewManagerListener.registerFailedFileTransferConsumer(failedTransferFileMessages::offer);
            jPanel = new JPanel(new GridLayout(0, 1));
            jPanel.setOpaque(true);
            jDialog.setContentPane(jPanel);
            jDialog.setVisible(false);
            initProgressUI();
            initFailedFileUI();
            Thread.startVirtualThread(this);
        }

        void initProgressUI() {
            progressPanel = new JPanel(new GridLayout(0, 1));
            progressPanel.setBackground(Color.GREEN);
            progressLabel = getMessageLabel("Transferring 5000 files....");
            JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
            container.setOpaque(false);
            jProgressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
            jProgressBar.setIndeterminate(false);
            jProgressBar.setValue(50);
            jProgressBar.setStringPainted(true);
            jProgressBar.setString("50%");
            container.add(jProgressBar);
            progressPanel.add(progressLabel);
            progressPanel.add(container);
        }

        void initFailedFileUI() {
            failedFileDialog = new JDialog(jFrame, "Failed files", true);
            failedFileDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            failedFileDialog.setSize(SETTING_SUBMENU_DIALOG_WIDTH, SETTING_SUBMENU_DIALOG_HEIGHT);
            failedFileDialog.setLocationRelativeTo(jFrame);
            failedFileDialog.setVisible(false);
            failedFilesListLabel = getMessageLabel("");
            failedFilesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            failedFilesPanel.setBackground(SETTING_SUBMENU_DIALOG_BACKGROUND);
            failedFilesPanel.add(failedFilesListLabel);
            JScrollPane jScrollPane = new JScrollPane(failedFilesPanel);
            jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.getVerticalScrollBar().setUnitIncrement(20);
            failedFileDialog.setContentPane(jScrollPane);
            failedFilePanel = new JPanel(new BorderLayout());
            failedFilePanel.setBackground(Color.BLUE.brighter());
            failedFileLabel = getMessageLabel("10 files failed to transfer, click to show the list....");
            failedFileLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showFailedFileMessages();
                }
            });
            failedFilePanel.add(failedFileLabel, BorderLayout.CENTER);
        }

        void showFailedFileMessages() {
            int n = failedTransferFileMessages.size();
            if (n == 0) {
                return;
            }
            StringBuilder stringBuilder = new StringBuilder("<html>");
            for (int i = 0; i < n; i++) {
                stringBuilder.append(failedTransferFileMessages.poll()).append("<br>");
            }
            failedFilesListLabel.setText(stringBuilder.append("</html>").toString());
            failedFileDialog.setVisible(true);
        }

        public void run() {
            boolean progressPanelAdded = false;
            boolean failedFilePanelAdded = false;
            int n, h = 0;
            while (!stop) {
                if ((n = directoryViewManagerListener.getNumberOfPendingFileTransfer()) != 0) {
                    progressLabel.setText("Transferring " + n + " files.");
                    double p = directoryViewManagerListener.getFileTransferProgress();
                    jProgressBar.setValue((int) p);
                    jProgressBar.setString(p + "%");
                    if (!progressPanelAdded) {
                        jPanel.add(progressPanel);
                        progressPanelAdded = true;
                    }
                } else if (progressPanelAdded) {
                    jPanel.remove(progressPanel);
                    progressPanelAdded = false;
                }
                if ((n = failedTransferFileMessages.size()) != 0) {
                    failedFileLabel.setText("Failed to transfer " + n + " files. Click to view them.");
                    if (!failedFilePanelAdded) {
                        jPanel.add(failedFilePanel);
                        failedFilePanelAdded = true;
                    }
                } else if (failedFilePanelAdded) {
                    jPanel.remove(failedFilePanel);
                    failedFilePanelAdded = false;
                }
                if (progressPanelAdded && failedFilePanelAdded) {
                    if (h != PROGRESS_HEIGHT) {
                        jDialog.setSize(PROGRESS_WIDTH, h = PROGRESS_HEIGHT);
                        jDialog.setVisible(true);
                        setLocation();
                    }
                } else if (progressPanelAdded || failedFilePanelAdded) {
                    if (h != (PROGRESS_HEIGHT >> 1)) {
                        jDialog.setSize(PROGRESS_WIDTH, h = PROGRESS_HEIGHT >> 1);
                        jDialog.setVisible(true);
                        setLocation();
                    }
                } else {
                    if (h != 0) {
                        h = 0;
                        jDialog.setVisible(false);
                    }
                }
                try {
                    Thread.sleep(REFRESH_DELAY_MS);
                } catch (Exception _) {
                }
            }
        }

        private void setLocation() {
            Point point = jFrame.getLocation();
            jDialog.setLocation(new Point(point.x + 3, point.y + jFrame.getSize().height - jDialog.getSize().height - 5));
        }

        public void stop() {
            stop = true;
        }
    }
}

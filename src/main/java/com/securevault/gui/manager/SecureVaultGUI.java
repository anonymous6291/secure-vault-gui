package com.securevault.gui.manager;

import com.securevault.gui.displayable.Constants;
import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.displayable.directory.DirectoryViewManager;
import com.securevault.gui.displayable.keys.KeyManager;
import com.securevault.gui.displayable.keys.KeyType;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

public class SecureVaultGUI implements WindowListener {
    private final SecureVaultGUIListener secureVaultGUIListener;
    private final JFrame jFrame;
    private final Dimension dimension;
    private volatile CompletableFuture<String> optionQuery = null;
    private final Semaphore dialogLock = new Semaphore(1, true);
    private JPanel loginPanel;
    private JPanel vaultViewPanel;
    private JDialog optionDialog;
    private JLabel optionDialogMessageLabel;
    private JComboBox<String> optionDialogOptions;
    private JButton optionDialogButton;
    private JDialog errorDialog;
    private JLabel errorDialogLabel;
    private DirectoryViewManager directoryViewManager;
    private KeyManager passwordManager;
    private KeyManager apiKeyManager;

    public SecureVaultGUI(SecureVaultGUIListener secureVaultGUIListener) {
        this.secureVaultGUIListener = secureVaultGUIListener;
        jFrame = new JFrame("SecureVault");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int windowWidth = toolkit.getScreenSize().width;
        int windowHeight = toolkit.getScreenSize().height;
        int width = Constants.WIDTH;
        int height = Constants.HEIGHT;
        dimension = new Dimension(width, height);
        jFrame.setSize(width, height);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);
        jFrame.setLocation((windowWidth - width) >> 1, (windowHeight - height) >> 1);
        jFrame.addWindowListener(this);
        initOptionDialog();
        initErrorDialog();
        initLoginPage();
        //initVaultViews();
        jFrame.setContentPane(loginPanel);
        jFrame.setVisible(true);
    }

    private JLabel getLabel(String text, Color fg) {
        JLabel jLabel = new JLabel(text);
        jLabel.setForeground(fg);
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        jLabel.setVerticalAlignment(JLabel.CENTER);
        jLabel.setHorizontalTextPosition(JLabel.CENTER);
        jLabel.setVerticalTextPosition(JLabel.CENTER);
        return jLabel;
    }

    private JLabel getLoginPageLabel(String text) {
        return getLabel(text, Constants.LOGIN_PAGE_LABEL_FOREGROUND);
    }

    private JButton getButton(String text, Color bg, Color fg, Font font, ActionListener actionListener) {
        JButton jButton = new JButton(text);
        jButton.addActionListener(actionListener);
        jButton.setFocusPainted(false);
        jButton.setBackground(bg);
        jButton.setForeground(fg);
        jButton.setFont(font);
        return jButton;
    }

    private void initLoginPage() {
        loginPanel = new ImagePanel(ResourceManager.getResource(Constants.LOGIN_PAGE_BACKGROUND_IMAGE), dimension.width, dimension.height);
        loginPanel.setLayout(new GridLayout(0, 1));
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Done");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JLabel vaultPathLabel = getLoginPageLabel(Constants.VAULT_PATH_LABEL_MESSAGE);
        loginPanel.add(vaultPathLabel);
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setOpaque(false);
        JTextField vaultPathField = new JTextField(30);
        vaultPathField.setText("/home");
        vaultPathField.setBackground(Constants.TEXT_FIELD_BACKGROUND);
        vaultPathField.setForeground(Constants.TEXT_FIELD_FOREGROUND);
        vaultPathField.setPreferredSize(new Dimension(50, 30));
        vaultPathField.setFont(Constants.TEXT_FIELD_FONT);
        JButton choosePathButton = getButton("Choose", Color.GREEN, Color.BLACK, Constants.CONFIRM_BUTTON_FONT, _ -> {
            if (fileChooser.showOpenDialog(loginPanel) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    vaultPathField.setText(selectedFile.getPath());
                }
            }
        });
        container.add(vaultPathField);
        container.add(choosePathButton);
        loginPanel.add(container);
        JLabel passwordFieldLabel = getLoginPageLabel(Constants.VAULT_PASSWORD_LABEL_MESSAGE);
        loginPanel.add(passwordFieldLabel);
        container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setOpaque(false);
        JPasswordField passwordField = new JPasswordField(30);
        passwordField.setText("Hello");
        passwordField.setBackground(Constants.TEXT_FIELD_BACKGROUND);
        passwordField.setForeground(Constants.TEXT_FIELD_FOREGROUND);
        passwordField.setFont(Constants.TEXT_FIELD_FONT);
        passwordField.setPreferredSize(new Dimension(50, 30));
        passwordField.setEchoChar('*');
        JCheckBox showPassword = new JCheckBox("Show");
        showPassword.setForeground(Color.CYAN);
        showPassword.setFocusPainted(false);
        showPassword.setOpaque(false);
        showPassword.setSelected(false);
        showPassword.addActionListener(_ -> {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar('\0');
            } else {
                passwordField.setEchoChar('*');
            }
        });
        container.add(passwordField);
        container.add(showPassword);
        loginPanel.add(container);
        container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setOpaque(false);
        JCheckBox create = new JCheckBox(Constants.VAULT_CREATE_CHECKBOX_LABEL_MESSAGE);
        create.setForeground(Color.CYAN);
        create.setFocusPainted(false);
        create.setOpaque(false);
        create.setSelected(true);
        container.add(create);
        loginPanel.add(container);
        container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setOpaque(false);
        ActionListener proceedButtonListener = _ -> {
            try {
                String text = vaultPathField.getText();
                Path path = Path.of(text);
                boolean flag = false;
                if (text.isBlank() || !Files.isDirectory(path)) {
                    vaultPathLabel.setText(Constants.VAULT_PATH_LABEL_ERROR_MESSAGE);
                    flag = true;
                } else {
                    vaultPathLabel.setText(Constants.VAULT_PATH_LABEL_MESSAGE);
                }
                String password = new String(passwordField.getPassword());
                if (password.length() < 5) {
                    passwordFieldLabel.setText(Constants.VAULT_PASSWORD_LABEL_INVALID_MESSAGE);
                    flag = true;
                } else {
                    passwordFieldLabel.setText(Constants.VAULT_PASSWORD_LABEL_MESSAGE);
                }
                if (flag) {
                    return;
                }
                if (secureVaultGUIListener.doLogin(path, password, create.isSelected())) {
                    initVaultViewsAndShow();
                }
            } catch (Exception _) {
            }
        };
        JButton proceed = getButton("Create", Constants.CANCEL_BUTTON_BACKGROUND, Constants.CANCEL_BUTTON_FOREGROUND, Constants.CANCEL_BUTTON_FONT, proceedButtonListener);
        create.addActionListener(_ -> {
            if (create.isSelected()) {
                proceed.setText("Create");
            } else {
                proceed.setText("Open");
            }
        });
        proceed.setPreferredSize(new Dimension(100, 30));
        container.add(proceed);
        loginPanel.add(container);
    }

    private JDialog getJDialog(String name, int w, int h, int closeOperation) {
        JDialog jDialog = new JDialog(jFrame, name, true);
        jDialog.setSize(new Dimension(w, h));
        jDialog.setLocationRelativeTo(jFrame);
        jDialog.setResizable(false);
        jDialog.setDefaultCloseOperation(closeOperation);
        return jDialog;
    }

    private void initOptionDialog() {
        optionDialog = getJDialog("Option", Constants.SETTING_SUBMENU_DIALOG_WIDTH, Constants.SETTING_SUBMENU_DIALOG_HEIGHT, JDialog.DO_NOTHING_ON_CLOSE);
        optionDialog.setVisible(false);
        JPanel jPanel = new JPanel(new GridLayout(0, 1));
        jPanel.setBackground(Constants.SETTING_SUBMENU_DIALOG_BACKGROUND);
        optionDialogMessageLabel = getLabel("", Constants.SETTING_SUBMENU_DIALOG_FOREGROUND);
        jPanel.add(optionDialogMessageLabel);
        jPanel.add(getLabel("<html>Select one of the options:</html>", Constants.SETTING_SUBMENU_DIALOG_FOREGROUND));
        optionDialogOptions = new JComboBox<>();
        optionDialogOptions.setFocusable(false);
        optionDialogOptions.setOpaque(false);
        optionDialogOptions.setFont(Constants.SETTING_SUBMENU_DIALOG_FONT);
        optionDialogOptions.setBackground(new Color(0xD315D3));
        optionDialogOptions.setForeground(Constants.SETTING_SUBMENU_DIALOG_FOREGROUND);
        optionDialogOptions.setFont(Constants.SETTING_SUBMENU_DIALOG_FONT);
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setOpaque(false);
        container.add(optionDialogOptions);
        jPanel.add(container);
        container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setOpaque(false);
        ActionListener actionListener = _ -> {
            Object selectedItem = optionDialogOptions.getSelectedItem();
            optionQuery.complete(selectedItem == null ? "" : selectedItem.toString());
            optionDialog.setVisible(false);
            dialogLock.release();
        };
        optionDialogButton = getButton("Proceed", Constants.CANCEL_BUTTON_BACKGROUND, Constants.CANCEL_BUTTON_FOREGROUND, Constants.CANCEL_BUTTON_FONT, actionListener);
        container.add(optionDialogButton);
        jPanel.add(container);
        optionDialog.setContentPane(jPanel);
    }

    private void initErrorDialog() {
        errorDialog = getJDialog("Error", Constants.SETTING_SUBMENU_DIALOG_WIDTH, Constants.SETTING_SUBMENU_DIALOG_HEIGHT, JDialog.HIDE_ON_CLOSE);
        JPanel jPanel = new JPanel(new GridLayout(0, 1));
        jPanel.setBackground(Constants.SETTING_SUBMENU_DIALOG_BACKGROUND);
        errorDialogLabel = getLabel("", Constants.SETTING_SUBMENU_DIALOG_FOREGROUND);
        jPanel.add(errorDialogLabel);
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setOpaque(false);
        JButton jButton = getButton("Ok", Constants.CANCEL_BUTTON_BACKGROUND, Constants.CANCEL_BUTTON_FOREGROUND, Constants.CANCEL_BUTTON_FONT, _ -> {
            errorDialog.setVisible(false);
            dialogLock.release();
        });
        container.add(jButton);
        jPanel.add(container);
        errorDialog.setContentPane(jPanel);
    }

    private void initVaultViewsAndShow() {
        directoryViewManager = new DirectoryViewManager(jFrame, secureVaultGUIListener, dimension);
        passwordManager = new KeyManager(jFrame, secureVaultGUIListener, KeyType.PASSWORD, dimension);
        apiKeyManager = new KeyManager(jFrame, secureVaultGUIListener, KeyType.API_KEY, dimension);
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add("Files", directoryViewManager.getDisplayPanel());
        tabbedPane.add("Passwords", passwordManager.getDisplayPanel());
        tabbedPane.add("APIKeys", apiKeyManager.getDisplayPanel());
        //tabbedPane.setSelectedIndex(1);
        vaultViewPanel = new JPanel();
        vaultViewPanel.setLayout(new BorderLayout());
        vaultViewPanel.add(tabbedPane, BorderLayout.CENTER);
        directoryViewManager.addFiles(secureVaultGUIListener.getFilesList());
        passwordManager.addKeysToView(secureVaultGUIListener.getKeysList(KeyType.PASSWORD));
        apiKeyManager.addKeysToView(secureVaultGUIListener.getKeysList(KeyType.API_KEY));
        jFrame.setContentPane(vaultViewPanel);
    }

    public void showLoginPage() {
        jFrame.setContentPane(loginPanel);
    }

    public String askForQuery(String query, List<String> options) {
        try {
            dialogLock.acquire();
        } catch (InterruptedException _) {
        }
        try {
            optionQuery = new CompletableFuture<>();
            optionDialogMessageLabel.setText("<html>" + query + "</html>");
            optionDialogOptions.removeAllItems();
            options.forEach(optionDialogOptions::addItem);
            optionDialog.validate();
            optionDialog.repaint();
            optionDialog.setVisible(true);
            return optionQuery.get();
        } catch (Exception e) {
            return "";
        }
    }

    public void showErrorDialog(String message) {
        try {
            dialogLock.acquire();
        } catch (Exception _) {
        }
        errorDialogLabel.setText("<html>" + message + "</html>");
        errorDialog.setVisible(true);
    }

    public void addFile(Path filePath) {
        directoryViewManager.addFile(filePath);
    }

    public void addFiles(List<Path> files) {
        directoryViewManager.addFiles(files);
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (directoryViewManager != null) {
            directoryViewManager.shutdown();
        }
        secureVaultGUIListener.shutdown();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}

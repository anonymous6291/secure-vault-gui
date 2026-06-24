package com.securevault.gui.displayable.keys;

import com.securevault.gui.displayable.ImagePanel;
import com.securevault.gui.displayable.JDialogDisplayer;
import com.securevault.gui.displayable.WrapLayout;
import com.securevault.gui.displayable.keys.listeners.KeyManagerListener;
import com.securevault.gui.displayable.keys.listeners.KeyViewListener;
import com.securevault.gui.resource.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import static com.securevault.gui.displayable.Constants.*;

public class KeyManager implements KeyViewListener {
    private final JFrame windowFrame;
    private final KeyManagerListener keyManagerListener;
    private final KeyType keyType;
    private final JPanel displayPanel;
    private final Semaphore lock = new Semaphore(1, true);
    private final Map<WebsiteIdPair, KeyView> keyViewMap = new TreeMap<>();
    private final JPopupMenu addPopupMenu;
    private final JPopupMenu allPopupMenu;
    private volatile KeyView currentKeyView = new KeyView(new WebsiteIdPair("", ""), null);
    private JDialog addOrEditKeyDialog;
    private JTextField addOrEditDialogWebsiteField;
    private JLabel addOrEditDialogWebsiteFieldLabel;
    private JTextField addOrEditDialogIdField;
    private JLabel addOrEditDialogIdFieldLabel;
    private JTextField addOrEditDialogValueField;
    private JLabel addOrEditDialogValueFieldLabel;
    private volatile boolean addMode;
    private JDialog deleteKeyDialog;
    private JTextField deleteDialogWebsiteField;
    private JTextField deleteDialogIdField;

    public KeyManager(JFrame windowFrame, KeyManagerListener keyManagerListener, KeyType keyType, Dimension dimension) {
        this.windowFrame = windowFrame;
        this.keyManagerListener = keyManagerListener;
        this.keyType = keyType;
        this.displayPanel = new ImagePanel(ResourceManager.getResource(KEYS_VIEW_BACKGROUND_IMAGE), dimension.width, dimension.height);
        displayPanel.setLayout(new BorderLayout());
        addPopupMenu = new JPopupMenu();
        addPopupMenu.add(getMenuItem("Add Key", _ -> displayAddKeyDialog()));
        allPopupMenu = new JPopupMenu();
        allPopupMenu.add(getMenuItem("Add Key", _ -> displayAddKeyDialog()));
        allPopupMenu.add(getMenuItem("Edit Key", _ -> displayEditKeyDialog()));
        allPopupMenu.add(getMenuItem("Delete Key", _ -> displayDeleteKeyDialog()));
        initAddOrEditDialog();
        initDeleteDialog();
        displayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    addPopupMenu.show(displayPanel, e.getX(), e.getY());
                }
            }
        });
    }

    private JMenuItem getMenuItem(String name, ActionListener actionListener) {
        JMenuItem jMenuItem = new JMenuItem(name);
        jMenuItem.addActionListener(actionListener);
        jMenuItem.setBackground(SETTING_POPUP_MENU_BACKGROUND);
        jMenuItem.setForeground(SETTING_POPUP_MENU_FOREGROUND);
        return jMenuItem;
    }

    private JDialog getDialog(String text) {
        JDialog jDialog = new JDialog(windowFrame, text, true);
        jDialog.setSize(new Dimension(SETTING_SUBMENU_DIALOG_WIDTH, SETTING_SUBMENU_DIALOG_HEIGHT));
        jDialog.setLocationRelativeTo(windowFrame);
        jDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        return jDialog;
    }

    private JLabel getLabel(String text) {
        JLabel jLabel = new JLabel(text);
        jLabel.setFont(TEXT_FIELD_FONT);
        jLabel.setForeground(Color.BLACK);
        return jLabel;
    }

    private JTextField getTextField() {
        JTextField jTextField = new JTextField(20);
        jTextField.setBackground(TEXT_FIELD_BACKGROUND);
        jTextField.setForeground(TEXT_FIELD_FOREGROUND);
        jTextField.setFont(TEXT_FIELD_FONT);
        jTextField.setPreferredSize(new Dimension(50, 30));
        return jTextField;
    }

    private JButton getButton(String text, Color bg, Color fg, Font font, ActionListener actionListener) {
        JButton jButton = new JButton(text);
        jButton.setFocusPainted(false);
        jButton.setBackground(bg);
        jButton.setForeground(fg);
        jButton.setFont(font);
        jButton.addActionListener(actionListener);
        return jButton;
    }

    private JPanel getFieldPanel(JLabel jLabel, JTextField jTextField) {
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        jPanel.setOpaque(false);
        jPanel.add(jLabel);
        jPanel.add(jTextField);
        return jPanel;
    }

    private void initAddOrEditDialog() {
        addOrEditKeyDialog = getDialog("");
        addOrEditDialogWebsiteField = getTextField();
        addOrEditDialogIdField = getTextField();
        addOrEditDialogValueField = getTextField();
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBackground(SETTING_POPUP_MENU_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        addOrEditDialogWebsiteFieldLabel = getLabel(WEBSITE_FIELD_MESSAGE);
        addOrEditDialogIdFieldLabel = getLabel(ID_FIELD_MESSAGE);
        addOrEditDialogValueFieldLabel = getLabel(VALUE_FIELD_MESSAGE);
        jPanel.add(getFieldPanel(addOrEditDialogWebsiteFieldLabel, addOrEditDialogWebsiteField), gbc);
        gbc.gridy++;
        jPanel.add(getFieldPanel(addOrEditDialogIdFieldLabel, addOrEditDialogIdField), gbc);
        gbc.gridy++;
        jPanel.add(getFieldPanel(addOrEditDialogValueFieldLabel, addOrEditDialogValueField), gbc);
        gbc.gridy++;
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 50));
        buttonContainer.setOpaque(false);
        JButton no = getButton("Cancel", UNSAFE_BUTTON_BACKGROUND, UNSAFE_BUTTON_FOREGROUND, UNSAFE_BUTTON_FONT, _ -> addOrEditKeyDialog.setVisible(false));
        JButton yes = getButton("Save", SAFE_BUTTON_BACKGROUND, SAFE_BUTTON_FOREGROUND, SAFE_BUTTON_FONT, _ -> addOrEditConfirmButtonPressed());
        buttonContainer.add(no);
        buttonContainer.add(yes);
        jPanel.add(buttonContainer, gbc);
        addOrEditKeyDialog.setContentPane(jPanel);
    }

    private void initDeleteDialog() {
        deleteKeyDialog = getDialog("Delete Key");
        deleteDialogWebsiteField = getTextField();
        deleteDialogIdField = getTextField();
        deleteDialogWebsiteField.setEditable(false);
        deleteDialogIdField.setEditable(false);
        JPanel jPanel = new JPanel(new GridBagLayout());
        jPanel.setBackground(SETTING_POPUP_MENU_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        jPanel.add(getFieldPanel(getLabel("Website: "), deleteDialogWebsiteField), gbc);
        gbc.gridy++;
        jPanel.add(getFieldPanel(getLabel("ID: "), deleteDialogIdField), gbc);
        gbc.gridy++;
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 50));
        buttonContainer.setOpaque(false);
        JButton no = getButton("Cancel", SAFE_BUTTON_BACKGROUND, SAFE_BUTTON_FOREGROUND, SAFE_BUTTON_FONT, _ -> deleteKeyDialog.setVisible(false));
        JButton yes = getButton("Delete", UNSAFE_BUTTON_BACKGROUND, UNSAFE_BUTTON_FOREGROUND, UNSAFE_BUTTON_FONT, _ -> deleteConfirmButtonPressed());
        buttonContainer.add(no);
        buttonContainer.add(yes);
        jPanel.add(buttonContainer, gbc);
        deleteKeyDialog.setContentPane(jPanel);
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

    private void addKeyToView0(WebsiteIdPair websiteIdPair) {
        setLock();
        keyViewMap.put(websiteIdPair, new KeyView(websiteIdPair, this));
        unlock();
        updateUI();
    }

    public void addKeyToView(WebsiteIdPair websiteIdPair) {
        addKeyToView0(websiteIdPair);
    }

    public void addKeysToView(List<WebsiteIdPair> websiteIdPairs) {
        websiteIdPairs.forEach(this::addKeyToView0);
    }

    private void deleteKeyFromView0(WebsiteIdPair websiteIdPair) {
        setLock();
        keyViewMap.remove(websiteIdPair);
        unlock();
        updateUI();
    }

    public void deleteKeyFromView(WebsiteIdPair websiteIdPair) {
        deleteKeyFromView0(websiteIdPair);
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
            jPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        addPopupMenu.show(jPanel, e.getX(), e.getY());
                    }
                }
            });
            keyViewMap.values().forEach(x -> jPanel.add(x.getView()));
            displayPanel.removeAll();
            displayPanel.validate();
            displayPanel.add(jScrollPane, BorderLayout.CENTER);
            displayPanel.validate();
            displayPanel.repaint();
        } finally {
            unlock();
        }
    }


    private void addOrEditConfirmButtonPressed() {
        String websiteName = addOrEditDialogWebsiteField.getText();
        String id = addOrEditDialogIdField.getText();
        String value = addOrEditDialogValueField.getText();
        boolean flag = false;
        if (websiteName.isEmpty()) {
            addOrEditDialogWebsiteFieldLabel.setText(WEBSITE_FIELD_ERROR_MESSAGE);
            flag = true;
        } else {
            addOrEditDialogWebsiteFieldLabel.setText(WEBSITE_FIELD_MESSAGE);
        }
        if (id.isEmpty()) {
            addOrEditDialogIdFieldLabel.setText(ID_FIELD_ERROR_MESSAGE);
            flag = true;
        } else {
            addOrEditDialogIdFieldLabel.setText(ID_FIELD_MESSAGE);
        }
        if (value.isEmpty()) {
            addOrEditDialogValueFieldLabel.setText(VALUE_FIELD_ERROR_MESSAGE);
            flag = true;
        } else {
            addOrEditDialogValueFieldLabel.setText(VALUE_FIELD_MESSAGE);
        }
        if (flag) {
            return;
        }
        WebsiteIdPair websiteIdPair = new WebsiteIdPair(websiteName, id);
        if (addMode) {
            if (keyViewMap.containsKey(websiteIdPair)) {
                addOrEditDialogIdFieldLabel.setText(ID_FIELD_EXISTS_MESSAGE);
                return;
            }
            addOrEditDialogIdFieldLabel.setText(ID_FIELD_MESSAGE);
            addKeyToView(websiteIdPair);
        } else {
            keyManagerListener.deleteKey(websiteIdPair, keyType);
        }
        keyManagerListener.addKey(websiteIdPair, value, keyType);
        addOrEditKeyDialog.setVisible(false);
        addOrEditDialogValueField.setText("");
    }

    private void deleteConfirmButtonPressed() {
        WebsiteIdPair websiteIdPair = new WebsiteIdPair(deleteDialogWebsiteField.getText(), deleteDialogIdField.getText());
        keyManagerListener.deleteKey(websiteIdPair, keyType);
        deleteKeyFromView(websiteIdPair);
        deleteKeyDialog.setVisible(false);
    }

    private void displayAddKeyDialog() {
        addOrEditDialogWebsiteField.setText("");
        addOrEditDialogWebsiteField.setEditable(true);
        addOrEditDialogIdField.setText("");
        addOrEditDialogIdField.setEditable(true);
        addOrEditDialogValueField.setText("");
        addMode = true;
        addOrEditKeyDialog.setTitle("Add Key");
        JDialogDisplayer.makeVisible(addOrEditKeyDialog);
    }

    private void displayEditKeyDialog() {
        WebsiteIdPair websiteIdPair = currentKeyView.getPair();
        addOrEditDialogWebsiteField.setText(websiteIdPair.websiteName());
        addOrEditDialogWebsiteField.setEditable(false);
        addOrEditDialogIdField.setText(websiteIdPair.id());
        addOrEditDialogIdField.setEditable(false);
        addOrEditDialogValueField.setText(keyManagerListener.getKey(websiteIdPair, keyType));
        addMode = false;
        addOrEditKeyDialog.setTitle("Edit Key");
        JDialogDisplayer.makeVisible(addOrEditKeyDialog);
    }

    private void displayDeleteKeyDialog() {
        WebsiteIdPair websiteIdPair = currentKeyView.getPair();
        deleteDialogWebsiteField.setText(websiteIdPair.websiteName());
        deleteDialogIdField.setText(websiteIdPair.id());
        JDialogDisplayer.makeVisible(deleteKeyDialog);
    }

    @Override
    public String getValue(WebsiteIdPair websiteIdPair) {
        return keyManagerListener.getKey(websiteIdPair, keyType);
    }

    @Override
    public void clicked(MouseEvent mouseEvent, KeyView keyView) {
        if (currentKeyView != keyView) {
            currentKeyView.removeBorder();
            keyView.setBorder();
            currentKeyView = keyView;
        }
        if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
            allPopupMenu.show(keyView.getView(), mouseEvent.getX(), mouseEvent.getY());
        }
    }
}
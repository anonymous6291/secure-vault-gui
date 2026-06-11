package com.securevault.gui.displayable.keys;

import com.securevault.gui.displayable.ImagePanel;
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
    private final Dimension dimension;
    private final JPanel displayPanel;
    private final JPanel keyViews;
    private final Semaphore lock = new Semaphore(1, true);
    private final Map<Pair, KeyView> keyViewMap = new TreeMap<>();
    private final JPopupMenu addPopupMenu;
    private final JPopupMenu allPopupMenu;
    private volatile KeyView currentKeyView = new KeyView(new Pair("", ""), null);
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
        this.dimension = dimension;
        this.displayPanel = new ImagePanel(ResourceManager.getResource(KEYS_VIEW_BACKGROUND_IMAGE), dimension.width, dimension.height);
        displayPanel.setLayout(new BorderLayout());
        keyViews = new JPanel(new BorderLayout());
        keyViews.setOpaque(false);
        displayPanel.add(keyViews, BorderLayout.CENTER);
        addPopupMenu = new JPopupMenu();
        addPopupMenu.add(getMenuItem("Add Key", _ -> displayAddKeyDialog()));
        allPopupMenu = new JPopupMenu();
        allPopupMenu.add(getMenuItem("Add Key", _ -> displayAddKeyDialog()));
        allPopupMenu.add(getMenuItem("Edit Key", _ -> displayEditKeyDialog()));
        allPopupMenu.add(getMenuItem("Delete Key", _ -> displayDeleteKeyDialog()));
        initAddOrEditDialog();
        initDeleteDialog();
        if (keyType == KeyType.API_KEY)
            addOrEditKeyDialog.setVisible(false);
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
        addOrEditDialogWebsiteFieldLabel = getLabel("Website: ");
        addOrEditDialogIdFieldLabel = getLabel("ID: ");
        addOrEditDialogValueFieldLabel = getLabel("Value: ");
        jPanel.add(getFieldPanel(addOrEditDialogWebsiteFieldLabel, addOrEditDialogWebsiteField), gbc);
        gbc.gridy++;
        jPanel.add(getFieldPanel(addOrEditDialogIdFieldLabel, addOrEditDialogIdField), gbc);
        gbc.gridy++;
        jPanel.add(getFieldPanel(addOrEditDialogValueFieldLabel, addOrEditDialogValueField), gbc);
        gbc.gridy++;
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 50));
        buttonContainer.setOpaque(false);
        JButton no = getButton("Cancel", CONFIRM_BUTTON_BACKGROUND, CONFIRM_BUTTON_FOREGROUND, CONFIRM_BUTTON_FONT, _ -> addOrEditKeyDialog.setVisible(false));
        JButton yes = getButton("Save", CANCEL_BUTTON_BACKGROUND, CANCEL_BUTTON_FOREGROUND, CANCEL_BUTTON_FONT, _ -> addOrEditConfirmButtonPressed());
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
        JButton no = getButton("Cancel", CANCEL_BUTTON_BACKGROUND, CANCEL_BUTTON_FOREGROUND, CANCEL_BUTTON_FONT, _ -> deleteKeyDialog.setVisible(false));
        JButton yes = getButton("Delete", CONFIRM_BUTTON_BACKGROUND, CONFIRM_BUTTON_FOREGROUND, CONFIRM_BUTTON_FONT, _ -> deleteConfirmButtonPressed());
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
            jPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        IO.println("Enter");
                        addPopupMenu.show(jPanel, e.getX(), e.getY());
                        IO.println("Enter0");
                    }
                }
            });
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


    private void addOrEditConfirmButtonPressed() {
        Pair pair = new Pair(addOrEditDialogWebsiteField.getText(), addOrEditDialogIdField.getText());
        String value = addOrEditDialogValueField.getText();
    }

    private void deleteConfirmButtonPressed() {
    }

    private void displayAddKeyDialog() {
        addOrEditDialogWebsiteField.setText("");
        addOrEditDialogWebsiteField.setEditable(true);
        addOrEditDialogIdField.setText("");
        addOrEditDialogIdField.setEditable(true);
        addOrEditDialogValueField.setText("");
        addMode = true;
        addOrEditKeyDialog.setTitle("Add Key");
        addOrEditKeyDialog.setVisible(true);
    }

    private void displayEditKeyDialog() {
        Pair pair = currentKeyView.getPair();
        addOrEditDialogWebsiteField.setText(pair.websiteName());
        addOrEditDialogWebsiteField.setEditable(false);
        addOrEditDialogIdField.setText(pair.id());
        addOrEditDialogIdField.setEditable(false);
        addOrEditDialogValueField.setText(keyManagerListener.getKey(pair, keyType));
        addMode = false;
        addOrEditKeyDialog.setTitle("Edit Key");
        addOrEditKeyDialog.setVisible(true);
    }

    private void displayDeleteKeyDialog() {
        Pair pair = currentKeyView.getPair();
        deleteDialogWebsiteField.setText(pair.websiteName());
        deleteDialogIdField.setText(pair.id());
        deleteKeyDialog.setVisible(true);
    }

    @Override
    public String getValue(Pair pair) {
        return "Hello World!!!!";
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
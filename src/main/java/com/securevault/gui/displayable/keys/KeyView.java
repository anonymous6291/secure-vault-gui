package com.securevault.gui.displayable.keys;

import com.securevault.gui.displayable.keys.listeners.KeyViewListener;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.securevault.gui.displayable.Constants.*;

public class KeyView extends MouseAdapter {
    private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();
    private static final String HIDDEN_PASSWORD_TEXT = ("" + PASSWORD_ECHO_CHAR).repeat(6);
    private final WebsiteIdPair websiteIdPair;
    private final KeyViewListener keyViewListener;
    private final JPanel view;
    private final JTextField password;
    private final JButton showButton;
    private volatile boolean isPasswordVisible;

    KeyView(WebsiteIdPair websiteIdPair, KeyViewListener keyViewListener) {
        this.websiteIdPair = websiteIdPair;
        this.keyViewListener = keyViewListener;
        view = new JPanel(new GridLayout(0, 1));
        view.setBorder(KEY_VIEW_BORDER);
        view.setPreferredSize(new Dimension(KEY_VIEW_WIDTH, KEY_VIEW_HEIGHT));
        view.setBackground(KEY_VIEW_BACKGROUND);
        view.addMouseListener(this);
        removeBorder();
        view.add(getLabel("Website:   " + websiteIdPair.websiteName()));
        view.add(getLabel("ID:   " + websiteIdPair.id()));
        JPanel passwordViewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        passwordViewPanel.setOpaque(false);
        password = new JTextField(HIDDEN_PASSWORD_TEXT, 15);
        password.setBackground(PASSWORD_VIEW_FIELD_BACKGROUND);
        password.setForeground(PASSWORD_VIEW_FIELD_FOREGROUND);
        password.setFont(PASSWORD_VIEW_FIELD_FONT);
        password.setPreferredSize(new Dimension(50, 30));
        password.setEditable(false);
        showButton = getButton("Show", SHOW_PASSWORD_BUTTON_BACKGROUND, SHOW_PASSWORD_BUTTON_FOREGROUND, SHOW_PASSWORD_BUTTON_FONT, _ -> showPasswordButtonPressed());
        passwordViewPanel.add(password);
        passwordViewPanel.add(showButton);
        passwordViewPanel.add(getButton("Copy", COPY_BUTTON_BACKGROUND, COPY_BUTTON_FOREGROUND, COPY_BUTTON_FONT, _ -> copyButtonPressed()));
        view.add(passwordViewPanel);
    }

    private JLabel getLabel(String text) {
        JLabel jLabel = new JLabel(text);
        jLabel.setForeground(KEY_VIEW_FOREGROUND);
        jLabel.setFont(KEY_LABEL_FONT);
        jLabel.setVerticalTextPosition(JLabel.CENTER);
        jLabel.setHorizontalTextPosition(JLabel.CENTER);
        jLabel.setVerticalAlignment(JLabel.CENTER);
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        return jLabel;
    }

    private JButton getButton(String text, Color bg, Color fg, Font font, ActionListener actionListener) {
        JButton jButton = new JButton(text);
        jButton.addActionListener(actionListener);
        jButton.setBackground(bg);
        jButton.setForeground(fg);
        jButton.setFont(font);
        jButton.setFocusPainted(false);
        jButton.setPreferredSize(new Dimension(70, 30));
        return jButton;
    }

    public JPanel getView() {
        return view;
    }

    public WebsiteIdPair getPair() {
        return websiteIdPair;
    }

    private void showPasswordButtonPressed() {
        if (isPasswordVisible) {
            showButton.setText("Show");
            password.setText(HIDDEN_PASSWORD_TEXT);
        } else {
            showButton.setText("Hide");
            password.setText(keyViewListener.getValue(websiteIdPair));
        }
        isPasswordVisible = !isPasswordVisible;
    }

    private void copyButtonPressed() {
        try {
            TOOLKIT.getSystemClipboard().setContents(new StringSelection(keyViewListener.getValue(websiteIdPair)), null);
        } catch (Exception _) {
        }
    }

    public void setBorder() {
        view.setBorder(SELECTED_KEY_BORDER);
    }

    public void removeBorder() {
        view.setBorder(UNSELECTED_KEY_BORDER);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        keyViewListener.clicked(e, this);
    }
}

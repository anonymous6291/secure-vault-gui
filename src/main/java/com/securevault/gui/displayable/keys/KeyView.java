package com.securevault.gui.displayable.keys;

import com.securevault.gui.displayable.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class KeyView {
    private static final Random random = new Random();
    private final String name;
    private final JPanel view;

    KeyView(String name, int width) {
        this.name = name;
        view = new JPanel(new BorderLayout());
        JLabel jLabel = new JLabel(name);
        jLabel.setForeground(Color.BLACK);
        jLabel.setFont(Constants.KEY_LABEL_FONT);
        jLabel.setVerticalTextPosition(JLabel.CENTER);
        jLabel.setHorizontalTextPosition(JLabel.CENTER);
        jLabel.setVerticalAlignment(JLabel.CENTER);
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        view.add(jLabel, BorderLayout.CENTER);
        view.setBorder(Constants.KEY_VIEW_BORDER);
        view.setPreferredSize(new Dimension(width,Constants.KEY_VIEW_HEIGHT));
        view.setBackground(Constants.KEY_VIEW_BACKGROUND);
    }

    public JPanel getView() {
        return view;
    }

    private int nextValue() {
        return random.nextInt(256);
    }

    private Color getRandomBackgroundColor() {
        return new Color(nextValue(), nextValue(), nextValue(), 200).brighter();
    }
}

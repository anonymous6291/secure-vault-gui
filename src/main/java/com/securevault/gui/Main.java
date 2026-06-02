package com.securevault.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public class Main {
    static void main() {
        File image = new File("/home/anonymous/Pictures/Background/Screenshot_20250531_153330_Instagram.jpg");
        IO.println("Hello and welcome!");
        JFrame jFrame = new JFrame("Hello");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int windowWidth = toolkit.getScreenSize().width;
        int windowHeight = toolkit.getScreenSize().height;
        int width = Constants.WIDTH;
        int height = Constants.HEIGHT;
        new ImageIcon();
        jFrame.setSize(width, height);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);
        jFrame.setLocation((windowWidth - width) >> 1, (windowHeight - height) >> 1);
        JPanel background = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paintComponent(g);
                g.drawImage(new ImageIcon(image.toString()).getImage(), 0, 0, this);
            }
        };
        background.setBackground(Color.CYAN);
        jFrame.setContentPane(background);
        jFrame.setVisible(true);
        DirectoryManager directoryManager = new DirectoryManager(Path.of(""), null, new DirectoryManagerListener() {
            @Override
            public void displayDirectory(JPanel jPanel) {
                jFrame.setGlassPane(jPanel);
                jPanel.setVisible(true);
                jFrame.repaint();
                jFrame.validate();
            }

            @Override
            public Dimension getDisplayDimension() {
                return new Dimension(width - 1, height);
            }

            @Override
            public void refreshUI() {

            }

            @Override
            public void addFile(Path from, Path to) {

            }

            @Override
            public void getFile(Path from, Path to) {

            }

            @Override
            public void deleteFile(Path from) {

            }
        });
        String[] files = new String[]{"A", "B", "C"};
        directoryManager.addFile(files, 0, files.length);
        files = new String[]{"F", "B", "C"};
        directoryManager.addFile(files, 0, files.length);
        files = new String[]{"B"};
        directoryManager.addFile(files, 0, files.length);
        for (int i = 0; i < 200; i++) {
            String[] d = new String[]{i+".java"};
            directoryManager.addFile(d, 0, d.length);
        }
        directoryManager.display();
        //JPanel glass = getPanel();
        //jFrame.setGlassPane(glass);
        //glass.setVisible(true);
    }

    private static JPanel getPanel() {
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jPanel.setOpaque(false);
        jPanel.setBackground(Color.GREEN);
        for (int i = 0; i < 50; i++) {
            jPanel.add(getButton(i));
        }
        jPanel.setVisible(true);
        return jPanel;
    }

    private static JButton getButton(int i) {
        JButton jButton = new JButton("[" + i + "]");
        jButton.setOpaque(false);
        jButton.setBackground(Color.BLUE);
        jButton.setPreferredSize(new Dimension(80, 50));
        return jButton;
    }
}

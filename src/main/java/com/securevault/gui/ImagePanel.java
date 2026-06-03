package com.securevault.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImagePanel extends JPanel {
    private final BufferedImage bufferedImage;
    private final int width;
    private final int height;

    ImagePanel(URL imageURL, int width, int height) {
        BufferedImage bufferedImage = null;
        try {
            if (imageURL != null) {
                bufferedImage = ImageIO.read(imageURL);
            }
        } catch (IOException e) {
            IO.println(e);
        }
        this.bufferedImage = bufferedImage;
        this.width = width;
        this.height = height;
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        if (bufferedImage != null) {
            graphics.drawImage(bufferedImage, 0, 0, width, height, this);
        }
    }
}

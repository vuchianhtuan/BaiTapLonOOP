package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.objects.Paddle;

import java.awt.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.engine.AssetManager;

public class StickyPaddlePowerUp extends PowerUp {
    private String imageName = "stickyPowerUp";
    public StickyPaddlePowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "sticky", 600);
    }

    @Override public void applyEffect(Paddle paddle) {
        paddle.setSticky(true);
    }
    @Override public void removeEffect(Paddle paddle) {
        paddle.setSticky(false);
    }
    @Override public void update() {
        this.y += fallSpeed;
    }
    @Override public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        }
    }
}

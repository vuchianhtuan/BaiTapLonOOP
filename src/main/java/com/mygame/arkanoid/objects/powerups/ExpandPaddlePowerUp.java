package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Paddle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class ExpandPaddlePowerUp extends PowerUp {
    private String imageName;
    private static final int EXPAND_AMOUNT = 50;
    @Override public void update() {
        this.y += fallSpeed;
    }

    public ExpandPaddlePowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "expand", 600);
        this.imageName = "expandPowerUp";
    }

    @Override
    public void applyEffect(Paddle paddle) {
        paddle.resetWidth();
        paddle.expand(EXPAND_AMOUNT);
    }

    @Override
    public void removeEffect(Paddle paddle) {
        paddle.resetWidth();
    }

    @Override
    public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(this.x, this.y, this.width, this.height);
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}

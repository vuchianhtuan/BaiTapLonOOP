package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;


public class FastBallPowerUp extends PowerUp {
    private String imageName = "fastBallPowerUp";
    private static final double FAST_SPEED_FACTOR = 1.5; // Tăng 50

    public FastBallPowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "fast_ball", 300);
    }

    @Override public void applyEffect(GameManager gameManager) {
        double currentSpeed = gameManager.getBall().getSpeed();
        gameManager.getBall().setSpeed(currentSpeed * FAST_SPEED_FACTOR);
    }
    @Override public void removeEffect(GameManager gameManager) {
        gameManager.getBall().resetSpeed();
    }
    @Override public void update() {
        this.y += fallSpeed;
    }
    @Override public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        } else {
            g.setColor(Color.ORANGE);
            g.fillRect(this.x, this.y, this.width, this.height);
        }
    }

}
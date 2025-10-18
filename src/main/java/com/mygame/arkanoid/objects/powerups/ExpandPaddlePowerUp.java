package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.systems.ScalingManager;

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
    public void applyEffect(GameManager gameManager) {
        gameManager.getPaddle().resetWidth();
        gameManager.getPaddle().expand(EXPAND_AMOUNT);
    }

    @Override
    public void removeEffect(GameManager gameManager) {
        gameManager.getPaddle().resetWidth();
    }

    @Override
    public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}

package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.systems.ScalingManager;

/**
 * Lớp đại diện cho vật phẩm tăng sức mạnh thêm mạng sống (extra life).
 */
public class ExtraLifePowerUp extends PowerUp {
    private String imageName = "extraLifePowerUp";

    public ExtraLifePowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "extra_life", 300);
    }

    /**
     * ÁP DỤNG HIỆU ỨNG: Thêm 1 mạng cho người chơi.
     * @param gameManager
     */
    @Override public void applyEffect(GameManager gameManager) {
        int pre_lives = gameManager.getLives();
        gameManager.setLives(pre_lives + 1);
    }
    @Override public void removeEffect(GameManager gameManager) {}
    @Override public void update() {
        this.y += fallSpeed;
    }
    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}

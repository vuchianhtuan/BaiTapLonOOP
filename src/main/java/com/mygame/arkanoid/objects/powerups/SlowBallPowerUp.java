package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;
import java.awt.image.BufferedImage;

// POWER-UP GIẢM TỐC ĐỘ BÓNG
public class SlowBallPowerUp extends PowerUp {
    @Override public void update() {
        this.y += fallSpeed;
    }
    
    private String imageName = "slowBallPowerUp";
    private static final double SLOW_SPEED_FACTOR = 0.5; // Giảm 50% tốc độ

    public SlowBallPowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "slow_ball", 300);
    }

    /**
     * ÁP DỤNG HIỆU ỨNG: Giảm tốc độ bóng.
     * @param gameManager
     */
    @Override
    public void applyEffect(GameManager gameManager) {
        double originalSpeed = gameManager.getBall().getOriginalSpeed();
        gameManager.getBall().setSpeed(originalSpeed * SLOW_SPEED_FACTOR);
    }

    @Override
    public void removeEffect(GameManager gameManager) {
        // Khôi phục tốc độ gốc
        gameManager.getBall().resetSpeed();
    }

    @Override
    public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            g.setColor(Color.CYAN);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}

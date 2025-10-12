package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Ball;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MultiBallPowerUp extends PowerUp {
    private String imageName = "multiBallPowerUp";
    public static final int BALL_COUNT = 3; // Số lượng bóng thêm vào

    public MultiBallPowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "multi_ball", 50); // duration = 0 vì hiệu ứng là tức thì
    }

    @Override public void applyEffect(GameManager gameManager) {
        Ball mainBall = gameManager.getBall();
        int x = mainBall.getX();
        int y = mainBall.getY();
        for (int i = 0; i < BALL_COUNT; i++) {
            Ball b = new Ball(x, y, 15, 15);
            b.setStuckToPaddle(false);

            if (i == 0) {
                b.setDx(1);  b.setDy(-1);
            } else if (i == 1) {
                b.setDx(-1); b.setDy(1);
            } else {
                b.setDx(1);  b.setDy(1);
            }
            gameManager.addball(b);
        }
    }
    @Override public void removeEffect(GameManager gameManager) {}
    @Override public void update() {
        this.y += fallSpeed;
    }
    @Override public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        } else {
            g.setColor(Color.MAGENTA);
            g.fillRect(this.x, this.y, this.width, this.height);
        }
    }
}
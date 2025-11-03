package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Ball;

import java.awt.*;
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.systems.ScalingManager;

/**
 * Lớp đại diện cho vật phẩm tăng sức mạnh tạo thêm nhiều bóng.
 */
public class MultiBallPowerUp extends PowerUp {
    private String imageName = "multiBallPowerUp";
    public static final int BALL_COUNT = 3;

    // THÊM: Biến góc xoay và tốc độ xoay
    private double rotationAngle = 0;
    private final double ROTATION_SPEED = 4.5;

    public MultiBallPowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "multi_ball", 50);
    }

    /**
     * ÁP DỤNG HIỆU ỨNG: Tạo thêm nhiều bóng từ vị trí bóng chính.
     * @param gameManager
     */
    @Override public void applyEffect(GameManager gameManager) {
        Ball mainBall = gameManager.getBall();
        int x = mainBall.getX();
        int y = mainBall.getY();
        for (int i = 0; i < BALL_COUNT; i++) {
            Ball b = new Ball(x, y, 18, 18, gameManager.getSelectedBallSkinKey());
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

        // CẬP NHẬT GÓC XOAY
        this.rotationAngle += ROTATION_SPEED;
        if (this.rotationAngle >= 360) {
            this.rotationAngle -= 360;
        }
    }

    @Override public void render(Graphics g, ScalingManager sm) {
        // SỬ DỤNG GRAPHICS2D ĐỂ ÁP DỤNG XOAY
        Graphics2D g2d = (Graphics2D) g.create();

        int scaledX = sm.scaleX(this.x);
        int scaledY = sm.scaleY(this.y);
        int scaledWidth = sm.scaleWidth(this.width);
        int scaledHeight = sm.scaleHeight(this.height);

        double centerX = scaledX + scaledWidth / 2.0;
        double centerY = scaledY + scaledHeight / 2.0;

        // Xoay quanh tâm
        double rotateRadian = Math.toRadians(this.rotationAngle);
        g2d.rotate(rotateRadian, centerX, centerY);

        // --- Logic vẽ ---
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g2d.drawImage(img, scaledX, scaledY, scaledWidth, scaledHeight, null);
        } else {
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
        }

        g2d.dispose(); // Khôi phục trạng thái Graphics
    }
}
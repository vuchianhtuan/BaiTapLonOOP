package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.core.GameManager;
import java.awt.Graphics;
import java.awt.Graphics2D; // <-- Cần import Graphics2D
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.helper.ScalingManager;

public class StickyPaddlePowerUp extends PowerUp {
    private String imageName = "stickyPowerUp";

    // THÊM: Biến góc xoay và tốc độ xoay
    private double rotationAngle = 0;
    private final double ROTATION_SPEED = 5.0; // Xoay 5 độ mỗi lần cập nhật

    public StickyPaddlePowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "sticky", 600);
    }

    @Override public void applyEffect(GameManager gameManager) {
        gameManager.getPaddle().setSticky(true);
    }

    @Override public void removeEffect(GameManager gameManager) {
        gameManager.getPaddle().setSticky(false);
    }

    @Override public void update() {
        this.y += fallSpeed;

        // CẬP NHẬT GÓC XOAY
        this.rotationAngle += ROTATION_SPEED;
        if (this.rotationAngle >= 360) {
            this.rotationAngle -= 360;
        }
    }

    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);

        if (img != null) {
            // SỬ DỤNG GRAPHICS2D ĐỂ ÁP DỤNG XOAY
            Graphics2D g2d = (Graphics2D) g.create(); // Tạo bản sao

            int scaledX = sm.scaleX(this.x);
            int scaledY = sm.scaleY(this.y);
            int scaledWidth = sm.scaleWidth(this.width);
            int scaledHeight = sm.scaleHeight(this.height);

            double centerX = scaledX + scaledWidth / 2.0;
            double centerY = scaledY + scaledHeight / 2.0;

            // Xoay quanh tâm
            double rotateRadian = Math.toRadians(this.rotationAngle);
            g2d.rotate(rotateRadian, centerX, centerY);

            // Vẽ ảnh đã được xoay
            g2d.drawImage(img, scaledX, scaledY, scaledWidth, scaledHeight, null);

            g2d.dispose(); // Khôi phục trạng thái Graphics
        }
    }
}
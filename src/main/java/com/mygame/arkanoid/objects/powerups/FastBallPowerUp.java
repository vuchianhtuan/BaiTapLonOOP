package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Ball;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;
import java.awt.image.BufferedImage;


public class FastBallPowerUp extends PowerUp {
    private String imageName = "fastBallPowerUp";
    private static final double FAST_SPEED_FACTOR = 1.5; // Tăng 50
    private static final int TRAIL_SEGMENTS = 9;
    // Khoảng cách giữa các vệt (tùy chỉnh để hiệu ứng đẹp nhất)
    private static final int TRAIL_SPACING = 2;

    public FastBallPowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "fast_ball", 300);
    }

    @Override public void applyEffect(GameManager gameManager) {
        Ball targetBall = gameManager.getBall();
        double currentSpeed = gameManager.getBall().getSpeed();
        gameManager.getBall().setSpeed(currentSpeed * FAST_SPEED_FACTOR);
        targetBall.setBurning(true);
    }
    @Override public void removeEffect(GameManager gameManager) {
        Ball targetBall = gameManager.getBall();
        gameManager.getBall().resetSpeed();
        targetBall.setBurning(false);
    }
    @Override public void update() {
        this.y += fallSpeed;
    }


    @Override
    public void render(Graphics g, ScalingManager sm) {

        Graphics2D g2d = (Graphics2D) g;

        int scaledX = sm.scaleX(this.x);
        int scaledY = sm.scaleY(this.y);
        int scaledWidth = sm.scaleWidth(this.width);
        int scaledHeight = sm.scaleHeight(this.height);

        // --- BẮT ĐẦU HIỆU ỨNG THÊM SINH ĐỘNG: VỆT SÁNG ---

        // 1. Vẽ các vệt sáng
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        Color baseColor = Color.ORANGE;

        for (int i = 1; i <= TRAIL_SEGMENTS; i++) {
            // Tính độ trong suốt giảm dần (vệt càng xa càng mờ)
            float alphaFactor = 1.0f - (i / (float) TRAIL_SEGMENTS); // Giảm từ 1.0 xuống gần 0

            // Tính toán vị trí Y của vệt (phía trên vật phẩm)
            int trailY = sm.scaleY(this.y - (i * TRAIL_SPACING));

            // Thiết lập độ trong suốt
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaFactor * 0.4f)); // nhân 0.4f để các vệt không quá đậm

            // Vẽ vệt
            if (img != null) {
                // Vẽ ảnh mờ
                g2d.drawImage(img, scaledX, trailY, scaledWidth, scaledHeight, null);
            } else {
                // Vẽ hình chữ nhật màu cam mờ
                g2d.setColor(baseColor);
                g2d.fillRect(scaledX, trailY, scaledWidth, scaledHeight);
            }
        }

        // 2. Phục hồi độ trong suốt về mặc định (RẤT QUAN TRỌNG)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // --- KẾT THÚC VỆT SÁNG ---

        // 3. Vẽ vật phẩm chính (không mờ)
        if (img != null) {
            g2d.drawImage(img, scaledX, scaledY, scaledWidth, scaledHeight, null);
        } else {
            g2d.setColor(Color.ORANGE);
            g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
        }
    }
}
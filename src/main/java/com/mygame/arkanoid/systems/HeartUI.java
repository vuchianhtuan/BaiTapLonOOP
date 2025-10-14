package com.mygame.arkanoid.systems;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import java.awt.Graphics; // CHỈ DÙNG Graphics
import java.awt.image.BufferedImage;

public class HeartUI {
    private GameManager gameManager;
    private String imageName;
    private int lastKnownLives;
    private static final int HEART_SIZE = 24;

    // Biến cho hiệu ứng nhấp nháy thông thường
    private int blinkTimer = 0;

    // Biến cho hiệu ứng biến mất
    private int disappearingHeartIndex = -1; // Vị trí của tim đang biến mất
    private int disappearEffectTimer = 0; // Bộ đếm thời gian cho hiệu ứng

    public HeartUI(GameManager gm) {
        this.gameManager = gm;
        this.lastKnownLives = gm.getLives();
        this.imageName = "heart";
    }

    public void update() {
        blinkTimer = (blinkTimer + 1) % 60; // Lặp lại mỗi giây

        // Kiểm tra nếu mất mạng
        int currentLives = gameManager.getLives();
        if (currentLives < lastKnownLives) {
            disappearingHeartIndex = lastKnownLives - 1; // Đánh dấu trái tim vừa mất
            disappearEffectTimer = 60; // Bắt đầu đếm ngược hiệu ứng (60 frame = 1 giây)
            lastKnownLives = currentLives;
        }

        // Cập nhật bộ đếm của hiệu ứng biến mất
        if (disappearEffectTimer > 0) {
            disappearEffectTimer--;
            if (disappearEffectTimer <= 0) {
                disappearingHeartIndex = -1; // Kết thúc hiệu ứng
            }
        }
    }

    public void draw(Graphics g) {
        int lives = gameManager.getLives();
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);

        // Lặp qua số mạng tối đa để vẽ đúng hiệu ứng
        for (int i = 0; i < gameManager.getLives(); i++) {
            int x = 10 + (i * 30);
            int y = 10;

            if (i == disappearingHeartIndex) {
                if (disappearEffectTimer > 0 && (disappearEffectTimer / 5) % 2 == 0) {
                    g.drawImage(img, x, y, HEART_SIZE, HEART_SIZE, null);
                }
            }
            else if (i < lives) {
                // Nhấp nháy chậm cho trái tim cuối cùng
                if (i == lives - 1 && blinkTimer < 30) {
                    // Trong nửa chu kỳ nháy, không vẽ
                } else {
                    g.drawImage(img, x, y, HEART_SIZE, HEART_SIZE, null);
                }
            }
        }
    }
}
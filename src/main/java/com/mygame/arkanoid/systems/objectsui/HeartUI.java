package com.mygame.arkanoid.systems.objectsui;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.helper.ScalingManager;

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
        // Lấy instance của ScalingManager
        ScalingManager sm = ScalingManager.getInstance();

        int lives = gameManager.getLives();
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img == null) return;

        // Lặp qua số mạng để vẽ
        for (int i = 0; i < gameManager.getLives(); i++) {
            int logicBaseX = 980; // 960 (game) + 20 (padding)
            int logicBaseY = 230; // Vị trí Y (bạn có thể điều chỉnh)
            int heartPadding = 5;

            int logicX = logicBaseX + (i * (HEART_SIZE + heartPadding));
            int logicY = logicBaseY;

            // Chỉ vẽ nếu hiệu ứng cho phép
            boolean shouldDraw = true;
            if (i == disappearingHeartIndex) {
                if (disappearEffectTimer <= 0 || (disappearEffectTimer / 5) % 2 != 0) {
                    shouldDraw = false;
                }
            } else if (i < lives) {
                if (i == lives - 1 && blinkTimer < 30) {
                    shouldDraw = false;
                }
            }

            if (shouldDraw) {
                // Sử dụng ScalingManager để "dịch" tọa độ và kích thước logic ra màn hình thật
                g.drawImage(img,
                        sm.scaleX(logicX),
                        sm.scaleY(logicY),
                        sm.scaleWidth(HEART_SIZE),
                        sm.scaleHeight(HEART_SIZE),
                        null);
            }
        }
    }
}
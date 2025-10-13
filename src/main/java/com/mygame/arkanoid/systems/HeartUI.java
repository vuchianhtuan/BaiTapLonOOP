package com.mygame.arkanoid.systems;

// File: UI.java (trong package systems.ui)
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class HeartUI {
    private GameManager gameManager;
    private String imageName;
    private int lastKnownLives;

    // Biến quản lý animation, thay thế cho class HeartUI
    private int blinkTimer = 0;
    private int fadingHeartIndex = -1; // Vị trí của trái tim đang mờ dần
    private float fadingAlpha = 1.0f;  // Độ trong suốt của trái tim đang mờ

    public HeartUI(GameManager gm) {
        this.gameManager = gm;
        this.lastKnownLives = gm.getLives();
        this.imageName = "heart";
        // Tải ảnh trái tim tại đây
        // heartImage = ImageIO.read(...);
    }

    public void update() {
        // --- Cập nhật logic nhấp nháy ---
        blinkTimer = (blinkTimer + 1) % 60; // Bộ đếm lặp lại từ 0-59 (1 giây ở 60FPS)

        // --- Cập nhật logic mất mạng và mờ dần ---
        int currentLives = gameManager.getLives();
        if (currentLives < lastKnownLives) {
            fadingHeartIndex = lastKnownLives - 1; // Đánh dấu trái tim vừa mất
            fadingAlpha = 1.0f; // Bắt đầu mờ
            lastKnownLives = currentLives;
        }

        // Nếu có trái tim đang mờ, giảm độ trong suốt của nó
        if (fadingHeartIndex != -1) {
            fadingAlpha -= 0.05f;
            if (fadingAlpha <= 0) {
                fadingHeartIndex = -1; // Kết thúc hiệu ứng mờ
            }
        }
    }

    public void draw(Graphics2D g2d) {
        int lives = gameManager.getLives();
        final int HEART_SIZE = 24;
        // Vẽ tất cả các trái tim
        for (int i = 0; i < gameManager.getLives(); i++) {
            // Vị trí vẽ trái tim
            int x = 10 + (i * 30);
            int y = 10;

            // --- Logic vẽ hiệu ứng ---
            BufferedImage img = AssetManager.getInstance().getImage(this.imageName);

            // 1. Nếu đây là trái tim đang mờ dần
            if (i == fadingHeartIndex) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadingAlpha));
                g2d.drawImage(img, x, y, HEART_SIZE, HEART_SIZE, null);
                // Reset lại để không ảnh hưởng các hình khác
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            // 2. Nếu đây là trái tim còn lại (chưa bị mất)
            else if (i < lives) {
                // Chỉ nhấp nháy trái tim ngoài cùng
                if (i == lives - 1 && blinkTimer < 30) {
                    // Trong nửa chu kỳ nháy, không vẽ gì cả
                } else {
                    g2d.drawImage(img, x, y, HEART_SIZE, HEART_SIZE, null);
                }
            }
        }
    }
}
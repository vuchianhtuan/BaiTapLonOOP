package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.engine.AssetManager;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class GameOverScreen {
    private BufferedImage[] frames; // Mảng chứa các frame ảnh của animation
    private int currentFrameIndex;
    private int x, y;

    private int displayWidth;
    private int displayHeight;

    private final int totalFrames = 3;

    private int frameCounter;           // Bộ đếm số lần hàm update() được gọi
    private int framesPerAnimation;     // Số khung hình cần chờ trước khi chuyển ảnh

    public GameOverScreen(int x, int y, int displayWidth, int displayHeight) {
        this.x = x;
        this.y = y;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.framesPerAnimation = 30;
        loadFrames();
        reset();
    }

    private void loadFrames() {
        this.frames = new BufferedImage[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            int imageNumber = i + 1;
            String assetKey = "gameover" + imageNumber;
            frames[i] = AssetManager.getInstance().getImage(assetKey);
        }
    }

    public void update() {
        frameCounter++;

        if (frameCounter >= framesPerAnimation) {
            // Chuyển sang ảnh tiếp theo
            currentFrameIndex++;

            if (currentFrameIndex >= totalFrames) {
                currentFrameIndex = 0;
            }
            frameCounter = 0;
        }
    }

    public void draw(Graphics g) {
        if (frames != null && frames[currentFrameIndex] != null) {
            g.drawImage(frames[currentFrameIndex], x, y, displayWidth, displayHeight, null);
        }
    }

    // --- HÀM RESET CŨNG ĐƯỢC CẬP NHẬT ---
    public void reset() {
        this.currentFrameIndex = 0;
        this.frameCounter = 0; // Đặt lại bộ đếm khi reset
    }
}
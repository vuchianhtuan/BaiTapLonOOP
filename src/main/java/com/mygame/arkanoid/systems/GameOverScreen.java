package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.engine.AssetManager;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class GameOverScreen {
    private BufferedImage[] frames;
    private int currentFrameIndex;

    // Bây giờ các biến này sẽ lưu trữ tọa độ và kích thước LOGIC
    private int logicX, logicY;
    private int logicWidth, logicHeight;

    private final int totalFrames = 3;
    private int frameCounter;
    private int framesPerAnimation;

    public GameOverScreen(int logicX, int logicY, int logicWidth, int logicHeight) {
        // Constructor bây giờ nhận và lưu các giá trị LOGIC
        this.logicX = logicX;
        this.logicY = logicY;
        this.logicWidth = logicWidth;
        this.logicHeight = logicHeight;
        this.framesPerAnimation = 30;
        loadFrames();
        reset();
    }

    // ... loadFrames(), update(), reset() giữ nguyên ...
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
            currentFrameIndex = (currentFrameIndex + 1) % totalFrames;
            frameCounter = 0;
        }
    }

    public void reset() {
        this.currentFrameIndex = 0;
        this.frameCounter = 0;
    }


    public void draw(Graphics g) {
        if (frames != null && frames[currentFrameIndex] != null) {
            ScalingManager sm = ScalingManager.getInstance();

            // Vẽ ảnh ra màn hình với tọa độ và kích thước đã được scale
            g.drawImage(frames[currentFrameIndex],
                    sm.scaleX(this.logicX),
                    sm.scaleY(this.logicY),
                    sm.scaleWidth(this.logicWidth),
                    sm.scaleHeight(this.logicHeight),
                    null);
        }
    }
}
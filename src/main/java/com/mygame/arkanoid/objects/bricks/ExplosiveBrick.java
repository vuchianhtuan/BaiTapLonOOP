// File: com/mygame/arkanoid/objects/bricks/ExplosiveBrick.java

package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.engine.AssetManager;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ExplosiveBrick extends Brick {

    private enum State {
        ALIVE,
        EXPLODING,
        FINISHED
    }

    private State currentState;
    private ExplosionEffect explosionEffect;

    // Biến static để lưu các frame, đảm bảo chỉ load 1 lần
    private static BufferedImage[] explosionFrames;
    private static final int FRAME_COUNT = 8; // Tổng số frame ảnh

    public ExplosiveBrick(int x, int y, int width, int height) {
        super(x, y, width, height, 1, "Explosive", "explosiveBrick");
        this.currentState = State.ALIVE;

        // Chỉ load ảnh nếu chưa được load trước đó
        if (explosionFrames == null) {
            loadExplosionFrames();
        }
    }

    // THAY ĐỔI: Phương thức này giờ đây sẽ tải 8 ảnh riêng lẻ
    private static void loadExplosionFrames() {
        explosionFrames = new BufferedImage[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            // Tạo tên ảnh theo thứ tự: "explosion_render1", "explosion_render2", ...
            String imageName = "explosion_render" + (i + 1);

            // Lấy ảnh đã được load sẵn từ AssetManager
            explosionFrames[i] = AssetManager.getInstance().getImage(imageName);

            if (explosionFrames[i] == null) {
                System.err.println("Explosion frame '" + imageName + "' not loaded!");
                // Thoát khỏi vòng lặp nếu có lỗi để tránh NullPointerException
                break;
            }
        }
    }

    @Override
    public void takeHit() {
        if (currentState == State.ALIVE) {
            super.takeHit();
            if (super.isDestroyed()) {
                this.currentState = State.EXPLODING;
                int centerX = this.x + this.width / 2;
                int centerY = this.y + this.height / 2;
                this.explosionEffect = new ExplosionEffect(centerX, centerY);
            }
        }
    }

    @Override
    public boolean isDestroyed() {
        return currentState != State.ALIVE;
    }

    public boolean isFinished() {
        return currentState == State.FINISHED;
    }

    @Override
    public void update() {
        if (currentState == State.EXPLODING) {
            explosionEffect.update();
            if (explosionEffect.isFinished()) {
                currentState = State.FINISHED;
            }
        }
    }

    @Override
    public void render(Graphics g) {
        switch (currentState) {
            case ALIVE:
                BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
                if (img != null) {
                    g.drawImage(img, this.x, this.y, this.width, this.height, null);
                }
                break;
            case EXPLODING:
                explosionEffect.draw(g);
                break;
            case FINISHED:
                // Không vẽ gì
                break;
        }
    }

    private static class ExplosionEffect {
        private int x, y;
        private int currentFrameIndex;
        private boolean isFinished;
        private long lastFrameTime;
        private long frameDuration = 75;

        public ExplosionEffect(int x, int y) {
            this.x = x;
            this.y = y;
            this.currentFrameIndex = 0;
            this.isFinished = false;
            this.lastFrameTime = System.currentTimeMillis();
        }

        public void update() {
            if (isFinished || explosionFrames == null) return;
            if (System.currentTimeMillis() - lastFrameTime > frameDuration) {
                currentFrameIndex++;
                lastFrameTime = System.currentTimeMillis();
                if (currentFrameIndex >= FRAME_COUNT) { // Dùng hằng số FRAME_COUNT
                    isFinished = true;
                }
            }
        }

        public void draw(Graphics g) {
            if (!isFinished && explosionFrames != null && explosionFrames[currentFrameIndex] != null) {
                BufferedImage currentFrame = explosionFrames[currentFrameIndex];
                int frameWidth = currentFrame.getWidth() / 2;
                int frameHeight = currentFrame.getHeight() / 2;
                g.drawImage(currentFrame, x - frameWidth / 2, y - frameHeight / 2, frameWidth, frameHeight, null);
            }
        }

        public boolean isFinished() {
            return isFinished;
        }
    }
}
package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.effects.Shard;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ExplosiveBrick extends Brick {

    // 1. THÊM TRẠNG THÁI 'IGNITED'
    private enum State {
        ALIVE,      // Sống, bình thường
        IGNITED,    // Bị kích hoạt, đang đếm giờ chờ nổ
        EXPLODING,  // Đang chạy hoạt ảnh nổ
        FINISHED    // Nổ xong
    }

    private State currentState;
    private ExplosionEffect explosionEffect;
    private int ignitionTimer; // Bộ đếm giờ chờ (tính bằng số frame)

    private static BufferedImage[] explosionFrames;
    private static final int FRAME_COUNT = 8;

    public ExplosiveBrick(int x, int y, int width, int height) {
        super(x, y, width, height, 1, "Explosive", "explosiveBrick");
        this.currentState = State.ALIVE;

        if (explosionFrames == null) {
            loadExplosionFrames();
        }
    }

    private static void loadExplosionFrames() {
        explosionFrames = new BufferedImage[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            String imageName = "explosion_render" + (i + 1);
            explosionFrames[i] = AssetManager.getInstance().getImage(imageName);
            if (explosionFrames[i] == null) {
                System.err.println("Explosion frame '" + imageName + "' not loaded!");
                break;
            }
        }
    }

    // 2. TẠO HÀM HELPER ĐỂ TRÁNH TRÙNG LẶP CODE
    private void startExplosion() {
        this.currentState = State.EXPLODING;
        int centerX = this.x + this.width / 2;
        int centerY = this.y + this.height / 2;
        this.explosionEffect = new ExplosionEffect(centerX, centerY);
    }

    // 3. THÊM PHƯƠNG THỨC MỚI ĐỂ KÍCH HOẠT VỤ NỔ CHUỖI
    /**
     * Kích hoạt vụ nổ này sau một khoảng thời gian chờ (delay).
     * @param delayFrames Số frame phải chờ trước khi nổ.
     */
    public void ignite(int delayFrames) {
        // Chỉ kích hoạt nếu gạch còn sống
        if (this.currentState == State.ALIVE) {
            this.currentState = State.IGNITED;
            this.ignitionTimer = delayFrames;
        }
    }

    // Phương thức helper để GameManager kiểm tra
    public boolean isAlive() {
        return this.currentState == State.ALIVE;
    }


    public boolean isAwaitingDetonation() {
        return this.currentState == State.IGNITED;
    }

    // 4. CẬP NHẬT CÁC PHƯƠNG THỨC CŨ
    @Override
    public void takeHit() {
        if (currentState == State.ALIVE || currentState == State.IGNITED) {
            if (currentState == State.IGNITED) {
                this.ignitionTimer = 0;
            }

            super.takeHit(); // Đặt hitPoints = 0
            startExplosion(); // Nổ ngay
        }
        // Nếu đã EXPLODING hoặc FINISHED thì không làm gì
    }

    public boolean detonateOnHit(List<Shard> activeShards) {
        if (this.currentState == State.IGNITED) {

            // 1. TẠO VỠ VỤN TỨC THÌ (tương đương với bị phá hủy)
            activeShards.addAll(this.shatter());

            // 2. Kích hoạt vụ nổ (chuyển sang EXPLODING)
            this.takeHit();

            return true;
        }
        return false;
    }


    @Override
    public boolean isDestroyed() {
        // Gạch được coi là "hỏng" ngay khi nó không còn SỐNG và không còn ĐANG CHỜ NỔ
        return currentState != State.ALIVE && currentState != State.IGNITED;
    }

    public boolean isFinished() {
        return currentState == State.FINISHED;
    }

    @Override
    public void update() {
        switch (currentState) {
            case IGNITED:
                // Đếm ngược bộ đếm giờ
                ignitionTimer--;
                if (ignitionTimer <= 0) {
                    startExplosion(); // Hết giờ, bắt đầu nổ
                }
                break;
            case EXPLODING:
                // Cập nhật hoạt ảnh nổ
                explosionEffect.update();
                if (explosionEffect.isFinished()) {
                    currentState = State.FINISHED;
                }
                break;
            default:
                // ALIVE và FINISHED không cần làm gì
                break;
        }
    }

    @Override
    public void render(Graphics g, ScalingManager sm) {
        switch (currentState) {
            case ALIVE:
                // Vẽ gạch bình thường
                BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
                if (img != null) {
                    g.drawImage(img,
                            sm.scaleX(this.x), sm.scaleY(this.y),
                            sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
                }
                break;
            case IGNITED:
                // (Tùy chọn) Bạn có thể vẽ gạch nhấp nháy màu đỏ ở đây
                // Tạm thời, chúng ta vẫn vẽ gạch bình thường
                BufferedImage imgIgnited = AssetManager.getInstance().getImage(this.imageName);
                if (imgIgnited != null) {
                    g.drawImage(imgIgnited,
                            sm.scaleX(this.x), sm.scaleY(this.y),
                            sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
                }
                break;
            case EXPLODING:
                // Vẽ hiệu ứng nổ
                explosionEffect.draw(g, sm);
                break;
            case FINISHED:
                // Không vẽ gì
                break;
        }
    }

    // ==================================================================
    // 5. CLASS NỘI BỘ (KHÔNG THAY ĐỔI)
    // ==================================================================
    private static class ExplosionEffect {
        private int x, y;
        private int currentFrameIndex;
        private boolean isFinished;
        private long lastFrameTime;
        private long frameDuration = 55;

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
                if (currentFrameIndex >= FRAME_COUNT) {
                    isFinished = true;
                }
            }
        }

        public void draw(Graphics g, ScalingManager sm) {
            if (!isFinished && explosionFrames != null && explosionFrames[currentFrameIndex] != null) {
                BufferedImage currentFrame = explosionFrames[currentFrameIndex];
                int originalFrameWidth = currentFrame.getWidth();
                int originalFrameHeight = currentFrame.getHeight();

                // Lấy kích thước (ví dụ 1/2)
                int scaledWidth = sm.scaleWidth(originalFrameWidth) / 5;
                int scaledHeight = sm.scaleHeight(originalFrameHeight) / 5;

                // Lấy tâm
                int scaledCenterX = sm.scaleX(this.x);
                int scaledCenterY = sm.scaleY(this.y);

                // Căn giữa (Đã sửa)
                int drawX = scaledCenterX - scaledWidth / 2;
                int drawY = scaledCenterY - scaledHeight / 2;

                g.drawImage(currentFrame, drawX, drawY, scaledWidth, scaledHeight, null);
            }
        }

        public boolean isFinished() {
            return isFinished;
        }
    }
}
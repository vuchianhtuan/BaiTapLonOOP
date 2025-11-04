package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.effects.Shard;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Lớp đại diện cho Gạch Nổ.
 * <p>
 * Đây là một gạch phức tạp, có trạng thái (stateful). Nó có thể bị phá hủy
 * bởi bóng ({@code takeHit}) hoặc được kích hoạt bởi một vụ nổ khác
 * ({@code ignite}) để tạo ra các vụ nổ dây chuyền (chain reaction).
 */
public class ExplosiveBrick extends Brick {

    /**
     * Các trạng thái của gạch nổ:
     * - ALIVE: Bình thường, đang chờ bị va chạm.
     * - IGNITED: Đã bị kích hoạt (từ 1 vụ nổ khác), đang đếm ngược để nổ.
     * - EXPLODING: Đang phát hoạt ảnh nổ.
     * - FINISHED: Đã nổ xong, chờ bị dọn dẹp (cleanup).
     */
    private enum State {
        ALIVE,
        IGNITED,
        EXPLODING,
        FINISHED
    }

    private State currentState;
    private ExplosionEffect explosionEffect;
    /** Bộ đếm giờ (tính bằng số frame) khi ở trạng thái IGNITED. */
    private int ignitionTimer;

    /** Mảng cache (static) cho các khung hình (frames) của hoạt ảnh nổ. */
    private static BufferedImage[] explosionFrames;
    private static final int FRAME_COUNT = 8;

    public ExplosiveBrick(int x, int y, int width, int height) {
        super(x, y, width, height, 1, "Explosive", "explosiveBrick");
        this.currentState = State.ALIVE;

        // Chỉ tải frames một lần (khi đối tượng đầu tiên được tạo)
        if (explosionFrames == null) {
            loadExplosionFrames();
        }
    }

    /**
     * Tải (load) một lần (static) các khung hình (frames) của hoạt ảnh nổ
     * từ {@link AssetManager} vào cache ({@code explosionFrames}).
     */
    private static void loadExplosionFrames() {
        explosionFrames = new BufferedImage[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            // Tên frames phải là "explosion_render1", "explosion_render2", v.v.
            String imageName = "explosion_render" + (i + 1);
            explosionFrames[i] = AssetManager.getInstance().getImage(imageName);
            if (explosionFrames[i] == null) {
                System.err.println("Explosion frame '" + imageName + "' not loaded!");
                break;
            }
        }
    }

    /**
     * Hàm nội bộ: Bắt đầu phát hoạt ảnh nổ.
     * Chuyển trạng thái sang `EXPLODING` và tạo đối tượng hiệu ứng.
     */
    private void startExplosion() {
        this.currentState = State.EXPLODING;
        int centerX = this.x + this.width / 2;
        int centerY = this.y + this.height / 2;
        this.explosionEffect = new ExplosionEffect(centerX, centerY);
    }

    /**
     * Kích hoạt (ignite) gạch này để nó nổ sau một khoảng trễ.
     * Dùng cho các vụ nổ dây chuyền (chain reaction) từ {@code ExplosionSystem}.
     *
     * @param delayFrames Số frame phải chờ trước khi tự nổ.
     */
    public void ignite(int delayFrames) {
        // Chỉ kích hoạt nếu gạch còn sống (chưa bị kích hoạt hoặc chưa nổ)
        if (this.currentState == State.ALIVE) {
            this.currentState = State.IGNITED;
            this.ignitionTimer = delayFrames;
        }
    }

    /**
     * Kiểm tra xem gạch có ở trạng thái `ALIVE` (mặc định) không.
     * @return true nếu gạch đang ở trạng thái ALIVE.
     */
    public boolean isAlive() {
        return this.currentState == State.ALIVE;
    }

    /**
     * Kiểm tra xem gạch có đang trong trạng thái `IGNITED` (đếm ngược) không.
     * @return true nếu gạch đang đếm ngược.
     */
    public boolean isAwaitingDetonation() {
        return this.currentState == State.IGNITED;
    }

    /**
     * Xử lý khi gạch bị va chạm (ví dụ: bởi bóng).
     * <p>
     * Ghi đè (override) phương thức cơ sở: Khi bị va chạm, gạch nổ sẽ
     * nổ *ngay lập tức*, bất kể đang ở trạng thái `ALIVE` hay `IGNITED`.
     */
    @Override
    public void takeHit() {
        // Chỉ phản ứng nếu gạch chưa bắt đầu nổ
        if (currentState == State.ALIVE || currentState == State.IGNITED) {
            if (currentState == State.IGNITED) {
                this.ignitionTimer = 0; // Hủy bỏ đếm ngược (nếu có)
            }

            super.takeHit(); // Đặt hitPoints = 0
            startExplosion(); // Nổ ngay
        }
        // Nếu đã EXPLODING hoặc FINISHED thì không làm gì
    }

    /**
     * Kích nổ gạch này ngay lập tức (do bị một vụ nổ khác tác động).
     * <p>
     * Phương thức này chỉ hoạt động nếu gạch đang ở trạng thái `IGNITED`.
     * Nó sẽ tạo mảnh vỡ và kích hoạt vụ nổ ngay lập tức.
     *
     * @param activeShards Danh sách để thêm các mảnh vỡ (shards) vào.
     * @return true nếu kích nổ thành công, false nếu không.
     */
    public boolean detonateOnHit(List<Shard> activeShards) {
        if (this.currentState == State.IGNITED) {

            // 1. Tạo vụn vỡ tức thì (tương đương với bị phá hủy)
            activeShards.addAll(this.shatter());

            // 2. Kích hoạt vụ nổ (chuyển sang EXPLODING)
            this.takeHit(); // Gọi takeHit để xử lý logic nổ

            return true;
        }
        return false;
    }


    /**
     * Ghi đè (override): Gạch nổ được coi là "bị phá hủy" (để bóng đi xuyên qua)
     * ngay khi nó không còn ở trạng thái `ALIVE` hoặc `IGNITED`.
     */
    @Override
    public boolean isDestroyed() {
        return currentState != State.ALIVE && currentState != State.IGNITED;
    }

    /**
     * Kiểm tra xem gạch đã nổ xong (hoạt ảnh đã kết thúc) hay chưa.
     * Được dùng bởi {@code EntityManager} để dọn dẹp (cleanup) đối tượng.
     *
     * @return true nếu trạng thái là `FINISHED`.
     */
    public boolean isFinished() {
        return currentState == State.FINISHED;
    }

    /**
     * Cập nhật logic của gạch nổ dựa trên trạng thái (state).
     */
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
                    currentState = State.FINISHED; // Nổ xong
                }
                break;
            default:
                // ALIVE và FINISHED không cần làm gì
                break;
        }
    }

    /**
     * Vẽ gạch nổ dựa trên trạng thái (state).
     */
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
                    // (Ví dụ hiệu ứng nhấp nháy đơn giản)
                    if (ignitionTimer % 10 < 5) { // Nhấp nháy mỗi 10 frame
                        g.drawImage(imgIgnited,
                                sm.scaleX(this.x), sm.scaleY(this.y),
                                sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
                    }
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

    /**
     * Lớp nội bộ (private inner class) để quản lý hoạt ảnh nổ.
     * Quản lý khung hình (frame) hiện tại và thời gian của hoạt ảnh.
     */
    private static class ExplosionEffect {
        private int x, y; // Tọa độ TÂM (logic) của vụ nổ
        private int currentFrameIndex;
        private boolean isFinished;
        private long lastFrameTime;
        private long frameDuration = 55; // Thời gian (ms) cho mỗi khung hình

        /**
         * @param x Tọa độ X (logic) của TÂM vụ nổ.
         * @param y Tọa độ Y (logic) của TÂM vụ nổ.
         */
        public ExplosionEffect(int x, int y) {
            this.x = x;
            this.y = y;
            this.currentFrameIndex = 0;
            this.isFinished = false;
            this.lastFrameTime = System.currentTimeMillis();
        }

        /**
         * Cập nhật logic, chuyển sang khung hình (frame) tiếp theo nếu đủ thời gian.
         */
        public void update() {
            if (isFinished || explosionFrames == null) return;
            // Chuyển frame nếu đã hết 'frameDuration'
            if (System.currentTimeMillis() - lastFrameTime > frameDuration) {
                currentFrameIndex++;
                lastFrameTime = System.currentTimeMillis();
                // Nếu là frame cuối cùng, đánh dấu là hoàn thành
                if (currentFrameIndex >= FRAME_COUNT) {
                    isFinished = true;
                }
            }
        }

        /**
         * Vẽ khung hình (frame) hoạt ảnh nổ hiện tại.
         */
        public void draw(Graphics g, ScalingManager sm) {
            if (!isFinished && explosionFrames != null && explosionFrames[currentFrameIndex] != null) {
                BufferedImage currentFrame = explosionFrames[currentFrameIndex];
                int originalFrameWidth = currentFrame.getWidth();
                int originalFrameHeight = currentFrame.getHeight();

                // Co giãn kích thước của frame (ví dụ: nhỏ hơn 5 lần)
                int scaledWidth = sm.scaleWidth(originalFrameWidth) / 5;
                int scaledHeight = sm.scaleHeight(originalFrameHeight) / 5;

                // Lấy tọa độ TÂM (đã scale)
                int scaledCenterX = sm.scaleX(this.x);
                int scaledCenterY = sm.scaleY(this.y);

                // Tính tọa độ vẽ (drawX, drawY) để căn giữa frame tại tâm
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
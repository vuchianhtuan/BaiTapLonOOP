package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.core.GameManager;
import java.awt.Graphics;
import java.awt.Graphics2D; // <-- Cần import Graphics2D
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;

/**
 * Lớp đại diện cho vật phẩm tăng sức mạnh (PowerUp) "Paddle Dính" (Sticky Paddle).
 * <p>
 * Khi được thu thập, PowerUp này cho phép paddle "bắt dính" quả bóng
 * ({@link com.mygame.arkanoid.objects.Ball}) khi va chạm,
 * thay vì nảy ra ngay lập tức.
 * <p>
 * Khi rơi, vật phẩm này có hiệu ứng hình ảnh tự xoay (rotation).
 */
public class StickyPaddlePowerUp extends PowerUp {
    private String imageName = "stickyPowerUp";

    /** Góc xoay hiện tại (tính bằng độ) cho hiệu ứng hình ảnh. */
    private double rotationAngle = 0;
    /** Tốc độ xoay (tính bằng độ mỗi frame). */
    private final double ROTATION_SPEED = 5.0;

    /**
     * Khởi tạo PowerUp "Paddle Dính".
     * <p>
     * Gọi constructor lớp cha với loại (type) "sticky" và
     * thời gian hiệu lực (duration) là 600 frame (khoảng 10 giây).
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public StickyPaddlePowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "sticky", 600);
    }

    /**
     * Áp dụng hiệu ứng: Đặt trạng thái "dính" (sticky) của paddle thành {@code true}.
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override public void applyEffect(GameManager gameManager) {
        gameManager.getPaddle().setSticky(true);
    }

    /**
     * Gỡ bỏ hiệu ứng: Đặt trạng thái "dính" (sticky) của paddle về {@code false}.
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override public void removeEffect(GameManager gameManager) {
        gameManager.getPaddle().setSticky(false);
    }

    /**
     * Cập nhật logic: Xử lý việc rơi xuống và cập nhật góc xoay cho hiệu ứng.
     */
    @Override public void update() {
        // 1. Cập nhật vị trí rơi
        this.y += fallSpeed;

        // 2. Cập nhật góc xoay
        this.rotationAngle += ROTATION_SPEED;
        if (this.rotationAngle >= 360) {
            this.rotationAngle -= 360; // Giữ góc xoay trong khoảng [0, 360)
        }
    }

    /**
     * Vẽ (render) PowerUp lên màn hình, áp dụng hiệu ứng xoay.
     * <p>
     * Sử dụng {@link Graphics2D} để thực hiện phép biến đổi (xoay)
     * {@code rotationAngle} quanh tâm của vật phẩm trước khi vẽ hình ảnh.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);

        if (img != null) {
            // Sử dụng Graphics2D để có thể áp dụng phép biến đổi (xoay)
            Graphics2D g2d = (Graphics2D) g.create(); // Tạo bản sao

            // Tính toán tọa độ và kích thước đã co giãn (scale)
            int scaledX = sm.scaleX(this.x);
            int scaledY = sm.scaleY(this.y);
            int scaledWidth = sm.scaleWidth(this.width);
            int scaledHeight = sm.scaleHeight(this.height);

            // Tính toán tâm (đã scale) để xoay quanh đó
            double centerX = scaledX + scaledWidth / 2.0;
            double centerY = scaledY + scaledHeight / 2.0;

            // Áp dụng phép xoay (tính bằng radian) quanh tâm
            double rotateRadian = Math.toRadians(this.rotationAngle);
            g2d.rotate(rotateRadian, centerX, centerY);

            // Vẽ ảnh (đã được xoay)
            g2d.drawImage(img, scaledX, scaledY, scaledWidth, scaledHeight, null);

            // Hủy (dispose) bản sao Graphics2D để khôi phục
            // trạng thái Graphics ban đầu (quan trọng!)
            g2d.dispose();
        }
    }
}
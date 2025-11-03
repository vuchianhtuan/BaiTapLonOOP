package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Lớp đại diện cho vật phẩm tăng sức mạnh (PowerUp) "Làm Chậm Bóng" (Slow Ball).
 * <p>
 * Khi được thu thập, PowerUp này sẽ giảm tốc độ của bóng ({@link com.mygame.arkanoid.objects.Ball})
 * đi một hệ số ({@code SLOW_SPEED_FACTOR}) trong một khoảng thời gian
 * ({@code duration}) nhất định.
 */
public class SlowBallPowerUp extends PowerUp {

    /** Cập nhật logic: Xử lý việc rơi xuống. */
    @Override public void update() {
        this.y += fallSpeed;
    }

    private String imageName = "slowBallPowerUp";
    /** Hệ số nhân tốc độ (ví dụ: 0.5 = giảm 50%). */
    private static final double SLOW_SPEED_FACTOR = 0.5;

    /**
     * Khởi tạo PowerUp "Làm Chậm Bóng".
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public SlowBallPowerUp(int x, int y, int width, int height) {
        // Gọi constructor lớp cha với loại "slow_ball" và thời gian 300 frame (5s)
        super(x, y, width, height, "slow_ball", 300);
    }

    /**
     * Áp dụng hiệu ứng: Giảm tốc độ bóng.
     * <p>
     * Tốc độ mới được tính dựa trên tốc độ *gốc* của bóng
     * ({@code originalSpeed}) để tránh cộng dồn/nhân dồn lỗi.
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override
    public void applyEffect(GameManager gameManager) {
        double originalSpeed = gameManager.getBall().getOriginalSpeed();
        // Đặt tốc độ mới = tốc độ gốc * hệ số
        gameManager.getBall().setSpeed(originalSpeed * SLOW_SPEED_FACTOR);
    }

    /**
     * Gỡ bỏ hiệu ứng: Đặt lại tốc độ bóng về gốc.
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override
    public void removeEffect(GameManager gameManager) {
        // Khôi phục tốc độ gốc
        gameManager.getBall().resetSpeed();
    }

    /**
     * Vẽ (render) PowerUp lên màn hình.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override
    public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            // Vẽ ảnh
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            // Fallback: Vẽ hình chữ nhật màu lục lam (Cyan) nếu ảnh bị lỗi
            g.setColor(Color.CYAN);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}
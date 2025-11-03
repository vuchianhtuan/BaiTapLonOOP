package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.systems.ScalingManager;

/**
 * Lớp đại diện cho vật phẩm tăng sức mạnh (PowerUp) "Thêm Mạng" (Extra Life).
 * <p>
 * Khi được thu thập, PowerUp này ngay lập tức cộng thêm 1 mạng (life)
 * cho người chơi. Nó không có hiệu ứng theo thời gian (duration)
 * và không có hành động {@code removeEffect}.
 */
public class ExtraLifePowerUp extends PowerUp {
    private String imageName = "extraLifePowerUp";

    /**
     * Khởi tạo PowerUp "Thêm Mạng".
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public ExtraLifePowerUp(int x, int y, int width, int height) {
        // Gọi constructor lớp cha.
        // Loại "extra_life", thời gian tồn tại 300 frame (chỉ là thời gian rơi)
        super(x, y, width, height, "extra_life", 300);
    }

    /**
     * Áp dụng hiệu ứng: Thêm 1 mạng cho người chơi.
     * <p>
     * Được gọi bởi {@code GameManager} ngay khi va chạm với paddle.
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override public void applyEffect(GameManager gameManager) {
        int pre_lives = gameManager.getLives();
        gameManager.setLives(pre_lives + 1);
    }

    /**
     * Gỡ bỏ hiệu ứng (không làm gì).
     * <p>
     * Ghi đè (override) phương thức trừu tượng nhưng để trống,
     * vì hiệu ứng cộng mạng là vĩnh viễn (cho đến khi bị mất).
     *
     * @param gameManager Tham chiếu đến GameManager (không được sử dụng).
     */
    @Override public void removeEffect(GameManager gameManager) {}

    /**
     * Cập nhật logic: Xử lý việc rơi xuống.
     */
    @Override public void update() {
        this.y += fallSpeed;
    }

    /**
     * Vẽ (render) PowerUp lên màn hình.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            // Vẽ ảnh (thường là hình trái tim)
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            // Fallback: Vẽ hình chữ nhật màu đỏ nếu ảnh bị lỗi
            g.setColor(Color.RED);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}
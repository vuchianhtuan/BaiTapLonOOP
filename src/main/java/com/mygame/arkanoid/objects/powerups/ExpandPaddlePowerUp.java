package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.Graphics;
import java.awt.Graphics2D;      // <-- THÊM
import java.awt.AlphaComposite; // <-- THÊM
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.Math;          // <-- THÊM

/**
 * Lớp đại diện cho vật phẩm tăng sức mạnh (PowerUp) "Mở Rộng Paddle".
 * <p>
 * Khi người chơi thu thập vật phẩm này, chiều rộng của thanh đỡ (paddle)
 * sẽ được tăng lên một lượng cố định ({@code EXPAND_AMOUNT}) trong một
 * khoảng thời gian ({@code duration}) nhất định.
 * <p>
 * Power-up này cũng có hiệu ứng hình ảnh nhấp nháy (sử dụng hàm Sin)
 * khi nó rơi xuống.
 */
public class ExpandPaddlePowerUp extends PowerUp {
    private String imageName;
    /** Lượng pixel (logic) sẽ được cộng thêm vào chiều rộng của paddle. */
    private static final int EXPAND_AMOUNT = 50;

    // --- Biến cho hiệu ứng nhấp nháy (Fading/Pulsing) ---
    /** Bộ đếm thời gian (timer) nội bộ cho hàm sin, tăng dần. */
    private float fadeTimer = 0;
    /** Giá trị độ mờ (alpha) hiện tại, được tính toán mỗi frame. */
    private float currentAlpha = 1.0f;
    /** Tốc độ tăng của `fadeTimer`, quyết định tốc độ nhấp nháy. */
    private static final float FADE_SPEED = 0.03f;

    /**
     * Cập nhật logic của PowerUp mỗi frame.
     * <p>
     * Ghi đè (override) để xử lý việc rơi xuống (thay đổi y) và
     * tính toán hiệu ứng nhấp nháy (alpha).
     */
    @Override public void update() {
        // 1. Cập nhật vị trí rơi
        this.y += fallSpeed;

        // 2. Cập nhật logic hiệu ứng nhấp nháy
        // Tăng bộ đếm thời gian
        this.fadeTimer += FADE_SPEED;

        // Tính toán alpha dựa trên hàm sin
        // Math.sin(fadeTimer) dao động từ -1 đến 1.
        // Math.abs(...) làm cho nó dao động từ 0 (mờ nhất) đến 1 (rõ nhất)
        // tạo ra hiệu ứng "thở" (pulsing) mượt mà.
        this.currentAlpha = Math.abs((float)Math.sin(this.fadeTimer));
    }

    /**
     * Khởi tạo PowerUp "Mở Rộng Paddle".
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public ExpandPaddlePowerUp(int x, int y, int width, int height) {
        // Gọi constructor lớp cha với thời gian tồn tại 600 frame (10 giây)
        super(x, y, width, height, "expand", 600);
        this.imageName = "expandPowerUp";
    }

    /**
     * Áp dụng hiệu ứng: Mở rộng paddle.
     * <p>
     * Phương thức này được gọi bởi {@code GameManager} khi PowerUp
     * được kích hoạt (va chạm với paddle).
     * <p>
     * <b>Quan trọng:</b> Luôn gọi {@code resetWidth()} trước khi mở rộng
     * để tránh cộng dồn hiệu ứng nếu nhặt 2 power-up cùng loại.
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.getPaddle().resetWidth(); // Đặt lại về chiều rộng gốc
        gameManager.getPaddle().expand(EXPAND_AMOUNT); // Mở rộng
    }

    /**
     * Gỡ bỏ hiệu ứng: Đặt lại chiều rộng paddle.
     * <p>
     * Phương thức này được gọi bởi {@code GameManager} khi
     * PowerUp hết hạn (timer).
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override
    public void removeEffect(GameManager gameManager) {
        // Đặt lại paddle về chiều rộng mặc định
        gameManager.getPaddle().resetWidth();
    }

    /**
     * Vẽ (render) PowerUp lên màn hình.
     * <p>
     * Ghi đè (override) để áp dụng hiệu ứng nhấp nháy (alpha)
     * đã được tính toán trong {@link #update()}.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override
    public void render(Graphics g, ScalingManager sm) {
        // Tạo một bản sao của Graphics để tránh làm ảnh hưởng
        // đến các thao tác vẽ khác (vì chúng ta sẽ thay đổi AlphaComposite)
        Graphics2D g2d = (Graphics2D) g.create();

        // Áp dụng độ mờ (alpha) đã tính toán
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.currentAlpha));

        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);

        // Tính toán tọa độ và kích thước đã co giãn (scale)
        int scaledX = sm.scaleX(this.x);
        int scaledY = sm.scaleY(this.y);
        int scaledWidth = sm.scaleWidth(this.width);
        int scaledHeight = sm.scaleHeight(this.height);

        if (img != null) {
            // Vẽ ảnh với độ mờ (alpha) đã thiết lập
            g2d.drawImage(img, scaledX, scaledY, scaledWidth, scaledHeight, null);
        } else {
            // Fallback: Vẽ hình chữ nhật màu xanh nếu ảnh bị lỗi
            g2d.setColor(Color.GREEN);
            g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
        }

        // Hủy (dispose) bản sao Graphics2D để khôi phục
        // trạng thái Graphics ban đầu (quan trọng!)
        g2d.dispose();
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
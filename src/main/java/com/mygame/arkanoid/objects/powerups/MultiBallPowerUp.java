package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Ball;

import java.awt.*;
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.systems.ScalingManager;

/**
 * Lớp đại diện cho vật phẩm tăng sức mạnh (PowerUp) "Nhân Bản Bóng" (Multi Ball).
 * <p>
 * Khi được thu thập, PowerUp này ngay lập tức tạo ra thêm
 * {@code BALL_COUNT} quả bóng mới tại vị trí của quả bóng chính.
 * Nó không có hiệu ứng theo thời gian (duration) và không có hành động
 * {@code removeEffect}.
 * <p>
 * Khi rơi, vật phẩm này có hiệu ứng hình ảnh tự xoay (rotation).
 */
public class MultiBallPowerUp extends PowerUp {
    private String imageName = "multiBallPowerUp";
    /** Số lượng bóng mới sẽ được tạo ra. */
    public static final int BALL_COUNT = 3;

    // --- Biến cho hiệu ứng xoay ---
    /** Góc xoay hiện tại (tính bằng độ). */
    private double rotationAngle = 0;
    /** Tốc độ xoay (tính bằng độ mỗi frame). */
    private final double ROTATION_SPEED = 4.5;

    /**
     * Khởi tạo PowerUp "Nhân Bản Bóng".
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public MultiBallPowerUp(int x, int y, int width, int height) {
        // Gọi constructor lớp cha.
        // Thời gian tồn tại 50 frame (chỉ là thời gian rơi, vì hiệu ứng là tức thời)
        super(x, y, width, height, "multi_ball", 50);
    }

    /**
     * Áp dụng hiệu ứng: Tạo thêm nhiều bóng từ vị trí bóng chính.
     * <p>
     * Được gọi bởi {@code GameManager} ngay khi va chạm với paddle.
     * Tạo ra {@code BALL_COUNT} quả bóng mới với các hướng
     * ban đầu được gán cứng (hardcoded).
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override public void applyEffect(GameManager gameManager) {
        Ball mainBall = gameManager.getBall();
        int x = mainBall.getX();
        int y = mainBall.getY();

        // Tạo ra các quả bóng mới
        for (int i = 0; i < BALL_COUNT; i++) {
            Ball b = new Ball(x, y, 18, 18, gameManager.getSelectedBallSkinKey());
            b.setStuckToPaddle(false); // Đảm bảo bóng mới di chuyển ngay

            // Gán hướng di chuyển ban đầu khác nhau
            if (i == 0) {
                b.setDx(1);  b.setDy(-1);
            } else if (i == 1) {
                b.setDx(-1); b.setDy(1);
            } else {
                b.setDx(1);  b.setDy(1);
            }
            gameManager.addball(b); // Thêm bóng mới vào EntityManager
        }
    }

    /**
     * Gỡ bỏ hiệu ứng (không làm gì).
     * <p>
     * Ghi đè (override) phương thức trừu tượng nhưng để trống,
     * vì hiệu ứng nhân bản bóng là tức thời và không bị gỡ bỏ.
     *
     * @param gameManager Tham chiếu đến GameManager (không được sử dụng).
     */
    @Override public void removeEffect(GameManager gameManager) {}

    /**
     * Cập nhật logic: Xử lý việc rơi xuống và hiệu ứng xoay.
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
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override public void render(Graphics g, ScalingManager sm) {
        // Sử dụng Graphics2D để có thể áp dụng phép biến đổi (xoay)
        Graphics2D g2d = (Graphics2D) g.create();

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

        // --- Logic vẽ ---
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            // Vẽ ảnh (đã được xoay)
            g2d.drawImage(img, scaledX, scaledY, scaledWidth, scaledHeight, null);
        } else {
            // Fallback: Vẽ hình chữ nhật màu tím
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
        }

        // Hủy (dispose) bản sao Graphics2D để khôi phục
        // trạng thái Graphics ban đầu (quan trọng!)
        g2d.dispose();
    }
}
package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Laser;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.image.BufferedImage;

import java.awt.*;

/**
 * Lớp đại diện cho một viên gạch có khả năng bắn tia laser ({@link Laser}).
 * <p>
 * Lớp này kế thừa từ {@link StrongBrick} vì nó cũng có nhiều máu (hit points).
 * Nó có thêm logic để đếm ngược thời gian (cooldown) và bắn ra
 * các đối tượng Laser.
 */
public class LaserShooterBrick extends StrongBrick {
    /** Hình ảnh riêng biệt cho gạch bắn laser. */
    private BufferedImage laserShooterImage;

    /** Bộ đếm thời gian (frame) còn lại trước khi có thể bắn lần tiếp theo. */
    private int shootCooldown;
    /** Thời gian (frame) cố định giữa mỗi lần bắn. (180 frames ≈ 3 giây @ 60 FPS) */
    private static final int SHOOT_INTERVAL = 180;

    /**
     * Khởi tạo một Gạch Bắn Laser mới.
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     * @param hits Số lần chịu đòn (máu).
     */
    public LaserShooterBrick(int x, int y, int width, int height, int hits) {
        super(x, y, width, height, hits); // Gọi constructor của StrongBrick
        // Ngẫu nhiên hóa thời gian bắn ban đầu để các gạch không bắn cùng lúc
        this.shootCooldown = (int) (Math.random() * SHOOT_INTERVAL);
        this.laserShooterImage = AssetManager.getInstance().getImage("laserShooter");
    }

    /**
     * Cập nhật logic của gạch.
     * <p>
     * Ghi đè (override) để thêm logic đếm ngược (cooldown)
     * cho việc bắn laser.
     */
    @Override
    public void update() {
        super.update(); // Gọi update của lớp cha (StrongBrick)
        // Đếm ngược thời gian hồi chiêu
        if (shootCooldown > 0) {
            shootCooldown--;
        }
    }

    /**
     * Thử thực hiện một cú bắn.
     * <p>
     * Chỉ bắn thành công (trả về một {@link Laser} mới) nếu
     * {@code shootCooldown} đã đếm về 0.
     *
     * @return Một đối tượng {@code Laser} mới nếu bắn thành công,
     * ngược lại trả về {@code null}.
     */
    public Laser tryToShoot() {
        if (shootCooldown <= 0) {
            shootCooldown = SHOOT_INTERVAL; // Đặt lại thời gian hồi chiêu
            // Tạo laser ngay dưới tâm của gạch
            int laserX = this.getX() + this.getWidth() / 2;
            int laserY = this.getY() + this.getHeight();
            return new Laser(laserX, laserY);
        }
        return null; // Chưa hồi chiêu xong, không bắn
    }

    /**
     * Ghi đè (override) phương thức vẽ.
     * <p>
     * Vẽ hình ảnh "laserShooter" riêng biệt thay vì các
     * hình ảnh gạch cứng (strong brick) của lớp cha.
     * Có phương án dự phòng (fallback) nếu ảnh không được tải.
     */
    @Override
    public void render(Graphics g, ScalingManager sm) {
        if (laserShooterImage != null) {
            // Vẽ ảnh gạch bắn laser
            g.drawImage(laserShooterImage,
                    sm.scaleX(this.x),
                    sm.scaleY(this.y),
                    sm.scaleWidth(this.width),
                    sm.scaleHeight(this.height),
                    null);
        } else {
            // Phương án dự phòng: nếu không tìm thấy ảnh
            // Vẽ như StrongBrick mặc định (lớp cha)
            super.render(g, sm);
            // (Tùy chọn) Vẽ một lớp phủ màu đỏ để dễ gỡ lỗi (debug)
            g.setColor(new Color(255, 0, 0, 100)); // Màu đỏ mờ
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}
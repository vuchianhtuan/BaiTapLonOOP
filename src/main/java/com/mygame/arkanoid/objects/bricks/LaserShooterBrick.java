package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Laser;
import com.mygame.arkanoid.systems.helper.ScalingManager;
import java.awt.image.BufferedImage;

import java.awt.*;

public class LaserShooterBrick extends StrongBrick {
    private BufferedImage laserShooterImage;

    private int shootCooldown;
    private static final int SHOOT_INTERVAL = 180; // Bắn mỗi 3 giây (180 frames @ 60 FPS)

    public LaserShooterBrick(int x, int y, int width, int height, int hits) {
        super(x, y, width, height, hits);
        this.shootCooldown = (int) (Math.random() * SHOOT_INTERVAL); // Ngẫu nhiên hóa thời gian bắn ban đầu
        this.laserShooterImage = AssetManager.getInstance().getImage("laserShooter");
    }

    /**
     * Cập nhật bộ đếm thời gian bắn.
     */
    @Override
    public void update() {
        super.update(); // Gọi update của lớp cha (cho các hiệu ứng nếu có)
        if (shootCooldown > 0) {
            shootCooldown--;
        }
    }

    /**
     * Cố gắng bắn ra một tia laser.
     * @return một đối tượng Laser nếu bắn thành công, ngược lại trả về null.
     */
    public Laser tryToShoot() {
        if (shootCooldown <= 0) {
            shootCooldown = SHOOT_INTERVAL; // Reset thời gian
            int laserX = this.getX() + this.getWidth() / 2;
            int laserY = this.getY() + this.getHeight();
            return new Laser(laserX, laserY);
        }
        return null;
    }

    /**
     * Tùy chỉnh cách vẽ để phân biệt với gạch thường.
     */
    @Override
    public void render(Graphics g, ScalingManager sm) {
        if (laserShooterImage != null) {
            g.drawImage(laserShooterImage,
                    sm.scaleX(this.x),
                    sm.scaleY(this.y),
                    sm.scaleWidth(this.width),
                    sm.scaleHeight(this.height),
                    null);
        } else {
            // Phương án dự phòng: nếu không tìm thấy ảnh, vẽ như StrongBrick hoặc một màu nào đó
            // Bạn có thể giữ super.render(g, sm); để vẽ StrongBrick mặc định
            super.render(g, sm);
            // Hoặc vẽ một hình chữ nhật màu đỏ để biết đây là shooter bị thiếu ảnh
            g.setColor(Color.RED);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}
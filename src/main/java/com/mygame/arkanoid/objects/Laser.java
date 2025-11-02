package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.engine.AssetManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Laser {
    private int x, y;
    private final int width, height;
    private final int speed;
    private final Rectangle bounds;

    public Laser(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 10;   // Chiều rộng của tia laser
        this.height = 25; // Chiều dài của tia laser
        this.speed = 5;   // Tốc độ di chuyển
        this.bounds = new Rectangle(x, y, width, height);
    }

    /**
     * Cập nhật vị trí của laser (di chuyển xuống dưới).
     */
    public void update() {
        y += speed;
        bounds.y = y;
    }

    /**
     * Vẽ laser lên màn hình.
     */
    public void render(Graphics g, ScalingManager sm) {
        BufferedImage laserImage = AssetManager.getInstance().getImage("laser");

        // 2. Kiểm tra xem ảnh có tồn tại không
        if (laserImage != null) {
            // 3. Nếu có, vẽ ảnh ra màn hình với kích thước đã co giãn
            g.drawImage(laserImage,
                    sm.scaleX(this.x),
                    sm.scaleY(this.y),
                    sm.scaleWidth(this.width),
                    sm.scaleHeight(this.height),
                    null);
        } else {
            // Phương án dự phòng: Nếu không tìm thấy ảnh, vẽ hình chữ nhật màu đỏ
            g.setColor(Color.RED);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }

    public int getY() {
        return y;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}

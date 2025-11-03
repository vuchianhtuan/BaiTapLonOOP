package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.engine.AssetManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Lớp Laser đại diện cho một viên đạn (tia laser) trong trò chơi.
 * <p>
 * Đây là một đối tượng đơn giản, di chuyển thẳng xuống ({@code update()})
 * và được vẽ ({@code render()}). Nó được bắn ra bởi
 * {@link com.mygame.arkanoid.objects.bricks.LaserShooterBrick}.
 * <p>
 * Lưu ý: Lớp này *không* kế thừa từ {@link GameObject}
 * (có thể là một quyết định thiết kế).
 */
public class Laser {
    private int x, y;
    private final int width, height;
    private final int speed;
    /** Hình chữ nhật bao (bounding box) được cập nhật thủ công. */
    private final Rectangle bounds;

    /**
     * Khởi tạo một tia Laser mới tại vị trí chỉ định.
     * Tọa độ {@code x} thường được căn giữa so với vật thể bắn ra.
     *
     * @param x Vị trí X (logic) ban đầu.
     * @param y Vị trí Y (logic) ban đầu.
     */
    public Laser(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 10;   // Chiều rộng của tia laser
        this.height = 25; // Chiều dài của tia laser
        this.speed = 5;   // Tốc độ di chuyển
        this.bounds = new Rectangle(x, y, width, height);
    }

    /**
     * Cập nhật vị trí của laser (di chuyển thẳng xuống dưới).
     * Đồng thời cập nhật vị trí Y của hình chữ nhật bao ({@code bounds}).
     */
    public void update() {
        y += speed; // Di chuyển xuống
        bounds.y = y; // Cập nhật bounds
    }

    /**
     * Vẽ (render) laser lên màn hình.
     * <p>
     * Lấy ảnh "laser" từ {@link AssetManager} và vẽ nó tại
     * vị trí đã được co giãn (scale).
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    public void render(Graphics g, ScalingManager sm) {
        BufferedImage laserImage = AssetManager.getInstance().getImage("laser");

        if (laserImage != null) {
            // Vẽ ảnh laser
            g.drawImage(laserImage,
                    sm.scaleX(this.x),
                    sm.scaleY(this.y),
                    sm.scaleWidth(this.width),
                    sm.scaleHeight(this.height),
                    null);
        } else {
            // Fallback: Vẽ hình chữ nhật màu đỏ nếu ảnh bị lỗi
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
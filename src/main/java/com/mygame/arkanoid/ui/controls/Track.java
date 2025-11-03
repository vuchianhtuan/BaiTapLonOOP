package com.mygame.arkanoid.ui.controls;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.GameObject;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Lớp Track đại diện cho "đường ray" (phần nền)
 * của một thanh trượt (Slider) trong giao diện người dùng (UI).
 * <p>
 * Đây là một đối tượng {@link GameObject} tĩnh, chủ yếu chỉ dùng để
 * vẽ (render) hình ảnh của đường ray.
 */
public class Track extends GameObject {
    String imageName;

    /**
     * Khởi tạo một Track (đường ray) mới.
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public Track(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.imageName = "track"; // Gán cứng asset key
    }

    /**
     * (Bỏ trống) Phương thức cập nhật (update) logic.
     * Track là một đối tượng tĩnh (static) không cần cập nhật.
     */
    @Override
    public void update() {
        // Cập nhật logic của Track nếu cần thiết (ví dụ: hiệu ứng)
    }

    /**
     * Vẽ (render) đường ray (Track) lên màn hình.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override
    public void render(Graphics g, ScalingManager sm) {

        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            // Vẽ ảnh đường ray đã co giãn (scale)
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            // Fallback: Vẽ hình chữ nhật màu xám nếu ảnh bị lỗi
            g.setColor(Color.GRAY);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}
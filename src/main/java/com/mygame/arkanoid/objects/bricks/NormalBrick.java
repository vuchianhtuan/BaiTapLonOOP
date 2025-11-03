package com.mygame.arkanoid.objects.bricks;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;

/**
 * Đại diện cho một viên gạch bình thường (Normal Brick).
 * <p>
 * Đây là loại gạch cơ bản nhất, chỉ cần 1 lần va chạm để phá hủy
 * và không có hành vi (logic) đặc biệt nào trong {@code update}.
 */
public class NormalBrick extends Brick {

    /**
     * Phương thức cập nhật (update) logic.
     * Bỏ trống vì gạch thường không có hành vi di chuyển hay bắn.
     */
    @Override public void update() {}

    /**
     * Vẽ (render) gạch lên màn hình.
     * Lấy hình ảnh (asset) dựa trên {@code imageName}
     * (được đặt trong constructor) từ {@link AssetManager}.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager) để tính toán tọa độ.
     */
    @Override public void render(Graphics g, ScalingManager sm) {
        // Lấy ảnh (đã được tải theo theme) từ AssetManager
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            // Vẽ ảnh đã được co giãn (scale)
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        }
    }

    /**
     * Khởi tạo một Gạch Thường mới.
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public NormalBrick(int x, int y, int width, int height) {
        // Gọi constructor của lớp cha (Brick):
        // Mặc định gạch thường có 1 máu (hitPoints = 1)
        // và sử dụng asset key là "normalBrick".
        super(x, y, width, height, 1, "normal", "normalBrick");
    }
}
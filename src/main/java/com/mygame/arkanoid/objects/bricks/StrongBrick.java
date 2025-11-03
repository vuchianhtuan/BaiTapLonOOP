package com.mygame.arkanoid.objects.bricks;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Lớp StrongBrick đại diện cho một viên gạch cứng (cần nhiều hơn 1 hit)
 * trong trò chơi Arkanoid.
 * <p>
 * Đặc điểm chính là gạch này thay đổi hình ảnh (texture) để
 * hiển thị các trạng thái vỡ khi máu (hitPoints) giảm.
 */
public class StrongBrick extends Brick {

    /**
     * Cập nhật trạng thái của gạch.
     * <p>
     * Được gọi mỗi frame, phương thức này kiểm tra số máu ({@code hitPoints})
     * hiện tại và thay đổi {@code this.imageName} (asset key)
     * để phản ánh đúng trạng thái trực quan (vỡ ít, vỡ nhiều).
     */
    @Override public void update() {
        if (hitPoints == 2) {
            this.imageName = "strongBrick1"; // Ảnh vỡ 1
        } else if (hitPoints == 1) {
            this.imageName = "strongBrick2"; // Ảnh vỡ 2
        } else if (hitPoints >= 3) {
            this.imageName = "strongBrick"; // Ảnh gốc (3+ máu)
        }
    }

    /**
     * Hàm trợ giúp (helper) nội bộ (private static)
     * Lấy tên hình ảnh ban đầu dựa trên số máu khởi tạo.
     * <p>
     * Điều này đảm bảo gạch được tạo với 2 HP (ví dụ từ {@code BrickFactory})
     * sẽ sử dụng ngay ảnh "strongBrick1" thay vì ảnh "strongBrick".
     *
     * @param health số máu ban đầu của gạch.
     * @return tên (asset key) của hình ảnh ban đầu.
     */
    private static String getInitialImageName(int health) {
        if (health == 2) {
            return "strongBrick1"; // Gạch 2 HP bắt đầu với ảnh 1
        }
        if (health == 1) {
            return "strongBrick2"; // Gạch 1 HP bắt đầu với ảnh 2
        }
        // Mặc định (cho 3 HP hoặc nhiều hơn) là ảnh "strongBrick"
        return "strongBrick";
    }

    /**
     * Vẽ (render) gạch lên màn hình.
     * <p>
     * Vẽ hình ảnh có tên tương ứng với {@code this.imageName}
     * (đã được {@link #update()} cập nhật).
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager) để tính toán tọa độ.
     */
    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        }
    }

    /**
     * Khởi tạo một Gạch Cứng (StrongBrick) mới.
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     * @param health Số lần chịu đòn (máu) ban đầu.
     */
    public StrongBrick(int x, int y, int width, int height, int health) {
        // Gọi constructor của lớp cha (Brick)
        // 1. Đặt máu (health)
        // 2. Đặt loại (type) là "StrongBrick"
        // 3. Sử dụng helper `getInitialImageName` để chọn đúng asset key ban đầu
        super(x, y, width, height, health, "StrongBrick", getInitialImageName(health));
    }

}
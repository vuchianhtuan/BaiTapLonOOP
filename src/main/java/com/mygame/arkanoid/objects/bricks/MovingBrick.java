package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Đại diện cho một viên gạch có khả năng di chuyển ngang (trái/phải).
 * <p>
 * Lớp này kế thừa từ {@link Brick} (mặc định là 2 máu) và thêm logic
 * {@code update} để tự động di chuyển. Gạch sẽ đổi hướng khi:
 * 1. Chạm vào tường (biên màn hình).
 * 2. Đi quá phạm vi di chuyển ({@code moveRange}) so với vị trí ban đầu.
 * 3. Va chạm với một viên gạch khác trên cùng một hàng.
 */
public class MovingBrick extends Brick {
    /** Vận tốc di chuyển ngang (pixel/frame). Âm là sang trái, dương là sang phải. */
    private int speedX;
    /** Vị trí X ban đầu, dùng làm mốc (anchor) để tính toán {@code moveRange}. */
    private int originalX;
    /** Phạm vi di chuyển tối đa (tính bằng pixel) so với {@code originalX}. */
    private int moveRange;

    /**
     * Khởi tạo một Gạch Di Chuyển mới.
     *
     * @param x Vị trí X ban đầu (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     * @param speedX Vận tốc di chuyển ngang ban đầu.
     * @param moveRange Phạm vi di chuyển tối đa (pixel) so với vị trí {@code x} ban đầu.
     */
    public MovingBrick(int x, int y, int width, int height, int speedX, int moveRange) {
        // Gọi constructor của Brick, mặc định 2 máu và dùng ảnh "normalBrick"
        super(x, y, width, height, 2, "moving", "normalBrick");
        this.speedX = speedX;
        this.originalX = x;
        this.moveRange = moveRange;
    }

    /**
     * Phương thức này ghi đè (override) phương thức trừu tượng của {@link Brick}
     * nhưng bị bỏ trống.
     * <p>
     * Logic cập nhật thực tế nằm trong {@link #update(List)}
     * (được nạp chồng - overload) vì nó cần danh sách gạch để kiểm tra va chạm.
     */
    @Override public void update() {}

    /**
     * Cập nhật logic di chuyển ngang.
     * Xử lý đổi hướng khi va chạm tường, vượt quá phạm vi, hoặc va chạm gạch khác.
     *
     * @param brickList Danh sách *tất cả* các gạch khác trong level để kiểm tra va chạm.
     */
    public void update(List<Brick> brickList) {
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        // 1. Cập nhật vị trí
        this.x += this.speedX;

        // 2. Kiểm tra nếu chạm tường bên trái hoặc bên phải, đổi hướng
        if (this.x <= 0 || this.x + this.width >= gameAreaWidth) {
            this.speedX = -this.speedX; // Đảo chiều
            this.x += this.speedX; // Điều chỉnh vị trí 1 lần nữa để tránh kẹt
        }

        // 3. Kiểm tra nếu vượt quá phạm vi di chuyển, đổi hướng
        // (So sánh khoảng cách hiện tại với vị trí gốc)
        if (Math.abs(this.x - this.originalX) >= this.moveRange) {
            this.speedX = -this.speedX;
            this.x += this.speedX;
        }

        // 4. Kiểm tra va chạm với các viên gạch khác và đảo chiều nếu cần
        for (Brick other : brickList) {
            // Điều kiện va chạm:
            // - không phải là chính nó
            // - có chồng lấn (overlap) theo trục X
            // - phải ở trên CÙNG MỘT HÀNG (y == y)
            if (other != this && this.x < other.getX() + other.getWidth() && this.x + this.width > other.getX() && this.y == other.getY()){
                this.speedX = -this.speedX;
                this.x += this.speedX;
                break; // Chỉ xử lý 1 va chạm mỗi frame
            }
        }
    }

    /**
     * Vẽ gạch tại vị trí đã cập nhật.
     * Sử dụng {@code imageName} được kế thừa (mặc định là "normalBrick").
     */
    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        }
    }
}
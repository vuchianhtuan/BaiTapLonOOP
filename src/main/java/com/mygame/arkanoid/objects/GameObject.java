package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;

/**
 * Lớp trừu tượng (abstract) cơ sở cho tất cả các vật thể
 * tồn tại trong thế giới game (ví dụ: Ball, Paddle, Brick, PowerUp).
 * <p>
 * Cung cấp các thuộc tính cơ bản về vị trí (x, y),
 * kích thước (width, height), và một hình chữ nhật bao (bounding box)
 * để kiểm tra va chạm ({@code getBounds()}).
 */
public abstract class GameObject {
    /** Vị trí X (tọa độ logic) của góc trên bên trái vật thể. */
    protected int x, y;
    /** Kích thước (tọa độ logic) của vật thể. */
    protected int width, height;

    /**
     * Khởi tạo một GameObject mới.
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Phương thức trừu tượng để cập nhật logic của vật thể (được gọi mỗi frame).
     * Lớp con phải triển khai (implement) hành vi này (ví dụ: di chuyển, đếm ngược).
     */
    public abstract void update();

    /**
     * Phương thức trừu tượng để vẽ (render) vật thể lên màn hình.
     * Lớp con phải triển khai (implement) logic vẽ này.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager) để tính toán tọa độ.
     */
    public abstract void render(Graphics g, ScalingManager sm);

    // --- Getters and Setters ---
    // (Bỏ qua Javadoc cho các phương thức getter/setter đơn giản)

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    /**
     * Lấy hình chữ nhật bao (bounding box) của vật thể,
     * dùng để kiểm tra va chạm (collision detection).
     *
     * @return Một đối tượng {@link Rectangle} mới đại diện cho vị trí và kích thước.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
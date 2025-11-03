package com.mygame.arkanoid.objects;

/**
 * Lớp trừu tượng đại diện cho một {@link GameObject} có khả năng di chuyển.
 * <p>
 * Kế thừa từ {@link GameObject}, lớp này bổ sung thêm các thành phần
 * vận tốc ({@code dx}, {@code dy}) và một phương thức trừu tượng
 * {@link #move()} để xử lý logic di chuyển.
 */
public abstract class MovableObject extends GameObject {
    /** Vận tốc (delta) theo trục X. */
    protected double dx;
    /** Vận tốc (delta) theo trục Y. */
    protected double dy;

    /**
     * Phương thức trừu tượng (abstract) để cập nhật vị trí
     * ({@code x}, {@code y}) của vật thể dựa trên vận tốc
     * ({@code dx}, {@code dy}) và các quy tắc vật lý (ví dụ: va chạm tường).
     */
    public abstract void move();

    /**
     * Khởi tạo một MovableObject mới.
     * Vận tốc ban đầu (dx, dy) được đặt là 0.
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public MovableObject(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.dx = 0; // Vận tốc ban đầu bằng 0
        this.dy = 0;
    }

    public double getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }
}
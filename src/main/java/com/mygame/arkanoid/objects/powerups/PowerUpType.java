package com.mygame.arkanoid.objects.powerups;

/**
 * Định nghĩa (liệt kê) các loại vật phẩm tăng sức mạnh (PowerUp)
 * có thể xuất hiện trong trò chơi.
 * <p>
 * Enum này được sử dụng bởi {@link com.mygame.arkanoid.level.Level}
 * để định cấu hình tỷ lệ rơi (drop rate) và bởi {@link PowerUpFactory}
 * để tạo (instantiate) đối tượng PowerUp tương ứng.
 */
public enum PowerUpType {
    /** Mở rộng thanh đỡ (Paddle). */
    EXPAND,
    /** Thêm 1 mạng (life) cho người chơi. */
    EXTRA_LIFE,
    /** Tăng tốc độ của bóng. */
    FAST_BALL,
    /** Nhân bản bóng hiện tại thành nhiều bóng. */
    MULTI_BALL,
    /** Giảm tốc độ của bóng. */
    SLOW_BALL,
    /** Làm cho bóng dính vào thanh đỡ (Paddle) khi va chạm. */
    STICKY
}
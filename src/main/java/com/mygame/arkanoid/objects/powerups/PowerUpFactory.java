package com.mygame.arkanoid.objects.powerups;

/**
 * Lớp tiện ích (utility class) hoạt động theo mẫu Factory (Nhà máy).
 * <p>
 * Chịu trách nhiệm "đóng gói" logic tạo (instantiation) các đối tượng
 * {@link PowerUp} cụ thể (ví dụ: {@code ExpandPaddlePowerUp})
 * dựa trên một {@link PowerUpType} enum.
 * <p>
 * Lớp này là {@code final} và có constructor {@code private}
 * để ngăn chặn việc tạo đối tượng.
 */
public final class PowerUpFactory {

    /** Constructor private để ngăn chặn việc tạo đối tượng (instantiation). */
    private PowerUpFactory() {}

    /**
     * Phương thức factory chính: Tạo một đối tượng PowerUp cụ thể.
     * <p>
     * Dựa trên {@code type} được cung cấp, phương thức này sẽ
     * khởi tạo và trả về một lớp con (subclass) tương ứng của {@link PowerUp}.
     * Kích thước (width, height) của mỗi PowerUp được gán cứng (hardcoded)
     * tại đây, dựa trên kích thước sprite (hình ảnh) của chúng.
     *
     * @param type Loại PowerUp (enum) cần tạo (ví dụ: {@code PowerUpType.EXPAND}).
     * @param x Vị trí X (logic) ban đầu (thường là từ gạch vỡ).
     * @param y Vị trí Y (logic) ban đầu.
     * @return Một đối tượng PowerUp mới (ví dụ: {@code new ExpandPaddlePowerUp(...)}).
     * @throws IllegalArgumentException Nếu {@code type} không được hỗ trợ trong {@code switch}.
     */
    public static PowerUp create(PowerUpType type, int x, int y) {
        switch (type) {
            case EXPAND: return new ExpandPaddlePowerUp(x, y, 63, 30);
            case STICKY: return new StickyPaddlePowerUp(x, y, 59, 30);
            case SLOW_BALL: return new SlowBallPowerUp(x, y, 63, 30);
            case FAST_BALL: return new FastBallPowerUp(x, y, 63, 30);
            case EXTRA_LIFE: return new ExtraLifePowerUp(x, y, 30, 30);
            case MULTI_BALL: return new MultiBallPowerUp(x, y, 61, 30);
            default:
                // Ném ra lỗi nếu một PowerUpType mới được thêm vào enum
                // mà chưa được cập nhật trong factory này.
                throw new IllegalArgumentException("Unsupported PowerUpType: " + type);
        }
    }
}
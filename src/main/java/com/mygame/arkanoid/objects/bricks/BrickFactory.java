package com.mygame.arkanoid.objects.bricks;

/**
 * Lớp tiện ích (utility class) hoạt động theo mẫu Factory.
 * Chịu trách nhiệm tạo (instantiate) các đối tượng {@link Brick}
 * (hoặc các lớp con của nó) dựa trên một ký tự (char) đại diện
 * (thường được đọc từ tệp level).
 * <p>
 * Lớp này là {@code final} và có constructor {@code private}
 * để ngăn chặn việc tạo đối tượng.
 */
public final class BrickFactory {
    // Kích thước mặc định cho gạch
    private static final int DEFAULT_WIDTH = 45;
    private static final int DEFAULT_HEIGHT = 20;

    // Các giá trị mặc định cho các loại gạch đặc biệt
    private static final int DEFAULT_STRONG_HP = 3;
    private static final int DEFAULT_MOVING_SPEED = 2;
    private static final int DEFAULT_SHOOTER_FIRE_RATE = 5;

    /** Constructor private để ngăn chặn việc tạo đối tượng (instantiation). */
    private BrickFactory() {}

    /**
     * Phương thức tiện lợi (convenience method) để tạo gạch với kích thước mặc định.
     * Gọi đến phương thức `create` đầy đủ.
     *
     * @param type Ký tự đại diện loại gạch.
     * @param x Tọa độ X logic.
     * @param y Tọa độ Y logic.
     * @return Một đối tượng {@link Brick}, hoặc {@code null} nếu ký tự là rỗng.
     */
    public static Brick create(char type, int x, int y) {
        return create(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, type);
    }

    /**
     * Phương thức factory chính: Tạo một loại gạch cụ thể dựa trên ký tự.
     *
     * @param x Tọa độ X logic.
     * @param y Tọa độ Y logic.
     * @param width Chiều rộng gạch.
     * @param height Chiều cao gạch.
     * @param type Ký tự đại diện loại gạch.
     * @return Một đối tượng {@link Brick}, hoặc {@code null} nếu ký tự không được hỗ trợ.
     */
    public static Brick create(int x, int y, int width, int height, char type) {
        switch (type) {
            // --- Gạch thường ---
            case '1': return new NormalBrick(x, y, width, height);
            case '2': return new StrongBrick(x, y, width, height, 2); // Gạch 2 máu
            case 'E': return new ExplosiveBrick(x, y, width, height);
            case 'M': return new MovingBrick(x, y, width, height, DEFAULT_MOVING_SPEED, 200);
            case 'L': return new LaserShooterBrick(x, y, width, height, 3);

            // --- Gạch của Boss (có thể giống hoặc khác gạch thường) ---
            case 'B': return new NormalBrick(x, y, width, height); // Gạch thường của Boss
            case 'T': return new StrongBrick(x, y, width, height, 3); // Gạch cứng của Boss (3 máu)
            case 'C': return new LaserShooterBrick(x, y, width, height, 5); // Gạch bắn laser của Boss

            // --- Ký tự rỗng (Empty space) ---
            case '_': // Dùng cho dễ nhìn trong file level
            case '0': // Dùng cho dễ nhìn
            default: throw new IllegalArgumentException("Không hỗ trợ BrickType: " + type);
        }
    }
}
package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.objects.Laser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp kiểm thử (test class) JUnit 5 dành cho {@link LaserShooterBrick}.
 * <p>
 * Tập trung vào việc xác minh logic bắn (shooting)
 * và cơ chế hồi chiêu (cooldown) của gạch.
 */
@DisplayName("Kiểm tra logic Gạch Bắn Laze")
class LaserShooterBrickTest {

    /** Đối tượng gạch bắn laze (SUT - System Under Test) cho mỗi kiểm thử. */
    private LaserShooterBrick brick;
    /**
     * Bản sao (local copy) của hằng số thời gian hồi chiêu
     * (từ code game) để sử dụng trong logic kiểm thử.
     */
    private static final int SHOOT_INTERVAL = 180;

    /**
     * Thiết lập (setup) chạy trước *mỗi* phương thức {@code @Test}.
     * <p>
     * Khởi tạo một đối tượng {@code LaserShooterBrick} mới
     * để đảm bảo mỗi kiểm thử là độc lập (isolated).
     */
    @BeforeEach
    void setUp() {
        // Tạo gạch, máu 3
        brick = new LaserShooterBrick(100, 100, 45, 20, 3);
        // (Code gốc của gạch có cooldown ngẫu nhiên khi khởi tạo)
        // Các bài test sẽ mô phỏng (simulate) thời gian trôi qua
        // bằng cách gọi update() nhiều lần.
    }

    /**
     * Kiểm tra rằng gạch KHÔNG bắn khi đang trong thời gian hồi chiêu
     * (cooldown).
     * <p>
     * Do gạch mới tạo có thời gian hồi chiêu ngẫu nhiên (lớn hơn 0),
     * việc gọi `tryToShoot` ngay lập tức (sau 1 update)
     * phải luôn trả về `null`.
     */
    @Test
    @DisplayName("Gạch KHÔNG bắn khi đang hồi chiêu")
    void testDoesNotShootOnCooldown() {
        // --- Sắp xếp (Arrange) ---
        // Khi mới tạo, gạch có cooldown ngẫu nhiên > 0 (theo code của bạn)
        // Chúng ta gọi update 1 lần
        brick.update();

        // --- Hành động (Act) ---
        Laser laser = brick.tryToShoot(); // Cố gắng bắn

        // --- Xác minh (Assert) ---
        assertNull(laser, "Gạch không được bắn khi đang trong thời gian hồi chiêu");
    }

    /**
     * Kiểm tra rằng gạch BẮN ra laze sau khi thời gian hồi chiêu kết thúc.
     * <p>
     * <b>Logic:</b>
     * <ol>
     * <li>Mô phỏng (simulate) thời gian trôi qua bằng cách gọi
     * `update()` (SHOOT_INTERVAL + 1) lần để đảm bảo cooldown về 0.</li>
     * <li>Kiểm tra rằng `tryToShoot()` trả về một đối tượng {@link Laser}.</li>
     * <li>Kiểm tra rằng `tryToShoot()` lần thứ hai ngay lập tức
     * trả về `null` (xác nhận cooldown đã được reset).</li>
     * </ol>
     */
    @Test
    @DisplayName("Gạch BẮN ra laze khi hết hồi chiêu")
    void testShootsWhenCooldownFinishes() {
        // --- Sắp xếp (Arrange) ---
        // Gọi update() đủ số lần để đảm bảo cooldown về 0
        // (SHOOT_INTERVAL + 1 để chắc chắn)
        for (int i = 0; i < SHOOT_INTERVAL + 1; i++) {
            brick.update();
        }

        // --- Hành động (Act) ---
        Laser laser = brick.tryToShoot(); // Cố gắng bắn

        // --- Xác minh (Assert) ---
        assertNotNull(laser, "Gạch PHẢI bắn ra laze sau khi hết thời gian hồi chiêu");

        // Kiểm tra xem cooldown đã được reset chưa
        Laser laser2 = brick.tryToShoot(); // Cố bắn lần nữa
        assertNull(laser2, "Gạch không được bắn 2 lần liên tiếp (phải reset cooldown)");
    }
}
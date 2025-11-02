package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.objects.Laser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm tra logic Gạch Bắn Laze")
class LaserShooterBrickTest {

    private LaserShooterBrick brick;
    // Lấy hằng số thời gian hồi chiêu từ code game (180 frames)
    private static final int SHOOT_INTERVAL = 180;

    @BeforeEach
    void setUp() {
        // Tạo gạch, máu 3
        brick = new LaserShooterBrick(100, 100, 45, 20, 3);
        // Giả lập gạch đã sẵn sàng bắn ngay (cooldown = 0)
        // (Code gốc của bạn là ngẫu nhiên, chúng ta cần ép nó về 0)
        // Chúng ta sẽ cần thêm 1 hàm setter, hoặc sửa lại test.

        // Tạm thời, chúng ta sẽ test bằng cách gọi update() 180 lần.
    }

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
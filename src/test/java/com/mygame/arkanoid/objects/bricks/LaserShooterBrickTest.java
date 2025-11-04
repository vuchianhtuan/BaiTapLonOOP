package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.objects.Laser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm tra logic Gạch Bắn Laze")
class LaserShooterBrickTest {

    private LaserShooterBrick brick;
    // Thời gian hồi chiêu (180 frames)
    private static final int SHOOT_INTERVAL = 180;

    @BeforeEach
    void setUp() {
        // Tạo gạch, máu 3
        brick = new LaserShooterBrick(100, 100, 45, 20, 3);
        // Khi mới tạo, gạch có cooldown ngẫu nhiên.
    }

    @Test
    @DisplayName("Gạch KHÔNG bắn khi đang hồi chiêu")
    void testDoesNotShootOnCooldown() {
        // Khi mới tạo, gạch đang hồi chiêu (cooldown > 0)
        brick.update(); // Cập nhật 1 frame

        // Cố gắng bắn
        Laser laser = brick.tryToShoot();

        // Gạch không được bắn khi đang hồi chiêu
        assertNull(laser, "Gạch không được bắn khi đang trong thời gian hồi chiêu");
    }

    @Test
    @DisplayName("Gạch BẮN ra laze khi hết hồi chiêu")
    void testShootsWhenCooldownFinishes() {
        // Tua nhanh update() để đảm bảo cooldown về 0
        // (SHOOT_INTERVAL + 1 để chắc chắn)
        for (int i = 0; i < SHOOT_INTERVAL + 1; i++) {
            brick.update();
        }

        // Cố gắng bắn
        Laser laser = brick.tryToShoot();

        // Gạch PHẢI bắn ra laze sau khi hết hồi chiêu
        assertNotNull(laser, "Gạch PHẢI bắn ra laze sau khi hết thời gian hồi chiêu");

        // Ngay sau khi bắn, gạch phải bắt đầu hồi chiêu lại
        Laser laser2 = brick.tryToShoot(); // Cố bắn lần nữa ngay lập tức
        assertNull(laser2, "Gạch không được bắn 2 lần liên tiếp (phải reset cooldown)");
    }
}
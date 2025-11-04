package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.NormalBrick;
import com.mygame.arkanoid.systems.ScalingManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm tra logic của Trùm (Boss)")
class BossTest {

    private Boss boss;
    private Brick bossBrick1;
    private Brick bossBrick2;

    private static int GAME_AREA_WIDTH;

    // Khởi tạo ScalingManager (Cấp 2 Test)
    @BeforeAll
    static void setupGlobal() {
        ScalingManager sm = ScalingManager.getInstance();
        sm.update(1120, 720);
        GAME_AREA_WIDTH = sm.GAME_AREA_WIDTH;
    }

    @BeforeEach
    void setUp() {
        bossBrick1 = new NormalBrick(10, 50, 45, 20);
        bossBrick2 = new NormalBrick(60, 50, 45, 20);

        List<Brick> bossBricks = new ArrayList<>(List.of(bossBrick1, bossBrick2));

        // Tính toán vị trí gốc của boss
        Rectangle bossBounds = new Rectangle(10, 50, (60+45)-10, 20);
        float startX = (GAME_AREA_WIDTH / 2.0f) - (bossBounds.width / 2.0f);

        // Tạo Boss, truyền vào gạch và vị trí gốc
        boss = new Boss(bossBricks, startX, 50, bossBounds.x, GAME_AREA_WIDTH);

        // Cập nhật 1 lần để gạch vào đúng vị trí
        boss.update();
    }

    @Test
    @DisplayName("Boss và gạch di chuyển sang phải khi mới tạo")
    void testBossMovesRightInitially() {
        // Lấy vị trí X của gạch 1 sau khi setup
        int initialX = bossBrick1.getX();
        boss.update();
        assertTrue(bossBrick1.getX() > initialX, "Gạch của Boss phải di chuyển sang phải (X tăng)");
    }

    @Test
    @DisplayName("Boss đổi hướng khi chạm tường phải")
    void testBossReversesAtRightWall() {
        int x_snapshot1 = bossBrick1.getX();
        for (int i = 0; i < 500; i++) {
            boss.update();
        }
        int x_snapshot2 = bossBrick1.getX();
        assertTrue(x_snapshot2 < x_snapshot1, "Boss đáng lẽ phải quay lại (X giảm) sau khi chạm tường phải");
    }

    @Test
    @DisplayName("Boss bị đánh bại (isDefeated) khi hết gạch")
    void testIsDefeated() {
        assertFalse(boss.isDefeated(), "Boss không được bị đánh bại khi mới tạo");
        bossBrick1.takeHit();
        bossBrick2.takeHit();
        boss.removeDestroyedBricks();
        assertEquals(0, boss.getBricks().size(), "Danh sách gạch của Boss phải rỗng");
        assertTrue(boss.isDefeated(), "Boss PHẢI bị đánh bại khi hết gạch");
    }
}
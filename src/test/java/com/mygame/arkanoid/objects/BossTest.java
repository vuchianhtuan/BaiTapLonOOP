package com.mygame.arkanoid.objects;

// Import thư viện JUnit
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
    private Brick bossBrick1; // Gạch bên trái
    private Brick bossBrick2; // Gạch bên phải

    private static int GAME_AREA_WIDTH;

    // Khởi tạo ScalingManager (Cấp 2 Test)
    @BeforeAll
    static void setupGlobal() {
        ScalingManager sm = ScalingManager.getInstance();
        sm.update(1120, 720);
        GAME_AREA_WIDTH = sm.GAME_AREA_WIDTH; // Sẽ là 960
    }

    @BeforeEach
    void setUp() {
        // --- SẮP XẾP (Arrange) ---
        // Tạo các viên gạch "thật" cho boss
        // Gạch 1 ở tọa độ gốc (10, 50)
        bossBrick1 = new NormalBrick(10, 50, 45, 20);
        // Gạch 2 ở tọa độ gốc (60, 50)
        bossBrick2 = new NormalBrick(60, 50, 45, 20);

        List<Brick> bossBricks = new ArrayList<>(List.of(bossBrick1, bossBrick2));

        // Tính toán vị trí gốc của boss
        // originalMinX = 10, startY = 50
        // Boss sẽ được tạo ở vị trí X = (960 - (105-10))/2 = 432.5 (làm tròn 432)
        // (Logic này dựa trên code GameManager của bạn)
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

        // --- HÀNH ĐỘNG (Act) ---
        boss.update(); // Gọi di chuyển lần nữa

        // --- XÁC MINH (Assert) ---
        // (Vì dx ban đầu = 2.0f)
        assertTrue(bossBrick1.getX() > initialX, "Gạch của Boss phải di chuyển sang phải (X tăng)");
    }

    @Test
    @DisplayName("Boss đổi hướng khi chạm tường phải")
    void testBossReversesAtRightWall() {
        // Lấy vị trí X của gạch 1
        int x_snapshot1 = bossBrick1.getX();

        // --- HÀNH ĐỘNG (Act) ---
        // Chạy update 500 lần để đảm bảo nó chạm tường phải và quay lại
        for (int i = 0; i < 500; i++) {
            boss.update();
        }

        // --- XÁC MINH (Assert) ---
        // Sau 500 lần, nó chắc chắn đã quay lại
        int x_snapshot2 = bossBrick1.getX();
        assertTrue(x_snapshot2 < x_snapshot1, "Boss đáng lẽ phải quay lại (X giảm) sau khi chạm tường phải");
    }

    @Test
    @DisplayName("Boss bị đánh bại (isDefeated) khi hết gạch")
    void testIsDefeated() {
        // --- Sắp xếp (Arrange) ---
        assertFalse(boss.isDefeated(), "Boss không được bị đánh bại khi mới tạo");

        // --- HÀNH ĐỘNG (Act) ---
        // Phá hủy tất cả gạch
        bossBrick1.takeHit(); // 1 HP
        bossBrick2.takeHit(); // 1 HP

        // Gọi hàm dọn dẹp
        boss.removeDestroyedBricks();

        // --- XÁC MINH (Assert) ---
        assertEquals(0, boss.getBricks().size(), "Danh sách gạch của Boss phải rỗng");
        assertTrue(boss.isDefeated(), "Boss PHẢI bị đánh bại khi hết gạch");
    }
}
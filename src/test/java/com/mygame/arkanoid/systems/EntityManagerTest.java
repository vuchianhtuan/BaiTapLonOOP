package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.objects.Ball;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.NormalBrick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm tra Trình Quản lý Thực thể (EntityManager)")
class EntityManagerTest {

    private EntityManager entityManager;
    private Ball ball_in_bounds;
    private Ball ball_out_of_bounds;
    private Brick brick_alive;
    private Brick brick_destroyed;

    private final int SCREEN_HEIGHT = 720;

    @BeforeEach
    void setUp() {
        entityManager = new EntityManager();

        ball_in_bounds = new Ball(100, 100, 18, 18, "skin");
        ball_out_of_bounds = new Ball(100, 9999, 18, 18, "skin"); // Y > 720

        entityManager.addBall(ball_in_bounds);
        entityManager.addBall(ball_out_of_bounds);

        brick_alive = new NormalBrick(200, 200, 45, 20);
        brick_destroyed = new NormalBrick(300, 300, 45, 20);
        brick_destroyed.takeHit();

        entityManager.getBricks().add(brick_alive);
        entityManager.getBricks().add(brick_destroyed);
    }

    @Test
    @DisplayName("Dọn dẹp (cleanup) các đối tượng đã bị phá hủy")
    void testCleanupDestroyedObjects() {
        assertEquals(2, entityManager.getBalls().size(), "Phải có 2 quả bóng trước khi dọn dẹp");
        assertEquals(2, entityManager.getBricks().size(), "Phải có 2 viên gạch trước khi dọn dẹp");

        entityManager.cleanupDestroyedObjects(SCREEN_HEIGHT);

        assertEquals(1, entityManager.getBalls().size(), "Chỉ 1 quả bóng được giữ lại");
        assertTrue(entityManager.getBalls().contains(ball_in_bounds), "Bóng trong màn hình phải được giữ lại");
        assertFalse(entityManager.getBalls().contains(ball_out_of_bounds), "Bóng ngoài màn hình phải bị xóa");

        assertEquals(1, entityManager.getBricks().size(), "Chỉ 1 viên gạch được giữ lại");
        assertTrue(entityManager.getBricks().contains(brick_alive), "Gạch còn sống phải được giữ lại");
        assertFalse(entityManager.getBricks().contains(brick_destroyed), "Gạch đã vỡ phải bị xóa");
    }
}
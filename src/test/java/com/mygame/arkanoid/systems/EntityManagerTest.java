package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.objects.Ball;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.NormalBrick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp kiểm thử (test class) JUnit 5 dành cho {@link EntityManager}.
 * <p>
 * Lớp này tập trung vào việc xác minh logic "dọn dẹp" (cleanup)
 * của trình quản lý, đặc biệt là phương thức
 * {@link EntityManager#cleanupDestroyedObjects(int)}.
 */
@DisplayName("Kiểm tra Trình Quản lý Thực thể (EntityManager)")
class EntityManagerTest {

    /** Đối tượng EntityManager (SUT - System Under Test) cho mỗi kiểm thử. */
    private EntityManager entityManager;
    /** Dữ liệu kiểm thử: Một quả bóng "thật" nằm trong biên màn hình (cần được giữ lại). */
    private Ball ball_in_bounds;
    /** Dữ liệu kiểm thử: Một quả bóng "thật" nằm ngoài biên màn hình (cần bị dọn dẹp). */
    private Ball ball_out_of_bounds;
    /** Dữ liệu kiểm thử: Một viên gạch "thật" còn sống (cần được giữ lại). */
    private Brick brick_alive;
    /** Dữ liệu kiểm thử: Một viên gạch "thật" đã bị phá hủy (cần bị dọn dẹp). */
    private Brick brick_destroyed;

    /** Hằng số (constant) giả lập chiều cao màn hình để kiểm tra biên (va chạm đáy). */
    private final int SCREEN_HEIGHT = 720;

    /**
     * Thiết lập (setup) chạy trước *mỗi* phương thức {@code @Test}.
     * <p>
     * Khởi tạo một {@code EntityManager} "sạch" và "dàn cảnh" (stage)
     * bằng cách thêm vào đó các đối tượng "thật" (2 bóng, 2 gạch)
     * với các trạng thái (sống/chết, trong/ngoài biên) được định sẵn
     * để phục vụ cho việc kiểm thử (Arrange).
     */
    @BeforeEach
    void setUp() {
        // --- SẮP XẾP (Arrange) ---
        entityManager = new EntityManager();

        // 1. Tạo các quả bóng
        ball_in_bounds = new Ball(100, 100, 18, 18, "skin");
        ball_out_of_bounds = new Ball(100, 9999, 18, 18, "skin"); // Y > 720

        entityManager.addBall(ball_in_bounds);
        entityManager.addBall(ball_out_of_bounds);

        // 2. Tạo các viên gạch
        brick_alive = new NormalBrick(200, 200, 45, 20);
        brick_destroyed = new NormalBrick(300, 300, 45, 20);
        brick_destroyed.takeHit(); // Làm cho nó bị vỡ (isDestroyed() == true)

        entityManager.getBricks().add(brick_alive);
        entityManager.getBricks().add(brick_destroyed);
    }

    /**
     * Kiểm tra kịch bản (scenario) của hàm {@link EntityManager#cleanupDestroyedObjects(int)}.
     * <p>
     * <b>Xác minh (Assert):</b>
     * <ul>
     * <li>Bóng đã ra khỏi màn hình ({@code ball_out_of_bounds}) phải bị xóa.</li>
     * <li>Gạch đã bị phá hủy ({@code brick_destroyed}) phải bị xóa.</li>
     * <li>Bóng còn trong màn hình ({@code ball_in_bounds}) phải được giữ lại.</li>
     * <li>Gạch còn sống ({@code brick_alive}) phải được giữ lại.</li>
     * </ul>
     */
    @Test
    @DisplayName("Dọn dẹp (cleanup) các đối tượng đã bị phá hủy")
    void testCleanupDestroyedObjects() {
        // --- Kiểm tra trạng thái ban đầu (Pre-condition Assertions) ---
        assertEquals(2, entityManager.getBalls().size(), "Phải có 2 quả bóng trước khi dọn dẹp");
        assertEquals(2, entityManager.getBricks().size(), "Phải có 2 viên gạch trước khi dọn dẹp");

        // --- HÀNH ĐỘNG (Act) ---
        // Chạy hàm dọn dẹp, truyền vào chiều cao màn hình
        entityManager.cleanupDestroyedObjects(SCREEN_HEIGHT);

        // --- XÁC MINH (Assert) (Post-condition Assertions) ---

        // 1. Kiểm tra List Bóng
        assertEquals(1, entityManager.getBalls().size(), "Chỉ 1 quả bóng được giữ lại");
        assertTrue(entityManager.getBalls().contains(ball_in_bounds), "Bóng trong màn hình phải được giữ lại");
        assertFalse(entityManager.getBalls().contains(ball_out_of_bounds), "Bóng ngoài màn hình phải bị xóa");

        // 2. Kiểm tra List Gạch
        assertEquals(1, entityManager.getBricks().size(), "Chỉ 1 viên gạch được giữ lại");
        assertTrue(entityManager.getBricks().contains(brick_alive), "Gạch còn sống phải được giữ lại");
        assertFalse(entityManager.getBricks().contains(brick_destroyed), "Gạch đã vỡ phải bị xóa");
    }
}
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

/**
 * Lớp kiểm thử (test class) JUnit 5 dành cho {@link Boss}.
 * <p>
 * Lớp này xác minh các hành vi cốt lõi của Boss:
 * <ul>
 * <li>Logic di chuyển ngang (movement).</li>
 * <li>Logic đổi hướng khi va chạm tường (wall collision).</li>
 * <li>Logic bị đánh bại (defeat condition) khi hết gạch.</li>
 * </ul>
 */
@DisplayName("Kiểm tra logic của Trùm (Boss)")
class BossTest {

    /** Đối tượng Boss (SUT - System Under Test) cho mỗi kiểm thử. */
    private Boss boss;
    /** Gạch "con" (component) bên trái, dùng để kiểm tra vị trí. */
    private Brick bossBrick1;
    /** Gạch "con" (component) bên phải. */
    private Brick bossBrick2;

    /** Bản sao (local copy) của chiều rộng khu vực chơi game (lấy từ ScalingManager). */
    private static int GAME_AREA_WIDTH;

    /**
     * Thiết lập (setup) toàn cục, chạy một lần duy nhất trước tất cả các test.
     * <p>
     * <b>Quan trọng:</b> Logic của {@link Boss#update()} phụ thuộc vào
     * {@link ScalingManager} để biết chiều rộng màn hình ({@code GAME_AREA_WIDTH}).
     * Do đó, chúng ta phải 'giả lập' (initialize) ScalingManager
     * trước khi bất kỳ test nào chạy để tránh {@code NullPointerException}.
     */
    @BeforeAll
    static void setupGlobal() {
        ScalingManager sm = ScalingManager.getInstance();
        sm.update(1120, 720); // Giả lập kích thước cửa sổ gốc
        GAME_AREA_WIDTH = sm.GAME_AREA_WIDTH; // Sẽ là 960
    }

    /**
     * Thiết lập (setup) chạy trước *mỗi* phương thức {@code @Test}.
     * <p>
     * Tạo ra một kịch bản (scenario) 'sạch' bằng cách:
     * <ol>
     * <li>Tạo các gạch 'con' (bricks) thật.</li>
     * <li>Tính toán vị trí bắt đầu (căn giữa) cho Boss,
     * mô phỏng logic của {@code GameManager} hoặc {@code EntityManager}.</li>
     * <li>Khởi tạo đối tượng {@link Boss} mới.</li>
     * <li>Gọi {@code update()} một lần để các gạch con
     * di chuyển vào đúng vị trí ban đầu (đồng bộ với điểm neo của Boss).</li>
     * </ol>
     */
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
        // (Logic này mô phỏng theo code hydrateLevel của EntityManager)
        Rectangle bossBounds = new Rectangle(10, 50, (60+45)-10, 20); // bounds gốc
        float startX = (GAME_AREA_WIDTH / 2.0f) - (bossBounds.width / 2.0f); // Căn giữa

        // Tạo Boss, truyền vào gạch và vị trí gốc
        boss = new Boss(bossBricks, startX, 50, bossBounds.x, GAME_AREA_WIDTH);

        // Cập nhật 1 lần để gạch vào đúng vị trí
        boss.update();
    }

    /**
     * Kiểm tra rằng Boss (và các gạch con) di chuyển sang phải
     * (theo vận tốc {@code dx} mặc định = 2.0f) ngay sau khi được tạo.
     */
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

    /**
     * Kiểm tra logic đổi hướng (đảo ngược {@code dx})
     * khi Boss chạm vào tường bên phải.
     * <p>
     * Mô phỏng (simulate) bằng cách chạy {@code update()} lặp đi lặp lại
     * (500 lần) để đảm bảo nó đã chạm tường phải và quay đầu lại.
     */
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
        // Sau 500 lần lặp, nó chắc chắn đã va chạm và đổi hướng
        int x_snapshot2 = bossBrick1.getX();
        assertTrue(x_snapshot2 < x_snapshot1, "Boss đáng lẽ phải quay lại (X giảm) sau khi chạm tường phải");
    }

    /**
     * Kiểm tra rằng trạng thái {@link Boss#isDefeated()}
     * chuyển sang {@code true} chỉ sau khi tất cả các gạch con (bricks)
     * đã bị phá hủy ({@code takeHit()}) VÀ
     * hàm dọn dẹp ({@link Boss#removeDestroyedBricks()}) đã được gọi.
     */
    @Test
    @DisplayName("Boss bị đánh bại (isDefeated) khi hết gạch")
    void testIsDefeated() {
        // --- Sắp xếp (Arrange) ---
        assertFalse(boss.isDefeated(), "Boss không được bị đánh bại khi mới tạo");

        // --- HÀNH ĐỘNG (Act) ---
        // Phá hủy tất cả gạch (chúng có 1 HP)
        bossBrick1.takeHit();
        bossBrick2.takeHit();

        // Gọi hàm dọn dẹp
        boss.removeDestroyedBricks();

        // --- XÁC MINH (Assert) ---
        assertEquals(0, boss.getBricks().size(), "Danh sách gạch của Boss phải rỗng");
        assertTrue(boss.isDefeated(), "Boss PHẢI bị đánh bại khi hết gạch");
    }
}
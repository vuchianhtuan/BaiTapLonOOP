package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.effects.Shard;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.ExplosiveBrick;
import com.mygame.arkanoid.objects.bricks.NormalBrick;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Lớp kiểm thử (test class) JUnit 5 dành cho {@link ExplosionSystem}.
 * <p>
 * Lớp này sử dụng Mockito để "giả lập" (mock) các trình quản lý (dependency)
 * như {@link GameManager}.
 * <p>
 * Mục đích là để kiểm thử logic nổ ({@code explode}) của {@code ExplosionSystem}
 * một cách độc lập (in isolation), xác minh rằng nó xử lý chính xác:
 * <ul>
 * <li>Sát thương (damage) lên gạch thường trong bán kính.</li>
 * <li>Kích hoạt nổ dây chuyền (chain reaction)
 * lên gạch nổ khác trong bán kính.</li>
 * <li>Bỏ qua các gạch ngoài bán kính.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Bỏ qua các stub không cần thiết
@DisplayName("Kiểm tra Hệ thống Nổ (ExplosionSystem)")
class ExplosionSystemTest {

    /** Một {@link GameManager} giả lập (mock)
     * để theo dõi các lệnh gọi (ví dụ: addScore, getBricks). */
    @Mock
    private GameManager gameManager; // GameManager "giả"

    // --- Các đối tượng "Thật" (SUT và Test Data) ---

    /** Hệ thống Nổ (SUT - System Under Test) "thật" đang được kiểm thử. */
    private ExplosionSystem explosionSystem;
    /** Gạch nổ "thật" (nguồn) phát ra vụ nổ. */
    private ExplosiveBrick sourceBrick;
    /** Gạch nổ "thật" (mục tiêu) nằm trong tầm nổ, dùng để kiểm tra nổ lan. */
    private ExplosiveBrick targetBrickExplosive;
    /** Gạch thường "thật" (mục tiêu) nằm trong tầm nổ, dùng để kiểm tra sát thương. */
    private Brick targetBrickNormal;
    /** Gạch thường "thật" nằm ngoài tầm nổ, dùng để kiểm tra không bị ảnh hưởng. */
    private Brick outOfRangeBrick;

    /**
     * Thiết lập (setup) toàn cục, chạy MỘT LẦN DUY NHẤT trước tất cả các test.
     * <p>
     * <b>Quan trọng:</b> Cần khởi tạo các Singleton ({@link ScalingManager},
     * {@link AssetManager})
     * vì các đối tượng "thật" (như {@link ExplosiveBrick})
     * phụ thuộc vào chúng (ví dụ: để tải hoạt ảnh nổ).
     */
    @BeforeAll
    static void setupGlobal() {
        // Giả lập khởi động ScalingManager (cho các hàm tính toán nếu có)
        ScalingManager.getInstance().update(1120, 720);
        // Tải tài sản (cho constructor của ExplosiveBrick)
        AssetManager.getInstance().loadGlobalAssets();
    }

    /**
     * Thiết lập (setup) chạy trước *mỗi* phương thức {@code @Test}.
     * <p>
     * Dàn cảnh (Arrange) kịch bản kiểm thử bằng cách:
     * <ol>
     * <li>Khởi tạo SUT ({@code explosionSystem}).</li>
     * <li>Khởi tạo các đối tượng gạch "thật" (dữ liệu test)
     * với các vị trí được định sẵn (trong/ngoài tầm nổ).</li>
     * <li>Thiết lập (stub) {@code gameManager} "giả"
     * để trả về các danh sách gạch "thật" này khi được hỏi.</li>
     * </ol>
     */
    @BeforeEach
    void setUp() {
        // --- 1. Khởi tạo đối tượng cần test (Thật) ---
        explosionSystem = new ExplosionSystem();

        // --- 2. Khởi tạo các đối tượng "Thật" dùng cho kịch bản ---
        // Đặt tọa độ cho các gạch
        sourceBrick = new ExplosiveBrick(100, 100, 45, 20); // Tâm nổ ~ (122, 110)

        // Gạch nổ này ở (110, 100), cách gạch gốc ~10px -> TRONG TẦM NỔ
        targetBrickExplosive = new ExplosiveBrick(110, 100, 45, 20);

        // Gạch thường này ở (120, 100), cách gạch gốc ~20px -> TRONG TẦM NỔ
        targetBrickNormal = new NormalBrick(120, 100, 45, 20);

        // Gạch này ở (500, 500) -> NGOÀI TẦM NỔ
        outOfRangeBrick = new NormalBrick(500, 500, 45, 20);

        // --- 3. Cài đặt cho GameManager "Giả" ---

        // Tạo 1 list gạch "thật" mà GameManager sẽ quản lý
        List<Brick> allBricks = Arrays.asList(targetBrickExplosive, targetBrickNormal, outOfRangeBrick);

        // Dạy GameManager trả về list gạch này
        when(gameManager.getBricks()).thenReturn(allBricks);
        // Dạy GameManager trả về list rỗng (không có Gạch Laser)
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());
        // Dạy GameManager trả về 1 list Shard "thật" (để tránh NPE khi gạch vỡ)
        when(gameManager.getActiveShards()).thenReturn(new ArrayList<Shard>());
    }

    /**
     * Kiểm tra kịch bản (scenario) nổ lan (chain reaction)
     * và sát thương trong bán kính.
     * <p>
     * <b>Xác minh (Verify):</b>
     * <ul>
     * <li>Gạch nổ trong tầm ({@code targetBrickExplosive})
     * phải được "kích hoạt" ({@code isAwaitingDetonation() == true}).</li>
     * <li>Gạch thường trong tầm ({@code targetBrickNormal})
     * phải bị phá hủy ({@code isDestroyed() == true}).</li>
     * <li>Gạch ngoài tầm ({@code outOfRangeBrick})
     * phải còn nguyên ({@code isDestroyed() == false}).</li>
     * <li>{@code GameManager.addScore(10)}
     * phải được gọi 1 lần (cho gạch thường bị phá hủy).</li>
     * </ul>
     */
    @Test
    @DisplayName("Test Gạch nổ kích hoạt nổ lan (Chain Reaction)")
    void testExplosionChainReaction() {
        // --- 1. SẮP XẾP (Arrange) ---
        // Kiểm tra trạng thái ban đầu (pre-conditions)
        assertTrue(targetBrickExplosive.isAlive(), "Gạch nổ mục tiêu phải còn sống trước khi test");
        assertFalse(targetBrickNormal.isDestroyed(), "Gạch thường mục tiêu không được vỡ trước khi test");
        assertFalse(outOfRangeBrick.isDestroyed(), "Gạch ngoài tầm không được vỡ trước khi test");

        // --- 2. HÀNH ĐỘNG (Act) ---
        // Kích hoạt vụ nổ tại `sourceBrick` với bán kính 50px
        // (Bán kính 50px đủ để trúng gạch 10px và 20px)
        explosionSystem.explode(sourceBrick, 50.0, gameManager);

        // --- 3. XÁC MINH (Assert / Verify) ---

        // A. Xác minh Tác động (Assert state changes)

        // Gạch nổ mục tiêu (cách 10px) phải chuyển sang trạng thái "đang chờ nổ"
        assertTrue(targetBrickExplosive.isAwaitingDetonation(), "Gạch nổ mục tiêu đáng lẽ phải bị 'kích hoạt' (ignite)");

        // Gạch thường mục tiêu (cách 20px) phải bị phá hủy (vì 1 HP)
        assertTrue(targetBrickNormal.isDestroyed(), "Gạch thường mục tiêu đáng lẽ phải bị phá hủy");

        // Gạch ngoài tầm (cách 500px) phải còn nguyên
        assertFalse(outOfRangeBrick.isDestroyed(), "Gạch ngoài tầm đáng lẽ phải còn nguyên");


        // B. Xác minh Hành vi (Verify method calls)

        // Phải cộng 10 điểm cho gạch thường bị phá hủy
        verify(gameManager, times(1)).addScore(10);

        // (Xác minh rằng tổng số lần gọi addScore chỉ là 1,
        // không cộng điểm cho gạch bị ignite hoặc gạch ngoài tầm)
    }
}
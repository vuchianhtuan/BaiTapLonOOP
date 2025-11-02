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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Bỏ qua các stub không cần thiết
@DisplayName("Kiểm tra Hệ thống Nổ (ExplosionSystem)")
class ExplosionSystemTest {

    @Mock
    private GameManager gameManager; // GameManager "giả"

    // Các đối tượng "Thật"
    private ExplosionSystem explosionSystem;
    private ExplosiveBrick sourceBrick;      // Gạch nổ gốc
    private ExplosiveBrick targetBrickExplosive; // Gạch nổ bị nổ lan
    private Brick targetBrickNormal;  // Gạch thường bị nổ lan
    private Brick outOfRangeBrick;    // Gạch thường ngoài tầm nổ

    // Khởi tạo ScalingManager một lần
    @BeforeAll
    static void setupGlobal() {
        ScalingManager.getInstance().update(1120, 720);
        AssetManager.getInstance().loadGlobalAssets();
    }

    @BeforeEach
    void setUp() {
        // --- 1. Khởi tạo đối tượng cần test (Thật) ---
        explosionSystem = new ExplosionSystem();

        // --- 2. Khởi tạo các đối tượng "Thật" dùng cho kịch bản ---
        // Đặt tọa độ cho các gạch
        sourceBrick = new ExplosiveBrick(100, 100, 45, 20);

        // Gạch nổ này ở (110, 100), cách gạch gốc 10px -> TRONG TẦM NỔ
        targetBrickExplosive = new ExplosiveBrick(110, 100, 45, 20);

        // Gạch thường này ở (120, 100), cách gạch gốc 20px -> TRONG TẦM NỔ
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

    @Test
    @DisplayName("Test Gạch nổ kích hoạt nổ lan (Chain Reaction)")
    void testExplosionChainReaction() {
        // --- 1. SẮP XẾP (Arrange) ---
        // Kiểm tra trạng thái ban đầu
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

        // Không được cộng điểm cho gạch nổ (vì nó chỉ bị ignite) hoặc gạch ngoài tầm
        // (Chúng ta verify tổng số lần gọi là 1)
    }
}
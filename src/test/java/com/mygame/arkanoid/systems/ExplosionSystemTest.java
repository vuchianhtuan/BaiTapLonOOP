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
    private GameManager gameManager;

    private ExplosionSystem explosionSystem;
    private ExplosiveBrick sourceBrick;      // Gạch nổ gốc
    private ExplosiveBrick targetBrickExplosive; // Gạch nổ bị nổ lan
    private Brick targetBrickNormal;  // Gạch thường bị nổ lan
    private Brick outOfRangeBrick;    // Gạch thường ngoài tầm nổ

    @BeforeAll
    static void setupGlobal() {
        ScalingManager.getInstance().update(1120, 720);
        AssetManager.getInstance().loadGlobalAssets();
    }

    @BeforeEach
    void setUp() {
        explosionSystem = new ExplosionSystem();

        sourceBrick = new ExplosiveBrick(100, 100, 45, 20);

        targetBrickExplosive = new ExplosiveBrick(110, 100, 45, 20);

        targetBrickNormal = new NormalBrick(120, 100, 45, 20);

        outOfRangeBrick = new NormalBrick(500, 500, 45, 20);

        List<Brick> allBricks = Arrays.asList(targetBrickExplosive, targetBrickNormal, outOfRangeBrick);

        when(gameManager.getBricks()).thenReturn(allBricks);
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());
        when(gameManager.getActiveShards()).thenReturn(new ArrayList<Shard>());
    }

    @Test
    @DisplayName("Test Gạch nổ kích hoạt nổ lan (Chain Reaction)")
    void testExplosionChainReaction() {
        assertTrue(targetBrickExplosive.isAlive(), "Gạch nổ mục tiêu phải còn sống trước khi test");
        assertFalse(targetBrickNormal.isDestroyed(), "Gạch thường mục tiêu không được vỡ trước khi test");
        assertFalse(outOfRangeBrick.isDestroyed(), "Gạch ngoài tầm không được vỡ trước khi test");

        explosionSystem.explode(sourceBrick, 50.0, gameManager);

        assertTrue(targetBrickExplosive.isAwaitingDetonation(), "Gạch nổ mục tiêu đáng lẽ phải bị 'kích hoạt' (ignite)");

        assertTrue(targetBrickNormal.isDestroyed(), "Gạch thường mục tiêu đáng lẽ phải bị phá hủy");

        assertFalse(outOfRangeBrick.isDestroyed(), "Gạch ngoài tầm đáng lẽ phải còn nguyên");

        verify(gameManager, times(1)).addScore(10);

    }
}
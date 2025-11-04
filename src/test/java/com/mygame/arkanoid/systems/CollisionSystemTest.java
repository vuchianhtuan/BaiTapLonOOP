package com.mygame.arkanoid.systems;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mygame.arkanoid.objects.powerups.ExpandPaddlePowerUp;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import java.util.ArrayList;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.SoundManager;
import com.mygame.arkanoid.level.Level;
import com.mygame.arkanoid.level.LevelManager;
import com.mygame.arkanoid.objects.Ball;
import com.mygame.arkanoid.objects.Paddle;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.NormalBrick;
import com.mygame.arkanoid.objects.powerups.PowerUpType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm tra Hệ thống Va chạm (CollisionSystem)")
class CollisionSystemTest {
    @Mock
    private GameManager gameManager; // GameManager "giả"
    @Mock
    private SoundManager soundManager; // SoundManager "giả"
    @Mock
    private LevelManager levelManager; // LevelManager "giả"
    @Mock
    private Level level; // Level "giả"
    @Mock
    private Paddle paddle; // Paddle "giả" (dùng cho các test khác)

    private CollisionSystem collisionSystem;
    private Ball realBall;
    private Brick realBrick;

    @BeforeAll
    static void setupGlobal() {
        ScalingManager.getInstance().update(1120, 720);
    }

    @BeforeEach
    void setUp() {
        collisionSystem = new CollisionSystem();
        realBall = new Ball(100, 100, 18, 18, "skin");
        realBall.setStuckToPaddle(false);
        realBrick = new NormalBrick(100, 100, 45, 20);

        when(gameManager.getLevelManager()).thenReturn(levelManager);
        when(levelManager.getCurrentLevel()).thenReturn(level);
        when(level.getRandomPowerUpType()).thenReturn(null);
        when(paddle.getBounds()).thenReturn(new java.awt.Rectangle(-100, -100, 1, 1));

        when(paddle.isSticky()).thenReturn(false);
        when(gameManager.getActiveShards()).thenReturn(new java.util.ArrayList<>());
        when(gameManager.getSoundManager()).thenReturn(soundManager);
    }

    @Test
    @DisplayName("Test Bóng va chạm Gạch Thường (NormalBrick)")
    void testBallHitsNormalBrick() {
        List<Ball> balls = Arrays.asList(realBall);
        List<Brick> bricks = Arrays.asList(realBrick);

        when(gameManager.getPaddle()).thenReturn(paddle);
        when(gameManager.getBalls()).thenReturn(balls);
        when(gameManager.getBricks()).thenReturn(bricks);
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());

        assertFalse(realBrick.isDestroyed(), "Gạch không nên bị vỡ trước khi test");
        collisionSystem.checkAllCollisions(gameManager);
        assertTrue(realBrick.isDestroyed(), "Gạch LẼ RA PHẢI bị vỡ sau va chạm");

        verify(gameManager, times(1)).addScore(10);
        verify(soundManager, times(1)).playSound(SoundManager.SFX_BRICK_HIT);
        verify(gameManager, never()).addPowerUp(any(com.mygame.arkanoid.objects.powerups.PowerUp.class));
    }

    @Test
    @DisplayName("Test Paddle nhặt Power-up (Expand)")
    void testPaddleCollectsPowerUp() {
        PowerUp realPowerUp = new ExpandPaddlePowerUp(300, 500, 30, 15);
        List<PowerUp> fallingPowerUps = new ArrayList<>(Arrays.asList(realPowerUp));
        Paddle realPaddle = new Paddle(300, 500, 120, 20, "skin");

        when(gameManager.getPaddle()).thenReturn(realPaddle);
        when(gameManager.getPowerUps()).thenReturn(fallingPowerUps);
        when(gameManager.getLasers()).thenReturn(Collections.emptyList());
        when(gameManager.getBalls()).thenReturn(Collections.emptyList());
        when(gameManager.getBricks()).thenReturn(Collections.emptyList());
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());
        assertEquals(1, fallingPowerUps.size(), "Power-up phải ở trong list trước khi va chạm");

        collisionSystem.checkAllCollisions(gameManager);
        assertEquals(0, fallingPowerUps.size(), "Power-up đáng lẽ phải bị xóa khỏi list sau khi nhặt");

        verify(gameManager, times(1)).activatePowerUp(realPowerUp);
    }

    @Test
    @DisplayName("Test Bóng va chạm Paddle dính (Sticky Paddle)")
    void testBallHitsStickyPaddle() {
        Ball realBall = new Ball(300, 500, 18, 18, "skin");
        realBall.setStuckToPaddle(false); // Đảm bảo bóng đang bay

        Paddle realPaddle = new Paddle(300, 500, 120, 20, "skin");
        realPaddle.setSticky(true); // Bật chế độ dính

        when(gameManager.getPaddle()).thenReturn(realPaddle);
        when(gameManager.getBalls()).thenReturn(Arrays.asList(realBall));
        when(gameManager.getLasers()).thenReturn(Collections.emptyList());
        when(gameManager.getPowerUps()).thenReturn(Collections.emptyList());
        when(gameManager.getBricks()).thenReturn(Collections.emptyList());
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());

        assertFalse(realBall.isStuckToPaddle(), "Bóng không nên bị dính trước khi test");
        collisionSystem.checkAllCollisions(gameManager);
        assertTrue(realBall.isStuckToPaddle(), "Bóng đáng lẽ phải dính vào paddle");
        verify(soundManager, times(1)).playSound(SoundManager.SFX_PADDLE_HIT);
    }

    @Test
    @DisplayName("Test Gạch vỡ VÀ thả ra Power-Up")
    void testBallDestroysBrickAndSpawnsPowerUp() {
        Ball testBall = new Ball(100, 100, 18, 18, "skin");
        testBall.setStuckToPaddle(false);
        Brick testBrick = new NormalBrick(100, 100, 45, 20);

        List<Ball> balls = Arrays.asList(testBall);
        List<Brick> bricks = Arrays.asList(testBrick);

        when(gameManager.getBalls()).thenReturn(balls);
        when(gameManager.getBricks()).thenReturn(bricks);
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());

        when(gameManager.getPaddle()).thenReturn(paddle);
        when(level.getRandomPowerUpType()).thenReturn(PowerUpType.EXPAND);

        assertFalse(testBrick.isDestroyed(), "Gạch không nên vỡ trước khi test");
        collisionSystem.checkAllCollisions(gameManager);

        assertTrue(testBrick.isDestroyed(), "Gạch đáng lẽ phải bị vỡ");
        verify(gameManager, times(1)).addScore(10);
        verify(soundManager, times(1)).playSound(SoundManager.SFX_BRICK_HIT);
        verify(gameManager, times(1)).addPowerUp(any(ExpandPaddlePowerUp.class));
    }
}
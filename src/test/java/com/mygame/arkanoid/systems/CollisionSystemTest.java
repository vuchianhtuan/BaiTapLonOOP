package com.mygame.arkanoid.systems;

// Import thư viện Mockito
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

// Import các lớp của game
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

// BÁO CHO JUNIT 5 BIẾT LÀ CHÚNG TA DÙNG MOCKITO
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm tra Hệ thống Va chạm (CollisionSystem)")
class CollisionSystemTest {

    // --- Đối tượng "Giả" (Mock) ---
    // Chúng ta yêu cầu Mockito tạo ra các phiên bản giả của các lớp này
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

    // --- Đối tượng "Thật" (Real) ---
    private CollisionSystem collisionSystem; // Lớp chúng ta muốn test
    private Ball realBall; // Bóng thật
    private Brick realBrick; // Gạch thật

    // Khởi tạo kích thước game (giống như bài test Paddle)
    @BeforeAll
    static void setupGlobal() {
        ScalingManager.getInstance().update(1120, 720);
    }

    @BeforeEach
    void setUp() {
        collisionSystem = new CollisionSystem();

        // Tạo bóng và gạch thật
        realBall = new Ball(100, 100, 18, 18, "skin");
        realBall.setStuckToPaddle(false);
        realBrick = new NormalBrick(100, 100, 45, 20);

        // --- Cài đặt cho các đối tượng "Giả" ---

        when(gameManager.getLevelManager()).thenReturn(levelManager);
        when(levelManager.getCurrentLevel()).thenReturn(level);
        when(level.getRandomPowerUpType()).thenReturn(null);

        // --- SỬA LỖI UnnecessaryStubbingException ---

        // 1. Stub này LÀ CẦN THIẾT (để checkCollision không bị NPE)
        when(paddle.getBounds()).thenReturn(new java.awt.Rectangle(-100, -100, 1, 1));

        when(paddle.isSticky()).thenReturn(false);
        // 3. THÊM MỚI: Sửa lỗi NPE sắp xảy ra khi gạch vỡ
        //    Dạy GameManager trả về 1 List thật (rỗng) khi bị hỏi
        when(gameManager.getActiveShards()).thenReturn(new java.util.ArrayList<>());
        when(gameManager.getSoundManager()).thenReturn(soundManager);
    }

    @Test
    @DisplayName("Test Bóng va chạm Gạch Thường (NormalBrick)")
    void testBallHitsNormalBrick() {
        // --- 1. SẮP XẾP (Arrange) ---
        // Tạo danh sách chứa các vật thể thật của chúng ta
        List<Ball> balls = Arrays.asList(realBall);
        List<Brick> bricks = Arrays.asList(realBrick);

        // Dạy GameManager "giả" cách trả lời khi được hỏi
        when(gameManager.getPaddle()).thenReturn(paddle); // Trả về paddle giả
        when(gameManager.getBalls()).thenReturn(balls); // Trả về list bóng thật
        when(gameManager.getBricks()).thenReturn(bricks); // Trả về list gạch thật
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList()); // Trả về list rỗng

        // --- 2. HÀNH ĐỘNG (Act) ---
        // Lấy trạng thái gạch TRƯỚC khi va chạm
        assertFalse(realBrick.isDestroyed(), "Gạch không nên bị vỡ trước khi test");

        // CHẠY HÀM CẦN TEST
        collisionSystem.checkAllCollisions(gameManager);

        // --- 3. XÁC MINH (Assert / Verify) ---

        // A. Xác minh Tác động (Assert state changes)
        assertTrue(realBrick.isDestroyed(), "Gạch LẼ RA PHẢI bị vỡ sau va chạm");

        // B. Xác minh Hành vi (Verify method calls)
        // Chúng ta muốn chắc chắn rằng CollisionSystem đã GỌI các hàm này:

        // 1. Xác minh rằng hàm .addScore(10) đã được gọi 1 lần
        verify(gameManager, times(1)).addScore(10);

        // 2. Xác minh rằng hàm .playSound(SFX_BRICK_HIT) đã được gọi 1 lần
        verify(soundManager, times(1)).playSound(SoundManager.SFX_BRICK_HIT);

        // 3. Xác minh rằng không có power-up nào được thêm
        verify(gameManager, never()).addPowerUp(any(com.mygame.arkanoid.objects.powerups.PowerUp.class));
    }

    @Test
    @DisplayName("Test Paddle nhặt Power-up (Expand)")
    void testPaddleCollectsPowerUp() {
        // --- 1. SẮP XẾP (Arrange) ---

        // Tạo một PowerUp "thật" đang rơi
        PowerUp realPowerUp = new ExpandPaddlePowerUp(300, 500, 30, 15);
        // Tạo một list "thật" chứa power-up đó
        // Chúng ta phải dùng ArrayList vì code CollisionSystem gọi .iterator()
        List<PowerUp> fallingPowerUps = new ArrayList<>(Arrays.asList(realPowerUp));

        // Tạo một Paddle "thật" ở vị trí va chạm
        Paddle realPaddle = new Paddle(300, 500, 120, 20, "skin");

        // Dạy GameManager "giả":
        // 1. Trả về paddle THẬT
        when(gameManager.getPaddle()).thenReturn(realPaddle);
        // 2. Trả về list power-up THẬT (đang rơi)
        when(gameManager.getPowerUps()).thenReturn(fallingPowerUps);
        // 3. Trả về list rỗng cho những thứ không liên quan
        when(gameManager.getLasers()).thenReturn(Collections.emptyList());
        when(gameManager.getBalls()).thenReturn(Collections.emptyList());
        when(gameManager.getBricks()).thenReturn(Collections.emptyList());
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());

        // --- 2. HÀNH ĐỘNG (Act) ---
        // Kiểm tra xem power-up có trong list trước khi va chạm không
        assertEquals(1, fallingPowerUps.size(), "Power-up phải ở trong list trước khi va chạm");

        // Chạy hàm cần test
        collisionSystem.checkAllCollisions(gameManager);

        // --- 3. XÁC MINH (Assert / Verify) ---

        // A. Xác minh Tác động (Assert state changes)
        // Power-up phải bị XÓA khỏi danh sách đang rơi sau khi bị nhặt
        assertEquals(0, fallingPowerUps.size(), "Power-up đáng lẽ phải bị xóa khỏi list sau khi nhặt");

        // B. Xác minh Hành vi (Verify method calls)
        // Hệ thống va chạm phải gọi hàm .activatePowerUp() CỦA GameManager
        // với CHÍNH XÁC cái power-up mà nó vừa nhặt.
        verify(gameManager, times(1)).activatePowerUp(realPowerUp);
    }

    @Test
    @DisplayName("Test Bóng va chạm Paddle dính (Sticky Paddle)")
    void testBallHitsStickyPaddle() {
        // --- 1. SẮP XẾP (Arrange) ---

        // Tạo Bóng và Paddle "thật" ở cùng tọa độ
        Ball realBall = new Ball(300, 500, 18, 18, "skin");
        realBall.setStuckToPaddle(false); // Đảm bảo bóng đang bay

        Paddle realPaddle = new Paddle(300, 500, 120, 20, "skin");
        realPaddle.setSticky(true); // QUAN TRỌNG: Bật chế độ dính

        // Dạy GameManager "giả":
        // 1. Trả về paddle THẬT
        when(gameManager.getPaddle()).thenReturn(realPaddle);
        // 2. Trả về list bóng THẬT
        when(gameManager.getBalls()).thenReturn(Arrays.asList(realBall));

        // 3. Trả về list rỗng cho những thứ không liên quan
        when(gameManager.getLasers()).thenReturn(Collections.emptyList());
        when(gameManager.getPowerUps()).thenReturn(Collections.emptyList());
        when(gameManager.getBricks()).thenReturn(Collections.emptyList());
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());

        // (SoundManager đã được stub trong setUp() rồi)

        // --- 2. HÀNH ĐỘNG (Act) ---
        // Lấy trạng thái bóng TRƯỚC khi va chạm
        assertFalse(realBall.isStuckToPaddle(), "Bóng không nên bị dính trước khi test");

        // Chạy hàm cần test
        collisionSystem.checkAllCollisions(gameManager);

        // --- 3. XÁC MINH (Assert / Verify) ---

        // A. Xác minh Tác động (Assert state changes)
        // Trạng thái của bóng THẬT phải là "đã dính"
        assertTrue(realBall.isStuckToPaddle(), "Bóng đáng lẽ phải dính vào paddle");

        // B. Xác minh Hành vi (Verify method calls)
        // Phải phát ra âm thanh va chạm
        verify(soundManager, times(1)).playSound(SoundManager.SFX_PADDLE_HIT);
    }

    @Test
    @DisplayName("Test Gạch vỡ VÀ thả ra Power-Up")
    void testBallDestroysBrickAndSpawnsPowerUp() {
        // --- 1. SẮP XẾP (Arrange) ---

        Ball testBall = new Ball(100, 100, 18, 18, "skin");
        testBall.setStuckToPaddle(false);
        Brick testBrick = new NormalBrick(100, 100, 45, 20);

        List<Ball> balls = Arrays.asList(testBall);
        List<Brick> bricks = Arrays.asList(testBrick);

        // "Dạy" GameManager "giả" trả về các list "thật"
        when(gameManager.getBalls()).thenReturn(balls);
        when(gameManager.getBricks()).thenReturn(bricks);
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());

        // --- THÊM DÒNG NÀY ĐỂ SỬA LỖI ---
        when(gameManager.getPaddle()).thenReturn(paddle); // Trả về paddle "giả"
        // --------------------------------

        // "Dạy" Level "giả" thả ra Power-Up
        when(level.getRandomPowerUpType()).thenReturn(PowerUpType.EXPAND);

        // --- 2. HÀNH ĐỘNG (Act) ---
        assertFalse(testBrick.isDestroyed(), "Gạch không nên vỡ trước khi test");
        collisionSystem.checkAllCollisions(gameManager);

        // --- 3. XÁC MINH (Assert / Verify) ---
        assertTrue(testBrick.isDestroyed(), "Gạch đáng lẽ phải bị vỡ");
        verify(gameManager, times(1)).addScore(10);
        verify(soundManager, times(1)).playSound(SoundManager.SFX_BRICK_HIT);
        verify(gameManager, times(1)).addPowerUp(any(ExpandPaddlePowerUp.class));
    }
}
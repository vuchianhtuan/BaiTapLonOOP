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

/**
 * Lớp kiểm thử (test class) JUnit 5 dành cho {@link CollisionSystem}.
 * <p>
 * Lớp này sử dụng Mockito để "giả lập" (mock) các trình quản lý (dependency)
 * như {@link GameManager} và {@link SoundManager}.
 * <p>
 * Mục đích là để kiểm thử logic va chạm của {@code CollisionSystem}
 * một cách độc lập (in isolation), xác minh rằng nó
 * gọi đúng các phương thức (ví dụ: {@code addScore}, {@code playSound})
 * khi va chạm xảy ra.
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito
@MockitoSettings(strictness = Strictness.LENIENT) // (Tùy chọn) Giảm bớt cảnh báo "stubbing" không cần thiết
@DisplayName("Kiểm tra Hệ thống Va chạm (CollisionSystem)")
class CollisionSystemTest {

    // --- Đối tượng "Giả" (Mock) ---
    // Mockito sẽ tự động "tiêm" (inject) các đối tượng giả lập vào đây.

    /** Một {@link GameManager} giả lập (mock) để theo dõi các lệnh gọi (ví dụ: addScore). */
    @Mock
    private GameManager gameManager;
    /** Một {@link SoundManager} giả lập (mock) để theo dõi lệnh gọi {@code playSound}. */
    @Mock
    private SoundManager soundManager;
    /** Một {@link LevelManager} giả lập (mock). */
    @Mock
    private LevelManager levelManager;
    /** Một {@link Level} giả lập (mock) (ví dụ: để điều khiển việc thả power-up). */
    @Mock
    private Level level;
    /** Một {@link Paddle} giả lập (mock) (dùng cho các kịch bản không cần paddle thật). */
    @Mock
    private Paddle paddle;

    // --- Đối tượng "Thật" (Real) ---

    /** Hệ thống Va chạm (SUT - System Under Test) "thật" đang được kiểm thử. */
    private CollisionSystem collisionSystem;
    /** Một đối tượng {@link Ball} "thật" dùng trong các kịch bản va chạm. */
    private Ball realBall;
    /** Một đối tượng {@link Brick} "thật" dùng trong các kịch bản va chạm. */
    private Brick realBrick;

    /**
     * Thiết lập (setup) toàn cục, chạy MỘT LẦN DUY NHẤT trước tất cả các test.
     * <p>
     * <b>Quan trọng:</b> Cần khởi tạo {@link ScalingManager} (là một Singleton)
     * với kích thước màn hình giả lập, vì các đối tượng "thật"
     * (như {@code Ball}) có thể phụ thuộc vào nó (ví dụ: để kiểm tra biên).
     */
    @BeforeAll
    static void setupGlobal() {
        ScalingManager.getInstance().update(1120, 720);
    }

    /**
     * Thiết lập (setup) chạy trước *mỗi* phương thức {@code @Test}.
     * <p>
     * 1. Khởi tạo {@code CollisionSystem} (SUT).
     * 2. Khởi tạo các đối tượng "thật" (Ball, Brick).
     * 3. Thiết lập các hành vi (stub) mặc định cho các đối tượng "giả" (mock)
     * để tránh {@code NullPointerException}
     * (ví dụ: dạy {@code gameManager} trả về một danh sách rỗng thay vì `null`).
     */
    @BeforeEach
    void setUp() {
        collisionSystem = new CollisionSystem();

        // Tạo bóng và gạch thật
        realBall = new Ball(100, 100, 18, 18, "skin");
        realBall.setStuckToPaddle(false); // Đảm bảo bóng đang bay
        realBrick = new NormalBrick(100, 100, 45, 20); // Gạch tại cùng vị trí

        // --- Cài đặt (Stub) cho các đối tượng "Giả" ---
        // "Dạy" các mock trả về các giá trị mặc định khi được hỏi

        // Dạy gameManager trả về các mock khác
        when(gameManager.getLevelManager()).thenReturn(levelManager);
        when(levelManager.getCurrentLevel()).thenReturn(level);
        when(gameManager.getSoundManager()).thenReturn(soundManager);

        // Dạy level mặc định là KHÔNG thả power-up
        when(level.getRandomPowerUpType()).thenReturn(null);

        // Dạy paddle giả (dùng khi không cần paddle thật)
        when(paddle.getBounds()).thenReturn(new java.awt.Rectangle(-100, -100, 1, 1)); // Đặt ở xa
        when(paddle.isSticky()).thenReturn(false);

        // QUAN TRỌNG: Dạy GameManager trả về 1 List THẬT (rỗng)
        // để tránh NullPointerException khi code thật gọi .addAll()
        when(gameManager.getActiveShards()).thenReturn(new java.util.ArrayList<>());
    }

    /**
     * Kiểm tra kịch bản (scenario) cơ bản: Bóng va chạm Gạch.
     * <p>
     * <b>Xác minh (Verify):</b>
     * <ul>
     * <li>Trạng thái (State): Gạch phải bị phá hủy ({@code isDestroyed() == true}).</li>
     * <li>Hành vi (Behavior): Phải gọi {@code addScore(10)} và {@code playSound()}.</li>
     * <li>Hành vi (Behavior): Không được gọi {@code addPowerUp()}.</li>
     * </ul>
     */
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
        assertFalse(realBrick.isDestroyed(), "Gạch không nên bị vỡ trước khi test");

        // CHẠY HÀM CẦN TEST
        collisionSystem.checkAllCollisions(gameManager);

        // --- 3. XÁC MINH (Assert / Verify) ---

        // A. Xác minh Tác động (Assert state changes)
        assertTrue(realBrick.isDestroyed(), "Gạch LẼ RA PHẢI bị vỡ sau va chạm");

        // B. Xác minh Hành vi (Verify method calls)
        // 1. Xác minh rằng hàm .addScore(10) đã được gọi 1 lần
        verify(gameManager, times(1)).addScore(10);
        // 2. Xác minh rằng hàm .playSound(SFX_BRICK_HIT) đã được gọi 1 lần
        verify(soundManager, times(1)).playSound(SoundManager.SFX_BRICK_HIT);
        // 3. Xác minh rằng không có power-up nào được thêm
        verify(gameManager, never()).addPowerUp(any(com.mygame.arkanoid.objects.powerups.PowerUp.class));
    }

    /**
     * Kiểm tra kịch bản Paddle "nhặt" (thu thập) một PowerUp "thật" đang rơi.
     * <p>
     * <b>Xác minh (Verify):</b>
     * <ul>
     * <li>Trạng thái (State): PowerUp phải bị xóa khỏi danh sách đang rơi.</li>
     * <li>Hành vi (Behavior): Phải gọi {@code activatePowerUp()}
     * với chính xác đối tượng PowerUp đã nhặt.</li>
     * </ul>
     */
    @Test
    @DisplayName("Test Paddle nhặt Power-up (Expand)")
    void testPaddleCollectsPowerUp() {
        // --- 1. SẮP XẾP (Arrange) ---

        // Tạo một PowerUp "thật" đang rơi
        PowerUp realPowerUp = new ExpandPaddlePowerUp(300, 500, 30, 15);
        // Dùng ArrayList vì code (SUT) gọi .iterator().remove()
        List<PowerUp> fallingPowerUps = new ArrayList<>(Arrays.asList(realPowerUp));

        // Tạo một Paddle "thật" ở vị trí va chạm
        Paddle realPaddle = new Paddle(300, 500, 120, 20, "skin");

        // Dạy GameManager "giả":
        when(gameManager.getPaddle()).thenReturn(realPaddle); // Trả về paddle THẬT
        when(gameManager.getPowerUps()).thenReturn(fallingPowerUps); // Trả về list power-up THẬT

        // Trả về list rỗng cho những thứ không liên quan
        when(gameManager.getLasers()).thenReturn(Collections.emptyList());
        when(gameManager.getBalls()).thenReturn(Collections.emptyList());
        when(gameManager.getBricks()).thenReturn(Collections.emptyList());
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());

        // --- 2. HÀNH ĐỘNG (Act) ---
        assertEquals(1, fallingPowerUps.size(), "Power-up phải ở trong list trước khi va chạm");

        // Chạy hàm cần test
        collisionSystem.checkAllCollisions(gameManager);

        // --- 3. XÁC MINH (Assert / Verify) ---

        // A. Xác minh Tác động (Assert state changes)
        // Power-up phải bị XÓA khỏi danh sách đang rơi sau khi bị nhặt
        assertEquals(0, fallingPowerUps.size(), "Power-up đáng lẽ phải bị xóa khỏi list sau khi nhặt");

        // B. Xác minh Hành vi (Verify method calls)
        // Hệ thống va chạm phải gọi .activatePowerUp() của GameManager
        verify(gameManager, times(1)).activatePowerUp(realPowerUp);
    }

    /**
     * Kiểm tra kịch bản Bóng va chạm với một Paddle "thật"
     * đang ở trạng thái 'dính' (sticky).
     * <p>
     * <b>Xác minh (Verify):</b>
     * <ul>
     * <li>Trạng thái (State): Trạng thái của bóng {@code isStuckToPaddle()}
     * phải chuyển thành {@code true}.</li>
     * <li>Hành vi (Behavior): Phải phát ra âm thanh va chạm.</li>
     * </ul>
     */
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
        when(gameManager.getPaddle()).thenReturn(realPaddle); // Trả về paddle THẬT
        when(gameManager.getBalls()).thenReturn(Arrays.asList(realBall)); // Trả về list bóng THẬT

        // Trả về list rỗng cho những thứ không liên quan
        when(gameManager.getLasers()).thenReturn(Collections.emptyList());
        when(gameManager.getPowerUps()).thenReturn(Collections.emptyList());
        when(gameManager.getBricks()).thenReturn(Collections.emptyList());
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());

        // (SoundManager đã được stub trong setUp())

        // --- 2. HÀNH ĐỘNG (Act) ---
        assertFalse(realBall.isStuckToPaddle(), "Bóng không nên bị dính trước khi test");

        // Chạy hàm cần test
        collisionSystem.checkAllCollisions(gameManager);

        // --- 3. XÁC MINH (Assert / Verify) ---

        // A. Xác minh Tác động (Assert state changes)
        assertTrue(realBall.isStuckToPaddle(), "Bóng đáng lẽ phải dính vào paddle");

        // B. Xác minh Hành vi (Verify method calls)
        verify(soundManager, times(1)).playSound(SoundManager.SFX_PADDLE_HIT);
    }

    /**
     * Kiểm tra kịch bản (scenario) phức tạp:
     * Bóng va chạm gạch -> Gạch bị phá hủy -> Gạch thả ra PowerUp.
     * <p>
     * <b>Xác minh (Verify):</b>
     * <ul>
     * <li>Trạng thái (State): Gạch phải bị phá hủy.</li>
     * <li>Hành vi (Behavior): Phải gọi {@code addScore} và {@code playSound}.</li>
     * <li>Hành vi (Behavior): Phải gọi {@code addPowerUp()}
     * (vì chúng ta đã stub {@code level.getRandomPowerUpType()}
     * để trả về {@code PowerUpType.EXPAND}).</li>
     * </ul>
     */
    @Test
    @DisplayName("Test Gạch vỡ VÀ thả ra Power-Up")
    void testBallDestroysBrickAndSpawnsPowerUp() {
        // --- 1. SẮP XẾP (Arrange) ---

        // (Sử dụng các đối tượng "thật" đã được tạo trong setUp())
        List<Ball> balls = Arrays.asList(realBall);
        List<Brick> bricks = Arrays.asList(realBrick);

        // "Dạy" GameManager "giả" trả về các list "thật"
        when(gameManager.getBalls()).thenReturn(balls);
        when(gameManager.getBricks()).thenReturn(bricks);
        when(gameManager.getLaserShooters()).thenReturn(Collections.emptyList());
        when(gameManager.getPaddle()).thenReturn(paddle); // Trả về paddle "giả"

        // QUAN TRỌNG: "Dạy" (stub) Level "giả"
        // PHẢI thả ra Power-Up loại EXPAND khi được hỏi
        when(level.getRandomPowerUpType()).thenReturn(PowerUpType.EXPAND);

        // --- 2. HÀNH ĐỘNG (Act) ---
        assertFalse(realBrick.isDestroyed(), "Gạch không nên vỡ trước khi test");
        collisionSystem.checkAllCollisions(gameManager);

        // --- 3. XÁC MINH (Assert / Verify) ---

        // Xác minh trạng thái
        assertTrue(realBrick.isDestroyed(), "Gạch đáng lẽ phải bị vỡ");

        // Xác minh hành vi
        verify(gameManager, times(1)).addScore(10);
        verify(soundManager, times(1)).playSound(SoundManager.SFX_BRICK_HIT);
        // Xác minh rằng GameManager ĐÃ ĐƯỢC YÊU CẦU thêm một PowerUp
        // thuộc đúng loại ExpandPaddlePowerUp
        verify(gameManager, times(1)).addPowerUp(any(ExpandPaddlePowerUp.class));
    }
}
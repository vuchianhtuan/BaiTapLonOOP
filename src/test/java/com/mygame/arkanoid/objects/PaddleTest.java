package com.mygame.arkanoid.objects;

// Import thư viện JUnit
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Import các lớp cần thiết
import com.mygame.arkanoid.systems.ScalingManager;

/**
 * Lớp kiểm thử (test class) JUnit 5 dành cho {@link Paddle}.
 * <p>
 * Lớp này xác minh logic di chuyển (movement) của Paddle,
 * đặc biệt là hành vi "kẹp" (clamping) - tức là ngăn
 * Paddle di chuyển ra ngoài các biên (tường) của khu vực chơi game.
 */
@DisplayName("Kiểm tra logic của Paddle")
class PaddleTest {

    /** Đối tượng Paddle (SUT - System Under Test) cho mỗi kiểm thử. */
    private Paddle paddle;
    /** Bản sao (local copy) của chiều rộng khu vực chơi game (lấy từ ScalingManager). */
    private static int GAME_AREA_WIDTH;
    /** Chiều rộng mặc định của Paddle dùng cho kiểm thử. */
    private static final int PADDLE_WIDTH = 120;
    /** Tốc độ di chuyển mặc định của Paddle dùng cho kiểm thử. */
    private static final int PADDLE_SPEED = 15;

    /**
     * Thiết lập (setup) toàn cục, chạy một lần duy nhất trước tất cả các test.
     * <p>
     * <b>Quan trọng:</b> Logic của {@link Paddle#moveRight()} phụ thuộc vào
     * {@link ScalingManager} để biết chiều rộng màn hình ({@code GAME_AREA_WIDTH}).
     * Do đó, chúng ta phải 'giả lập' (initialize) ScalingManager
     * trước khi bất kỳ test nào chạy để tránh {@code NullPointerException}.
     */
    @BeforeAll
    static void setupGlobal() {
        // Chúng ta phải "giả lập" việc game khởi động và
        // khởi tạo ScalingManager với kích thước gốc.
        ScalingManager sm = ScalingManager.getInstance();
        sm.update(1120, 720); // Cung cấp kích thước cửa sổ giả

        // Lưu lại kích thước game area để dùng cho các test
        GAME_AREA_WIDTH = sm.GAME_AREA_WIDTH; // Sẽ là 960
    }

    /**
     * Thiết lập (setup) chạy trước *mỗi* phương thức {@code @Test}.
     * <p>
     * Khởi tạo một đối tượng {@link Paddle} mới, "sạch"
     * tại vị trí chính giữa màn hình cho mỗi kiểm thử.
     */
    @BeforeEach
    void setUp() {
        // Tạo paddle ở giữa màn hình game
        int startX = (GAME_AREA_WIDTH - PADDLE_WIDTH) / 2; // (960 - 120) / 2 = 420
        paddle = new Paddle(startX, 600, PADDLE_WIDTH, 20, "skin_test");
    }

    /**
     * Kiểm tra trường hợp cơ bản: Paddle di chuyển sang phải
     * một khoảng cách bằng {@code PADDLE_SPEED}.
     */
    @Test
    @DisplayName("Di chuyển sang phải (không chạm tường)")
    void testMoveRight() {
        int initialX = paddle.getX(); // 420
        paddle.moveRight();
        assertEquals(initialX + PADDLE_SPEED, paddle.getX(), "Paddle di chuyển phải thất bại");
    }

    /**
     * Kiểm tra trường hợp cơ bản: Paddle di chuyển sang trái
     * một khoảng cách bằng {@code PADDLE_SPEED}.
     */
    @Test
    @DisplayName("Di chuyển sang trái (không chạm tường)")
    void testMoveLeft() {
        int initialX = paddle.getX(); // 420
        paddle.moveLeft();
        assertEquals(initialX - PADDLE_SPEED, paddle.getX(), "Paddle di chuyển trái thất bại");
    }

    /**
     * Kiểm tra logic "kẹp" (clamping) ở biên phải.
     * Xác minh rằng Paddle không thể di chuyển VƯỢT QUÁ
     * vị trí tối đa ({@code GAME_AREA_WIDTH - PADDLE_WIDTH}).
     */
    @Test
    @DisplayName("Không thể di chuyển qua mép phải")
    void testMoveRightAtEdge() {
        // Vị trí tối đa là 960 (GAME_AREA) - 120 (WIDTH) = 840
        int max_x = GAME_AREA_WIDTH - PADDLE_WIDTH;

        paddle.setX(max_x - 5); // Đặt paddle sát mép (vị trí 835)
        paddle.moveRight(); // Di chuyển +15 -> 850

        // Logic "clamp" phải giữ paddle ở 840
        assertEquals(max_x, paddle.getX(), "Paddle đã di chuyển VƯỢT QUÁ mép phải");

        // Thử di chuyển lần nữa khi đã ở mép
        paddle.moveRight();
        assertEquals(max_x, paddle.getX(), "Paddle vẫn di chuyển dù đã ở mép phải");
    }

    /**
     * Kiểm tra logic "kẹp" (clamping) ở biên trái.
     * Xác minh rằng Paddle không thể di chuyển VƯỢT QUÁ
     * vị trí tối thiểu (x = 0).
     */
    @Test
    @DisplayName("Không thể di chuyển qua mép trái")
    void testMoveLeftAtEdge() {
        paddle.setX(5); // Đặt paddle sát mép trái (vị trí 5)
        paddle.moveLeft(); // Di chuyển -15 -> -10

        // Logic "clamp" phải giữ paddle ở 0
        assertEquals(0, paddle.getX(), "Paddle đã di chuyển VƯỢT QUÁ mép trái");

        // Thử di chuyển lần nữa
        paddle.moveLeft();
        assertEquals(0, paddle.getX(), "Paddle vẫn di chuyển dù đã ở mép trái");
    }
}
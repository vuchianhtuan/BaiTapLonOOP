package com.mygame.arkanoid.objects;

// Import thư viện JUnit
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Import các lớp cần thiết
import com.mygame.arkanoid.systems.ScalingManager;

@DisplayName("Kiểm tra logic của Paddle")
class PaddleTest {

    private Paddle paddle;
    private static int GAME_AREA_WIDTH;
    private static final int PADDLE_WIDTH = 120;
    private static final int PADDLE_SPEED = 15;

    // !!! GIẢI PHÁP CHO SINGLETON !!!
    // Hàm này chạy MỘT LẦN DUY NHẤT trước tất cả các test trong class này
    @BeforeAll
    static void setupGlobal() {
        // Chúng ta phải "giả lập" việc game khởi động và
        // khởi tạo ScalingManager với kích thước gốc.
        ScalingManager sm = ScalingManager.getInstance();
        sm.update(1120, 720); // Cung cấp kích thước cửa sổ giả

        // Lưu lại kích thước game area để dùng cho các test
        GAME_AREA_WIDTH = sm.GAME_AREA_WIDTH; // Sẽ là 960
    }

    // Hàm này chạy trước MỖI test
    @BeforeEach
    void setUp() {
        // Tạo paddle ở giữa màn hình game
        int startX = (GAME_AREA_WIDTH - PADDLE_WIDTH) / 2; // (960 - 120) / 2 = 420
        paddle = new Paddle(startX, 600, PADDLE_WIDTH, 20, "skin_test");
    }

    @Test
    @DisplayName("Di chuyển sang phải (không chạm tường)")
    void testMoveRight() {
        int initialX = paddle.getX(); // 420
        paddle.moveRight();
        assertEquals(initialX + PADDLE_SPEED, paddle.getX(), "Paddle di chuyển phải thất bại");
    }

    @Test
    @DisplayName("Di chuyển sang trái (không chạm tường)")
    void testMoveLeft() {
        int initialX = paddle.getX(); // 420
        paddle.moveLeft();
        assertEquals(initialX - PADDLE_SPEED, paddle.getX(), "Paddle di chuyển trái thất bại");
    }

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
package com.mygame.arkanoid.objects;

// Import các lớp cần thiết cho test
import com.mygame.arkanoid.systems.ScalingManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp kiểm thử (test class) JUnit 5 dành cho {@link Ball}.
 * <p>
 * Tập trung vào việc xác minh logic vật lý va chạm (physics) của bóng,
 * đặc biệt là các kịch bản nảy (bounce) phức tạp:
 * <ul>
 * <li>Va chạm với gạch (đơn giản).</li>
 * <li>Va chạm với các vị trí khác nhau trên Paddle (tâm, mép trái, mép phải)
 * để kiểm tra logic tính toán góc nảy (reflection angle).</li>
 * </ul>
 */
@DisplayName("Kiểm tra logic vật lý của Bóng (Ball)")
class BallTest {

    /** Đối tượng Bóng (SUT - System Under Test) cho mỗi kiểm thử. */
    private Ball ball;

    /**
     * Thiết lập (setup) toàn cục, chạy một lần duy nhất trước tất cả các test.
     * <p>
     * <b>Quan trọng:</b> Lớp {@link Ball} (cụ thể là hàm {@code bounceOff})
     * có một phụ thuộc (dependency) vào {@link ScalingManager#getInstance()}
     * để lấy {@code GAME_AREA_WIDTH}.
     * <p>
     * Do đó, chúng ta *phải* "giả lập" (mock/initialize) rằng ScalingManager
     * đã được khởi tạo với kích thước màn hình gốc (native)
     * trước khi bất kỳ kiểm thử nào chạy, nếu không sẽ gây ra {@code NullPointerException}.
     */
    @BeforeAll
    static void setupGlobal() {
        // Chúng ta phải "giả lập" game đã khởi động
        // và khởi tạo ScalingManager với kích thước màn hình
        ScalingManager.getInstance().update(1120, 720);
    }

    /**
     * Thiết lập (setup) chạy trước *mỗi* phương thức {@code @Test}.
     * <p>
     * Khởi tạo một đối tượng {@link Ball} mới, "sạch" cho mỗi kiểm thử
     * và đặt nó ở trạng thái di chuyển cơ bản (bay xuống dưới).
     */
    @BeforeEach
    void setUp() {
        // Tạo một quả bóng mới cho mỗi test
        ball = new Ball(300, 300, 18, 18, "skin");
        // Giả lập bóng đang bay xuống (dx=0, dy=1)
        ball.setDx(0);
        ball.setDy(1);
    }

    /**
     * Kiểm tra logic nảy cơ bản (đảo chiều Y) khi va chạm
     * với một vật thể chung (không phải Paddle).
     */
    @Test
    @DisplayName("Bóng nảy khỏi GẠCH (logic chung)")
    void testBounceOffBrick() {
        // --- Sắp xếp (Arrange) ---
        // Gạch ở ngay dưới quả bóng
        // (Sử dụng Paddle như một GameObject đơn giản để test)
        Paddle brick = new Paddle(300, 320, 45, 20, "skin");
        ball.setDy(1); // Đảm bảo bóng đang bay xuống

        // --- Hành động (Act) ---
        ball.bounceOff(brick); // Va chạm

        // --- Xác minh (Assert) ---
        // Logic bounceOff chung (không phải Paddle) sẽ đảo chiều dy
        assertTrue(ball.getDy() < 0, "Bóng phải nảy LÊN (dy < 0) sau khi chạm gạch");
    }

    /**
     * Kiểm tra kịch bản (scenario) va chạm với chính giữa Paddle.
     * <p>
     * Theo logic vật lý trong {@code Ball.bounceOff(Paddle)},
     * va chạm ở tâm (hoặc trong "vùng chết" - dead zone)
     * phải dẫn đến góc nảy gần như 0 độ (dx=0, dy=-1),
     * tức là bóng bay thẳng đứng lên trên.
     */
    @Test
    @DisplayName("Bóng nảy khỏi GIỮA Paddle")
    void testBounceOffPaddleCenter() {
        // --- Sắp xếp (Arrange) ---
        // Paddle (x: 200, width: 120) -> tâm ở 260
        Paddle paddle = new Paddle(200, 400, 120, 20, "skin");

        // Bóng (x: 251, width: 18) -> tâm ở 260
        ball.setX(251);
        ball.setY(380); // Đặt bóng ngay trên tâm paddle

        // --- Hành động (Act) ---
        ball.bounceOff(paddle); // Va chạm

        // --- Xác minh (Assert) ---
        // DX phải gần bằng 0, DY phải gần bằng -1 (bay thẳng lên)
        // (Sử dụng delta 0.1 để cho phép sai số nhỏ do tính toán float)
        assertEquals(0.0, ball.getDx(), 0.1, "DX phải gần bằng 0 khi nảy ở tâm");
        assertEquals(-1.0, ball.getDy(), 0.1, "DY phải gần bằng -1 khi nảy ở tâm");
    }

    /**
     * Kiểm tra kịch bản (scenario) va chạm với mép phải của Paddle.
     * <p>
     * Va chạm ở mép phải phải dẫn đến góc nảy dương (ví dụ: 30-60 độ),
     * khiến bóng bay LÊN (dy < 0) và sang PHẢI (dx > 0).
     */
    @Test
    @DisplayName("Bóng nảy khỏi MÉP PHẢI Paddle")
    void testBounceOffPaddleFarRight() {
        // --- Sắp xếp (Arrange) ---
        // Paddle (x: 200, width: 120) -> mép phải ở 320
        Paddle paddle = new Paddle(200, 400, 120, 20, "skin");

        // Đặt bóng ở mép phải paddle (x: 300)
        ball.setX(300);
        ball.setY(380);

        // --- Hành động (Act) ---
        ball.bounceOff(paddle); // Va chạm

        // --- Xác minh (Assert) ---
        assertTrue(ball.getDx() > 0, "DX phải là số dương (bay sang phải)");
        assertTrue(ball.getDy() < 0, "DY phải là số âm (bay lên)");
    }

    /**
     * Kiểm tra kịch bản (scenario) va chạm với mép trái của Paddle.
     * <p>
     * Va chạm ở mép trái phải dẫn đến góc nảy âm (ví dụ: -30 đến -60 độ),
     * khiến bóng bay LÊN (dy < 0) và sang TRÁI (dx < 0).
     */
    @Test
    @DisplayName("Bóng nảy khỏi MÉP TRÁI Paddle")
    void testBounceOffPaddleFarLeft() {
        // --- Sắp xếp (Arrange) ---
        // Paddle (x: 200, width: 120) -> mép trái ở 200
        Paddle paddle = new Paddle(200, 400, 120, 20, "skin");

        // Đặt bóng ở mép trái paddle (x: 190)
        ball.setX(190);
        ball.setY(380);

        // --- Hành động (Act) ---
        ball.bounceOff(paddle); // Va chạm

        // --- Xác minh (Assert) ---
        assertTrue(ball.getDx() < 0, "DX phải là số âm (bay sang trái)");
        assertTrue(ball.getDy() < 0, "DY phải là số âm (bay lên)");
    }
}
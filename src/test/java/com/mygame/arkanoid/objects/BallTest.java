package com.mygame.arkanoid.objects;

// Import các lớp cần thiết cho test
import com.mygame.arkanoid.systems.ScalingManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm tra logic vật lý của Bóng (Ball)")
class BallTest {

    private Ball ball;

    // !!! BƯỚC QUAN TRỌNG CỦA TEST CẤP 2 !!!
    // Hàm này chạy MỘT LẦN DUY NHẤT trước tất cả các test
    @BeforeAll
    static void setupGlobal() {
        // Chúng ta phải "giả lập" game đã khởi động
        // và khởi tạo ScalingManager với kích thước màn hình
        ScalingManager.getInstance().update(1120, 720);
    }

    // Hàm này chạy trước mỗi @Test
    @BeforeEach
    void setUp() {
        // Tạo một quả bóng mới cho mỗi test
        ball = new Ball(300, 300, 18, 18, "skin");
        // Giả lập bóng đang bay xuống
        ball.setDx(0);
        ball.setDy(1);
    }

    @Test
    @DisplayName("Bóng nảy khỏi GẠCH (logic chung)")
    void testBounceOffBrick() {
        // Gạch ở ngay dưới quả bóng
        Paddle brick = new Paddle(300, 320, 45, 20, "skin"); // Dùng Paddle làm "gạch" cũng được

        ball.setDy(1); // Đang bay xuống
        ball.bounceOff(brick); // Va chạm

        // Logic bounceOff chung (không phải Paddle) sẽ đảo chiều dy
        assertTrue(ball.getDy() < 0, "Bóng phải nảy LÊN (dy < 0) sau khi chạm gạch");
    }

    @Test
    @DisplayName("Bóng nảy khỏi GIỮA Paddle")
    void testBounceOffPaddleCenter() {
        // Paddle (x: 200, width: 120) -> tâm ở 260
        Paddle paddle = new Paddle(200, 400, 120, 20, "skin");

        // Bóng (x: 251, width: 18) -> tâm ở 260
        ball.setX(251);
        ball.setY(380); // Đặt bóng ngay trên tâm paddle

        ball.bounceOff(paddle); // Va chạm

        // Theo logic code của bạn, góc nảy (reflectAngleDeg) sẽ RẤT GẦN 0
        // sin(0) = 0, cos(0) = 1
        // dx = 0, dy = -1 (bay thẳng lên)
        assertEquals(0.0, ball.getDx(), 0.1, "DX phải gần bằng 0 khi nảy ở tâm");
        assertEquals(-1.0, ball.getDy(), 0.1, "DY phải gần bằng -1 khi nảy ở tâm");
    }

    @Test
    @DisplayName("Bóng nảy khỏi MÉP PHẢI Paddle")
    void testBounceOffPaddleFarRight() {
        // Paddle (x: 200, width: 120) -> mép phải ở 320
        Paddle paddle = new Paddle(200, 400, 120, 20, "skin");

        // Đặt bóng ở mép phải paddle (x: 300)
        ball.setX(300);
        ball.setY(380);

        ball.bounceOff(paddle); // Va chạm

        // Theo logic code, góc nảy sẽ lớn (ví dụ 60-80 độ)
        // sin(góc > 0) -> dx > 0 (bay sang phải)
        // -cos(góc > 0) -> dy < 0 (bay lên)
        assertTrue(ball.getDx() > 0, "DX phải là số dương (bay sang phải)");
        assertTrue(ball.getDy() < 0, "DY phải là số âm (bay lên)");
    }

    @Test
    @DisplayName("Bóng nảy khỏi MÉP TRÁI Paddle")
    void testBounceOffPaddleFarLeft() {
        // Paddle (x: 200, width: 120) -> mép trái ở 200
        Paddle paddle = new Paddle(200, 400, 120, 20, "skin");

        // Đặt bóng ở mép trái paddle (x: 190)
        ball.setX(190);
        ball.setY(380);

        ball.bounceOff(paddle); // Va chạm

        // Theo logic code, góc nảy sẽ âm (ví dụ -60 đến -80 độ)
        // sin(góc < 0) -> dx < 0 (bay sang trái)
        // -cos(góc < 0) -> dy < 0 (bay lên)
        assertTrue(ball.getDx() < 0, "DX phải là số âm (bay sang trái)");
        assertTrue(ball.getDy() < 0, "DY phải là số âm (bay lên)");
    }
}
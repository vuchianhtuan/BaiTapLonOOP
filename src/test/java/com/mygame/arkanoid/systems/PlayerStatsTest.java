package com.mygame.arkanoid.systems;

// Import các thư viện JUnit 5 cần thiết
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Import lớp chúng ta muốn test
import com.mygame.arkanoid.config.GameConstants;

/**
 * Lớp kiểm thử (test class) JUnit 5 dành cho {@link PlayerStats}.
 * <p>
 * Lớp này xác minh rằng logic tính toán và quản lý chỉ số
 * (điểm, mạng, thời gian, reset) hoạt động chính xác
 * trong các kịch bản (scenarios) khác nhau.
 */
@DisplayName("Kiểm tra logic của PlayerStats")
class PlayerStatsTest {

    /** Đối tượng PlayerStats (SUT - System Under Test) cho mỗi kiểm thử. */
    private PlayerStats stats;

    /**
     * Hàm này sẽ chạy TRƯỚC MỖI @Test.
     * <p>
     * Khởi tạo một đối tượng {@code PlayerStats} mới ("sạch")
     * cho mỗi kiểm thử để đảm bảo tính độc lập (isolation)
     * và tránh các kiểm thử ảnh hưởng lẫn nhau.
     */
    @BeforeEach
    void setUp() {
        // Tạo một đối tượng PlayerStats mới tinh cho mỗi test
        // để đảm bảo các test không ảnh hưởng lẫn nhau.
        stats = new PlayerStats();
    }

    /**
     * Kiểm tra rằng các giá trị (điểm, mạng, thời gian)
     * được khởi tạo chính xác theo giá trị mặc định
     * ngay sau khi constructor được gọi.
     */
    @Test
    @DisplayName("Kiểm tra chỉ số ban đầu khi mới tạo")
    void testInitialStats() {
        // assertXXX(expected, actual, "Message if failed")
        assertEquals(0, stats.getScore(), "Điểm ban đầu phải là 0");

        // Sử dụng hằng số từ GameConstants để kiểm tra
        assertEquals(GameConstants.PLAYER_STARTING_LIVES, stats.getLives(), "Mạng sống ban đầu phải đúng");
        assertEquals(0, stats.getPlaytimeMillis(), "Thời gian chơi ban đầu phải là 0");
    }

    /**
     * Kiểm tra logic cộng dồn (accumulation) của
     * phương thức {@link PlayerStats#addScore(int)}.
     * Xác minh rằng điểm số tăng chính xác sau nhiều lần gọi.
     */
    @Test
    @DisplayName("Kiểm tra việc cộng điểm")
    void testAddScore() {
        // --- Kịch bản 1: Cộng lần đầu ---
        stats.addScore(100);
        assertEquals(100, stats.getScore(), "Cộng 100 điểm lần 1 thất bại");

        // --- Kịch bản 2: Cộng dồn ---
        stats.addScore(50);
        assertEquals(150, stats.getScore(), "Cộng 50 điểm lần 2 (tổng 150) thất bại");
    }

    /**
     * Kiểm tra logic giảm mạng sống (lives) của
     * phương thức {@link PlayerStats#loseLife()}.
     * Xác minh rằng số mạng giảm chính xác sau nhiều lần gọi.
     */
    @Test
    @DisplayName("Kiểm tra việc mất mạng")
    void testLoseLife() {
        int initialLives = stats.getLives(); // Lấy số mạng ban đầu
        stats.loseLife();
        assertEquals(initialLives - 1, stats.getLives(), "Mất 1 mạng thất bại");

        stats.loseLife();
        stats.loseLife();
        assertEquals(initialLives - 3, stats.getLives(), "Mất 3 mạng thất bại");
    }

    /**
     * Kiểm tra phương thức {@link PlayerStats#resetForNewGame()}.
     * <p>
     * Đầu tiên, "làm bẩn" (dirty) đối tượng {@code stats} bằng cách
     * thay đổi các giá trị của nó.
     * Sau đó, gọi {@code resetForNewGame()} và xác minh rằng
     * tất cả các chỉ số đã quay trở về giá trị mặc định ban đầu.
     */
    @Test
    @DisplayName("Kiểm tra reset game mới (resetForNewGame)")
    void testResetForNewGame() {
        // 1. Giả lập một game đang chơi dở (Arrange)
        stats.addScore(5000);
        stats.setPlaytimeMillis(100000);
        stats.loseLife();
        stats.loseLife();

        // 2. Gọi hàm reset (Act)
        stats.resetForNewGame();

        // 3. Kiểm tra mọi thứ đã về mặc định (Assert)
        assertEquals(0, stats.getScore(), "Reset điểm thất bại");
        assertEquals(GameConstants.PLAYER_STARTING_LIVES, stats.getLives(), "Reset mạng sống thất bại");
        assertEquals(0, stats.getPlaytimeMillis(), "Reset thời gian chơi thất bại");
    }
}
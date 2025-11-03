package com.mygame.arkanoid.systems;

// Import các thư viện JUnit 5 cần thiết
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Import lớp chúng ta muốn test
import com.mygame.arkanoid.config.GameConstants;

@DisplayName("Kiểm tra logic của PlayerStats")
class PlayerStatsTest {

    private PlayerStats stats;

    // Hàm này sẽ chạy TRƯỚC MỖI @Test
    @BeforeEach
    void setUp() {
        // Tạo một đối tượng PlayerStats mới tinh cho mỗi test
        // để đảm bảo các test không ảnh hưởng lẫn nhau.
        stats = new PlayerStats();
    }

    @Test
    @DisplayName("Kiểm tra chỉ số ban đầu khi mới tạo")
    void testInitialStats() {
        // assertXXX(expected, actual, "Message if failed")
        assertEquals(0, stats.getScore(), "Điểm ban đầu phải là 0");

        // Sử dụng hằng số từ GameConstants để kiểm tra
        assertEquals(GameConstants.PLAYER_STARTING_LIVES, stats.getLives(), "Mạng sống ban đầu phải đúng");
        assertEquals(0, stats.getPlaytimeMillis(), "Thời gian chơi ban đầu phải là 0");
    }

    @Test
    @DisplayName("Kiểm tra việc cộng điểm")
    void testAddScore() {
        stats.addScore(100);
        assertEquals(100, stats.getScore(), "Cộng 100 điểm lần 1 thất bại");

        stats.addScore(50);
        assertEquals(150, stats.getScore(), "Cộng 50 điểm lần 2 (tổng 150) thất bại");
    }

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

    @Test
    @DisplayName("Kiểm tra reset game mới (resetForNewGame)")
    void testResetForNewGame() {
        // 1. Giả lập một game đang chơi dở
        stats.addScore(5000);
        stats.setPlaytimeMillis(100000);
        stats.loseLife();
        stats.loseLife();

        // 2. Gọi hàm reset
        stats.resetForNewGame();

        // 3. Kiểm tra mọi thứ đã về mặc định
        assertEquals(0, stats.getScore(), "Reset điểm thất bại");
        assertEquals(GameConstants.PLAYER_STARTING_LIVES, stats.getLives(), "Reset mạng sống thất bại");
        assertEquals(0, stats.getPlaytimeMillis(), "Reset thời gian chơi thất bại");
    }
}
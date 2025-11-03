package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.config.GameConstants;

/**
 * Quản lý và theo dõi tất cả các chỉ số (stats) của người chơi trong
 * một phiên (session) chơi game.
 * <p>
 * Lớp này hoạt động như một "POJO" (Plain Old Java Object) có logic,
 * đóng gói các dữ liệu như điểm, mạng, thời gian chơi, và cung cấp
 * các phương thức tiện ích ({@code addScore}, {@code loseLife})
 * để thao tác an toàn với các dữ liệu đó.
 */
public class PlayerStats {

    // --- Chỉ số phiên (Session Stats) ---
    /** Tổng điểm số của toàn bộ phiên chơi. */
    private int score;
    /** Số mạng (lives) còn lại. */
    private int lives;
    /** Tổng thời gian chơi (tính bằng mili giây) của toàn bộ phiên. */
    private long playtimeMillis;
    /** Dấu thời gian (timestamp) nano giây của lần cập nhật (update) cuối cùng.
     * Dùng để tính toán delta time (thời gian trôi qua giữa các frame). */
    private long lastUpdateTime;

    // --- Chỉ số màn chơi (Level Stats) ---
    /** Thời gian chơi (tính bằng mili giây) chỉ tính trong màn chơi (level) hiện tại. */
    private long currentLevelPlaytimeMillis;
    /** Điểm số chỉ tính trong màn chơi (level) hiện tại. */
    private int currentLevelScore;

    // --- Chỉ số cuối cùng (Final Stats) ---
    /** Điểm số cuối cùng, được "chốt" (capture) khi game kết thúc (thắng/thua). */
    private int finalScore;
    /** Tổng thời gian chơi cuối cùng, được "chốt" khi game kết thúc. */
    private long finalPlaytimeMillis;

    /**
     * Khởi tạo đối tượng PlayerStats.
     * Tự động gọi {@link #resetForNewGame()} để đặt các giá trị mặc định ban đầu.
     */
    public PlayerStats() {
        // Khởi tạo giá trị mặc định
        resetForNewGame();
    }

    /**
     * Cập nhật (tăng) bộ đếm thời gian chơi.
     * Được gọi mỗi frame bởi {@link com.mygame.arkanoid.core.GameManager}.
     *
     * @param deltaMillis Thời gian (tính bằng mili giây) đã trôi qua
     * kể từ frame trước.
     */
    public void updatePlaytime(long deltaMillis) {
        this.playtimeMillis += deltaMillis;
        this.currentLevelPlaytimeMillis += deltaMillis;
    }

    /**
     * Cộng điểm vào cả tổng điểm (session) và điểm của màn chơi (level) hiện tại.
     *
     * @param points Số điểm cần cộng.
     */
    public void addScore(int points) {
        this.score += points;
        this.currentLevelScore += points;
    }

    /**
     * Trừ 1 mạng sống (life).
     */
    public void loseLife() {
        this.lives--;
    }

    /**
     * Thiết lập lại (reset) tất cả các chỉ số về giá trị mặc định ban đầu.
     * Được gọi khi bắt đầu một lượt chơi mới (New Game).
     */
    public void resetForNewGame() {
        this.playtimeMillis = 0;
        this.lastUpdateTime = System.nanoTime(); // Đặt lại đồng hồ delta time
        this.lives = GameConstants.PLAYER_STARTING_LIVES;
        this.score = 0;
        this.currentLevelScore = 0;
        this.currentLevelPlaytimeMillis = 0;
        this.finalScore = 0;
        this.finalPlaytimeMillis = 0;
    }

    /**
     * Chỉ thiết lập lại (reset) các chỉ số thuộc về màn chơi (level).
     * Được gọi khi người chơi hoàn thành một màn và chuyển sang màn tiếp theo.
     * (Tổng điểm, mạng, và tổng thời gian chơi được giữ nguyên).
     */
    public void resetForNextLevel() {
        this.currentLevelScore = 0;
        this.currentLevelPlaytimeMillis = 0;
    }

    /**
     * "Chốt" (snapshot) điểm số và thời gian chơi hiện tại vào các biến
     * {@code finalScore} và {@code finalPlaytimeMillis}.
     * Được gọi khi trò chơi kết thúc (GAME_OVER hoặc GAME_WIN).
     */
    public void captureFinalStats() {
        this.finalScore = this.score;
        this.finalPlaytimeMillis = this.playtimeMillis;
    }

    // --- Getters và Setters ---
    // (Bỏ qua Javadoc cho các phương thức getter/setter đơn giản)

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public long getPlaytimeMillis() { return playtimeMillis; }
    public void setPlaytimeMillis(long playtimeMillis) { this.playtimeMillis = playtimeMillis; }

    public long getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }

    public long getCurrentLevelPlaytimeMillis() { return currentLevelPlaytimeMillis; }
    public void setCurrentLevelPlaytimeMillis(long time) { this.currentLevelPlaytimeMillis = time; }

    public int getCurrentLevelScore() { return currentLevelScore; }

    public int getFinalScore() { return finalScore; }

    public long getFinalPlaytimeMillis() { return finalPlaytimeMillis; }

    /**
     * Phương thức tiện ích (helper) để khôi phục (restore)
     * các chỉ số chính từ một file save ({@link com.mygame.arkanoid.save.SaveData}).
     *
     * @param score Tổng điểm.
     * @param lives Số mạng.
     * @param playtime Tổng thời gian chơi.
     * @param levelPlaytime Thời gian chơi của màn hiện tại.
     */
    public void setSessionStats(int score, int lives, long playtime, long levelPlaytime) {
        this.score = score;
        this.lives = lives;
        this.playtimeMillis = playtime;
        this.currentLevelPlaytimeMillis = levelPlaytime;
    }
}
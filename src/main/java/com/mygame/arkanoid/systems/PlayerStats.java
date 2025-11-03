package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.util.config.GameConstants;

/**
 * Quản lý chỉ số người chơi trong game Arkanoid.
 */
public class PlayerStats {

    // --- Chỉ số người chơi ---
    private int score;
    private int lives;
    private long playtimeMillis;
    private long lastUpdateTime;
    private long currentLevelPlaytimeMillis;
    private int currentLevelScore;

    private int finalScore;
    private long finalPlaytimeMillis;

    public PlayerStats() {
        // Khởi tạo giá trị mặc định
        resetForNewGame();
    }

    /**
     * Cập nhật bộ đếm thời gian.
     * Được gọi mỗi frame bởi GameManager.
     */
    public void updatePlaytime(long deltaMillis) {
        this.playtimeMillis += deltaMillis;
        this.currentLevelPlaytimeMillis += deltaMillis;
    }

    /**
     * Cộng điểm vào tổng điểm và điểm của màn hiện tại.
     */
    public void addScore(int points) {
        this.score += points;
        this.currentLevelScore += points;
    }

    /**
     * Trừ 1 mạng sống.
     */
    public void loseLife() {
        this.lives--;
    }

    /**
     * Thiết lập lại mọi chỉ số khi bắt đầu game mới.
     */
    public void resetForNewGame() {
        this.playtimeMillis = 0;
        this.lastUpdateTime = System.nanoTime(); // Cập nhật thời điểm bắt đầu
        this.lives = GameConstants.PLAYER_STARTING_LIVES;
        this.score = 0;
        this.currentLevelScore = 0;
        this.currentLevelPlaytimeMillis = 0;
        this.finalScore = 0;
        this.finalPlaytimeMillis = 0;
    }

    /**
     * Chỉ thiết lập lại chỉ số của màn chơi khi qua màn.
     */
    public void resetForNextLevel() {
        this.currentLevelScore = 0;
        this.currentLevelPlaytimeMillis = 0;
    }

    /**
     * Ghi lại điểm và thời gian cuối cùng khi game kết thúc.
     */
    public void captureFinalStats() {
        this.finalScore = this.score;
        this.finalPlaytimeMillis = this.playtimeMillis;
    }

    // --- Getters và Setters ---

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

    public void setSessionStats(int score, int lives, long playtime, long levelPlaytime) {
        this.score = score;
        this.lives = lives;
        this.playtimeMillis = playtime;
        this.currentLevelPlaytimeMillis = levelPlaytime;
    }
}
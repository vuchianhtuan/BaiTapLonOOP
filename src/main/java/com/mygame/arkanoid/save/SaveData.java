package com.mygame.arkanoid.save;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// LỚP DỮ LIỆU LƯU TRẠNG THÁI GAME
public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    // Versioning để tương thích về sau
    private int version = 1;

    // Game progress
    private int levelIndex;
    private int score;
    private int lives;

    // Thời gian chơi
    private long playtimeMillis;              // Tổng thời gian phiên chơi
    private long currentLevelPlaytimeMillis;  // Thời gian của màn hiện tại

    // Trạng thái có thể Continue
    private boolean canContinue;

    // Dấu thời gian lưu
    private long savedAtEpochMillis;


    // NEW: danh sách ID gạch còn sống (bao gồm cả LaserShooter và gạch Boss)
    private List<Integer> aliveBrickIds = new ArrayList<>();

    public SaveData() {}

    // Getters/Setters
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public int getLevelIndex() { return levelIndex; }
    public void setLevelIndex(int levelIndex) { this.levelIndex = levelIndex; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public long getPlaytimeMillis() { return playtimeMillis; }
    public void setPlaytimeMillis(long playtimeMillis) { this.playtimeMillis = playtimeMillis; }

    public long getCurrentLevelPlaytimeMillis() { return currentLevelPlaytimeMillis; }
    public void setCurrentLevelPlaytimeMillis(long currentLevelPlaytimeMillis) {
        this.currentLevelPlaytimeMillis = currentLevelPlaytimeMillis;
    }

    public boolean isCanContinue() { return canContinue; }
    public void setCanContinue(boolean canContinue) { this.canContinue = canContinue; }

    public long getSavedAtEpochMillis() { return savedAtEpochMillis; }
    public void setSavedAtEpochMillis(long savedAtEpochMillis) { this.savedAtEpochMillis = savedAtEpochMillis; }


    public List<Integer> getAliveBrickIds() { return aliveBrickIds; }
    public void setAliveBrickIds(List<Integer> aliveBrickIds) { this.aliveBrickIds = aliveBrickIds; }

}

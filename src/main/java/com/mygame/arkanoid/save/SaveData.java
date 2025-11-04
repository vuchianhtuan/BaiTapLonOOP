package com.mygame.arkanoid.save;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Đại diện cho cấu trúc dữ liệu "sạch" (Plain Old Java Object - POJO)
 * dùng để lưu trữ trạng thái (state) của trò chơi.
 * <p>
 * Lớp này triển khai (implements) {@link Serializable} để cho phép
 * Java tuần tự hóa (serialize) toàn bộ đối tượng này thành một dòng byte,
 * sẵn sàng để ghi ra tệp tin (file).
 * <p>
 * Nó chỉ chứa dữ liệu, không chứa logic nghiệp vụ (business logic).
 */
public class SaveData implements Serializable {
    /**
     * Mã phiên bản (version ID) duy nhất cho việc tuần tự hóa.
     * Nếu bạn thay đổi cấu trúc của lớp này (ví dụ: thêm/xóa/đổi tên trường),
     * bạn nên thay đổi giá trị này để các file save cũ (với serialVersionUID cũ)
     * không được tải (gây ra InvalidClassException).
     */
    private static final long serialVersionUID = 1L;

    /** Phiên bản (version) của logic save, dùng để xử lý tương thích (migration) về sau. */
    private int version = 1;

    // --- Trạng thái tiến trình (Game progress) ---
    /** Chỉ số (index) của màn chơi (level) hiện tại (ví dụ: 0, 1, 2...). */
    private int levelIndex;
    /** Tổng điểm số của người chơi. */
    private int score;
    /** Số mạng (lives) còn lại của người chơi. */
    private int lives;

    // --- Thời gian chơi ---
    /** Tổng thời gian đã chơi trong toàn bộ phiên (session) này (tính bằng mili giây). */
    private long playtimeMillis;
    /** Thời gian đã chơi chỉ trong màn (level) hiện tại (tính bằng mili giây). */
    private long currentLevelPlaytimeMillis;

    /** Cờ (flag) cho biết file save này có thể được "Tiếp tục" (Continue) từ menu không. */
    private boolean canContinue;
    /** Dấu thời gian (timestamp) khi file save này được tạo (tính bằng epoch milliseconds). */
    private long savedAtEpochMillis;
    /**
     * Danh sách các ID duy nhất của những viên gạch (Brick) *còn sống*
     * trong màn chơi tại thời điểm lưu.
     */
    private List<Integer> aliveBrickIds = new ArrayList<>();

    /**
     * Constructor mặc định (không tham số).
     * Bắt buộc phải có để thư viện tuần tự hóa (như Jackson hoặc Java Serialization)
     * hoạt động chính xác.
     */
    public SaveData() {}

    // --- Getters/Setters ---

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
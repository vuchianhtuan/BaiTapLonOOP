package com.mygame.arkanoid.level;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý việc tải và chuyển đổi giữa các màn chơi (levels) trong game.
 */
public class LevelManager {
    private List<String> levelFiles;
    private int currentLevelIndex;
    private Level currentLevel;

    public LevelManager() {
        levelFiles = new ArrayList<>();
        currentLevelIndex = -1; // Bắt đầu từ -1
    }

    /**
     * Tải danh sách các file màn chơi.
     */
    public void loadLevels() {
        levelFiles.add("/levels/Level1.txt");
        levelFiles.add("/levels/Level2.txt");
        levelFiles.add("/levels/Level_Boss.txt");
        // Thêm các file level khác nếu có
    }

    /**
     * Tải trực tiếp một màn chơi theo chỉ số (index).
     * @param index Chỉ số của màn (0, 1, 2...)
     * @return true nếu tải thành công, false nếu chỉ số không hợp lệ.
     */
    public boolean loadSpecificLevel(int index) {
        if (index >= 0 && index < levelFiles.size()) {
            currentLevelIndex = index; // Cập nhật chỉ số hiện tại
            String levelFile = levelFiles.get(currentLevelIndex);

            // Thử tải level từ file
            try {
                currentLevel = new Level(levelFile);
                return true;
            } catch (IllegalArgumentException e) {
                System.err.println("Không thể tải level (không hợp lệ): " + e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("Lỗi khi tải level: " + e.getMessage());
                e.printStackTrace();
            }

            currentLevel = null;
            return false;
        }
        System.err.println("Lỗi: Chỉ số level không hợp lệ: " + index);
        currentLevel = null; // Đặt level hiện tại là null nếu lỗi
        return false; // Chỉ số không hợp lệ
    }
    /**
     * Đặt màn chơi hiện tại và tải nó.
     * @param index Chỉ số của màn muốn đặt (0, 1, 2...)
     */
    public void setCurrentLevel(int index) {
        // Không cần trừ 1 nữa, gọi trực tiếp loadSpecificLevel
        loadSpecificLevel(index);
    }

    /**
     * Tải level tiếp theo trong danh sách.
     * @return true nếu còn level và tải thành công, false nếu hết level.
     */
    public boolean loadNextLevel() {
        currentLevelIndex++; // Tăng chỉ số để lấy level tiếp theo
        if (hasMoreLevels()) {
            String nextLevelFile = levelFiles.get(currentLevelIndex);
            try {
                currentLevel = new Level(nextLevelFile);
                return true;
            } catch (RuntimeException e) {
                System.err.println("Không thể tải level tiếp theo: " + e.getMessage());
                e.printStackTrace();
                currentLevel = null;
            }
        }
        currentLevel = null; // Đặt là null khi hết level
        return false; // Không còn level nào
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public boolean hasMoreLevels() {
        return currentLevelIndex < levelFiles.size();
    }

    public void reset() {
        currentLevelIndex = -1; // Reset để loadNextLevel bắt đầu từ 0
        currentLevel = null;
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }
}
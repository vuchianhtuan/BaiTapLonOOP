package com.mygame.arkanoid.systems;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    /*private List<Level> levels;
    private int currentLevelIndex;

    public void loadLevels() {}
    public Level getCurrentLevel() { return null; }
    public void nextLevel() {}
    public boolean hasMoreLevels() { return false; }
    */

    private List<String> levelFiles;
    private int currentLevelIndex;
    private Level currentLevel;

    public LevelManager() {
        levelFiles = new ArrayList<>();
        currentLevelIndex = -1; // Bắt đầu từ -1 để khi gọi nextLevel() lần đầu sẽ là 0
    }

    // Tải danh sách các file level
    public void loadLevels() {
        levelFiles.add("/levels/Level1.txt");
        //levelFiles.add("/levels/Level2.txt");
        //levelFiles.add("/levels/Level3.txt");
        levelFiles.add("/levels/Level_Boss.txt");
    }

    // Tải level tiếp theo
    public boolean loadNextLevel() {
        currentLevelIndex++;
        if (hasMoreLevels()) {
            String nextLevelFile = levelFiles.get(currentLevelIndex);
            currentLevel = new Level(nextLevelFile);
            return true;
        }
        return false; // Không còn level nào
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    // Kiểm tra xem còn màn chơi tiếp theo không
    public boolean hasMoreLevels() {
        return currentLevelIndex < levelFiles.size();
    }

    // Reset về màn đầu tiên khi chơi lại
    public void reset() {
        currentLevelIndex = -1;
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }
}
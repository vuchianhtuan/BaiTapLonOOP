package com.mygame.arkanoid.systems;

import java.util.List;

public class LevelManager {
    private List<Level> levels;
    private int currentLevelIndex;

    public void loadLevels() {}
    public Level getCurrentLevel() { return null; }
    public void nextLevel() {}
    public boolean hasMoreLevels() { return false; }
}
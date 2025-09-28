package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.util.ErrorHandler;

import java.util.List;

public class Level {
    private int levelNumber;
    private List<Brick> bricks;

    public void loadLevel() {
        try {
            // load data
        } catch (Exception e) {
            ErrorHandler.log("Lỗi load level: " + e.getMessage());
        }
    }

    public boolean isCompleted() { return false; }
    public List<Brick> getBricks() { return bricks; }
}

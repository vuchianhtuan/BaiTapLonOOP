package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GamePanel;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.*;

public class ScoreManager {
    private int currentScore;
    private int highScore;
    private Leaderboard leaderboard;
    private Image BackgroundImage;


    public ScoreManager() {
        currentScore = 0;
        highScore = 0;
        leaderboard = new Leaderboard();
        BackgroundImage = AssetManager.getInstance().getImage("scoreBackground");
    }
    public void addScore(int points) {
        currentScore += points;
    }
    public int getCurrentScore() { return currentScore; }
    public int getHighScore() { return highScore; }
    public void resetScore() {
        currentScore = 0;
    }

    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();

        // Vẽ nền co giãn để lấp đầy toàn bộ cửa sổ
        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage, 0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        // (Sau này bạn sẽ thêm logic vẽ điểm số ở đây, nhớ dùng sm.scale...())
    }
}
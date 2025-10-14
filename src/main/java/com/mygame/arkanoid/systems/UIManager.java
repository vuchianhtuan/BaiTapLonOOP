package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;

import java.awt.*;

public class UIManager {
    private GameManager gameManager;
    private HeartUI heartUI;
    private GameOverScreen gameOverScreen;
    private ScoreUI scoreUI;
    // Sau này có thể thêm ScoreUI, TimerUI...
    // private ScoreUI scoreUI;

    public UIManager(GameManager gm) {
        this.gameManager = gm;
        this.heartUI = new HeartUI(gm);
        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 600;

        int imageWidth = 490;
        int imageHeight = 280;

        int x = (SCREEN_WIDTH - imageWidth) / 2;
        int y = (SCREEN_HEIGHT - imageHeight) / 2;

        this.gameOverScreen = new GameOverScreen(x, y, imageWidth, imageHeight);

        this.scoreUI = new ScoreUI(gm);
    }

    public void update() {
        heartUI.update();
        if ("GAME_OVER".equals(gameManager.getGameState())) {
            gameOverScreen.update(); // <-- THÊM DÒNG NÀY
        }

        scoreUI.update();
    }

    public void draw(Graphics d) {
        heartUI.draw(d);
        if ("GAME_OVER".equals(gameManager.getGameState())) {
            // Thay vì vẽ chữ, giờ ta vẽ animation
            gameOverScreen.draw(d); // <-- THAY ĐỔI DÒNG NÀY
        }
    }

    public void draw(Graphics2D g2d) {
        // Vẽ tất cả các thành phần UI
        heartUI.draw(g2d);
        scoreUI.draw(g2d);
    }

}
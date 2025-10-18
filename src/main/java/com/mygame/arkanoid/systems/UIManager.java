package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.core.GamePanel;

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

        int imageWidth = 490;
        int imageHeight = 280;

        int x = (GamePanel.WIDTH - imageWidth) / 2;
        int y = (GamePanel.HEIGHT - imageHeight) / 2;

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
        Graphics2D g2d = (Graphics2D) d;
        scoreUI.draw(g2d);
        if ("GAME_OVER".equals(gameManager.getGameState())) {
            gameOverScreen.draw(d);
        }
    }
}
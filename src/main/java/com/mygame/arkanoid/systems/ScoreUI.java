package com.mygame.arkanoid.systems;

// File: UI.java (trong package systems.ui)
import com.mygame.arkanoid.core.GameManager;

import java.awt.*;

public class ScoreUI {
    GameManager gameManager;
    public ScoreUI(GameManager gm) {
        this.gameManager = gm;
    }

    public void update() {
        // Hiện chưa có logic gì để update
    }

    public void draw(Graphics2D g) {
        String scoreText = "Score: " + gameManager.getScore();
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString(scoreText, 350, 30);
    }
}

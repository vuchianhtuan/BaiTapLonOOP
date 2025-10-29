package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.core.GamePanel;

import java.awt.*;

public class UIManager {
    private GameManager gameManager;
    private HeartUI heartUI;
    private ScoreUI scoreUI;

    public UIManager(GameManager gm) {
        this.gameManager = gm;
        this.heartUI = new HeartUI(gm);
        this.scoreUI = new ScoreUI(gm);
    }

    public void update() {
        heartUI.update();
        scoreUI.update();
    }

    public void draw(Graphics d) {
        heartUI.draw(d);
        Graphics2D g2d = (Graphics2D) d;
        scoreUI.draw(g2d);
    }
}
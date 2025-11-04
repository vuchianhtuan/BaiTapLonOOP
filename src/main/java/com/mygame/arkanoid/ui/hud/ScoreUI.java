package com.mygame.arkanoid.ui.hud;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;

/**
 * Lớp ScoreUI quản lý hiển thị điểm số trong giao diện người dùng.
 */
public class ScoreUI {
    GameManager gameManager;
    public ScoreUI(GameManager gm) {
        this.gameManager = gm;
    }

    public void update() {}

    /**
     * Vẽ điểm số lên màn hình.
     */
    public void draw(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();

        String scoreText = "SCORE";
        String scoreValue = String.valueOf(gameManager.getScore());
        int logicX = 980; // 960 (game) + 20 (padding)
        int logicY_title = 100;
        int logicY_value = 130;

        Font baseFont = new Font("Arial", Font.BOLD, 24);
        Font scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));

        g.setFont(scaledFont);
        g.setColor(Color.WHITE);
        g.drawString(scoreText, sm.scaleX(logicX), sm.scaleY(logicY_title));

        baseFont = new Font("Arial", Font.PLAIN, 22);
        scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));

        g.setFont(scaledFont);
        g.drawString(scoreValue, sm.scaleX(logicX), sm.scaleY(logicY_value));
    }
}

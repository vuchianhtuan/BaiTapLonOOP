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

    public void draw(Graphics g) { // Nên dùng Graphics thay vì Graphics2D cho nhất quán
        ScalingManager sm = ScalingManager.getInstance();

        String scoreText = "Score: " + gameManager.getScore();
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);

        // Tọa độ logic
        int logicX = 580; // Căn giữa trong thế giới 1280
        int logicY = 30;

        // Vẽ ra màn hình với tọa độ đã được scale
        g.drawString(scoreText, sm.scaleX(logicX), sm.scaleY(logicY));
    }
}

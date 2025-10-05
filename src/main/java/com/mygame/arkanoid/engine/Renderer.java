package com.mygame.arkanoid.engine;
import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.*;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

/*
public class Renderer {
    private AssetManager assetManager;
    public void draw(GameObject obj) {}
}
*/

public class Renderer {
    public Renderer() {}

    public void renderGame(Graphics g, Paddle paddle, Ball ball, List<Brick> bricks, List<PowerUp> powerUps) {
        // 1. Vẽ background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);

        // 2. Ra lệnh cho từng đối tượng tự vẽ
        if (paddle != null) {
            paddle.render(g);
        }
        if (ball != null) {
            ball.render(g);
        }
        if (bricks != null) {
            for (Brick brick : bricks) {
                brick.render(g);
            }
        }
        if (powerUps != null) {
            for (PowerUp powerUp : powerUps) {
                powerUp.render(g);
            }
        }
    }
}
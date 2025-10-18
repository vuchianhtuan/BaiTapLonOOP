package com.mygame.arkanoid.engine;
import com.mygame.arkanoid.core.GamePanel;
import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.*;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class Renderer {
    public Renderer() {}

    public void renderGame(Graphics g, Paddle paddle, Ball ball, List<Brick> bricks,
                           List<PowerUp> powerUps, List<Ball> balls, Boss boss,
                           List<Laser> lasers, List<LaserShooterBrick> laserShooters, Image background) {
        ScalingManager sm = ScalingManager.getInstance();
        if (background != null) {
            // Vẽ ảnh nền co giãn lấp đầy màn hình
            g.drawImage(background, 0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT), null);
        } else {
            // Phương án dự phòng (nếu ảnh nền là null)
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        if (bricks != null) {
            for (Brick brick : bricks) {
                brick.render(g, sm);
            }
        }

        if (laserShooters != null) {
            for (LaserShooterBrick shooter : laserShooters) {
                shooter.render(g, sm);
            }
        }

        if (boss != null) {
            for (Brick brick : boss.getBricks()) {
                brick.render(g, sm);
            }
        }

        if (paddle != null) {
            paddle.render(g, sm);
        }

        // 4. Vẽ power-ups
        if (powerUps != null) {
            for (PowerUp powerUp : powerUps) {
                powerUp.render(g, sm);
            }
        }

        if (balls != null) {
            for (Ball b : balls) {
                b.render(g, sm);
            }
        }

        // 6. Vẽ laser
        if (lasers != null) {
            for (Laser laser : lasers) {
                laser.render(g, sm);
            }
        }
    }
}
package com.mygame.arkanoid.engine;

import com.mygame.arkanoid.effects.Shard;
import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.*;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.Image;
import java.awt.Graphics;
import java.util.List;

public class Renderer {
    public Renderer() {}

    // THAY ĐỔI: Thêm LevelTransition transition vào danh sách tham số
    public void renderGame(Graphics g, Paddle paddle, Ball ball, List<Brick> bricks,
                           List<PowerUp> powerUps, List<Ball> balls, Boss boss,
                           List<Laser> lasers, List<LaserShooterBrick> laserShooters, Image background, List<Shard> shards) {
        ScalingManager sm = ScalingManager.getInstance();

        // 1. Vẽ tất cả gạch thường (bao gồm cả gạch của Boss)
        if (bricks != null) {
            for (Brick brick : bricks) {
                brick.render(g, sm);
            }
        }

        // 2. Vẽ tất cả gạch laser (bao gồm cả gạch laser của Boss)
        if (laserShooters != null) {
            for (LaserShooterBrick shooter : laserShooters) {
                shooter.render(g, sm);
            }
        }

        // 3. Vẽ Paddle
        if (paddle != null) {
            paddle.render(g, sm);
        }

        // 4. Vẽ power-ups
        if (powerUps != null) {
            for (PowerUp powerUp : powerUps) {
                powerUp.render(g, sm);
            }
        }

        // 5. Vẽ Balls
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

        // 7. Vẽ các mảnh vỡ (shards)
        if (shards != null) {
            for (Shard shard : shards) {
                shard.render(g, sm);
            }
        }
    }
}
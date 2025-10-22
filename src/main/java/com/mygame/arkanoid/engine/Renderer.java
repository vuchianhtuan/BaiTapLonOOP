package com.mygame.arkanoid.engine;// package com.mygame.arkanoid.engine;
import com.mygame.arkanoid.core.GamePanel;
import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.*;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.systems.LevelTransition; // <-- IMPORT MỚI
import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class Renderer {
    public Renderer() {}

    // THAY ĐỔI: Thêm LevelTransition transition vào danh sách tham số
    public void renderGame(Graphics g, Paddle paddle, Ball ball, List<Brick> bricks,
                           List<PowerUp> powerUps, List<Ball> balls, Boss boss,
                           List<Laser> lasers, List<LaserShooterBrick> laserShooters, BackButton backButton, Image background) {
                           List<Laser> lasers, List<LaserShooterBrick> laserShooters,
                           Image background, List<Shard> shard, LevelTransition transition) { // <-- THAM SỐ MỚI

        ScalingManager sm = ScalingManager.getInstance();

        // --- LOGIC HIỂN THỊ HÌNH NỀN ---
        if (background != null) {
            g.drawImage(background, 0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        // --- QUẢN LÝ HIỆU ỨNG GẠCH XUẤT HIỆN ---
        LevelTransition.State transitionState = transition.getCurrentState();
        int maxBricksToRender = Integer.MAX_VALUE;
        boolean isBrickSpawning = transitionState == LevelTransition.State.BRICK_SPAWN;

        if (isBrickSpawning) {
            // Lấy số lượng gạch tối đa được phép vẽ trong frame này
            maxBricksToRender = transition.getBrickSpawnCount();
        }

        int brickIndex = 0;

        // 1. Vẽ Gạch Thường (bricks)
        if (bricks != null) {
            for (Brick brick : bricks) {
                if (isBrickSpawning && brickIndex >= maxBricksToRender) break; // Dừng nếu đã đủ số gạch cho phép
                brick.render(g, sm);
                brickIndex++;
            }
        }

        // 2. Vẽ Gạch Bắn Laser (laserShooters)
        if (laserShooters != null) {
            for (LaserShooterBrick shooter : laserShooters) {
                if (isBrickSpawning && brickIndex >= maxBricksToRender) break; // Dừng nếu đã đủ số gạch cho phép
                shooter.render(g, sm);
                brickIndex++;
            }
        }

        // 3. Vẽ Gạch Boss (boss bricks)
        // Gạch boss thường không xuất hiện dần dần, nhưng nếu muốn thì cần thay đổi Boss.getBricks()
        // Để đơn giản, ta chỉ vẽ gạch boss nếu *toàn bộ* gạch thường đã xuất hiện
        // Hoặc khi không còn trong pha BRICK_SPAWN nữa.
        if (boss != null && !isBrickSpawning) {
            for (Brick brick : boss.getBricks()) {
                brick.render(g, sm);
            }
        }

        // --- VẼ CÁC THÀNH PHẦN KHÔNG BỊ ẢNH HƯỞNG BỞI BRICK_SPAWN ---

        // Vẽ Paddle
        if (paddle != null) {
            // Khi đang ở pha PADDLE_FLY_UP, Paddle sẽ được vẽ ở vị trí đã update trong LevelTransition
            paddle.render(g, sm);
        }

        // Vẽ Mảnh Vỡ (Shard)
        if (shard != null) {
            for (Shard s : shard) {
                s.render(g, sm);
            }
        }

        // Vẽ Power-ups, Ball, Laser (Chỉ nên vẽ khi không còn trong pha chuyển cảnh tối/sáng)
        if (transitionState != LevelTransition.State.FADE_TO_BLACK &&
                transitionState != LevelTransition.State.FADE_FROM_BLACK) {

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

            if (lasers != null) {
                for (Laser laser : lasers) {
                    laser.render(g, sm);
                }
            }
        }

        if (backButton != null) {
            backButton.draw(g, sm);
        }
    }
}
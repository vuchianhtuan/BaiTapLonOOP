package com.mygame.arkanoid.systems.helper;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.SoundManager;
import com.mygame.arkanoid.objects.Ball;
import com.mygame.arkanoid.objects.Laser;
import com.mygame.arkanoid.objects.Paddle;
import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.objects.powerups.PowerUpFactory;
import com.mygame.arkanoid.objects.powerups.PowerUpType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CollisionSystem {

    // Danh sách tạm để tránh tạo mới mỗi frame
    private List<Brick> allTargets = new ArrayList<>();

    /**
     * Phương thức chính, gọi tất cả các hàm kiểm tra va chạm con.
     */
    public void checkAllCollisions(GameManager gm) {
        Paddle paddle = gm.getPaddle();
        int screenHeight = gm.getScreenHeight(); // Lấy từ trường package-private của GameManager

        if (paddle == null) return; // Không thể va chạm nếu không có paddle

        // 1. Va chạm giữa Laser và Paddle (và ra khỏi màn hình)
        checkLaserVsPaddle(gm, paddle, screenHeight);

        // 2. Va chạm giữa Ball và Paddle
        checkBallVsPaddle(gm, paddle);

        // 3. Va chạm giữa PowerUp (đang rơi) và Paddle
        checkPowerUpVsPaddle(gm, paddle, screenHeight);

        // 4. Va chạm giữa Ball và Gạch
        checkBallVsBricks(gm);
    }

    private void checkLaserVsPaddle(GameManager gm, Paddle paddle, int screenHeight) {
        Iterator<Laser> laserIterator = gm.getLasers().iterator();
        while (laserIterator.hasNext()) {
            Laser laser = laserIterator.next();

            if (laser.getBounds().intersects(paddle.getBounds())) {
                int lives = gm.getLives() - 1;
                gm.setLives(lives);
                laserIterator.remove();
                if (lives <= 0) {
                    // setGameState sẽ tự động xử lý gameOverTimer
                    gm.setGameState("GAME_OVER");
                }
                continue; // Laser đã bị xóa, không cần kiểm tra ra khỏi màn hình
            }

            if (laser.getY() > screenHeight) {
                laserIterator.remove();
            }
        }
    }

    private void checkBallVsPaddle(GameManager gm, Paddle paddle) {
        for (Ball b : gm.getBalls()) {
            if(b.checkCollision(paddle) && !b.isStuckToPaddle()) {
                gm.getSoundManager().playSound(SoundManager.SFX_PADDLE_HIT);
                if (paddle.isSticky()) {
                    b.stickToPaddle(paddle);
                } else {
                    b.bounceOff(paddle);
                }
            }
        }
    }

    private void checkPowerUpVsPaddle(GameManager gm, Paddle paddle, int screenHeight) {
        // gm.getPowerUps() là danh sách power-up đang rơi
        Iterator<PowerUp> fallingPowerUpIterator = gm.getPowerUps().iterator();
        while (fallingPowerUpIterator.hasNext()) {
            PowerUp p = fallingPowerUpIterator.next();

            if (paddle.getBounds().intersects(p.getBounds())) {
                // Kích hoạt power-up mới
                gm.activatePowerUp(p); // <-- Phương thức này cần được đổi thành public
                fallingPowerUpIterator.remove(); // Xóa khỏi danh sách đang rơi
            }
            // Nếu power-up rơi ra ngoài màn hình
            else if (p.getY() > screenHeight) {
                fallingPowerUpIterator.remove();
            }
        }
    }

    private void checkBallVsBricks(GameManager gm) {
        // Xây dựng danh sách mục tiêu
        allTargets.clear();
        allTargets.addAll(gm.getLaserShooters());
        allTargets.addAll(gm.getBricks());
        // Lưu ý: Giả định gạch của Boss đã được thêm vào danh sách `bricks` khi tải level

        for (Ball b : gm.getBalls()) {
            Iterator<Brick> targetIterator = allTargets.iterator();
            while (targetIterator.hasNext()) {
                Brick target = targetIterator.next();

                // Chỉ kiểm tra va chạm với những viên gạch chưa bị phá hủy
                if (!target.isDestroyed() && b.checkCollision(target)) {
                    gm.getSoundManager().playSound(SoundManager.SFX_BRICK_HIT);
                    boolean detonatedImmediately = false;

                    // 1. KIỂM TRA GẠCH NỔ
                    if (target instanceof ExplosiveBrick) {
                        ExplosiveBrick eb = (ExplosiveBrick) target;
                        if (eb.isAwaitingDetonation()) {
                            eb.detonateOnHit(gm.getActiveShards());
                            gm.addScore(10); // <-- Chúng ta sẽ thêm phương thức này vào GameManager
                            b.bounceOff(target);
                            detonatedImmediately = true;
                        }
                    }

                    // 2. XỬ LÝ VA CHẠM THƯỜNG
                    if (!detonatedImmediately) {
                        if (target.getHitPoints() > 1) {
                            target.shatterHit(gm.getActiveShards());
                        }

                        target.takeHit();
                        gm.addScore(10); // <-- Dùng phương thức mới
                        b.bounceOff(target);

                        // B. KIỂM TRA PHÁ HỦY HOÀN TOÀN
                        if (target.isDestroyed()) {
                            if (!(target instanceof ExplosiveBrick)) {
                                gm.getActiveShards().addAll(target.shatter());
                            }

                            // Logic thả PowerUp
                            PowerUpType typeToDrop = gm.getLevelManager().getCurrentLevel().getRandomPowerUpType();
                            if (typeToDrop != null) {
                                PowerUp newPowerUp = PowerUpFactory.create(typeToDrop, target.getX(), target.getY());
                                if (newPowerUp != null) {
                                    gm.addPowerUp(newPowerUp);
                                }
                            }

                            // Logic sinh LaserShooter
                            if (!(target instanceof LaserShooterBrick) && !(target instanceof ExplosiveBrick)) {
                                double spawnRate = gm.getLevelManager().getCurrentLevel().getLaserShooterSpawnRate();
                                if (Math.random() < spawnRate) {
                                    LaserShooterBrick newShooter = new LaserShooterBrick(target.getX(), target.getY(), target.getWidth(), target.getHeight(), 2);
                                    gm.addLaserShooter(newShooter);
                                }
                            }
                        }
                    }
                    // Bắt buộc ngắt vòng lặp khi va chạm xảy ra
                    break;
                }
            }
        }
    }
}
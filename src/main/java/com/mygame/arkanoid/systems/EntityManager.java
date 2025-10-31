package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntityManager {

    private Paddle paddle;
    private Ball ball; // Bóng chính (dùng khi reset)
    private int ballSize;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> stagingBricks = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>(); // Power-up đang rơi
    private List<PowerUp> activePowerUps = new ArrayList<>(); // Power-up đang kích hoạt
    private List<Shard> activeShards = new ArrayList<>();
    private List<HeartUI> hearts = new ArrayList<>();
    private List<Laser> lasers = new ArrayList<>();
    private Boss boss;
    private List<LaserShooterBrick> laserShooters = new ArrayList<>();

    public EntityManager() {
        // Constructor để trống, các list đã được khởi tạo
    }

    /**
     * Cập nhật logic di chuyển/hành vi của TẤT CẢ các đối tượng game.
     */
    public void updateAll(InputHandler inputHandler) {
        if (paddle != null) {
            paddle.update(inputHandler);
        }

        for (Ball b : balls) {
            b.update(inputHandler, paddle);
        }

        for (Brick brick : bricks) {
            if (brick instanceof MovingBrick) {
                ((MovingBrick) brick).update(bricks);
            }
            brick.update();
        }

        for (LaserShooterBrick shooter : laserShooters) {
            shooter.update();
            Laser newLaser = shooter.tryToShoot();
            if (newLaser != null) {
                lasers.add(newLaser);
            }
        }

        for (Laser laser : lasers) {
            laser.update();
        }

        for (PowerUp p : powerUps) {
            p.update(); // Cập nhật rơi
        }

        if (boss != null) {
            boss.update();
        }

        activeShards.removeIf(s -> {
            s.update();
            return !s.isAlive();
        });
    }

    /**
     * Dọn dẹp tất cả các đối tượng đã bị "hủy" hoặc ra khỏi màn hình.
     */
    public void cleanupDestroyedObjects(int screenHeight) {
        bricks.removeIf(brick -> {
            if (brick instanceof ExplosiveBrick) {
                return ((ExplosiveBrick) brick).isFinished();
            }
            return brick.isDestroyed();
        });

        laserShooters.removeIf(Brick::isDestroyed);

        if (boss != null) {
            boss.removeDestroyedBricks();
        }

        balls.removeIf(b -> b.getY() > screenHeight + 50);
    }

    /**
     * Kiểm tra điều kiện thắng (không còn gạch).
     */
    public boolean isLevelWon() {
        if (boss != null) {
            return boss.isDefeated() && bricks.isEmpty() && laserShooters.isEmpty();
        } else {
            return bricks.isEmpty() && laserShooters.isEmpty();
        }
    }

    /**
     * Dọn dẹp và thiết lập lại các đối tượng cho một màn chơi mới.
     * (Đây là logic từ setupLevelObjects() cũ)
     */
    public void resetForNewLevel(String paddleSkinKey, String ballSkinKey) {
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        int nativeHeight = ScalingManager.getInstance().NATIVE_HEIGHT;

        int paddleWidth = 120;
        this.ballSize = 18;

        int finalPaddleX = (gameAreaWidth / 2) - (paddleWidth / 2);
        int spawnPaddleY = nativeHeight + 20;

        paddle = new Paddle(finalPaddleX, spawnPaddleY, paddleWidth, 20, paddleSkinKey);
        ball = new Ball(finalPaddleX + (paddleWidth / 2) - (ballSize / 2), spawnPaddleY - ballSize - 1, ballSize, ballSize, ballSkinKey);
        ball.resetBallPosition(paddle);

        balls.clear();
        balls.add(ball);
        powerUps.clear();
        lasers.clear();
        boss = null;
        bricks.clear();
        laserShooters.clear();
        activeShards.clear();
        activePowerUps.clear(); // Xóa cả power-up đang kích hoạt
    }

    /**
     * Nạp gạch từ Level data vào các danh sách
     */
    public void hydrateLevel(Level currentLevel, Boss newBoss) {
        this.boss = newBoss; // Nhận boss đã được tạo

        bricks.clear();
        laserShooters.clear();

        List<Brick> source = new ArrayList<>();
        source.addAll(currentLevel.getBricks());
        if (boss != null) {
            source.addAll(boss.getBricks());
        }

        for (Brick brickToSpawn : source) {
            if (brickToSpawn instanceof LaserShooterBrick) {
                laserShooters.add((LaserShooterBrick) brickToSpawn);
            } else {
                bricks.add(brickToSpawn);
            }
        }
    }

    /**
     * Nạp gạch từ SaveData
     */
    public void hydrateFromSave(Level currentLevel, Boss loadedBoss, Set<Integer> aliveBrickIds) {
        this.boss = loadedBoss;
        bricks.clear();
        laserShooters.clear();

        List<Brick> allBricksInLevel = new ArrayList<>(currentLevel.getBricks());
        if (boss != null) {
            allBricksInLevel.addAll(boss.getBricks());
        }

        for (Brick b : allBricksInLevel) {
            if (aliveBrickIds.contains(b.getId())) {
                if (b instanceof LaserShooterBrick) {
                    laserShooters.add((LaserShooterBrick) b);
                } else {
                    bricks.add(b);
                }
            }
        }
    }

    public void respawnBall(String ballSkinKey) {
        if (paddle == null) return; // Không thể hồi sinh nếu không có paddle

        int size = (this.ballSize > 0) ? this.ballSize : 18; // Dùng ballSize nội bộ

        // Tạo bóng mới
        Ball newBall = new Ball(
                paddle.getX() + (paddle.getWidth() / 2) - (size / 2),
                paddle.getY() - size - 1,
                size,
                size,
                ballSkinKey
        );

        newBall.stickToPaddle(paddle);

        this.balls.add(newBall); // Thêm vào danh sách
        this.ball = newBall; // Cập nhật tham chiếu "bóng chính"
    }

    // --- Getters (Để các hệ thống khác truy cập) ---
    public List<Ball> getBalls() { return balls; }
    public Paddle getPaddle() { return paddle; }
    public Ball getBall() { return ball; }
    public List<Brick> getBricks() { return bricks; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public List<PowerUp> getActivePowerUps() { return activePowerUps; }
    public List<Shard> getActiveShards() { return activeShards; }
    public List<Laser> getLasers() { return lasers; }
    public List<LaserShooterBrick> getLaserShooters() { return laserShooters; }
    public Boss getBoss() { return boss; }
    public List<Brick> getStagingBricks() { return stagingBricks; }
    public int getBallSize() { return ballSize; }

    // --- Helpers ---
    public boolean areBallsEmpty() { return balls.isEmpty(); }
    public void addBall(Ball b) { balls.add(b); }
    public void addLaserShooter(LaserShooterBrick b) { laserShooters.add(b); }
    public void addPowerUp(PowerUp p) { powerUps.add(p); }
    public void addActivePowerUp(PowerUp p) { activePowerUps.add(p); }

    public void setPaddleSkin(String skinKey) {
        if (paddle != null) {
            paddle.setImageName(skinKey);
        }
    }
    public void setBallSkin(String skinKey) {
        if (balls != null) {
            for (Ball b : balls) {
                if (b != null) b.setImageName(skinKey);
            }
        }
    }
}
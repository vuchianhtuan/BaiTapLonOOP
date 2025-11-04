package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.effects.Shard;
import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.level.Level;
import com.mygame.arkanoid.config.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Quản lý vòng đời của tất cả các đối tượng trong game.
 */
public class EntityManager {

    // --- Danh sách các đối tượng trong game ---
    private Paddle paddle;
    private Ball ball; // Bóng chính (dùng khi reset)
    private int ballSize;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> stagingBricks = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>(); // Power-up đang rơi
    private List<PowerUp> activePowerUps = new ArrayList<>(); // Power-up đang kích hoạt
    private List<Shard> activeShards = new ArrayList<>();
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

        laserShooters.removeIf(Brick::isDestroyed); // Xóa gạch bắn laser đã bị hủy

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

        int paddleWidth = GameConstants.PADDLE_WIDTH;
        int paddleHeight = GameConstants.PADDLE_HEIGHT;
        this.ballSize = GameConstants.BALL_SIZE;

        // Tính vị trí xuất hiện của paddle và bóng
        int finalPaddleX = (gameAreaWidth / 2) - (paddleWidth / 2);
        int spawnPaddleY = nativeHeight + 20;

        // Tạo paddle và bóng mới
        paddle = new Paddle(finalPaddleX, spawnPaddleY, paddleWidth, paddleHeight, paddleSkinKey);
        ball = new Ball(finalPaddleX + (paddleWidth / 2) - (ballSize / 2), spawnPaddleY - ballSize - 1, ballSize, ballSize, ballSkinKey);
        ball.resetBallPosition(paddle);

        // Xóa tất cả các đối tượng cũ
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
    public void hydrateLevel(Level currentLevel) {
        Boss newBoss = null;
        // Tạo Boss nếu có
        if (currentLevel.isBossLevel() && !currentLevel.getBossBricks().isEmpty()) {
            java.awt.Rectangle bossBounds = currentLevel.getBossInitialBounds();
            float startX = (ScalingManager.getInstance().GAME_AREA_WIDTH / 2.0f) - (bossBounds.width / 2.0f);
            float startY = bossBounds.y;

            newBoss = new Boss(currentLevel.getBossBricks(), startX, startY, bossBounds.x, ScalingManager.getInstance().GAME_AREA_WIDTH);
        }

        this.boss = newBoss; // Gán boss vừa tạo

        // Gán ID cho gạch (Logic đã chuyển từ GameManager)
        // Chúng ta phải gán ID cho gạch GỐC trong 'currentLevel'
        int idCounter = 0;
        for (Brick b : currentLevel.getBricks()) {
            if (b.getId() < 0) b.setId(idCounter++);
        }
        if (this.boss != null) {
            for (Brick b : this.boss.getBricks()) {
                if (b.getId() < 0) b.setId(idCounter++);
            }
        }

        // Nạp gạch vào danh sách
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
     * Nạp gạch từ SaveData.
     */
    public void hydrateFromSave(Level currentLevel, Set<Integer> aliveBrickIds) {
        bricks.clear();
        laserShooters.clear();

        List<Brick> allBricksInLevel = new ArrayList<>(currentLevel.getBricks());
        if (boss != null) {
            allBricksInLevel.addAll(boss.getBricks()); // Thêm gạch của boss nếu có
        }

        for (Brick b : allBricksInLevel) {
            if (aliveBrickIds.contains(b.getId())) {
                if (b instanceof LaserShooterBrick) {
                    laserShooters.add((LaserShooterBrick) b); // Thêm vào danh sách gạch bắn laser
                } else {
                    bricks.add(b); // Thêm vào danh sách gạch thường
                }
            }
        }
    }

    /**
     * Hồi sinh một quả bóng mới và đặt nó lên paddle.
     */
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

    /**
     * Thay đổi skin của paddle.
     */
    public void setPaddleSkin(String skinKey) {
        if (paddle != null) {
            paddle.setImageName(skinKey);
        }
    }

    /**
     * Thay đổi skin của tất cả các quả bóng.
     */
    public void setBallSkin(String skinKey) {
        if (balls != null) {
            for (Ball b : balls) {
                if (b != null) b.setImageName(skinKey);
            }
        }
    }
    public void setPaddle(Paddle paddle) {
        this.paddle = paddle;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
    }
}
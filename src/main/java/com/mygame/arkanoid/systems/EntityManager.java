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
 * Quản lý vòng đời (lifecycle) của tất cả các thực thể (entities) trong game.
 * <p>
 * Lớp này hoạt động như một "container" trung tâm cho tất cả các đối tượng
 * game đang hoạt động. Nó chịu trách nhiệm:
 * <ul>
 * <li>Lưu trữ các danh sách đối tượng (Paddles, Balls, Bricks, v.v.).</li>
 * <li>Gọi phương thức {@code update()} của tất cả các đối tượng (trong {@link #updateAll}).</li>
 * <li>Dọn dẹp (cleanup) các đối tượng đã bị phá hủy hoặc ra khỏi màn hình
 * (trong {@link #cleanupDestroyedObjects}).</li>
 * <li>Nạp (hydrate) các đối tượng từ {@link Level}
 * hoặc {@link com.mygame.arkanoid.save.SaveData}.</li>
 * </ul>
 * Đây là một phần cốt lõi của mẫu thiết kế ECS (Entity Component System),
 * cụ thể là phần "System" (Hệ thống) và "Entity" (Thực thể).
 */
public class EntityManager {

    // --- Danh sách các đối tượng (thực thể) trong game ---
    private Paddle paddle;
    /** Tham chiếu đến quả bóng chính (thường là quả bóng cuối cùng được tạo). */
    private Ball ball;
    private int ballSize; // Kích thước bóng chuẩn
    private List<Ball> balls = new ArrayList<>();
    /** Danh sách gạch chờ (staging) dùng trong hiệu ứng chuyển cảnh (transition). */
    private List<Brick> stagingBricks = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>(); // Power-up đang rơi
    private List<PowerUp> activePowerUps = new ArrayList<>(); // Power-up đang có hiệu lực
    private List<Shard> activeShards = new ArrayList<>();
    private List<Laser> lasers = new ArrayList<>();
    private Boss boss;
    private List<LaserShooterBrick> laserShooters = new ArrayList<>();

    public EntityManager() {
        // Constructor để trống, các list đã được khởi tạo
    }

    /**
     * Cập nhật logic di chuyển/hành vi của TẤT CẢ các thực thể đang hoạt động.
     * Được gọi mỗi frame bởi {@link com.mygame.arkanoid.core.GameManager}.
     *
     * @param inputHandler Trình xử lý đầu vào, được truyền cho các đối tượng
     * cần nó (Paddle, Ball).
     */
    public void updateAll(InputHandler inputHandler) {
        // Cập nhật Paddle (nhận input)
        if (paddle != null) {
            paddle.update(inputHandler);
        }

        // Cập nhật Balls (nhận input + paddle để kiểm tra dính)
        for (Ball b : balls) {
            b.update(inputHandler, paddle);
        }

        // Cập nhật gạch (đặc biệt là MovingBrick)
        for (Brick brick : bricks) {
            if (brick instanceof MovingBrick) {
                // MovingBrick cần danh sách gạch khác để kiểm tra va chạm
                ((MovingBrick) brick).update(bricks);
            }
            brick.update(); // Gọi update() chung (cho StrongBrick, ExplosiveBrick)
        }

        // Cập nhật gạch bắn laser (và tạo laser mới)
        for (LaserShooterBrick shooter : laserShooters) {
            shooter.update();
            Laser newLaser = shooter.tryToShoot();
            if (newLaser != null) {
                lasers.add(newLaser);
            }
        }

        // Cập nhật (di chuyển) laser
        for (Laser laser : lasers) {
            laser.update();
        }

        // Cập nhật (rơi) PowerUp
        for (PowerUp p : powerUps) {
            p.update();
        }

        // Cập nhật Boss (di chuyển và đồng bộ gạch con)
        if (boss != null) {
            boss.update();
        }

        // Cập nhật mảnh vỡ (shards) và xóa nếu đã hết "sống"
        activeShards.removeIf(s -> {
            s.update(); // Cập nhật vật lý
            return !s.isAlive(); // Trả về true để xóa nếu không còn sống
        });
    }

    /**
     * Dọn dẹp (cleanup) tất cả các thực thể đã bị "phá hủy" (destroyed)
     * hoặc bay ra khỏi màn hình.
     * Được gọi mỗi frame bởi {@link com.mygame.arkanoid.core.GameManager}.
     *
     * @param screenHeight Chiều cao (logic) của màn hình.
     */
    public void cleanupDestroyedObjects(int screenHeight) {
        // Xóa gạch
        bricks.removeIf(brick -> {
            if (brick instanceof ExplosiveBrick) {
                // Gạch nổ chỉ bị xóa khi hoạt ảnh nổ KẾT THÚC
                return ((ExplosiveBrick) brick).isFinished();
            }
            // Gạch thường/cứng bị xóa ngay khi hết máu
            return brick.isDestroyed();
        });

        // Xóa gạch bắn laser đã hết máu
        laserShooters.removeIf(Brick::isDestroyed);

        // Yêu cầu Boss tự dọn dẹp các gạch con đã bị phá hủy
        if (boss != null) {
            boss.removeDestroyedBricks();
        }

        // Xóa bóng đã bay ra khỏi đáy màn hình (mất mạng)
        balls.removeIf(b -> b.getY() > screenHeight + 50);
    }

    /**
     * Kiểm tra điều kiện thắng màn chơi.
     * @return true nếu tất cả gạch (và boss, nếu có) đã bị phá hủy.
     */
    public boolean isLevelWon() {
        if (boss != null) {
            // Nếu là màn boss: phải thắng boss VÀ dọn sạch gạch thường/laser
            return boss.isDefeated() && bricks.isEmpty() && laserShooters.isEmpty();
        } else {
            // Nếu là màn thường: chỉ cần dọn sạch gạch thường/laser
            return bricks.isEmpty() && laserShooters.isEmpty();
        }
    }

    /**
     * Dọn dẹp (xóa) tất cả các thực thể và
     * thiết lập lại Paddle/Ball cho một màn chơi mới.
     *
     * @param paddleSkinKey Key (mã) của skin paddle sẽ sử dụng.
     * @param ballSkinKey Key (mã) của skin bóng sẽ sử dụng.
     */
    public void resetForNewLevel(String paddleSkinKey, String ballSkinKey) {
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        int nativeHeight = ScalingManager.getInstance().NATIVE_HEIGHT;

        int paddleWidth = GameConstants.PADDLE_WIDTH;
        int paddleHeight = GameConstants.PADDLE_HEIGHT;
        this.ballSize = GameConstants.BALL_SIZE;

        // Tính vị trí xuất hiện của paddle (ngoài màn hình, chờ bay vào)
        int finalPaddleX = (gameAreaWidth / 2) - (paddleWidth / 2);
        int spawnPaddleY = nativeHeight + 20;

        // Tạo paddle và bóng mới
        paddle = new Paddle(finalPaddleX, spawnPaddleY, paddleWidth, paddleHeight, paddleSkinKey);
        ball = new Ball(finalPaddleX + (paddleWidth / 2) - (ballSize / 2), spawnPaddleY - ballSize - 1, ballSize, ballSize, ballSkinKey);
        ball.resetBallPosition(paddle); // Gắn bóng vào paddle (ở vị trí chờ)

        // Xóa tất cả các đối tượng cũ
        balls.clear();
        balls.add(ball);
        powerUps.clear();
        lasers.clear();
        boss = null;
        bricks.clear();
        laserShooters.clear();
        activeShards.clear();
        activePowerUps.clear(); // Xóa cả power-up đang có hiệu lực
    }

    /**
     * Nạp (Hydrate) các thực thể (gạch, boss) từ đối tượng {@link Level} đã tải.
     * <p>
     * <b>Quan trọng:</b> Phương thức này cũng chịu trách nhiệm gán ID duy nhất
     * cho mỗi gạch (để dùng cho hệ thống save/load).
     *
     * @param currentLevel Đối tượng Level chứa dữ liệu gạch thô.
     */
    public void hydrateLevel(Level currentLevel) {
        Boss newBoss = null;
        // 1. Tạo Boss (nếu đây là màn boss)
        if (currentLevel.isBossLevel() && !currentLevel.getBossBricks().isEmpty()) {
            java.awt.Rectangle bossBounds = currentLevel.getBossInitialBounds();
            // Tính vị trí X ban đầu để căn giữa Boss
            float startX = (ScalingManager.getInstance().GAME_AREA_WIDTH / 2.0f) - (bossBounds.width / 2.0f);
            float startY = bossBounds.y;

            newBoss = new Boss(currentLevel.getBossBricks(), startX, startY, bossBounds.x, ScalingManager.getInstance().GAME_AREA_WIDTH);
        }
        this.boss = newBoss; // Gán boss

        // 2. Gán ID duy nhất cho TẤT CẢ gạch trong level (cho save/load)
        // (Chúng ta gán ID cho gạch GỐC trong đối tượng 'currentLevel')
        int idCounter = 0;
        for (Brick b : currentLevel.getBricks()) {
            if (b.getId() < 0) b.setId(idCounter++); // Chỉ gán nếu chưa có ID
        }
        if (this.boss != null) {
            for (Brick b : this.boss.getBricks()) {
                if (b.getId() < 0) b.setId(idCounter++);
            }
        }

        // 3. Nạp gạch vào danh sách hoạt động (bricks, laserShooters)
        bricks.clear();
        laserShooters.clear();

        List<Brick> source = new ArrayList<>();
        source.addAll(currentLevel.getBricks()); // Gạch thường
        if (boss != null) {
            source.addAll(boss.getBricks()); // Gạch của Boss
        }

        // Phân loại gạch vào đúng danh sách
        for (Brick brickToSpawn : source) {
            if (brickToSpawn instanceof LaserShooterBrick) {
                laserShooters.add((LaserShooterBrick) brickToSpawn);
            } else {
                bricks.add(brickToSpawn);
            }
        }
    }

    /**
     * Nạp (Hydrate) trạng thái gạch từ một đối tượng {@link com.mygame.arkanoid.save.SaveData}.
     * <p>
     * Lọc danh sách gạch trong {@code currentLevel}
     * chỉ giữ lại những gạch có ID nằm trong {@code aliveBrickIds}.
     *
     * @param currentLevel Đối tượng Level (đã được tải).
     * @param aliveBrickIds {@link Set} chứa các ID của gạch còn sống (từ file save).
     */
    public void hydrateFromSave(Level currentLevel, Set<Integer> aliveBrickIds) {
        bricks.clear();
        laserShooters.clear();

        // Lấy danh sách TẤT CẢ gạch (bao gồm cả boss) từ định nghĩa level
        List<Brick> allBricksInLevel = new ArrayList<>(currentLevel.getBricks());
        if (boss != null) {
            allBricksInLevel.addAll(boss.getBricks());
        }

        // Duyệt qua tất cả gạch, chỉ thêm lại những gạch "còn sống"
        for (Brick b : allBricksInLevel) {
            if (aliveBrickIds.contains(b.getId())) {
                // Nếu gạch còn sống, phân loại và thêm vào danh sách
                if (b instanceof LaserShooterBrick) {
                    laserShooters.add((LaserShooterBrick) b);
                } else {
                    bricks.add(b);
                }
            }
        }
    }

    /**
     * Hồi sinh (respawn) một quả bóng mới và đặt nó (dính) lên paddle.
     *
     * @param ballSkinKey Key (mã) của skin bóng sẽ sử dụng.
     */
    public void respawnBall(String ballSkinKey) {
        if (paddle == null) return; // Không thể hồi sinh nếu không có paddle

        int size = (this.ballSize > 0) ? this.ballSize : 18; // Dùng ballSize nội bộ

        // Tạo bóng mới tại vị trí giữa paddle
        Ball newBall = new Ball(
                paddle.getX() + (paddle.getWidth() / 2) - (size / 2),
                paddle.getY() - size - 1, // Ngay phía trên paddle
                size,
                size,
                ballSkinKey
        );

        newBall.stickToPaddle(paddle); // Gắn bóng

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

    // --- Helpers (Các phương thức tiện ích) ---
    public boolean areBallsEmpty() { return balls.isEmpty(); }
    public void addBall(Ball b) { balls.add(b); }
    public void addLaserShooter(LaserShooterBrick b) { laserShooters.add(b); }
    public void addPowerUp(PowerUp p) { powerUps.add(p); }
    public void addActivePowerUp(PowerUp p) { activePowerUps.add(p); }

    /**
     * Áp dụng skin (thay đổi hình ảnh) cho paddle hiện tại.
     * @param skinKey Key (mã) của skin paddle mới.
     */
    public void setPaddleSkin(String skinKey) {
        if (paddle != null) {
            paddle.setImageName(skinKey);
        }
    }

    /**
     * Áp dụng skin (thay đổi hình ảnh) cho TẤT CẢ các quả bóng hiện có.
     * @param skinKey Key (mã) của skin bóng mới.
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
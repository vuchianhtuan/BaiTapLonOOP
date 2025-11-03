package com.mygame.arkanoid.systems;

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

/**
 * Hệ thống xử lý va chạm (Collision Detection) trung tâm của game.
 * <p>
 * Lớp này không có trạng thái (stateless) và cung cấp các phương thức
 * để kiểm tra, phát hiện và xử lý va chạm giữa các nhóm đối tượng khác nhau
 * (ví dụ: Ball vs. Paddle, Ball vs. Bricks, Laser vs. Paddle).
 */
public class CollisionSystem {

    /**
     * Danh sách tạm (cache) để chứa tất cả các mục tiêu (gạch)
     * cần kiểm tra va chạm trong một frame.
     * <p>
     * Được tái sử dụng mỗi frame (thông qua {@code clear()}) để tránh
     * việc tạo (allocate) danh sách mới liên tục, giúp giảm tải cho
     * bộ dọn rác (Garbage Collector).
     */
    private List<Brick> allTargets = new ArrayList<>();

    /**
     * Phương thức chính, điều phối tất cả các bước kiểm tra va chạm.
     * Được gọi mỗi frame từ {@link GameManager#updateGame()}.
     *
     * @param gm Tham chiếu đến {@link GameManager} để truy cập các danh sách đối tượng.
     */
    public void checkAllCollisions(GameManager gm) {
        Paddle paddle = gm.getPaddle();
        int screenHeight = gm.getScreenHeight();

        // Nếu paddle không tồn tại (ví dụ: trong lúc chuyển cảnh), không kiểm tra
        if (paddle == null) return;

        // 1. Va chạm giữa Laser (do địch bắn) và Paddle
        checkLaserVsPaddle(gm, paddle, screenHeight);

        // 2. Va chạm giữa Ball và Paddle
        checkBallVsPaddle(gm, paddle);

        // 3. Va chạm giữa PowerUp (đang rơi) và Paddle
        checkPowerUpVsPaddle(gm, paddle, screenHeight);

        // 4. Va chạm giữa Ball và Gạch
        checkBallVsBricks(gm);
    }

    /**
     * Kiểm tra va chạm giữa Laser (do địch bắn) và Paddle.
     * Cũng xử lý việc dọn dẹp (cleanup) các laser bay ra khỏi màn hình.
     *
     * @param gm GameManager.
     * @param paddle Paddle của người chơi.
     * @param screenHeight Chiều cao (logic) của màn hình để kiểm tra biên.
     */
    private void checkLaserVsPaddle(GameManager gm, Paddle paddle, int screenHeight) {
        // Sử dụng Iterator để có thể xóa (remove) phần tử khỏi danh sách
        // một cách an toàn ngay trong lúc duyệt (tránh ConcurrentModificationException).
        Iterator<Laser> laserIterator = gm.getLasers().iterator();
        while (laserIterator.hasNext()) {
            Laser laser = laserIterator.next();

            // 1. Kiểm tra va chạm Laser vs. Paddle
            if (laser.getBounds().intersects(paddle.getBounds())) {
                int lives = gm.getLives() - 1;
                gm.setLives(lives); // Trừ mạng
                laserIterator.remove(); // Xóa laser

                // Kiểm tra Game Over
                if (lives <= 0) {
                    gm.setGameState("GAME_OVER");
                }
                continue; // Laser đã bị xóa, chuyển sang laser tiếp theo
            }

            // 2. Kiểm tra Laser bay ra khỏi màn hình
            if (laser.getY() > screenHeight) {
                laserIterator.remove(); // Xóa laser
            }
        }
    }

    /**
     * Kiểm tra va chạm giữa tất cả các Ball và Paddle.
     * Xử lý logic nảy (bounce) hoặc dính (sticky).
     *
     * @param gm GameManager.
     * @param paddle Paddle của người chơi.
     */
    private void checkBallVsPaddle(GameManager gm, Paddle paddle) {
        for (Ball b : gm.getBalls()) {
            // Chỉ kiểm tra va chạm nếu bóng không đang dính sẵn
            if(b.checkCollision(paddle) && !b.isStuckToPaddle()) {
                gm.getSoundManager().playSound(SoundManager.SFX_PADDLE_HIT);

                // Xử lý logic "Sticky Paddle" PowerUp
                if (paddle.isSticky()) {
                    b.stickToPaddle(paddle);
                } else {
                    // Logic nảy (vật lý)
                    b.bounceOff(paddle);
                }
            }
        }
    }

    /**
     * Kiểm tra va chạm giữa PowerUp đang rơi và Paddle (thu thập).
     * Cũng xử lý việc dọn dẹp PowerUp rơi ra khỏi màn hình.
     *
     * @param gm GameManager.
     * @param paddle Paddle của người chơi.
     * @param screenHeight Chiều cao (logic) của màn hình.
     */
    private void checkPowerUpVsPaddle(GameManager gm, Paddle paddle, int screenHeight) {
        // gm.getPowerUps() là danh sách power-up đang rơi
        // Sử dụng Iterator để xóa an toàn
        Iterator<PowerUp> fallingPowerUpIterator = gm.getPowerUps().iterator();
        while (fallingPowerUpIterator.hasNext()) {
            PowerUp p = fallingPowerUpIterator.next();

            // 1. Kiểm tra va chạm PowerUp vs. Paddle (thu thập)
            if (paddle.getBounds().intersects(p.getBounds())) {
                // Kích hoạt power-up (thêm vào danh sách active, gỡ bỏ cái cũ, v.v.)
                gm.activatePowerUp(p);
                fallingPowerUpIterator.remove(); // Xóa khỏi danh sách đang rơi
            }
            // 2. Kiểm tra PowerUp rơi ra khỏi màn hình
            else if (p.getY() > screenHeight) {
                fallingPowerUpIterator.remove(); // Xóa
            }
        }
    }

    /**
     * Kiểm tra va chạm giữa tất cả các Ball và tất cả các loại gạch.
     * Đây là phương thức phức tạp nhất, xử lý:
     * - Gạch thường, gạch cứng (StrongBrick), gạch nổ (ExplosiveBrick).
     * - Logic thả PowerUp.
     * - Logic sinh ra LaserShooterBrick.
     *
     * @param gm GameManager.
     */
    private void checkBallVsBricks(GameManager gm) {
        // 1. Xây dựng danh sách mục tiêu (tái sử dụng allTargets)
        allTargets.clear();
        allTargets.addAll(gm.getLaserShooters()); // Gạch bắn laser
        allTargets.addAll(gm.getBricks());       // Gạch thường, gạch nổ, gạch di động...
        // (Giả định gạch của Boss đã được thêm vào gm.getBricks() bởi EntityManager)

        // Duyệt qua từng quả bóng
        for (Ball b : gm.getBalls()) {
            // Duyệt qua từng mục tiêu (gạch)
            Iterator<Brick> targetIterator = allTargets.iterator();
            while (targetIterator.hasNext()) {
                Brick target = targetIterator.next();

                // 2. KIỂM TRA VA CHẠM
                // Chỉ va chạm nếu gạch chưa bị phá hủy và bóng va chạm với nó
                if (!target.isDestroyed() && b.checkCollision(target)) {
                    gm.getSoundManager().playSound(SoundManager.SFX_BRICK_HIT);
                    boolean detonatedImmediately = false; // Cờ cho gạch nổ

                    // 3. XỬ LÝ LOGIC VA CHẠM ĐẶC BIỆT (GẠCH NỔ)
                    if (target instanceof ExplosiveBrick) {
                        ExplosiveBrick eb = (ExplosiveBrick) target;
                        // Nếu gạch nổ này đang trong trạng thái "chờ nổ" (IGNITED)
                        if (eb.isAwaitingDetonation()) {
                            eb.detonateOnHit(gm.getActiveShards()); // Kích nổ ngay
                            gm.addScore(10);
                            b.bounceOff(target); // Bóng nảy ra
                            detonatedImmediately = true; // Đánh dấu đã xử lý
                        }
                    }

                    // 4. XỬ LÝ VA CHẠM THƯỜNG (hoặc gạch nổ chưa bị kích hoạt)
                    if (!detonatedImmediately) {
                        // A. Xử lý trước khi phá hủy (ví dụ: tạo mảnh vỡ nhỏ)
                        if (target.getHitPoints() > 1) {
                            target.shatterHit(gm.getActiveShards()); // Hiệu ứng vỡ
                        }

                        // B. Gây sát thương
                        target.takeHit();
                        gm.addScore(10);
                        b.bounceOff(target); // Bóng nảy ra

                        // C. Xử lý SAU KHI phá hủy (nếu hitPoints <= 0)
                        if (target.isDestroyed()) {
                            // Tạo mảnh vỡ lớn (trừ gạch nổ, vì nó có hiệu ứng riêng)
                            if (!(target instanceof ExplosiveBrick)) {
                                gm.getActiveShards().addAll(target.shatter());
                            }

                            // --- Logic thả PowerUp ---
                            // Lấy loại power-up ngẫu nhiên từ cấu hình level
                            PowerUpType typeToDrop = gm.getLevelManager().getCurrentLevel().getRandomPowerUpType();
                            if (typeToDrop != null) {
                                // Dùng Factory để tạo power-up
                                PowerUp newPowerUp = PowerUpFactory.create(typeToDrop, target.getX(), target.getY());
                                if (newPowerUp != null) {
                                    gm.addPowerUp(newPowerUp); // Thêm vào danh sách đang rơi
                                }
                            }

                            // --- Logic sinh (spawn) LaserShooterBrick ---
                            // Chỉ spawn nếu gạch bị phá hủy KHÔNG PHẢI là LaserShooter hoặc Explosive
                            if (!(target instanceof LaserShooterBrick) && !(target instanceof ExplosiveBrick)) {
                                double spawnRate = gm.getLevelManager().getCurrentLevel().getLaserShooterSpawnRate();
                                if (Math.random() < spawnRate) { // Quyết định ngẫu nhiên
                                    // Tạo gạch bắn laser mới tại vị trí gạch cũ
                                    LaserShooterBrick newShooter = new LaserShooterBrick(target.getX(), target.getY(), target.getWidth(), target.getHeight(), 2);
                                    gm.addLaserShooter(newShooter);
                                }
                            }
                        }
                    }

                    // QUAN TRỌNG: Ngắt vòng lặp (vòng lặp gạch).
                    // Một quả bóng chỉ có thể phá hủy 1 gạch mỗi frame
                    // để tránh bóng đi xuyên qua nhiều gạch (tunneling).
                    break;
                }
            }
        }
    }
}
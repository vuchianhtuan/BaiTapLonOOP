package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Ball;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.systems.LevelManager;
import com.mygame.arkanoid.systems.ScoreManager;
import com.mygame.arkanoid.objects.Paddle;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.engine.SoundManager;
import com.mygame.arkanoid.util.ErrorHandler;
import com.mygame.arkanoid.systems.MenuManager;

import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class GameManager {
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private List<PowerUp> activePowerUps;
    private int score;
    private int lives;
    private String gameState;

    private ScoreManager scoreManager;
    private LevelManager levelManager;
    private Renderer renderer;
    private SoundManager soundManager;
    private InputHandler inputHandler;
    private MenuManager menuManager;

    public void startGame() {
        this.lives = 3;
        paddle = new Paddle(350, 550, 100, 20);
        ball = new Ball(390, 530, 15, 15);
        ball.resetBallPosition(paddle);
        bricks.clear();
        powerUps.clear();

        // Hủy bỏ hiệu ứng power-up cũ khi bắt đầu game mới
        for (PowerUp p : activePowerUps) {
            p.removeEffect(this);
        }
        activePowerUps.clear();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                if (i == 2 && j == 5) {
                    bricks.add(new ExplosiveBrick(j * 70 + 50, i * 30 + 50, 60, 20));
                } else if (i == 4 && j ==5) {
                    bricks.add(new StrongBrick(j * 70 + 50, i * 30 + 50, 60, 20));
                }
                else {
                    bricks.add(new NormalBrick(j * 70 + 50, i * 30 + 50, 60, 20));
                }
            }
        }
        bricks.add(new MovingBrick(400, 200 ,60, 20, 2, 200));

    }

    // Thêm phương thức này vào cuối lớp GameManager
    private void explode(Brick sourceBrick, double radius) {
        // Lấy tọa độ trung tâm của viên gạch nổ
        int sourceCenterX = sourceBrick.getX() + sourceBrick.getWidth() / 2;
        int sourceCenterY = sourceBrick.getY() + sourceBrick.getHeight() / 2;

        // Tạo một danh sách để chứa những viên gạch sẽ bị phá hủy bởi vụ nổ
        List<Brick> bricksToDestroy = new ArrayList<>();

        // Duyệt qua tất cả các viên gạch còn lại
        for (Brick otherBrick : bricks) {
            // Bỏ qua chính viên gạch vừa nổ
            if (otherBrick == sourceBrick) {
                continue;
            }

            int otherCenterX = otherBrick.getX() + otherBrick.getWidth() / 2;
            int otherCenterY = otherBrick.getY() + otherBrick.getHeight() / 2;

            // Tính khoảng cách giữa hai tâm gạch
            double distance = Math.sqrt(Math.pow(sourceCenterX - otherCenterX, 2) + Math.pow(sourceCenterY - otherCenterY, 2));

            // Nếu gạch khác nằm trong bán kính vụ nổ, thêm nó vào danh sách cần phá hủy
            if (distance <= radius) {
                bricksToDestroy.add(otherBrick);
            }
        }

        // Phá hủy và cộng điểm cho những viên gạch trong danh sách
        for (Brick brick : bricksToDestroy) {
            // Đảm bảo không xử lý lặp lại nếu một viên gạch đã bị phá hủy
            if (!brick.isDestroyed()) {
                // Giảm máu của gạch về 0 để chắc chắn nó bị phá hủy
                while(!brick.isDestroyed()) {
                    brick.takeHit();
                }
                score += 10; // Cộng điểm
            }
        }
    }

    public void updateGame() {
        if ("PLAYING".equals(gameState)) {
            paddle.update(inputHandler);
            ball.update(inputHandler, paddle);

            for (Brick brick : bricks) {
                brick.update();
            }

            Iterator<PowerUp> fallingPowerUpIterator = powerUps.iterator();
            while (fallingPowerUpIterator.hasNext()) {
                PowerUp p = fallingPowerUpIterator.next();
                p.update(); // Cho power-up rơi xuống

                if (paddle.getBounds().intersects(p.getBounds())) {
                    // Kích hoạt power-up mới
                    activatePowerUp(p);
                    fallingPowerUpIterator.remove(); // Xóa khỏi danh sách đang rơi
                }
                // Nếu power-up rơi ra ngoài màn hình
                else if (p.getY() > 600) {
                    fallingPowerUpIterator.remove();
                }
            }

            // 2. QUẢN LÝ THỜI GIAN CỦA TẤT CẢ POWER-UP ĐANG HOẠT ĐỘNG
            Iterator<PowerUp> activePowerUpIterator = activePowerUps.iterator();
            while (activePowerUpIterator.hasNext()) {
                PowerUp p = activePowerUpIterator.next();
                p.tick(); // Đếm ngược thời gian

                if (p.isExpired()) {
                    p.removeEffect(this); // Hủy hiệu ứng
                    activePowerUpIterator.remove(); // Xóa khỏi danh sách đang hoạt động
                }
            }

            if(ball.checkCollision(paddle)) {
                if (paddle.isSticky()) {
                    ball.stickToPaddle(paddle);
                } else {
                    ball.bounceOff(paddle);
                }
            }

            Iterator<Brick> brickIterator = bricks.iterator();
            while (brickIterator.hasNext()) {
                Brick brick = brickIterator.next();

                // Chỉ kiểm tra va chạm với những viên gạch chưa bị phá hủy
                if (!brick.isDestroyed() && ball.checkCollision(brick)) {
                    brick.takeHit(); // Gạch nhận sát thương
                    score += 10;
                    ball.bounceOff(brick);

                    // Kiểm tra ngay sau khi nhận sát thương, nếu gạch bị phá hủy thì xóa nó
                    if (brick.isDestroyed()) {
                        double rand = Math.random();
                        if (rand < 0.1) { // 10% ra Expand
                            powerUps.add(new ExpandPaddlePowerUp(brick.getX(), brick.getY(), 30, 30));
                        } else if (rand < 0.2) { // 10% ra Sticky
                            powerUps.add(new StickyPaddlePowerUp(brick.getX(), brick.getY(), 30, 30));
                        } else if (rand < 0.3) { // 10% ra Slow Ball
                            powerUps.add(new SlowBallPowerUp(brick.getX(), brick.getY(), 30, 30));
                        }

                        // (Nếu có logic cũ cho explosive brick thì giữ nguyên)
                        if (brick instanceof ExplosiveBrick) {
                            explode(brick, 100.0);
                        }
                    }
                    break; // Thoát khỏi vòng lặp để bóng không va chạm nhiều gạch trong 1 frame
                }
            }
            bricks.removeIf(brick -> brick.isDestroyed());

            // Xử lý khi bóng rơi xuống đất.
            if (ball.getY() > 600) {
                lives--;
                if (lives > 0) {
                    ball.resetBallPosition(paddle);
                } else {
                    setGameState("MENU");
                }
            }
        } else if ("MENU".equals(gameState)) {
            menuManager.update();
        }
    }

    public void loadAssets() {
        AssetManager.getInstance().loadImage("normalBrick", "/images/button_blue.png");
        AssetManager.getInstance().loadImage("ball", "/images/ball_red_large.png");
        AssetManager.getInstance().loadImage("paddle", "/images/button_yellow.png");
        AssetManager.getInstance().loadImage("expandPowerUp", "/images/hole_small_end.png");
        AssetManager.getInstance().loadImage("menuBackground", "/images/backGroundMenu.png");
        AssetManager.getInstance().loadImage("explosiveBrick", "/images/button_grey.png");
        AssetManager.getInstance().loadImage("stickyPowerUp", "/images/star.png");
        AssetManager.getInstance().loadImage("slowBallPowerUp", "/images/ball_blue_large.png");
        AssetManager.getInstance().loadImage("strongBrick", "/images/strongbrick.png");
        AssetManager.getInstance().loadImage("strongBrick1", "/images/strongbrick1.png");
        AssetManager.getInstance().loadImage("strongBrick2", "/images/strongbrick2.png");

    }

    public GameManager() {
        inputHandler = new InputHandler();
        bricks = new ArrayList<>();
        powerUps = new ArrayList<>();
        activePowerUps = new ArrayList<>();
        this.soundManager = new SoundManager();
        loadAssets();
        menuManager = new MenuManager(this, inputHandler);
        // Đặt trạng thái ban đầu của game là MENU
        this.gameState = "MENU";
        soundManager.playBackgroundMusic("RegressiveTrip_Release.wav");
    }

    public MenuManager getMenuManager() { return menuManager; }
    public String getGameState() { return gameState; }
    public void setGameState(String state) {
        if (this.gameState != null && this.gameState.equals(state)) {
            return;
        }
        this.gameState = state; // Cập nhật trạng thái mới
        // Dựa vào trạng thái mới để đổi nhạc
        if ("PLAYING".equals(state)) {
            // Nếu chuyển sang màn hình chơi, bật nhạc game
            soundManager.playBackgroundMusic("ExoticBaryon_PhaseXX.wav");
        } else if ("MENU".equals(state)) {
            soundManager.playBackgroundMusic("RegressiveTrip_Release.wav");
        } else {
            // Nếu là các trạng thái khác (GAME_OVER,...) thì dừng nhạc
            soundManager.stopBackgroundMusic();
        }
    }

    private void activatePowerUp(PowerUp newPowerUp) {
        // Dùng iterator để có thể xóa phần tử một cách an toàn
        Iterator<PowerUp> iterator = activePowerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp existingPowerUp = iterator.next();
            // Nếu đã có power-up cùng loại đang hoạt động
            if (existingPowerUp.getType().equals(newPowerUp.getType())) {
                // Hủy hiệu ứng cũ và xóa nó khỏi danh sách
                existingPowerUp.removeEffect(this);
                iterator.remove();
            }
        }

        // Thêm power-up mới vào danh sách và áp dụng hiệu ứng
        activePowerUps.add(newPowerUp);
        newPowerUp.applyEffect(this);
    }

    public void handleInput() {}
    public void checkCollisions() {}
    public void gameOver() {}
    public Paddle getPaddle() { return paddle; }
    public Ball getBall() { return ball; }
    public List<Brick> getBricks() { return bricks; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public InputHandler getInputHandler() { return inputHandler; }
}

